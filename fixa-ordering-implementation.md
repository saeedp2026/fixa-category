You are a senior Java/Spring Boot backend engineer.

Your task is to implement the following technical assessment completely in the EXISTING Spring Boot project.

IMPORTANT — EXISTING PROJECT:
- The Spring Boot project has ALREADY been initialized.
- Java, Spring Boot, Maven, and the required base configuration are ALREADY configured.
- The project is ready for implementation.
- DO NOT create a new Spring Boot project.
- DO NOT recreate the project from start.spring.io.
- DO NOT replace or regenerate the existing pom.xml unless a dependency explicitly required by the task is genuinely missing.
- DO NOT change the existing Java/Spring Boot versions unless absolutely necessary.
- DO NOT scaffold a new project.
- DO NOT delete or replace the existing project structure.
- Work directly inside the existing repository/workspace.
- First inspect the existing project structure, pom.xml, configuration, and source code.
- Preserve the existing project setup and conventions wherever possible.
- Only implement the functionality required by this assessment on top of the existing project.

The project is already initialized and ready. Your job is to IMPLEMENT THE TASK, not to initialize the project.

Before making changes:
1. Inspect the existing project.
2. Understand its current structure and configuration.
3. Identify what is already available.
4. Identify what needs to be implemented for this assessment.
5. Create a concise implementation plan.
6. Then implement the complete solution.

Do NOT ask unnecessary questions. Make reasonable engineering decisions when details are unspecified.

After implementation:
- Run the full test suite.
- Fix all failures.
- Review the implementation against every requirement.
- Do not claim tests passed unless you actually ran them.

==================================================
TECHNICAL ASSESSMENT
==================================================

Title:
Home Services Order Registration

Goal:
Implement a Spring Boot backend service for registering customer service orders.

The system allows a customer to create an order for a service category such as:
- Boiler repair
- Air conditioner service

The system must validate:
- Service category
- Customer
- Address ownership
- Region availability

Then create an order with:
- Unique order code
- Requested date
- Initial status
- Status history/audit

The assessment evaluates:
1. Spring Boot and JPA
2. Domain modeling
3. Business rules
4. State machine
5. Testing
6. Clean code and SOLID principles

==================================================
1. EXISTING PROJECT / TECHNOLOGY CONSTRAINTS
   ==================================================

The project has already been initialized with the required Spring Boot / Java / Maven setup.

First inspect the existing:

- pom.xml
- src/main
- src/test
- application configuration
- existing dependencies
- existing packages/classes

Reuse the existing setup.

Expected technology stack:

- Java 21
- Spring Boot 3.x
- Maven
- Spring Web
- Spring Data JPA
- H2
- Jakarta Bean Validation
- spring-boot-starter-test
- AssertJ

IMPORTANT:

Do not unnecessarily modify dependencies.

If one of the required dependencies is already present, reuse it.

If a genuinely required dependency is missing, add only the minimal required dependency.

Do NOT introduce unnecessary libraries.

Do NOT introduce:

- Company-specific/internal libraries
- Private Maven repositories
- Company-specific shared classes
- Keycloak
- OAuth2
- External authentication services
- PostgreSQL
- Redis
- RabbitMQ
- Kafka
- External infrastructure dependencies

The application must remain completely self-contained.

Everything required by the task must be implemented within the existing project.

==================================================
2. EXISTING PROJECT MUST BE PRESERVED
   ==================================================

Do NOT:

- Reinitialize the Maven project
- Generate a new Spring Initializr project
- Replace pom.xml wholesale
- Delete existing source code
- Replace existing configuration unnecessarily
- Rename the project unnecessarily
- Change package names unnecessarily
- Introduce a completely new architecture if the existing architecture can support the task

Instead:

- Extend the existing codebase.
- Follow existing conventions when they are reasonable.
- Reuse existing utilities/configuration when appropriate.
- Add only the classes required for the task.

If the project is mostly empty, add the required implementation to its existing structure rather than creating a separate project.

==================================================
3. DOMAIN MODEL
   ==================================================

Implement at least the following domain entities.

--------------------------------------------------
ServiceCategory
--------------------------------------------------

Represents a service category.

Required concepts:
- id
- name
- enabled

Relationship:

One ServiceCategory can have many Orders.

Order * -> 1 ServiceCategory

--------------------------------------------------
Customer
--------------------------------------------------

Represents the customer.

Required concepts:
- id
- firstName
- lastName
- mobile
- nationalCode

Relationships:

