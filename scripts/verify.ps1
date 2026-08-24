$ErrorActionPreference = "Stop"

Write-Host "[1/5] Compile and test all modules"
.\mvnw.cmd clean verify

Write-Host "[2/5] Package all services"
.\mvnw.cmd -DskipTests package

Write-Host "[3/5] Validate Compose configuration"
docker compose config --quiet

Write-Host "[4/5] Start the complete stack"
docker compose up -d --build

Write-Host "[5/5] Run business and Kafka smoke test"
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
docker compose ps

Write-Host "Verification completed. Gateway: http://localhost:8080"
