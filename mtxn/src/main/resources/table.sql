-- auto-generated definition
create table  com_data_source
(
    id                varchar(50)                  default '0' not null comment '主键'
        primary key,
    name              varchar(80)                              not null comment '数据源名称',
    type              varchar(30)                                  null comment '数据源类型(0 MySQL,1 Oracle,2 DM,3 PostgreSQL,4 SQL Server, 5 IoTDB, 6 MongoDB, 7 Redis, 8 ElasticSearch, 9 Kafka, 10 Hive 2, 11 HBase, 12 Apache Phoenix, 13 GeoMesa Cassandra,14 InfluxDB, 15 oscar, 16 http, 17 webservice)',
    host              varchar(120)                             null comment '数据源host',
    port              int                                      null comment '数据源端口',
    dbName            varchar(30)                              not null comment '数据库名称',
    username          varchar(50)                              not null comment '数据库用户名',
    password          varchar(50)                              not null comment '数据库密码',
    creator           varchar(50)                              not null comment '创建者',
    createTime        datetime                                 not null comment '创建时间',
    modifier          varchar(50)                              null comment '更新者',
    modifyTime        datetime                                 null comment '更新时间',
    tenantId          varchar(50)                              null comment '租户id',
    jdbcUrl           varchar(500)                             null,
    driverClass       varchar(100)                             null comment '驱动类',
    testSql           varchar(100)                             null comment '测试sql',
    status            varchar(30)                                  null comment '状态(0 未检测，1 已连接实例化数据源失，2 连接失败)',
    minIdle           int                          default 0   null comment '最小空闲连接',
    maxPoolSize       int                          default 8   null comment '最大连接',
    connectionTimeout bigint                                   null comment '连接超时时间',
    description       varchar(2000)                            null comment '描述',
    disabled          int(1)  default 0   null comment '是否禁用。0:未禁用，1:已禁用'
)
    comment '是否禁用。0:未禁用，1:已禁用' collate = utf8mb4_bin;



