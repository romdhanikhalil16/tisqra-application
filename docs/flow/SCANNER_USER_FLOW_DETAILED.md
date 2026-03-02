# 📱 Scanner User Complete User Flow

## Scanner/Operator Role - Complete Workflow & Endpoints

This guide covers the complete workflow for Scanner users with all 10+ endpoints in testing order.

---

## 📋 Scanner User Overview

**Role**: Scanner/Operator  
**Access Level**: Event check-in access  
**Responsibilities**: Scan QR codes, validate tickets, check-in attendees, view attendance  
**Test User**: scanner@tisqra.com / ScannerPass123!  
**Organization**: Event Company XYZ (org-456)  

---

## 🔐 Step 1: Scanner Login

### Login Request
```
POST http://localhost:8080/api/v1/users/login
Content-Type: application/json

{
  "email": "scanner@tisqra.com",
  "password": "ScannerPass123!"
}
```

### Expected Response
```json
{
  "userId": "scanner-1",
  "email": "scanner@tisqra.com",
  "firstName": "Event",
  "lastName": "Scanner",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh-token-xyz",
  "expiresIn": 1800,
  "roles": ["ROLE_SCANNER"],
  "organizationId": "org-456"
}
```

**Save**: `accessToken` and `organizationId` for subsequent requests.

---

## 📊 Step 2: View Scanner Dashboard

### Get Scanner Profile
```
GET http://localhost:8080/api/v1/users/profile
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "id": "scanner-1",
  "firstName": "Event",
  "lastName": "Scanner",
  "email": "scanner@tisqra.com",
  "phoneNumber": "+1-555-444-5555",
  "organizationId": "org-456",
  "role": "ROLE_SCANNER",
  "createdAt": "2026-02-20T10:00:00Z",
  "lastLogin": "2026-02-25T09:00:00Z"
}
```

---

## 🎪 Step 3: Select Event for Scanning

### 3.1 Get List of Scanner's Events
```
GET http://localhost:8080/api/v1/events?organizationId=org-456&status=PUBLISHED&page=0&size=20
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "event-789",
      "title": "Tech Conference 2026",
      "location": "Convention Center, New York",
      "startDate": "2026-06-15T09:00:00Z",
      "endDate": "2026-06-15T17:00:00Z",
      "capacity": 500,
      "ticketsSold": 25,
      "status": "PUBLISHED"
    },
    {
      "id": "event-music-001",
      "title": "Summer Music Festival 2026",
      "location": "Central Park, New York",
      "startDate": "2026-07-20T14:00:00Z",
      "endDate": "2026-07-20T22:00:00Z",
      "capacity": 2000,
      "ticketsSold": 150,
      "status": "PUBLISHED"
    }
  ],
  "totalElements": 5
}
```

---

### 3.2 Get Upcoming Events (Today & This Week)
```
GET http://localhost:8080/api/v1/events?organizationId=org-456&status=PUBLISHED&upcomingOnly=true
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "event-music-001",
      "title": "Summer Music Festival 2026",
      "startDate": "2026-07-20T14:00:00Z",
      "endDate": "2026-07-20T22:00:00Z",
      "capacity": 2000,
      "ticketsSold": 150,
      "checkedIn": 0,
      "attendanceRate": 0.0
    }
  ],
  "totalElements": 1
}
```

---

### 3.3 Get Event Details for Check-in
```
GET http://localhost:8080/api/v1/events/event-music-001
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "id": "event-music-001",
  "title": "Summer Music Festival 2026",
  "location": "Central Park, New York",
  "startDate": "2026-07-20T14:00:00Z",
  "endDate": "2026-07-20T22:00:00Z",
  "capacity": 2000,
  "ticketsSold": 150,
  "status": "PUBLISHED",
  "categories": [
    {
      "id": "category-vip-001",
      "name": "VIP Pass",
      "quantity": 200,
      "sold": 50
    },
    {
      "id": "category-ga-001",
      "name": "General Admission",
      "quantity": 1500,
      "sold": 100
    }
  ]
}
```

**Save**: `eventId` = `event-music-001`

