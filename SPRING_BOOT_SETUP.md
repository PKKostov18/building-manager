# Spring Boot Setup Guide

This project has been refactored to a Spring Boot 3.4.0 Gradle application.

## What Changed

The project was converted from a basic Gradle Java application to a full Spring Boot application with:
- **Spring Boot 3.4.0**
- **Spring Web** (for REST APIs)
- **Spring Data JPA** (for database access)
- **Spring Security** (for authentication/authorization)
- **MS SQL Server** driver support
- **Java 17** toolchain

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/housemanager/
│   │   │   ├── App.java                    # Main Spring Boot application
│   │   │   └── controller/
│   │   │       └── HomeController.java     # Example REST controller
│   │   └── resources/
│   │       └── application.properties       # Spring Boot configuration
│   └── test/
│       ├── java/com/housemanager/
│       │   └── AppTest.java                 # Spring Boot tests
│       └── resources/
│           └── application.properties       # Test configuration
└── build.gradle                             # Gradle build configuration
```

## Running the Application

### Build the project
```bash
./gradlew build
```

### Run the application
```bash
./gradlew bootRun
```

The application will start on http://localhost:8080

### Run tests
```bash
./gradlew test
```

## Available Endpoints

- `GET /api/` - Welcome message
- `GET /api/health` - Health check endpoint

**Note:** Spring Security is enabled by default. You'll need to authenticate to access the endpoints. The generated password is shown in the console when the application starts.

## Database Configuration

Update the database connection in `app/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=housemanager;encrypt=false
spring.datasource.username=sa
spring.datasource.password=YourPassword123
```

## Next Steps

Based on the README.md requirements, you can now implement:

1. **Entities:** Create JPA entities for Companies, Buildings, Apartments, Residents, Employees
2. **Repositories:** Create Spring Data JPA repositories for data access
3. **Services:** Implement business logic (fee calculation, building assignment, etc.)
4. **Controllers:** Create REST controllers for CRUD operations
5. **Security:** Configure Spring Security with roles (ADMIN, COMPANY, RESIDENT)

## Dependencies

Key dependencies included:
- `spring-boot-starter-web` - REST API support
- `spring-boot-starter-data-jpa` - JPA/Hibernate support
- `spring-boot-starter-security` - Security framework
- `mssql-jdbc` - MS SQL Server driver
- `spring-boot-starter-test` - Testing framework

## Build Tool

This project uses **Gradle 9.2.0** with the Gradle Wrapper.

All dependencies are managed in `gradle/libs.versions.toml` using the version catalog feature.
