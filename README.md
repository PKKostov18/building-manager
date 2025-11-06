# Building Manager (SaaS Platform)

This is a Java/Spring Boot application that implements a SaaS (Software-as-a-Service) platform, "Building Manager". The project is based on the CSCB525 course assignment and simulates a real system that allows professional property management companies to manage their clients, buildings, employees, and monthly fees.

The system is built with Spring Security and supports 3 different user roles:
1.  **ADMIN:** Full control over the platform; manages the client companies.
2.  **COMPANY:** A profile for a property management company. Manages *its own* employees, buildings, apartments, and residents.
3.  **RESIDENT:** A profile for a resident (inspired by Livo). Has access only to *their own* apartment, payment history, and current dues.

## 🎯 Core Features

* Full CRUD operations for Companies, Buildings, Apartments, Residents, and Employees.
* Secure authentication and authorization with **Spring Security** (password hashing with BCrypt).
* Complex business logic for **automatic fee calculation** based on:
    * Apartment area.
    * Number of residents over 7 years of age.
    * Presence of pets.
* Business logic for **automatic assignment** of new buildings to the employee with the fewest properties.
* Logic for **re-assigning** buildings upon an employee's departure.
* Report generation (total revenue, buildings per employee, etc.).
* Saving paid fee data to a file.

## 🛠️ Tech Stack

* **Backend:** Java 17+
* **Framework:** Spring Boot 3+
* **Security:** Spring Security
* **Database:** Spring Data JPA / Hibernate
* **Database Type:** MSSQL
* **Build Tool:** Gradle