One Customer can have many Orders.

One Customer can have many Addresses.

Customer 1 -> * Order

Customer 1 -> * Address

--------------------------------------------------
Address
--------------------------------------------------

Represents a customer's address.

Required concepts:
- id
- name
- details

Relationships:

Each Address belongs to one Customer.

Each Address belongs to one Region.

Customer 1 -> * Address

Region 1 -> * Address

--------------------------------------------------
Region
--------------------------------------------------

Represents a city/region.

Required concepts:
- id
- name
- enabled

Relationship:

One Region can have many Addresses.

--------------------------------------------------
Order
--------------------------------------------------

Represents a customer's service order.

Required concepts:
- id
- orderCode
- requestedDate
- customer
- serviceCategory
- address
- status

Relationships:

Order -> Customer
Order -> ServiceCategory
Order -> Address
Order -> OrderStatus

--------------------------------------------------
OrderStatus
--------------------------------------------------

Represent order status.

Use an enum for the status values.

Required minimum statuses:

FINAL_ORDER
TECHNICIAN_ACCEPTED
ORDER_IN_PROGRESS
ORDER_COMPLETED
CLIENT_CANCELLED
ADMIN_CANCELLED

Also implement a mechanism/table/service for validating allowed status transitions.

==================================================
4. BUSINESS RULES
   ==================================================

When creating an order, validate these rules IN THIS ORDER:

--------------------------------------------------
Rule 1 - Service Category
--------------------------------------------------

The requested ServiceCategory must:

- Exist
- Be enabled

If it does not exist or is disabled:
- Reject the order
- Return a clear business error

--------------------------------------------------
Rule 2 - Customer
--------------------------------------------------

The Customer must exist.

If not:
- Reject the request
- Return a clear business error

--------------------------------------------------
Rule 3 - Address and Region
--------------------------------------------------

The Address must:

- Exist
- Belong to the requested customer

The Region associated with the Address must:

- Exist
- Be enabled

If the address belongs to another customer:
- Reject the request

If the region is disabled:
- Reject the request

Return meaningful business errors.

--------------------------------------------------
Rule 4 - Order Code
--------------------------------------------------

Every order must receive a unique orderCode.

You may use a design such as:

DATE-SEQUENCE

Example:

60912-00042

The exact implementation is your engineering decision, but uniqueness must be guaranteed correctly.

Do NOT rely only on random generation without considering uniqueness.

The database should enforce uniqueness where appropriate.

--------------------------------------------------
Rule 5 - Initial Status
--------------------------------------------------

Every newly created order must have:

FINAL_ORDER

as its initial status.

Direct status mutation must NOT be allowed from arbitrary code.

BAD:

order.setStatus(...)

from application/business services.

Instead, status changes must go through one centralized domain/service mechanism.

--------------------------------------------------
Rule 6 - Status Transition
--------------------------------------------------

All status changes must be validated against the allowed transition rules.

Minimum allowed transitions:

FINAL_ORDER
-> TECHNICIAN_ACCEPTED

TECHNICIAN_ACCEPTED
-> ORDER_IN_PROGRESS

ORDER_IN_PROGRESS
-> ORDER_COMPLETED

FINAL_ORDER
-> CLIENT_CANCELLED

FINAL_ORDER
-> ADMIN_CANCELLED

Invalid transitions must:

- Be rejected
- Produce a specific business exception
- Have a clear error message

Example invalid transition:

ORDER_COMPLETED -> FINAL_ORDER

must fail.

--------------------------------------------------
Rule 7 - Status History
--------------------------------------------------

Every status change must create an audit/history record.

The initial FINAL_ORDER status must also have a history record.

The history should contain useful information such as:

- id
- order
- previousStatus
- newStatus
- changedAt

Additional fields may be added if useful.

--------------------------------------------------
Rule 8 - Transactionality
--------------------------------------------------

Order creation and the initial status history must happen in ONE atomic transaction.

If one fails:

- The order must not be persisted
- The status history must not be persisted

Use proper Spring transaction management.

==================================================
5. API
   ==================================================

--------------------------------------------------
POST /api/orders
--------------------------------------------------

Create an order.

Request:

{
"serviceCategoryId": 1,
"customerId": 2,
"addressId": 3,
"requestedDate": "2026-09-20"
}

Use:

@Valid

at the controller boundary.

Successful response:

HTTP 201 Created

Example:

{
"id": 1,
"orderCode": "60912-00042"
}

The exact response structure can be improved if needed, but the required information must be present.

