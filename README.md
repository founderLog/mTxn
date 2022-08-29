### 主要解决什么问题
#### 动态数据源管理

* 通过配置文件

典型的SpringBoot多数据源配置如下所示：
```yaml
spring:
  application:
    name: share
  datasource:
    dynamic:
      primary: db1 # 配置默认数据库
    db1:
      jdbc-url: jdbc:mysql://localhost:3306/test?serverTimezone=Asia/Chongqing&useUnicode=true&characterEncoding=utf8&characterSetResults=utf8&useSSL=false&verifyServerCertificate=false&autoReconnct=true&autoReconnectForPools=true&allowMultiQueries=true
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver
    db2:
      jdbc-url: jdbc:mysql://localhost:3306/test2?serverTimezone=Asia/Chongqing&useUnicode=true&characterEncoding=utf8&characterSetResults=utf8&useSSL=false&verifyServerCertificate=false&autoReconnct=true&autoReconnectForPools=true&allowMultiQueries=true
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver
```
使用配置文件配置2个数据源：`db1,db2`。指定db1为主数据源。除此之外，还需配通过Java Bean的方式配置Mybatis/Hibernate等ORM框架的Factory。
这种方式是静态配置方法，无法在运行时动态维护数据源。比如：新增一个数据源是无法支持的。

* 通过数据表管理

本项目提供的DynamicDataSource只需要配置一个主数据源，然后在运行时提供接口服务来动态管理这些数据源。
```java
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
```
业务上只需调用DataSourceService ，即可通过前端界面来维护数据源的管理。

### 多数据源和事务管理
* 如何切换数据源
在web分层场景下的典型结构：

```java
import org.springframework.beans.factory.annotation.Autowired;

@RestCOntroller
public class TestController {
    @Autowired
    private Service service;

    public void testMethod() {
        service.testMethod();
    }
}
```
服务类：

```java
import com.github.mtxn.transaction.annotation.MultiTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Service1 {
    @Autowired
    private Service2 service2;

    @MultiTransaction
    public void testMethod() {
        ...
        Integer dataSourceId = 1001;
        service2.testMethod2(dataSourceId);
    }
}

@Service
public class Service2 {
    @MultiTransaction(dataSourceId = "#id")
    public void testMethod2(String id) {
        ...
    }
}
```
@MultiTransaction 结构如下：
```java
public @interface MultiTransaction {

    String transactionManager() default "multiTransactionManager";

    // 默认数据隔离级别，随数据库本身默认值
    IsolationLevel isolationLevel() default IsolationLevel.DEFAULT;

    // 默认为主库数据源
    String datasourceId() default "default";

    // 只读事务，若有更新操作会抛出异常
    boolean readOnly() default false;
}
```
使用的时候只需指定datasourceId，如果不指定则是配置文件中的主库数据源。

Service1在调用Service2的方法之前，把Service2需要操作的数据源ID，通过第一个参数传递过来。

规则如下：`#datasourceId`。'#'是前缀，后面是参数名。

>由于数据源是在表：com_data_source中维护的，因此业务上一般要维护id和业务上的关联。通过业务id，能过直接找到datasourceId。
* 事务如何保证

@MultiTransaction其实包含两层：
1. 切换数据源
2. 同时保证事务的统一提交和回滚

这主要是通过AOP和ConnectionProxy的重写底层数据库连接实现的。实现并不复杂，具体可看源码。

### 相关说明
本项目的实现只是应用层上事务的控制，更多的适用于单体项目同时管理多个数据源的场景。

如果多个分支事务在统一提交的时候，某个分支commit失败，是无法保证一致性的。

如果需要强一致性，请使用XA协议或者Seata等解决方案。