# 📦 Order Service – E-Commerce Microservice

## 📌 Overview

The **Order Service** is a core microservice in a distributed e-commerce platform responsible for **order creation, lifecycle management, transactional consistency, and secure order operations**.

The service follows an **Orchestration-Based Distributed Transaction Model** with **Compensating Transactions**, integrates with multiple downstream services, and participates in a cloud-native microservices ecosystem using **Service Discovery**, **API Gateway**, and **Load Balancing**.

---

## 🏗️ Architecture Position

```text
Client
   │
   ▼
API Gateway
   │
   ▼
Order Service
   │
   ├── Product Service
   │
   └── Inventory Service

All services registered with Eureka Service Discovery
```

### Integrated Components

* Service Discovery (Eureka)
* API Gateway
* Client-side Load Balancing
* JWT Authentication
* Distributed Transaction Handling
* Resilience4j Fault Tolerance
* OpenFeign Service Communication

---

## 🚀 Key Features

### 📦 Order Management

* Create Order
* Get Order By ID
* Get Orders By User
* Cancel Order
* Update Order Status
* Fetch Order By Idempotency Key

---

### 🔁 Idempotent Order Creation

* Uses `Idempotency-Key` header
* Prevents duplicate order submissions
* Supports safe retries
* Ensures consistency in distributed environments

---

### 🧮 Pricing & Calculation

* Fetches latest product details from Product Service
* Captures price snapshot during order creation
* Calculates total order amount dynamically
* Preserves historical pricing accuracy

---

## 🔐 Security

### Authentication

* JWT-Based Authentication

### Authorization

Role-Based Access Control (RBAC)

| Role  | Permissions                              |
| ----- | ---------------------------------------- |
| USER  | Create, view and cancel own orders       |
| ADMIN | Extendable for administrative operations |

### Security Features

* Stateless Authentication
* Request Filtering
* User-Level Resource Isolation
* Secure API Access

---

## 🔗 Service Discovery

Integrated with **Netflix Eureka**.

### Benefits

* Dynamic Service Registration
* Service Lookup
* Decoupled Service Communication
* High Availability Support

Example:

```text
ORDER-SERVICE
PRODUCT-SERVICE
INVENTORY-SERVICE
API-GATEWAY
```

All services register automatically with Eureka on startup.

---

## ⚖️ Client-Side Load Balancing

Implemented using Spring Cloud LoadBalancer.

Instead of calling:

```text
http://localhost:8080
```

Services communicate using:

```text
http://PRODUCT-SERVICE
```

LoadBalancer automatically routes traffic across available service instances.

### Example

```text
PRODUCT-SERVICE
 ├── Instance 1
 ├── Instance 2
 └── Instance 3
```

Requests are distributed automatically.

---

## 🚪 API Gateway Integration

All external traffic is routed through API Gateway.

### Benefits

* Centralized Routing
* Security Enforcement
* Authentication Layer
* Request Filtering
* Future Rate Limiting Support

Example:

```text
Client
   ↓
API Gateway
   ↓
Order Service
```

---

## 🔗 Inter-Service Communication

Implemented using OpenFeign.

### Product Service

Responsibilities:

* Validate product existence
* Fetch product information
* Fetch product pricing

### Inventory Service

Responsibilities:

* Validate stock
* Reserve stock
* Confirm stock sale
* Release stock on rollback

---

## ⚡ Fault Tolerance & Resilience

Implemented using Resilience4j.

### Circuit Breaker

Prevents cascading failures when downstream services become unavailable.

### Retry

Automatically retries transient failures.

### Fallback Methods

Graceful degradation when external services fail.

---

## 🧠 Distributed Transaction Design

### Current Implementation

#### Orchestration-Based Distributed Transaction

Order Service acts as the orchestrator.

Workflow:

```text
Create Order
      ↓
Validate Product
      ↓
Validate Stock
      ↓
Reserve Stock
      ↓
Confirm Sale
      ↓
Confirm Order
```

---

### Compensation Logic

If any step fails:

```text
Undo Confirmed Sale
      ↓
Release Reserved Stock
      ↓
Mark Order Failed
```

Implemented through compensating transactions.

---

### Why This Design?

* Avoids distributed database transactions
* Maintains service autonomy
* Improves scalability
* Supports eventual consistency

---

## 🔄 Example Order Flow

### Success Scenario

