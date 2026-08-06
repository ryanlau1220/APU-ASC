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

set_env_value() {
    local env_file="$1"
    local key="$2"
    local value="$3"

    if grep -q "^${key}=" "$env_file"; then
        sed -i "s|^${key}=.*|${key}=${value}|" "$env_file"
    else
        printf '\n%s=%s\n' "$key" "$value" >> "$env_file"
    fi
}

read_env_value() {
    local env_file="$1"
    local key="$2"
    sed -n "s/^${key}=//p" "$env_file" | tail -n 1
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
            export OBSERVABILITY_PROMETHEUS_ALLOWED_CIDR="127.0.0.1/32,${prometheus_cidr}"
            echo -e "${GREEN}✓ [OK] Prometheus scrape allow-list configured for loopback and ${prometheus_cidr}.${RESET}"
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
    mobile)
        echo -e "${MAGENTA}Launching APU-ASC Mobile (Expo)...${RESET}"
        EXPO_PUBLIC_API_BASE_URL=http://localhost:8081 \
        EXPO_PUBLIC_KEYCLOAK_ISSUER=http://localhost:8080/auth/realms/apu-asc \
        pnpm --filter @apu-asc/mobile start
        ;;
    mobile:android)
        echo -e "${MAGENTA}Launching APU-ASC Mobile on Android...${RESET}"
        EXPO_PUBLIC_API_BASE_URL=http://localhost:8081 \
        EXPO_PUBLIC_KEYCLOAK_ISSUER=http://localhost:8080/auth/realms/apu-asc \
        pnpm --filter @apu-asc/mobile android
        ;;
    mobile:verify)
        pnpm --filter @apu-asc/mobile typecheck
        pnpm --filter @apu-asc/mobile verify
        ;;
    docker)
        echo -e "${YELLOW}Starting Docker Compose infrastructure and observability stack...${RESET}"
        docker compose --profile observability up -d
        echo -e "${GREEN}✓ [OK] Docker infrastructure and observability containers started successfully in detached mode.${RESET}"
        ;;
    docker:down)
        echo -e "${YELLOW}Stopping Docker Compose infrastructure and observability stack...${RESET}"
        docker compose --profile observability down
        echo -e "${GREEN}✓ [OK] Docker infrastructure and observability containers stopped and removed.${RESET}"
        ;;
    prod:up)
        prod_env="${2:-deployment/.env.prod}"
        if [ ! -f "$prod_env" ]; then
            echo -e "${RED}Production environment file not found: $prod_env${RESET}"
            exit 1
        fi
        echo -e "${YELLOW}Starting the production stack...${RESET}"
        docker compose --env-file "$prod_env" -f deployment/docker-compose.prod.yml pull
        docker compose --env-file "$prod_env" -f deployment/docker-compose.prod.yml up -d --no-build --remove-orphans --wait --wait-timeout 180
        echo -e "${GREEN}✓ [OK] Production stack started. Check status with: ./manage.sh prod:status${RESET}"
        ;;
    prod:deploy)
        prod_env="${2:-deployment/.env.prod}"
        image_tag="${3:-}"
        image_registry="${4:-}"
        if [ ! -f "$prod_env" ]; then
            echo -e "${RED}Production environment file not found: $prod_env${RESET}"
            exit 1
        fi
        if [[ ! "$image_tag" =~ ^[0-9a-f]{40}$ ]]; then
            echo -e "${RED}IMAGE_TAG must be a 40-character lowercase Git commit SHA.${RESET}"
            exit 1
        fi
        if [[ ! "$image_registry" =~ ^ghcr\.io/[a-z0-9][a-z0-9._-]*$ ]]; then
            echo -e "${RED}IMAGE_REGISTRY must be a lowercase GHCR namespace, such as ghcr.io/your-account.${RESET}"
            exit 1
        fi

        set_env_value "$prod_env" "IMAGE_TAG" "$image_tag"
        set_env_value "$prod_env" "IMAGE_REGISTRY" "$image_registry"
        set_env_value "$prod_env" "OBSERVABILITY_RELEASE" "$image_tag"

        ghcr_username="$(read_env_value "$prod_env" "GHCR_USERNAME")"
        ghcr_pull_token="$(read_env_value "$prod_env" "GHCR_PULL_TOKEN")"
        if [ -n "$ghcr_username" ] || [ -n "$ghcr_pull_token" ]; then
            if [ -z "$ghcr_username" ] || [ -z "$ghcr_pull_token" ]; then
                echo -e "${RED}Set both GHCR_USERNAME and GHCR_PULL_TOKEN, or leave both empty for public images.${RESET}"
                exit 1
            fi
            printf '%s' "$ghcr_pull_token" | docker login ghcr.io --username "$ghcr_username" --password-stdin
        fi

        "$0" prod:up "$prod_env"

        app_domain="$(read_env_value "$prod_env" "APP_DOMAIN")"
        if [[ ! "$app_domain" =~ ^[A-Za-z0-9.-]+$ ]]; then
            echo -e "${RED}APP_DOMAIN must be a valid hostname before deployment verification can run.${RESET}"
            exit 1
        fi
        curl --fail --silent --show-error --retry 12 --retry-all-errors --retry-delay 5 "https://${app_domain}/" >/dev/null
        echo -e "${GREEN}✓ [OK] Production release ${image_tag} is healthy at https://${app_domain}/.${RESET}"
        ;;
    prod:down)
        prod_env="${2:-deployment/.env.prod}"
        echo -e "${YELLOW}Stopping the production stack without deleting volumes...${RESET}"
        docker compose --env-file "$prod_env" -f deployment/docker-compose.prod.yml down
        ;;
    prod:status)
        prod_env="${2:-deployment/.env.prod}"
        docker compose --env-file "$prod_env" -f deployment/docker-compose.prod.yml ps
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
        echo "Running Mobile TypeScript checks..."
        pnpm --filter @apu-asc/mobile typecheck
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
        echo "Usage: ./manage.sh {dev|mobile|mobile:android|mobile:verify|docker|docker:down|prod:up|prod:deploy|prod:down|prod:status|build|lint|check|test|test:e2e|clean} [production-env-file]"
        exit 1
        ;;
esac