--------------------------------------------------
GET /api/orders/{id}
--------------------------------------------------

Return the order details.

The response should contain useful information including:

- id
- orderCode
- requestedDate
- customer
- serviceCategory
- address
- status

Avoid exposing JPA entities directly from controllers.

Use DTOs.

==================================================
6. VALIDATION
   ==================================================

Use Jakarta Bean Validation.

Examples:

- @NotNull
- @NotBlank
- @FutureOrPresent where appropriate
- @Size
- etc.

Do not put all business validation into Bean Validation.

Separate:

INPUT VALIDATION

from:

BUSINESS VALIDATION

For example:

@NotNull
for serviceCategoryId is input validation.

"Service category must be enabled"
is business validation.

==================================================
7. ERROR HANDLING
   ==================================================

Create centralized exception handling using:

@RestControllerAdvice

Handle at least:

- Validation errors
- Resource not found
- Business rule violations
- Invalid state transitions
- Unexpected errors

Validation errors should provide field-level information.

For example:

{
"serviceCategoryId": "must not be null",
"requestedDate": "must be a future or present date"
}

Business errors should contain a clear message.

Use appropriate HTTP status codes.

Possible structure:

{
"code": "SERVICE_CATEGORY_DISABLED",
"message": "The selected service category is disabled."
}

The exact error DTO structure is your engineering decision as long as it is consistent and clear.

==================================================
8. ARCHITECTURE
   ==================================================

Use a clean layered architecture while respecting the existing project's structure.

A reasonable structure would be:

controller
dto
service
domain/entity
repository
mapper
exception
config

You may improve the package structure if it makes the code cleaner, but do not unnecessarily restructure existing code.

Expected flow:

Controller
↓
Application/Service layer
↓
Domain/business logic
↓
Repository
↓
Database

Keep controllers thin.

Do not put business rules inside controllers.

Do not put business workflows inside repositories.

Avoid giant service classes.

Use focused classes with single responsibilities.

Follow SOLID principles.

==================================================
9. ENTITY <-> DTO MAPPING
   ==================================================

Entity-to-DTO and DTO-to-entity mapping must be handled through dedicated Mapper classes.

Do NOT do repetitive manual field-by-field mapping directly inside services/controllers.

For example:

OrderMapper

CustomerMapper

AddressMapper

etc.

You do not need MapStruct.

A clean manual Mapper implementation is acceptable.

If the existing project already contains a mapping mechanism, evaluate whether it can be reused before creating another one.

==================================================
10. JPA DESIGN
    ==================================================

Design the JPA relationships correctly.

Consider:

@ManyToOne
@OneToMany
@OneToOne

where appropriate.

Important:

- Avoid accidental eager loading.
- Prefer LAZY relationships where appropriate.
- Avoid infinite JSON serialization.
- Do not expose entities directly from REST controllers.
- Use proper foreign keys.
- Add unique constraint/index for orderCode.
- Use appropriate cascade configuration.
- Do not blindly use CascadeType.ALL everywhere.

Use a base entity if useful, for example:

BaseEntity
- id
- createdAt
- updatedAt

If the existing project already has a suitable base entity, reuse it instead of creating another one.

==================================================
11. STATE MACHINE DESIGN
    ==================================================

The state transition mechanism is an important part of this assessment.

Do NOT scatter transition logic across multiple services.

Create one centralized component responsible for transitions.

For example:

OrderStatusTransitionService

or

OrderStateMachine

The design must make invalid transitions difficult/impossible to perform accidentally.

Example:

changeStatus(order, newStatus)

must:

1. Read current status
2. Check whether transition is allowed
3. Reject invalid transitions
4. Update status
5. Persist status history

Direct status manipulation from unrelated services should not be used.

==================================================
12. TESTING
    ==================================================

Testing is a major part of the assessment.

Use:

- JUnit 5
- AssertJ
- Spring Boot Test
- MockMvc where appropriate
- H2

IMPORTANT:

Repository implementations must NOT be mocked.

Integration tests involving persistence must use a real H2 database.

Mock only external boundaries when necessary.

You should create a meaningful test suite covering:

--------------------------------------------------
Scenario 1
--------------------------------------------------

Valid order registration.

Expected:

201 Created

and:

- order persisted
- unique orderCode generated
- status = FINAL_ORDER
- initial status history persisted

--------------------------------------------------
Scenario 2
--------------------------------------------------

Disabled service category.

Expected:

Business error.

Order must not be created.

