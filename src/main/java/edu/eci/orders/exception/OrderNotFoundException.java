package edu.eci.orders.exception;

/** Se lanza cuando se busca un pedido que no existe. */
public class OrderNotFoundException extends OrderException {

    public OrderNotFoundException(String id) {
        super("ORDER_NOT_FOUND", "Order " + id + " does not exist");
    }
}