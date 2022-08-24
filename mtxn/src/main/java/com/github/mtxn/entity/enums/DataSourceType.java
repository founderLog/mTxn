package com.github.mtxn.entity.enums;

import com.github.mtxn.json.Enumerator;

public enum DataSourceType implements Enumerator {

    /**
     * MySQL
     */
    MYSQL("0", "MySQL"),

    /**
     * Oracle
     */
    ORACLE("1", "Oracle"),

    /**
     * DM
     */
    DM("2", "DM"),

    POSTGRESQL("3", "PostgreSQL"),
    SQL_SERVER("4", "Microsoft SQL Server"),
    ;

    private final String value;
    private final String text;

    DataSourceType(String value, String text) {
        this.value = value;
        this.text = text;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getText() {
        return this.text;
    }
}



