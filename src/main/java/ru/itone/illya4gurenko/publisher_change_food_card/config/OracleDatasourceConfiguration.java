package ru.itone.illya4gurenko.publisher_change_food_card.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class OracleDatasourceConfiguration {
    // Создает и привязывает свойства подключения к БД Oracle
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.oracle")
    public DataSourceProperties oracleDataSourceProperties() {
        return new DataSourceProperties();
    }

    // Создает и настраивает Primary пул соединений DataSource для Oracle
    @Bean(name = "oracleDataSource")
    @Primary
    public DataSource oracleDataSource() {
        return oracleDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

}