---

## 🔍 Step 4: Validate QR Codes

### 4.1 Scan and Validate QR Code
```
POST http://localhost:8080/api/v1/tickets/validate-qr
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "qrCode": "TICKET-1001-QR-CODE-STRING-DATA",
  "eventId": "event-music-001"
}
```

### Success Response (200 OK)
```json
{
  "valid": true,
  "ticket": {
    "id": "ticket-1001",
    "eventId": "event-music-001",
    "eventTitle": "Summer Music Festival 2026",
    "categoryName": "VIP Pass",
    "categoryId": "category-vip-001",
    "serialNumber": "TICKET-1001-12345",
    "status": "ACTIVE",
    "userId": "user-123",
    "userName": "John Doe",
    "userEmail": "john@example.com",
    "userPhoneNumber": "+1-555-123-4567"
  }
}
```

### Invalid QR Code Response (404 Not Found)
```json
{
  "valid": false,
  "timestamp": "2026-07-20T14:05:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Invalid QR code - Ticket not found"
}
```

---

### 4.2 Validate Already Checked-in Ticket
```
POST http://localhost:8080/api/v1/tickets/validate-qr
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "qrCode": "TICKET-1001-QR-CODE-STRING-DATA",
  "eventId": "event-music-001"
}
```

### Response (Already Checked-in)
```json
{
  "valid": false,
  "ticket": {
    "id": "ticket-1001",
    "status": "CHECKED_IN",
    "checkedInAt": "2026-07-20T14:00:00Z"
  },
  "message": "⚠️ This ticket has already been checked in at 14:00:00"
}
```

---

## ✅ Step 5: Check-in Attendees

### Check-in Ticket
```
POST http://localhost:8080/api/v1/tickets/ticket-1001/check-in
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "eventId": "event-music-001",
  "checkInTime": "2026-07-20T14:15:00Z",
  "scannedBy": "scanner-1",
  "location": "Main Entrance"
}
```

### Response (200 OK)
```json
{
  "id": "ticket-1001",
  "eventId": "event-music-001",
  "status": "CHECKED_IN",
  "checkedInAt": "2026-07-20T14:15:00Z",
  "checkedInBy": "scanner-1",
  "userName": "John Doe",
  "categoryName": "VIP Pass",
  "message": "✅ Welcome John Doe! Your ticket has been checked in successfully.",
  "displayMessage": "Welcome VIP Guest!"
}
```

---

### Check-in Multiple Tickets in Bulk
```
POST http://localhost:8080/api/v1/tickets/bulk-check-in
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "eventId": "event-music-001",
  "ticketIds": [
    "ticket-1001",
    "ticket-1002",
    "ticket-1003"
  ],
  "checkInTime": "2026-07-20T14:15:00Z",
  "scannedBy": "scanner-1"
}
```

### Response
```json
{
  "successful": 3,
  "failed": 0,
  "checkedInTickets": [
    {
      "id": "ticket-1001",
      "userName": "John Doe",
      "status": "CHECKED_IN"
    },
    {
      "id": "ticket-1002",
      "userName": "Jane Smith",
      "status": "CHECKED_IN"
    },
    {
      "id": "ticket-1003",
      "userName": "Bob Johnson",
      "status": "CHECKED_IN"
    }
  ]
}
```

---

## 📊 Step 6: View Attendance Reports

### 6.1 Get Event Attendance Summary
```
GET http://localhost:8080/api/v1/events/event-music-001/attendance
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "eventId": "event-music-001",
  "eventTitle": "Summer Music Festival 2026",
  "eventDate": "2026-07-20T14:00:00Z",
  "capacity": 2000,
  "ticketsSold": 150,
  "totalCheckedIn": 89,
  "attendanceRate": 59.3,
  "checkedInByCategory": [
    {
      "categoryName": "VIP Pass",
      "sold": 50,
      "checkedIn": 45,
      "attendanceRate": 90.0
    },
    {
      "categoryName": "General Admission",
      "sold": 100,
      "checkedIn": 44,
      "attendanceRate": 44.0
    }
  ],
  "recentCheckIns": [
    {
      "id": "ticket-1001",
      "userName": "John Doe",
      "categoryName": "VIP Pass",
      "checkedInAt": "2026-07-20T14:15:00Z",
      "checkedInBy": "scanner-1"
    },
    {
      "id": "ticket-1002",
      "userName": "Jane Smith",
      "categoryName": "General Admission",
      "checkedInAt": "2026-07-20T14:16:00Z",
      "checkedInBy": "scanner-1"
    }
  ]
}
```

