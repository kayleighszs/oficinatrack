# Rodando com Docker

Pré-requisito: Docker Desktop (ou Docker Engine + Compose v2) instalado e **em execução**.

## Subir tudo (banco + aplicação)

```bash
cp .env.example .env      # opcional: os valores padrão já funcionam
docker compose up --build
```

- Aplicação: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Banco PostgreSQL: `localhost:5433` (usuário e senha em `.env`, padrão `postgres`/`postgres`)

A aplicação só inicia depois que o banco estiver saudável. As tabelas são criadas pelo Hibernate no primeiro start.

## Comandos úteis

```bash
docker compose up -d --build     # sobe em segundo plano
docker compose logs -f app       # acompanha os logs da aplicação
docker compose ps                # estado e healthcheck dos serviços
docker compose down              # para os containers (mantém os dados)
docker compose down -v           # para e APAGA o volume do banco
```

## Somente o banco (aplicação rodando pela IDE)

```bash
docker compose up -d db
./mvnw spring-boot:run
```

O banco fica em `localhost:5433` (a 5432 costuma estar ocupada por um PostgreSQL local), então informe a URL:

```bash
DB_URL=jdbc:postgresql://localhost:5433/oficina ./mvnw spring-boot:run
```

## Só a imagem da aplicação

```bash
docker build -t oficinatrack .
docker run --rm -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5433/oficina \
  -e DB_USERNAME=postgres -e DB_PASSWORD=postgres \
  oficinatrack
```

## Variáveis

| Variável | Padrão | Uso |
|---|---|---|
| `DB_NAME` | `oficina` | nome do banco |
| `DB_USERNAME` | `postgres` | usuário do banco |
| `DB_PASSWORD` | `postgres` | senha do banco |
| `DB_PORT` | `5433` | porta do banco no host |
| `APP_PORT` | `8080` | porta da aplicação no host |
| `JAVA_OPTS` | `-XX:MaxRAMPercentage=75` | opções da JVM no container |
