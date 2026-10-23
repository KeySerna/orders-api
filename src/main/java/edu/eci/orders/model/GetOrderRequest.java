package edu.eci.orders.model;

/** Entrada para buscar un pedido por id: {"id": "ORD-001"} */
public class GetOrderRequest {

    private String id;

    public GetOrderRequest() {
    }

    public GetOrderRequest(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}