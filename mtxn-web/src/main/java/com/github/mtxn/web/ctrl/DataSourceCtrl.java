package com.github.mtxn.web.ctrl;

import com.github.mtxn.entity.DataSource;
import com.github.mtxn.service.DataSourceService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.List;

import static com.github.mtxn.web.service.OrderService.STOCK_DATASOURCE_ID;

@RestController
@RequestMapping(value = "/dataSource")
@AllArgsConstructor
public class DataSourceCtrl {
    private DataSourceService dataSourceService;


    /**
     * 列出当前所有的数据源
     *
     * @return
     */
    @GetMapping("/list")
    @ResponseBody
    public List<DataSource> list() {
        List<DataSource> dataSourceList = dataSourceService.getAll();
        return dataSourceList;
    }

    /**
     * 新增一个新数据源，仅仅是为了测试
     * 这里数据源id指定为10001，在实际的应用种会把数据源id跟业务绑定
     *
     * @return
     */
    @GetMapping("/add")
    @ResponseBody
    public DataSource add() {
        DataSource dataSource = DataSource.builder().name("mtxn2").jdbcUrl("jdbc:mysql://localhost:3306/mtxn2?useSSL=false&allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8")
                .username("root").password("123456").disabled(false).creator("mtxn").createTime(new Timestamp(System.currentTimeMillis()))
                .minIdle(1).maxPoolSize(8).id(1).dbName("mtxn2").id(STOCK_DATASOURCE_ID).build();
        dataSourceService.insert(dataSource);
        return dataSource;
    }
}
