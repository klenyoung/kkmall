#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/opt/kkmall/backend"
JAR_NAME="kkmall-backend-0.1.0.jar"

if [ ! -f "$JAR_NAME" ]; then
  echo "Missing $JAR_NAME. Copy it into the current directory first."
  exit 1
fi

sudo mkdir -p "$APP_DIR"
sudo cp "$JAR_NAME" "$APP_DIR/$JAR_NAME"

if [ ! -f "$APP_DIR/.env" ]; then
  sudo tee "$APP_DIR/.env" >/dev/null <<'ENV'
SERVER_PORT=8080
MYSQL_URL=jdbc:mysql://127.0.0.1:3306/kkmall?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
MYSQL_USER=root
MYSQL_PASSWORD=replace-me
KKMALL_AUTH_SECRET=replace-with-a-long-random-secret
KKMALL_MOCK_CODE=123456
ENV
  echo "Created $APP_DIR/.env. Edit it with real values before starting the service."
fi

sudo cp kkmall-backend.service /etc/systemd/system/kkmall-backend.service
sudo systemctl daemon-reload
sudo systemctl enable kkmall-backend
sudo systemctl restart kkmall-backend
sudo systemctl status kkmall-backend --no-pager
