# Development Setup Guide - Tisqra Platform

## Prerequisites Installation

### 1. Install Java 17+ ☕

**Required for backend development**

#### Windows
```powershell
# Download and install Eclipse Temurin (recommended)
# Visit: https://adoptium.net/temurin/releases/?version=17

# Or use Chocolatey
choco install temurin17

# Or use winget
winget install EclipseAdoptium.Temurin.17.JDK
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# Fedora/RHEL
sudo dnf install java-17-openjdk-devel
```

#### macOS
```bash
# Using Homebrew
brew install openjdk@17

# Link it
sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk \
  /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

**Verify Installation:**
```bash
java -version
# Should show: openjdk version "17.x.x"
```

### 2. Install Maven 3.8+ 📦

#### Windows
```powershell
# Using Chocolatey
choco install maven

# Or download from: https://maven.apache.org/download.cgi
```

#### Linux
```bash
# Ubuntu/Debian
sudo apt install maven

# Fedora/RHEL
sudo dnf install maven
```

#### macOS
```bash
brew install maven
```

**Verify Installation:**
```bash
mvn -version
```

### 3. Install Docker Desktop 🐳

#### Windows/macOS
Download from: https://www.docker.com/products/docker-desktop

#### Linux
```bash
# Ubuntu
sudo apt install docker.io docker-compose
sudo usermod -aG docker $USER
```

**Verify Installation:**
```bash
docker --version
docker-compose --version
```

### 4. Install Flutter (for mobile development) 📱

#### Windows
```powershell
# Download from: https://docs.flutter.dev/get-started/install/windows

# Or use Chocolatey
choco install flutter

# Or use winget
winget install Google.Flutter
```

#### Linux/macOS
```bash
# Follow official guide
# https://docs.flutter.dev/get-started/install
```

**Verify Installation:**
```bash
flutter doctor
```

---

## Quick Start Development

### Option 1: Docker-Only Development (Recommended for Quick Start)

**Best for**: Testing, quick demos, no Java installation needed

```powershell
# Start all services with Docker
cd Desktop/tisqra-platform
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

**Access Services:**
- API Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761
- PostgreSQL: localhost:5432
- Redis: localhost:6379

---

### Option 2: Local Development (Recommended for Active Development)

**Best for**: Backend development, debugging, hot reload

#### Step 1: Start Infrastructure Only
```powershell
cd Desktop/tisqra-platform

# Start only databases and message queues
docker-compose up -d postgres redis kafka zookeeper

# Verify they're running
docker-compose ps
```

#### Step 2: Build Backend Services
```powershell
# Build all services (from project root)
mvn clean install -DskipTests

# Or build specific service
cd services/user-service
mvn clean install
```

#### Step 3: Run Services Locally

**Option A: Run from IDE (Recommended)**
- Open project in IntelliJ IDEA / Eclipse / VS Code
- Import as Maven project
- Run each service's main application class:
  - `com.tisqra.user.UserServiceApplication`
  - `com.tisqra.event.EventServiceApplication`
  - etc.

**Option B: Run from Command Line**
```powershell
# Terminal 1: Start Config Server (FIRST!)
cd infrastructure/config-server
mvn spring-boot:run

# Terminal 2: Start Eureka Discovery (SECOND!)
cd infrastructure/discovery-service
mvn spring-boot:run

# Terminal 3: Start API Gateway (THIRD!)
cd infrastructure/api-gateway
mvn spring-boot:run

# Terminal 4: Start User Service
cd services/user-service
mvn spring-boot:run

# Terminal 5: Start Event Service
cd services/event-service
mvn spring-boot:run

# Continue for other services...
```

**Service Startup Order:**
1. Config Server (8888)
2. Discovery Service/Eureka (8761)
3. API Gateway (8080)
4. All other microservices (8081-8088)

---

### Option 3: Hybrid Development

**Best for**: Developing one service, others in Docker

```powershell
# Start everything with Docker
docker-compose up -d

# Stop the service you want to develop
docker-compose stop user-service

# Run that service locally
cd services/user-service
mvn spring-boot:run
```

---

## IDE Setup

### IntelliJ IDEA (Recommended)

1. **Open Project**
   - File → Open → Select `Desktop/tisqra-platform`
   - Wait for Maven import to complete

2. **Configure JDK**
   - File → Project Structure → Project
   - Set SDK to Java 17+

3. **Run Configuration**
   - Run → Edit Configurations → + → Spring Boot
   - Main class: `com.tisqra.user.UserServiceApplication`
   - Working directory: `$MODULE_WORKING_DIR$`
   - Environment variables:
     ```
     SPRING_PROFILES_ACTIVE=dev
     ```

4. **Multi-Service Run**
   - Create compound run configuration
   - Add all service configurations
   - Run all services with one click

### VS Code

1. **Install Extensions**
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Flutter (for mobile)

2. **Open Project**
   ```bash
   cd Desktop/tisqra-platform
   code .
   ```

3. **Run Service**
   - Open service's main application file
   - Click "Run" above the main method

---

## Database Setup

### PostgreSQL Databases

