# Transaction Service

A Spring Boot application for managing banking transactions, designed to handle transaction recording, viewing, and management operations with high performance and scalability.

## Overview

This service provides a RESTful API for:
- Creating transactions (deposits, withdrawals, transfers)
- Updating transaction information
- Deleting transactions
- Listing transactions with pagination

## Technology Stack

- Java 21
- Spring Boot 3.2.0
- Maven for dependency management
- In-memory storage (no persistence)
- Docker/Kubernetes for containerization and orchestration

## External Libraries (outside JDK)

| Library | Purpose |
|---------|---------|
| Spring Boot Starter Web | Core Spring MVC support for RESTful APIs |
| Spring Boot Starter Validation | Bean validation (JSR-380) |
| Spring Boot Starter Cache | Caching infrastructure support |
| Spring Boot Starter Actuator | Production-ready features like health checks |
| Caffeine | High-performance, near-optimal caching library |
| SpringDoc OpenAPI | API documentation with Swagger UI |

## API Endpoints

- `POST /api/transactions` - Create a new transaction
- `GET /api/transactions` - List transactions with pagination
- `PUT /api/transactions/{id}` - Update transaction details
- `DELETE /api/transactions/{id}` - Delete a transaction

## Data Validation

The application implements comprehensive validation for transaction data:

| Field | Validation Rules |
|-------|-----------------|
| Account Number | Required, numeric, 5-20 digits |
| Amount | Required, positive value |
| Transaction Type | Required, must be one of: DEPOSIT, WITHDRAWAL, TRANSFER |
| Description | Optional, max 255 characters |
| Destination Account | Required for TRANSFER type, must be different from source account |

## Error Handling

The API handles various error scenarios with appropriate HTTP status codes:

| Error Scenario | HTTP Status | Description |
|----------------|-------------|-------------|
| Transaction not found | 404 Not Found | When attempting to retrieve, update, or delete a non-existent transaction |
| Duplicate transaction | 409 Conflict | When attempting to create a transaction with the same details within 60 seconds |
| Validation errors | 400 Bad Request | When request data fails validation constraints |
| Invalid pagination parameters | 400 Bad Request | When page number is negative or page size is less than 1 |
| Server errors | 500 Internal Server Error | For unexpected server-side errors |

## Running the Application

### Local Development

```bash
mvn clean install
mvn spring-boot:run
```

### Docker

```bash
docker build -t transaction-service .
docker run -p 8080:8080 transaction-service
```

### Kubernetes

```bash
kubectl apply -f kubernetes/deployment.yaml
```

## Performance Features

- Caffeine caching for frequently accessed data
- Thread-safe in-memory storage with ConcurrentHashMap
- Pagination support for large data sets
- Efficient exception handling
- Virtual threads (Project Loom) for improved concurrency
- Optimized thread pool configuration
- Tomcat connection pool tuning

## Performance Testing

The application includes stress tests to verify it can handle high load:

- Concurrent creation test: Simulates multiple users creating transactions simultaneously
- Mixed operations test: Simulates realistic workload with a mix of operations (create, update, delete, query)

Performance targets:
- Throughput: >50 transactions/second for creation operations
- Throughput: >30 operations/second for mixed workload
- Response time: <100ms for 95% of requests under normal load

## Testing

The application includes:
- Unit tests for service and controller layers
- Integration tests for API endpoints
- Validation tests for data integrity
- Stress tests for performance validation
- Error handling tests for exceptional cases 