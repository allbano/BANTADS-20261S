package br.dac.bantads.ms_auth.infrastructure.persistence.scylladb;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScyllaSchemaInitializer {

    @Bean
    public ApplicationRunner initScyllaSchema(CqlSession cqlSession) {
        return args -> cqlSession.execute(
                """
                CREATE TABLE IF NOT EXISTS user_accounts (
                    email text PRIMARY KEY,
                    password_hash text,
                    account_role text
                );
                """
        );
    }
}
