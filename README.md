# Library App Borrow Service

Distributed Library System — Borrow Service

---

## Overview

The Borrow Service is the core transactional microservice within the distributed library platform.

It is responsible for managing book borrowing and return operations, enforcing borrowing consistency rules, validating loan states, and publishing domain events through Kafka for downstream services.

The service acts as the central orchestration layer of the library ecosystem by coordinating inventory updates, analytical processing, and historical record generation through asynchronous event-driven communication.

---

## Service Boundaries

### Provides

- Borrow transaction management
- Return transaction management
- Loan state validation
- Kafka event publication
- Borrow consistency enforcement
- RESTful borrowing APIs
- Inventory coordination
- Borrow lifecycle orchestration

### Does Not Handle

- Book catalog metadata management
- Recommendation generation
- Analytical aggregation
- User account management
- Authentication provider management
- Search indexing

---

## Badges

<!-- Code Quality & Tests -->

[![Tests](https://github.com/damouu/library-app-borrow/actions/workflows/run-tests.yml/badge.svg?branch=test)](https://github.com/damouu/library-app-borrow/actions/workflows/run-tests.yml)
[![Merge PR](https://github.com/damouu/library-app-borrow/actions/workflows/merge-pr.yml/badge.svg)](https://github.com/damouu/library-app-borrow/actions/workflows/merge-pr.yml)
[![Prepare](https://github.com/damouu/library-app-borrow/actions/workflows/prepare.yml/badge.svg)](https://github.com/damouu/library-app-borrow/actions/workflows/prepare.yml)
[![YouTrack-Staging](https://github.com/damouu/library-app-borrow/actions/workflows/youtrack-staging.yml/badge.svg)](https://github.com/damouu/library-app-borrow/actions/workflows/youtrack-staging.yml)
[![YouTrack Closed](https://github.com/damouu/library-app-borrow/actions/workflows/youtrack-done.yml/badge.svg)](https://github.com/damouu/library-app-borrow/actions/workflows/youtrack-done.yml)

<!-- Coverage -->

[![Codecov](https://codecov.io/gh/damouu/library-app-borrow/branch/test/graph/badge.svg)](https://codecov.io/gh/damouu/library-app-borrow)

<!-- Docker -->

[![Docker Build](https://github.com/damouu/library-app-borrow/actions/workflows/build-and-publish.yml/badge.svg)](https://github.com/damouu/library-app-borrow/actions/workflows/build-and-publish.yml)
[![Docker Image](https://img.shields.io/docker/v/damou/library-app-borrow?label=docker&logo=docker)](https://hub.docker.com/r/damou/library-app-borrow)
[![Docker Pulls](https://img.shields.io/docker/pulls/damou/library-app-borrow?logo=docker)](https://hub.docker.com/r/damou/library-app-borrow)

<!-- Git / Version -->

[![Git Tag](https://img.shields.io/github/v/tag/damouu/library-app-borrow?logo=github)](https://github.com/damouu/library-app-borrow/tags)

<!-- Observability / Monitoring -->

![OpenTelemetry](https://img.shields.io/badge/OpenTelemetry-instrumented-brightgreen)
![Kafka](https://img.shields.io/badge/Kafka-integrated-orange)
![Prometheus](https://img.shields.io/badge/Prometheus-monitored-blue)

---

## Responsibilities

- Process borrowing requests
- Process return requests
- Validate borrowing eligibility
- Publish borrowing events
- Publish return events
- Coordinate inventory updates
- Maintain transactional consistency
- Enforce borrowing lifecycle rules

---

## Technology Stack

| Category              | Technology                          |
|-----------------------|-------------------------------------|
| Runtime               | Java 21                              |
| Framework             | Spring Boot 2.7                      |
| Messaging             | Kafka                                |
| Persistence           | Spring Data JPA                      |
| Database              | PostgreSQL / H2                      |
| API Documentation     | Springdoc OpenAPI                    |
| Validation            | Bean Validation                      |
| Security              | OAuth2 Resource Server               |
| Testing               | JUnit 5 / Mockito / JaCoCo / Instancio |
| Monitoring            | Spring Actuator / Prometheus         |
| Containerization      | Docker                               |
| CI/CD                 | GitHub Actions                       |

---

## API Endpoints

### Borrow Operations

#### Borrow Books

```http
POST /borrow/{memberCardUUID}
```

Creates a new borrowing transaction and publishes borrowing events.

---

#### Return Borrowed Books

```http
POST /return/{memberCardUUID}/{borrowUUID}
```

Processes a return transaction and publishes return events.

---

## Event Processing

### Published Kafka Topics

| Topic                | Description              |
|----------------------|--------------------------|
| `library.borrow.v1`  | Borrow creation events   |
| `library.return.v1`  | Return completion events |

---

## Processing Flow

Borrow Request

↓

Validation Layer

↓

Persistence Layer

↓

Kafka Event Publication

↓

Downstream Service Consumption

---

## Borrow Lifecycle

Client Request

↓

Borrow Validation

↓

Loan Persistence

↓

Kafka Borrow Event

↓

Inventory Update

↓

Analytics Aggregation

↓

Record Persistence

---

## API Documentation

`/swagger-ui/`

---

## Build Quality

The CI pipeline enforces:

- Automated test execution
- Coverage thresholds
- Cyclomatic complexity checks
- Pull request validation
- Docker image publication
- Branch protection workflows

---

## Configuration

```env
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
SPRING_KAFKA_BOOTSTRAP_SERVERS=
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=
```

Environment-driven configuration.

---

## Monitoring

`/actuator/health`

`/actuator/prometheus`

---

## Architectural Role

The Borrow Service represents the transactional core of the distributed library system.

Most downstream services rely on the events emitted by this service to maintain consistency across the platform.

---

## License

MIT
