#!/usr/bin/env pwsh
# Tisqra Platform - Service Status Checker

Write-Host "🔍 TISQRA PLATFORM SERVICE STATUS" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

# Check Docker containers
Write-Host "📦 Docker Containers:" -ForegroundColor Yellow
docker-compose ps

Write-Host ""
Write-Host "🌐 Service Health Checks:" -ForegroundColor Yellow
Write-Host ""

$services = @(
    @{Name="Discovery Service (Eureka)"; URL="http://localhost:8761"; Port=8761},
    @{Name="Config Server"; URL="http://localhost:8888/actuator/health"; Port=8888},
    @{Name="API Gateway"; URL="http://localhost:8080/actuator/health"; Port=8080},
    @{Name="User Service"; URL="http://localhost:8081/actuator/health"; Port=8081},
    @{Name="Event Service"; URL="http://localhost:8082/actuator/health"; Port=8082},
    @{Name="Order Service"; URL="http://localhost:8083/actuator/health"; Port=8083},
    @{Name="Payment Service"; URL="http://localhost:8084/actuator/health"; Port=8084},
    @{Name="Ticket Service"; URL="http://localhost:8085/actuator/health"; Port=8085}
)

foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri $service.URL -TimeoutSec 2 -ErrorAction Stop
        Write-Host "  ✅ $($service.Name) - Running on port $($service.Port)" -ForegroundColor Green
    } catch {
        Write-Host "  ⏳ $($service.Name) - Not ready yet (port $($service.Port))" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "💡 TIP: Run this script again in a few minutes to check progress" -ForegroundColor Cyan
