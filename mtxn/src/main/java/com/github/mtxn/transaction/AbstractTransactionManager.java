package com.github.mtxn.transaction;

import com.github.mtxn.transaction.annotation.MultiTransaction;
import com.github.mtxn.transaction.context.DataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * <h3>magic-world</h3>
 * <p>事务公用方法</p>
 *
 * @author : xufangong
 * @date : 2022-08-18 12:44
 **/
@Slf4j
public abstract class AbstractTransactionManager {
    private static final String SPL_START = "#";

    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 切换数据源
     *
     * @param point
     * @param signature
     * @param multiTransaction
     * @return 切换是否成功
     * @throws Throwable
     */
    public boolean switchDataSource(ProceedingJoinPoint point, MethodSignature signature, MultiTransaction multiTransaction) {
        String value = multiTransaction.datasourceId();
        if (StringUtils.isEmpty(value)) {
            if (log.isDebugEnabled())
                log.debug("dataSourceId not exist, use default DataSource");
            return true;
        }
        String dataSourceId = parseCtxParams(point, signature, value);

        // 切换数据源
        if (log.isDebugEnabled())
            log.debug("dataSourceId {} exist, switch DataSource", dataSourceId);
        DataSourceContextHolder.setKey(dataSourceId);
        return false;
    }

    /**
     * 解析注解参数
     *
     * @param point
     * @param signature
     * @param value
     * @return
     */
    public String parseCtxParams(ProceedingJoinPoint point, MethodSignature signature, String value) {
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
        return dataSourceId;
    }
}
