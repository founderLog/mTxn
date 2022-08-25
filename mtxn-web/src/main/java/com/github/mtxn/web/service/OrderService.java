package com.github.mtxn.web.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.mtxn.transaction.annotation.MultiTransaction;
import com.github.mtxn.web.ctrl.DataSourceCtrl;
import com.github.mtxn.web.entity.Order;
import com.github.mtxn.web.mapper.OrderMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderMapper orderMapper;
    private final StockService stockService;

    // 库存所在数据源id
    public static final Integer STOCK_DATASOURCE_ID = 10001;

    //库存id
    public static final Integer STOCK_ID = 1000;
    public List<Order> list() {
        return orderMapper.selectList(Wrappers.query());
    }


    /**
     * 下单，减库存
     * 这里开启了跨库事务，接着stockService会开启新的事务
     * @param order
     */
    @MultiTransaction
    public void save(Order order) {
        orderMapper.insert(order);
        // 指定库存减去1
        stockService.decreasingStock(STOCK_DATASOURCE_ID,1,STOCK_ID);
        System.out.println(1/0);
    }
}
