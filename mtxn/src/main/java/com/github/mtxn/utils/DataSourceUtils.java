package com.github.mtxn.utils;

import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.github.mtxn.entity.DataSource;
import com.github.mtxn.entity.enums.DataSourceStatus;
import org.apache.commons.lang3.StringUtils;

public class DataSourceUtils {
    public static void check(DataSource source) {
        if (source == null) {
            throw ExceptionUtils.mpe("数据源不能为空", new Object[0]);
        } else if (StringUtils.isBlank(source.getJdbcUrl())) {
            throw ExceptionUtils.mpe("数据源连接不能为空", new Object[0]);
        } else if (StringUtils.isBlank(source.getUsername())) {
            throw ExceptionUtils.mpe("数据源用户名不能为空", new Object[0]);
        } else if (StringUtils.isBlank(source.getPassword())) {
            throw ExceptionUtils.mpe("数据源用户密码不能为空", new Object[0]);
        } else {
            setDefaultValue(source);
        }
    }

    public static void setDefaultValue(DataSource source) {
        String[] hostUrl;
        if (StringUtils.isBlank(source.getHost()) || source.getPort() == null) {
            try {
                hostUrl = source.getJdbcUrl().split("://");
                String[] host = hostUrl[1].split("/")[0].split(":");
                source.setHost(host[0]);
                source.setPort(Integer.valueOf(host[1]));
            } catch (Exception var4) {
                source.setHost("localhost");
                source.setPort(3306);
            }
        }

        if (StringUtils.isBlank(source.getDbName())) {
            try {
                hostUrl = source.getJdbcUrl().split("://");
                String host = hostUrl[1].split("/")[1];
                source.setDbName(host.substring(0, host.indexOf("?")));
            } catch (Exception var3) {
                source.setDbName(source.getName());
            }
        }

        if (source.getStatus() == null) {
            source.setStatus(DataSourceStatus.NOT_DETECTED);
        }

        if (source.getMinIdle() == null || source.getMinIdle() <= 0) {
            source.setMinIdle(1);
        }

        if (source.getMaxPoolSize() == null || source.getMaxPoolSize() <= 0) {
            source.setMaxPoolSize(8);
        }

        if (source.getMinIdle() > source.getMaxPoolSize()) {
            source.setMinIdle(source.getMaxPoolSize());
        }

        if (source.getConnectionTimeout() == null || source.getConnectionTimeout() <= 0L) {
            source.setConnectionTimeout(30000L);
        }

    }
}
