# Library App Borrow Service / 貸出サービス

Distributed Library System — Borrow Service

分散型図書館システム — 貸出サービス

---

## Overview / 概要

The Borrow Service is the core transactional microservice within the distributed library platform.

It is responsible for managing book borrowing and return operations, enforcing borrowing consistency rules, validating loan states, and publishing domain events through Kafka for downstream services.

The service acts as the central orchestration layer of the library ecosystem by coordinating inventory updates, analytical processing, and historical record generation through asynchronous event-driven communication.

---

Borrow Service は分散型図書館システムにおける中核トランザクションサービスです。

書籍の貸出・返却処理、貸出整合性検証、状態管理を担当し、Kafka を通じてドメインイベントを配信します。

本サービスは、在庫管理・分析・履歴管理サービスへイベント連携を行うことで、システム全体のオーケストレーション層として機能します。

---

## Service Boundaries / サービス境界

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

## Responsibilities / 責務

### English

- Process borrowing requests
- Process return requests
- Validate borrowing eligibility
- Publish borrowing events
- Publish return events
- Coordinate inventory updates
- Maintain transactional consistency
- Enforce borrowing lifecycle rules

### 日本語

- 貸出処理
- 返却処理
- 貸出可否検証
- 貸出イベント配信
- 返却イベント配信
- 在庫連携管理
- トランザクション整合性維持
- 貸出ライフサイクル管理

---

## Technology Stack / 技術スタック

| Category | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 2.7 |
| Messaging | Kafka |
| Persistence | Spring Data JPA |
| Database | PostgreSQL / H2 |
| API Documentation | Springdoc OpenAPI |
| Validation | Bean Validation |
| Security | OAuth2 Resource Server |
| Testing | JUnit 5 / Mockito / JaCoCo / Instancio |
| Monitoring | Spring Actuator / Prometheus |
| Containerization | Docker |
| CI/CD | GitHub Actions |

---

## API Endpoints / API エンドポイント

### Borrow Operations / 貸出操作

#### Borrow Books

``` http
POST /borrow/{memberCardUUID}
```

Creates a new borrowing transaction and publishes borrowing events.

新しい貸出トランザクションを生成し、Kafka イベントを配信します。
---

Return Borrowed Books

``` http
POST /return/{memberCardUUID}/{borrowUUID}
```

Processes a return transaction and publishes return events.

返却処理を実行し、返却イベントを配信します。

---

Event Processing / イベント処理

Published Kafka Topics

Topic| Description

"library.borrow.v1"| Borrow creation events

"library.return.v1"| Return completion events

---

Processing Flow

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

Borrow Lifecycle / 貸出ライフサイクル

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

API Documentation / API ドキュメント

/swagger-ui/

---

Local Development / ローカル開発

Requirements

- Java 21
- Maven
- Docker
- PostgreSQL
- Kafka

---

Run

docker compose up --build

---

Testing / テスト

./mvnw verify

Includes

- Unit tests
- Integration tests
- Controller tests
- Kafka event validation
- Coverage verification
- Complexity verification

---

日本語

含まれるテスト:

- ユニットテスト
- 統合テスト
- コントローラテスト
- Kafka イベント検証
- カバレッジ検証
- 複雑度検証

---

Build Quality / 品質保証

The CI pipeline enforces:

- Automated test execution
- Coverage thresholds
- Cyclomatic complexity checks
- Pull request validation
- Docker image publication
- Branch protection workflows

---

日本語

CI パイプラインでは以下を保証します:

- 自動テスト実行
- カバレッジ閾値管理
- サイクロマティック複雑度検証
- Pull Request 検証
- Docker イメージ配布
- ブランチ保護ワークフロー

---

Configuration / 設定

SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
SPRING_KAFKA_BOOTSTRAP_SERVERS=
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=

Environment-driven configuration.

環境変数ベースで構成されています。

---

Monitoring / モニタリング

/actuator/health

/actuator/prometheus

---

Architectural Role / アーキテクチャ上の役割

The Borrow Service represents the transactional core of the distributed library system.

Most downstream services rely on the events emitted by this service to maintain consistency across the platform.

---

Borrow Service は分散型図書館システム全体の中核トランザクションサービスです。

下流サービスは、本サービスが配信するイベントを利用して整合性を維持します。

---

License / ライセンス

MIT
