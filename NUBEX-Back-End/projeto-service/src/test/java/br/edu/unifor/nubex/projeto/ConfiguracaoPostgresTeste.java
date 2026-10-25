package br.edu.unifor.nubex.projeto;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Sobe um PostgreSQL 16 de verdade, em container, só para os testes.
 * Mesma configuração do usuario-service, de propósito: os dois serviços
 * testam do mesmo jeito.
 *
 * O @ServiceConnection faz o Spring Boot apontar o spring.datasource.* para
 * este container automaticamente, com precedência sobre o application.properties
 * e o application-dev.properties. É o que permite rodar os testes no CI, onde
 * não existe banco local nem arquivo de credencial (os dois são gitignorados).
 *
 * Este serviço ainda não tem migração nem entidade. Quando tiver, o Flyway vai
 * aplicá-la neste container e o ddl-auto=validate vai conferir as entidades
 * contra o schema resultante, sem nenhuma mudança aqui.
 *
 * ATENÇÃO para quem for copiar isto de tutorial: o Spring Boot 4.1.1 fixa o
 * Testcontainers 2.0.5, e na linha 2.x o PostgreSQLContainer deixou de ser
 * genérico. O "new PostgreSQLContainer<>(...)" dos exemplos da 1.x não compila.
 */
@TestConfiguration(proxyBeanMethods = false)
class ConfiguracaoPostgresTeste {

    /** Mesma imagem do docker-compose.yml, para o teste usar o que vai para produção. */
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:16");
    }
}
