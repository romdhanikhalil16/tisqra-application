# 📥 Tisqra Platform - API Import Instructions for Postman & Swagger

## 🎯 Overview

You now have **5 comprehensive documentation files** to test and explore all Tisqra Platform APIs:

| File | Purpose | Format |
|------|---------|--------|
| **Tisqra-Complete-API.postman_collection.json** | Import into Postman | JSON Collection |
| **API_TESTING_GUIDE.md** | Step-by-step testing workflow | Markdown |
| **API_QUICK_REFERENCE.md** | Copy-paste curl commands | Markdown |
| **SWAGGER_ENDPOINTS.md** | All Swagger UI links & OpenAPI specs | Markdown |
| **BACKEND_STARTUP_GUIDE.md** | Backend services overview | Markdown |

---

## 📥 Step 1: Import into Postman

### Option A: Direct Import (Recommended)
1. Open **Postman** desktop application
2. Click **File** → **Import**
3. Select **File** tab
4. Browse to: `postman/Tisqra-Complete-API.postman_collection.json`
5. Click **Import**

### Option B: Copy URL and Import
1. Open **Postman**
2. Click **File** → **Import**
3. Select **Link** tab
4. Paste the file path or URL
5. Click **Import**

### Option C: Drag & Drop
1. Open **Postman**
2. Locate `postman/Tisqra-Complete-API.postman_collection.json` in file explorer
3. Drag and drop the file into Postman window
4. Click **Import**

---

## 🔐 Step 2: Set Up Authentication

### Get Keycloak Access Token

**In Postman:**
1. Navigate to **Authentication & Security** folder
2. Find **"Keycloak - Get Token"** request
3. Click **Send**
4. Copy the `access_token` value from the response

**Save Token to Variable:**
1. Click **Settings** (⚙️ gear icon)
2. Go to **Variables** tab
3. Find `access_token` variable
4. Paste the token in the **Current Value** field
5. Click **Save**

**Or via curl:**
```bash
curl -X POST http://localhost:8180/realms/tisqra-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=tisqra-client&username=admin&password=admin"
```

Copy the `access_token` from response and use it in your requests.

---

## 🧪 Step 3: Start Testing

### In Postman
1. **Health Checks** - Each service folder has a "Health Check" request
   - Start with these to verify services are running
   - Should get `{"status": "UP"}` response

2. **CRUD Operations** - Test each service:
   - Create (POST)
   - Read (GET)
   - Update (PUT)
   - Delete (DELETE)

3. **Organized by Service** - Collection structure:
   ```
   Tisqra Platform - Complete API Collection
   ├── 🔐 Authentication & Security
   ├── 👤 User Service (Port 8081)
   ├── 🏢 Organization Service (Port 8082)
   ├── 🎭 Event Service (Port 8083)
   ├── 📦 Order Service (Port 8084)
   ├── 🎫 Ticket Service (Port 8085)
   ├── 💳 Payment Service (Port 8086)
   ├── 📧 Notification Service (Port 8087)
   ├── 📈 Analytics Service (Port 8088)
   ├── 🌐 API Gateway (Port 8080)
   └── 🔍 Infrastructure Services
   ```

---

## 🌐 Step 4: Use Swagger UI (Alternative)

### Live Interactive Documentation
Instead of Postman, you can test directly in the browser using Swagger UI:

**Click any link below to open interactive API documentation:**

