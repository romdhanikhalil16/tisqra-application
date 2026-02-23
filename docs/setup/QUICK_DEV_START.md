# Quick Development Start - Tisqra Platform

## 🚀 Get Started in 3 Steps

### Step 1: Start Docker Desktop

**Windows:**
1. Search for "Docker Desktop" in Start Menu
2. Click to open
3. Wait for the whale icon to appear in system tray (green = ready)
4. This usually takes 30-60 seconds

**Or start from PowerShell:**
```powershell
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
# Wait 60 seconds for it to start
Start-Sleep -Seconds 60
```

### Step 2: Choose Your Development Mode

We have 3 development modes:

#### Option A: Full Stack (Quickest - Everything in Docker)
**Best for**: Testing, demos, quick start

```powershell
cd Desktop\tisqra-platform
.\dev-start.ps1 -FullStack
```

**What it does:**
- Starts ALL services in Docker
- No need to run anything from IDE
- Just wait and test APIs

**Access:**
- API Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761

---

#### Option B: Local Development (Recommended for Development)
**Best for**: Active backend development, debugging

```powershell
# 1. Start infrastructure only
cd Desktop\tisqra-platform
.\dev-start.ps1 -Infrastructure

# 2. Build backend
.\dev-start.ps1 -Backend

# 3. Open in IDE and run services individually
```

**Then in your IDE (IntelliJ/VS Code):**
1. Open `Desktop/tisqra-platform`
2. Run services in order:
   - `config-server` (port 8888)
   - `discovery-service` (port 8761)
   - `api-gateway` (port 8080)
   - `user-service` (port 8081)
   - Other services as needed

---

#### Option C: Hybrid (Best of Both Worlds)
**Best for**: Developing one service, others in Docker

```powershell
# 1. Start everything
.\dev-start.ps1 -FullStack

# 2. Stop the service you want to develop
docker-compose stop user-service

# 3. Run that service from IDE
# Open user-service in IDE and run it
```

---

### Step 3: Test the APIs

#### Using PowerShell Script:
```powershell
# Test all endpoints
.\dev-test.ps1 -All

# Or test individually
.\dev-test.ps1 -Health
.\dev-test.ps1 -Register
.\dev-test.ps1 -Login
```

#### Using Postman:
1. Open Postman
2. Import: `Desktop/tisqra-platform/postman/Tisqra-Platform.postman_collection.json`
3. Set environment variable: `base_url = http://localhost:8080`
4. Run tests from the collection

#### Using curl:
```bash
# Health check
curl http://localhost:8080/actuator/health

# Register user
curl -X POST http://localhost:8080/api/user/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@tisqra.com",
    "password": "Test123!",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+1234567890"
  }'

# Login
curl -X POST http://localhost:8080/api/user/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@tisqra.com",
    "password": "Test123!"
  }'
```

---

## 📱 Mobile App Development

### Setup Flutter App:
```powershell
.\dev-start.ps1 -Mobile
```

### Run Mobile App:
```bash
cd Desktop/tisqra-platform/mobile/tisqra_mobile_app
flutter run
```

**Note**: Backend must be running at http://localhost:8080

---

## 🛠️ Common Commands

### Docker Commands
```powershell
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f [service-name]

# Restart a service
docker-compose restart [service-name]

# Check running containers
docker-compose ps

# Remove everything (including volumes)
docker-compose down -v
```

### Maven Commands
```powershell
# Build all services
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run a specific service
cd services/user-service
mvn spring-boot:run

# Run tests
mvn test
```

### Flutter Commands
```bash
cd mobile/tisqra_mobile_app

# Get dependencies
flutter pub get

# Run app
flutter run

# Run tests
flutter test

# Build APK
flutter build apk

# Clean
flutter clean
```

---

## 🔍 Troubleshooting

### Docker Desktop won't start
1. Restart Windows
2. Make sure Hyper-V is enabled (Windows Features)
3. Update Docker Desktop to latest version
4. Check Docker Desktop logs in system tray

### Ports already in use
```powershell
# Find what's using port 8080
netstat -ano | findstr :8080

# Kill the process
taskkill /PID <PID> /F
```

### Maven build fails
```powershell
# Clear Maven cache
mvn clean

# Update dependencies
mvn clean install -U

# Skip tests
mvn clean install -DskipTests
```

### Services can't connect to database
```powershell
# Check if PostgreSQL is running
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### Can't access APIs
```powershell
# Check if API Gateway is running
curl http://localhost:8080/actuator/health

# Check Eureka Dashboard
# Open: http://localhost:8761

# Check if services are registered
# They should appear in Eureka Dashboard
```

---

## 📊 Service Ports Reference

| Service | Port | Description |
|---------|------|-------------|
| config-server | 8888 | Configuration Server |
| discovery-service | 8761 | Eureka Service Discovery |
| api-gateway | 8080 | Main API Gateway |
| user-service | 8081 | User & Authentication |
| event-service | 8082 | Event Management |
| order-service | 8083 | Order Processing |
| payment-service | 8084 | Payments |
| ticket-service | 8085 | Ticket Generation |
| organization-service | 8086 | Organizations |
| analytics-service | 8087 | Analytics |
| notification-service | 8088 | Notifications |
| PostgreSQL | 5432 | Database |
| Redis | 6379 | Cache |
| Kafka | 9092 | Message Broker |
| Zookeeper | 2181 | Kafka Coordination |

---

## 🎯 Development Workflow

### Daily Workflow:
1. **Start Docker Desktop** (if not auto-starting)
2. **Start infrastructure**: `.\dev-start.ps1 -Infrastructure`
3. **Open IDE** and run services you're working on
4. **Make changes** to code
5. **Hot reload** (if IDE supports) or restart service
6. **Test** with Postman or `.\dev-test.ps1`
7. **Commit** changes

### Before Committing:
```powershell
# Run tests
mvn test

# Check code style (if configured)
mvn checkstyle:check

# Build everything
mvn clean install
```

---

## 📚 Next Steps

- **[DEV_SETUP_GUIDE.md](DEV_SETUP_GUIDE.md)** - Detailed setup instructions
- **[DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)** - Development best practices
- **[START_HERE.md](START_HERE.md)** - Complete platform overview
- **[postman/README.md](postman/README.md)** - API testing guide

---

## 🆘 Need Help?

1. Check the troubleshooting section above
2. Review service logs: `docker-compose logs -f [service]`
3. Check Eureka Dashboard: http://localhost:8761
4. Verify prerequisites: Java 17+, Maven 3.8+, Docker

---

## ✅ Checklist for First Time Setup

- [ ] Docker Desktop installed and running
- [ ] Java 17+ installed
- [ ] Maven 3.8+ installed
- [ ] Flutter installed (for mobile dev)
- [ ] Project cloned/downloaded
- [ ] Infrastructure started: `.\dev-start.ps1 -Infrastructure`
- [ ] Backend built: `.\dev-start.ps1 -Backend`
- [ ] APIs tested: `.\dev-test.ps1 -All`
- [ ] Postman collection imported
- [ ] Mobile app dependencies installed: `.\dev-start.ps1 -Mobile`

---

**Ready to develop? Start Docker Desktop and run:**
```powershell
cd Desktop\tisqra-platform
.\dev-start.ps1 -FullStack
```

Then test:
```powershell
.\dev-test.ps1 -All
```

🎉 **Happy coding!**
