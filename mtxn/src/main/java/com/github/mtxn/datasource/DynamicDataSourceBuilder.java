package com.github.mtxn.datasource;

import com.github.mtxn.datasource.config.DataSourceConfig;
import com.github.mtxn.entity.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


@Component
@Slf4j
public class DynamicDataSourceBuilder implements BeanClassLoaderAware {


    private static ClassLoader classLoader;

    private DynamicDataSourceBuilder() {
    }

    public static javax.sql.DataSource create(DataSourceConfig config) {
        DataSourceBuilder<HikariDataSource> builder = DataSourceBuilder.create(classLoader).type(HikariDataSource.class)
                .username(config.getUsername()).password(config.getPassword());

        String jdbcUrl = String.format(config.getJdbcUrl(), config.getHost(), config.getPort(), config.getDbName());
        builder.driverClassName(config.getDriverClass()).url(jdbcUrl);

        HikariDataSource ds = builder.build();
        // 启动线程池不校验连接
        ds.setInitializationFailTimeout(-1);
        // 连接超时
        ds.setConnectionTimeout(config.getConnectionTimeout() != null ? config.getConnectionTimeout() : 30000L);
        // 最小连接池
        ds.setMinimumIdle(config.getMinIdle() != null ? config.getMinIdle() : 1);
        // 最大连接池
        ds.setMaximumPoolSize(config.getMinIdle() != null ? config.getMaxPoolSize() : 8);
        return ds;
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

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        DynamicDataSourceBuilder.classLoader = classLoader;
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
            log.warn("会话验证失败: {}", e.getMessage());
            return false;
        } finally {
//              释放资源  --- 由于 数据库 链接非常的稀缺, 所以 在 操作完成后,记得释放资源 , 都会放到 finally 代码块 中
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                resultSet = null;
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                statement = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                connection = null;
            }
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

    }
}