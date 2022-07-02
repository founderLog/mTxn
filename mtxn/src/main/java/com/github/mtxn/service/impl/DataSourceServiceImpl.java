package com.github.mtxn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.mtxn.datasource.driver.DriverFactory;
import com.github.mtxn.datasource.driver.DriverFactoryManager;
import com.github.mtxn.entity.DataSource;
import com.github.mtxn.entity.enums.DataSourceStatus;
import com.github.mtxn.mapper.DataSourceMapper;
import com.github.mtxn.service.DataSourceService;
import com.github.mtxn.utils.DataSourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataSourceServiceImpl  implements DataSourceService {
    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private DriverFactoryManager driverFactoryManager;

    @Override
    public List<DataSource> getAll() {
        return dataSourceMapper.selectList(Wrappers.query());
    }

    @Override
    public DataSource getById(Integer id) {
        return dataSourceMapper.selectById(id);
    }

    @Override
    public DataSource getByName(String name) {
        return dataSourceMapper.selectOne(Wrappers.<DataSource>lambdaQuery().eq(DataSource::getName,name));
    }


    @Override
    public Integer insert(DataSource dataSource) {
        // 补充其他数据源信息
        this.fillOtherDataSourceProps(dataSource);
        dataSourceMapper.insert(dataSource);
        return dataSource.getId();
    }

    @Override
    public void update(DataSource dataSource) {
        dataSourceMapper.updateById(dataSource);
    }

    @Override
    public int deleteById(String id) {
        return dataSourceMapper.deleteById(id);
    }

    @Override
    public void modifyStatus(Integer id, DataSourceStatus status) {
        dataSourceMapper.updateById(DataSource.builder().id(id).status(status).build());
    }

    private void fillOtherDataSourceProps(DataSource source) {
        DataSourceUtils.check(source);
        LambdaQueryWrapper<DataSource> queryWrapper =  Wrappers.<DataSource>lambdaQuery();
        if (source.getId() == null) {
            queryWrapper.eq(DataSource::getName,source.getName());
        } else {
            queryWrapper.eq(DataSource::getName,source.getName()).eq(DataSource::getId,source.getId());
        }

        boolean exists = dataSourceMapper.exists(queryWrapper);
        if (exists) {
            throw ExceptionUtils.mpe("数据源名称重复: " + source.getName(), new Object[0]);
        } else {
            DataSource dbDataSource = (DataSource)this.dataSourceMapper.selectById(source.getId());
            if (dbDataSource != null) {
                if (StringUtils.isBlank(source.getHost()) || source.getPort() == null) {
                    source.setHost(dbDataSource.getHost());
                    source.setPort(dbDataSource.getPort());
                }

                if (StringUtils.isBlank(source.getDbName())) {
                    source.setDbName(dbDataSource.getDbName());
                }

                if (StringUtils.isBlank(source.getCreator())) {
                    source.setCreator(dbDataSource.getCreator());
                    source.setCreateTime(dbDataSource.getCreateTime());
                }

                DataSourceUtils.setDefaultValue(dbDataSource);
            }

            if (StringUtils.isEmpty(source.getDriverClass())) {
                DriverFactory driverFactory = this.driverFactoryManager.findUsableDriverFactory(source);
                driverFactory.autoFillDriverAttribute(source);
            }

        }
    }
}
