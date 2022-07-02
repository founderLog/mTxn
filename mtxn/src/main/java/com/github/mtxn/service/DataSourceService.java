package com.github.mtxn.service;

import com.github.mtxn.entity.DataSource;
import com.github.mtxn.entity.enums.DataSourceStatus;

import java.util.List;

public interface DataSourceService {

    List<DataSource> getAll();

    DataSource getById(Integer id);

    DataSource getByName(String name);

    /**
     * 新增数据源
     *
     * @param dataSource DataSource
     * @return 主键
     */
    Integer insert(DataSource dataSource);

    /**
     * 更新数据源
     *
     * @param dataSource DataSource
     */
    void update(DataSource dataSource);

    /**
     * 删除数据源
     *
     * @param id 主键
     * @return OperateResult
     */
    int deleteById(String id);


    /**
     * 修改数据源的状态
     *
     * @param id     数据源
     * @param status 状态
     */
    void modifyStatus(Integer id, DataSourceStatus status);

}