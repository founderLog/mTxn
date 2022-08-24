package com.github.mtxn.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.mtxn.application.Application;
import com.github.mtxn.datasource.config.DataSourceConfig;
import com.github.mtxn.entity.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


@Component
@Slf4j
public class DynamicDataSourceBuilder implements BeanClassLoaderAware {


    private static final int MIN_IDLE = 1;
    private static final int INITIAL_SIZE = 8;
    private static final int MAX_POOLSIZE = 8;
    private static final long CONNECTION_TIMEOUT = 30000L;
    private static ClassLoader classLoader;


    private DynamicDataSourceBuilder() {
    }

    public static javax.sql.DataSource createHikariDataSource(DataSourceConfig config) {
        DataSourceBuilder<HikariDataSource> builder = DataSourceBuilder.create(classLoader).type(HikariDataSource.class)
                .username(config.getUsername()).password(config.getPassword());

        String jdbcUrl = String.format(config.getJdbcUrl(), config.getHost(), config.getPort(), config.getDbName());
        builder.driverClassName(config.getDriverClass()).url(jdbcUrl);

        HikariDataSource ds = builder.build();
        // 启动线程池不校验连接
        ds.setInitializationFailTimeout(-1);
        // 连接超时
        ds.setConnectionTimeout(config.getConnectionTimeout() != null ? config.getConnectionTimeout() : CONNECTION_TIMEOUT);
        // 最小连接池
        ds.setMinimumIdle(config.getMinIdle() != null ? config.getMinIdle() : MIN_IDLE);
        // 最大连接池
        ds.setMaximumPoolSize(config.getMaxPoolSize() != null ? config.getMaxPoolSize() : MAX_POOLSIZE);
        return ds;
    }

    public static javax.sql.DataSource createDruidDataSource(DataSourceConfig config) {
        DataSourceBuilder<DruidDataSource> builder = DataSourceBuilder.create(classLoader).type(DruidDataSource.class)
                .username(config.getUsername()).password(config.getPassword());

        String jdbcUrl = String.format(config.getJdbcUrl(), config.getHost(), config.getPort(), config.getDbName());
        builder.driverClassName(config.getDriverClass()).url(jdbcUrl);

        DruidDataSource ds = builder.build();
        // 初始连接
        ds.setInitialSize(config.getMaxPoolSize() != null ? config.getMaxPoolSize() : INITIAL_SIZE);
        // 最小连接池
        ds.setMinIdle(config.getMinIdle() != null ? config.getMinIdle() : MIN_IDLE);
        // 最大连接池
        ds.setMaxActive(config.getMaxPoolSize() != null ? config.getMaxPoolSize() : MAX_POOLSIZE);
        ds.setMaxWait(CONNECTION_TIMEOUT);
        ds.setName(config.getName());
        try {
            ds.setFilters("stat");
            ds.setConnectionProperties("druid.stat.slowSqlMillis=1000");
        } catch (SQLException e) {
            log.error("创建数据源失败", e);
        }
        return ds;
    }

    /**
     * 创建com_datasource中的数据源，兼容hikari/druid
     *
     * @param config
     * @return
     */
    public static javax.sql.DataSource create(DataSourceConfig config) {
        DataSourceProperties dataSourceProperties = Application.resolve(DataSourceProperties.class);
        switch (dataSourceProperties.getType().getName()) {
            case "com.alibaba.druid.pool.DruidDataSource":
                return createDruidDataSource(config);
            case "com.zaxxer.hikari.HikariDataSource":
                return createHikariDataSource(config);

        }
        return null;
    }

    public static boolean testConnection(DataSourceConfig config, javax.sql.DataSource dataSource) {

        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(config.getTestSql())
        ) {
            return resultSet.next();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 验证配置是否有效
     *
     * @param dataSource
     * @return
     */
    public static boolean validDataSource(DataSource dataSource) {
        String jdbcUrl = String.format(dataSource.getJdbcUrl(), dataSource.getHost(), dataSource.getPort(), dataSource.getDbName());
        HikariDataSource ds = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            DataSourceBuilder<HikariDataSource> builder = DataSourceBuilder.create(classLoader).type(HikariDataSource.class)
                    .username(dataSource.getUsername()).password(dataSource.getPassword());

            builder.driverClassName(dataSource.getDriverClass()).url(jdbcUrl);
            ds = builder.build();
            // 连接超时
            ds.setConnectionTimeout(dataSource.getConnectionTimeout());
            // 最小连接池
            ds.setMinimumIdle(dataSource.getMinIdle());
            // 最大连接池
            ds.setMaximumPoolSize(dataSource.getMaxPoolSize());

            connection = ds.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(dataSource.getTestSql());
            return resultSet.next();
        } catch (Exception e) {
            log.warn("验证 {} 链接失败", jdbcUrl);
            if (log.isWarnEnabled()) {
                log.warn("会话验证失败: {}", e.getMessage());
            }
            return false;
        } finally {
//              释放资源  --- 由于 数据库 链接非常的稀缺, 所以 在 操作完成后,记得释放资源 , 都会放到 finally 代码块 中
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("数据库链接关闭失败:{}", e.getMessage());
                    }
                }
                resultSet = null;
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("数据库链接关闭失败:{}", e.getMessage());
                    }
                }
                statement = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("数据库链接关闭失败:{}", e.getMessage());
                    }
                }
                connection = null;
            }
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        DynamicDataSourceBuilder.classLoader = classLoader;
    }
}