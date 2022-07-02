package com.github.mtxn.datasource.driver;

import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.github.mtxn.entity.DataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;


@Slf4j
@Component
@AllArgsConstructor
public class DriverFactoryManager {

    private final List<DriverFactory> factories;

    public DriverFactory findUsableDriverFactory(DataSource config) {
        for (DriverFactory factory : factories) {
            if (factory.usable(config)) {
                return factory;
            }
        }
        throw ExceptionUtils.mpe("找不到数据源驱动: " + config.getJdbcUrl() + ", 请联系管理员适配");
    }
}
