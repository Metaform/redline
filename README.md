# Redline

A Spring Boot application with REST endpoints, JPA repositories, Spring Security, and Keycloak authentication.

## Features

- Keycloak integration for authentication
- PostgreSQL and H2 database support

## Prerequisites

- Java 21 (LTS) JDK
- Gradle (or use the included wrapper - `./gradlew`)
- PostgreSQL (for production profile)
- Keycloak server (for authentication)

## Database Configuration

### Development Profile (H2)

The development profile uses an in-memory H2 database:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Access H2 Console at: http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:redlinedb`
- Username: `sa`
- Password: (leave empty)

### Production Profile (PostgreSQL)

The production profile uses PostgreSQL. Set the following environment variables:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/redlinedb
export DATABASE_USERNAME=redline
export DATABASE_PASSWORD=your_password
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## Quick Start with Docker Compose

The easiest way to get started is using Docker Compose, which will start both PostgreSQL and Keycloak:

```bash
# Start PostgreSQL and Keycloak
docker-compose up -d

# Check logs
docker-compose logs -f

# Stop services
docker-compose down
```

After starting the services:
- Keycloak Admin Console: http://localhost:8080 (admin/admin)
- PostgreSQL: localhost:5432 (redline/redline)

## Keycloak Configuration

### Environment Variables

Set these environment variables to configure Keycloak:

```bash
export KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/your-realm
export KEYCLOAK_JWK_SET_URI=http://localhost:8080/realms/your-realm/protocol/openid-connect/certs
```

### Keycloak Setup Steps

1. Start Keycloak server
2. Create a new realm (e.g., "redline")
3. Create a client for your application
4. Configure the client:
   - Access Type: bearer-only or public
   - Valid Redirect URIs: http://localhost:8081/*
5. Create roles (e.g., USER, ADMIN)
6. Create users and assign roles

## Building the Application

```bash
./gradlew build
```

## Running the Application

### Quick Start (Development with H2)

Use the convenience script:

```bash
./run-dev.sh
```

Or run directly:

```bash
./gradlew bootRun
```

This starts the application with:
- H2 in-memory database
- H2 Console at http://localhost:8081/h2-console
- Server at http://localhost:8081

### With PostgreSQL (Production)

```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

The application will start on port 8081.

## API Endpoints

### Public Endpoints (No Authentication Required)

- `GET /api/public/health` - Health check
- `GET /api/public/info` - Application info

### Protected Endpoints (Authentication Required)

#### User Endpoints

- `GET /api/users` - Get all users (requires USER role)
- `GET /api/users/{id}` - Get user by ID (requires USER role)
- `POST /api/users` - Create new user (requires ADMIN role)
- `PUT /api/users/{id}` - Update user (requires ADMIN role)
- `DELETE /api/users/{id}` - Delete user (requires ADMIN role)
- `GET /api/users/me` - Get current authenticated user

## Authentication

This application uses OAuth2 JWT tokens from Keycloak. To access protected endpoints:

1. Obtain a JWT token from Keycloak
2. Include the token in the Authorization header:

```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8081/api/users/me
```

## Testing

Run tests with:

```bash
./gradlew test
```

## License

This project is licensed under the Apache 2.0 License.
