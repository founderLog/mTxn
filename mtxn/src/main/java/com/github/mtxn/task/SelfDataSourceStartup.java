package com.github.mtxn.task;

import com.github.mtxn.entity.DataSource;
import com.github.mtxn.entity.enums.DataSourceType;
import com.github.mtxn.service.DataSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * 项目启动注册本身数据源到com_data_source
 */
@Order(89)
@Component
@Slf4j
@RequiredArgsConstructor
public class SelfDataSourceStartup implements CommandLineRunner {
    private final DataSourceProperties dataSourceProperties;
    private final DataSourceService dataSourceService;

    private static final String DATA_SOURCE_PREFIX = "self_";


    public void run(String... args) {
        try {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            log.info("checking the self datasource has been init to com_data_source");
            String url = this.dataSourceProperties.getUrl();
            String name = this.determineSourceName(url);
            DataSourceType type = this.determineSourceType(url);
            log.info("the current datasource name: {}, type: {}, jdbc url: {}", new Object[]{name, type, url});
            DataSource selfDataSource = dataSourceService.getByName(name);
            if (selfDataSource == null) {
                log.info("the self datasource has not been init to com_data_source yet,make it");
                DataSource source = new DataSource();
                source.setName(name);
                source.setDescription("init by mTxn");
                source.setType(type);
                source.setJdbcUrl(url);
                source.setUsername(this.dataSourceProperties.getUsername());
                source.setPassword(this.dataSourceProperties.getPassword());
                source.setMinIdle(0);
                source.setMaxPoolSize(8);
                source.setCreator("mTxn");
                source.setCreateTime(now);
                this.dataSourceService.insert(source);
            } else {
                if (selfDataSource.getType() == null) {
                    selfDataSource.setType(type);
                }
                selfDataSource.setJdbcUrl(url);
                selfDataSource.setUsername(this.dataSourceProperties.getUsername());
                selfDataSource.setPassword(this.dataSourceProperties.getPassword());
                selfDataSource.setModifier("mTxn");
                selfDataSource.setModifyTime(now);
                log.info("the self datasource has been init to com_data_source,update username and password");
                dataSourceService.update(selfDataSource);
            }
        } catch (Exception e) {
            log.error("make default datasource error: {}", e);
        }

    }

    private String determineSourceName(String url) {
        if (StringUtils.startsWith(url, "jdbc:mysql://")) {
            url = StringUtils.substringBefore(url, "?");
            url = StringUtils.substringAfterLast(url, "/");
            return DATA_SOURCE_PREFIX + url;
        } else if (StringUtils.startsWith(url, "jdbc:oracle")) {
            return DATA_SOURCE_PREFIX + StringUtils.substringAfterLast(url, "/");
        } else {
            return StringUtils.startsWith(url, "jdbc:dm://") ? DATA_SOURCE_PREFIX + StringUtils.substringAfterLast(url, "/") : "self";
        }
    }

    private DataSourceType determineSourceType(String url) {
        if (StringUtils.startsWith(url, "jdbc:mysql://")) {
            return DataSourceType.MYSQL;
        } else if (StringUtils.startsWith(url, "jdbc:oracle")) {
            return DataSourceType.ORACLE;
        } else {
            return StringUtils.startsWith(url, "jdbc:dm://") ? DataSourceType.DM : null;
        }
    }
}