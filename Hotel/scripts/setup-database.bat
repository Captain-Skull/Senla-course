@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

set DB_HOST=127.0.0.1
set DB_PORT=5432
set DB_NAME=hotel_db
set APP_USER=hotel_app
set APP_PASSWORD=hotel_password

set /p DB_ADMIN=Администратор PostgreSQL:

for /f "delims=" %%p in ('powershell -Command "$p=Read-Host -Prompt 'Пароль администратора' -AsSecureString; [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($p))"') do set DB_ADMIN_PASSWORD=%%p

echo.
set PGPASSWORD=%DB_ADMIN_PASSWORD%

echo [1/2] Создание пользователя приложения...
(
echo DO $$
echo BEGIN
echo     IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '%APP_USER%') THEN
echo         CREATE ROLE %APP_USER% WITH LOGIN PASSWORD '%APP_PASSWORD%';
echo     ELSE
echo         ALTER ROLE %APP_USER% WITH PASSWORD '%APP_PASSWORD%';
echo     END IF;
echo END
echo $$;
) | psql -h %DB_HOST% -p %DB_PORT% -U %DB_ADMIN% -d postgres -v ON_ERROR_STOP=1
if %ERRORLEVEL% NEQ 0 goto :error

echo [2/2] Создание базы данных...
psql -h %DB_HOST% -p %DB_PORT% -U %DB_ADMIN% -d postgres -v ON_ERROR_STOP=1 -c "DROP DATABASE IF EXISTS %DB_NAME%;"
if %ERRORLEVEL% NEQ 0 goto :error

psql -h %DB_HOST% -p %DB_PORT% -U %DB_ADMIN% -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE %DB_NAME% OWNER %APP_USER%;"
if %ERRORLEVEL% NEQ 0 goto :error

set PGPASSWORD=
echo.
echo =========================================
echo Пустая база данных создана!
echo =========================================
echo Таблицы и данные будут созданы
echo автоматически при запуске приложения
echo через Liquibase.
echo =========================================
echo Подключение для приложения:
echo   Host:     %DB_HOST%
echo   Port:     %DB_PORT%
echo   Database: %DB_NAME%
echo   User:     %APP_USER%
echo   Password: %APP_PASSWORD%
echo =========================================
pause
exit /b 0

:error
set PGPASSWORD=
echo.
echo ОШИБКА: Скрипт завершился неудачно.
pause
exit /b 1