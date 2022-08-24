package com.github.mtxn.datasource;


import com.github.mtxn.manager.DataSourceManager;
import com.github.mtxn.transaction.MultiTransactionManager;
import com.github.mtxn.transaction.context.DataSourceContextHolder;
import com.github.mtxn.transaction.support.ConnectionProxy;
import com.github.mtxn.transaction.support.TransactionHolder;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 动态数据源
 */
@Slf4j
@Primary
public class DynamicDataSource extends AbstractDataSource implements DataSourceManager {

    private final ConcurrentMap<Integer, DataSource> dataSources;

    private DataSource defaultDataSource;


    public DynamicDataSource() {
        dataSources = new ConcurrentHashMap<>();
    }

    public void setDefaultTargetDataSource(DataSource defaultTargetDataSource) {
        this.defaultDataSource = defaultTargetDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        TransactionHolder transactionHolder = MultiTransactionManager.TRANSACTION_HOLDER_THREAD_LOCAL.get();
        if (Objects.isNull(transactionHolder)) {
            return determineTargetDataSource().getConnection();
        }
        ConnectionProxy connectionProxy = transactionHolder.getConnectionMap()
                .get(transactionHolder.getExecuteStack().peek());
        if (connectionProxy == null) {
            // 没开跨库事务，直接返回
            return determineTargetDataSource().getConnection();
        } else {
            // 开了跨库事务，从当前线程中拿包装过的Connection
            if (connectionProxy.isClosed()) {
                log.warn("get a closed connection,executeId:{},datasourceKey:{}",
                        transactionHolder.getExecuteStack().peek(), transactionHolder.getDatasourceKeyStack().peek());
            }
            return connectionProxy;
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineTargetDataSource().getConnection(username, password);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return determineTargetDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
    }

    public DataSource determineTargetDataSource() {
        String lookupKey = DataSourceContextHolder.getKey();
        DataSource dataSource = Optional.ofNullable(lookupKey)
                .map(dataSources::get)
                .orElse(defaultDataSource);
        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine DataSource for lookup key [" + lookupKey + "]");
        }
        return dataSource;
    }

    @Override
    public void put(Integer id, DataSource dataSource) {
        log.info("put DataSource: {}", id);
        dataSources.put(id, dataSource);
    }

    @Override
    public DataSource get(Integer id) {
        return dataSources.get(id);
    }

    @Override
    public Boolean hasDataSource(Integer id) {
        return dataSources.containsKey(id);
    }

    @Override
    public void remove(Integer id) {
        log.warn("remove DataSource: {}", id);
        dataSources.remove(id);
    }

    @Override
    public Collection<DataSource> all() {
        return dataSources.values();
    }

    @Override
    public void closeDataSource(Integer id) {
        DataSource dataSource = dataSources.get(id);
        if (dataSource != null) {
            try {
                if (dataSource instanceof HikariDataSource) {
                    ((HikariDataSource) dataSource).close();
                }
            } catch (Exception exception) {
                log.error("关闭数据源异常", exception);
            }
        }
        dataSources.remove(id);
    }
}