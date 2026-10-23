package edu.eci.orders.lambda;

import edu.eci.orders.exception.OrderNotFoundException;
import edu.eci.orders.model.FindOrdersRequest;
import edu.eci.orders.model.GetOrderRequest;
import edu.eci.orders.model.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderFunctionsTest {

    private final OrderFunctions functions = new OrderFunctions();

    @Test
    void getsSampleOrderById() {
        Order order = functions.getOrder(new GetOrderRequest("ORD-001"));
        assertEquals("CUST-42", order.getCustomerId());
    }

    @Test
    void throwsWhenSampleOrderDoesNotExist() {
        assertThrows(OrderNotFoundException.class,
                () -> functions.getOrder(new GetOrderRequest("ORD-999")));
    }

    @Test
    void findsSampleOrdersByCustomer() {
        List<Order> orders = functions.findOrdersByCustomer(new FindOrdersRequest("CUST-100"));
        assertFalse(orders.isEmpty());
        assertTrue(orders.stream().allMatch(o -> o.getCustomerId().equals("CUST-100")));
    }
}