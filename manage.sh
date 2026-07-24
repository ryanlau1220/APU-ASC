#!/bin/bash

# APU Automotive Service Centre (APU-ASC) Management Script

set -e

# ANSI Color Tokens
CYAN='\033[36m'
MAGENTA='\033[35m'
YELLOW='\033[33m'
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
        # Fast health check for PostgreSQL port 5432
        if ! (nc -z localhost 5432 2>/dev/null || (echo > /dev/tcp/localhost/5432) 2>/dev/null); then
            echo -e "${RED}⚠️  [WARN] PostgreSQL database is not reachable on port 5432.${RESET}"
            echo -e "${YELLOW}Please start the Docker infrastructure in another terminal using:${RESET} ${CYAN}./manage.sh docker${RESET}"
            exit 1
        fi

        # Automatically free ports 8080 and 3000 from lingering background processes
        fuser -k 8080/tcp 3000/tcp 2>/dev/null || true

        trap cleanup_dev INT TERM

        echo -e "${CYAN}Starting APU-ASC Backend (Spring Boot) & Web (TanStack Start) concurrently...${RESET}"
        (cd apps/backend && mvn spring-boot:run 2>&1 | stdbuf -oL sed "s/^/$(printf "${CYAN}[backend]${RESET}") /") &
        (pnpm --filter @apu-asc/web dev 2>&1 | stdbuf -oL sed "s/^/$(printf "${MAGENTA}[web]${RESET}") /") &
        wait
        ;;
    docker)
        echo -e "${YELLOW}Starting Docker Compose infrastructure stack (PostgreSQL, Keycloak, MinIO, Traefik)...${RESET}"
        echo -e "${YELLOW}Press Ctrl+C to stop all containers gracefully.${RESET}"
        docker compose up || true
        echo -e "${YELLOW}Shutting down Docker Compose containers...${RESET}"
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
