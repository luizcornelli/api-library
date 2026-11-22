# Como este projeto foi desenvolvido com IA

Este projeto foi construído com um assistente de IA para código (Claude Code), trabalhando sob um
conjunto de instruções que eu escrevi e mantenho. A IA executou a maior parte da digitação. As
decisões, as regras e a validação ficaram comigo.

Este documento explica os princípios, o fluxo de trabalho e os mecanismos de controle que usei, e
mostra exemplos reais deste repositório.

> **Princípio central:** eu terceirizo trabalho para a IA, não responsabilidade. Tudo o que foi
> entregue eu preciso conseguir entender, explicar e defender.

---

## 1. Princípios de engenharia que guiam a IA

As instruções do projeto incluem os meus princípios técnicos, destilados de um baralho de estudo
próprio, para a IA decidir do jeito que eu decidiria:

- **Não existe bala de prata.** Toda proposta precisa dizer do que estamos abrindo mão (tempo,
  memória, simplicidade, contexto).
- **Simples primeiro.** KISS, YAGNI e DRY antes de qualquer padrão sofisticado.
- **Critérios antes de código.** Meu modelo de esforço é 10% critérios de aceitação, 80%
  processamento da IA e 10% validação humana.
- **Padrões abertos e baixo acoplamento com fornecedores.** HTTP, SQL, RFC 9457, OpenAPI e
  Jakarta Persistence em vez de soluções proprietárias.
- **Modelo rico onde há regra de negócio**, retorno antecipado em vez de `else`, e monólito
  modular antes de microsserviços.
- **Comunicação objetiva:** respostas curtas, com detalhe só quando eu peço.

## 2. Fluxo de trabalho: a IA não escreve código sem aprovação

Toda atividade segue etapas fixas. A IA para ao fim de cada uma até eu responder:

```
branch → /criterios → /solucoes → /implementar → /commit
```

| Etapa | O que a IA faz | O que eu faço |
|---|---|---|
| `/criterios` | Transforma a descrição em critérios de aceitação **Dado / Quando / Então**. Marca cada um como `[descrição]` ou `[inferido]` e lista lacunas, contradições e redundâncias, cada uma com uma resposta sugerida | Confirmo ou corrijo cada dúvida |
| `/solucoes` | Faz perguntas até ter **98% de confiança**, com um checklist visível (regra de negócio, local do código, dados, integração, perfil, erro). Depois propõe **uma** solução, com trade-offs, custo, risco e alternativas descartadas | Aprovo, ajusto ou escolho outro caminho |
| `/implementar` | Cobre cada critério com teste, no máximo 3 tentativas por falha, sem afrouxar asserções. Termina com uma tabela critério → teste → resultado | Valido o resultado e as suposições declaradas |
| `/commit` | Agrupa as mudanças em commits granulares por contexto e mostra o plano antes de executar | Aprovo o plano. Push só manual |

Regras que valem em todas as etapas:
- **Perguntar em vez de supor.** Quando precisa assumir algo, a IA diz o que assumiu e pede
  confirmação.
- **Voltar à etapa certa.** Se a implementação revela um critério errado ou uma solução
  inviável, a IA volta só à etapa afetada, em vez de ajustar por conta própria.
- **Menor mudança possível.** Nada de refatorar, renomear ou reformatar fora do escopo.
- **Explicar cada trecho alterado.** Trecho que não pode ser explicado é reescrito ou removido.

## 3. Mecanismos de controle (guardrails)

