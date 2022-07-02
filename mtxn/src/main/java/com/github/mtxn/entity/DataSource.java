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

@Data
@NoArgsConstructor
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
}