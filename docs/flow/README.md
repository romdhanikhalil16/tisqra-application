# 📱 Application Flows & Endpoints Guide

## Overview
This directory contains comprehensive documentation for the Tisqra Platform application flows, endpoints, and testing procedures organized by user roles.

## 📚 Document Structure

### Main Documentation Files

1. **FLOWS_AND_ENDPOINTS_MASTER_INDEX.md** ⭐ **START HERE**
   - Master navigation guide
   - Quick reference for all documents
   - Overview of all user roles and their flows

2. **APPLICATION_FLOWS_WITH_ENDPOINTS.md**
   - Complete application flow architecture
   - All endpoints listed in testing order
   - Service integration points

3. **USER_ROLE_ENDPOINTS_GUIDE.md**
   - Endpoints organized by user role
   - Permission levels and access control
   - Role-based endpoint mappings

### User Role Specific Flows

4. **SUPERADMIN_FLOW_DETAILED.md**
   - SuperAdmin user complete workflow
   - System management endpoints
   - Organization and admin management
   - Analytics and reporting

5. **ADMIN_ORG_FLOW_DETAILED.md**
   - Organization Admin workflow
   - Event management and creation
   - Ticket configuration and pricing
   - Order and revenue management

6. **GUEST_USER_FLOW_DETAILED.md**
   - Guest user complete journey
   - Browse and search events
   - Ticket purchase and checkout
   - Order history and ticket management

7. **SCANNER_USER_FLOW_DETAILED.md**
   - Scanner/Operator workflow
   - Event selection and ticket validation
   - Check-in and attendance tracking
   - QR code scanning procedures

### Setup & Configuration Guides

8. **KEYCLOAK_AUTH_SETUP_GUIDE.md**
   - OAuth2 and JWT authentication setup
   - Keycloak configuration
   - Token generation and refresh
   - Role mapping configuration

### API Reference & Examples

9. **API_REQUEST_RESPONSE_EXAMPLES.md**
   - Real JSON request/response examples
   - Sample data for all endpoints
   - Error response examples
   - All HTTP status codes explained

10. **USER_ROLE_ENDPOINTS_GUIDE.md**
    - Detailed endpoint reference
    - HTTP methods (GET, POST, PUT, DELETE)
    - Request parameters
    - Response schemas

### Testing Guides

11. **POSTMAN_TESTING_WORKFLOW.md**
    - Step-by-step Postman setup
    - Environment variables configuration
    - Collection import instructions
    - Test execution workflows

12. **COMPLETE_TESTING_FLOW_GUIDE.md**
    - End-to-end testing scenarios
    - Real-world use cases
    - Error handling and edge cases
    - Performance testing tips

## 🚀 Getting Started

### For Frontend Developers
1. Read `FLOWS_AND_ENDPOINTS_MASTER_INDEX.md`
2. Review your user role flow document
3. Check `API_REQUEST_RESPONSE_EXAMPLES.md` for data formats
4. Integrate with your Flutter/Web app

### For QA/Testing
1. Start with `COMPLETE_TESTING_FLOW_GUIDE.md`
2. Import Postman collection: `postman/Tisqra-Complete-User-Flows.postman_collection.json`
3. Follow `POSTMAN_TESTING_WORKFLOW.md`
4. Execute all flows and verify responses

### For Backend Developers
1. Review `APPLICATION_FLOWS_WITH_ENDPOINTS.md`
2. Check endpoint implementations match the specs
3. Verify all error responses are correct
4. Test with Postman collection

### For System Administrators
1. Read `KEYCLOAK_AUTH_SETUP_GUIDE.md`
2. Configure authentication and roles
3. Set up Postman environment
4. Validate all flows work correctly

## 📊 User Roles Overview

### 🔐 SuperAdmin
- **Access Level**: Full system access
- **Endpoints**: 25+
- **Main Functions**: Organization management, admin management, analytics, system configuration
- **File**: `SUPERADMIN_FLOW_DETAILED.md`

