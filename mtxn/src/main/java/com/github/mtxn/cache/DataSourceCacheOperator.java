package com.github.mtxn.cache;

import com.github.mtxn.entity.DataSource;

import java.util.concurrent.ConcurrentMap;

public interface DataSourceCacheOperator {

    DataSource getCacheData(Integer id);

    DataSource getCacheDataIfPresent(Integer id);

    void clearCacheData();

    ConcurrentMap<String, DataSource> getCacheDataMap();
}
