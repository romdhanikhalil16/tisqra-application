# 🔐 Keycloak Authentication Setup Guide

## Complete OAuth2 & JWT Configuration for Tisqra Platform

This guide provides step-by-step instructions for setting up Keycloak authentication for the Tisqra Platform.

---

## 📋 Prerequisites

- Keycloak running on http://localhost:8180
- Admin access to Keycloak
- Postman or similar API testing tool
- Understanding of OAuth2/JWT concepts

---

## 🚀 Step 1: Access Keycloak Admin Console

### 1.1 Open Keycloak Admin Console
```
URL: http://localhost:8180
Click: "Administration Console"
```

### 1.2 Login with Default Admin Credentials
```
Username: admin
Password: admin
```

### 1.3 Verify Login
You should see the Keycloak dashboard with:
- Master realm selected
- Left sidebar with configuration options

---

## 🏗️ Step 2: Create Realm

### 2.1 Create New Realm
```
1. Click "Master" dropdown at top left
2. Click "Create Realm"
3. Enter name: "tisqra"
4. Click "Create"
```

### 2.2 Verify Realm Created
You should now see "tisqra" realm selected in the dropdown.

---

## 👥 Step 3: Create Roles

### 3.1 Create SuperAdmin Role
```
1. Navigate: Realm Settings → Roles → Create Role
2. Role Name: ROLE_SUPERADMIN
3. Description: "Super Administrator with full system access"
4. Click "Save"
```

### 3.2 Create Admin Organization Role
```
1. Click "Create Role"
2. Role Name: ROLE_ADMIN_ORG
3. Description: "Organization Administrator"
4. Click "Save"
```

### 3.3 Create Guest Role
```
1. Click "Create Role"
2. Role Name: ROLE_GUEST
3. Description: "Guest User - Can browse and purchase tickets"
4. Click "Save"
```

### 3.4 Create Scanner Role
```
1. Click "Create Role"
2. Role Name: ROLE_SCANNER
3. Description: "Scanner/Operator - Can check-in attendees"
4. Click "Save"
```

### Roles Summary
| Role | Description | Permissions |
|------|---|---|
| ROLE_SUPERADMIN | Full system access | Manage orgs, users, analytics |
| ROLE_ADMIN_ORG | Organization access | Manage events, orders, tickets |
| ROLE_GUEST | Public access | Browse, buy tickets |
| ROLE_SCANNER | Check-in access | Scan QR, check-in |

---

## 🔌 Step 4: Create OAuth2 Clients

### 4.1 Create Mobile App Client
```
1. Navigate: Clients → Create Client
2. Client Type: OpenID Connect
3. Client ID: tisqra-mobile
4. Next
5. Client Authentication: OFF (for mobile, use PKCE)
6. Authentication Flow: Standard flow, Direct access grants
7. Save
```

### 4.2 Configure Mobile Client
Go back to `tisqra-mobile` client:

**Redirect URIs:**
```
http://localhost:8080/callback
com.tisqra.mobile://oauth/callback
```

**Web Origins:**
```
http://localhost:8080
```

**Valid post logout redirect URIs:**
```
http://localhost:8080
com.tisqra.mobile://
```

### 4.3 Create Web App Client
```
1. Navigate: Clients → Create Client
2. Client Type: OpenID Connect
3. Client ID: tisqra-web
4. Next
5. Client Authentication: ON
6. Authentication Flow: Standard flow, Direct access grants
7. Save
```

### 4.4 Configure Web Client
Go back to `tisqra-web` client:

**Redirect URIs:**
```
http://localhost:3000/callback
http://localhost:3000
```

**Web Origins:**
```
http://localhost:3000
```

