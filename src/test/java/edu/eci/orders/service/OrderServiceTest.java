package edu.eci.orders.service;

import edu.eci.orders.exception.InvalidOrderException;
import edu.eci.orders.exception.OrderNotFoundException;
import edu.eci.orders.model.CreateOrderRequest;
import edu.eci.orders.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(); // servicio vacío en cada prueba
    }

    // ---------- Crear ----------

    @Test
    void createsValidOrder() {
        Order order = service.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", 1));

        assertEquals("ORD-001", order.getId());
        assertEquals("CUST-42", order.getCustomerId());
        assertEquals("MacBook Air", order.getProduct());
        assertEquals(1, order.getQuantity());
    }

    @Test
    void assignsDifferentIdsToEachOrder() {
        Order first = service.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", 1));
        Order second = service.createOrder(new CreateOrderRequest("CUST-42", "Mouse", 2));

        assertNotEquals(first.getId(), second.getId());
    }

    // ---------- Recuperar ----------

    @Test
    void retrievesExistingOrder() {
        Order created = service.createOrder(new CreateOrderRequest("CUST-7", "Keyboard", 2));

        Order found = service.getOrder(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Keyboard", found.getProduct());
    }

    @Test
    void throwsWhenOrderDoesNotExist() {
        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class,
                () -> service.getOrder("ORD-999"));

        assertEquals("ORDER_NOT_FOUND", ex.getErrorCode());
        assertEquals("Order ORD-999 does not exist", ex.getMessage());
    }

    // ---------- Buscar por cliente ----------

    @Test
    void findsOrdersByCustomer() {
        service.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", 1));
        service.createOrder(new CreateOrderRequest("CUST-42", "Mouse", 2));
        service.createOrder(new CreateOrderRequest("CUST-100", "Keyboard", 1));

        List<Order> result = service.getOrdersByCustomer("CUST-42");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(o -> o.getCustomerId().equals("CUST-42")));
    }

    @Test
    void returnsEmptyListWhenCustomerHasNoOrders() {
        service.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", 1));

        assertTrue(service.getOrdersByCustomer("CUST-999").isEmpty());
    }

    @Test
    void rejectsSearchWithoutCustomerId() {
        InvalidOrderException ex = assertThrows(InvalidOrderException.class,
                () -> service.getOrdersByCustomer(" "));

        assertEquals("MISSING_CUSTOMER_ID", ex.getErrorCode());
    }

    // ---------- Entrada inválida ----------

    @Test
    void rejectsNullRequest() {
        assertInvalid(null, "Request body is required");
    }

    @Test
    void rejectsBlankCustomerId() {
        assertInvalid(new CreateOrderRequest("", "MacBook Air", 1), "customerId is required");
    }

    @Test
    void rejectsBlankProduct() {
        assertInvalid(new CreateOrderRequest("CUST-42", "  ", 1), "product is required");
    }

    @Test
    void rejectsZeroQuantity() {
        assertInvalid(new CreateOrderRequest("CUST-42", "MacBook Air", 0), "quantity must be greater than 0");
    }

    @Test
    void rejectsMissingQuantity() {
        assertInvalid(new CreateOrderRequest("CUST-42", "MacBook Air", null), "quantity must be greater than 0");
    }

    private void assertInvalid(CreateOrderRequest request, String expectedMessage) {
        InvalidOrderException ex = assertThrows(InvalidOrderException.class,
                () -> service.createOrder(request));
        assertEquals("INVALID_REQUEST", ex.getErrorCode());
        assertEquals(expectedMessage, ex.getMessage());
    }
}