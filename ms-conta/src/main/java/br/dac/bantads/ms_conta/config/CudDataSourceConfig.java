package br.dac.bantads.ms_conta.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Datasource do lado de COMANDO (CQRS) — banco {@code conta_cud}.
 *
 * Mantem as entidades de escrita (ContaModel, MovimentacaoModel) e e o
 * datasource PRIMARIO do servico. As leituras usam o datasource separado
 * configurado em {@link ReadDataSourceConfig} (banco {@code conta_r}).
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "br.dac.bantads.ms_conta.repository.cud",
        entityManagerFactoryRef = "cudEntityManagerFactory",
        transactionManagerRef = "cudTransactionManager")
public class CudDataSourceConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource cudDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean cudEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("cudDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("br.dac.bantads.ms_conta.model.cud")
                .persistenceUnit("cud")
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager cudTransactionManager(
            @Qualifier("cudEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
