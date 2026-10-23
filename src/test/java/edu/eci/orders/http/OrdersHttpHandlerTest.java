package edu.eci.orders.http;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.orders.model.Order;
import edu.eci.orders.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Prueba el adaptador HTTP localmente, simulando eventos de API Gateway (sin AWS). */
class OrdersHttpHandlerTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private OrdersHttpHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrdersHttpHandler(OrderService.withSampleData());
    }

    // ---------- GET /orders/{id} ----------

    @Test
    void getExistingOrderReturns200() throws Exception {
        APIGatewayProxyResponseEvent res = call("GET", "/orders/{id}", Map.of("id", "ORD-001"), null, null);

        assertEquals(200, res.getStatusCode().intValue());
        assertEquals("CUST-42", body(res).get("customerId").asText());
    }

    @Test
    void getMissingOrderReturns404() throws Exception {
        APIGatewayProxyResponseEvent res = call("GET", "/orders/{id}", Map.of("id", "ORD-999"), null, null);

        assertEquals(404, res.getStatusCode().intValue());
        assertEquals("ORDER_NOT_FOUND", body(res).get("error").asText());
        assertEquals("Order ORD-999 does not exist", body(res).get("message").asText());
    }

    // ---------- GET /orders?customerId= ----------

    @Test
    void findByCustomerReturns200WithList() throws Exception {
        APIGatewayProxyResponseEvent res = call("GET", "/orders", null, Map.of("customerId", "CUST-42"), null);

        assertEquals(200, res.getStatusCode().intValue());
        assertEquals(2, body(res).size());
    }

    @Test
    void findWithoutCustomerIdReturns400() throws Exception {
        APIGatewayProxyResponseEvent res = call("GET", "/orders", null, null, null);

        assertEquals(400, res.getStatusCode().intValue());
        assertEquals("MISSING_CUSTOMER_ID", body(res).get("error").asText());
    }

    // ---------- POST /orders ----------

    @Test
    void createValidOrderReturns201WithLocation() throws Exception {
        String json = "{\"customerId\":\"CUST-42\",\"product\":\"MacBook Air\",\"quantity\":1}";
        APIGatewayProxyResponseEvent res = call("POST", "/orders", null, null, json);

        assertEquals(201, res.getStatusCode().intValue());
        String id = body(res).get("id").asText();
        assertEquals("/orders/" + id, res.getHeaders().get("Location"));
    }

    @Test
    void createInvalidOrderReturns400() throws Exception {
        String json = "{\"customerId\":\"CUST-42\",\"product\":\"MacBook Air\",\"quantity\":0}";
        APIGatewayProxyResponseEvent res = call("POST", "/orders", null, null, json);

        assertEquals(400, res.getStatusCode().intValue());
        assertEquals("INVALID_REQUEST", body(res).get("error").asText());
    }

    @Test
    void createWithMalformedJsonReturns400() throws Exception {
        APIGatewayProxyResponseEvent res = call("POST", "/orders", null, null, "{not json");

        assertEquals(400, res.getStatusCode().intValue());
        assertEquals("Malformed JSON body", body(res).get("message").asText());
    }

    @Test
    void createWithoutBodyReturns400() throws Exception {
        APIGatewayProxyResponseEvent res = call("POST", "/orders", null, null, null);

        assertEquals(400, res.getStatusCode().intValue());
    }

    // ---------- Errores inesperados ----------

    @Test
    void unexpectedErrorReturns500WithoutStackTrace() throws Exception {
        OrderService broken = new OrderService() {
            @Override
            public Order getOrder(String id) {
                throw new IllegalStateException("database exploded");
            }
        };
        handler = new OrdersHttpHandler(broken);

        APIGatewayProxyResponseEvent res = call("GET", "/orders/{id}", Map.of("id", "ORD-001"), null, null);

        assertEquals(500, res.getStatusCode().intValue());
        assertEquals("INTERNAL_ERROR", body(res).get("error").asText());
        assertFalse(res.getBody().contains("database exploded"));
    }

    @Test
    void unknownRouteReturns404() throws Exception {
        APIGatewayProxyResponseEvent res = call("PUT", "/orders", null, null, null);

        assertEquals(404, res.getStatusCode().intValue());
    }

    // ---------- Ayudas ----------

    private APIGatewayProxyResponseEvent call(String method, String resource,
                                              Map<String, String> pathParams,
                                              Map<String, String> queryParams,
                                              String body) {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod(method)
                .withResource(resource)
                .withPathParameters(pathParams)
                .withQueryStringParameters(queryParams)
                .withBody(body);
        return handler.handleRequest(event, null);
    }

    private static JsonNode body(APIGatewayProxyResponseEvent res) throws Exception {
        return JSON.readTree(res.getBody());
    }
}