---

### 6.2 Get Detailed Attendance List
```
GET http://localhost:8080/api/v1/events/event-music-001/attendance-list?page=0&size=50&sort=checkedInAt,desc
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "ticket-1001",
      "serialNumber": "TICKET-1001-12345",
      "userName": "John Doe",
      "userEmail": "john@example.com",
      "categoryName": "VIP Pass",
      "status": "CHECKED_IN",
      "checkedInAt": "2026-07-20T14:15:00Z",
      "checkedInBy": "scanner-1",
      "checkInDuration": "PT15M30S"
    },
    {
      "id": "ticket-1002",
      "serialNumber": "TICKET-1002-12346",
      "userName": "Jane Smith",
      "userEmail": "jane@example.com",
      "categoryName": "General Admission",
      "status": "CHECKED_IN",
      "checkedInAt": "2026-07-20T14:16:00Z",
      "checkedInBy": "scanner-1",
      "checkInDuration": "PT15M"
    },
    {
      "id": "ticket-1003",
      "serialNumber": "TICKET-1003-12347",
      "userName": "Bob Johnson",
      "userEmail": "bob@example.com",
      "categoryName": "VIP Pass",
      "status": "ACTIVE",
      "checkedInAt": null
    }
  ],
  "totalElements": 150,
  "checkedInCount": 89,
  "pendingCount": 61
}
```

---

### 6.3 Export Attendance Report
```
GET http://localhost:8080/api/v1/reports/event/event-music-001/attendance?format=CSV
Authorization: Bearer {accessToken}
```

### Response
```
CSV file content:
TicketID,SerialNumber,Name,Email,Category,Status,CheckedInAt,CheckedInBy
ticket-1001,TICKET-1001-12345,John Doe,john@example.com,VIP Pass,CHECKED_IN,2026-07-20T14:15:00Z,scanner-1
ticket-1002,TICKET-1002-12346,Jane Smith,jane@example.com,General Admission,CHECKED_IN,2026-07-20T14:16:00Z,scanner-1
ticket-1003,TICKET-1003-12347,Bob Johnson,bob@example.com,VIP Pass,ACTIVE,
...
```

---

## 🔧 Step 7: Scanner Tools & Utilities

### 7.1 Rescan Settings (Offline Mode Simulation)
```
POST http://localhost:8080/api/v1/scanner/settings
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "offlineMode": true,
  "allowDuplicateScans": false,
  "playSound": true,
  "vibrate": true,
  "displayName": true
}
```

### Response
```json
{
  "settingsSaved": true,
  "offlineMode": true,
  "syncStatus": "SYNCED"
}
```

---

### 7.2 Get Scanner Statistics (Real-time)
```
GET http://localhost:8080/api/v1/scanner/statistics?eventId=event-music-001
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "scannerId": "scanner-1",
  "scannerName": "Event Scanner",
  "eventId": "event-music-001",
  "totalScans": 89,
  "validScans": 89,
  "invalidScans": 0,
  "duplicateScans": 3,
  "averageScanTime": "PT5S",
  "lastScanTime": "2026-07-20T14:15:00Z",
  "scanRate": "15 scans/minute"
}
```

---

## 📋 Step 8: Ticket Lookup (Manual Entry)

### Manual Ticket Lookup by Serial Number
```
GET http://localhost:8080/api/v1/tickets/search?serialNumber=TICKET-1001-12345&eventId=event-music-001
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "id": "ticket-1001",
  "serialNumber": "TICKET-1001-12345",
  "eventId": "event-music-001",
  "eventTitle": "Summer Music Festival 2026",
  "categoryName": "VIP Pass",
  "userName": "John Doe",
  "userEmail": "john@example.com",
  "status": "ACTIVE",
  "qrCode": "TICKET-1001-QR-CODE-STRING-DATA"
}
```

