package com.github.mtxn.config;

import com.alibaba.druid.filter.config.ConfigFilter;
import com.alibaba.druid.filter.encoding.EncodingConvertFilter;
import com.alibaba.druid.filter.logging.CommonsLogFilter;
import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.filter.logging.Log4jFilter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.github.mtxn.datasource.DruidDataSourceWrapper;
import com.github.mtxn.datasource.DynamicDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.PropertyElf;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

@Configuration
public class DataSourceConfiguration {

    @ConditionalOnProperty(prefix = "spring.datasource", name = "type", havingValue = "com.zaxxer.hikari.HikariDataSource")
    @Configuration
    @EnableAspectJAutoProxy
    @RequiredArgsConstructor
    public static class HikariDataSourceConfiguration {

        @Autowired
        private DataSourceProperties dataSourceConfigProperties;


        @Bean
        @ConfigurationProperties("spring.datasource.hikari")
        public Properties hikariProperties() {
            return new Properties();
        }


        /**
         * 动态数据源
         *
         * @return DynamicDataSource
         */
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        @Bean
        @Primary
        public DataSource dataSource(DataSourceProperties properties, Properties hikariProperties) {
            DataSource defaultDataSource = createDefaultDataSource(properties, hikariProperties);
            DynamicDataSource dynamicDataSource = new DynamicDataSource();
            dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
            return dynamicDataSource;
        }

        @SuppressWarnings("Duplicates")
        private DataSource createDefaultDataSource(DataSourceProperties properties, Properties hikariProperties) {


            HikariConfig config = new HikariConfig();

            PropertyElf.setTargetFromProperties(config, hikariProperties);

            if (hikariProperties.containsKey("connection-timeout")) {
                config.setConnectionTimeout(Long.parseLong(hikariProperties.getProperty("connection-timeout")));
            }
            if (hikariProperties.containsKey("maximum-pool-size")) {
                config.setMaximumPoolSize(Integer.parseInt(hikariProperties.getProperty("maximum-pool-size")));
            }
            if (hikariProperties.containsKey("minimum-idle")) {
                config.setMinimumIdle(Integer.parseInt(hikariProperties.getProperty("minimum-idle")));
            }
            if (hikariProperties.containsKey("idle-timeout")) {
                config.setIdleTimeout(Long.parseLong(hikariProperties.getProperty("idle-timeout")));
            }
            if (hikariProperties.containsKey("max-lifetime")) {
                config.setMaxLifetime(Long.parseLong(hikariProperties.getProperty("max-lifetime")));
            }
            if (hikariProperties.containsKey("connection-test-query")) {
                config.setConnectionTestQuery(hikariProperties.getProperty("connection-test-query"));
            }

            // 确保连接信息
            if (config.getUsername() == null) {
                config.setUsername(properties.determineUsername());
            }
            if (config.getPassword() == null) {
                config.setPassword(properties.determinePassword());
            }
            if (config.getJdbcUrl() == null) {
                config.setJdbcUrl(properties.determineUrl());
            }
            if (config.getDriverClassName() == null) {
                config.setDriverClassName(properties.getDriverClassName());
            }

            HikariDataSource defaultDataSource = new HikariDataSource(config);

            return defaultDataSource;
        }

    }

    @ConditionalOnProperty(prefix = "spring.datasource", name = "type", havingValue = "com.alibaba.druid.pool.DruidDataSource")
    @Configuration
    @EnableAspectJAutoProxy
    @RequiredArgsConstructor
    public class DruidDataSourceConfiguration {

        @Autowired(required = false)
        private List<StatFilter> statFilters;

        @Autowired(required = false)
        private List<ConfigFilter> configFilters;

        @Autowired(required = false)
        private List<EncodingConvertFilter> encodingConvertFilter;

        @Autowired(required = false)
        private List<Slf4jLogFilter> slf4jLogFilters;

        @Autowired(required = false)
        private List<Log4jFilter> log4jFilters;

        @Autowired(required = false)
        private List<Log4j2Filter> log4j2Filters;

        @Autowired(required = false)
        private List<CommonsLogFilter> commonsLogFilters;

        @Autowired(required = false)
        private List<WallFilter> wallFilters;

        @Bean
        @ConfigurationProperties("spring.datasource.druid")
        public Properties druidProperties() {
            return new Properties();
        }


        /**
         * 动态数据源
         *
         * @return DynamicDataSource
         */
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        @Bean
        @Primary
        public DataSource dataSource(DataSourceProperties properties, Properties druidProperties) {
            DataSource defaultDataSource = createDefaultDataSource(properties, druidProperties);
            DynamicDataSource dynamicDataSource = new DynamicDataSource();
            dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
            return dynamicDataSource;
        }

        @SuppressWarnings("Duplicates")
        private DataSource createDefaultDataSource(DataSourceProperties properties, Properties druidProperties) {
            DruidDataSourceWrapper defaultDataSource = new DruidDataSourceWrapper();
            // 赋值
            PropertyElf.setTargetFromProperties(defaultDataSource, druidProperties);
            // 过滤器
            if (statFilters != null) {
                statFilters.forEach(defaultDataSource::addStatFilter);
            }
            if (configFilters != null) {
                configFilters.forEach(defaultDataSource::addConfigFilter);
            }
            if (encodingConvertFilter != null) {
                encodingConvertFilter.forEach(defaultDataSource::addEncodingConvertFilter);
            }
            if (slf4jLogFilters != null) {
                slf4jLogFilters.forEach(defaultDataSource::addSlf4jLogFilter);
            }
            if (log4jFilters != null) {
                log4jFilters.forEach(defaultDataSource::addLog4jFilter);
            }
            if (log4j2Filters != null) {
                log4j2Filters.forEach(defaultDataSource::addLog4j2Filter);
            }
            if (commonsLogFilters != null) {
                commonsLogFilters.forEach(defaultDataSource::addCommonsLogFilter);
            }
            if (wallFilters != null) {
                wallFilters.forEach(defaultDataSource::addWallFilter);
            }
            // 确保连接信息
            if (defaultDataSource.getUsername() == null) {
                defaultDataSource.setUsername(properties.determineUsername());
            }
            if (defaultDataSource.getPassword() == null) {
                defaultDataSource.setPassword(properties.determinePassword());
            }
            if (defaultDataSource.getUrl() == null) {
                defaultDataSource.setUrl(properties.determineUrl());
            }
            if (defaultDataSource.getDriverClassName() == null) {
                defaultDataSource.setDriverClassName(properties.getDriverClassName());
            }
            return defaultDataSource;
        }
    }

}