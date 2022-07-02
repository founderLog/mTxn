package com.github.mtxn.transaction.aop;

import com.github.mtxn.transaction.context.DataSourceContextHolder;
import com.github.mtxn.transaction.MultiTransactionManager;
import com.github.mtxn.transaction.annotations.MultiTransaction;
import com.github.mtxn.transaction.wrapper.IsolationLevel;
import com.github.mtxn.transaction.context.TransactionHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Aspect
@Component
@Slf4j
@Order(99999)
public class TransactionAop {
    private static final String SPL_START = "#";

    private final ExpressionParser parser = new SpelExpressionParser();
    @Autowired
    private MultiTransactionManager multiTransactionManager;

    @Pointcut("@annotation(com.github.mtxn.transaction.annotations.MultiTransaction)")
    public void pointcut() {
        if (log.isDebugEnabled()) {
            log.debug("start in transaction pointcut...");
        }
    }


    @Around("pointcut()")
    public Object aroundTransaction(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        // 从切面中获取当前方法
        Method method = signature.getMethod();
        MultiTransaction multiTransaction = method.getAnnotation(MultiTransaction.class);
        if (multiTransaction == null) {
            return point.proceed();
        }
        IsolationLevel isolationLevel = multiTransaction.isolationLevel();
        boolean readOnly = multiTransaction.readOnly();

        // 切数据源，如果失败使用默认库
        if (switchDataSource(point, signature, multiTransaction)) return point.proceed();

        // 开启事务栈
        TransactionHolder transactionHolder = startTransaction(isolationLevel, readOnly, multiTransactionManager);
        // 数据源key入栈
        transactionHolder.getDatasourceKeyStack().push(DataSourceContextHolder.getKey());
        Object proceed;
        String mainTransId = transactionHolder.getMainTransactionId();
        String executeId = transactionHolder.getExecuteStack().peek();
        try {
            proceed = point.proceed();
            if (mainTransId.equals(executeId)) {
                multiTransactionManager.commit();
            }
        } catch (Throwable ex) {
            log.error("execute method:{}#{},err:",method.getDeclaringClass(), method.getName(), ex);
            if (Objects.equals(mainTransId, executeId)) {
                multiTransactionManager.rollback();
            }
            throw ex;
        } finally {
            if (multiTransactionManager.isTransOpen()) {
                // 当前事务结束出栈
                multiTransactionManager.getTrans().getExecuteStack().pop();
            }
            DataSourceContextHolder.setKey(transactionHolder.getDatasourceKeyStack().pop());
            // 最后回到主事务，关闭此次事务
            if (mainTransId.equals(executeId)) {
                multiTransactionManager.close();
                DataSourceContextHolder.clearKey();
            }
        }
        return proceed;

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
    private TransactionHolder startTransaction(IsolationLevel isolationLevel, boolean readOnly, MultiTransactionManager multiTransactionManager) throws SQLException {
        TransactionHolder transactionHolder = multiTransactionManager.getTrans();
        if (Objects.isNull(transactionHolder)) {
            String mainTransId = UUID.randomUUID().toString();
            transactionHolder = TransactionHolder.builder().
                    mainTransactionId(mainTransId).
                    executeStack(new Stack<>()).
                    datasourceKeyStack(new Stack<>()).
                    isOpen(Boolean.TRUE).
                    readOnly(readOnly).
                    isolationLevel(isolationLevel).
                    transCount(new AtomicInteger()).
                    connectionMap(new ConcurrentHashMap<>()).
                    build();
            multiTransactionManager.openTrans(transactionHolder);
            transactionHolder.getExecuteStack().push(mainTransId);
            if (log.isDebugEnabled())
                log.debug("begin execute main trans:{}",mainTransId);
        } else { // 嵌套生成一个新事务
            transactionHolder.getExecuteStack().push(UUID.randomUUID().toString());
            if (log.isDebugEnabled())
                log.debug("begin execute child trans:{}",transactionHolder.getExecuteStack().peek());
        }
        multiTransactionManager.bindConnection();
        return transactionHolder;
    }

    /**
     * 切换数据源
     * @param point
     * @param signature
     * @param multiTransaction
     * @return 切换是否成功
     * @throws Throwable
     */
    private boolean switchDataSource(ProceedingJoinPoint point, MethodSignature signature, MultiTransaction multiTransaction) {
        String value = multiTransaction.datasourceId();
        if (StringUtils.isEmpty(value)) {
            if (log.isDebugEnabled())
                log.debug("dataSourceId not exist, use default DataSource");
            return true;
        }
        String dataSourceId = value;
        if (value.startsWith(SPL_START)) {
            List<String> paramNames = Arrays.asList(signature.getParameterNames());
            List<Object> paramValues = Arrays.asList(point.getArgs());

            EvaluationContext ctx = new StandardEvaluationContext();

            // 将方法的参数名和参数值一一对应的放入上下文中
            for (int i = 0; i < paramNames.size(); i++) {
                ctx.setVariable(paramNames.get(i), paramValues.get(i));
            }

            dataSourceId = Optional.ofNullable(parser.parseExpression(value).getValue(ctx))
                    .map(Object::toString)
                    .orElse(null);
        }

        // 切换数据源
        if (log.isDebugEnabled())
            log.debug("dataSourceId {} exist, switch DataSource", dataSourceId);
        DataSourceContextHolder.setKey(dataSourceId);
        return false;
    }
}