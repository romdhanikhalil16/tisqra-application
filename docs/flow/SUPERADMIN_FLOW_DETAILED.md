# 👑 SuperAdmin Complete User Flow

## SuperAdmin Role - Complete Workflow & Endpoints

This guide covers the complete workflow for SuperAdmin users with all 25+ endpoints in testing order.

---

## 📋 SuperAdmin Overview

**Role**: Super Administrator  
**Access Level**: Full system access  
**Responsibilities**: Manage organizations, admins, subscriptions, analytics, audit logs  
**Test User**: superadmin / SuperAdminPass123!  
**Email**: superadmin@tisqra.com  

---

## 🔐 Step 1: SuperAdmin Login

### Login Request
```
POST http://localhost:8080/api/v1/users/login
Content-Type: application/json

{
  "email": "superadmin@tisqra.com",
  "password": "SuperAdminPass123!"
}
```

### Expected Response
```json
{
  "userId": "superadmin-1",
  "email": "superadmin@tisqra.com",
  "firstName": "Super",
  "lastName": "Admin",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh-token-xyz",
  "expiresIn": 1800,
  "roles": ["ROLE_SUPERADMIN"]
}
```

**Next Step**: Copy `accessToken` and use in Authorization header for all subsequent requests.

---

## 📊 Step 2: View Dashboard Analytics

### Get Dashboard Metrics
```
GET http://localhost:8080/api/v1/analytics/dashboard
Authorization: Bearer {accessToken}
```

### Expected Response
```json
{
  "systemMetrics": {
    "totalOrganizations": 45,
    "totalUsers": 5000,
    "totalRevenue": 125000.00,
    "activeEvents": 89,
    "ticketsSold": 12000,
    "averageTicketPrice": 10.42
  },
  "revenueByMonth": [
    {"month": "January 2026", "revenue": 15000.00},
    {"month": "February 2026", "revenue": 18500.00}
  ],
  "topEvents": [
    {"id": "event-789", "title": "Tech Conference 2026", "revenue": 15000.00}
  ],
  "chartData": {...}
}
```

---

## 🏢 Step 3: Organization Management

### 3.1 Create New Organization

```
POST http://localhost:8080/api/v1/organizations
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "Tech Events Corporation",
  "description": "Leading tech event organizers",
  "email": "admin@techeventscorp.com",
  "phoneNumber": "+1-555-123-4567",
  "address": "456 Tech Boulevard",
  "city": "San Francisco",
  "state": "CA",
  "zipCode": "94105",
  "country": "USA",
  "website": "https://techeventscorp.com"
}
```

### Response
```json
{
  "id": "org-new-001",
  "name": "Tech Events Corporation",
  "email": "admin@techeventscorp.com",
  "status": "ACTIVE",
  "createdAt": "2026-02-25T10:00:00Z"
}
```

**Save**: Copy `org-new-001` for next steps.

---

### 3.2 View All Organizations

```
GET http://localhost:8080/api/v1/organizations?page=0&size=20&sort=createdAt,desc
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "org-new-001",
      "name": "Tech Events Corporation",
      "city": "San Francisco",
      "status": "ACTIVE",
      "createdAt": "2026-02-25T10:00:00Z"
    },
    {...more organizations}
  ],
  "pageable": {
    "totalElements": 45,
    "totalPages": 3,
    "currentPage": 0
  }
}
```

---

### 3.3 Get Organization Details

```
GET http://localhost:8080/api/v1/organizations/org-new-001
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "id": "org-new-001",
  "name": "Tech Events Corporation",
  "email": "admin@techeventscorp.com",
  "status": "ACTIVE",
  "subscriptionPlan": "FREE",
  "subscriptionExpiresAt": "2026-12-31T23:59:59Z",
  "stats": {
    "eventsCreated": 0,
    "ticketsSold": 0,
    "totalRevenue": 0.00,
    "activeEvents": 0
  },
  "createdAt": "2026-02-25T10:00:00Z"
}
```

---

### 3.4 Update Organization

```
PUT http://localhost:8080/api/v1/organizations/org-new-001
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "name": "Tech Events Corporation",
  "description": "Leading tech event organizers - Updated",
  "email": "admin@techeventscorp.com",
  "phoneNumber": "+1-555-123-4567",
  "website": "https://techeventscorp.com"
}
```

### Response
```json
{
  "id": "org-new-001",
  "name": "Tech Events Corporation",
  "description": "Leading tech event organizers - Updated",
  "updatedAt": "2026-02-25T10:05:00Z"
}
```

---

## 👨‍💼 Step 4: Manage Organization Admins

### 4.1 Create Organization Admin

