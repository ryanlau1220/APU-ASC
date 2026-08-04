#!/bin/sh
set -eu

escape() {
  printf '%s' "$1" | sed 's/[\\&|]/\\\\&/g'
}

rendered=/tmp/alertmanager.yml
sed \
  -e "s|__ALERT_EMAIL_TO__|$(escape "${ALERT_EMAIL_TO:?Set ALERT_EMAIL_TO in .env}")|g" \
  -e "s|__SPRING_MAIL_HOST__|$(escape "${SPRING_MAIL_HOST:?Set SPRING_MAIL_HOST in .env}")|g" \
  -e "s|__SPRING_MAIL_PORT__|$(escape "${SPRING_MAIL_PORT:?Set SPRING_MAIL_PORT in .env}")|g" \
  -e "s|__SPRING_MAIL_USERNAME__|$(escape "${SPRING_MAIL_USERNAME:?Set SPRING_MAIL_USERNAME in .env}")|g" \
  -e "s|__SPRING_MAIL_PASSWORD__|$(escape "${SPRING_MAIL_PASSWORD:?Set SPRING_MAIL_PASSWORD in .env}")|g" \
  /etc/alertmanager/alertmanager.yml.tmpl > "$rendered"

exec /bin/alertmanager --config.file="$rendered"
