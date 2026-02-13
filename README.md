# testing-copilot-agent

Testing copilot

## TodoList API

A REST API built with Spring Boot (Kotlin) for managing todo lists with user authentication.

### Features

- RESTful API built with Spring Boot 3.3.0
- Kotlin as the programming language
- PostgreSQL database
- Flyway for database migrations
- JWT-based authentication with refresh tokens
- User registration and login
- Hello World endpoint at `/api/hello`

### Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher (for production)
- Gradle (included via wrapper)

### Running the Application

1. **Clone the repository**
   ```bash
   git clone https://github.com/MateusNavarro77/testing-copilot-agent.git
   cd testing-copilot-agent
   ```

2. **Configure the database**
   
   Update `src/main/resources/application.yml` with your PostgreSQL credentials if needed:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/todolist
       username: postgres
       password: postgres
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

4. **Test the Hello World endpoint**
   ```bash
   curl http://localhost:8080/api/hello
   ```
   
   Expected response:
   ```json
   {"message":"Hello World!"}
   ```

### Authentication API

The API uses JWT (JSON Web Token) authentication with refresh tokens. Access tokens expire in 1 hour, while refresh tokens last for 30 days.

#### Register a new user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "secure_password123"
  }'
```

Response:
```json
{
  "message": "User registered successfully"
}
```

#### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "secure_password123"
  }'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "tokenType": "Bearer"
}
```

#### Access protected endpoints

Use the access token in the Authorization header:

```bash
curl http://localhost:8080/api/protected-endpoint \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### Refresh access token

When your access token expires, use the refresh token to get a new one:

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "tokenType": "Bearer"
}
```

#### Logout

Invalidate your refresh token:

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }'
```

Response:
```json
{
  "message": "Logged out successfully"
}
```

### Running Tests

```bash
./gradlew test
```

Tests use an H2 in-memory database and don't require PostgreSQL to be running.

### Building the Application

```bash
./gradlew build
```

### Project Structure

```
src/
├── main/
│   ├── kotlin/
│   │   └── com/todolist/api/
│   │       ├── TodoListApplication.kt       # Main application class
│   │       ├── config/
│   │       │   ├── SecurityConfig.kt        # Spring Security configuration
│   │       │   └── JwtAuthenticationFilter.kt # JWT authentication filter
│   │       ├── controller/
│   │       │   ├── HelloController.kt       # Hello World REST controller
│   │       │   └── AuthController.kt        # Authentication endpoints
│   │       ├── dto/
│   │       │   └── AuthDtos.kt              # Authentication DTOs
│   │       ├── entity/
│   │       │   ├── User.kt                  # User entity
│   │       │   └── RefreshToken.kt          # Refresh token entity
│   │       ├── repository/
│   │       │   ├── UserRepository.kt        # User repository
│   │       │   └── RefreshTokenRepository.kt # Refresh token repository
│   │       └── service/
│   │           ├── JwtService.kt            # JWT token service
│   │           ├── RefreshTokenService.kt   # Refresh token service
│   │           └── CustomUserDetailsService.kt # User details service
│   └── resources/
│       ├── application.yml                  # Application configuration
│       └── db/migration/
│           ├── V1__initial_schema.sql       # Initial Flyway migration
│           └── V2__create_users_and_refresh_tokens.sql # User and token tables
└── test/
    ├── kotlin/
    │   └── com/todolist/api/controller/
    │       ├── HelloControllerTest.kt       # Controller tests
    │       └── AuthControllerTest.kt        # Authentication tests
    └── resources/
        └── application-test.yml             # Test configuration
```

### Security Configuration

- JWT secret key is configured in `application.yml` under `jwt.secret`
- Access token expiration: 1 hour (3600000 milliseconds)
- Refresh token expiration: 30 days (2592000000 milliseconds)
- Passwords are encrypted using BCrypt

**Note:** In production, the JWT secret should be stored securely (e.g., environment variables, secrets manager) and not hardcoded in configuration files.

### Future Scope

The application will be expanded to include:
- Todo checklist management
- Individual todo items within checklists
- Additional user profile features