```
POST http://localhost:8080/api/v1/users/register
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "firstName": "Event",
  "lastName": "Admin",
  "email": "admin@techeventscorp.com",
  "password": "AdminPass123!",
  "phoneNumber": "+1-555-987-6543",
  "organizationId": "org-new-001",
  "role": "ROLE_ADMIN_ORG"
}
```

### Response
```json
{
  "id": "admin-tech-001",
  "email": "admin@techeventscorp.com",
  "firstName": "Event",
  "lastName": "Admin",
  "organizationId": "org-new-001",
  "role": "ROLE_ADMIN_ORG",
  "createdAt": "2026-02-25T10:00:00Z"
}
```

---

### 4.2 List Organization Admins

```
GET http://localhost:8080/api/v1/users/organization/org-new-001/admins
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "admin-tech-001",
      "email": "admin@techeventscorp.com",
      "firstName": "Event",
      "lastName": "Admin",
      "role": "ROLE_ADMIN_ORG",
      "status": "ACTIVE",
      "createdAt": "2026-02-25T10:00:00Z"
    }
  ],
  "totalElements": 1
}
```

---

### 4.3 Remove Admin from Organization

```
DELETE http://localhost:8080/api/v1/users/admin-tech-001/organization
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "message": "Admin removed from organization successfully"
}
```

---

## 💳 Step 5: Manage Subscription Plans

### 5.1 View All Subscription Plans

```
GET http://localhost:8080/api/v1/subscriptions/plans
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "plan-free",
      "name": "FREE",
      "description": "Free plan for small organizers",
      "maxEvents": 5,
      "maxTicketsPerEvent": 100,
      "maxOrganizations": 1,
      "price": 0.00,
      "billingCycle": "MONTHLY"
    },
    {
      "id": "plan-basic",
      "name": "BASIC",
      "description": "Basic plan for growing organizers",
      "maxEvents": 50,
      "maxTicketsPerEvent": 5000,
      "maxOrganizations": 3,
      "price": 49.99,
      "billingCycle": "MONTHLY"
    },
    {
      "id": "plan-premium",
      "name": "PREMIUM",
      "description": "Premium plan for large organizers",
      "maxEvents": 100,
      "maxTicketsPerEvent": 10000,
      "maxOrganizations": 10,
      "price": 199.99,
      "billingCycle": "MONTHLY"
    }
  ]
}
```

---

### 5.2 Upgrade Organization Subscription

```
PUT http://localhost:8080/api/v1/subscriptions/org-new-001
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "planId": "plan-basic",
  "billingCycle": "MONTHLY"
}
```

### Response
```json
{
  "id": "subscription-001",
  "organizationId": "org-new-001",
  "planId": "plan-basic",
  "planName": "BASIC",
  "status": "ACTIVE",
  "startDate": "2026-02-25T10:00:00Z",
  "expiresAt": "2026-03-25T10:00:00Z",
  "amount": 49.99,
  "billingCycle": "MONTHLY"
}
```

---

## 📈 Step 6: View Organization Analytics

### 6.1 Get Organization Performance

```
GET http://localhost:8080/api/v1/analytics/organization/org-new-001
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "organizationId": "org-new-001",
  "organizationName": "Tech Events Corporation",
  "organizationMetrics": {
    "eventsCreated": 0,
    "ticketsSold": 0,
    "totalRevenue": 0.00,
    "activeEvents": 0,
    "totalOrders": 0
  },
  "revenueChart": [...],
  "ticketsChart": [...]
}
```

---

### 6.2 Get Sales Analytics

```
GET http://localhost:8080/api/v1/analytics/sales?startDate=2026-01-01&endDate=2026-02-28
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "totalSales": 125000.00,
  "totalOrders": 2500,
  "averageOrderValue": 50.00,
  "topOrganizations": [
    {
      "id": "org-456",
      "name": "Event Company XYZ",
      "sales": 15000.00
    }
  ],
  "topEvents": [
    {
      "id": "event-789",
      "title": "Tech Conference 2026",
      "sales": 12500.00
    }
  ]
}
```

---

## 🔐 Step 7: System Configuration

### 7.1 View System Settings

```
GET http://localhost:8080/api/v1/system/settings
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "platformName": "Tisqra Platform",
  "platformVersion": "1.0.0",
  "maxOrganizations": 1000,
  "maxUsersPerOrganization": 100,
  "emailVerificationRequired": true,
  "enableTwoFactor": false,
  "defaultTimezone": "UTC",
  "maintenanceMode": false,
  "maintenanceMessage": ""
}
```

---

### 7.2 Update System Settings

```
PUT http://localhost:8080/api/v1/system/settings
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "maintenanceMode": false,
  "emailVerificationRequired": true,
  "enableTwoFactor": false
}
```

### Response
```json
{
  "message": "System settings updated successfully",
  "updatedFields": ["maintenanceMode"]
}
```

---

