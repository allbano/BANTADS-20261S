package br.dac.bantads.ms_auth.infrastructure.configuration;

import br.dac.bantads.ms_auth.application.service.BaseTestAccountSeedService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseTestDataInitializer {

    @Bean
    public ApplicationRunner ensureBaseUsers(BaseTestAccountSeedService baseTestAccountSeedService) {
        return args -> baseTestAccountSeedService.ensureBaseTestAccounts();
    }
}
