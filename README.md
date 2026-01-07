# Building Manager (SaaS Platform)

**Building Manager** is a comprehensive Java/Spring Boot SaaS application designed for property management companies. It simulates a real-world system allowing companies to manage their clients, buildings, employees, residents, and monthly fee collections efficiently.

The project features a modern, responsive UI, complex business logic for tax calculations, and a real-time administrative dashboard.

## 🚀 Key Features & Updates

### 📊 Real-Time Admin Dashboard
* **Activity Tracking:** Visual line chart (using **Chart.js**) displaying website logins/page views over the last 7 days based on real database entries.
* **Live System Stats:** Real-time monitoring of **Active Sessions** (users currently online) and **Server Memory Load** (RAM usage).
* **Automated Processes:** Scheduled background tasks (e.g., simulated Database Backup at 03:00 AM).

### 🏢 Modern Building Management
* **Visual Apartment Layout:** Interactive grid view of apartments organized by floors.
* **Status Indicators:** Visual cues for Empty (vacant), Occupied, and Debt (unpaid fees) apartments.
* **Financial KPIs:** Instant overview of collected revenue vs. outstanding liabilities per building.
* **Quick Configuration:** Modal-based forms to assign owners, residents, and pets to units.

### 💰 Automated Fee Calculation
The system implements complex logic to calculate monthly fees automatically based on:
* **Base Rate:** Building tax per square meter.
* **Elevator Tax:** Applied only if the resident uses the elevator.
* **Pet Tax:** Additional fee for households with pets.
* **Resident Count:** Fees adjusted based on the number of occupants.

## 👥 User Roles & Security

The application uses **Spring Security** to enforce strict role-based access control (RBAC):

1.  **ADMIN:**
    * Global system access.
    * Manages Company registrations.
    * Views system-wide statistics (Graphs, Logs, Health).
2.  **COMPANY:**
    * Manager profile for the client company.
    * Manages Buildings and Employees.
    * Views financial reports for their specific company.
3.  **EMPLOYEE:**
    * Operational staff member.
    * Manages assigned Buildings and Residents.
    * Can edit apartment configurations and resident details.
4.  **RESIDENT:**
    * End-user profile.
    * View monthly bills and payment history.

## 🛠️ Tech Stack

### Backend
* **Language:** Java 17
* **Framework:** Spring Boot 3+
* **Security:** Spring Security (BCrypt, Session Management, Listeners)
* **Database:** MSSQL (via Spring Data JPA / Hibernate)
* **Scheduling:** Spring Scheduler (Cron jobs)

### Frontend
* **Template Engine:** Thymeleaf
* **Styling:** Bootstrap 5, Custom CSS
* **Icons:** FontAwesome 6
* **Charts:** Chart.js
* **Design:** Modern "SaaS-style" layout with responsive cards and grids.

### Build Tools
* **Build System:** Gradle

## ⚙️ Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/your-username/building-manager.git](https://github.com/your-username/building-manager.git)
    ```
2.  **Configure Database:**
    Update `src/main/resources/application.properties` with your MSSQL credentials.
3.  **Run the Application:**
    ```bash
    ./gradlew bootRun
    ```
4.  **Access the App:**
    Open `http://localhost:8080` in your browser.

## 🔄 Automated Logic Details

* **Employee Assignment:** New buildings are automatically assigned to the employee with the fewest current properties to ensure balanced workload.
* **Login Logging:** A custom `ApplicationListener` captures every successful login event and stores it in the `login_logs` table for analytics.
* **Session Tracking:** An `HttpSessionListener` tracks live user sessions to display real-time activity on the dashboard.

