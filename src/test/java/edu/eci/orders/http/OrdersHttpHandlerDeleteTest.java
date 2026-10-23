package edu.eci.orders.http;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import edu.eci.orders.service.OrderService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrdersHttpHandlerDeleteTest {

    private final OrdersHttpHandler handler = new OrdersHttpHandler(OrderService.withSampleData());

    @Test
    void deleteExistingOrderReturns204WithoutBody() {
        APIGatewayProxyResponseEvent res = call("DELETE", "ORD-001");

        assertEquals(204, res.getStatusCode().intValue());
        assertNull(res.getBody());
    }

    @Test
    void deleteMissingOrderReturns404() {
        APIGatewayProxyResponseEvent res = call("DELETE", "ORD-999");

        assertEquals(404, res.getStatusCode().intValue());
        assertTrue(res.getBody().contains("ORDER_NOT_FOUND"));
    }

    @Test
    void getAfterDeleteReturns404() {
        call("DELETE", "ORD-001");

        APIGatewayProxyResponseEvent res = call("GET", "ORD-001");

        assertEquals(404, res.getStatusCode().intValue());
    }

    private APIGatewayProxyResponseEvent call(String method, String id) {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod(method)
                .withResource("/orders/{id}")
                .withPathParameters(Map.of("id", id));
        return handler.handleRequest(event, null);
    }
}