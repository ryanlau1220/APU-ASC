#!/bin/sh
set -eu

psql --username "$POSTGRES_USER" --dbname postgres \
  --set=keycloak_db="$KEYCLOAK_DB" \
  --set=keycloak_db_user="$KEYCLOAK_DB_USER" \
  --set=keycloak_db_password="$KEYCLOAK_DB_PASSWORD" <<'SQL'
SELECT format('CREATE ROLE %I LOGIN PASSWORD %L', :'keycloak_db_user', :'keycloak_db_password')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = :'keycloak_db_user')
\gexec

SELECT format('CREATE DATABASE %I OWNER %I', :'keycloak_db', :'keycloak_db_user')
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = :'keycloak_db')
\gexec
SQL
