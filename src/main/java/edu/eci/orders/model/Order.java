package edu.eci.orders.model;

/**
 * Representa un pedido. Es Java normal: no sabe nada de HTTP ni de AWS.
 */
public class Order {

    private String id;
    private String customerId;
    private String product;
    private int quantity;

    // Constructor vacío: más adelante Lambda lo necesitará para convertir JSON a objeto
    public Order() {
    }

    public Order(String id, String customerId, String product, int quantity) {
        this.id = id;
        this.customerId = customerId;
        this.product = product;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return "Order{id='" + id + "', customerId='" + customerId
                + "', product='" + product + "', quantity=" + quantity + "}";
    }
}