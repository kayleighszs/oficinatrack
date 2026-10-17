# OficinaTrack

API REST para acompanhar atendimentos de uma oficina mecânica, desde o cadastro do cliente até a entrega do veículo.

## Requisitos

- Java 21
- Docker e Docker Compose

## Execução com Docker

Copie `.env.example` para `.env`, troque todas as credenciais e defina um `JWT_SECRET` com pelo menos 32 caracteres.

```bash
docker compose up --build
```

A API fica disponível em `http://localhost:8080`. A documentação local fica em `http://localhost:8080/swagger-ui.html`.

## Execução local

Suba somente o PostgreSQL e execute a aplicação:

```bash
docker compose up -d db
set -a
source .env
set +a
./mvnw spring-boot:run
```

## Testes

```bash
./mvnw verify
```

O projeto usa PostgreSQL, Flyway, Spring Security com JWT, OpenAPI e JaCoCo.

## Fluxo principal

1. Cadastre cliente e veículo.
2. Abra a OS e inicie o diagnóstico.
3. Inclua serviços e peças.
4. Calcule e envie o orçamento.
5. Emita um acesso público para o cliente.
6. O cliente consulta e decide o orçamento usando esse token.
7. Finalize o serviço e confirme a entrega.

Rotas administrativas usam `Authorization: Bearer <jwt>`. Rotas sob `/publico` usam o token exclusivo da OS, também como Bearer. Os contratos completos ficam disponíveis no Swagger.

Consultas administrativas:

- `GET /ordens-servico` aceita status, cliente, veículo, período e paginação.
- `GET /ordens-servico/{id}` retorna a OS completa.
- `GET /relatorios/tempo-medio-execucao` retorna a média entre execução e finalização.

Tokens públicos são exibidos somente na emissão. Guarde o valor entregue ao cliente; o banco armazena apenas seu hash.

## Qualidade

`./mvnw verify` executa testes unitários e integrados em PostgreSQL via Testcontainers. O build falha quando a cobertura das regras de domínio e aplicação fica abaixo de 80% das linhas.

## Documentação do domínio

Diagramas e mapeamento dos épicos estão no diretório `docs/`.
