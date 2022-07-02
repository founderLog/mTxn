package com.github.mtxn.datasource.config;

import com.github.mtxn.entity.enums.DataSourceType;

public interface DataSourceConfig {
    String getName();

    String getDbName();

    String getUsername();

    String getPassword();

    String getHost();

    Integer getPort();

    DataSourceType getType();

    String getJdbcUrl();

    String getDriverClass();

    String getTestSql();

    Integer getMinIdle();

    Integer getMaxPoolSize();

    Long getConnectionTimeout();
}