package edu.eci.orders.http;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.orders.exception.OrderException;
import edu.eci.orders.exception.OrderNotFoundException;
import edu.eci.orders.model.CreateOrderRequest;
import edu.eci.orders.model.Order;
import edu.eci.orders.service.OrderService;

import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador HTTP para la integracion Lambda PROXY de API Gateway.
 *
 * Esta es la UNICA clase que conoce HTTP y AWS:
 *  - lee metodo, ruta, parametros y cuerpo del evento de API Gateway;
 *  - llama a OrderService (logica de negocio, Java normal);
 *  - decide el codigo HTTP (200, 201, 400, 404, 500), los headers y el JSON de respuesta.
 *
 * Handler en Lambda: edu.eci.orders.http.OrdersHttpHandler::handleRequest
 */
public class OrdersHttpHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final ObjectMapper JSON = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final OrderService service;

    /** Lo usa Lambda: una sola instancia por entorno, con 3 pedidos de ejemplo. */
    public OrdersHttpHandler() {
        this(OrderService.withSampleData());
    }

    /** Lo usan las pruebas para inyectar el servicio. */
    public OrdersHttpHandler(OrderService service) {
        this.service = service;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            return route(event);
        } catch (OrderNotFoundException e) {
            return json(404, new ErrorResponse(e.getErrorCode(), e.getMessage()), null);
        } catch (OrderException e) {
            return json(400, new ErrorResponse(e.getErrorCode(), e.getMessage()), null);
        } catch (JsonProcessingException e) {
            return json(400, new ErrorResponse("INVALID_REQUEST", "Malformed JSON body"), null);
        } catch (RuntimeException e) {
            // Error inesperado: se registra el detalle en los logs, pero NO se envia al cliente
            e.printStackTrace();
            return json(500, new ErrorResponse("INTERNAL_ERROR", "Unexpected error"), null);
        }
    }

    // ---------- Enrutamiento ----------

    private APIGatewayProxyResponseEvent route(APIGatewayProxyRequestEvent event) throws JsonProcessingException {
        String method = event.getHttpMethod();
        String resource = event.getResource(); // plantilla de la ruta: "/orders" o "/orders/{id}"

        if ("/orders".equals(resource) && "POST".equals(method)) {
            return createOrder(event.getBody());
        }
        if ("/orders".equals(resource) && "GET".equals(method)) {
            return findOrders(param(event.getQueryStringParameters(), "customerId"));
        }
        if ("/orders/{id}".equals(resource) && "GET".equals(method)) {
            return getOrder(param(event.getPathParameters(), "id"));
        }
        if ("/orders/{id}".equals(resource) && "DELETE".equals(method)) {
            service.deleteOrder(param(event.getPathParameters(), "id"));
            return noContent();
        }
        return json(404, new ErrorResponse("ROUTE_NOT_FOUND", method + " " + resource + " is not supported"), null);
    }

    // ---------- Operaciones ----------

    private APIGatewayProxyResponseEvent createOrder(String body) throws JsonProcessingException {
        CreateOrderRequest request = (body == null || body.isBlank())
                ? null
                : JSON.readValue(body, CreateOrderRequest.class);
        Order order = service.createOrder(request);
        return json(201, order, Map.of("Location", "/orders/" + order.getId()));
    }

    private APIGatewayProxyResponseEvent getOrder(String id) {
        return json(200, service.getOrder(id), null);
    }

    private APIGatewayProxyResponseEvent findOrders(String customerId) {
        return json(200, service.getOrdersByCustomer(customerId), null);
    }

    // ---------- Utilidades HTTP ----------

    /** 204 No Content: exito sin cuerpo. */
    private static APIGatewayProxyResponseEvent noContent() {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(204)
                .withHeaders(new HashMap<>());
    }

    private static String param(Map<String, String> params, String name) {
        return params == null ? null : params.get(name);
    }

    private static APIGatewayProxyResponseEvent json(int status, Object body, Map<String, String> extraHeaders) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        if (extraHeaders != null) {
            headers.putAll(extraHeaders);
        }
        String text;
        try {
            text = JSON.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            status = 500;
            text = "{\"error\":\"INTERNAL_ERROR\",\"message\":\"Unexpected error\"}";
        }
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withHeaders(headers)
                .withBody(text);
    }
}