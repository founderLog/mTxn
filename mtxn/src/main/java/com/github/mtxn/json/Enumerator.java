package com.github.mtxn.json;

import com.fasterxml.jackson.annotation.JsonValue;

public interface Enumerator {
    /**
     * 获取枚举码值
     * 序列化时采用改值
     *
     * @return
     */
    @JsonValue
    String getValue();

    /**
     * 获取枚举描述
     *
     * @return
     */
    String getText();
}