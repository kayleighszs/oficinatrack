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

## Documentação do domínio

Diagramas e mapeamento dos épicos estão no diretório `docs/`.
