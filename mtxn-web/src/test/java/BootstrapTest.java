import com.github.mtxn.Bootstrap;
import com.github.mtxn.web.entity.Order;
import com.github.mtxn.web.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Bootstrap.class)
public class BootstrapTest {

    @Autowired
    private OrderService orderService;

    @Test
    public void testStudentService() {
        List<Order> orderList = orderService.list();
        Assert.assertTrue(orderList.size() > 0);
    }


}
