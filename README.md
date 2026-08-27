# Customer Support Ticketing

A Spring Boot REST API for managing customer support users, categories, tickets, comments, assignments, and ticket status history.

## Tech Stack

- Java 21
- Spring Boot 4.0.8
- Spring Web MVC
- Spring Data JPA with Hibernate
- Spring Boot Validation
- PostgreSQL JDBC driver (version is managed by the Spring Boot parent; no explicit version is declared in `pom.xml`)
- Maven, using the included Maven Wrapper (`mvnw` and `mvnw.cmd`)
- Lombok

## Project Structure

```text
src/main/java/com/aldaleel/ticketing/
├── controller/   REST endpoints for users, categories, tickets, and ticket comments
├── dto/
│   ├── request/  Validated request records for creating, updating, assigning, and changing ticket status
│   └── response/ Response records for users, categories, comments, tickets, and status history
├── entity/       JPA entities for users, categories, tickets, comments, and ticket status history
├── exception/    Global REST exception handling and error response creation
├── mapper/       Conversion of entities into response DTOs, including nested ticket data
├── repository/   Spring Data JPA repositories and ticket/comment lookup methods
└── service/      Transactional business operations and ticket status rules
```

## Prerequisites

- JDK 21
- PostgreSQL
- Maven, or use the included Maven Wrapper

## How to Run Locally

Create the PostgreSQL database configured below, set the required connection values, then run:

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

The project does not define a custom `server.port`; Spring Boot therefore uses its default port, `8080`.

## Required Configuration

Database connection settings are in `src/main/resources/application.yaml` (the project uses `.yaml`, although this is commonly also called `application.yml`). Set these values for your local PostgreSQL instance:

```yaml
spring:
  datasource:
    url: <POSTGRESQL_JDBC_URL>
    username: <POSTGRESQL_USERNAME>
    password: <POSTGRESQL_PASSWORD>
```

The current JPA configuration uses `ddl-auto: update`, enables SQL logging, and enables formatted SQL logging. Do not commit real database passwords to source control.

## API Endpoints

All endpoints are rooted at `/api` and accept/return JSON unless noted otherwise.

| Method   | Path                                            | Description                                             |
| -------- | ----------------------------------------------- | ------------------------------------------------------- |
| `POST`   | `/api/users`                                    | Create a user                                           |
| `GET`    | `/api/users`                                    | List all users                                          |
| `GET`    | `/api/users/{id}`                               | Get a user by ID                                        |
| `PUT`    | `/api/users/{id}`                               | Update a user's name and role                           |
| `DELETE` | `/api/users/{id}`                               | Delete a user                                           |
| `POST`   | `/api/categories`                               | Create a category                                       |
| `GET`    | `/api/categories`                               | List all categories                                     |
| `GET`    | `/api/categories/{id}`                          | Get a category by ID                                    |
| `PUT`    | `/api/categories/{id}`                          | Update a category                                       |
| `DELETE` | `/api/categories/{id}`                          | Delete a category                                       |
| `POST`   | `/api/tickets`                                  | Create a ticket                                         |
| `GET`    | `/api/tickets`                                  | List all tickets                                        |
| `GET`    | `/api/tickets/{id}`                             | Get a ticket, including comments and status history     |
| `PUT`    | `/api/tickets/{id}`                             | Partially update ticket title, description, or priority |
| `PUT`    | `/api/tickets/{id}/assign`                      | Assign a ticket to an agent                             |
| `PUT`    | `/api/tickets/{id}/status?changedById={userId}` | Change ticket status and record status history          |
| `DELETE` | `/api/tickets/{id}`                             | Delete a ticket                                         |
| `POST`   | `/api/tickets/{ticketId}/comments`              | Add a comment to a ticket                               |
| `GET`    | `/api/tickets/{ticketId}/comments`              | List comments for a ticket                              |
| `GET`    | `/api/tickets/{ticketId}/comments/{commentId}`  | Get a comment by ID                                     |
| `DELETE` | `/api/tickets/{ticketId}/comments/{commentId}`  | Delete a comment                                        |

## Entities

- **User**: Has a unique email, name, and role: `CUSTOMER`, `AGENT`, or `ADMIN`.
- **Category**: Has a unique name and an optional description. A ticket belongs to one category.
- **Ticket**: Has a title, description, priority, status, category, customer, optional assigned agent, comments, and status history. Priorities are `LOW`, `MEDIUM`, `HIGH`, and `URGENT`; statuses are `OPEN`, `IN_PROGRESS`, `RESOLVED`, and `CLOSED`.
- **Comment**: Belongs to one ticket and one authoring user.
- **TicketStatusHistory**: Belongs to one ticket and records the previous status, new status, user who changed it, and timestamp.

A customer creates tickets. An agent may be assigned to a ticket. Tickets contain comments and status-history records, and deleting a ticket cascades to its comments and status history.

## Business Rules

- New tickets must have a nonblank title and description, a category, and a customer. Titles may not exceed 200 characters.
- Only users with the `CUSTOMER` role can create tickets.
- If no priority is supplied, it defaults to `MEDIUM`. Accepted values are `LOW`, `MEDIUM`, `HIGH`, and `URGENT`.
- New tickets start with `OPEN` status.
- Only users with the `AGENT` role can be assigned as ticket agents. Assigning an `OPEN` ticket automatically changes it to `IN_PROGRESS` and records the change using the assigned agent.
- Status changes must follow these transitions: `OPEN` -> `IN_PROGRESS` or `CLOSED`; `IN_PROGRESS` -> `RESOLVED` or `OPEN`; `RESOLVED` -> `CLOSED` or `IN_PROGRESS`. A ticket cannot be changed to its current status.
- `CLOSED` tickets cannot be modified, including status changes, and cannot receive comments.
- Status changes are recorded in ascending change-time order. Ticket and comment timestamps are set automatically when persisted.
- User creation requires a nonblank name, a valid email, and a role. Email addresses must be unique.
- Comment creation requires nonblank content and an author ID. The referenced ticket and author must exist.
- Referenced users, categories, tickets, and comments must exist for their corresponding operations. Validation and illegal argument errors return HTTP 400; illegal state errors return HTTP 409.

## Testing

Run the unit tests with:

.\mvnw.cmd test

The project includes unit tests for ticket business logic such as:

- Ticket creation
- Customer validation
- Category validation
- Agent assignment
- Status transitions
- Closed ticket protection

All 10 unit tests currently pass successfully.

## Database Backup

A PostgreSQL database backup is included with the project.

The backup can be restored using PostgreSQL tools.