--------------------------------------------------
Scenario 3
--------------------------------------------------

Disabled region.

Expected:

Business error.

Order must not be created.

--------------------------------------------------
Scenario 4
--------------------------------------------------

Address belongs to another customer.

Expected:

Business error.

Order must not be created.

--------------------------------------------------
Scenario 5
--------------------------------------------------

Customer does not exist.

Expected:

Business error.

Order must not be created.

--------------------------------------------------
Scenario 6
--------------------------------------------------

Service category does not exist.

Expected:

Business error.

--------------------------------------------------
Scenario 7
--------------------------------------------------

Valid status transition.

Example:

FINAL_ORDER
→ TECHNICIAN_ACCEPTED

Expected:
- successful transition
- status updated
- history created

--------------------------------------------------
Scenario 8
--------------------------------------------------

Invalid status transition.

Example:

FINAL_ORDER
← ORDER_COMPLETED

Expected:
- business exception
- status unchanged
- appropriate error response

--------------------------------------------------
Scenario 9
--------------------------------------------------

Transaction rollback.

Force an error after order creation but before completion of status history creation.

Verify that:

- order is not persisted
- history is not persisted

--------------------------------------------------
Scenario 10
--------------------------------------------------

GET /api/orders/{id}

Verify that the correct DTO is returned.

--------------------------------------------------
Scenario 11
--------------------------------------------------

Invalid request body.

For example:

{
"serviceCategoryId": null,
"customerId": null,
"addressId": null
}

Expected:

400 Bad Request

with field-level validation errors.

==================================================
13. CODE QUALITY
    ==================================================

Follow these rules:

- Java 21
- Modern Spring Boot 3.x APIs
- No deprecated APIs
- No unnecessary dependencies
- No unnecessary abstractions
- No giant classes
- No duplicated business logic
- Meaningful names
- Small methods
- Clear responsibilities
- Constructor injection
- Immutable DTOs where appropriate
- Proper exception hierarchy
- Proper transaction boundaries
- Proper database constraints

All code comments MUST be written in English.

Do not add unnecessary comments.

Prefer self-explanatory code.

==================================================
14. DATABASE
    ==================================================

Use the existing H2 configuration if already present.

If H2 is already configured:
- Reuse it.
- Do not replace it unnecessarily.

If H2 configuration is missing:
- Add the minimal configuration required by the assessment.

The project must run locally without requiring:

- Docker
- PostgreSQL
- Redis
- RabbitMQ
- Kafka
- External services

If seed/sample data is useful, keep it minimal and clearly separated from production logic.

==================================================
15. API DESIGN QUALITY
    ==================================================

Use REST conventions.

For creation:

POST /api/orders

Return:

201 Created

For retrieval:

GET /api/orders/{id}

Return:

200 OK

For missing resources:

404 Not Found

For invalid request:

400 Bad Request

For business rule violations:

Use a suitable 4xx response consistently.

Keep endpoint naming consistent.

==================================================
16. ORDER CODE CONCURRENCY
    ==================================================

Pay special attention to orderCode uniqueness.

The implementation must remain correct under concurrent order creation.

Do not assume that:

read max(sequence) + 1

is safe.

If a sequence-based strategy is used, design it safely for concurrent transactions.

A simpler safe strategy is acceptable if it guarantees uniqueness and remains understandable.

Add a database-level unique constraint as the final safety layer.

==================================================
17. TRANSACTION BOUNDARIES
    ==================================================

The order creation use case should be transactional.

Conceptually:

@Transactional
createOrder(...)

must perform:

1. Validate service category
2. Validate customer
3. Validate address
4. Validate region
5. Generate order code
6. Create order with FINAL_ORDER
7. Persist order
8. Create initial status history
9. Persist history
10. Commit

If any required step fails, rollback the complete operation.

==================================================
18. DELIVERABLE
    ==================================================

The final implementation should contain, as required:

- Domain entities
- Repositories
- DTOs
- Mappers
- Services
- State machine
- Exceptions
- Controller
- REST API
- Validation
- H2 integration
- Tests
- README.md

Do NOT recreate project-level files unnecessarily.

README.md should explain:

1. Project overview
2. Architecture
3. How to run
4. How to run tests
5. API endpoints
6. Sample requests
7. Business rules
8. State transition rules
9. Design decisions
10. Any assumptions

==================================================
19. ACCEPTANCE CHECKLIST
    ==================================================

Before considering the task complete, verify ALL of the following:

