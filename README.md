# Customer Support Ticketing API

This project is a Spring Boot support ticket system for managing customer requests, agent assignments, comments, categories, and ticket lifecycle changes.

## Tech stack

- Java 21
- Spring Boot 4.0.8
- Spring Web
- Spring Security + JWT
- Spring Data JPA + Hibernate
- PostgreSQL (runtime)
- H2 (test profile)
- Maven Wrapper

## Roles

- CUSTOMER: can create and view their own tickets
- AGENT: can view and update tickets assigned to them, and change status
- ADMIN: can manage all records, including deletes

## Local run

Requirements:

- JDK 21
- PostgreSQL 16+ (or Docker Compose)
- Optional: Docker Desktop if you want the package to run with containers

Set environment variables before running:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/customer_support_db"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
$env:JWT_SECRET = "replace-with-a-long-random-secret"
$env:JWT_EXPIRATION_MS = "3600000"
```

Then start the app:

```powershell
./mvnw spring-boot:run
```

The app listens on port 8080 by default.

## Docker

This project includes a Dockerfile and Docker Compose setup:

```powershell
docker compose up --build
```

That starts:

- PostgreSQL on port 5432
- the Spring Boot API on port 8080

## Authentication

The API exposes a JWT login endpoint:

- `POST /api/auth/login`

Request body:

```json
{
  "email": "customer@example.com",
  "password": "secret"
}
```

Successful responses return a JWT in the `token` field.

## Main endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/api/users` | Public | Create a user account |
| POST | `/api/auth/login` | Public | Authenticate and receive a JWT |
| GET | `/api/categories` | Authenticated | List categories |
| POST | `/api/categories` | Authenticated | Create a category |
| GET | `/api/tickets` | Authenticated | List tickets visible to the current user |
| POST | `/api/tickets` | Authenticated | Create a ticket as the current customer |
| GET | `/api/tickets/{id}` | Authenticated | Get one ticket if access is allowed |
| PUT | `/api/tickets/{id}` | Authenticated | Update a ticket the user owns or manages |
| PUT | `/api/tickets/{id}/assign` | AGENT/ADMIN | Assign an agent to a ticket |
| PUT | `/api/tickets/{id}/status` | AGENT/ADMIN | Move the ticket through its allowed state machine |
| DELETE | `/api/tickets/{id}` | ADMIN | Delete a ticket |
| GET | `/api/tickets/{ticketId}/comments` | Authenticated | List comments |
| POST | `/api/tickets/{ticketId}/comments` | Authenticated | Add a comment |

## State machine

Ticket transitions are enforced in the service layer. Supported flow:

- OPEN -> IN_PROGRESS
- OPEN -> CANCELED
- IN_PROGRESS -> RESOLVED
- IN_PROGRESS -> CANCELED
- RESOLVED -> CLOSED

Any other transition is rejected with a custom exception and a 409 response.

## Testing

Run the test suite with:

```powershell
./mvnw test
```

The project uses H2 in the test profile so tests do not require a running PostgreSQL instance.
