package br.dac.bantads.ms_conta.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Datasource do lado de CONSULTA (CQRS) — banco {@code conta_r}.
 *
 * Mantem a projecao denormalizada de leitura (ContaView), sincronizada por
 * RabbitMQ a partir do lado de Comando ({@link CudDataSourceConfig}). Datasource
 * NAO-primario; configurado via {@code spring.datasource-read} no application.yaml.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "br.dac.bantads.ms_conta.repository.read",
        entityManagerFactoryRef = "readEntityManagerFactory",
        transactionManagerRef = "readTransactionManager")
public class ReadDataSourceConfig {

    @Bean
    @ConfigurationProperties("read.datasource")
    public DataSource readDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean readEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("readDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("br.dac.bantads.ms_conta.model.read")
                .persistenceUnit("read")
                .build();
    }

    @Bean
    public PlatformTransactionManager readTransactionManager(
            @Qualifier("readEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