- [User Service Swagger](http://localhost:8081/swagger-ui.html)
- [Organization Service Swagger](http://localhost:8082/swagger-ui.html)
- [Event Service Swagger](http://localhost:8083/swagger-ui.html)
- [Order Service Swagger](http://localhost:8084/swagger-ui.html)
- [Ticket Service Swagger](http://localhost:8085/swagger-ui.html)
- [Payment Service Swagger](http://localhost:8086/swagger-ui.html)
- [Notification Service Swagger](http://localhost:8087/swagger-ui.html)
- [Analytics Service Swagger](http://localhost:8088/swagger-ui.html)

### Using Swagger UI
1. Click the link above for any service
2. Expand an endpoint (e.g., GET /api/v1/users)
3. Click **Try it out** button
4. Add authorization: Click lock icon, paste token
5. Click **Execute** to test the endpoint
6. View response in **Response** section

---

## 📖 Step 5: Read Documentation

### Quick References
- **API_QUICK_REFERENCE.md** - Copy-paste curl commands for all endpoints
- **API_TESTING_GUIDE.md** - Complete step-by-step testing workflow
- **SWAGGER_ENDPOINTS.md** - OpenAPI specs and Swagger links
- **BACKEND_STARTUP_GUIDE.md** - Services, credentials, architecture

### How to Use
1. Open any `.md` file in your text editor or GitHub
2. Find the endpoint you want to test
3. Copy the curl command or HTTP request
4. Modify as needed (IDs, payloads, etc.)
5. Execute in terminal or Postman

---

## 🚀 Complete Testing Workflow

### Flow 1: Create Event & Order
```
1. Create User (User Service)
   POST /api/v1/users
   
2. Create Organization (Organization Service)
   POST /api/v1/organizations
   
3. Create Event (Event Service)
   POST /api/v1/events
   
4. Create Order (Order Service)
   POST /api/v1/orders
   
5. Create Payment (Payment Service)
   POST /api/v1/payments
   
6. View Generated Tickets (Ticket Service)
   GET /api/v1/tickets
   
7. Analytics (Analytics Service)
   GET /api/v1/analytics/sales
```

### Flow 2: Ticket Operations
```
1. Get User's Tickets
   GET /api/v1/tickets/user/{userId}
   
2. Validate Ticket
   POST /api/v1/tickets/{ticketId}/validate
   
3. Generate QR Code
   GET /api/v1/tickets/{ticketId}/qr-code
   
4. Transfer Ticket
   POST /api/v1/tickets/{ticketId}/transfer
```

### Flow 3: Payment & Notifications
```
1. Process Payment
   POST /api/v1/payments
   
2. Send Confirmation Email
   POST /api/v1/notifications/email
   
3. Get Payment Status
   GET /api/v1/payments/{paymentId}
```

---

## 💡 Pro Tips

### 1. Save Responses as Variables
In Postman, you can save response values:
```javascript
// Add to Tests tab of a request
var jsonData = pm.response.json();
pm.environment.set("userId", jsonData.id);
```

Then use in next request:
```
http://localhost:8081/api/v1/users/{{userId}}
```

### 2. Test Collections Sequentially
1. Click **Collection Runner** icon (play button)
2. Select the collection
3. Run requests in order with auto-saved IDs

### 3. Pretty Print JSON Responses
In Postman:
- View → Response → Pretty (or Raw)
- Click **Preview** to see formatted output

### 4. Export Collections
1. Right-click collection → Export
2. Format: JSON v2.1
3. Share with team members

### 5. Create Environment for Different Servers
1. Click **Environments** → **Create New**
2. Add variables: `base_url`, `access_token`
3. Switch environments to test on dev/staging/prod

---

## 🐛 Troubleshooting

### "Cannot GET /swagger-ui.html"
- Service might not be running
- Check: `docker-compose ps`
- Restart: `docker-compose restart {service-name}`

### "401 Unauthorized"
- Token has expired (valid for 5 minutes)
- Get new token from Keycloak
- Add to Authorization header: `Bearer {new_token}`

### "503 Service Unavailable"
- Service might be initializing
- Check logs: `docker-compose logs {service-name}`
- Wait 30-60 seconds and retry
- Restart service: `docker-compose restart {service-name}`

### "Cannot connect to localhost:PORT"
- Verify service is running: `docker-compose ps`
- Check port mapping in `docker-compose.yml`
- Ensure Docker is running
- Try: `telnet localhost 8081`

### Postman Variables Not Working
1. Check syntax: `{{variable_name}}`
2. Verify variable exists in **Variables** tab
3. Ensure variable has a value
4. Try **Ctrl+Shift+P** → Clear cache

---

## 📊 File Locations

```
tisqra-platform/
├── postman/
│   ├── Tisqra-Complete-API.postman_collection.json    ← IMPORT THIS
│   ├── Tisqra-Platform.postman_collection.json
│   └── Advanced-Endpoints.postman_collection.json
│
├── API_TESTING_GUIDE.md                                 ← READ THIS
├── API_QUICK_REFERENCE.md                               ← COPY FROM THIS
├── SWAGGER_ENDPOINTS.md                                 ← LINK REFERENCE
├── BACKEND_STARTUP_GUIDE.md                             ← BACKGROUND INFO
└── API_IMPORT_INSTRUCTIONS.md                           ← YOU ARE HERE
```

---

## ✅ Verification Checklist

After importing into Postman:

- [ ] Collection imported successfully
- [ ] Access token obtained from Keycloak
- [ ] Token variable set in Postman
- [ ] Health check for User Service returns UP
- [ ] Health check for Event Service returns UP
- [ ] Can create a new user (POST)
- [ ] Can get all users (GET)
- [ ] Can update a user (PUT)
- [ ] Can delete a user (DELETE)
- [ ] Tested at least 3 different services
- [ ] Opened Swagger UI for one service
- [ ] Executed request in Swagger UI
- [ ] Reviewed one of the markdown documentation files

---

## 🎓 Learning Path

### Beginner
1. Import Postman collection
2. Get Keycloak token
3. Test User Service endpoints (Create, Read, Update, Delete)
4. Read API_QUICK_REFERENCE.md

### Intermediate
1. Create complete event booking flow (User → Organization → Event → Order)
2. Test Payment Service
3. Test Ticket Service (transfer, validate)
4. Read API_TESTING_GUIDE.md

### Advanced
1. Test event-driven features (Kafka topics)
2. Test Analytics endpoints
3. Test Notification Service
4. Study BACKEND_STARTUP_GUIDE.md for architecture
5. Explore OpenAPI specs at `/v3/api-docs` endpoints

---

## 📞 Quick Support

### Service Won't Start?
```bash
# Check status
docker-compose ps

# Check logs
docker-compose logs service-name

# Restart
docker-compose restart service-name
```

### Need Database Access?
```bash
# Connect to PostgreSQL
docker exec -it eventticket-postgres psql -U postgres -d eventticket_db

# List tables
\dt
```

### Keycloak Issues?
- Admin Console: http://localhost:8180/admin
- Username: admin
- Password: admin

---

## 📚 Additional Resources

- **Spring Cloud Documentation**: https://spring.io/projects/spring-cloud
- **Swagger/OpenAPI 3.0**: https://swagger.io/specification/
- **Postman Learning**: https://learning.postman.com/
- **Keycloak Docs**: https://www.keycloak.org/documentation
- **Apache Kafka**: https://kafka.apache.org/documentation/

---

## 🎉 You're All Set!

You now have everything needed to:
✅ Import APIs into Postman
✅ Test all 8 microservices
✅ View interactive Swagger documentation
✅ Access copy-paste curl commands
✅ Understand the backend architecture

**Start with:** Import `postman/Tisqra-Complete-API.postman_collection.json` into Postman!

---

**Questions?** Check the relevant markdown file or Swagger UI for your service.

*Last Updated: 2026-02-23*
