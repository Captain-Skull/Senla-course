#!/bin/bash
set -e

DB_HOST="127.0.0.1"
DB_PORT="5432"
DB_NAME="hotel_db"
APP_USER="hotel_app"
APP_PASSWORD="hotel_password"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SQL_DIR="$SCRIPT_DIR/../sql"

read -p "Администратор PostgreSQL: " DB_ADMIN
read -sp "Пароль администратора: " DB_ADMIN_PASSWORD
echo

export PGPASSWORD="$DB_ADMIN_PASSWORD"

echo "[1/4] Создание пользователя приложения..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN" -d postgres -v ON_ERROR_STOP=1 <<SQL
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '$APP_USER') THEN
        CREATE ROLE $APP_USER WITH LOGIN PASSWORD '$APP_PASSWORD';
    ELSE
        ALTER ROLE $APP_USER WITH PASSWORD '$APP_PASSWORD';
    END IF;
END
\$\$;
SQL

echo "[2/4] Создание базы данных..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN" -d postgres -v ON_ERROR_STOP=1 \
  -c "DROP DATABASE IF EXISTS $DB_NAME;"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN" -d postgres -v ON_ERROR_STOP=1 \
  -c "CREATE DATABASE $DB_NAME OWNER $APP_USER;"

echo "[3/4] Создание структуры..."
export PGPASSWORD="$APP_PASSWORD"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$APP_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 \
  -f "$SQL_DIR/schema.sql"

echo "[4/4] Загрузка тестовых данных..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$APP_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 \
  -f "$SQL_DIR/data.sql"

unset PGPASSWORD

echo ""
echo "========================================="
echo "База данных создана!"
echo "========================================="
echo "Подключение для приложения:"
echo "  Host:     $DB_HOST"
echo "  Port:     $DB_PORT"
echo "  Database: $DB_NAME"
echo "  User:     $APP_USER"
echo "  Password: $APP_PASSWORD"
echo "========================================="