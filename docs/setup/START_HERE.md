# 🚀 START HERE - Tisqra Platform

Welcome to the **Tisqra Platform**! This guide will get you up and running in minutes.

---

## 📋 What is Tisqra Platform?

Tisqra Platform is a comprehensive event management and ticketing system built with:
- **Backend**: Spring Boot microservices (8 services)
- **Mobile**: Flutter app with clean architecture
- **Infrastructure**: Docker, Kubernetes, Redis, Kafka, PostgreSQL
- **CI/CD**: GitHub Actions + Jenkins pipelines

---

## ✅ Prerequisites

### Required
- **Java 17+** (for backend)
- **Maven 3.8+** (for backend)
- **Docker & Docker Compose** (for containerization)
- **Flutter 3.0+** (for mobile app)
- **Git** (version control)

### Optional
- **Kubernetes/kubectl** (for K8s deployment)
- **Postman** (for API testing)
- **Jenkins** (for CI/CD)
- **IDE**: IntelliJ IDEA / VS Code / Android Studio

---

## 🎯 Quick Start (3 Steps)

### Step 1: Clone & Setup
```powershell
# Navigate to project
cd Desktop/tisqra-platform

# Run quick start script
./scripts/quick-start.ps1  # Windows
./scripts/quick-start.sh   # Linux/Mac
```

This will:
- ✅ Install dependencies
- ✅ Build all services
- ✅ Start Docker containers
- ✅ Initialize databases
- ✅ Start all microservices

### Step 2: Test the APIs
```powershell
# Import Postman collection
# File: Desktop/tisqra-platform/postman/Tisqra-Platform.postman_collection.json

# Set environment variable in Postman:
base_url = http://localhost:8080

# Test authentication
POST http://localhost:8080/api/user/auth/register
POST http://localhost:8080/api/user/auth/login
```

### Step 3: Run Mobile App
```bash
cd Desktop/tisqra-platform/mobile/tisqra_mobile_app

# Get dependencies
flutter pub get

# Run app (connects to backend automatically)
flutter run
```

---

## 📁 Project Structure

```
tisqra-platform/
├── services/                    # 8 Microservices
│   ├── user-service/           # Authentication & Users (formerly user-service)
│   ├── event-service/          # Event management
│   ├── order-service/          # Order processing
│   ├── payment-service/        # Payment handling
│   ├── ticket-service/         # Ticket generation
│   ├── organization-service/   # Organization management
│   ├── analytics-service/      # Analytics & reporting
│   └── notification-service/   # Email/SMS notifications
│
├── infrastructure/              # Infrastructure Services
│   ├── api-gateway/            # API Gateway (Port 8080)
│   ├── config-server/          # Config Server (Port 8888)
│   └── discovery-service/      # Eureka Server (Port 8761)
│
├── mobile/                      # Mobile Application
│   └── tisqra_mobile_app/      # Flutter mobile app
│
├── shared/                      # Shared Libraries
│   ├── common-models/          # Common data models
│   └── kafka-events/           # Kafka event definitions
│
├── deployment/                  # Deployment Configurations
│   ├── kubernetes/             # K8s manifests
│   └── docker/                 # Docker configs
│
├── jenkins/                     # Jenkins CI/CD
│   ├── pipelines/              # Pipeline definitions
│   └── shared-libraries/       # Reusable functions
│
├── .github/workflows/           # GitHub Actions
├── postman/                     # API testing collections
└── scripts/                     # Utility scripts
```

---

## 🔑 Key Services & Ports

| Service | Port | Description |
|---------|------|-------------|
| **API Gateway** | 8080 | Main entry point for all APIs |
| **User Service** | 8081 | User authentication & management |
| **Event Service** | 8082 | Event CRUD operations |
| **Order Service** | 8083 | Order management |
| **Payment Service** | 8084 | Payment processing |
| **Ticket Service** | 8085 | Ticket generation & validation |
| **Organization Service** | 8086 | Organization management |
| **Analytics Service** | 8087 | Analytics & reporting |
| **Notification Service** | 8088 | Email/SMS notifications |
| **Config Server** | 8888 | Configuration management |
| **Eureka Server** | 8761 | Service discovery |

### Infrastructure Services
| Service | Port | Credentials |
|---------|------|-------------|
| **PostgreSQL** | 5432 | postgres/postgres |
| **Redis** | 6379 | (no auth) |
| **Kafka** | 9092 | (no auth) |
| **Zookeeper** | 2181 | (no auth) |

