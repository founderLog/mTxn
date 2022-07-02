package com.github.mtxn.transaction.annotations;

import com.github.mtxn.transaction.wrapper.IsolationLevel;

import java.lang.annotation.*;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiTransaction {
    String transactionManager() default "multiTransactionManager";

    // 默认数据隔离级别，随数据库本身默认值
    IsolationLevel isolationLevel() default IsolationLevel.DEFAULT;

    // 默认为主库数据源
    String datasourceId() default "default";

    // 只读事务，若有更新操作会抛出异常
    boolean readOnly() default false;
}
