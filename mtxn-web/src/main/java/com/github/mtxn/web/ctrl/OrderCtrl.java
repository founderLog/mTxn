package com.github.mtxn.web.ctrl;

import com.github.mtxn.web.entity.Order;
import com.github.mtxn.web.entity.Stock;
import com.github.mtxn.web.service.OrderService;
import com.github.mtxn.web.service.StockService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static com.github.mtxn.web.service.OrderService.STOCK_DATASOURCE_ID;
import static com.github.mtxn.web.service.OrderService.STOCK_ID;

@RestController
@RequestMapping(value = "/order")
@AllArgsConstructor
public class OrderCtrl {

    private final OrderService orderService;
    private final StockService stockService;


    @GetMapping("/list")
    public List<Order> list() {
        List<Order> students = orderService.list();
        return students;
    }


    @GetMapping("/makeOrder")
    public String save() {
        orderService.save(Order.builder().name("buy").orderNo(UUID.randomUUID().toString()).createTime(new Timestamp(System.currentTimeMillis())).build());
        return "success";
    }

    /**
     * 初始化库存
     * 库存id写死。STOCK_ID
     * @return
     */
    @GetMapping("/init")
    public Stock init() {
        return stockService.save(STOCK_DATASOURCE_ID,
                Stock.builder().id(STOCK_ID).name("图书库存").amount(100L).lastModifyTime(new Timestamp(System.currentTimeMillis())).build());
    }
}
