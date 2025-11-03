# Quick Start Guide - House Manager Spring Boot Application

## Prerequisites
- Java 17 or higher
- MS SQL Server (for production use)

## Quick Commands

### Build the project
```bash
./gradlew build
```

### Run the application
```bash
./gradlew bootRun
```

### Run tests
```bash
./gradlew test
```

### Build executable JAR
```bash
./gradlew bootJar
```

### Run the JAR
```bash
java -jar app/build/libs/app.jar
```

## Access the Application

Once started, the application runs at:
- **URL:** http://localhost:8080
- **Health Check:** http://localhost:8080/api/health
- **Welcome:** http://localhost:8080/api/

## Default Login (Spring Security)

Spring Security is enabled by default. The generated password appears in the console when you start the application:

```
Using generated security password: [YOUR-PASSWORD-HERE]
```

- **Username:** `user`
- **Password:** Check console output

## Project Structure

```
house-manager/
├── app/
│   ├── build.gradle               # Build configuration
│   └── src/
│       ├── main/
│       │   ├── java/              # Java source code
│       │   └── resources/         # Configuration files
│       └── test/                  # Test code
├── gradle/
│   └── libs.versions.toml         # Dependency versions
└── gradlew                        # Gradle wrapper
```

## Configuration

Edit `app/src/main/resources/application.properties` to configure:
- Server port
- Database connection
- JPA/Hibernate settings
- Security settings
- Logging levels

## Technology Stack

- Spring Boot 3.4.0
- Spring Web (REST APIs)
- Spring Data JPA (Database)
- Spring Security (Auth)
- MS SQL Server
- Gradle 9.2.0
- Java 17

## Next Steps

1. Configure your database in `application.properties`
2. Create JPA entities for your domain model
3. Create repositories using Spring Data JPA
4. Implement business logic in service classes
5. Create REST controllers for your API endpoints
6. Configure Spring Security for your needs

## Documentation

- **SPRING_BOOT_SETUP.md** - Detailed setup guide
- **REFACTORING_SUMMARY.md** - Complete refactoring details
- **README.md** - Project overview

## Need Help?

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Guide](https://spring.io/guides/gs/accessing-data-jpa/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)

---

**You're all set!** Start building your Building Manager SaaS application! 🚀
