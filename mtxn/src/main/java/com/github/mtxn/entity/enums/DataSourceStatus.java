package com.github.mtxn.entity.enums;

import com.github.mtxn.json.Enumerator;

public enum DataSourceStatus implements Enumerator {
    NOT_DETECTED("0", "未检测"),
    CONNECTED("1", "已连接"),
    CONNECTION_FAILED("2", "连接失败");

    private final String value;
    private final String text;

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getText() {
        return this.text;
    }

     DataSourceStatus(String value, String text) {
        this.value = value;
        this.text = text;
    }
}