# OficinaTrack

API REST para acompanhar os atendimentos de uma oficina mecânica, do cadastro do cliente até a entrega do veículo.

## Objetivos

Uma oficina costuma controlar ordens de serviço, peças e orçamentos em papel ou planilhas, e o cliente fica sem saber em que pé está o seu carro. O OficinaTrack centraliza esse fluxo e tem quatro objetivos:

- **Organizar o atendimento:** cadastrar clientes e veículos e abrir uma Ordem de Serviço (OS) que passa por status bem definidos (recebida, em diagnóstico, aguardando aprovação, em execução, finalizada, entregue ou orçamento recusado), com histórico de cada mudança.
- **Controlar orçamento e estoque:** montar o orçamento com serviços do catálogo e peças do estoque, com total calculado pelo backend, e dar baixa no estoque de forma atômica quando o cliente aprova.
- **Dar transparência ao cliente:** gerar um acesso público e exclusivo para o cliente consultar a sua OS e aprovar ou recusar o orçamento, sem precisar de login.
- **Apoiar a gestão:** consultas filtráveis de OS e relatórios, como o tempo médio de execução.

### Perfis de acesso

| Perfil | O que faz |
|---|---|
| `GESTOR` | Acesso total, inclusive cadastro e movimentação de estoque e relatórios |
| `ATENDENTE` | Cadastra clientes e veículos, abre OS, cria e envia orçamentos, emite o acesso público e confirma a entrega |
| `MECANICO` | Consulta dados, registra o diagnóstico, inclui serviços e peças e finaliza a OS |
| Cliente (público) | Consulta a própria OS e decide o orçamento com um token exclusivo, em `/publico` |

## Tecnologias

Java 21, Spring Boot 4, Spring Security com JWT, Spring Data JPA, PostgreSQL 16, Flyway, MapStruct, springdoc (OpenAPI/Swagger), Testcontainers, JaCoCo e Docker.

## Como rodar (recomendado: Docker)

É o caminho mais simples. **Você só precisa do Docker.** Não precisa instalar Java, Maven nem PostgreSQL.

**Pré-requisito:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows/Mac) ou Docker Engine com Compose v2 (Linux), **aberto e em execução**.

```bash
# 1. Baixe o projeto
git clone https://github.com/kayleighszs/oficinatrack.git
cd oficinatrack

# 2. Crie o arquivo de configuração (os valores do exemplo já funcionam)
cp .env.example .env          # no PowerShell: Copy-Item .env.example .env

# 3. Suba o banco e a aplicação
docker compose up --build
```

Na primeira vez a imagem leva alguns minutos para ser construída. Quando aparecer `Started OficinatrackApplication`, está pronto:

| O quê | Endereço |
|---|---|
| Swagger (documentação interativa) | http://localhost:8080/swagger-ui.html |
| API | http://localhost:8080 |
| PostgreSQL (DBeaver, psql) | `localhost:5433`, banco `oficina`, usuário e senha `postgres` |

O banco é criado e as migrations do Flyway rodam sozinhas. Para parar tudo, use `Ctrl+C` ou, em outro terminal, `docker compose down`.

### Primeiro acesso

Na primeira execução são criados três usuários, a partir do `.env`:

| Usuário | Senha | Perfil |
|---|---|---|
| `gestor` | `troque-esta-senha` | GESTOR |
| `atendente` | `troque-esta-senha` | ATENDENTE |
| `mecanico` | `troque-esta-senha` | MECANICO |

Faça login para obter o token:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usuario":"gestor","senha":"troque-esta-senha"}'
```

A resposta traz `{"token":"..."}`. Use-o nas demais chamadas com o cabeçalho `Authorization: Bearer <token>`.
No Swagger, clique em **Authorize** e cole apenas o token.

> O token expira em 30 minutos (`JWT_EXPIRATION`). Em qualquer ambiente fora do seu computador, troque as senhas e o `JWT_SECRET` no `.env`.

### Comandos úteis

```bash
docker compose up -d --build   # sobe em segundo plano
docker compose logs -f app     # acompanha os logs da aplicação
docker compose ps              # estado e healthcheck dos serviços
docker compose down            # para os containers (mantém os dados)
docker compose down -v         # para e APAGA os dados do banco
```

### Configuração (`.env`)

Todas as variáveis têm exemplo em [.env.example](.env.example).

| Variável | Padrão | Descrição |
|---|---|---|
| `JWT_SECRET` | (obrigatória) | Chave de assinatura dos tokens, com no mínimo 32 caracteres |
| `JWT_EXPIRATION` | `1800000` | Validade do token, em milissegundos |
| `GESTOR_*`, `ATENDENTE_*`, `MECANICO_*` | vazio | Usuário e senha criados na primeira execução. Se vazios, o usuário não é criado |
| `PUBLIC_ACCESS_EXPIRATION_DAYS` | `7` | Validade do acesso público do cliente, em dias |
| `OPENAPI_ENABLED` | `true` | Liga ou desliga o Swagger |
| `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | `oficina`, `postgres`, `postgres` | Banco de dados |
| `DB_PORT` | `5433` | Porta do banco no seu computador |
| `APP_PORT` | `8080` | Porta da aplicação no seu computador |

