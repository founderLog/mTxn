package com.github.mtxn.transaction;

import com.github.mtxn.datasource.DynamicDataSource;
import com.github.mtxn.transaction.wrapper.ConnectionWrapper;
import com.github.mtxn.transaction.wrapper.IsolationLevel;
import com.github.mtxn.transaction.context.TransactionHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.transaction.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class MultiTransactionManager implements Transaction {

    @Autowired
    private DataSource dataSource;

    /**
     * 保存当前线程使用了事务的Connection
     */
    public static final ThreadLocal<TransactionHolder> TRANSACTION_HOLDER_THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void commit() throws SQLException {
        doCommit();
    }

    @Override
    public void rollback() throws SQLException {
        doRollback();
    }

    @Override
    public void close() throws SQLException {
        removeTrans();
    }

    @Override
    public Integer getTimeout() throws SQLException {
        return -1;
    }


    /**
     * 绑定当前数据库连接
     *
     * @throws SQLException sql异常
     */
    public void bindConnection() throws SQLException {
        TransactionHolder transactionHolder = TRANSACTION_HOLDER_THREAD_LOCAL.get();
        if (Objects.isNull(transactionHolder)) {
            return;
        }
        // 获取当前数据源
        DataSource dataSource = ((DynamicDataSource) this.dataSource).determineTargetDataSource();
        Connection connection = dataSource.getConnection();
        // 设置事务隔离级别
        if (transactionHolder.getIsolationLevel() != IsolationLevel.DEFAULT) {
            connection.setTransactionIsolation(transactionHolder.getIsolationLevel().getValue());
        }
        // 开启手动事务处理
        if (!transactionHolder.isReadOnly() && connection.getAutoCommit()) {
            connection.setAutoCommit(false);
        }
        connection.setReadOnly(transactionHolder.isReadOnly());
        // 当前事务连接
        transactionHolder.getConnectionMap()
                .computeIfAbsent(transactionHolder.getExecuteStack().peek(), k -> new ConnectionWrapper(connection));
    }

    /**
     * 提交事务
     *
     * @throws SQLException SQLException
     */
    private void doCommit() throws SQLException {
        TransactionHolder transactionHolder = getTransactionHolder();
        if (transactionHolder == null) return;
        Map<String, ConnectionWrapper> connectionWrapMap = transactionHolder.getConnectionMap();
        if (CollectionUtils.isEmpty(connectionWrapMap)) {
            return;
        }
        try {
            ConnectionWrapper connectionWrapper;
            for (Map.Entry<String, ConnectionWrapper> connectWarpEntry : connectionWrapMap.entrySet()) {
                connectionWrapper = connectWarpEntry.getValue();
                if (!connectionWrapper.isReadOnly() && !connectionWrapper.getAutoCommit()) {
                    if (log.isDebugEnabled())
                        log.debug("begin commit executeId:{}", connectWarpEntry.getKey());
                    connectWarpEntry.getValue().realCommit();
                    if (log.isDebugEnabled())
                        log.debug("end commit executeId:{}", connectWarpEntry.getKey());
                }
            }
        } finally {
            connectionWrapMap.forEach((s, connectionWrapper) -> {
                try {
                    connectionWrapper.realClose();
                } catch (SQLException e) {
                    log.error("close connection err:", e);
                }
            });
        }
    }

    /**
     * 回滚事务
     *
     * @throws SQLException SQLException
     */
    private void doRollback() throws SQLException {
        TransactionHolder transactionHolder = getTransactionHolder();
        if (transactionHolder == null) return;
        Map<String, ConnectionWrapper> connectionWrapMap = transactionHolder.getConnectionMap();
        if (connectionWrapMap == null) {
            return;
        }
        try {
            ConnectionWrapper connectionWrapper;
            for (Map.Entry<String, ConnectionWrapper> connectWarpEntry : connectionWrapMap.entrySet()) {
                connectionWrapper = connectWarpEntry.getValue();
                if (!connectionWrapper.isReadOnly() && !connectionWrapper.getAutoCommit()) {
                    if (log.isDebugEnabled())
                        log.debug("begin rollback executeId:{}", connectWarpEntry.getKey());
                    connectionWrapper.rollback();
                    if (log.isDebugEnabled())
                        log.debug("end rollback executeId:{}", connectWarpEntry.getKey());
                }
            }
        } finally {
            connectionWrapMap.forEach((s, connectionWrapper) -> {
                try {
                    connectionWrapper.realClose();
                } catch (SQLException e) {
                    log.error("close connection err:", e);
                }
            });
        }
    }

    private TransactionHolder getTransactionHolder() {
        TransactionHolder transactionHolder = TRANSACTION_HOLDER_THREAD_LOCAL.get();
        if (Objects.isNull(transactionHolder)) {
            return null;
        }
        return transactionHolder;
    }

    public void openTrans(TransactionHolder transactionHolder) {
        TRANSACTION_HOLDER_THREAD_LOCAL.set(transactionHolder);
    }

    public void removeTrans() {
        TRANSACTION_HOLDER_THREAD_LOCAL.remove();
    }

    /**
     * 判断当前线程是否开启了事务
     *
     * @return 是否开启了跨库事务
     * @See DynamicDataSource#bindConnection
     */
    public boolean isTransOpen() {
        TransactionHolder transactionHolder = TRANSACTION_HOLDER_THREAD_LOCAL.get();
        if (Objects.isNull(transactionHolder)) {
            return Boolean.FALSE;
        }
        return transactionHolder.isOpen();
    }

    public TransactionHolder getTrans() {
        return TRANSACTION_HOLDER_THREAD_LOCAL.get();
    }
}
