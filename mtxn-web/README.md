### mTxn | 多数据源事物演示项目
#### 一 测试场景
模拟下单扣库存的场景，新增com_order和com_stock两张表。在主库mtxn下订单，在库存库mtxn2减库存。
1. mtxn2中初始化库存表
```java    
    @GetMapping("/add")
    @ResponseBody
    public DataSource add() {
        DataSource dataSource = DataSource.builder().name("mtxn2").jdbcUrl("jdbc:mysql://localhost:3306/mTxn?useSSL=false&allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8")
                .username("root").password("123456").disabled(false).creator("mtxn").createTime(new Timestamp(System.currentTimeMillis()))
                .minIdle(1).maxPoolSize(8).id(1).dbName("mtxn2").id(STOCK_DATASOURCE_ID).build();
        dataSourceService.insert(dataSource);
        return dataSource;
    }
```
约定数据源id为STOCK_DATASOURCE_ID，dbName为mtxn2。
2. 新建订单和库存表
```sql
-- auto-generated definition
create table com_order
(
    id      int auto_increment
        primary key,
    name    varchar(200) null,
    orderNo  varchar(100) null,
    createTime    timestamp default current_timestamp(),
    address varchar(500) null
);

-- auto-generated definition
create table com_stock
(
    id      int auto_increment
        primary key,
    name    varchar(200) null,
    amount  bigint null,
    lastModifyTime    timestamp default current_timestamp()
);

```
entity,mapper,service再次不在赘述。

3. 初始化库存
```java
    @MultiTransaction(datasourceId = "#dataSourceId")
    public Stock save(Integer dataSourceId,Stock stock) {
        stockMapper.insert(stock);
        return stock;
        }
```
指定库存ID为固定值STOCK_ID，初始库存为100。
4. 实现下单和减库存
OderService
```java    
    @MultiTransaction
    public void save(Order order) {
        orderMapper.insert(order);
        // 指定库存减去1
        stockService.decreasingStock(STOCK_DATASOURCE_ID,1,STOCK_ID);
    }
```
@MultiTransaction默认开启主库mtxn事物，接着执行减库存开启mtxn2的事物。

StockService
```java 
    @MultiTransaction(datasourceId = "#dataSourceId")
    public void decreasingStock(Integer dataSourceId,long amount,Integer id){
        Stock stock = stockMapper.selectById(id);
        stock.setAmount(stock.getAmount() - amount);
        stock.setLastModifyTime(new Timestamp(System.currentTimeMillis()));
        stockMapper.updateById(stock);
    }
```
第一个参数datasourceId又下订单方法传过来，指定为库存所在数据源id。
`stockService.decreasingStock(STOCK_DATASOURCE_ID,1,STOCK_ID);`

下单之后，库存减一，接着来验证事物的完整性。

### 二 验证事物完整性
1. 下单：http://localhost:8080/order/makeOrder
查看订单和库存表是否正常入库
2. 增加异常代码
```java
    @MultiTransaction
    public void save(Order order) {
        orderMapper.insert(order);
        // 指定库存减去1
        stockService.decreasingStock(STOCK_DATASOURCE_ID,1,STOCK_ID);
        System.out.println(1/0);
        } 
```
执行减库存后抛出异常，查看两条数据是否都已经回滚。

### 三 使用总结
@MultiTransaction相当于是@Transactional，用于开始一个事物。如果不指定datasourceId，则默认是操作主数据源（yml中配置的datasourceId）。

@MultiTransaction(datasourceId = "xxx")，则是切换到datasourceId所在的数据源。另外MultiTransaction支持任意嵌套调用：
```
@MultiTransaction -> @MultiTransaction(datasourceId = "xxx") -> (datasourceId = "yyy")...
```
注意：嵌套调用必须是通过代理类进行（注入的bean），同一个类中方法用this调用是不生效的。这一点跟Transactional一致。

