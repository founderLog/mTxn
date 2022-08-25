package com.github.mtxn.task;

import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.mtxn.cache.DataSourceCacheOperator;
import com.github.mtxn.datasource.DynamicDataSourceBuilder;
import com.github.mtxn.entity.DataSource;
import com.github.mtxn.manager.DataSourceManager;
import com.github.mtxn.mapper.DataSourceMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;


@Component
@RequiredArgsConstructor
@Slf4j
@Order(999999)
public class DataSourceTask implements CommandLineRunner, DataSourceCacheOperator {


    protected Cache<String, DataSource> dataSourceCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).build();

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Override
    public void run(String... args) {
        this.load();
    }

    @Scheduled(fixedRate = 1000 * 60 * 5)
    public void load() {
        List<DataSource> dataSourceList = dataSourceMapper.selectList(Wrappers.query());
        dataSourceList.forEach(dataSource -> doLoad(dataSource));
    }

    public void doLoad(DataSource dataSource) {
        if (null != dataSource) {
            setCacheData(dataSource.getId(), dataSource);
            if (dataSourceManager.get(dataSource.getId()) == null) {
                try {
                    dataSourceManager.put(dataSource.getId(), DynamicDataSourceBuilder.create(dataSource));
                } catch (Exception exception) {
                    log.info("实例化数据源失败：{}", exception.getMessage());
                }
            }
        }
    }


    @Override
    public DataSource getCacheData(Integer id) {
        try {
            DataSource dataSource = dataSourceCache.get(DataSource.NAME + ":" + id, () -> dataSourceMapper.selectById(id));
            doLoad(dataSource);
            return dataSource;
        } catch (ExecutionException e) {
            log.warn("读取【数据源基础信息缓存】数据异常", e);
            throw ExceptionUtils.mpe("读取【数据源基础信息缓存】数据异常", e);
        }
    }

    @Override
    public DataSource getCacheDataIfPresent(Integer id) {
        //不能直接获取，可能出现数据库数据没写到缓存
        return getCacheData(id);
    }

    protected void setCacheData(Integer id, DataSource dataSource) {
        dataSourceCache.put(DataSource.NAME + ":" + id, dataSource);
    }

    @Override
    public void clearCacheData() {
        dataSourceCache.invalidateAll();
    }

    @Override
    public ConcurrentMap<String, DataSource> getCacheDataMap() {
        return dataSourceCache.asMap();
    }

}