| Área | Regra configurada |
|---|---|
| **Segredos** | A leitura de arquivos que costumam guardar segredos (`.env`, `application*.yml`, chaves, certificados) é **bloqueada** para a IA por configuração, e ela não pode contornar o bloqueio |
| **Proteção de dados (LGPD)** | Nenhum dado sensível em respostas, logs, testes, commits ou documentação. Os testes usam dados evidentemente fictícios |
| **Menor privilégio** | Endpoint novo começa bloqueado. O banco usa credenciais separadas para a aplicação e para as migrations. A IA não usa `sudo` |
| **Git** | Bloqueados por configuração: `push`, `--amend`, `--no-verify`, `rebase`, `reset --hard` e `git add .` |
| **Dependências** | Nenhuma dependência nova entra sem versão, licença, data de release e CVEs **consultados na hora** em fonte oficial, e sem a minha autorização |
| **Idempotência** | Toda operação repetível precisa ter efeito único (retry, timeout, clique duplo) |
| **Race conditions** | Todo "ler, decidir e gravar" sobre recurso limitado é protegido na camada de persistência |
| **Economia de tokens** | Leituras localizadas, saídas limitadas e buscas amplas delegadas, para manter o contexto focado |

## 4. Padrões de código e de teste exigidos

- **Arquitetura:** Clean Architecture e DDD, com domínio sem dependência de framework, SOLID e
  Object Calisthenics.
- **Limites para código novo:** até 5 linhas por método, 1 nível de indentação, 3 parâmetros e
  1 dependência injetada utilizada.
- **Convenções:** nomes em português, parâmetros com prefixo `p` e um comentário de propósito
  em cada método.
- **Testes:** toda feature tem teste que prova o critério, com nome iniciado por `deve`
  (ex.: `deveEmprestarUltimoExemplarParaApenasUmQuandoDoisPedidosChegamJuntos`).

## 5. Evidências neste repositório

**Decisões que foram minhas, não da IA:**
- **Domínio puro** em vez de usar as entidades JPA como domínio. A IA sugeriu o caminho mais
  simples, mostrou o trade-off e eu escolhi seguir a Clean Architecture à risca.
- **Fuso de Recife** para calcular "hoje" nos empréstimos e nas multas.
- **Nada de "CRUD" de empréstimos.** A IA apontou que editar ou apagar empréstimos destruiria o
  histórico, e eu decidi que essas chamadas retornam 405.
- **Nome de pacote.** Pedi `com.api-biblioteca`, a IA alertou que hífen é inválido em pacote
  Java, e seguimos com `com.apibiblioteca`.

**Números da primeira atividade (a API):**
- 31 critérios de aceitação e 12 dúvidas resolvidas antes de qualquer código.
- 11 perguntas de implementação respondidas no `/solucoes`.
- 82 testes automatizados: unitários com Mockito, `@WebMvcTest` e integração com PostgreSQL real
  via Testcontainers.
- Concorrência testada com threads de verdade: o último exemplar disputado por 2 pedidos
  simultâneos vai para um único leitor.
- 7 commits granulares por contexto (projeto, banco, compartilhado, autor, leitor, livro,
  empréstimo).

**Um problema real que os testes pegaram:** o critério "o e-mail do leitor não aparece em logs"
ganhou um teste que captura a saída da aplicação. O teste falhou e revelou que o driver do
PostgreSQL inclui o valor duplicado na mensagem de erro, que o Hibernate grava no log. A correção
foi feita na origem (`logServerErrorDetail=false`) e o teste passou. Sem o critério escrito antes,
esse vazamento passaria despercebido.

**Dependências verificadas na hora:** as versões do Spring Boot 4.2.0 e das bibliotecas foram
conferidas no Maven Central, e as CVEs na base do OSV, antes de entrarem no projeto.

## 6. Transparência: a IA também reporta onde não seguiu a regra

Ao fim da implementação, a IA lista os desvios em vez de escondê-los. Exemplos registrados neste
projeto:
- Os testes foram escritos por contexto, logo depois do código, e não um a um antes de cada
  critério.
- Alguns métodos de orquestração usam mais de uma dependência injetada.
- Um construtor de entidade JPA passa do limite de 5 linhas.

Eu decido se cada desvio é aceitável ou se precisa de correção. Esse é o papel dos 10% de
validação humana.
