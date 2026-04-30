# 📦 Order Service – E-Commerce Microservice

## 📌 Overview

The **Order Service** is a critical microservice in a distributed e-commerce system responsible for **order creation, lifecycle management, and transactional consistency across services**.

It implements a **synchronous orchestration-based flow with compensating transactions**, ensuring reliability even in partial failure scenarios.

---

## 🔗 Links

* 📂 GitHub Repository: https://github.com/ekanath-smr/ecommerce-order-service
* 📘 Swagger UI: http://localhost:8090/swagger-ui/index.html
* 📄 OpenAPI Docs: http://localhost:8090/v3/api-docs

---

## 🚀 Key Features

### 📦 Order Management

* Create order with multiple items
* Fetch order by ID
* Fetch orders by user (paginated)
* Fetch order using idempotency key
* Update order status
* Cancel order

---

### 🔁 Idempotent Order Creation

* Uses `Idempotency-Key` header
* Prevents duplicate order creation
* Ensures safe retries in distributed systems

---

### 🧮 Pricing & Order Calculation

* Price snapshot taken from Product Service
* Total amount calculated dynamically
* Ensures consistency even if product prices change later

---

## 🔐 Security & Access Control

* JWT-based authentication
* Role-based authorization (RBAC)
* User-level data isolation

| Role  | Permissions                     |
| ----- | ------------------------------- |
| USER  | Create, view, cancel own orders |
| ADMIN | (Extendable)                    |

---

## 🔗 Inter-Service Communication

* **Product Service**

    * Fetch product details
    * Validate product existence

* **Inventory Service**

    * Validate stock
    * Reserve stock
    * Confirm sale
    * Release stock (rollback)

---

### 🧠 Design Principle

* Order Service acts as an **orchestrator**
* External services are **source of truth**
* Implements **defensive programming with fallbacks**

---

## ⚡ Fault Tolerance & Resilience

Integrated **Resilience4j**:

* Circuit Breaker → Prevent cascading failures
* Retry → Handles transient issues
* Fallback methods → Graceful degradation

---

## 🧠 Distributed Transaction Design

### Current Approach:

**Synchronous Orchestration + Compensating Transactions**

Order flow:

1. Create order (CREATED)
2. Validate product & stock
3. Reserve stock → (RESERVED)
4. Confirm stock → (CONFIRMED)
5. On failure:

    * Undo confirmed sales
    * Release reserved stock
    * Mark order as FAILED

---

### 🔁 Compensation Logic

* `undoConfirmedSale()` → revert sold items
* `releaseStock()` → free reserved stock

---

### ⚠️ Note

> Designed for future migration to **Saga Pattern (event-driven / Kafka)** for better scalability and reliability.

---

## 🏗️ Tech Stack

* Java 21
* Spring Boot
* Spring Security (JWT + RBAC)
* Spring Data JPA (Hibernate)
* MySQL
* OpenFeign
* Resilience4j
* Spring Retry
* Lombok
* Maven
* Swagger / OpenAPI

---

## 📂 Project Structure

```
src/main/java/com/example/ecommerce_order_service
├── controllers
├── services
├── clients
├── repositories
├── models
├── dtos
├── mappers
├── security
├── advices
└── configs

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

```
spring.datasource.url=jdbc:mysql://localhost:3306/orderService
spring.jpa.hibernate.ddl-auto=update
```

### External Services

```
product.service.url=http://localhost:8080
inventory.service.url=http://localhost:8070
```

### JWT

```
jwt.secret=your-base64-secret
```

### Resilience4j

```
resilience4j.circuitbreaker.instances.inventoryService.failureRateThreshold=50
resilience4j.retry.instances.inventoryService.maxAttempts=3
```

---

## 🛡️ Error Handling

Centralized using `@RestControllerAdvice`.

### Handles:

* OrderNotFoundException
* OrderAccessDeniedException
* InvalidOrderRequestException
* DuplicateOrderRequestException
* InsufficientStockException
* OrderAlreadyCancelledException
* OrderAlreadyDeliveredException
* OrderStatusTransitionException
* InventoryServiceException
* ProductServiceException

---

## 📈 Logging Strategy

| Level | Usage                                          |
| ----- | ---------------------------------------------- |
| INFO  | Business flow (order creation, status updates) |
| WARN  | Invalid operations, user errors                |
| ERROR | System failures, external service issues       |

---

## 🔄 Example Order Flow

1. User creates order with idempotency key
2. Product Service validates product
3. Inventory Service validates stock
4. Stock reserved → confirmed
5. Order marked CONFIRMED

### Failure Case:

* Partial success triggers rollback:

    * Undo sale
    * Release stock
    * Mark order FAILED

---

## 🚧 Future Enhancements

* Kafka-based Saga orchestration
* Distributed tracing (Zipkin / OpenTelemetry)
* Centralized logging (ELK Stack)
* Service discovery (Eureka)
* API Gateway integration
* Rate limiting
* Payment service integration

---

## 💡 Design Highlights

* Idempotent order creation
* Compensating transaction pattern
* Strong validation across services
* Clean separation (clients, services, mappers)
* Production-grade exception handling & logging
* Resilience4j integration for fault tolerance

---

## 🏆 Resume Highlights

* Designed Order microservice using Spring Boot & MySQL
* Implemented distributed transaction handling with compensating logic
* Integrated OpenFeign for inter-service communication
* Applied Resilience4j (Circuit Breaker + Retry + Fallback)
* Built idempotent order creation mechanism
* Implemented secure APIs with JWT-based authentication
* Designed rollback strategies for partial failures

---

## 🧑‍💻 Author

**Ekanath S M R**
Backend Engineer | Java + Spring Boot Developer

GitHub:
https://github.com/ekanath-smr
