package me.ai.training.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * @author Rohit Muneshwar
 * @created on 2/18/2026
 *
 *
 */
@Configuration
@Slf4j
public class DataSourceConfig {
    @Autowired
    private Environment environment;
    private final String H2_DATASOURCE_PREFIX = "spring.custom.h2.datasource";
    private final String SQLITE_DATASOURCE_PREFIX = "spring.custom.sqlite.datasource";
    private final String POSTGRES_DATASOURCE_PREFIX = "spring.custom.postgres.datasource";

    @Bean
    @ConditionalOnBooleanProperty(prefix = SQLITE_DATASOURCE_PREFIX, name = "enabled")
    public DataSource sqliteDataSource(){
        log.info("Creating SQLite datasource");
        DriverManagerDataSource dataSource= new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(environment.getProperty(SQLITE_DATASOURCE_PREFIX+".driverClassName")));
        dataSource.setUrl(Objects.requireNonNull(environment.getProperty(SQLITE_DATASOURCE_PREFIX+".url")));
        dataSource.setUsername(Objects.requireNonNull(environment.getProperty(SQLITE_DATASOURCE_PREFIX+".username")));
        dataSource.setPassword(Objects.requireNonNull(environment.getProperty(SQLITE_DATASOURCE_PREFIX+".password")));
        return dataSource;
    }

    @Bean
    @ConditionalOnBooleanProperty(prefix = "spring.h2.console", name = "enabled")
    public DataSource h2DataSource(){
        log.info("Creating h2 datasource");
        DriverManagerDataSource dataSource= new DriverManagerDataSource();

        dataSource.setDriverClassName(Objects.requireNonNull(environment.getProperty(H2_DATASOURCE_PREFIX+".driverClassName")));
        dataSource.setUrl(Objects.requireNonNull(environment.getProperty(H2_DATASOURCE_PREFIX+".url")));
        dataSource.setUsername(Objects.requireNonNull(environment.getProperty(H2_DATASOURCE_PREFIX+".username")));
        dataSource.setPassword(Objects.requireNonNull(environment.getProperty(H2_DATASOURCE_PREFIX+".password")));
        return dataSource;
    }

    @Bean
    @ConditionalOnBooleanProperty(prefix = POSTGRES_DATASOURCE_PREFIX, name = "enabled")
    public DataSource postgresDataSource(){
        log.info("Creating Postgres datasource");
        DriverManagerDataSource dataSource= new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(environment.getProperty(POSTGRES_DATASOURCE_PREFIX+".driverClassName")));
        dataSource.setUrl(Objects.requireNonNull(environment.getProperty(POSTGRES_DATASOURCE_PREFIX+".url")));
        dataSource.setUsername(Objects.requireNonNull(environment.getProperty(POSTGRES_DATASOURCE_PREFIX+".username")));
        dataSource.setPassword(Objects.requireNonNull(environment.getProperty(POSTGRES_DATASOURCE_PREFIX+".password")));
        return dataSource;
    }
}
