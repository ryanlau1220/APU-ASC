# APU-ASC

APU-ASC is an automotive service-centre platform for customers, staff, technicians, and managers. It covers the service journey from vehicle registration and appointment booking through quotation approval, work execution, evidence capture, payment, and after-service feedback.

The deployed application is available at [apu-asc.duckdns.org](https://apu-asc.duckdns.org).

## Implemented capabilities

- Role-scoped customer, staff, technician, and manager portals with object-level authorization.
- OIDC single sign-on with Keycloak, employee email invitations, password activation, password reset, and just-in-time user provisioning.
- Vehicle management, capacity-aware appointment booking, rescheduling, cancellation, and appointment reminders.
- Separate appointment and work-order lifecycles with explicit state transitions.
- Staff and manager work-order queues, technician assignment/reassignment, diagnostics, status progression, and evidence review.
- Service categories and packages, immutable submitted quotations, customer approval, and quotation revision history.
- Stripe-hosted Checkout for customer payments with webhook-verified payment status updates.
- First-class documents stored in S3-compatible object storage: repair evidence, invoices, and customer-visible after-service documents.
- In-app notifications, scheduled email reminders, role/user-scoped server-sent events, and TanStack Query cache invalidation.
- Hardened audit records for business operations and administrative activity.
- Customer feedback and after-service work-order visibility on web and mobile.
- Installable PWA and an Expo/React Native Android application with customer, technician, staff, and manager workflows.

## Architecture

The backend is a modular Spring Boot application. Business operations publish Spring Modulith events; listeners handle durable asynchronous work such as audit entries, notifications, emails, and live-update events. The web and mobile clients use REST APIs generated from the OpenAPI contract and receive targeted updates through SSE.

```text
Web PWA / Expo mobile
          │
          ▼
Spring Boot + Spring Modulith ── PostgreSQL / Flyway
          │                     ├─ Valkey cache
          │                     ├─ Keycloak identity
          │                     ├─ S3-compatible document storage
          │                     └─ Stripe Checkout + webhooks
          ▼
Micrometer / OpenTelemetry → Alloy → Prometheus, Loki, Tempo, Grafana
```

Backend modules: appointment, audit, document, feedback, notification, payment, quotation, scheduling, service catalogue, user, vehicle, and work order.

## Stack

| Area | Technology |
| --- | --- |
| Web | TanStack Start, React, TanStack Query, Tailwind CSS, shadcn/ui |
| Mobile | Expo, React Native, Expo Router, TanStack Query, NativeWind |
| Backend | Java 21, Spring Boot, Spring Security, Spring Modulith, Spring Data JPA |
| Data | PostgreSQL, Flyway, Valkey, MinIO / S3-compatible storage |
| Identity | Keycloak, OAuth 2.0 / OpenID Connect |
| Payments | Stripe Checkout and signed webhooks |
| Observability | Micrometer, OpenTelemetry, Alloy, Prometheus, Loki, Tempo, Grafana, Alertmanager, Sentry, PostHog |
| Delivery | Docker Compose, GitHub Actions, GHCR, Nginx Proxy Manager |
| Quality | JUnit 5, Vitest, Playwright, Biome, Spotless, Checkstyle, SpotBugs, PMD |
| API tooling | SpringDoc, Scalar, OpenAPI TypeScript, Orval |

## Local development

### Prerequisites

- Docker Engine with Docker Compose
- Node.js 22 with Corepack/pnpm
- Java 21 and Maven
- A populated `.env` based on [.env.example](.env.example)

The local Docker stack includes PostgreSQL, Keycloak, Valkey, MinIO, Traefik, the Stripe sandbox listener, and the observability services. Configure the SMTP, Stripe, S3/R2, Sentry, PostHog, and alerting values required by your environment before starting it.

```bash
cp .env.example .env
./manage.sh docker
./manage.sh dev
```

| Service | Local address |
| --- | --- |
| Web application | http://localhost:3000 |
| Backend API | http://localhost:8081 |
| Scalar API reference | http://localhost:8081/scalar |
| Keycloak administration | http://localhost/auth/admin/ |
| Traefik dashboard | http://localhost:8088 |
| Grafana | http://localhost:3001 |
| MinIO console | http://localhost:9001 |
| PostgreSQL | `localhost:5433` |

### Mobile development

Connect an Android device or start an emulator, then use the local backend and Keycloak endpoints configured by the management script.

```bash
./manage.sh mobile:android
```

Use `./manage.sh mobile:build:local` when native dependencies or Expo configuration change. Use `./manage.sh mobile:build` to create an EAS Android development build.

## Common commands

| Command | Purpose |
| --- | --- |
| `./manage.sh docker` | Start local infrastructure, observability, and Stripe sandbox listener. |
| `./manage.sh docker:down` | Stop local Docker services without deleting volumes. |
| `./manage.sh dev` | Start the Spring Boot API and web application. |
| `./manage.sh check` | Run web checks/type checks, mobile type checks, and backend quality plugins. |
| `./manage.sh test` | Run backend unit/integration tests and web unit tests. |
| `./manage.sh test:e2e` | Run backend E2E/integration tests and Playwright journeys. |
| `./manage.sh build` | Build the backend JAR and web application. |
| `./manage.sh mobile:verify` | Run mobile type and project verification checks. |
| `./manage.sh docs` | Generate Spring Modulith diagrams and module documentation. |

## Production deployment

Production configuration lives in `deployment/` and uses [deployment/.env.prod.example](deployment/.env.prod.example) as its template. It runs ARM64 images from GHCR with PostgreSQL, Valkey, Keycloak, the application gateway, Grafana, Prometheus, Alertmanager, Alloy, Loki, Tempo, and WAL-G PostgreSQL backup services.

The gateway is attached to the Nginx Proxy Manager Docker network, which terminates TLS for the configured `APP_DOMAIN`. The backend and observability services are not publicly exposed by the Compose stack.

```bash
./manage.sh prod:up
./manage.sh prod:status
./manage.sh prod:down
```

GitHub Actions runs quality checks, unit/integration tests, browser E2E tests, ARM64 image builds, and production deployment from `main`. The deployment job synchronizes configuration, selects the immutable commit-SHA image tag, and verifies the public application URL.

## Observability and backups

- Prometheus scrapes backend metrics; Grafana provides the operational dashboard.
- Alloy receives OTLP telemetry and forwards traces to Tempo and logs to Loki.
- Alertmanager delivers operational alerts; Sentry captures application errors and traces; PostHog provides product analytics.
- Loki and Tempo use S3-compatible object storage in the configured environment.
- PostgreSQL archives WAL and performs scheduled WAL-G base backups to S3-compatible storage.

## Verification

The repository currently verifies backend unit/integration coverage, web unit coverage, and Playwright role workflows covering appointment booking, work-order progression, quotation approval, payment handoff, document visibility, and authorization boundaries.

```bash
./manage.sh check
./manage.sh test
./manage.sh test:e2e
```