---

## 🎨 API Endpoints Overview

### User Service (Authentication)
```http
POST   /api/user/auth/register     # Register new user
POST   /api/user/auth/login        # Login
POST   /api/user/auth/refresh      # Refresh token
GET    /api/user/users/{id}        # Get user profile
PUT    /api/user/users/{id}        # Update user
DELETE /api/user/users/{id}        # Delete user
```

### Event Service
```http
GET    /api/events                 # List all events
POST   /api/events                 # Create event
GET    /api/events/{id}            # Get event details
PUT    /api/events/{id}            # Update event
DELETE /api/events/{id}            # Delete event
GET    /api/events/search          # Search events
```

### Order Service
```http
GET    /api/orders                 # List orders
POST   /api/orders                 # Create order
GET    /api/orders/{id}            # Get order details
PUT    /api/orders/{id}/cancel     # Cancel order
```

### Payment Service
```http
POST   /api/payments               # Process payment
GET    /api/payments/{id}          # Get payment status
POST   /api/payments/webhook       # Payment webhook
```

### Ticket Service
```http
GET    /api/tickets                # List tickets
POST   /api/tickets                # Generate ticket
GET    /api/tickets/{id}           # Get ticket
POST   /api/tickets/validate       # Validate QR code
```

**Full API documentation**: See Postman collection for all 50+ endpoints!

---

## 🧪 Testing

### 1. API Testing with Postman
```bash
# Import collection
File: Desktop/tisqra-platform/postman/Tisqra-Platform.postman_collection.json

# Also import advanced endpoints
File: Desktop/tisqra-platform/postman/Advanced-Endpoints.postman_collection.json
```

### 2. Unit Tests
```bash
# Test all services
mvn test

# Test specific service
cd services/user-service
mvn test
```

### 3. Integration Tests
```bash
# Run integration tests
mvn verify
```

### 4. Mobile Tests
```bash
cd mobile/tisqra_mobile_app
flutter test
```

---

## 🐳 Docker Deployment

### Development
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Production
```bash
cd deployment/docker
docker-compose -f docker-compose.prod.yml up -d
```

---

## ☸️ Kubernetes Deployment

### Deploy to Kubernetes
```bash
cd deployment

# Windows
./deploy.ps1

# Linux/Mac
./deploy.sh
```

### Manual Deployment
```bash
# Create namespace
kubectl apply -f kubernetes/namespace.yaml

# Deploy infrastructure
kubectl apply -f kubernetes/postgres-deployment.yaml
kubectl apply -f kubernetes/redis-deployment.yaml
kubectl apply -f kubernetes/kafka-deployment.yaml

# Deploy services
kubectl apply -f kubernetes/api-gateway-deployment.yaml
kubectl apply -f kubernetes/user-service-deployment.yaml
kubectl apply -f kubernetes/event-service-deployment.yaml

# Check status
kubectl get pods -n tisqra
kubectl get services -n tisqra
```

---

## 🔄 CI/CD

### GitHub Actions
Workflows are automatically triggered on:
- **Push to main**: Full CI/CD pipeline
- **Pull request**: CI tests only
- **Tag push**: Release workflow

### Jenkins
```bash
# Start Jenkins
cd jenkins
./setup-jenkins.ps1  # Windows
./setup-jenkins.sh   # Linux/Mac

# Access Jenkins
http://localhost:9090
```

---

## 📚 Documentation

### Core Documentation
- **[README.md](README.md)** - Project overview
- **[GETTING_STARTED.md](GETTING_STARTED.md)** - Detailed setup
- **[DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)** - Development best practices
- **[MIGRATION_SUMMARY.md](MIGRATION_SUMMARY.md)** - Migration from old naming
- **[CHANGELOG.md](CHANGELOG.md)** - Version history

### Technical Documentation
- **[CI_CD_GUIDE.md](CI_CD_GUIDE.md)** - CI/CD pipelines
- **[JENKINS_GUIDE.md](JENKINS_GUIDE.md)** - Jenkins setup
- **[ARCHITECTURE_OVERVIEW.md](ARCHITECTURE_OVERVIEW.md)** - System architecture
- **[deployment/README.md](deployment/README.md)** - Deployment strategies