### 4.5 Get Client Credentials
For `tisqra-web` client:
1. Go to Credentials tab
2. Copy Client Secret (you'll need this for backend)
3. Keep it secure!

---

## 👤 Step 5: Create Test Users

### 5.1 Create SuperAdmin User
```
1. Navigate: Users → Add User
2. Username: superadmin
3. Email: superadmin@tisqra.com
4. Email verified: ON
5. First name: Super
6. Last name: Admin
7. Save
```

Set password:
```
1. Go to Credentials tab
2. Set Password
3. Value: SuperAdminPass123!
4. Temporary: OFF
5. Set Password
```

Assign role:
```
1. Go to Role Mappings
2. Client Roles: Select "tisqra-web"
3. Available Roles: ROLE_SUPERADMIN
4. Click "Add selected"
```

### 5.2 Create Organization Admin User
```
1. Navigate: Users → Add User
2. Username: admin@eventxyz
3. Email: admin@eventxyz.com
4. Email verified: ON
5. First name: Event
6. Last name: Admin
7. Save
```

Set password:
```
Value: AdminPass123!
Temporary: OFF
Set Password
```

Assign role:
```
1. Go to Role Mappings
2. Client Roles: Select "tisqra-web"
3. Available Roles: ROLE_ADMIN_ORG
4. Click "Add selected"
```

### 5.3 Create Guest User
```
1. Navigate: Users → Add User
2. Username: john.doe
3. Email: john@example.com
4. Email verified: ON
5. First name: John
6. Last name: Doe
7. Save
```

Set password:
```
Value: SecurePass123!
Temporary: OFF
Set Password
```

Assign role:
```
1. Go to Role Mappings
2. Client Roles: Select "tisqra-web"
3. Available Roles: ROLE_GUEST
4. Click "Add selected"
```

### 5.4 Create Scanner User
```
1. Navigate: Users → Add User
2. Username: scanner
3. Email: scanner@tisqra.com
4. Email verified: ON
5. First name: Event
6. Last name: Scanner
7. Save
```

Set password:
```
Value: ScannerPass123!
Temporary: OFF
Set Password
```

Assign role:
```
1. Go to Role Mappings
2. Client Roles: Select "tisqra-web"
3. Available Roles: ROLE_SCANNER
4. Click "Add selected"
```

---

## 🔑 Step 6: Configure Token Settings

### 6.1 Set Token Expiration
```
1. Navigate: Realm Settings → Tokens
2. Access Token Lifespan: 30 Minutes
3. Refresh Token Lifespan: 7 Days
4. Save
```

### 6.2 Configure Token Mappers
For each client (`tisqra-web` and `tisqra-mobile`):

```
1. Go to Client → Client Scopes
2. Go to mappers tab
3. Create Mapper for roles:
   - Name: roles
   - Mapper Type: User Client Role
   - Token Claim Name: roles
   - Full path: OFF
   - Save
```

---

## 🧪 Step 7: Test OAuth2 Token Generation

### 7.1 Get Token via Keycloak Direct Grant

**Option A: Using Postman**

```
POST /realms/tisqra/protocol/openid-connect/token
Host: localhost:8180
Content-Type: application/x-www-form-urlencoded

username=superadmin
password=SuperAdminPass123!
client_id=tisqra-web
client_secret={YOUR_CLIENT_SECRET}
grant_type=password
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 1800,
  "refresh_expires_in": 604800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "not_before_policy": 0,
  "session_state": "abc123...",
  "scope": "profile email"
}
```

### 7.2 Test Token via API

```
GET /api/v1/users/profile
Headers: Authorization: Bearer {access_token}

Response: 200 OK
{
  "id": "superadmin-1",
  "username": "superadmin",
  "email": "superadmin@tisqra.com",
  "roles": ["ROLE_SUPERADMIN"]
}
```

---

## 📱 Step 8: Setup Postman Environment

### 8.1 Create Postman Environment

Create new environment called "Tisqra Dev":

```json
{
  "name": "Tisqra Dev",
  "values": [
    {
      "key": "base_url",
      "value": "http://localhost:8080",
      "enabled": true
    },
    {
      "key": "keycloak_url",
      "value": "http://localhost:8180",
      "enabled": true
    },
    {
      "key": "realm",
      "value": "tisqra",
      "enabled": true
    },
    {
      "key": "client_id",
      "value": "tisqra-web",
      "enabled": true
    },
    {
      "key": "client_secret",
      "value": "{YOUR_CLIENT_SECRET}",
      "enabled": true
    },
    {
      "key": "access_token",
      "value": "",
      "enabled": true
    },
    {
      "key": "refresh_token",
      "value": "",
      "enabled": true
    }
  ]
}
```

### 8.2 Create Get Token Request

**Request Details:**

```
POST {{keycloak_url}}/realms/{{realm}}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

username={{username}}
password={{password}}
client_id={{client_id}}
client_secret={{client_secret}}
grant_type=password
```

**Pre-request Script:**
```javascript
// Set username and password based on selected user
const selectedUser = pm.environment.get("selected_user") || "superadmin";

if (selectedUser === "superadmin") {
  pm.environment.set("username", "superadmin");
  pm.environment.set("password", "SuperAdminPass123!");
} else if (selectedUser === "admin") {
  pm.environment.set("username", "admin@eventxyz");
  pm.environment.set("password", "AdminPass123!");
} else if (selectedUser === "guest") {
  pm.environment.set("username", "john.doe");
  pm.environment.set("password", "SecurePass123!");
} else if (selectedUser === "scanner") {
  pm.environment.set("username", "scanner");
  pm.environment.set("password", "ScannerPass123!");
}
```

**Tests (Post-request Script):**
```javascript
if (pm.response.code === 200) {
  const response = pm.response.json();
  pm.environment.set("access_token", response.access_token);
  pm.environment.set("refresh_token", response.refresh_token);
  console.log("✅ Token obtained successfully");
} else {
  console.log("❌ Token generation failed");
  console.log(pm.response.text());
}
```

### 8.3 Create Refresh Token Request

```
POST {{keycloak_url}}/realms/{{realm}}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id={{client_id}}
client_secret={{client_secret}}
grant_type=refresh_token
refresh_token={{refresh_token}}
```

**Tests:**
```javascript
if (pm.response.code === 200) {
  const response = pm.response.json();
  pm.environment.set("access_token", response.access_token);
  pm.environment.set("refresh_token", response.refresh_token);
}
```

---

## 🔄 Step 9: Understand JWT Token Structure

### 9.1 Access Token Example

Decode this JWT to see the payload:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiJzdXBlcmFkbWluIiwiaWF0IjoxNjQ0MjUwNDAwLCJleHAiOjE2NDQyNTIyMDB9.
TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ
```

**Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload:**
```json
{
  "jti": "uuid",
  "exp": 1644252200,
  "nbf": 0,
  "iat": 1644250400,
  "iss": "http://localhost:8180/realms/tisqra",
  "aud": "account",
  "sub": "superadmin",
  "typ": "Bearer",
  "azp": "tisqra-web",
  "session_state": "abc123",
  "acr": "1",
  "realm_access": {
    "roles": [
      "ROLE_SUPERADMIN"
    ]
  },
  "resource_access": {
    "tisqra-web": {
      "roles": [
        "ROLE_SUPERADMIN"
      ]
    }
  },
  "name": "Super Admin",
  "preferred_username": "superadmin",
  "given_name": "Super",
  "family_name": "Admin",
  "email": "superadmin@tisqra.com"
}
```

### 9.2 Using Token in Requests

All API requests require:

```
Authorization: Bearer {access_token}
```

Example:
```
GET /api/v1/users/profile
Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## ⏰ Step 10: Token Lifecycle

### Token Expiration Handling

```
1. Token obtained from Keycloak
2. Token valid for 30 minutes
3. API returns 401 Unauthorized when token expires
4. Use refresh_token to get new access_token
5. If refresh_token also expired, user must login again
```

### Refresh Token Flow

```
Current Token Expired?
         ↓
    Yes / No
   /      \
  Yes      No
  ↓        ↓
Refresh   Use token
Token     ↓
  ↓     Retry request
Get new
Token
  ↓
Retry request
```

---

## 🛠️ Troubleshooting

### Issue 1: Cannot Connect to Keycloak
**Solution:**
```
1. Verify Keycloak is running: docker-compose ps | grep keycloak
2. Check port 8180 is accessible: curl http://localhost:8180
3. Restart if needed: docker-compose restart tisqra-keycloak
```

### Issue 2: Invalid Client ID/Secret
**Solution:**
```
1. Go to Keycloak Admin Console
2. Navigate: Clients → tisqra-web
3. Copy Client Secret from Credentials tab
4. Update Postman environment with correct secret
```

### Issue 3: User Has No Roles
**Solution:**
```
1. Go to Keycloak Admin Console
2. Navigate: Users → Select user
3. Go to Role Mappings tab
4. Assign required role from available roles
5. Test token generation again
```

### Issue 4: Token Contains Wrong Roles
**Solution:**
```
1. Verify token mappers are configured
2. Go to Client → Scopes → Mappers
3. Ensure "roles" mapper exists and is enabled
4. Generate new token
```

### Issue 5: CORS Errors
**Solution:**
```
1. Verify API Gateway configuration
2. Check CORS headers are set correctly
3. Add origin to Web Origins in Keycloak client
```

---

## 📋 Testing Checklist

- [ ] Keycloak running and accessible
- [ ] Realm "tisqra" created
- [ ] All 4 roles created
- [ ] Clients "tisqra-web" and "tisqra-mobile" created
- [ ] 4 test users created (superadmin, admin, guest, scanner)
- [ ] All users assigned correct roles
- [ ] Token endpoint working in Postman
- [ ] Access token obtained successfully
- [ ] Token contains correct roles
- [ ] Token refresh working
- [ ] API accepts bearer token
- [ ] 401 returned for invalid token
- [ ] User profile endpoint returns user data

---

## 🔒 Security Checklist

- [ ] Client Secret never exposed in client-side code
- [ ] Use PKCE for mobile/web apps
- [ ] HTTPS in production (not HTTP)
- [ ] Token stored securely in app
- [ ] Token not logged or exposed
- [ ] Refresh token rotation implemented
- [ ] Token validation on backend
- [ ] Rate limiting on token endpoint
- [ ] Account lockout after failed logins
- [ ] Regular security audits

---

## 📚 Quick Reference

### Test User Credentials

| User Type | Username | Password | Email |
|---|---|---|---|
| SuperAdmin | superadmin | SuperAdminPass123! | superadmin@tisqra.com |
| Admin Org | admin@eventxyz | AdminPass123! | admin@eventxyz.com |
| Guest | john.doe | SecurePass123! | john@example.com |
| Scanner | scanner | ScannerPass123! | scanner@tisqra.com |

### Key Keycloak URLs

```
Admin Console: http://localhost:8180
Realm Settings: http://localhost:8180/admin/master/console/#/realms/tisqra
Users: http://localhost:8180/admin/master/console/#/realms/tisqra/users
Clients: http://localhost:8180/admin/master/console/#/realms/tisqra/clients
Token Endpoint: http://localhost:8180/realms/tisqra/protocol/openid-connect/token
```

### Common Token Endpoints

```
Get Token: POST /realms/{realm}/protocol/openid-connect/token
Refresh: POST /realms/{realm}/protocol/openid-connect/token (with refresh_token)
Logout: POST /realms/{realm}/protocol/openid-connect/logout
OpenID Config: GET /realms/{realm}/.well-known/openid-configuration
```

---

**Last Updated**: February 2026
**Version**: 1.0.0
**Status**: Production Ready ✅
