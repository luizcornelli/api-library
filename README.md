# API de Biblioteca

API REST em Java 25 e Spring Boot 4 para autores, livros, leitores e empréstimos, com multa por
atraso calculada por categoria de livro.

## Documentação

- **[DEPLOY_PLANNING.md](DEPLOY_PLANNING.md):** stack, arquitetura, endpoints, configuração por
  profile, tutorial para rodar localmente e planejamento de deploy em produção.
- **[DESENVOLVIMENTO_COM_IA.md](DESENVOLVIMENTO_COM_IA.md):** princípios, fluxo de trabalho e
  controles usados para desenvolver o projeto com IA.

## Início rápido

Pré-requisitos: Java 25 e Docker. O passo a passo completo está em
[DEPLOY_PLANNING.md](DEPLOY_PLANNING.md#5-mini-tutorial-rodando-localmente).

```bash
cp .env.example .env   # preencha DB_USUARIO e DB_SENHA
docker compose up -d
./mvnw spring-boot:run
```

Com a API no ar, a documentação interativa fica em <http://localhost:8080/swagger-ui.html>.
