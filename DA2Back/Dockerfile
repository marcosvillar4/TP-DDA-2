# syntax=docker/dockerfile:1.7
# ---------------------------------------------------------------------------
# DA2Back - imagen de produccion/demo (multi-stage)
#   Etapa 1: compila con Maven + JDK 17 (se omiten tests: los corre el pipeline de CI)
#   Etapa 2: JRE 17 minimo, usuario no-root
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
# Cache de ~/.m2 entre builds (BuildKit). -Djacoco.skip: no hace falta cobertura en la imagen.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests -Djacoco.skip=true clean package \
    && cp target/*.jar /build/app.jar

FROM eclipse-temurin:17-jre-alpine AS runtime
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build --chown=app:app /build/app.jar /app/app.jar
USER app
EXPOSE 8081
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
# /actuator/health exige rol ADMIN en SecurityConfig: se chequea que el puerto responda.
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=12 \
    CMD nc -z 127.0.0.1 8081 || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
