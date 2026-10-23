package edu.eci.orders.model;

/** Entrada para buscar pedidos de un cliente: {"customerId": "CUST-42"} */
public class FindOrdersRequest {

    private String customerId;

    public FindOrdersRequest() {
    }

    public FindOrdersRequest(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}