# fixa-ordering — Home Services Order Registration

A self-contained Spring Boot backend service for registering customer service orders
(boiler repair, air conditioner service, ...). It validates the service category, the
customer, address ownership and region availability, then creates an order with a
unique order code, an initial `FINAL_ORDER` status and a full status history/audit trail.

## Technology

- **Java 21**
- **Spring Boot 4.1.1** (Spring Framework 7, Jakarta EE, Jackson 3, Hibernate ORM 7)
- Maven (wrapper included)
- Spring Web MVC, Spring Data JPA, H2 (in-memory), Jakarta Bean Validation
- JUnit 5 + AssertJ + MockMvc for testing

> Note: the assessment text mentions "Spring Boot 3.x", but the existing (pre-initialized)
> project already targets **Spring Boot 4.1.1**. Per the assessment constraints
> ("do not change the existing versions"), the existing version was preserved and all
> Boot 4 APIs (modular starters, moved test annotations, etc.) are used accordingly.

## Architecture

A clean layered architecture. Flow: Controller → Service (use case) → Domain → Repository → H2.

```
com.example.fixaordering
├── controller        OrderController (thin, delegates to services)
├── dto               Immutable request/response records (never entities)
├── mapper            CustomerMapper, AddressMapper, RegionMapper, ServiceCategoryMapper,
│                     OrderMapper, OrderStatusHistoryMapper (static, focused mappers)
├── service           OrderService (use case orchestration), OrderCodeGenerator +
│                     OrderSequenceAllocator (concurrency-safe code generation),
│                     validation/ OrderValidationStep chain (category -> customer ->
│                     address ownership -> region availability, in that order)
├── domain            BaseEntity, Customer, Address, Region, ServiceCategory, Order,
│                     OrderStatusHistory, OrderSequence, OrderStatus (enum with the
│                     transition policy), OrderStateMachine (only component allowed to
│                     change status), OrderStatusHistoryRecorder (+ repository-backed impl)
├── repository        Spring Data JPA repositories (H2)
├── exception         BusinessException hierarchy + central GlobalExceptionHandler
└── config            TimeConfig (Clock bean), DevDataInitializer (@Profile("dev") seed data)
```

Key design rules applied:

- Controllers are thin; all business rules live in services/domain.
- Business validation (disabled category, wrong owner, disabled region) is clearly
  separated from input validation (`@NotNull`, `@FutureOrPresent` on the DTO).
- All relationships are `@ManyToOne(fetch = LAZY)`; `spring.jpa.open-in-view=false`
  forces transactional DTO mapping (no lazy-loading leaks, no infinite JSON).
- `Order` exposes no public status setter; the only status mutation path is
  `OrderStateMachine` → `Order.applyTransition(...)` (package-private, validated first).
- The `orders.order_code` column has a DB-level unique constraint as the final safety layer.

## How to run

```bash
./mvnw spring-boot:run                      # plain start (empty database)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # start with seed data
```

The application runs against an in-memory H2 database — no Docker, PostgreSQL, Redis,
RabbitMQ, Kafka or any external service is required. The H2 web console is available at
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:testdb` is shown in the log).

With the `dev` profile the seeder (`DevDataInitializer`) creates: two regions (Tehran
enabled, Karaj disabled), three categories (Boiler Repair and Air Conditioner Service
enabled, Window Cleaning disabled) and two customers each with an address. The ids are
logged on startup (`customerId`, `addressId`, category/region ids) for use in sample requests.

## How to run tests

```bash
./mvnw test          # or: ./mvnw clean test
./mvnw clean package # build jar + run all tests
```

Important: the build must run on **JDK 21** (`JAVA_HOME` pointing to a JDK 21 installation).

All tests use the real embedded H2 database — repository implementations are never mocked.
The only mocked seam is the `OrderStatusHistoryRecorder` bean in
`OrderTransactionRollbackTest`, which simulates a persistence failure after the order
insert to prove transaction rollback (scenario 9).

## API endpoints

### `POST /api/orders`

Registers a new order. Input validation via Bean Validation; business rules are applied
in the required order (category -> customer -> address/owner -> region).

Request:

```json
{
  "serviceCategoryId": 1,
  "customerId": 1,
  "addressId": 1,
  "requestedDate": "2026-09-20"
}
```

`201 Created`:

```json
{
  "id": 1,
  "orderCode": "260912-00042"
}
```

Errors: `404` (`SERVICE_CATEGORY_NOT_FOUND`, `CUSTOMER_NOT_FOUND`, `ADDRESS_NOT_FOUND`,
`ORDER_NOT_FOUND`), `422` (`SERVICE_CATEGORY_DISABLED`, `ADDRESS_NOT_OWNED_BY_CUSTOMER`,
`REGION_DISABLED`), `400` (field-level validation errors).

### `GET /api/orders/{id}`

Returns the order details: `id`, `orderCode`, `requestedDate`, `status`, nested
`customer`, `serviceCategory`, `address` (with its region) and the full `statusHistory`.

`200 OK`, or `404` with `ORDER_NOT_FOUND`.

### `POST /api/orders/{id}/status` (extension)

Applies a status transition through the centralized state machine:

```json
{ "newStatus": "TECHNICIAN_ACCEPTED" }
```

Returns `200 OK` with the updated order (including the appended history entry), `409
CONFLICT` with `INVALID_STATUS_TRANSITION` for disallowed transitions, or `404` for an
unknown order.

### `GET /api/customers`, `GET /api/service-categories`, `GET /api/addresses`

Read-only lookup lists used when composing an order from the client:

- `GET /api/customers` → `[{ "id": 1, "firstName": "Ali", "lastName": "Ahmadi", "mobile": "09123456789", "nationalCode": "0023456789" }]`
- `GET /api/service-categories` → `[{ "id": 1, "name": "Boiler Repair" }]`
- `GET /api/addresses` → `[{ "id": 1, "name": "Home", "details": "Tehran, Valiasr St, No 5", "region": { "id": 1, "name": "Tehran" } }]`

All return `200 OK` with a JSON array.

## Sample requests (dev seed data)

```bash
# ids as logged by DevDataInitializer, e.g. category 1 = Boiler Repair
curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -d \
  '{"serviceCategoryId": 1, "customerId": 1, "addressId": 1, "requestedDate": "2026-10-01"}'

