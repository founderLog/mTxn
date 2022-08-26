import com.github.mtxn.Bootstrap;
import com.github.mtxn.application.Application;
import com.github.mtxn.web.entity.Order;
import com.github.mtxn.web.entity.Stock;
import com.github.mtxn.web.service.OrderService;
import com.github.mtxn.web.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.github.mtxn.web.service.OrderService.STOCK_DATASOURCE_ID;
import static com.github.mtxn.web.service.OrderService.STOCK_ID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Bootstrap.class)
public class BootstrapTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockService stockService;

    /**
     *  演示且库：
     *      订单库->库存库->订单库
     */
    @Test
    public void testOrderService() {
        List<Order> orderList = orderService.list();
        Assert.assertTrue(orderList.size() > 0);
        OrderService orderServiceProxy = Application.resolve(OrderService.class);
        // 切库，访问库存
        List<Stock> stockList = stockService.list(STOCK_DATASOURCE_ID);
        Assert.assertTrue(stockList.size() > 0);
        // 切回主库，访问订单
        orderList = orderServiceProxy.list();
        Assert.assertTrue(orderList.size() > 0);
    }
}