---

### Manual Ticket Lookup by Email
```
GET http://localhost:8080/api/v1/tickets/search?email=john@example.com&eventId=event-music-001
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "content": [
    {
      "id": "ticket-1001",
      "serialNumber": "TICKET-1001-12345",
      "eventTitle": "Summer Music Festival 2026",
      "categoryName": "VIP Pass",
      "status": "ACTIVE",
      "userName": "John Doe"
    },
    {
      "id": "ticket-1005",
      "serialNumber": "TICKET-1005-12349",
      "eventTitle": "Summer Music Festival 2026",
      "categoryName": "General Admission",
      "status": "ACTIVE",
      "userName": "John Doe"
    }
  ],
  "totalElements": 2
}
```

---

## 🎯 Step 9: Issue Handling

### Report Ticket Issue
```
POST http://localhost:8080/api/v1/tickets/ticket-1001/report-issue
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "issueType": "DAMAGED_QR",
  "description": "QR code is not readable, customer cannot scan",
  "notes": "Reprint recommended"
}
```

### Response
```json
{
  "id": "issue-001",
  "ticketId": "ticket-1001",
  "issueType": "DAMAGED_QR",
  "status": "REPORTED",
  "reportedBy": "scanner-1",
  "reportedAt": "2026-07-20T14:20:00Z",
  "message": "Issue reported - Admin will review shortly"
}
```

---

## 📞 Step 10: Support & Communication

### Get Event Organizer Info
```
GET http://localhost:8080/api/v1/events/event-music-001/organizer
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "organizerId": "admin-event-001",
  "organizationName": "Event Company XYZ",
  "organizerName": "Event Admin",
  "organizerEmail": "admin@eventxyz.com",
  "organizerPhone": "+1-555-123-4567",
  "message": "Contact info for support during event"
}
```

---

## ✅ Scanner User Testing Checklist

- [ ] Can login successfully
- [ ] Can view profile
- [ ] Can see list of events
- [ ] Can get event details
- [ ] Can scan QR code (valid)
- [ ] Can detect invalid QR code
- [ ] Can detect already checked-in ticket
- [ ] Can check-in single ticket
- [ ] Can check-in multiple tickets
- [ ] Can view attendance summary
- [ ] Can view attendance list
- [ ] Can export attendance report
- [ ] Can configure scanner settings
- [ ] Can view scanner statistics
- [ ] Can manually lookup by serial number
- [ ] Can manually lookup by email
- [ ] Can report ticket issues
- [ ] Can get organizer contact info
- [ ] Real-time attendance updates work
- [ ] Token refresh works
- [ ] Offline mode functionality
- [ ] Sound/vibration alerts work

---

## 🎪 Real-World Scenario: Event Day Workflow

```
1. 14:00 - Event Starts
   - Scanner logs in
   - Selects event (Summer Music Festival 2026)
   - Views attendance dashboard

2. 14:00-14:30 - Early Arrivals
   - First guests start arriving
   - Scanner scans QR codes at entrance
   - System validates each ticket
   - Check-in successful

3. 14:30-15:00 - Peak Check-in Time
   - 20+ guests per minute arriving
   - Scanner handles high volume of scans
   - Bulk check-in feature for groups
   - Real-time statistics update

4. 15:00 - Event in Progress
   - Occasional late arrivals
   - Scanner continues check-ins
   - Invalid/already checked QR codes handled

5. 22:00 - Event Ends
   - Final attendance report generated
   - Export attendance list as CSV
   - Submit report to event organizer

Final Stats:
- Total tickets: 150
- Checked in: 145
- No-shows: 5
- Attendance rate: 96.7%
```

---

**Total Scanner Endpoints**: 10+  
**Total Estimated Test Time**: 30 minutes - 1 hour  
**Status**: Production Ready ✅
