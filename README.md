This is a project refactored from OODJ Assignment (https://github.com/jasonwong1025/oodj-apuasc).

Tech Stack:
- Package Manager: pnpm + maven
- Web: TanStack Start
- Backend: Spring Boot + Spring Modulith
- Styles: Tailwind CSS + shadcn-ui
- State Management: TanStack Query
- Database: PostgreSQL
- Validation: Hibernate Validation + RFC 9457 Problem Details + Zod
- ORM: JPA Entities + Hibernate
- Migrations: Flyway
- Cache Layer: Valkey + Spring Cache
- Logging: Logback + SLF4J
- Identity Management: Keycloak
- Email Delivery: Jakarta Mail + Thymeleaf
- Object Storage: MinIO + Cloudflare R2 + AWS S3 SDK
- Observability Layer: Micrometer + Prometheus + OpenTelemetry/OTLP + Alloy + Tempo + Loki + Grafana + Alertmanager + Sentry + PostHog
- Reverse Proxy: Traefik
- Testing: JUnit 5 + Vitest + Playwright
- OpenAPI Docs: SpringDoc + Scalar
- Rate Limiting: bucket4j
- Security: Spring Security + OAuth2 / OIDC
- Infrastructure: Docker Compose
- Tools: Turborepo, Biome, Spotless, Checkstyle, SpotBugs, PMD, OpenApi-Typescript, Orval

To run project
1. ./manage.sh docker
2. ./manage.sh dev

Useful Commands
1. ./manage.sh docker
- run all docker services with docker compose

2. ./manage.sh check
- biome format and check at web and maven plugins on backend quality

3. ./manage.sh build
- frontend and backend build

4. ./manage.sh test && ./manage.sh test:e2e
- frontend and backend unit, integration, e2e tests

5. ./manage.sh dev
- run frontend and backend at local

Port Management
- 8081: Spring Boot Backend
- 3000: TanStack Start Web
- 8088: Traefik Dashboard
- 5433: PostgreSQL
- 8080: Keycloak Console
- 9001: MinIO Console
- 6379: Valkey

Project URL
- Web: http://localhost:3000
- Backend: http://localhost:8081
- Keycloak: http://localhost/auth/admin/ 
- Scalar: http://localhost:8081/scalar
- MinIO: http://localhost:9001
- Traefik: http://localhost:8088
- Grafana: http://localhost:3001

Credentials
username: admin
password: Admin123!