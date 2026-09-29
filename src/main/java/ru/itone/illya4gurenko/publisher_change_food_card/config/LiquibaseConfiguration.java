package ru.itone.illya4gurenko.publisher_change_food_card.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfiguration {

    @Bean
    public SpringLiquibase oracleLiquibase(
            @Qualifier("oracleDataSource") DataSource dataSource) {

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(
                "classpath:db/changelog/oracle/db.changelog-master.yaml"
        );
        return liquibase;
    }

    @Bean
    public SpringLiquibase postgresLiquibase(
            @Qualifier("postgresDataSource") DataSource dataSource) {

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(
                "classpath:db/changelog/postgres/db.changelog-master.yaml"
        );
        return liquibase;
    }
}