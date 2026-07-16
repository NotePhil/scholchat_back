@echo off
echo ============================================
echo   ScholChat - Database Setup Script
echo ============================================
echo.

set PSQL="C:\Program Files\PostgreSQL\18\bin\psql.exe"
set SQLDIR=c:\Users\Prince\Documents\PROJECT SCHOLCHAT\2025backendSchoolchat\db-init\src\main\resources\sql

echo Step 1: Drop existing database...
%PSQL% -U postgres -c "DROP DATABASE IF EXISTS scholchat;"
echo.

echo Step 2: Create database...
%PSQL% -U postgres -c "CREATE DATABASE scholchat OWNER scholchat_user;"
echo.

echo Step 3: Create schema and tables...
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\schema-postgres.sql"
echo.

echo Step 4: Grant permissions...
%PSQL% -U postgres -d scholchat -c "GRANT ALL ON SCHEMA ressources TO scholchat_user; GRANT ALL ON ALL TABLES IN SCHEMA ressources TO scholchat_user; GRANT ALL ON ALL SEQUENCES IN SCHEMA ressources TO scholchat_user; GRANT USAGE ON SCHEMA ressources TO scholchat_user; ALTER DEFAULT PRIVILEGES IN SCHEMA ressources GRANT ALL ON TABLES TO scholchat_user; ALTER DEFAULT PRIVILEGES IN SCHEMA ressources GRANT ALL ON SEQUENCES TO scholchat_user;"
echo.

echo Step 5: Insert data...
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\data-postgres.sql"
echo.

echo Step 6: Apply migrations...
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\notifications-schema.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-add-visibility.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-add-notifications.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-add-gestionnaires.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-questions.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-multi-roles.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-add-creator-id.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-live-sessions.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-session-attendance.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-type-assignation-etat-soumission.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-fix-moderator-droits.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-add-media-cours-id.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-add-offres-contrats.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-add-expire-par-offre.sql"
%PSQL% -U postgres -d scholchat -f "%SQLDIR%\migration-add-purge-et-restrictions-offres.sql"
echo.

echo ============================================
echo   Database setup complete!
echo ============================================
pause
