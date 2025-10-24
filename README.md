# Treatz - Food Delivery Platform

A microservices-based food delivery backend built with Spring Boot, demonstrating distributed system patterns and event-driven architecture.

## Tech Stack

- **Java 21** with Spring Boot 3.3.6
- **Spring Cloud** (Eureka, Gateway)
- **PostgreSQL** - database per service
- **RabbitMQ** - async messaging
- **JWT** authentication
- **MapStruct** for mapping

## Architecture

| Service | Port | Database | Role |
|---------|------|----------|------|
| discovery-service | 8761 | - | Service registry (Eureka) |
| api-gateway | 8080 | - | Entry point, JWT validation |
| auth-service | 9001 | treatz_auth_db | Authentication & authorization |
| restaurant-service | 9002 | treatz_restaurant_db | Restaurant & menu CRUD |
| order-service | 9003 | treatz_order_db | Order processing & payment |
| dispatch-service | 9005 | treatz_dispatch_db | Rider assignment |
| notification-service | 9004 | - | Event notifications |

**Communication:**
- REST for synchronous requests (gateway routing, data fetching)
- RabbitMQ for async events (order status changes trigger dispatch/notifications)

## Features

**Restaurant Service:**
- Restaurant and menu item management with categories
- Owner-based authorization (owners manage only their restaurants)
- BigDecimal pricing for accuracy

**Order Service:**
- Multi-item orders with menu validation
- Payment integration (UPI, Cards, COD) with simulated gateway
- Order lifecycle tracking (PENDING → CONFIRMED → PREPARING → READY_FOR_PICKUP → DISPATCHED → DELIVERED)
- Customer APIs for order history

**Dispatch Service:**
- Auto-assigns riders when orders are ready for pickup
- Rider availability tracking with race condition protection
- Scheduler retries failed assignments every 60 seconds
- Automatic rollback on failures

**Security:**
- JWT authentication across all services
- Role-based access control (Customer, Owner)

## Setup

**Prerequisites:** Java 21, Maven, PostgreSQL (5432), RabbitMQ (5672)

**Create databases:**
```sql
CREATE DATABASE treatz_auth_db;
CREATE DATABASE treatz_restaurant_db;
CREATE DATABASE treatz_order_db;
CREATE DATABASE treatz_dispatch_db;
```

**Start services:**
```bash
mvn clean install

# Start in order (wait 30s after discovery)
cd discovery-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd auth-service && mvn spring-boot:run
cd restaurant-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd dispatch-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

Eureka dashboard: `http://localhost:8761`

## API Examples

Base URL: `http://localhost:8080`

**Auth:**
```
POST /auth/register
POST /auth/login  # Returns JWT
```

**Restaurants:**
```
GET  /api/restaurants
POST /api/restaurants  # Owner only
PUT  /api/restaurants/{id}  # Owner only
```

**Orders:**
```
POST /api/orders
GET  /api/orders/my-orders
PUT  /api/orders/{id}/status  # Owner only
```

Include JWT: `Authorization: Bearer <token>`

## Technical Patterns

- **Service Discovery**: Netflix Eureka for dynamic service registration
- **API Gateway**: Centralized routing and security with Spring Cloud Gateway
- **Event-Driven**: RabbitMQ TopicExchange for decoupled communication
- **Database per Service**: Each service owns its data
- **Resilience**: Retry scheduler for handling missed events
- **Security**: Shared JWT secret, role-based access

## What I Learned

- Designing and implementing microservices architecture
- Synchronous vs asynchronous communication patterns
- Service discovery and API gateway implementation
- Event-driven design with message brokers
- Managing distributed data and eventual consistency
- JWT authentication in distributed systems
- Error handling and resilience patterns

---

Built to explore microservices and Spring Cloud.
