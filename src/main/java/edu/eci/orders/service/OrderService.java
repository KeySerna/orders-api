package edu.eci.orders.service;

import edu.eci.orders.exception.InvalidOrderException;
import edu.eci.orders.exception.OrderNotFoundException;
import edu.eci.orders.model.CreateOrderRequest;
import edu.eci.orders.model.Order;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Logica de la aplicacion. Java puro: no depende de HTTP, API Gateway,
 * Spring Boot ni de ningun servidor web.
 * Los datos estan en memoria: se pierden si el proceso (o la Lambda) se reinicia.
 */
public class OrderService {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(0);

    /** Crea un servicio con 3 pedidos de ejemplo (util para probar en AWS). */
    public static OrderService withSampleData() {
        OrderService service = new OrderService();
        service.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", 1));   // ORD-001
        service.createOrder(new CreateOrderRequest("CUST-42", "Mouse", 2));         // ORD-002
        service.createOrder(new CreateOrderRequest("CUST-100", "Keyboard", 1));     // ORD-003
        return service;
    }

    /** Crea un pedido nuevo y le asigna un id (ORD-001, ORD-002, ...). */
    public Order createOrder(CreateOrderRequest request) {
        validate(request);
        String id = String.format("ORD-%03d", sequence.incrementAndGet());
        Order order = new Order(id,
                request.getCustomerId().trim(),
                request.getProduct().trim(),
                request.getQuantity());
        orders.put(id, order);
        return order;
    }

    /** Devuelve un pedido por su id, o lanza OrderNotFoundException. */
    public Order getOrder(String id) {
        if (isBlank(id)) {
            throw new InvalidOrderException("INVALID_REQUEST", "id is required");
        }
        Order order = orders.get(id);
        if (order == null) {
            throw new OrderNotFoundException(id);
        }
        return order;
    }

    /** Devuelve los pedidos de un cliente (lista vacia si no tiene). */
    public List<Order> getOrdersByCustomer(String customerId) {
        if (isBlank(customerId)) {
            throw new InvalidOrderException("MISSING_CUSTOMER_ID", "customerId is required");
        }
        List<Order> result = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getCustomerId().equals(customerId)) {
                result.add(order);
            }
        }
        result.sort(Comparator.comparing(Order::getId));
        return result;
    }

    private void validate(CreateOrderRequest request) {
        if (request == null) {
            throw new InvalidOrderException("INVALID_REQUEST", "Request body is required");
        }
        if (isBlank(request.getCustomerId())) {
            throw new InvalidOrderException("INVALID_REQUEST", "customerId is required");
        }
        if (isBlank(request.getProduct())) {
            throw new InvalidOrderException("INVALID_REQUEST", "product is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new InvalidOrderException("INVALID_REQUEST", "quantity must be greater than 0");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}