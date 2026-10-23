package edu.eci.orders.lambda;

import edu.eci.orders.model.CreateOrderRequest;
import edu.eci.orders.model.FindOrdersRequest;
import edu.eci.orders.model.GetOrderRequest;
import edu.eci.orders.model.Order;
import edu.eci.orders.service.OrderService;

import java.util.List;

/**
 * Puntos de entrada para AWS Lambda (integracion NO proxy).
 * Cada metodo se usa como handler de una Lambda distinta.
 *
 * No usa clases de AWS: recibe y devuelve objetos Java normales.
 * La logica real esta en OrderService.
 *
 * Cada Lambda tiene su propia memoria; por eso cada una arranca
 * con 3 pedidos de ejemplo (ORD-001, ORD-002, ORD-003).
 */
public class OrderFunctions {

    // static: se crea una vez por entorno de ejecucion y se reutiliza
    private static final OrderService SERVICE = OrderService.withSampleData();

    /** Handler: edu.eci.orders.lambda.OrderFunctions::createOrder */
    public Order createOrder(CreateOrderRequest request) {
        return SERVICE.createOrder(request);
    }

    /** Handler: edu.eci.orders.lambda.OrderFunctions::getOrder */
    public Order getOrder(GetOrderRequest request) {
        return SERVICE.getOrder(request == null ? null : request.getId());
    }

    /** Handler: edu.eci.orders.lambda.OrderFunctions::findOrdersByCustomer */
    public List<Order> findOrdersByCustomer(FindOrdersRequest request) {
        return SERVICE.getOrdersByCustomer(request == null ? null : request.getCustomerId());
    }
}