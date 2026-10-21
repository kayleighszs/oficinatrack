# ---- Build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copia só o wrapper e o pom primeiro para aproveitar o cache de dependências
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src
# Os testes usam Testcontainers (exigem Docker), por isso ficam fora do build da imagem
RUN ./mvnw -q -DskipTests package

# ---- Runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /workspace/target/oficinatrack-*.jar app.jar
USER app

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