[ ] Existing project structure was preserved
[ ] Existing Spring Boot configuration was reused
[ ] No new Spring Boot project was created
[ ] pom.xml was not unnecessarily replaced
[ ] Java/Spring versions were not unnecessarily changed
[ ] Project builds successfully with Maven
[ ] Application starts successfully
[ ] Java 21 is used
[ ] Spring Boot 3.x is used
[ ] H2 is used
[ ] No private/company-specific dependency exists
[ ] ServiceCategory implemented
[ ] Customer implemented
[ ] Address implemented
[ ] Region implemented
[ ] Order implemented
[ ] OrderStatus implemented
[ ] Status history implemented
[ ] Correct JPA relationships implemented
[ ] POST /api/orders implemented
[ ] GET /api/orders/{id} implemented
[ ] @Valid used
[ ] Central @RestControllerAdvice implemented
[ ] DTOs used
[ ] Dedicated Mapper implemented
[ ] Service category validation implemented
[ ] Customer validation implemented
[ ] Address ownership validation implemented
[ ] Region enabled validation implemented
[ ] Unique orderCode implemented
[ ] Database uniqueness constraint exists
[ ] Initial status is FINAL_ORDER
[ ] Direct status mutation avoided
[ ] Centralized state transition mechanism implemented
[ ] Invalid transitions rejected
[ ] Status history created for transitions
[ ] Initial status history created
[ ] Order + initial history are atomic
[ ] Integration tests use real H2
[ ] Repository mocking is avoided
[ ] Valid order test exists
[ ] Disabled service category test exists
[ ] Disabled region test exists
[ ] Wrong customer/address test exists
[ ] Invalid state transition test exists
[ ] Validation error test exists
[ ] GET endpoint test exists
[ ] Transaction rollback is tested
[ ] All tests pass
[ ] README exists
[ ] No deprecated APIs are used
[ ] Code comments are in English
[ ] Code follows SOLID principles
[ ] No unnecessary over-engineering

==================================================
20. EXECUTION PROCESS
    ==================================================

Follow this workflow.

PHASE 1 — INSPECT

First inspect the EXISTING repository.

Specifically inspect:

- pom.xml
- src/main/java
- src/main/resources
- src/test/java
- application.properties / application.yml
- existing entities
- existing repositories
- existing services
- existing controllers
- existing exception handling
- existing configuration

Do NOT modify anything during the initial inspection.

Determine what already exists and what needs to be added.

PHASE 2 — PLAN

Create a concise implementation plan based on the existing project.

The plan should identify:

- Files/classes to add
- Existing files that need modification
- Entity relationships
- DTO structure
- Exception hierarchy
- Mapper strategy
- Order code generation strategy
- State machine design
- Transaction boundary
- Test strategy

Avoid unnecessary changes to existing code.

PHASE 3 — IMPLEMENT

Implement the complete solution inside the existing project.

Do not stop after scaffolding.

Implement production-quality code.

Reuse existing components when appropriate.

PHASE 4 — TEST

Run:

mvn test

Fix every failure.

Then run:

mvn clean test

If possible also verify:

mvn clean package

Do not claim success unless the commands actually succeed.

PHASE 5 — REVIEW

Perform a final code review against the entire specification.

Look specifically for:

- Missing requirements
- Incorrect JPA relationships
- Transaction problems
- Race conditions
- Incorrect state transitions
- Direct status mutation
- Missing validation
- Missing tests
- Bad exception handling
- N+1 query risks
- DTO/entity leakage
- Over-engineering
- SOLID violations
- Unnecessary changes to the existing project

Fix any issues found.

PHASE 6 — FINAL REPORT

At the end, provide:

1. Summary of implementation
2. Existing project components reused
3. New files/classes added
4. Files modified
5. Architecture overview
6. Important design decisions
7. State machine implementation
8. Order code generation approach
9. Transaction strategy
10. Test coverage
11. Maven test result
12. Any assumptions made

IMPORTANT:

The goal is NOT to create a new project.

The goal is to take the ALREADY INITIALIZED Spring Boot project and implement this technical assessment cleanly on top of it.

Use your judgment as a senior backend engineer.

Where the specification is ambiguous:

- Choose the simplest production-quality solution.
- Document important assumptions in README.md.
- Do not add unnecessary technologies.
- Do not create abstractions merely for the sake of abstraction.
- Prioritize correctness, readability, testability, and maintainability.

The final result should look like a realistic senior-level Spring Boot implementation rather than a minimal coding exercise.