### 👔 Organization Admin
- **Access Level**: Organization-specific access
- **Endpoints**: 35+
- **Main Functions**: Event management, ticket configuration, order management, analytics
- **File**: `ADMIN_ORG_FLOW_DETAILED.md`

### 👥 Guest User
- **Access Level**: Public access
- **Endpoints**: 20+
- **Main Functions**: Browse events, purchase tickets, manage orders, transfer tickets
- **File**: `GUEST_USER_FLOW_DETAILED.md`

### 📱 Scanner
- **Access Level**: Event check-in access
- **Endpoints**: 10+
- **Main Functions**: Scan QR codes, validate tickets, check-in attendees
- **File**: `SCANNER_USER_FLOW_DETAILED.md`

## 🔗 Service Endpoints Summary

| Service | Port | Endpoints | Documentation |
|---------|------|-----------|---|
| API Gateway | 8080 | Gateway routing | APPLICATION_FLOWS_WITH_ENDPOINTS.md |
| User Service | 8081 | Authentication, profiles | SUPERADMIN_FLOW_DETAILED.md |
| Organization Service | 8082 | Organizations, branding | ADMIN_ORG_FLOW_DETAILED.md |
| Event Service | 8083 | Events, categories | ADMIN_ORG_FLOW_DETAILED.md |
| Order Service | 8084 | Orders, items | ADMIN_ORG_FLOW_DETAILED.md |
| Ticket Service | 8085 | Tickets, transfers | GUEST_USER_FLOW_DETAILED.md |
| Payment Service | 8086 | Payments, refunds | ADMIN_ORG_FLOW_DETAILED.md |
| Notification Service | 8087 | Emails, notifications | All flows |
| Analytics Service | 8088 | Reports, metrics | SUPERADMIN_FLOW_DETAILED.md |

## 📝 Quick Reference

### Authentication Flow
```
1. Register/Login → Get OAuth2 Token
2. Include token in Authorization header
3. Token expires in 30 minutes
4. Refresh token to get new access token
```

### Common Endpoint Patterns
```
GET    /api/v1/resource           → List all
GET    /api/v1/resource/{id}      → Get one
POST   /api/v1/resource           → Create
PUT    /api/v1/resource/{id}      → Update
DELETE /api/v1/resource/{id}      → Delete
```

## 🛠️ Tools & Resources

### Postman Collection
- **File**: `postman/Tisqra-Complete-User-Flows.postman_collection.json`
- **Contains**: 150+ pre-configured requests
- **How to use**: Import into Postman → Select environment → Run requests

### Keycloak Admin Console
- **URL**: http://localhost:8180
- **Default Realm**: tisqra
- **For**: Manage users, roles, and permissions

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **For**: Interactive API documentation

## 📈 Testing Checklist

- [ ] Setup Keycloak and create test users
- [ ] Configure Postman environment
- [ ] Test SuperAdmin flow
- [ ] Test Admin Org flow
- [ ] Test Guest User flow
- [ ] Test Scanner flow
- [ ] Verify all error cases
- [ ] Test token refresh
- [ ] Validate permissions per role
- [ ] Check API response times

## 🆘 Need Help?

1. **Don't know where to start?** → Read `FLOWS_AND_ENDPOINTS_MASTER_INDEX.md`
2. **Need specific user flow?** → Open the corresponding `*_FLOW_DETAILED.md` file
3. **Need API examples?** → Check `API_REQUEST_RESPONSE_EXAMPLES.md`
4. **Want to test in Postman?** → Follow `POSTMAN_TESTING_WORKFLOW.md`
5. **Need auth setup?** → Read `KEYCLOAK_AUTH_SETUP_GUIDE.md`

---

**Last Updated**: February 2026
**Version**: 1.0.0
**Status**: Production Ready ✅
