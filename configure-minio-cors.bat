@echo off
echo Configuring MinIO CORS for bucket 'scholchat'...

REM Set MinIO alias (adjust credentials if needed)
mc alias set local http://localhost:9001 minioadmin minioadmin

REM Create CORS configuration JSON
echo [{"AllowedOrigins":["http://localhost:3000"],"AllowedMethods":["GET","PUT","POST","DELETE","HEAD"],"AllowedHeaders":["*"],"ExposeHeaders":["ETag","Content-Length","Content-Type"]}] > cors.json

REM Apply CORS configuration to bucket
mc anonymous set-json cors.json download local/scholchat

REM Clean up
del cors.json

echo CORS configuration applied successfully!
pause
