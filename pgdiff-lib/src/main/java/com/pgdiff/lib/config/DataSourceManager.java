package com.pgdiff.config;

import com.pgdiff.model.DatabaseSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Log4j2
@Component
public class DataSourceManager {

    /**
     * Подключение источника БД
     * @return возвращает источник БД
     */
    @Bean
    public DataSource getDataSource()
    {
        try{
            return createHikariDataSource(new DatabaseSettings());
        }catch (Exception e){
            log.info(e.getMessage());
            return null;
        }
    }

    /**
     * Созданее пула подключений к указанной БД
     *
     * @param settings парамметры подключения к БД
     * @return пул подключений к БД
     */
    public DataSource createHikariDataSource(DatabaseSettings settings) throws Exception
    {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(settings.getDriver());
        hikariConfig.setJdbcUrl(settings.getUrl());
        hikariConfig.setUsername(settings.getUsername());
        hikariConfig.setPassword(settings.getPassword());
        hikariConfig.setMaximumPoolSize(settings.getPoolSize());
        hikariConfig.setPoolName("main");
        return new HikariDataSource(hikariConfig);
    }



}
