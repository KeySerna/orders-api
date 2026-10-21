
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