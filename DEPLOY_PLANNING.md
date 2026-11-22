# API de Biblioteca: stack e planejamento de deploy

API REST para cadastro de autores, livros e leitores e para empréstimos com multa por atraso.
Este documento descreve a stack, mostra como rodar o projeto localmente e lista o que precisa
existir antes de um deploy em produção.

## 1. Stack

| Camada | Tecnologia | Versão | Licença |
|---|---|---|---|
| Linguagem | Java (OpenJDK) | 25 LTS | GPLv2 + Classpath Exception |
| Framework | Spring Boot | 4.2.0 | Apache-2.0 |
| ↳ Web | Spring Framework (Web MVC) e Tomcat embutido | 7.1.0 / 11.0.27 | Apache-2.0 |
| ↳ JSON | Jackson | 3.1.8 | Apache-2.0 |
| Persistência | Spring Data JPA + Hibernate ORM | 7.4.15 | Apache-2.0 |
| Validação | Jakarta Bean Validation + Hibernate Validator | 9.1.6 | Apache-2.0 |
| Banco de dados | PostgreSQL | 18 (imagem Docker) | PostgreSQL License |
| Driver | PostgreSQL JDBC | 42.7.14 | BSD-2-Clause |
| Migrations | Flyway | 12.11.0 | Apache-2.0 |
| Documentação | springdoc-openapi (Swagger UI) | 3.2.1 | Apache-2.0 |
| Build | Maven via Maven Wrapper (`./mvnw`) | Maven 3.10.1 | Apache-2.0 |
| Testes | JUnit Jupiter / Mockito | 6.1.3 / 5.24.0 | EPL-2.0 / MIT |
| Testes de integração | Testcontainers (PostgreSQL real) | 2.0.5 | MIT |
| Ambiente local | Docker + Docker Compose | — | — |

As versões gerenciadas pelo Spring Boot vêm do BOM `spring-boot-dependencies:4.2.0`. Na criação
do projeto (2026-11-22), nenhuma tinha CVE conhecida na base do OSV.

## 2. Arquitetura

Monólito modular, com um pacote por contexto (`autor`, `livro`, `leitor`, `emprestimo`) e um
pacote `compartilhado`. Dentro de cada contexto, as camadas seguem Clean Architecture:

| Camada | Conteúdo | Depende de |
|---|---|---|
| `dominio` | Regras de negócio puras, sem framework (ex.: `Emprestimo.devolver()`), e portas de repositório | nada |
| `aplicacao` | Casos de uso (services) | domínio |
| `api` | Controllers REST e DTOs (`record` com Builder) | aplicação |
| `infraestrutura` | Entidades JPA, repositórios Spring Data e adaptadores que implementam as portas | domínio |

Decisões que importam para a operação:

- **Erros:** todos seguem o formato `ProblemDetail` (RFC 9457). Um erro 500 nunca expõe stack trace.
- **Concorrência:** a baixa de estoque é um `UPDATE` atômico (`... WHERE quantidade_disponivel > 0`),
  então o estoque nunca fica negativo, mesmo com pedidos simultâneos.
- **Idempotência:**
  - Um índice único parcial impede dois empréstimos ativos do mesmo livro para o mesmo leitor.
    Repetir o pedido devolve o empréstimo existente com 200.
  - A devolução é condicional. Repetir a chamada devolve o resultado original, sem recalcular a
    multa e sem repor o exemplar duas vezes.
- **N+1:** a listagem de livros usa sempre 2 consultas (ids da página + `JOIN FETCH` dos autores).
- **Multa (Strategy):** livro `COMUM` paga R$ 1,00 por dia de atraso e livro `RARO` paga
  R$ 5,00 por dia, com teto de 30 dias. O prazo padrão é de 14 dias, e "hoje" segue o fuso
  `America/Recife`.
- **Proteção de dados:** o driver é configurado para não incluir na mensagem de erro o valor que
  violou uma constraint. Assim, um e-mail duplicado não vai parar no log.

## 3. Endpoints

| Recurso | Endpoints |
|---|---|
| Autores | `POST /autores`, `GET /autores`, `GET /autores/{id}`, `PUT /autores/{id}`, `DELETE /autores/{id}` |
| Livros | `POST /livros`, `GET /livros?titulo=&autorId=`, `GET /livros/{id}`, `PUT /livros/{id}`, `DELETE /livros/{id}` |
| Leitores | `POST /leitores`, `GET /leitores`, `GET /leitores/{id}` |
| Empréstimos | `POST /emprestimos`, `GET /emprestimos?status=ATIVO\|DEVOLVIDO`, `GET /emprestimos/{id}`, `PATCH /emprestimos/{id}/devolucao` |

- **Paginação:** as listagens aceitam `page` (começa em 0) e `size` (padrão 20, máximo 100).
- **Histórico de empréstimos:** não pode ser editado nem apagado. `PUT` e `DELETE` em
  `/emprestimos/{id}` retornam 405.

A documentação interativa fica em `/swagger-ui.html` e a especificação em `/v3/api-docs`, só nos
profiles `dev` e `test`.

## 4. Configuração e profiles

Cada profile Maven ativa o profile Spring de mesmo nome. O padrão é `dev`.

| Profile | Uso | Banco | Swagger |
|---|---|---|---|
| `dev` | Desenvolvimento local | PostgreSQL do `compose.yaml`, credenciais do `.env` | ligado |
| `test` | Testes automatizados | PostgreSQL em container (Testcontainers) | ligado |
| `prod` | Produção | Tudo por variáveis de ambiente | desligado |