## 📋 Step 8: Audit & Compliance

### 8.1 View Audit Logs

```
GET http://localhost:8080/api/v1/users/audit-logs?page=0&size=20&sort=timestamp,desc
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "audit-1",
      "userId": "superadmin-1",
      "action": "CREATE_ORGANIZATION",
      "resourceType": "ORGANIZATION",
      "resourceId": "org-new-001",
      "status": "SUCCESS",
      "timestamp": "2026-02-25T10:00:00Z",
      "details": {
        "organizationName": "Tech Events Corporation"
      }
    },
    {
      "id": "audit-2",
      "userId": "superadmin-1",
      "action": "UPDATE_SUBSCRIPTION",
      "resourceType": "SUBSCRIPTION",
      "resourceId": "subscription-001",
      "status": "SUCCESS",
      "timestamp": "2026-02-25T10:05:00Z",
      "details": {
        "oldPlan": "FREE",
        "newPlan": "BASIC"
      }
    }
  ],
  "totalElements": 250
}
```

---

### 8.2 View User Activities

```
GET http://localhost:8080/api/v1/users/activities?organizationId=org-new-001
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "activity-1",
      "userId": "admin-tech-001",
      "action": "CREATE_EVENT",
      "timestamp": "2026-02-25T10:00:00Z",
      "details": "Created event 'Tech Talk 2026'"
    }
  ]
}
```

---

## 🎯 Step 9: Reports & Exports

### 9.1 Generate Organization Report

```
GET http://localhost:8080/api/v1/reports/organization/org-new-001?format=PDF
Authorization: Bearer {accessToken}
```

### Response
```
Binary PDF file content
Content-Type: application/pdf
Content-Disposition: attachment; filename="organization-report-org-new-001.pdf"
```

---

### 9.2 Generate Sales Report

```
GET http://localhost:8080/api/v1/reports/sales?startDate=2026-01-01&endDate=2026-02-28&format=EXCEL
Authorization: Bearer {accessToken}
```

### Response
```
Binary Excel file content
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="sales-report-2026-01-02-28.xlsx"
```

---

## ✅ SuperAdmin Testing Checklist

- [ ] Login successful
- [ ] Can view dashboard analytics
- [ ] Can create organization
- [ ] Can list all organizations
- [ ] Can view organization details
- [ ] Can update organization
- [ ] Can create organization admin
- [ ] Can list organization admins
- [ ] Can remove admin
- [ ] Can view subscription plans
- [ ] Can upgrade organization subscription
- [ ] Can view organization analytics
- [ ] Can view sales analytics
- [ ] Can view system settings
- [ ] Can update system settings
- [ ] Can view audit logs
- [ ] Can view user activities
- [ ] Can generate organization report
- [ ] Can generate sales report
- [ ] Token refresh works

---

## 📊 SuperAdmin Endpoints Summary

| # | Method | Endpoint | Purpose |
|---|--------|----------|---------|
| 1 | POST | /api/v1/users/login | Login |
| 2 | GET | /api/v1/analytics/dashboard | View dashboard |
| 3 | POST | /api/v1/organizations | Create organization |
| 4 | GET | /api/v1/organizations | List organizations |
| 5 | GET | /api/v1/organizations/{id} | Get organization |
| 6 | PUT | /api/v1/organizations/{id} | Update organization |
| 7 | DELETE | /api/v1/organizations/{id} | Delete organization |
| 8 | POST | /api/v1/users/register | Create admin |
| 9 | GET | /api/v1/users/organization/{orgId}/admins | List admins |
| 10 | DELETE | /api/v1/users/{id}/organization | Remove admin |
| 11 | GET | /api/v1/subscriptions/plans | List plans |
| 12 | PUT | /api/v1/subscriptions/{orgId} | Upgrade subscription |
| 13 | GET | /api/v1/analytics/organization/{orgId} | Org analytics |
| 14 | GET | /api/v1/analytics/sales | Sales analytics |
| 15 | GET | /api/v1/system/settings | Get settings |
| 16 | PUT | /api/v1/system/settings | Update settings |
| 17 | GET | /api/v1/users/audit-logs | View audit logs |
| 18 | GET | /api/v1/users/activities | View activities |
| 19 | GET | /api/v1/reports/organization/{orgId} | Generate report |
| 20 | GET | /api/v1/reports/sales | Generate sales report |
| 21 | POST | /api/v1/users/refresh-token | Refresh token |
| 22 | GET | /api/v1/users/profile | Get profile |
| 23 | PUT | /api/v1/users/{id} | Update profile |
| 24 | POST | /api/v1/users/{id}/change-password | Change password |

---

**Total SuperAdmin Endpoints**: 24  
**Total Estimated Test Time**: 1-2 hours  
**Status**: Production Ready ✅
