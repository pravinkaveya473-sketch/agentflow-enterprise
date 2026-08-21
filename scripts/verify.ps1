$ErrorActionPreference = "Stop"

Write-Host "[1/4] Compile and test"
.\mvnw.cmd clean test

Write-Host "[2/4] Package"
.\mvnw.cmd -DskipTests package

Write-Host "[3/4] Start infrastructure"
docker compose up -d mysql redis

Write-Host "[4/4] Show containers"
docker compose ps

Write-Host "Verification completed. Start the app in IDEA or run: .\mvnw.cmd spring-boot:run"
