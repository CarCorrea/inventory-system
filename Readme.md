# Sistema de Inventario Distribuido - Prototipo

## Descripción

Este es un prototipo simplificado de un sistema de inventario distribuido para una cadena de retail, implementado con Spring Boot 3.5.5 y Java 17. El sistema maneja la disponibilidad de productos, reservas temporales y actualizaciones de stock con consistencia y tolerancia a fallos.

## Características Principales

### 🏗️ Arquitectura
- **Patrón CQRS**: Separación de operaciones de lectura y escritura
- **Event-Driven**: Comunicación basada en eventos (simulado)
- **Microservicios**: Servicios independientes para diferentes responsabilidades
- **Cache Multi-Nivel**: Caffeine para cache en memoria

### 🔒 Consistencia y Concurrencia
- **Pessimistic Locking**: Para operaciones críticas de reserva
- **Optimistic Locking**: Con manejo de retry automático
- **Transacciones ACID**: Isolation SERIALIZABLE para operaciones críticas
- **Reservas con TTL**: Limpieza automática de reservas expiradas

### 🛡️ Tolerancia a Fallos
- **Circuit Breaker**: Resilience4j para evitar cascadas de fallos
- **Retry Pattern**: Reintentos automáticos con backoff exponencial
- **Graceful Degradation**: Respuestas desde cache cuando servicios fallan
- **Health Checks**: Actuator para monitoreo de salud

### 📊 Base de Datos
- **H2 In-Memory**: Base de datos en memoria para prototipo
- **JPA/Hibernate**: ORM con validaciones automáticas
- **Migrations**: Inicialización automática de datos de prueba

## Estructura del Proyecto

```
src/main/java/com/retail/inventory/
├── domain/           # Entidades de dominio
├── repository/       # Repositorios JPA
├── service/          # Lógica de negocio
├── controller/       # APIs REST
├── dto/             # Data Transfer Objects
├── exception/       # Excepciones personalizadas
└── config/          # Configuraciones


## APIs Principales

### Consulta de Disponibilidad
```http
GET /api/v1/inventory/{productId}/availability?storeId={storeId}
```

### Reserva de Producto
```http
POST /api/v1/inventory/reserve
Content-Type: application/json

{
  "productId": "SKU001",
  "storeId": "STORE001", 
  "quantity": 2,
  "customerId": "CUST001",
  "reservationTtl": 1800
}
```

### Liberar Reserva
```http
DELETE /api/v1/inventory/release/{reservationId}
```

### Confirmar Reserva
```http
POST /api/v1/inventory/confirm/{reservationId}
```

### Ajuste de Stock
```http
PUT /api/v1/inventory/adjust
Content-Type: application/json

{
  "batchId": "BATCH001",
  "source": "POS_SYSTEM",
  "adjustments": [{
    "productId": "SKU001",
    "storeId": "STORE001",
    "delta": -2,
    "reason": "SALE",
    "referenceId": "order_123"
  }]
}
```

## Ejecución

### Requisitos
- Java 17+
- Maven 3.6+

### Comandos
```bash
# Compilar y ejecutar tests
mvn clean test

# Ejecutar aplicación
mvn spring-boot:run

# Crear JAR
mvn clean package
```

### Acceso
- **Aplicación**: http://localhost:8080/api/v1
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health
- **Métricas**: http://localhost:8080/actuator/metrics

### Datos de Prueba
La aplicación inicializa automáticamente:
- 5 productos (SKU001-SKU005)
- 5 tiendas (STORE001-STORE005)
- Stock aleatorio (10-50 unidades por producto/tienda)