**Auto-created by Docker Compose:**
- tisqra_user_db
- tisqra_event_db
- tisqra_order_db
- tisqra_payment_db
- tisqra_ticket_db
- tisqra_organization_db
- tisqra_analytics_db
- tisqra_notification_db

**Connect to Database:**
```bash
# Using Docker
docker exec -it tisqra-postgres psql -U postgres

# Or use GUI tool
# Host: localhost
# Port: 5432
# User: postgres
# Password: postgres
```

**Database Tools:**
- DBeaver (recommended): https://dbeaver.io/
- pgAdmin: https://www.pgadmin.org/
- DataGrip (IntelliJ)

---

## Testing the Setup

### 1. Health Check
```bash
# Check API Gateway
curl http://localhost:8080/actuator/health

# Check User Service directly
curl http://localhost:8081/actuator/health

# Check Eureka Dashboard
# Open: http://localhost:8761
```

### 2. Test Authentication
```bash
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

### 3. Test with Postman
```
Import: Desktop/tisqra-platform/postman/Tisqra-Platform.postman_collection.json
Set base_url = http://localhost:8080
Run authentication tests
```

---

## Mobile App Development

### Setup Flutter Environment

1. **Install Dependencies**
   ```bash
   cd Desktop/tisqra-platform/mobile/tisqra_mobile_app
   flutter pub get
   ```

2. **Run Code Generation**
   ```bash
   flutter pub run build_runner build --delete-conflicting-outputs
   ```

3. **Run App**
   ```bash
   # Android emulator
   flutter run

   # iOS simulator (macOS only)
   flutter run -d ios

   # Chrome (web)
   flutter run -d chrome
   ```

4. **Connect to Local Backend**
   - Update API URL in `lib/core/constants/api_constants.dart`
   - For Android emulator: Use `http://10.0.2.2:8080`
   - For iOS simulator: Use `http://localhost:8080`
   - For physical device: Use your computer's IP

---

## Development Workflow

### Backend Development Cycle

1. **Make code changes**
2. **Hot reload (if IDE supports)**
   - Or restart service: `Ctrl+C` → `mvn spring-boot:run`
3. **Test with Postman**
4. **Run tests**: `mvn test`
5. **Commit changes**: `git commit -am "Your message"`

### Mobile Development Cycle

1. **Make code changes**
2. **Hot reload**: Press `r` in terminal or IDE
3. **Hot restart**: Press `R` for full restart
4. **Run tests**: `flutter test`
5. **Commit changes**

---

## Common Development Tasks

### Rebuild Everything
```bash
# Backend
mvn clean install

# Mobile
cd mobile/tisqra_mobile_app
flutter clean
flutter pub get
flutter pub run build_runner build --delete-conflicting-outputs
```

### View Logs
```bash
# Docker logs
docker-compose logs -f user-service

# Application logs (if running locally)
tail -f services/user-service/logs/application.log
```

### Database Operations
```bash
# Connect to database
docker exec -it tisqra-postgres psql -U postgres -d tisqra_user_db

# List tables
\dt

# Query data
SELECT * FROM users;

# Exit
\q
```

### Clear Data and Restart
```bash
# Stop everything
docker-compose down -v

# Remove volumes (clears databases)
docker volume prune

# Restart
docker-compose up -d
```

---

## Troubleshooting

### Port Already in Use
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process
taskkill /PID <PID> /F

# Or change port in application.yml
```

### Maven Build Fails
```bash
# Clear Maven cache
mvn clean

# Force update dependencies
mvn clean install -U

# Skip tests
mvn clean install -DskipTests
```

### Docker Issues
```bash
# Restart Docker Desktop
# Or from command line:
docker-compose restart

# Remove all containers and start fresh
docker-compose down -v
docker-compose up -d
```

### Flutter Issues
```bash
# Clean and rebuild
flutter clean
flutter pub get
flutter pub run build_runner build --delete-conflicting-outputs

# Check for issues
flutter doctor

# Fix Android licenses
flutter doctor --android-licenses
```

---

## Performance Tips

### Speed Up Maven Builds
```xml
<!-- Add to ~/.m2/settings.xml -->
<settings>
  <mirrors>
    <mirror>
      <id>maven-default-http-blocker</id>
      <mirrorOf>external:http:*</mirrorOf>
      <name>Pseudo repository to mirror external repositories initially using HTTP.</name>
      <url>http://0.0.0.0/</url>
      <blocked>true</blocked>
    </mirror>
  </mirrors>
</settings>
```

### Speed Up Docker
- Increase Docker Desktop memory (8GB recommended)
- Enable BuildKit: `DOCKER_BUILDKIT=1`
- Use docker-compose v2

### Speed Up Flutter
```bash
# Enable web
flutter config --enable-web

# Use local cache
flutter pub cache repair
```

---

## Next Steps

1. ✅ Complete setup above
2. 📖 Read [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)
3. 🧪 Run tests: `mvn test`
4. 🔨 Start developing!
5. 📝 Check [START_HERE.md](START_HERE.md) for quick reference

---

**Need Help?**
- Check documentation in `docs/`
- Review troubleshooting section above
- Check service logs for errors
