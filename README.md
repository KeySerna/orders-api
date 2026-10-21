# Orders API — API REST serverless con AWS Lambda y API Gateway

> Entorno: JDK 21 compilando con `maven.compiler.release=17`. Runtime de Lambda: Java 17.

## 1. Diseño de la API (contrato)

### Recurso: pedido (Order)

```json
{
  "id": "ORD-001",
  "customerId": "CUST-42",
  "product": "MacBook Air",
  "quantity": 1
}
```

### Formato de error (igual para todos los errores)

```json
{
  "error": "ORDER_NOT_FOUND",
  "message": "Order ORD-999 does not exist"
}
```

- `error`: código fijo en mayúsculas, para que un programa lo pueda leer.
- `message`: explicación para una persona.

### Operaciones

| Operación | Método | URL | Parámetros | Cuerpo de la petición | Éxito | Errores |
|---|---|---|---|---|---|---|
| Crear pedido | POST | `/orders` | — | `{"customerId","product","quantity"}` | **201 Created** + pedido creado (con `id`) + header `Location: /orders/{id}` | **400** `INVALID_REQUEST` (campos vacíos, quantity ≤ 0, JSON mal formado) · **500** `INTERNAL_ERROR` |
| Obtener pedido | GET | `/orders/{id}` | Ruta: `id` | — | **200 OK** + pedido | **404** `ORDER_NOT_FOUND` · **500** `INTERNAL_ERROR` |
| Buscar por cliente | GET | `/orders?customerId={customerId}` | Consulta: `customerId` | — | **200 OK** + lista de pedidos (puede ser `[]`) | **400** `MISSING_CUSTOMER_ID` · **500** `INTERNAL_ERROR` |
| Eliminar pedido (reto final) | DELETE | `/orders/{id}` | Ruta: `id` | — | **204 No Content** (sin cuerpo) | **404** `ORDER_NOT_FOUND` · **500** `INTERNAL_ERROR` |

### Ejemplos de errores

```json
{ "error": "INVALID_REQUEST", "message": "quantity must be greater than 0" }
```
```json
{ "error": "MISSING_CUSTOMER_ID", "message": "customerId is required" }
```
```json
{ "error": "INTERNAL_ERROR", "message": "Unexpected error" }
```

## 2. Preguntas de diseño

**¿Cuáles son los recursos de la API?**
Un único recurso: los **pedidos** (`/orders`). `/orders` es la colección y `/orders/{id}` es un pedido específico.

**¿Por qué `/orders/{id}` para recuperar un pedido?**
Porque el `id` identifica un recurso único. En REST, la identidad del recurso va en la ruta: `/orders/ORD-42` *es* el pedido ORD-42.

**¿Por qué `customerId` va como parámetro de consulta?**
Porque no identifica un pedido, sino que **filtra** la colección de pedidos. Los filtros sobre una colección se expresan con query parameters (`/orders?customerId=CUST-42`).

**¿Qué devuelve `POST /orders` si tiene éxito?**
**201 Created**, el pedido creado con su `id` generado por el servidor y el header `Location: /orders/{id}` que indica dónde consultarlo.

**¿Qué devuelve la API si el pedido no existe?**
**404 Not Found** con `{"error":"ORDER_NOT_FOUND","message":"Order ORD-999 does not exist"}`.

**¿Qué debe contener una respuesta de error?**
Un código de error fijo (`error`), para que el cliente lo procese, y un mensaje legible (`message`). No debe incluir detalles internos como trazas de Java (*stack traces*).

**Decisión adicional:** si un cliente no tiene pedidos, `GET /orders?customerId=...` devuelve **200** con `[]` y no 404. La búsqueda funcionó, simplemente no hubo resultados.

## 3. Parte I — Aplicación en Java puro

### Estructura

```
src/main/java/edu/eci/orders/
├── model/       Order, CreateOrderRequest
├── exception/   OrderException, InvalidOrderException, OrderNotFoundException
└── service/     OrderService (createOrder, getOrder, getOrdersByCustomer)
src/test/java/edu/eci/orders/service/
└── OrderServiceTest (12 pruebas JUnit)
```

Los pedidos se guardan en memoria (`ConcurrentHashMap`). Los ids se generan como `ORD-001`, `ORD-002`, ...

### Cómo compilar y probar

```bash
mvn test       # ejecuta las pruebas
mvn package    # genera target/orders-api-1.0.jar
```

### Punto de control 1

**¿Se puede probar la funcionalidad sin AWS?**
Sí. `mvn test` ejecuta las 12 pruebas localmente sin conexión a AWS, porque `OrderService` es Java puro.

**¿La aplicación necesita un servidor web?**
No. Es un conjunto de clases Java que se usan llamando métodos normales.

**¿Requiere Spring Boot?**
No. La única dependencia es JUnit, y solo para pruebas.

**¿Qué contiene el JAR generado?**
Las clases compiladas (`.class`) de los paquetes `model`, `exception` y `service`, más `META-INF/MANIFEST.MF`. No incluye las pruebas ni JUnit (scope `test`).

**¿Qué partes de la aplicación conocen HTTP?**
Ninguna. Los errores usan códigos de negocio (`ORDER_NOT_FOUND`, `INVALID_REQUEST`), no códigos HTTP. Convertirlos a 404 o 400 será responsabilidad de una capa posterior (API Gateway o el adaptador HTTP).