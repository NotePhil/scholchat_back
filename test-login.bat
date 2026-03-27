@echo off
echo Testing login endpoint...

curl -X POST http://localhost:8486/scholchat/auth/login ^
  -H "Content-Type: application/json" ^
  -H "Accept: application/json" ^
  -d "{\"email\":\"test@example.com\",\"password\":\"TestPassword123!\"}" ^
  -v

pause