package ru.itone.illya4gurenko.publisher_change_food_card.config;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "ru.itone.illya4gurenko.publisher_change_food_card.oracle.repository",
        entityManagerFactoryRef = "oracleEntityManagerFactory",
        transactionManagerRef = "oracleTransactionManager"
)
public class OracleJpaConfiguration {
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean oracleEntityManagerFactory(
            @Qualifier("oracleDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        return builder
                .dataSource(dataSource)
                .packages("ru.itone.illya4gurenko.publisher_change_food_card.oracle.entity")
                .persistenceUnit("oracle")
                .properties(properties)
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager oracleTransactionManager(
            @Qualifier("oracleEntityManagerFactory") LocalContainerEntityManagerFactoryBean oracleEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(oracleEntityManagerFactory.getObject()));
    }
}
