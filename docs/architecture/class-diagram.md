---
config:
  layout: elk
---
classDiagram
direction TB
    class User {
	    -UUID id
	    -String email
	    -String passwordHash
	    -String firstName
	    -String lastName
	    -String phone
	    -UserRole role
	    -Boolean isActive
	    -LocalDateTime createdAt
	    +login() void
	    +logout() void
	    +resetPassword() void
	    +updateProfile() void
    }

    class Auditable {
	    +getCreatedAt() LocalDateTime
	    +getUpdatedAt() LocalDateTime
	    +getCreatedBy() UUID
    }

    class AdminOrg {
	    -UUID organizationId
	    +createEvent() Event
	    +managePermissions() void
	    +manageStaff() void
	    +viewDashboard() Dashboard
	    +configureOrganization() void
	    +inviteGuests() List~Ticket~
    }

    class Guest {
	    -List~String~ preferences
	    +searchEvents() List~Event~
	    +purchaseTickets() Order
	    +viewMyTickets() List~Ticket~
	    +transferTicket() void
    }

    class Scanner {
	    -UUID assignedEventId
	    -String deviceId
	    +validateQRCode() ValidationResult
	    +viewEntranceStats() Stats
    }

    class SubscriptionPlan {
	    -UUID id
	    -String name
	    -BigDecimal priceMonthly
	    -BigDecimal priceYearly
	    -Integer maxEventsPerMonth
	    -Integer maxTicketsPerEvent
	    -BigDecimal commissionPercentage
	    -Map features
	    +isFeatureEnabled(String) Boolean
	    +calculateCommission(BigDecimal) BigDecimal
    }

    class Branding {
	    -UUID organizationId
	    -String primaryColor
	    -String secondaryColor
	    -String logoUrl
	    +applyToTicket() String
	    +applyToEmail() String
    }

    class Event {
	    -UUID id
	    -UUID organizationId
	    -String name
	    -String slug
	    -String description
	    -EventCategory category
	    -LocalDateTime startDate
	    -LocalDateTime endDate
	    -Location location
	    -Integer capacity
	    -EventStatus status
	    -List~TicketCategory~ categories
	    -List~EventSchedule~ schedule
	    +publish() void
	    +cancel() void
	    +addCategory() TicketCategory
	    +getAvailableTickets() Integer
	    +isBookable() Boolean
    }

    class Location {
	    -String name
	    -String address
	    -String city
	    -Double latitude
	    -Double longitude
	    -String mapsUrl
	    +getDistance(Location) Double
	    +getGoogleMapsUrl() String
    }

    class EventSchedule {
	    -UUID id
	    -UUID eventId
	    -LocalTime time
	    -String title
	    -String description
	    -String speaker
	    -Integer sortOrder
    }

    class TicketCategory {
	    -UUID id
	    -UUID eventId
	    -String name
	    -String description
	    -BigDecimal price
	    -String currency
	    -Integer quantity
	    -Integer soldCount
	    -Integer reservedCount
	    -LocalDateTime saleStartDate
	    -LocalDateTime saleEndDate
	    -String color
	    -List~String~ features
	    +isAvailable() Boolean
	    +getAvailableCount() Integer
	    +incrementSold(Integer) void
	    +reserve(Integer) void
	    +releaseReservation(Integer) void
    }

    class PromoCode {
	    -UUID id
	    -String code
	    -UUID eventId
	    -BigDecimal discountValue
	    -Integer maxUses
	    -Integer usedCount
	    -LocalDateTime validFrom
	    -LocalDateTime validUntil
	    +isValid() Boolean
	    +calculateDiscount(BigDecimal) BigDecimal
	    +incrementUsage() void
    }

    class Order {
	    -UUID id
	    -String orderNumber
	    -UUID userId
	    -UUID eventId
	    -OrderStatus status
	    -BigDecimal subtotal
	    -BigDecimal discountAmount
	    -BigDecimal totalAmount
	    -String currency
	    -String promoCode
	    -List~OrderItem~ items
	    -Payment payment
	    -LocalDateTime expiresAt
	    +calculateTotal() BigDecimal
	    +applyPromoCode(PromoCode) void
	    +confirmPayment() void
	    +cancel() void
	    +isExpired() Boolean
    }

    class OrderItem {
	    -UUID id
	    -UUID orderId
	    -UUID ticketCategoryId
	    -Integer quantity
	    -BigDecimal unitPrice
	    -BigDecimal totalPrice
	    +calculateTotal() BigDecimal
    }

    class Payment {
	    -UUID id
	    -UUID orderId
	    -BigDecimal amount
	    -String currency
	    -PaymentMethod method
	    -String provider
	    -String providerPaymentId
	    -PaymentStatus status
	    -LocalDateTime paidAt
	    +process() PaymentResult
	    +refund(BigDecimal) Refund
	    +verify() Boolean
    }

    class PaymentRefund {
	    -UUID id
	    -UUID paymentId
	    -BigDecimal amount
	    -String reason
	    -RefundStatus status
	    -LocalDateTime processedAt
	    +process() void
    }

    class Ticket {
	    -UUID id
	    -String ticketNumber
	    -UUID orderId
	    -UUID eventId
	    -UUID ticketCategoryId
	    -String qrCode
	    -byte[] qrCodeImage
	    -String ownerEmail
	    -String ownerName
	    -AttendeeInfo attendee
	    -TicketStatus status
	    -Boolean isTransferable
	    -LocalDateTime validatedAt
	    -UUID validatedBy
	    +generateQRCode() void
	    +validate() ValidationResult
	    +transfer(String) void
	    +invalidate() void
	    +download() byte[]
    }

    class AttendeeInfo {
	    -String firstName
	    -String lastName
	    -String email
	    -String phone
	    +getFullName() String
    }

    class TicketTransfer {
	    -UUID id
	    -UUID ticketId
	    -String fromEmail
	    -String toEmail
	    -String message
	    -Boolean accepted
	    -LocalDateTime acceptedAt
	    +accept() void
	    +reject() void
    }

    class ValidationResult {
	    -Boolean valid
	    -String reason
	    -Ticket ticket
	    -String message
	    -LocalDateTime validatedAt
	    -String validatedBy
    }

    class AnalyticsEvent {
	    -UUID id
	    -String eventType
	    -UUID aggregateId
	    -UUID organizationId
	    -Map~String,Object~ data
	    -LocalDateTime occurredAt
	    +track() void
    }

    class SalesAnalytics {
	    -UUID id
	    -UUID organizationId
	    -UUID eventId
	    -LocalDate date
	    -Integer hour
	    -Integer ticketsSold
	    -BigDecimal revenue
	    -BigDecimal netRevenue
	    +calculateMetrics() void
    }

    class Dashboard {
	    -UUID organizationId
	    -LocalDate startDate
	    -LocalDate endDate
	    -BigDecimal totalRevenue
	    -Integer totalTicketsSold
	    -List~Event~ topEvents
	    -Map demographics
	    +generateReport() Report
    }

    class Notification {
	    -UUID id
	    -UUID userId
	    -NotificationType type
	    -NotificationChannel channel
	    -String subject
	    -String content
	    -LocalDateTime sentAt
	    +send() void
	    +markAsRead() void
    }

    class UserRole {
	    SUPER_ADMIN
	    ADMIN_ORG
	    GUEST
	    SCANNER
    }

    class NotificationType {
	    INFO,
	    WARNING,
	    ALERT,
	    REMINDER
    }

    class NotificationChannel {
	    EMAIL,
	    SMS,
	    PUSH,
	    IN_APP
    }

    class EventStatus {
	    DRAFT
	    PUBLISHED
	    CANCELLED
	    COMPLETED
    }

    class TicketStatus {
	    VALID
	    USED
	    CANCELLED
	    TRANSFERRED
	    EXPIRED
    }

    class PaymentStatus {
	    PROCESSING
	    COMPLETED
	    FAILED
	    REFUNDED
    }

    class PaymentMethod {
	    CARD
	    MOBILE_MONEY
	    BANK_TRANSFER
	    PAYPAL
	    WALLET
    }

    class OrderStatus {
    }

    class EmailTemplate {
	    -templateName
	    -templateType
	    -subject
    }

    class Organization {
	    -UUID id
	    -String name
	    -String email
	    -String domain
	    -SubscriptionPlan plan
	    -UUID ownerId
	    -Branding branding
	    -EmailTemplate emailTemplate
	    +createEvent() Event
	    +updateBranding() void
	    +getAnalytics() Analytics
	    +upgradeSubscription() void
    }

    class SuperAdmin {
        +createOrganization() Organization
        +manageSubscriptionPlans() void
        +viewGlobalAnalytics() Analystics
        +suspendOrganization() void
    }

	<<abstract>> User
	<<interface>> Auditable
	<<enumeration>> UserRole
	<<enumeration>> NotificationType
	<<enumeration>> NotificationChannel
	<<enumeration>> EventStatus
	<<enumeration>> TicketStatus
	<<enumeration>> PaymentStatus
	<<enumeration>> PaymentMethod
	<<enumeration>> OrderStatus

    User <|-- SuperAdmin
    User <|-- AdminOrg
    User <|-- Guest
    User <|-- Scanner
    User ..|> Auditable
    Organization "1" --> "1" SubscriptionPlan : subscribes
    Organization "1" --> "1" Branding : has
    Organization "1" --> "*" Event : hosts
    Organization "1" --> "1" User : owned by
    Event "1" --> "1" Location : located at
    Event "1" --> "*" TicketCategory : offers
    Event "1" --> "*" EventSchedule : has
    Event "1" --> "*" PromoCode : has
    Event "1" --> "*" Order : receives
    Order "1" --> "*" OrderItem : contains
    Order "1" --> "1" Payment : paid by
    Order "1" --> "*" Ticket : generates
    OrderItem "*" --> "1" TicketCategory : references
    Payment "1" --> "*" PaymentRefund : refunded via
    Ticket "*" --> "1" TicketCategory : belongs to
    Ticket "1" --> "1" AttendeeInfo : assigned to
    Ticket "1" --> "*" TicketTransfer : transferred via
    Ticket --> ValidationResult : validates to
    Guest "1" --> "*" Order : places
    Guest "1" --> "*" Ticket : owns
    Scanner --> ValidationResult : produces
    Organization "1" --> "*" AnalyticsEvent : generates
    Organization "1" --> "*" SalesAnalytics : tracks
    Organization "1" --> "1" Dashboard : views
    User "1" --> "*" Notification : receives
    Organization "1" --> "*" EmailTemplate : uses