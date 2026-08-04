#!/bin/bash

# APU Automotive Service Centre (APU-ASC) Management Script

set -e

# ANSI Color Tokens
CYAN='\033[36m'
MAGENTA='\033[35m'
YELLOW='\033[33m'
GREEN='\033[32m'
RED='\033[31m'
RESET='\033[0m'

# Cleanup handler for Ctrl+C signal in dev mode
cleanup_dev() {
    echo ""
    echo -e "${YELLOW}Stopping backend and frontend dev processes...${RESET}"
    kill $(jobs -p) 2>/dev/null || true
    echo -e "${YELLOW}Done.${RESET}"
    exit 0
}

case "$1" in
    dev)
        if [ -f .env ]; then
            set -a
            source .env
            set +a
        fi

        # Fast health check for PostgreSQL port 5433
        if ! (nc -z localhost 5433 2>/dev/null || (echo > /dev/tcp/localhost/5433) 2>/dev/null); then
            echo -e "${RED}⚠️  [WARN] PostgreSQL database is not reachable on port 5433.${RESET}"
            echo -e "${YELLOW}Please start the Docker infrastructure in another terminal using:${RESET} ${CYAN}./manage.sh docker${RESET}"
            exit 1
        fi

        echo -e "${YELLOW}Waiting for Keycloak OIDC issuer to become ready...${RESET}"
        until [ "$(docker inspect --format='{{.State.Health.Status}}' apu-asc-keycloak 2>/dev/null)" = "healthy" ]; do
            sleep 2
        done
        echo -e "${GREEN}✓ [OK] Keycloak OIDC Service is READY!${RESET}"

        prometheus_cidr="$(docker network inspect --format '{{range .IPAM.Config}}{{.Subnet}}{{end}}' apu-asc-net 2>/dev/null || true)"
        if [ -n "${prometheus_cidr}" ]; then
            export OBSERVABILITY_PROMETHEUS_ALLOWED_CIDR="${prometheus_cidr}"
            echo -e "${GREEN}✓ [OK] Prometheus scrape CIDR configured for ${prometheus_cidr}.${RESET}"
        else
            echo -e "${YELLOW}⚠️  [WARN] Could not determine the Docker bridge CIDR; using OBSERVABILITY_PROMETHEUS_ALLOWED_CIDR from .env.${RESET}"
        fi

        # Automatically free ports 8081 and 3000 from lingering background processes
        fuser -k 8081/tcp 3000/tcp 2>/dev/null || true

        trap cleanup_dev INT TERM

        echo -e "${CYAN}Launching APU-ASC Backend (Spring Boot)...${RESET}"
        (cd apps/backend && mvn spring-boot:run 2>&1 | stdbuf -oL sed "s/^/$(printf "${CYAN}[backend]${RESET}") /") &

        echo -e "${YELLOW}Waiting for Spring Boot backend to become healthy on port 8081...${RESET}"
        until curl -s -f http://localhost:8081/actuator/health >/dev/null 2>&1; do
            sleep 2
        done

        echo -e "${GREEN}✓ [OK] Spring Boot Backend is 100% HEALTHY & READY on port 8081!${RESET}"
        echo -e "${MAGENTA}Launching APU-ASC Web (TanStack Start)...${RESET}"
        (pnpm --filter @apu-asc/web dev 2>&1 | stdbuf -oL sed "s/^/$(printf "${MAGENTA}[web]${RESET}") /") &
        wait
        ;;
    docker)
        echo -e "${YELLOW}Starting Docker Compose infrastructure and observability stack...${RESET}"
        docker compose --profile observability up -d
        echo -e "${GREEN}✓ [OK] Docker infrastructure and observability containers started successfully in detached mode.${RESET}"
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
    test:e2e)
        if ! (nc -z localhost 8081 2>/dev/null || (echo > /dev/tcp/localhost/8081) 2>/dev/null); then
            echo -e "${YELLOW}ℹ️  [NOTE] Backend dev server (port 8081) is not running.${RESET}"
            echo -e "${YELLOW}For sub-second live API E2E tests, run ${CYAN}./manage.sh dev${YELLOW} in a separate terminal.${RESET}"
        fi
        echo "Running backend E2E integration tests..."
        (cd apps/backend && mvn test -Dtest=*E2eTest,*IntegrationTest)
        echo "Running frontend Playwright browser E2E tests..."
        pnpm --filter @apu-asc/web test:e2e
        echo "Full-stack E2E testing complete."
        ;;
    clean)
        echo "Cleaning Maven target directories and web build assets..."
        (cd apps/backend && mvn clean)
        rm -rf apps/web/.output apps/web/dist apps/web/.vinxi apps/web/.tanstack .turbo
        echo "Clean complete."
        ;;
    *)
        echo "Usage: ./manage.sh {dev|docker|build|lint|check|test|test:e2e|clean}"
        exit 1
        ;;
esac
