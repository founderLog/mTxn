package com.github.mtxn.transaction;

import com.github.mtxn.transaction.context.DataSourceContextHolder;
import com.github.mtxn.transaction.support.ConnectionProxy;
import com.github.mtxn.transaction.support.IsolationLevel;
import com.github.mtxn.transaction.support.TransactionHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.transaction.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class MultiTransactionManager extends AbstractTransactionManager implements Transaction {

    /**
     * 保存当前线程使用了事务的Connection
     */
    public static final ThreadLocal<TransactionHolder> TRANSACTION_HOLDER_THREAD_LOCAL = new ThreadLocal<>();
    @Autowired
    private DataSource dataSource;

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void commit() throws SQLException {
        String mainTransId = getTrans().getMainTransactionId();
        String executeId = getTrans().getExecuteStack().peek();
        if (mainTransId.equals(executeId)) {
            doCommit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        String mainTransId = getTrans().getMainTransactionId();
        String executeId = getTrans().getExecuteStack().peek();
        if (mainTransId.equals(executeId)) {
            doRollback();
        }
    }

    @Override
    public void close() throws SQLException {
        getTrans().setOpen(false);
        DataSourceContextHolder.clearKey();
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        if (conHolder != null) {
            conHolder.clear();
            conHolder.released();
        }
        removeTrans();
    }

    /**
     * 在主事务执行关闭
     *
     * @param executeId 事务id
     * @throws SQLException
     */
    public void close(String executeId) throws SQLException {
        String mainTransId = getTrans().getMainTransactionId();
        if (mainTransId.equals(executeId)) {
            this.close();
        }
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
    public void bindConnection(String transId) throws SQLException {
        TransactionHolder transactionHolder = TRANSACTION_HOLDER_THREAD_LOCAL.get();
        if (Objects.isNull(transactionHolder)) {
            return;
        }
        // 相同的数据源公用一个连接
        transactionHolder.getExecuteIdDatasourceKeyMap().forEach((e, k) -> {
            // 当前事务Id对应的key,在其他事务已存在
            if (k.equals(DataSourceContextHolder.getKey()) && transactionHolder.getConnectionMap().containsKey(e)) {
                transactionHolder.getConnectionMap()
                        .computeIfAbsent(transId, v -> transactionHolder.getConnectionMap().get(e));
            }
        });
        if (transactionHolder.getConnectionMap().containsKey(transId)) {
            return;
        }
        // 获取当前数据源
        Connection connection;
        // 使用try resource，避免sonarLint检测报错
        try (ConnectionProxy connectionProxy = new ConnectionProxy(this.dataSource.getConnection())) {
            connection = connectionProxy.getConnection();
        }
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
                .computeIfAbsent(transId, k -> new ConnectionProxy(connection));
    }

    /**
     * 提交事务
     *
     * @throws SQLException SQLException
     */
    private void doCommit() throws SQLException {
        TransactionHolder transactionHolder = getTransactionHolder();
        if (transactionHolder == null) return;
        Map<String, ConnectionProxy> connectionWrapMap = transactionHolder.getConnectionMap();
        if (CollectionUtils.isEmpty(connectionWrapMap)) {
            return;
        }
        try {
            ConnectionProxy connectionProxy;
            for (Map.Entry<String, ConnectionProxy> connectWarpEntry : connectionWrapMap.entrySet()) {
                connectionProxy = connectWarpEntry.getValue();
                if (!connectionProxy.isReadOnly() && !connectionProxy.getAutoCommit()) {
                    if (log.isDebugEnabled())
                        log.debug("begin commit executeId:{}", connectWarpEntry.getKey());
                    connectWarpEntry.getValue().realCommit();
                }
            }
        } finally {
            connectionWrapMap.forEach((s, connectionProxy) -> {
                try {
                    connectionProxy.realClose();
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
        Map<String, ConnectionProxy> connectionWrapMap = transactionHolder.getConnectionMap();
        if (connectionWrapMap == null) {
            return;
        }
        try {
            ConnectionProxy connectionProxy;
            for (Map.Entry<String, ConnectionProxy> connectWarpEntry : connectionWrapMap.entrySet()) {
                connectionProxy = connectWarpEntry.getValue();
                if (!connectionProxy.isReadOnly() && !connectionProxy.getAutoCommit()) {
                    if (log.isDebugEnabled())
                        log.debug("begin rollback executeId:{}", connectWarpEntry.getKey());
                    connectionProxy.rollback();
                }
            }
        } finally {
            connectionWrapMap.forEach((s, connectionProxy) -> {
                try {
                    connectionProxy.realClose();
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
        TRANSACTION_HOLDER_THREAD_LOCAL.get().clear();
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

    /**
     * 开启一个新事务
     *
     * @param isolationLevel          隔离级别
     * @param readOnly                只读事务
     * @param multiTransactionManager 事务管理器
     * @return TransactionHolder
     * @throws SQLException
     */
    public TransactionHolder startTransaction(String prevKey, IsolationLevel isolationLevel, boolean readOnly, MultiTransactionManager multiTransactionManager) throws SQLException {
        TransactionHolder transactionHolder = multiTransactionManager.getTrans();
        String transId = UUID.randomUUID().toString();
        if (Objects.isNull(transactionHolder)) {
            transactionHolder = TransactionHolder.builder().
                    mainTransactionId(transId).
                    executeStack(new LinkedList<>()).
                    datasourceKeyStack(new LinkedList<>()).
                    isOpen(Boolean.TRUE).
                    readOnly(readOnly).
                    isolationLevel(isolationLevel).
                    transCount(new AtomicInteger()).
                    connectionMap(new ConcurrentHashMap<>()).
                    executeIdDatasourceKeyMap(new ConcurrentHashMap<>()).
                    build();
            multiTransactionManager.openTrans(transactionHolder);
            transactionHolder.getExecuteStack().push(transId);
            // 保存事务前的Key，执行完恢复现场
            transactionHolder.getDatasourceKeyStack().push(prevKey);
            if (log.isDebugEnabled())
                log.debug("begin execute main trans:{}", transId);
        } else { // 嵌套生成一个新事务
            transactionHolder.getExecuteStack().push(transId);
            if (log.isDebugEnabled())
                log.debug("begin execute child trans:{}", transactionHolder.getExecuteStack().peek());
        }
        // 数据源key入栈
        transactionHolder.getDatasourceKeyStack().push(DataSourceContextHolder.getKey());
        transactionHolder.getExecuteIdDatasourceKeyMap().put(transId, DataSourceContextHolder.getKey());
        multiTransactionManager.bindConnection(transId);
        transactionHolder.addCount();
        return transactionHolder;
    }
}