| Variável | Profiles | Obrigatória | Descrição |
|---|---|---|---|
| `DB_USUARIO` | dev, prod | sim | Usuário do banco usado pela aplicação |
| `DB_SENHA` | dev, prod | sim | Senha desse usuário |
| `DB_NOME` | dev | não (padrão `biblioteca`) | Nome do banco local |
| `DB_PORTA` | dev | não (padrão `5432`) | Porta do PostgreSQL local |
| `DB_URL` | prod | sim | URL JDBC, ex.: `jdbc:postgresql://host:5432/biblioteca` |
| `DB_MIGRACAO_USUARIO` | prod | sim | Usuário com permissão de DDL, usado só pelo Flyway |
| `DB_MIGRACAO_SENHA` | prod | sim | Senha do usuário de migração |

Nenhum segredo fica versionado. O arquivo `.env` está no `.gitignore`, e o modelo sem valores é o
`.env.example`.

## 5. Mini tutorial: rodando localmente

### Pré-requisitos
- Java 25 (`java -version`).
- Docker com o Compose. No WSL, seu usuário precisa conseguir rodar `docker ps` sem `sudo`.
- Não é preciso instalar Maven: o projeto usa o Maven Wrapper (`./mvnw`).

### Passo a passo
Rode todos os comandos na raiz do projeto. O profile `dev` lê o `.env` dessa pasta.

1. Crie o `.env` a partir do modelo:

   ```bash
   cp .env.example .env
   ```

2. Edite o `.env` e preencha `DB_USUARIO` e `DB_SENHA` com valores locais quaisquer. Se a porta
   5432 já estiver em uso na sua máquina, mude também `DB_PORTA`.

3. Suba o PostgreSQL:

   ```bash
   docker compose up -d
   ```

4. Suba a API, que vai aplicar as migrations V1 e V2 sozinha:

   ```bash
   ./mvnw spring-boot:run
   ```

   O log deve mostrar `The following 1 profile is active: "dev"` e
   `Successfully applied 2 migrations`.

5. Abra o Swagger UI em <http://localhost:8080/swagger-ui.html>.

### Testando o fluxo completo
Os dados abaixo são fictícios.

```bash
curl -s -X POST localhost:8080/autores -H 'Content-Type: application/json' -d '{"nome":"Machado de Assis","nacionalidade":"Brasileira"}'
```

```bash
curl -s -X POST localhost:8080/livros -H 'Content-Type: application/json' -d '{"titulo":"Dom Casmurro","isbn":"978-0-306-40615-7","anoPublicacao":1899,"categoria":"RARO","quantidadeTotal":2,"autorIds":[1]}'
```

```bash
curl -s -X POST localhost:8080/leitores -H 'Content-Type: application/json' -d '{"nome":"Leitor Ficticio","email":"leitor@exemplo.test"}'
```

```bash
curl -s -X POST localhost:8080/emprestimos -H 'Content-Type: application/json' -d '{"livroId":1,"leitorId":1}'
```

```bash
curl -s -X PATCH localhost:8080/emprestimos/1/devolucao
```

Resultados esperados:
- O empréstimo volta com status 201, `status: "ATIVO"` e devolução prevista para daqui a 14 dias.
- A devolução volta com `status: "DEVOLVIDO"` e `multa: 0.00`, porque foi dentro do prazo.

### Rodando os testes
Os testes precisam do Docker no ar, porque os de integração sobem um PostgreSQL próprio. O banco
do `compose.yaml` não é usado.

```bash
./mvnw verify
```

### Parando tudo
Para parar a API, use `Ctrl+C` no terminal onde ela está rodando. Para parar o banco, mantendo
os dados:

```bash
docker compose down
```

Para apagar também os dados locais:

```bash
docker compose down -v
```

### Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| `Bind for 0.0.0.0:5432 failed: port is already allocated` | Outro PostgreSQL usando a porta 5432 | Mude `DB_PORTA` no `.env` |
| `password authentication failed` | A API está conectando num PostgreSQL diferente do compose, ou o `.env` mudou depois que o volume foi criado | Confira `DB_PORTA`. Se mudou usuário ou senha, rode `docker compose down -v` e suba de novo |
| `permission denied ... docker.sock` | Usuário fora do grupo `docker` | Rode `sudo usermod -aG docker $USER` e reinicie o WSL (`wsl.exe --shutdown`) |
| Testes de integração falham ao iniciar | Docker não está rodando | Inicie o Docker e rode os testes de novo |

## 6. Planejamento de deploy em produção

### Build
O artefato é um jar executável.

```bash
./mvnw -Pprod clean verify
```

```bash
java -jar target/api-biblioteca-0.0.1-SNAPSHOT.jar
```

Antes de rodar, defina as variáveis de ambiente da seção 4 (`DB_URL`, `DB_USUARIO`, `DB_SENHA`,
`DB_MIGRACAO_USUARIO`, `DB_MIGRACAO_SENHA`), de preferência a partir de um cofre de segredos.

### Banco de dados (princípio do menor privilégio)
A aplicação e as migrations usam credenciais separadas:

- **Usuário de migração:** dono do schema. O Flyway usa esse usuário ao subir a aplicação para
  criar e alterar tabelas.
- **Usuário da aplicação:** só lê e grava dados, sem DDL.

Exemplo de concessão de permissões. Os nomes são ilustrativos, e o comando deve rodar conectado
como o usuário de migração:

```sql
GRANT USAGE ON SCHEMA public TO biblioteca_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO biblioteca_app;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO biblioteca_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO biblioteca_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE ON SEQUENCES TO biblioteca_app;
```
