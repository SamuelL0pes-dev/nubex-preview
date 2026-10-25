package br.edu.unifor.nubex.usuario;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Sobe um PostgreSQL 16 de verdade, em container, só para os testes.
 *
 * Por que um Postgres real e não um banco em memória: a nossa migração usa
 * recursos exclusivos do PostgreSQL — índice único parcial
 * (WHERE matricula IS NOT NULL), o operador de expressão regular (cpf ~ '...')
 * e índice funcional sobre LOWER(email). O H2 não executa nenhum dos três, e
 * com ddl-auto=validate o teste precisa que a migração rode de verdade.
 *
 * O @ServiceConnection é a peça central: ele faz o Spring Boot apontar o
 * spring.datasource.* para este container automaticamente, com precedência
 * sobre o que estiver no application-dev.properties. Sem ele, o teste tentaria
 * o localhost:5433 da máquina de quem rodou — que não existe no CI.
 *
 * O container sobe numa porta aleatória, recebe a migração do Flyway num banco
 * vazio e é destruído no fim. Nada sobra na máquina e dois testes rodando ao
 * mesmo tempo não se atropelam.
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