### API Documentation
- **[postman/README.md](postman/README.md)** - API testing guide
- **[postman/ADVANCED_API_GUIDE.md](postman/ADVANCED_API_GUIDE.md)** - Advanced features

### Mobile Documentation
- **[mobile/tisqra_mobile_app/README.md](mobile/tisqra_mobile_app/README.md)** - Flutter app guide

---

## 🛠️ Common Tasks

### Build Everything
```bash
# Backend
mvn clean install

# Mobile
cd mobile/tisqra_mobile_app
flutter build apk
flutter build ios
```

### Database Management
```bash
# Connect to PostgreSQL
docker exec -it tisqra-postgres psql -U postgres

# List databases
\l

# Connect to specific database
\c tisqra_user_db

# List tables
\dt
```

### View Logs
```bash
# Docker logs
docker-compose logs -f user-service

# Kubernetes logs
kubectl logs -f deployment/user-service -n tisqra

# Application logs
tail -f services/user-service/logs/application.log
```

### Scale Services (Kubernetes)
```bash
# Scale user-service to 3 replicas
kubectl scale deployment user-service --replicas=3 -n tisqra

# Check status
kubectl get pods -n tisqra
```

---

## 🐛 Troubleshooting

### Backend Issues

**Problem**: Services won't start
```bash
# Check if ports are available
netstat -ano | findstr :8080

# Kill process using port
taskkill /PID <PID> /F

# Restart services
docker-compose restart
```

**Problem**: Database connection errors
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Restart PostgreSQL
docker-compose restart postgres

# Check connection
docker exec -it tisqra-postgres psql -U postgres -c "SELECT 1"
```

### Mobile Issues

**Problem**: Flutter build errors
```bash
# Clean and rebuild
flutter clean
flutter pub get
flutter run
```

**Problem**: Can't connect to backend
```bash
# Check backend is running
curl http://localhost:8080/actuator/health

# Update API URL in Flutter app
# File: lib/core/constants/api_constants.dart
```

### CI/CD Issues

**Problem**: Jenkins build fails
```bash
# Check Jenkins logs
docker logs jenkins

# Restart Jenkins
cd jenkins
docker-compose restart
```

**Problem**: GitHub Actions failing
- Check workflow logs in GitHub
- Verify secrets are configured
- Check Docker Hub credentials

---

## 📞 Support

### Resources
- **Documentation**: See `Desktop/tisqra-platform/docs/`
- **Issue Tracker**: GitHub Issues
- **Wiki**: Project Wiki (if available)

### Need Help?
1. Check documentation first
2. Search existing issues
3. Create new issue with:
   - Description of problem
   - Steps to reproduce
   - Expected vs actual behavior
   - Logs/screenshots

---

## 🎯 Next Steps

### For Developers
1. ✅ Complete quick start above
2. 📖 Read [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)
3. 🧪 Run tests: `mvn test`
4. 🔨 Start developing!

### For DevOps
1. ✅ Review [deployment/README.md](deployment/README.md)
2. 🐳 Set up Docker environment
3. ☸️ Configure Kubernetes cluster
4. 🔄 Set up CI/CD pipelines

### For QA
1. ✅ Import Postman collection
2. 🧪 Review test scenarios
3. 📝 Create test plans
4. 🐛 Start testing!

---

## 📊 Project Status

- **Version**: 2.0.0
- **Status**: ✅ Ready for Development
- **Last Updated**: 2026-02-17
- **Services**: 8 microservices + 3 infrastructure
- **Mobile**: Flutter app with clean architecture
- **CI/CD**: GitHub Actions + Jenkins
- **Deployment**: Docker + Kubernetes ready

---

## 🌟 Features

✅ User authentication & authorization  
✅ Event management (CRUD)  
✅ Ticket generation & QR codes  
✅ Order & payment processing  
✅ Organization management  
✅ Analytics & reporting  
✅ Email/SMS notifications  
✅ Multi-tenancy support  
✅ Clean architecture  
✅ Microservices architecture  
✅ Event-driven communication  
✅ API Gateway & service discovery  
✅ Centralized configuration  
✅ Docker containerization  
✅ Kubernetes deployment  
✅ CI/CD pipelines  
✅ Comprehensive API testing  

---

**Ready to start? Run the quick start script and you'll be up in 5 minutes!** 🚀

```powershell
cd Desktop/tisqra-platform
./scripts/quick-start.ps1
```
