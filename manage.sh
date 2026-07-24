#!/bin/bash

# APU Automotive Service Centre (APU-ASC) Management Script

set -e

# Cleanup handler for Ctrl+C signal
cleanup() {
    echo ""
    echo "Stopping background dev services..."
    kill $(jobs -p) 2>/dev/null || true
    echo "Done."
    exit 0
}

trap cleanup INT TERM

case "$1" in
    dev)
        echo "Starting Docker Infrastructure (PostgreSQL, Keycloak, MinIO, Traefik)..."
        docker compose up -d postgres keycloak minio traefik
        echo "Starting APU-ASC Backend (Spring Boot) & Web (TanStack Start) concurrently..."
        (cd apps/backend && mvn spring-boot:run) &
        (pnpm --filter @apu-asc/web dev) &
        wait
        ;;
    docker)
        echo "Starting full Docker Compose stack (Ctrl+C to shut down)..."
        docker compose up || true
        echo "Shutting down Docker Compose containers..."
        docker compose down
        ;;
    build)
        echo "Building backend Fat JAR..."
        (cd apps/backend && mvn clean package -DskipTests)
        echo "Building web frontend..."
        pnpm --filter @apu-asc/web build
        echo "Full-stack build complete."
        ;;
    lint)
        echo "Running Spotless code formatting..."
        (cd apps/backend && mvn spotless:apply)
        echo "Running Biome formatting..."
        pnpm --filter @apu-asc/web format
        ;;
    check)
        echo "Running Frontend Quality Checks (Biome check & TypeScript typecheck)..."
        pnpm --filter @apu-asc/web check
        pnpm --filter @apu-asc/web typecheck
        echo "Running Backend Maven Quality Plugins (Spotless, Checkstyle, SpotBugs, PMD)..."
        (cd apps/backend && mvn spotless:check checkstyle:check spotbugs:check pmd:check)
        echo "Full-stack check complete."
        ;;
    test)
        echo "Running backend JUnit 5 unit & integration tests..."
        (cd apps/backend && mvn test)
        echo "Running web unit tests..."
        pnpm --filter @apu-asc/web test
        echo "Full-stack testing complete."
        ;;
    clean)
        echo "Cleaning Maven target directories and web build assets..."
        (cd apps/backend && mvn clean)
        rm -rf apps/web/.output apps/web/dist apps/web/.vinxi apps/web/.tanstack .turbo
        echo "Clean complete."
        ;;
    *)
        echo "Usage: ./manage.sh {dev|docker|build|lint|check|test|clean}"
        exit 1
        ;;
esac