curl http://localhost:8080/api/orders/1

curl -X POST http://localhost:8080/api/orders/1/status -H "Content-Type: application/json" \
  -d '{"newStatus": "TECHNICIAN_ACCEPTED"}'
```

Error response shape:

```json
{ "code": "SERVICE_CATEGORY_DISABLED", "message": "The selected service category (id 3) is disabled." }
```

Validation errors return a flat field -> message map:

```json
{ "serviceCategoryId": "must not be null", "requestedDate": "must be a future or present date" }
```

## Business rules (order creation, applied in this order)

1. **Service category** — must exist, must be enabled.
2. **Customer** — must exist.
3. **Address** — must exist, must belong to the requested customer.
4. **Region** — the region of the address must exist and be enabled.
5. **Order code** — unique, DATE-SEQUENCE style.
6. **Initial status** — always `FINAL_ORDER`, with an initial audit record
   (previousStatus = null).
7. **Atomicity** — order + its initial history record are created in one
   `@Transactional` use case; any failure rolls back both (tested).

Every rule failure produces a typed `BusinessException` with a stable machine-readable
`code` and a clear message, mapped to HTTP status codes by `GlobalExceptionHandler`:
`404` for referenced resources that do not exist, `422` for business rule violations,
`409` for invalid state transitions, `400` for input validation.

## State transition rules

The allowed transitions are defined centrally in the `OrderStatus` enum
(`ALLOWED_TRANSITIONS` map + `canTransitionTo`):

| From                | Allowed to                                  |
|---------------------|---------------------------------------------|
| FINAL_ORDER         | TECHNICIAN_ACCEPTED, CLIENT_CANCELLED, ADMIN_CANCELLED |
| TECHNICIAN_ACCEPTED | ORDER_IN_PROGRESS                           |
| ORDER_IN_PROGRESS   | ORDER_COMPLETED                             |
| ORDER_COMPLETED     | (terminal)                                  |
| CLIENT_CANCELLED    | (terminal)                                  |
| ADMIN_CANCELLED     | (terminal)                                  |

Invalid transitions raise `InvalidOrderStatusTransitionException` (`409
INVALID_STATUS_TRANSITION`). Every successful transition (and the initial
`FINAL_ORDER` selection) writes an `OrderStatusHistory` record (previousStatus,
newStatus, changedAt).

## Order code generation

Format: `yyMMdd-#####` (e.g. `260912-00042`) — DATE part from the order creation day,
SEQUENCE part a per-day counter starting at 1, zero-padded to 5 digits.

Concurrency safety: the counter lives in an `order_sequences` table row keyed by the
day; `OrderSequenceAllocator` runs in its own `REQUIRES_NEW` transaction and reads the
row with a `SELECT ... FOR UPDATE` pessimistic lock before incrementing — concurrent
creators are serialized and each receives a distinct value. The rare race where two
first-of-the-day transactions both try to create the daily row is handled by a bounded
retry in `OrderCodeGenerator` (the failed transaction rolls back completely, the retry
then locks the now-existing row). The DB unique constraint on `orders.order_code`
backstops the guarantee. Sequence gaps after rollbacks are acceptable and harmless.

## Design decisions & assumptions

- **No authentication/authorization** — the assessment explicitly excludes
  Keycloak/OAuth2; all endpoints are open.
- **Spring Boot 4.1.1 kept** despite the "3.x" wording in the assessment (existing
  project constraint wins; documented above).
- **`POST /api/orders/{id}/status`** is a small deliberate extension beyond the two
  required endpoints, needed to exercise the state machine end to end (and required
  by scenarios 7/8 at the HTTP level).
- **Mapper classes are static utility classes** — states of none, no DI ceremony;
  dedicated per-aggregate mappers keep mapping out of services and controllers.
- **`OrderStatusHistoryRecorder` interface** exists to make status-history persistence
  an injectable seam (used by the transaction-rollback test with a `@Primary`
  test bean; the production implementation simply saves the entity).
- **`BaseEntity`** carries only the generated `id`; audit timestamps were not required
  and were omitted to keep entities minimal.
- **Uniqueness constraints** are intentionally limited to `order_code` (the only one
  the assessment requires); category/region names and customer mobile/national code
  are plain columns to keep fixture data simple.
- **Order code is not derived from the PK id**; it uses its own per-day sequence table
  so the code does not leak internal ids and gaps do not break readability.
- H2 is used for both production runs and tests; the schema is generated by Hibernate
  (`ddl-auto=update`) — below the required self-contained/local setup.