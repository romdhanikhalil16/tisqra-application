# Backend Architecture Guide - Complete Beginner's Guide to Tisqra Microservices

**Welcome!** This guide will teach you everything about how the Tisqra backend works, from the very basics to advanced concepts. Think of this as your complete textbook for understanding microservices with Spring Boot.

---

## Table of Contents

1. [What is a Microservice?](#what-is-a-microservice)
2. [Overall System Architecture](#overall-system-architecture)
3. [Infrastructure Components](#infrastructure-components)
4. [Business Services](#business-services)
5. [How Data Flows](#how-data-flows)
6. [Understanding Spring Boot Layers](#understanding-spring-boot-layers)
7. [Communication Between Services](#communication-between-services)
8. [Shared Libraries](#shared-libraries)
9. [Database Architecture](#database-architecture)
10. [Security and Authentication](#security-and-authentication)
11. [Complete Request Flow Examples](#complete-request-flow-examples)
12. [Best Practices and Patterns](#best-practices-and-patterns)

---

## 1. What is a Microservice?

### Traditional Monolithic Applications

Imagine you're building a house where every room, the kitchen, bathroom, bedroom, and living room are all built as one giant structure. If you want to renovate the kitchen, you might affect the entire house.

**A monolithic application** works the same way:
- All code lives in one large application
- If one part breaks, the whole application can crash
- Scaling means duplicating the entire application
- Different teams can't work independently

### Microservices Architecture

Now imagine building a house where each room is a separate, self-contained unit:
- The kitchen has its own plumbing and electricity
- The bedroom is independent
- You can renovate one room without touching others
- Each room can be built by different teams

**A microservices architecture** is similar:
- The application is split into small, independent services
- Each service has one specific job (Single Responsibility Principle)
- Services communicate over the network (like phone calls between rooms)
- If one service crashes, others keep running
- Each service can be scaled independently

### Why Tisqra Uses Microservices

Tisqra is an event ticketing platform. We split it into microservices because:
1. **User Service** handles authentication - high security needs
2. **Event Service** manages events - might get heavy traffic during popular event launches
3. **Order Service** processes purchases - needs to be highly reliable
4. **Payment Service** handles money - needs strict compliance
5. **Ticket Service** generates tickets - resource-intensive operations
6. **Notification Service** sends emails/push notifications - can fail independently

If email service is down, users can still buy tickets!

---

## 2. Overall System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          MOBILE/WEB CLIENTS                         │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 │ HTTPS Requests
                                 ▼
                    ┌────────────────────────┐
                    │    API GATEWAY         │◄────┐
                    │    (Port 8080)         │     │
                    └────────────┬───────────┘     │
                                 │                  │
                    Discovers ───┘                  │ Registers
                    Services                        │
                                                    │
                    ┌────────────────────────┐     │
                    │  DISCOVERY SERVICE     │◄────┤
                    │  (Eureka - Port 8761)  │     │
                    └────────────────────────┘     │
                                                    │
┌────────────────────────────────────────────────────────────────────┐│
│                     BUSINESS MICROSERVICES                         ││
├────────────────────────────────────────────────────────────────────┤│
│                                                                    ││
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           ││
│  │ User Service │  │Event Service │  │Order Service │           ││
│  │  Port 8081   │  │  Port 8083   │  │  Port 8084   │           ││
│  │  Database:   │  │  Database:   │  │  Database:   │           ││
│  │  user_db     │  │  event_db    │  │  order_db    │           ││
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘           ││
│         │                  │                  │                   ││
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           ││
│  │Ticket Service│  │Payment Svc   │  │Notification  │           ││
│  │  Port 8085   │  │  Port 8086   │  │Service       │           ││
│  │  Database:   │  │  Database:   │  │  Port 8087   │           ││
│  │  ticket_db   │  │  payment_db  │  │  Database:   │           ││
│  └──────┬───────┘  └──────┬───────┘  │notification  │           ││
│         │                  │          │  _db         │           ││
│  ┌──────────────┐  ┌──────────────┐  └──────┬───────┘           ││
│  │Organization  │  │Analytics     │         │                   ││
│  │Service       │  │Service       │         │                   ││
│  │  Port 8082   │  │  Port 8088   │         │                   ││
│  └──────────────┘  └──────────────┘         │                   ││
│         │                  │                  │                   ││
└─────────┼──────────────────┼──────────────────┼───────────────────┘│
          │                  │                  │                    │
          └──────────┬───────┴──────────┬───────┘                    │
                     │                  │                             │
         ┌───────────▼──────────────────▼──────────┐                 │
         │         KAFKA MESSAGE BUS               │                 │
         │      (Async Communication)              │                 │
         │  Topics: order.created, event.published │                 │
         │  payment.completed, ticket.generated    │                 │
         └─────────────────────────────────────────┘                 │
                                                                      │
         ┌─────────────────────────────────────────┐                 │
         │        SHARED INFRASTRUCTURE            │                 │
         ├─────────────────────────────────────────┤                 │
         │  PostgreSQL (Multiple Databases)        │                 │
         │  Redis (Caching)                        │                 │
         │  Keycloak (Authentication)              │                 │
         └─────────────────────────────────────────┘                 │
```

### Key Components:

1. **API Gateway** - Single entry point for all client requests
2. **Discovery Service** - Phone directory for services to find each other
3. **8 Business Services** - Each handles specific business functionality
4. **Kafka** - Message bus for asynchronous communication
5. **Databases** - Each service has its own database (Database per Service pattern)
6. **Redis** - Caching layer for performance
7. **Keycloak** - Centralized authentication and authorization

---

## 3. Infrastructure Components

These are the "foundation" services that support the business services.

### 3.1 Discovery Service (Netflix Eureka)

**What it does:** Acts like a phone directory for microservices.

**The Problem it Solves:**

In microservices, services are deployed on different servers/ports. How does the Order Service know where the Event Service is located? Hardcoding addresses like `http://192.168.1.10:8083` is bad because:
- IP addresses change
- Services can move to different servers
- Multiple instances of a service might exist

**How it Works:**

1. **Service Registration:** When a service starts, it registers itself with Eureka
   ```java
   @SpringBootApplication
   @EnableDiscoveryClient  // <-- This annotation registers with Eureka
   public class EventServiceApplication {
       public static void main(String[] args) {
           SpringApplication.run(EventServiceApplication.class, args);
       }
   }
   ```

2. **Heartbeat:** Every 30 seconds, services send a heartbeat to Eureka saying "I'm alive!"

3. **Service Discovery:** When Service A needs to call Service B:
   - It asks Eureka: "Where is Service B?"
   - Eureka responds: "Service B is at http://192.168.1.10:8083"
   - Service A calls Service B

**Configuration (application.yml):**
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/  # Where is Eureka?
    register-with-eureka: true                     # Register myself
    fetch-registry: true                           # Get other services list
  instance:
    prefer-ip-address: true                        # Use IP instead of hostname
```

**Real-World Analogy:**
Think of Eureka as a hotel reception desk. When guests (services) check in, they register their room number. When someone wants to visit them, they ask reception for the room number instead of wandering around.

---

### 3.2 API Gateway (Spring Cloud Gateway)

**What it does:** Single entry point for all client requests. All requests go through the gateway, which routes them to the correct service.

**The Problem it Solves:**

Without a gateway:
- Mobile app needs to know 8 different service addresses
- Each service needs its own authentication
- CORS configuration becomes complex
- No centralized logging/monitoring

**How it Works:**

The gateway uses **route definitions** to forward requests:

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Route for Event Service
        - id: event-service                    # Unique route name
          uri: lb://event-service              # lb = Load Balanced (uses Eureka)
          predicates:
            - Path=/api/events/**              # Match requests starting with /api/events
          filters:
            - StripPrefix=1                    # Remove /api from path before forwarding

        # Route for Order Service  
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
```

**Request Flow Example:**

1. Mobile app sends: `GET http://gateway:8080/api/events/123`
2. Gateway matches route: "This matches `/api/events/**` pattern"
3. Gateway asks Eureka: "Where is event-service?"
4. Eureka responds: "http://event-service:8083"
5. Gateway forwards to: `http://event-service:8083/events/123` (notice /api removed)
6. Event Service responds with event data
7. Gateway returns response to mobile app

**Benefits:**
- Single URL for clients: `http://gateway:8080`
- Centralized JWT authentication
- Load balancing across multiple instances
- Rate limiting, logging, monitoring

**Real-World Analogy:**
The gateway is like a receptionist at a company. Instead of visitors going directly to different departments, they tell the receptionist what they need, and the receptionist directs them to the right department.

---

### 3.3 Config Server (Optional - Not heavily used in this project)

**What it does:** Centralized configuration management for all services.

Instead of each service having its own `application.yml`, they fetch configuration from a central server. This allows changing configuration without redeploying services.

---

## 4. Business Services

Now let's explore each business service in detail.

### 4.1 User Service (Identity Service)

**Port:** 8081  
**Database:** user_db  
**Responsibility:** User authentication, registration, profile management

**Main Classes:**

```
user-service/
└── src/main/java/com/tisqra/user/
    ├── IdentityServiceApplication.java        # Main entry point
    ├── domain/                                 # Business logic layer
    │   ├── model/
    │   │   ├── User.java                      # User entity (database model)
    │   │   └── AuditLog.java                  # Audit trail
    │   └── repository/
    │       ├── UserRepository.java            # Database queries
    │       └── AuditLogRepository.java
    ├── application/                            # Application layer
    │   ├── service/
    │   │   ├── UserService.java               # Business logic
    │   │   ├── AuthenticationService.java     # Login/Register
    │   │   └── AuditLogService.java
    │   ├── dto/                                # Data Transfer Objects
    │   │   ├── UserDTO.java                   # User data for API responses
    │   │   ├── LoginRequest.java              # Login API request
    │   │   └── RegisterUserRequest.java
    │   └── mapper/
    │       └── UserMapper.java                # Convert Entity ↔ DTO
    └── infrastructure/                         # Infrastructure layer
        ├── controller/
        │   ├── AuthController.java            # REST endpoints /api/auth/*
        │   └── UserController.java            # REST endpoints /api/users/*
        ├── config/
        │   ├── SecurityConfig.java            # JWT validation
        │   └── RedisConfig.java               # Caching setup
        └── keycloak/
            └── KeycloakAdminClient.java       # Integration with Keycloak
```

**Key Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login and get JWT token |
| POST | /api/auth/refresh | Refresh expired token |
| GET | /api/users/{id} | Get user profile |
| PUT | /api/users/{id} | Update user profile |

---

### 4.2 Event Service

**Port:** 8083  
**Database:** event_db  
**Responsibility:** Manage events, ticket categories, schedules, promo codes

**Main Domain Model (Event.java):**

```java
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;                           // Unique identifier
    
    private UUID organizationId;               // Which organization owns this event
    private String name;                       // "Taylor Swift Concert"
    private String slug;                       // "taylor-swift-concert" (URL-friendly)
    private String description;
    private EventCategory category;            // MUSIC, SPORTS, CONFERENCE, etc.
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    @Embedded                                  // Embedded object (not separate table)
    private Location location;                 // Venue address, coordinates
    
    private Integer capacity;                  // Max attendees
    private EventStatus status;                // DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED
    
    @OneToMany(mappedBy = "event")            // One event has many ticket categories
    private List<TicketCategory> categories;   // VIP, General Admission, etc.
    
    @OneToMany(mappedBy = "event")
    private List<EventSchedule> schedule;      // Event agenda/schedule
    
    // Business methods
    public void publish() {
        this.status = EventStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
    
    public boolean isBookable() {
        return status == EventStatus.PUBLISHED && 
               LocalDateTime.now().isBefore(startDate);
    }
    
    public Integer getAvailableTickets() {
        return categories.stream()
            .mapToInt(TicketCategory::getAvailableCount)
            .sum();
    }
}
```

**Key Concepts:**

1. **@Entity**: Tells Spring "This is a database table"
2. **@OneToMany**: Defines relationship - One event has many ticket categories
3. **Business Methods**: `publish()`, `isBookable()` - Domain logic lives in the entity
4. **@Embedded**: Location is stored in the same table, not a separate one

**Service Layer (EventService.java):**

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {
    
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate;
    
    @Transactional  // Start a database transaction
    public EventDTO createEvent(CreateEventRequest request) {
        // 1. Validate: Check if organization can create events
        checkOrganizationCanCreateEvent(request.getOrganizationId());
        
        // 2. Business validation
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date must be after start date");
        }
        
        // 3. Generate unique slug from name
        String slug = generateSlug(request.getName());
        
        // 4. Create entity from request
        Event event = eventMapper.toEntity(request);
        event.setSlug(slug);
        event.setStatus(EventStatus.DRAFT);
        
        // 5. Add ticket categories
        if (request.getTicketCategories() != null) {
            request.getTicketCategories().forEach(catRequest -> {
                TicketCategory category = ticketCategoryMapper.toEntity(catRequest);
                event.addCategory(category);
            });
        }
        
        // 6. Save to database
        event = eventRepository.save(event);
        
        // 7. Notify other services (call organization service via REST)
        notifyOrganizationService(event.getOrganizationId());
        
        // 8. Return DTO (not entity)
        return eventMapper.toDTO(event);
    }
    
    @Cacheable(value = "events", key = "#id")  // Cache the result
    public EventDTO getEventById(UUID id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        return eventMapper.toDTO(event);
    }
}
```

**Key Annotations Explained:**

- **@Service**: Marks this as a Spring service component
- **@RequiredArgsConstructor**: Lombok generates constructor for dependency injection
- **@Transactional**: Database transaction - all-or-nothing operation
- **@Cacheable**: Stores result in Redis cache for faster subsequent reads

**Controller Layer (EventController.java):**

```java
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    
    private final EventService eventService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventDTO event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable UUID id) {
        EventDTO event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventDTO>> getUpcomingEvents(Pageable pageable) {
        Page<EventDTO> events = eventService.getUpcomingEvents(pageable);
        return ResponseEntity.ok(events);
    }
}
```

**Key Annotations:**

- **@RestController**: This class handles HTTP requests and returns JSON
- **@RequestMapping**: Base path for all endpoints
- **@PostMapping/@GetMapping**: HTTP method mappings
- **@PathVariable**: Extracts value from URL path
- **@RequestBody**: Converts JSON to Java object
- **@Valid**: Validates the request
- **@PreAuthorize**: Security check - only admins can create events

---

### 4.3 Order Service

**Port:** 8084  
**Database:** order_db  
**Responsibility:** Process ticket orders, manage cart, handle order lifecycle

**Order Creation Flow (Detailed):**

```java
@Transactional
public OrderDTO createOrder(CreateOrderRequest request) {
    // Step 1: Generate unique order number
    String orderNumber = generateOrderNumber();  // "ORD-20260218143025-A3F2B1"
    
    // Step 2: Create order entity
    Order order = Order.builder()
        .orderNumber(orderNumber)
        .userId(request.getUserId())
        .eventId(request.getEventId())
        .status(OrderStatus.PENDING)
        .expiresAt(LocalDateTime.now().plusMinutes(15))  // 15-minute reservation
        .build();
    
    // Step 3: For each ticket type in the order
    for (var itemRequest : request.getItems()) {
        // SYNCHRONOUS CALL: Fetch ticket details from Event Service
        Map<String, Object> categoryDetails = fetchTicketCategoryDetails(
            itemRequest.getTicketCategoryId()
        );
        
        BigDecimal unitPrice = new BigDecimal(categoryDetails.get("price").toString());
        
        // SYNCHRONOUS CALL: Reserve tickets in Event Service
        reserveTickets(itemRequest.getTicketCategoryId(), itemRequest.getQuantity());
        
        OrderItem item = OrderItem.builder()
            .ticketCategoryId(itemRequest.getTicketCategoryId())
            .quantity(itemRequest.getQuantity())
            .unitPrice(unitPrice)
            .build();
        
        order.addItem(item);
    }
    
    // Step 4: Calculate total
    order.calculateTotal();
    
    // Step 5: Save to database
    order = orderRepository.save(order);
    
    // Step 6: ASYNCHRONOUS: Publish Kafka event
    OrderCreatedEvent event = OrderCreatedEvent.builder()
        .orderId(order.getId())
        .userId(order.getUserId())
        .totalAmount(order.getTotalAmount())
        .build();
    kafkaTemplate.send(KafkaTopics.ORDER_CREATED, event);
    
    return orderMapper.toDTO(order);
}
```

**Scheduled Job - Auto-Expire Orders:**

```java
@Scheduled(fixedRate = 60000)  // Run every 60 seconds
@Transactional
public void expireOrders() {
    List<Order> expiredOrders = orderRepository.findExpiredOrders(LocalDateTime.now());
    
    for (Order order : expiredOrders) {
        // Release reserved tickets back to event service
        for (OrderItem item : order.getItems()) {
            releaseTickets(item.getTicketCategoryId(), item.getQuantity());
        }
        
        order.setStatus(OrderStatus.EXPIRED);
        orderRepository.save(order);
    }
}
```

**Why 15-minute expiry?** This prevents users from reserving tickets indefinitely without paying, making them unavailable to others.

---

### 4.4 Notification Service

**Port:** 8087  
**Database:** notification_db  
**Responsibility:** Send emails, push notifications, SMS

**Kafka Event Listener:**

```java
@Service
@RequiredArgsConstructor
public class NotificationEventListener {
    
    private final EmailService emailService;
    private final PushNotificationService pushNotificationService;
    
    @KafkaListener(topics = "notification.email.send", groupId = "notification-service")
    public void handleSendEmailEvent(SendEmailEvent event) {
        log.info("Received email notification event: {}", event.getNotificationId());
        
        emailService.sendEmail(
            null,
            event.getRecipientEmail(),
            event.getRecipientName(),
            event.getType(),
            event.getSubject(),
            event.getTemplateName(),
            event.getTemplateData()
        );
    }
    
    @KafkaListener(topics = "notification.push.send", groupId = "notification-service")
    public void handleSendPushEvent(SendPushEvent event) {
        log.info("Received push notification event: {}", event.getNotificationId());
        
        pushNotificationService.sendPushNotification(
            event.getUserId(),
            event.getDeviceToken(),
            event.getType(),
            event.getTitle(),
            event.getBody(),
            event.getData()
        );
    }
}
```

**Key Concept - Event-Driven Architecture:**

Instead of Order Service directly calling Notification Service to send email (tight coupling), it publishes an event to Kafka. Notification Service listens to this event and sends the email. This makes the system:
- **Loosely coupled**: Order Service doesn't know about Notification Service
- **Resilient**: If Notification Service is down, order still completes
- **Scalable**: Can add multiple notification consumers

---

## 5. Understanding Spring Boot Layers

Every microservice follows a **layered architecture**. Think of it like a building with floors:

```
┌──────────────────────────────────────┐
│  CONTROLLER LAYER (Infrastructure)   │  ← HTTP Requests come here
│  - REST Controllers                  │  ← Handles JSON, validation
│  - Request/Response DTOs             │
└────────────┬─────────────────────────┘
             │
             ▼
┌──────────────────────────────────────┐
│  SERVICE LAYER (Application)         │  ← Business logic lives here
│  - Business rules                    │  ← Orchestrates operations
│  - Transaction management            │  ← Calls repositories
└────────────┬─────────────────────────┘
             │
             ▼
┌──────────────────────────────────────┐
│  REPOSITORY LAYER (Domain)           │  ← Database access
│  - JPA Repositories                  │  ← CRUD operations
│  - Database queries                  │
└────────────┬─────────────────────────┘
             │
             ▼
┌──────────────────────────────────────┐
│  DATABASE (PostgreSQL)               │  ← Data storage
│  - Tables, Indexes                   │
└──────────────────────────────────────┘
```

### 5.1 Domain Layer

**Contains:**
- **Entities**: Database models (e.g., `Event.java`, `Order.java`)
- **Repositories**: Database query interfaces
- **Value Objects**: Immutable objects like `Location`, `AttendeeInfo`

**Example Entity:**

```java
@Entity
@Table(name = "events")
@EntityListeners(AuditingEntityListener.class)  // Automatic createdAt/updatedAt
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**Key Annotations:**

- **@Entity**: JPA annotation - this class maps to a database table
- **@Table(name = "events")**: Table name in database
- **@Id**: Primary key field
- **@GeneratedValue**: Auto-generate UUID
- **@Column**: Column configuration
- **@CreatedDate/@LastModifiedDate**: Automatically set by Spring
- **@Data**: Lombok - generates getters, setters, toString, equals, hashCode
- **@Builder**: Lombok - enables builder pattern: `Event.builder().name("Concert").build()`
- **@NoArgsConstructor/@AllArgsConstructor**: Lombok - generates constructors

**Example Repository:**

```java
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    
    // Spring Data JPA auto-generates this query
    Optional<Event> findBySlug(String slug);
    
    // Custom query using @Query annotation
    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND e.startDate >= :now")
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Method name query - Spring generates SQL
    Page<Event> findByOrganizationId(UUID organizationId, Pageable pageable);
}
```

**How Spring Data JPA Works:**

1. **Method Name Queries**: Spring parses method names and generates SQL
   - `findBySlug(String slug)` → `SELECT * FROM events WHERE slug = ?`
   - `findByOrganizationId(UUID id)` → `SELECT * FROM events WHERE organization_id = ?`

2. **@Query Annotation**: Write custom JPQL (Java Persistence Query Language)
   - JPQL uses entity names, not table names
   - `FROM Event` not `FROM events`

3. **Pageable**: Built-in pagination support
   ```java
   Pageable pageable = PageRequest.of(0, 20, Sort.by("startDate").descending());
   // Page 0, size 20, sorted by startDate descending
   ```

---

### 5.2 Application Layer

**Contains:**
- **Services**: Business logic
- **DTOs**: Data Transfer Objects for API
- **Mappers**: Convert Entity ↔ DTO

**Why DTOs?**

Never expose entities directly to the API! Here's why:

```java
// BAD: Exposing entity directly
@GetMapping("/{id}")
public Event getEvent(@PathVariable UUID id) {
    return eventRepository.findById(id).orElseThrow();
}
```

**Problems:**
1. Exposes database structure to clients
2. Can't customize API response
3. Lazy-loading issues (accessing related entities after transaction)
4. Security risk (exposing sensitive fields)

```java
// GOOD: Using DTO
@GetMapping("/{id}")
public EventDTO getEvent(@PathVariable UUID id) {
    Event event = eventRepository.findById(id).orElseThrow();
    return eventMapper.toDTO(event);  // Convert to DTO
}
```

**DTO Example:**

```java
@Data
@Builder
public class EventDTO {
    private UUID id;
    private String name;
    private String slug;
    private EventStatus status;
    private Integer availableTickets;  // Calculated field, not in database
    // No sensitive fields like internal IDs
}
```

**Mapper (MapStruct):**

```java
@Mapper(componentModel = "spring")
public interface EventMapper {
    
    @Mapping(target = "availableTickets", expression = "java(event.getAvailableTickets())")
    EventDTO toDTO(Event event);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Event toEntity(CreateEventRequest request);
}
```

MapStruct generates implementation at compile time. No reflection, very fast!

---

### 5.3 Infrastructure Layer

**Contains:**
- **Controllers**: REST endpoints
- **Configuration**: Spring configs
- **External integrations**: Keycloak, payment gateways

**Controller Best Practices:**

```java
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management APIs")  // Swagger doc
@SecurityRequirement(name = "bearer-jwt")
public class EventController {
    
    private final EventService eventService;
    
    @PostMapping
    @Operation(summary = "Create a new event")  // Swagger doc
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<EventDTO> createEvent(
            @Valid @RequestBody CreateEventRequest request) {
        EventDTO event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }
}
```

**Key Points:**
1. Controllers are **thin** - no business logic
2. Always validate input with `@Valid`
3. Use proper HTTP status codes (201 Created, 200 OK, 404 Not Found)
4. Use DTOs, never entities
5. Add security checks with `@PreAuthorize`

---

## 6. Communication Between Services

Microservices communicate in two ways: **Synchronous (REST)** and **Asynchronous (Kafka)**.

### 6.1 Synchronous Communication (REST)

**When to use:** When you need an immediate response.

**Example:** Order Service needs ticket price from Event Service

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final RestTemplate restTemplate;
    
    private Map<String, Object> fetchTicketCategoryDetails(UUID categoryId) {
        // Service name (not IP address!) - Eureka resolves it
        String url = "http://event-service/api/ticket-categories/" + categoryId;
        
        // Make HTTP GET request
        return restTemplate.getForObject(url, Map.class);
    }
    
    private void reserveTickets(UUID categoryId, Integer quantity) {
        String url = "http://event-service/api/ticket-categories/" + 
                     categoryId + "/reserve?quantity=" + quantity;
        
        // Make HTTP POST request
        restTemplate.postForObject(url, null, Void.class);
    }
}
```

**How it works:**

1. Order Service calls: `http://event-service/api/ticket-categories/123`
2. RestTemplate asks Eureka: "Where is event-service?"
3. Eureka responds: "event-service is at 192.168.1.10:8083"
4. RestTemplate makes HTTP request: `http://192.168.1.10:8083/api/ticket-categories/123`
5. Event Service responds with ticket data
6. RestTemplate returns response to Order Service

**RestTemplate Configuration:**

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    @LoadBalanced  // Enable service discovery via Eureka
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**Pros:**
- Simple to understand
- Immediate response
- Easy to debug

**Cons:**
- Tight coupling - if Event Service is down, Order Service fails
- Slower - waits for response
- Cascading failures

---

### 6.2 Asynchronous Communication (Kafka)

**When to use:** Fire-and-forget scenarios, notifications, event-driven workflows.

**Example:** When order is created, notify multiple services

```java
// Order Service - Publish event
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        // ... create order logic ...
        
        Order order = orderRepository.save(order);
        
        // Publish event to Kafka
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(order.getId())
            .orderNumber(order.getOrderNumber())
            .userId(order.getUserId())
            .eventId(order.getEventId())
            .totalAmount(order.getTotalAmount())
            .createdAt(order.getCreatedAt())
            .build();
        
        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, event);
        
        return orderMapper.toDTO(order);
    }
}
```

```java
// Notification Service - Listen to event
@Service
@RequiredArgsConstructor
public class NotificationEventListener {
    
    private final EmailService emailService;
    
    @KafkaListener(topics = "order.created", groupId = "notification-service")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Send order confirmation email
        emailService.sendOrderConfirmation(
            event.getUserId(),
            event.getOrderNumber(),
            event.getTotalAmount()
        );
    }
}
```

```java
// Analytics Service - Also listens to same event
@Service
@RequiredArgsConstructor
public class AnalyticsEventListener {
    
    private final SalesAnalyticsRepository analyticsRepository;
    
    @KafkaListener(topics = "order.created", groupId = "analytics-service")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Record sales data
        SalesAnalytics sales = SalesAnalytics.builder()
            .orderId(event.getOrderId())
            .eventId(event.getEventId())
            .amount(event.getTotalAmount())
            .recordedAt(LocalDateTime.now())
            .build();
        
        analyticsRepository.save(sales);
    }
}
```

**How it works:**

1. Order Service publishes `OrderCreatedEvent` to Kafka topic `order.created`
2. Kafka stores the event
3. Notification Service consumes the event and sends email
4. Analytics Service consumes the same event and records statistics
5. Both services process independently - if one fails, the other continues

**Kafka Topics Configuration:**

```java
public final class KafkaTopics {
    // Order topics
    public static final String ORDER_CREATED = "order.created";
    
    // Event topics
    public static final String EVENT_PUBLISHED = "event.published";
    
    // Payment topics
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";
    
    // Notification topics
    public static final String NOTIFICATION_EMAIL_SEND = "notification.email.send";
    public static final String NOTIFICATION_PUSH_SEND = "notification.push.send";
}
```

**Pros:**
- Loose coupling - services don't know about each other
- Resilient - if consumer is down, event is replayed later
- Scalable - multiple consumers can process same event
- Asynchronous - doesn't block the calling service

**Cons:**
- More complex to debug
- Eventual consistency (not immediate)
- Requires Kafka infrastructure

---

### 6.3 When to Use Which?

| Scenario | Communication Type | Reason |
|----------|-------------------|--------|
| Get ticket price | Synchronous (REST) | Need immediate response |
| Reserve tickets | Synchronous (REST) | Must confirm availability before proceeding |
| Send email notification | Asynchronous (Kafka) | Don't need to wait for email to send |
| Record analytics | Asynchronous (Kafka) | Fire-and-forget |
| Verify payment | Synchronous (REST) | Must know if payment succeeded |
| Generate tickets after payment | Asynchronous (Kafka) | Can happen in background |

---

## 7. Shared Libraries

Tisqra uses shared libraries to avoid code duplication.

### 7.1 Common Models

**Location:** `shared/common-models/`

Contains shared enums and exceptions used across all services:

```java
// EventStatus.java - Shared enum
public enum EventStatus {
    DRAFT,          // Event is being created
    PUBLISHED,      // Event is live and accepting bookings
    ONGOING,        // Event is currently happening
    COMPLETED,      // Event has finished
    CANCELLED       // Event has been cancelled
}

// OrderStatus.java
public enum OrderStatus {
    PENDING,        // Order created, awaiting payment
    CONFIRMED,      // Payment received
    COMPLETED,      // Tickets generated
    CANCELLED,      // Order cancelled
    EXPIRED,        // Reservation time expired
    REFUNDED        // Payment refunded
}

// BusinessException.java - Custom exception
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

// ResourceNotFoundException.java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: %s", resource, field, value));
    }
}
```

**Usage in services:**

```java
// In Event Service
if (event.getStatus() != EventStatus.DRAFT) {
    throw new BusinessException("Only draft events can be published");
}

// In Order Service
Order order = orderRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
```

---

### 7.2 Kafka Events

**Location:** `shared/kafka-events/`

Contains event classes for Kafka communication:

```java
// OrderCreatedEvent.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private UUID orderId;
    private String orderNumber;
    private UUID userId;
    private UUID eventId;
    private BigDecimal totalAmount;
    private String currency;
    private Integer ticketCount;
    private LocalDateTime createdAt;
}

// EventPublishedEvent.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventPublishedEvent {
    private UUID eventId;
    private UUID organizationId;
    private String eventName;
    private String slug;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime publishedAt;
}
```

**Why shared events?**
- Both publisher and consumer need same class definition
- Ensures type safety
- Prevents version mismatch issues

---

## 8. Database Architecture

### 8.1 Database per Service Pattern

Each microservice has its **own database**. Services cannot directly access another service's database.

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  User Service   │     │  Event Service  │     │  Order Service  │
│   Port: 8081    │     │   Port: 8083    │     │   Port: 8084    │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                        │
         ▼                       ▼                        ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    user_db      │     │    event_db     │     │    order_db     │
│                 │     │                 │     │                 │
│  - users        │     │  - events       │     │  - orders       │
│  - audit_logs   │     │  - categories   │     │  - order_items  │
└─────────────────┘     │  - schedules    │     └─────────────────┘
                        │  - promo_codes  │
                        └─────────────────┘
```

**Benefits:**
1. **Loose coupling**: Services can evolve independently
2. **Scalability**: Can scale databases independently
3. **Resilience**: Database failure affects only one service
4. **Technology flexibility**: Can use different database types (PostgreSQL, MongoDB, etc.)

**Challenges:**
1. **No joins**: Can't join data across services
2. **Data consistency**: Harder to maintain consistency
3. **Transactions**: Can't use database transactions across services

---

### 8.2 Database Migrations (Flyway)

Each service uses **Flyway** for database version control.

**Migration files:** `src/main/resources/db/migration/`

```
event-service/src/main/resources/db/migration/
├── V1__create_events_table.sql
├── V2__create_ticket_categories_table.sql
├── V3__create_event_schedules_table.sql
└── V4__create_promo_codes_table.sql
```

**Example Migration (V1__create_events_table.sql):**

```sql
CREATE TABLE events (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(300) NOT NULL,
    slug VARCHAR(150) UNIQUE NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    capacity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    published_at TIMESTAMP
);

CREATE INDEX idx_event_slug ON events(slug);
CREATE INDEX idx_event_org_id ON events(organization_id);
CREATE INDEX idx_event_status ON events(status);
```

**How Flyway works:**

1. On service startup, Flyway checks database
2. Compares applied migrations vs. available migration files
3. Runs any new migrations in order (V1, V2, V3...)
4. Records applied migrations in `flyway_schema_history` table

**Configuration:**

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

---

## 9. Security and Authentication

### 9.1 JWT Authentication

Tisqra uses **JWT (JSON Web Token)** for authentication via Keycloak.

**Authentication Flow:**

```
1. User logs in via mobile app
   ↓
2. Request goes to API Gateway → User Service
   ↓
3. User Service validates credentials with Keycloak
   ↓
4. Keycloak returns JWT token
   ↓
5. Mobile app stores JWT token
   ↓
6. For subsequent requests, app includes JWT in Authorization header
   ↓
7. API Gateway validates JWT with Keycloak
   ↓
8. If valid, forwards request to appropriate service
```

**JWT Token Example:**

```
Header: Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...

Decoded JWT:
{
  "sub": "user-uuid-123",
  "email": "john@example.com",
  "roles": ["USER"],
  "exp": 1708272000
}
```

**SecurityConfig.java (in each service):**

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/**").permitAll()  // Health checks
                .requestMatchers("/swagger-ui/**").permitAll() // API docs
                .anyRequest().authenticated()                  // All other requests need auth
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter()))
            );
        
        return http.build();
    }
}
```

**Role-Based Access Control:**

```java
@RestController
@RequestMapping("/api/events")
public class EventController {
    
    // Only SUPER_ADMIN and ADMIN_ORG can create events
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_ORG')")
    public ResponseEntity<EventDTO> createEvent(@RequestBody CreateEventRequest request) {
        // ...
    }
    
    // Anyone can view events (but must be authenticated)
    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable UUID id) {
        // ...
    }
}
```

---

## 10. Complete Request Flow Examples

Let's trace complete user journeys through the system.

### 10.1 Complete Flow: User Purchases Event Tickets

**Scenario:** John wants to buy 2 VIP tickets for "Taylor Swift Concert"

```
┌─────────────┐
│ Mobile App  │
└──────┬──────┘
       │
       │ 1. POST /api/orders
       │    {
       │      "userId": "john-uuid",
       │      "eventId": "concert-uuid",
       │      "items": [
       │        {"ticketCategoryId": "vip-uuid", "quantity": 2}
       │      ]
       │    }
       ↓
┌──────────────────┐
│  API Gateway     │
│  Port 8080       │
└────────┬─────────┘
         │ 2. Validates JWT token
         │ 3. Routes to Order Service
         ↓
┌──────────────────────────────────────────────────────────┐
│  Order Service (Port 8084)                               │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  OrderService.createOrder()                              │
│                                                           │
│  Step 1: Generate order number                           │
│  orderNumber = "ORD-20260218143025-A3F2B1"              │
│                                                           │
│  Step 2: Call Event Service to get ticket price         │
│  ┌────────────────────────────────────────┐             │
│  │ REST CALL                               │             │
│  │ GET http://event-service/api/          │             │
│  │     ticket-categories/vip-uuid          │             │
│  └────────────────────────────────────────┘             │
│         ↓                                                 │
│  ┌──────────────────────────────────┐                   │
│  │ Event Service responds:          │                   │
│  │ {                                 │                   │
│  │   "id": "vip-uuid",              │                   │
│  │   "name": "VIP Ticket",          │                   │
│  │   "price": 250.00,               │                   │
│  │   "available": 50                │                   │
│  │ }                                 │                   │
│  └──────────────────────────────────┘                   │
│         ↓                                                 │
│  Step 3: Reserve tickets                                 │
│  ┌────────────────────────────────────────┐             │
│  │ REST CALL                               │             │
│  │ POST http://event-service/api/         │             │
│  │      ticket-categories/vip-uuid/       │             │
│  │      reserve?quantity=2                 │             │
│  └────────────────────────────────────────┘             │
│         ↓                                                 │
│  Event Service decreases available count: 50 → 48       │
│                                                           │
│  Step 4: Create order                                    │
│  order = Order {                                         │
│    id: "order-uuid-123"                                  │
│    orderNumber: "ORD-20260218143025-A3F2B1"            │
│    userId: "john-uuid"                                   │
│    eventId: "concert-uuid"                               │
│    status: PENDING                                       │
│    subtotal: 500.00                                      │
│    total: 500.00                                         │
│    expiresAt: "2026-02-18 14:45:25"  (15 min)          │
│  }                                                        │
│                                                           │
│  Step 5: Save to database                                │
│  orderRepository.save(order)                             │
│                                                           │
│  Step 6: Publish Kafka event                             │
│  ┌────────────────────────────────────────┐             │
│  │ KAFKA PUBLISH                           │             │
│  │ Topic: "order.created"                  │             │
│  │ Event: OrderCreatedEvent {              │             │
│  │   orderId: "order-uuid-123"            │             │
│  │   userId: "john-uuid"                   │             │
│  │   totalAmount: 500.00                   │             │
│  │ }                                        │             │
│  └────────────────────────────────────────┘             │
│         │                                                 │
│  Step 7: Return response                                 │
│  return OrderDTO                                         │
│                                                           │
└───────────────────────┬───────────────────────────────────┘
                        │
                        │ Response to client:
                        │ {
                        │   "id": "order-uuid-123",
                        │   "orderNumber": "ORD-20260218143025-A3F2B1",
                        │   "status": "PENDING",
                        │   "total": 500.00,
                        │   "expiresAt": "2026-02-18T14:45:25"
                        │ }
                        ↓
┌──────────────────────────────────────────────────────────┐
│  MEANWHILE: Kafka consumers process the event           │
└──────────────────────────────────────────────────────────┘
         │
         ├──────────────────────────────────────┐
         │                                       │
         ↓                                       ↓
┌─────────────────────┐              ┌────────────────────┐
│ Notification Service│              │ Analytics Service  │
└─────────────────────┘              └────────────────────┘
         │                                       │
         │ Send email:                           │ Record sales:
         │ "Order ORD-...                        │ SalesAnalytics {
         │  created. Please                      │   orderId: ...
         │  complete payment                     │   amount: 500.00
         │  within 15 min"                       │   date: 2026-02-18
         │                                       │ }
         ↓                                       ↓
    Email sent                           Saved to analytics_db
```

**Key Points:**

1. **Synchronous calls** (REST) for getting price and reserving tickets - must succeed
2. **Asynchronous event** (Kafka) for notifications and analytics - fire-and-forget
3. **Transaction boundary**: Everything in Order Service happens in one database transaction
4. **15-minute timer**: Order expires if payment not completed
5. **Multiple consumers**: Both Notification and Analytics services process the same event

---

### 10.2 What Happens When Payment is Completed?

```
Mobile App → API Gateway → Payment Service
                              │
                              │ 1. Process payment via payment gateway
                              │    (Stripe, PayPal, etc.)
                              │
                              │ 2. Payment successful
                              │
                              │ 3. Publish Kafka event
                              ↓
                    ┌─────────────────────┐
                    │ KAFKA                │
                    │ Topic: payment.      │
                    │        completed     │
                    └──────────┬───────────┘
                              │
         ┌────────────────────┼────────────────────┐
         │                    │                    │
         ↓                    ↓                    ↓
┌─────────────────┐  ┌─────────────────┐  ┌──────────────────┐
│ Order Service   │  │ Ticket Service  │  │ Notification Svc │
└─────────────────┘  └─────────────────┘  └──────────────────┘
         │                    │                    │
         │                    │                    │
         │ Mark order as      │ Generate tickets   │ Send confirmation
         │ CONFIRMED          │ with QR codes      │ email with tickets
         │                    │                    │
         │ Update status:     │ Create 2 tickets:  │ Email: "Payment
         │ PENDING → CONFIRMED│ - Ticket 1         │ successful! Here
         │                    │ - Ticket 2         │ are your tickets"
         │                    │                    │
         │                    │ Publish event:     │
         │                    │ ticket.generated   │
         │                    │                    │
         ↓                    ↓                    ↓
    order_db updated      ticket_db updated    Email sent
```

**Event Chain:**

1. `order.created` → Notification Service sends "Order created" email
2. `payment.completed` → Order Service confirms order → Ticket Service generates tickets
3. `ticket.generated` → Notification Service sends tickets via email

This is **event-driven choreography** - services react to events independently.

---

### 10.3 What Happens When Order Expires?

```
Order Service has a scheduled job running every minute:

@Scheduled(fixedRate = 60000)  // Every 60 seconds
public void expireOrders() {
    // Find orders where expiresAt < now AND status = PENDING
    List<Order> expiredOrders = orderRepository.findExpiredOrders(LocalDateTime.now());
    
    for (Order order : expiredOrders) {
        // 1. Release reserved tickets
        for (OrderItem item : order.getItems()) {
            // Call Event Service to increase available count
            releaseTickets(item.getTicketCategoryId(), item.getQuantity());
        }
        
        // 2. Mark order as expired
        order.setStatus(OrderStatus.EXPIRED);
        orderRepository.save(order);
        
        // 3. Could publish event for notification
        // kafkaTemplate.send("order.expired", event);
    }
}
```

**Timeline:**

```
14:30:25 - Order created, expiresAt = 14:45:25
14:31:00 - Scheduler runs, order not expired yet
14:32:00 - Scheduler runs, order not expired yet
...
14:45:00 - Scheduler runs, order not expired yet
14:46:00 - Scheduler runs, order EXPIRED!
         - Tickets released (48 → 50)
         - Order status: PENDING → EXPIRED
```

---

## 11. Best Practices and Patterns

### 11.1 Transaction Management

**✅ DO:**

```java
@Transactional
public OrderDTO createOrder(CreateOrderRequest request) {
    // All database operations in one transaction
    Order order = new Order();
    order = orderRepository.save(order);
    
    for (ItemRequest item : request.getItems()) {
        OrderItem orderItem = new OrderItem();
        orderItemRepository.save(orderItem);
    }
    
    return orderMapper.toDTO(order);
}
```

**❌ DON'T:**

```java
// NO @Transactional - each save is separate transaction!
public OrderDTO createOrder(CreateOrderRequest request) {
    Order order = orderRepository.save(order);  // Transaction 1
    
    for (ItemRequest item : request.getItems()) {
        orderItemRepository.save(item);  // Transaction 2, 3, 4...
    }
    // If last save fails, order is saved but items are incomplete!
}
```

---

### 11.2 Error Handling

**Global Exception Handler:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        ApiError error = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex) {
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(error);
    }
}
```

---

### 11.3 Caching Strategy

**Cache frequently accessed data:**

```java
@Service
public class EventService {
    
    @Cacheable(value = "events", key = "#id")
    public EventDTO getEventById(UUID id) {
        // First call: fetches from database, stores in Redis
        // Subsequent calls: returns from Redis cache
        Event event = eventRepository.findById(id).orElseThrow();
        return eventMapper.toDTO(event);
    }
    
    @CacheEvict(value = "events", key = "#id")
    public EventDTO updateEvent(UUID id, UpdateEventRequest request) {
        // Removes from cache when updated
        Event event = eventRepository.findById(id).orElseThrow();
        // ... update logic ...
        return eventMapper.toDTO(event);
    }
}
```

---

### 11.4 API Versioning

```java
@RestController
@RequestMapping("/api/v1/events")  // Version in URL
public class EventControllerV1 {
    // Version 1 endpoints
}

@RestController
@RequestMapping("/api/v2/events")
public class EventControllerV2 {
    // Version 2 endpoints with breaking changes
}
```

---

## 12. Summary and Key Takeaways

### Architecture Overview

✅ **Tisqra uses 8 microservices**, each with single responsibility  
✅ **API Gateway** - Single entry point  
✅ **Eureka Discovery** - Service registry  
✅ **Kafka** - Asynchronous event-driven communication  
✅ **PostgreSQL** - Database per service  
✅ **Redis** - Caching layer  
✅ **Keycloak** - Authentication

### Communication Patterns

✅ **Synchronous (REST)** - When you need immediate response  
✅ **Asynchronous (Kafka)** - For notifications, analytics, non-critical operations

### Layered Architecture

✅ **Domain Layer** - Entities, repositories (database)  
✅ **Application Layer** - Services, DTOs, business logic  
✅ **Infrastructure Layer** - Controllers, configs, external integrations

### Key Annotations

✅ **@Entity** - Database table  
✅ **@Service** - Business logic component  
✅ **@RestController** - REST API endpoints  
✅ **@Transactional** - Database transaction  
✅ **@KafkaListener** - Listen to Kafka events  
✅ **@Cacheable** - Cache responses  
✅ **@PreAuthorize** - Role-based access control

---

## Next Steps for Learning

1. **Practice:** Clone the repository and run services locally
2. **Debug:** Set breakpoints and trace request flow
3. **Experiment:** Try modifying services and see what breaks
4. **Read:** Spring Boot documentation, Kafka documentation
5. **Build:** Create a new microservice for a new feature

**Remember:** Microservices are complex, but each service individually is simple. Focus on understanding one service at a time!

---

**Questions? Start with:**
- Run `docker-compose up` to start all services
- Access Swagger UI: `http://localhost:8080/swagger-ui.html`
- Check Eureka dashboard: `http://localhost:8761`
- Explore the code starting with `EventService.java`

**Happy Learning! 🚀**
