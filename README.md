# testing-copilot-agent

Testing copilot

## TodoList API

A REST API built with Spring Boot (Kotlin) for managing todo lists with user authentication.

### Features

- RESTful API built with Spring Boot 3.3.0
- Kotlin as the programming language
- PostgreSQL database
- Flyway for database migrations
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
│   │       └── controller/
│   │           └── HelloController.kt       # Hello World REST controller
│   └── resources/
│       ├── application.yml                  # Application configuration
│       └── db/migration/
│           └── V1__initial_schema.sql       # Initial Flyway migration
└── test/
    ├── kotlin/
    │   └── com/todolist/api/controller/
    │       └── HelloControllerTest.kt       # Controller tests
    └── resources/
        └── application-test.yml             # Test configuration
```

### Future Scope

The application will be expanded to include:
- User registration and login
- Todo checklist management
- Individual todo items within checklists