## Rodando sem Docker para a aplicação (desenvolvimento)

Requer Java 21. O banco continua no Docker e a aplicação roda pela linha de comando ou pela IDE:

```bash
docker compose up -d db
set -a; source .env; set +a      # carrega as variáveis (Git Bash / Linux / Mac)
./mvnw spring-boot:run
```

No PowerShell, defina as variáveis manualmente (por exemplo `$env:JWT_SECRET="..."`) ou configure-as na sua IDE.

## Testes

```bash
./mvnw verify
```

Os testes de integração usam o Testcontainers, então precisam do **Docker em execução**. O build falha se a cobertura das regras de domínio e aplicação ficar abaixo de 80%.

## Fluxo principal da API

1. Cadastre o cliente (`POST /clientes`) e o veículo (`POST /veiculos`).
2. Abra a OS (`POST /ordens-servico`) e registre o diagnóstico.
3. Inclua serviços (`/itens-servico`) e peças (`/itens-peca`).
4. Crie e envie o orçamento (`/orcamentos`).
5. Emita o acesso público para o cliente (`POST /ordens-servico/{id}/acessos-publicos`).
6. O cliente consulta e decide o orçamento em `/publico/ordens-servico/{numeroOs}`, com o token recebido.
7. Finalize o serviço (`/finalizacoes`) e confirme a entrega (`/entregas`).

| Recurso | Rotas |
|---|---|
| Autenticação | `POST /auth/login` |
| Clientes e veículos | `/clientes`, `/veiculos` |
| Catálogo de serviços | `/servicos` |
| Estoque de peças | `/pecas` |
| Ordens de serviço | `/ordens-servico` |
| Acesso do cliente | `/ordens-servico/{id}/acessos-publicos`, `/publico/ordens-servico/{numeroOs}` |
| Relatórios | `GET /relatorios/tempo-medio-execucao` |

Os contratos completos (campos, validações e respostas) estão no Swagger. O token público aparece **uma única vez**, na emissão. O banco guarda apenas o hash dele.

## Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| `required variable JWT_SECRET is missing` | Falta o arquivo `.env`. Rode `cp .env.example .env` |
| `failed to connect to the docker API` | O Docker Desktop está fechado. Abra e aguarde o status "Engine running" |
| `port is already allocated` ou `ports are not available` | A porta está em uso. Mude `APP_PORT` ou `DB_PORT` no `.env` |
| `relation "..." already exists` ao subir | Sobrou um banco antigo de outra versão. Rode `docker compose down -v` e suba de novo (**apaga os dados**) |
| Login retorna 401 | Confira o JSON (`usuario` e `senha`, não `username` e `password`) e se os usuários foram definidos no `.env` antes da primeira execução |
| Mudei o `.env` e nada mudou | Os usuários só são criados se ainda não existirem. Para recriar do zero, use `docker compose down -v` |

## Estrutura do projeto

```
src/main/java/br/com/oficinatrack/
├── auth/              login, JWT e usuários
├── cliente/ veiculo/  cadastros
├── catalogoservicos/  catálogo de serviços
├── estoque/           peças e movimentações
├── ordemservico/      OS, itens, orçamento e histórico de status
├── publico/           acesso e consulta pública do cliente
├── relatorios/        consultas gerenciais
└── shared/            configuração, exceções e validações comuns
src/main/resources/db/migration/   migrations do Flyway
docs/                              diagramas e mapeamento dos épicos
```

## Documentação

Diagramas de domínio, fluxos de negócio e o mapeamento da API estão em [docs/](docs/). O passo a passo da OS está em [docs/ordem-servico.md](docs/ordem-servico.md).
