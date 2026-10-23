package edu.eci.orders.service;

import edu.eci.orders.exception.OrderNotFoundException;
import edu.eci.orders.model.CreateOrderRequest;
import edu.eci.orders.model.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceDeleteTest {

    private final OrderService service = new OrderService();

    @Test
    void deletesExistingOrder() {
        Order order = service.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", 1));

        service.deleteOrder(order.getId());

        assertThrows(OrderNotFoundException.class, () -> service.getOrder(order.getId()));
    }

    @Test
    void throwsWhenDeletingMissingOrder() {
        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class,
                () -> service.deleteOrder("ORD-999"));
        assertEquals("ORDER_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void cannotDeleteSameOrderTwice() {
        Order order = service.createOrder(new CreateOrderRequest("CUST-42", "Mouse", 2));
        service.deleteOrder(order.getId());

        assertThrows(OrderNotFoundException.class, () -> service.deleteOrder(order.getId()));
    }
}