package com.github.mtxn.transaction.aop;

import com.github.mtxn.application.Application;
import com.github.mtxn.transaction.MultiTransactionManager;
import com.github.mtxn.transaction.annotation.MultiTransaction;
import com.github.mtxn.transaction.context.DataSourceContextHolder;
import com.github.mtxn.transaction.support.IsolationLevel;
import com.github.mtxn.transaction.support.TransactionHolder;
import com.github.mtxn.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


@Aspect
@Component
@Slf4j
@Order(99999)
public class MultiTransactionAop {

    @Pointcut("@annotation(com.github.mtxn.transaction.annotation.MultiTransaction)")
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
        String prevKey = DataSourceContextHolder.getKey();
        MultiTransactionManager multiTransactionManager = Application.resolve(multiTransaction.transactionManager());
        // 切数据源，如果失败使用默认库
        if (multiTransactionManager.switchDataSource(point, signature, multiTransaction)) return point.proceed();
        // 开启事务栈
        TransactionHolder transactionHolder = multiTransactionManager.startTransaction(prevKey, isolationLevel, readOnly, multiTransactionManager);
        Object proceed;

        try {
            proceed = point.proceed();
            multiTransactionManager.commit();
        } catch (Throwable ex) {
            log.error("execute method:{}#{},err:", method.getDeclaringClass(), method.getName(), ex);
            multiTransactionManager.rollback();
            throw ExceptionUtils.api(ex, "系统异常：%s", ex.getMessage());
        } finally {
            // 当前事务结束出栈
            String transId = multiTransactionManager.getTrans().getExecuteStack().pop();
            transactionHolder.getDatasourceKeyStack().pop();
            // 恢复上一层事务
            DataSourceContextHolder.setKey(transactionHolder.getDatasourceKeyStack().peek());
            // 最后回到主事务，关闭此次事务
            multiTransactionManager.close(transId);
        }
        return proceed;

    }


}