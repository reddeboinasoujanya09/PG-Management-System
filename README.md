# PG-Management-System
Microservices-based PG Management System that handles tenant onboarding, room allocation, rent tracking, service requests, and notifications. Built using Spring Boot, NestJS, and Node.js with an event-driven architecture for scalability, reliability, and maintainability.
## Run Services Locally

### Prerequisites
- Node.js + npm (for NestJS/Node services)
- Java 17+ (for Spring Boot services)
- Use separate terminal tabs/windows for each service.

---

### Admin Config (`admin-config`)
```bash
cd "<root>/project/admin-config"
npm install
PORT=3003 npm run start:dev
```

### API Gateway (`apiGateway`)
```bash
cd "<root>/project/apiGateway"
./mvnw spring-boot:run
```


### Billing Service (billingService)
```bash
cd "<root>/project/billingService"
./mvnw spring-boot:run
```


### Booking Service (bookingService)
```bash
cd "<root>/project/bookingService"
./mvnw spring-boot:run
```


### Payment Service (paymentService)
```bash
cd "<root>/project/paymentService"
./mvnw spring-boot:run
```


### Tenant Service (`tenantService`)
```bash
cd "<root>/project/tenantService"
./mvnw spring-boot:run
```
> Runs on port **8081**. Requires PostgreSQL (`pg_management_db`) to be running.
> On first run, Liquibase automatically applies all pending schema migrations.
> To override DB credentials: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars.

---

###  Room Service (room-service)
```bash
cd "<root>/project/room-service"
npm install
PORT=3001 npm run start:dev
```


###  Audit Service (audit-service)
```bash
cd "<root>/project/audit-service"
npm install
npm install --save-dev nodemon
npm run start
# or
npm run dev

```

###  Notification Service (notification-service)
```bash
cd "<root>/project/notification-service"
npm install
npm install --save-dev nodemon
npm run start
# or
npm run dev

```