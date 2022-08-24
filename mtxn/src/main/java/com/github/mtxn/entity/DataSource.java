package com.github.mtxn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.mtxn.datasource.config.DataSourceConfig;
import com.github.mtxn.entity.enums.DataSourceStatus;
import com.github.mtxn.entity.enums.DataSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;

@AllArgsConstructor
@Builder
@TableName("com_data_source")
public class DataSource implements DataSourceConfig {
    public static final String NAME = "base:data-source";

    //对应id，可不填
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("type")
    private DataSourceType type;

    @TableField("host")
    private String host;

    @TableField("port")
    private Integer port;

    @TableField("dbName")
    private String dbName;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("creator")
    private String creator;

    @DateTimeFormat(
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    @TableField("createTime")
    private Timestamp createTime;

    @TableField("modifier")
    private String modifier;

    @DateTimeFormat(
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    @TableField("modifyTime")
    private Timestamp modifyTime;

    @TableField("jdbcUrl")
    private String jdbcUrl;

    @TableField("driverClass")
    private String driverClass;

    @TableField("testSql")
    private String testSql;

    @TableField("status")
    private DataSourceStatus status;

    @TableField("minIdle")
    private Integer minIdle;

    @TableField("maxPoolSize")
    private Integer maxPoolSize;

    @TableField("connectionTimeout")
    private Long connectionTimeout;

    @TableField("description")
    private String description;

    @TableField("disabled")
    private Boolean disabled;

    public DataSource() {
    }

    public String toString() {
        return "DataSource(id=" + this.getId() + ", name=" + this.getName() + ", type=" + this.getType() + ", host=" + this.getHost() + ", port=" + this.getPort() + ", dbName=" + this.getDbName() + ", username=" + this.getUsername() + ", password=" + this.getPassword() + ", creator=" + this.getCreator() + ", createTime=" + this.getCreateTime() + ", modifier=" + this.getModifier() + ", modifyTime=" + this.getModifyTime() + ", jdbcUrl=" + this.getJdbcUrl() + ", driverClass=" + this.getDriverClass() + ", testSql=" + this.getTestSql() + ", status=" + this.getStatus() + ", minIdle=" + this.getMinIdle() + ", maxPoolSize=" + this.getMaxPoolSize() + ", connectionTimeout=" + this.getConnectionTimeout() + ", description=" + this.getDescription() + ", disabled=" + this.getDisabled() + ")";
    }

    public Boolean isEnabled() {
        return this.disabled == null || !this.disabled;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataSourceType getType() {
        return this.type;
    }

    public void setType(DataSourceType type) {
        this.type = type;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCreator() {
        return this.creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Timestamp getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getModifier() {
        return this.modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public Timestamp getModifyTime() {
        return this.modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getJdbcUrl() {
        return this.jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getDriverClass() {
        return this.driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getTestSql() {
        return this.testSql;
    }

    public void setTestSql(String testSql) {
        this.testSql = testSql;
    }

    public DataSourceStatus getStatus() {
        return this.status;
    }

    public void setStatus(DataSourceStatus status) {
        this.status = status;
    }

    public Integer getMinIdle() {
        return this.minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public Long getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDisabled() {
        return this.disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }


}