```text
User Creates Order
        ↓
Product Validation
        ↓
Stock Validation
        ↓
Reserve Stock
        ↓
Confirm Sale
        ↓
Order Confirmed
```

---

### Failure Scenario

```text
User Creates Order
        ↓
Reserve Stock
        ↓
Sale Confirmation Fails
        ↓
Undo Sale
        ↓
Release Stock
        ↓
Order Failed
```

---

## 🛡️ Error Handling

Centralized using:

```java
@RestControllerAdvice
```

### Supported Exceptions

* OrderNotFoundException
* OrderAccessDeniedException
* DuplicateOrderRequestException
* InvalidOrderRequestException
* InsufficientStockException
* InventoryServiceException
* ProductServiceException
* OrderAlreadyCancelledException
* OrderAlreadyDeliveredException
* OrderStatusTransitionException

---

## 📈 Logging Strategy

| Level | Purpose                                 |
| ----- | --------------------------------------- |
| INFO  | Business operations                     |
| WARN  | Invalid requests and recoverable issues |
| ERROR | Failures and unexpected exceptions      |

Examples:

* Order creation
* Status updates
* Rollback execution
* External service failures

---

## 🏗️ Technology Stack

### Backend

* Java 21
* Spring Boot 3

### Security

* Spring Security
* JWT Authentication

### Persistence

* Spring Data JPA
* Hibernate
* MySQL

### Cloud & Microservices

* Eureka Service Discovery
* Spring Cloud Gateway
* Spring Cloud LoadBalancer
* OpenFeign

### Reliability

* Resilience4j
* Spring Retry

### Documentation

* Swagger / OpenAPI

### Utilities

* Lombok
* Maven

### Testing

* JUnit 5
* Mockito
* H2 Database

---

## 📂 Project Structure

```text
src/main/java/com/example/ecommerce_order_service

├── controllers
├── services
├── repositories
├── clients
│   ├── productClient
│   └── inventoryClient
├── models
├── dtos
├── mappers
├── security
├── advices
├── configs
└── exceptions

src/test/java
└── services
```

---

## 🔄 API Endpoints

### Order APIs

| Method | Endpoint                     |
| ------ | ---------------------------- |
| POST   | /orders                      |
| GET    | /orders/{orderId}            |
| GET    | /orders                      |
| GET    | /orders/by-idempotency/{key} |
| PATCH  | /orders/{orderId}/status     |
| PATCH  | /orders/{orderId}/cancel     |

---

## ⚙️ Configuration

### Database

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/orderService
spring.jpa.hibernate.ddl-auto=update
```

### Eureka

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

### JWT

```properties
jwt.secret=your-secret-key
```

### Resilience4j

```properties
resilience4j.circuitbreaker.instances.inventoryService.failureRateThreshold=50
resilience4j.retry.instances.inventoryService.maxAttempts=3
```

---

## 🚧 Future Enhancements

* Kafka-Based Saga Orchestration
* Payment Service Integration
* Distributed Tracing (Zipkin/OpenTelemetry)
* ELK Stack Logging
* Redis Caching
* Rate Limiting
* Metrics Dashboard
* Kubernetes Deployment
* CI/CD Pipeline

---

## 💡 Design Highlights

* Distributed transaction handling
* Compensating transaction mechanism
* Idempotent order creation
* Service Discovery integration
* API Gateway integration
* Client-side load balancing
* JWT-secured APIs
* Resilience4j fault tolerance
* Production-grade exception handling
* Clean layered architecture

---

## 🏆 Resume Highlights

* Designed and developed Order Service using Java, Spring Boot, and MySQL.
* Implemented orchestration-based distributed transaction handling with compensation logic.
* Integrated Eureka Service Discovery for dynamic service registration and lookup.
* Implemented API Gateway routing for centralized request management.
* Configured client-side load balancing for high availability and traffic distribution.
* Built secure REST APIs using JWT Authentication and Role-Based Access Control.
* Integrated OpenFeign for inter-service communication.
* Applied Resilience4j Circuit Breaker, Retry, and Fallback mechanisms.
* Developed idempotent order creation workflow for safe distributed retries.
* Designed rollback strategies to handle partial failures and maintain consistency.

---

## 👨‍💻 Author

**Ekanath S M R**

Backend Engineer | Java | Spring Boot | Microservices

GitHub:
https://github.com/ekanath-smr
