FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /build
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY nubex-common nubex-common
COPY usuario-service usuario-service
COPY projeto-service projeto-service
COPY agendamento-service agendamento-service
COPY catalogo-service catalogo-service
COPY conteudo-service conteudo-service
# Os testes rodam separadamente no CI; a imagem constrói os módulos do reactor.
RUN sh mvnw -B -ntp -DskipTests package

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 nubex \
    && useradd --uid 10001 --gid nubex --no-create-home nubex
WORKDIR /app
ARG SERVICE
COPY --from=build --chown=nubex:nubex /build/${SERVICE}/target/${SERVICE}-*.jar /app/app.jar
USER nubex
ENV SERVER_PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
