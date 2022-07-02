package com.github.mtxn.datasource.driver;

import com.github.mtxn.entity.DataSource;

public interface DriverFactory {
    boolean usable(DataSource var1);

    DataSource autoFillDriverAttribute(DataSource var1);

    default int factoryOrder() {
        return 100;
    }
}
