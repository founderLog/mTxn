package com.github.mtxn.transaction.context;

import com.github.mtxn.transaction.wrapper.ConnectionWrapper;
import com.github.mtxn.transaction.wrapper.IsolationLevel;
import lombok.Builder;
import lombok.Data;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Data
@Builder
public class TransactionHolder {
    // 是否开启了一个MultiTransaction
    private boolean isOpen;
    // 是否只读事务
    private boolean readOnly;
    // 事务隔离级别
    private IsolationLevel isolationLevel;
    // 维护当前线程事务ID和连接关系
    private ConcurrentHashMap<String, ConnectionWrapper> connectionMap;
    // 事务执行栈
    private Stack<String> executeStack;
    // 数据源切换栈
    private Stack<String> datasourceKeyStack;
    // 主事务ID
    private String mainTransactionId;
    // 执行次数
    private AtomicInteger transCount;

    public void addCount() {
        transCount.incrementAndGet();
    }

    public int getCount() {
        return transCount.get();
    }

}
