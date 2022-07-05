package com.github.mtxn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.Table;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.github.mtxn.datasource.config.DataSourceConfig;
import com.github.mtxn.entity.enums.DataSourceStatus;
import com.github.mtxn.entity.enums.DataSourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;

@AllArgsConstructor
@Table(value = "com_data_source",comment="数据源表")
@Builder
@TableName("com_data_source")
public class DataSource implements DataSourceConfig {
    public static final String NAME = "base:data-source";

    //对应id，可不填
    @TableId(type = IdType.AUTO)
    @IsAutoIncrement
    @IsKey
    @Column(comment = "主键")
    private Integer id;

    @Column(comment = "名称")
    private String name;

    @Column(comment = "数据库类型",type = MySqlTypeConstant.VARCHAR)
    private DataSourceType type;

    @Column(comment = "主机")
    private String host;

    @Column(comment = "端口")
    private Integer port;

    @Column(comment = "数据库名称")
    private String dbName;

    @Column(comment = "用户名")
    private String username;

    @Column(comment = "密码")
    private String password;

    @Column(comment = "创建者")
    private String creator;

    @DateTimeFormat(
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    @Column(comment = "创建时间")
    private Timestamp createTime;

    @Column(comment = "修改者")
    private String modifier;

    @DateTimeFormat(
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    @Column(comment = "修改时间")
    private Timestamp modifyTime;

    @Column(comment = "jdbcUrl")
    private String jdbcUrl;

    @Column(comment = "driverClass")
    private String driverClass;

    @Column(comment = "testSql")
    private String testSql;

    @Column(comment = "status",type = MySqlTypeConstant.VARCHAR)
    private DataSourceStatus status;

    @Column(comment = "minIdle")
    private Integer minIdle;

    @Column(comment = "maxPoolSize")
    private Integer maxPoolSize;

    @Column(comment = "connectionTimeout")
    private Long connectionTimeout;

    @Column(comment = "description")
    private String description;

    @Column(comment = "disabled")
    private Boolean disabled;

    public String toString() {
        return "DataSource(id=" + this.getId() + ", name=" + this.getName() + ", type=" + this.getType() + ", host=" + this.getHost() + ", port=" + this.getPort() + ", dbName=" + this.getDbName() + ", username=" + this.getUsername() + ", password=" + this.getPassword() + ", creator=" + this.getCreator() + ", createTime=" + this.getCreateTime() + ", modifier=" + this.getModifier() + ", modifyTime=" + this.getModifyTime() + ", jdbcUrl=" + this.getJdbcUrl() + ", driverClass=" + this.getDriverClass() + ", testSql=" + this.getTestSql() + ", status=" + this.getStatus() + ", minIdle=" + this.getMinIdle() + ", maxPoolSize=" + this.getMaxPoolSize() + ", connectionTimeout=" + this.getConnectionTimeout() + ", description=" + this.getDescription() + ", disabled=" + this.getDisabled() + ")";
    }

    public Boolean isEnabled() {
        return this.disabled == null || !this.disabled;
    }

    public DataSource() {
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public DataSourceType getType() {
        return this.type;
    }

    public String getHost() {
        return this.host;
    }

    public Integer getPort() {
        return this.port;
    }

    public String getDbName() {
        return this.dbName;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getCreator() {
        return this.creator;
    }

    public Timestamp getCreateTime() {
        return this.createTime;
    }

    public String getModifier() {
        return this.modifier;
    }

    public Timestamp getModifyTime() {
        return this.modifyTime;
    }

    public String getJdbcUrl() {
        return this.jdbcUrl;
    }

    public String getDriverClass() {
        return this.driverClass;
    }

    public String getTestSql() {
        return this.testSql;
    }

    public DataSourceStatus getStatus() {
        return this.status;
    }

    public Integer getMinIdle() {
        return this.minIdle;
    }

    public Integer getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public Long getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public String getDescription() {
        return this.description;
    }

    public Boolean getDisabled() {
        return this.disabled;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(DataSourceType type) {
        this.type = type;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public void setTestSql(String testSql) {
        this.testSql = testSql;
    }

    public void setStatus(DataSourceStatus status) {
        this.status = status;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public void setConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }


}