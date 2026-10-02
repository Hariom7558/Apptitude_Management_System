# Aptitude Management System

A complete aptitude assessment platform built with Java, Spring Boot, Thymeleaf, MySQL, and Maven. The application is designed for a Computer Application branch and supports student practice tests, domain-wise question libraries, admin management, and scheduled live tests with leaderboard tracking.

## Overview

This system helps students practice aptitude questions across multiple technology domains such as:

- DSA
- Java
- C
- C++
- MySQL
- Python
- HTML/CSS
- JavaScript
- Data Science
- Computer Applications

The application also provides:

- Admin login and secure access
- Question management
- Domain management
- Practice assessments per domain
- Live test scheduler with timer and leaderboard
- Student result history
- Ranking system for live tests

## Tech Stack

- Java 17
- Spring Boot 3.3.4
- Spring MVC
- Spring Data JPA
- Thymeleaf
- MySQL
- Maven
- BCrypt password hashing

## Application Features

### Student Features
- Student login with name and email
- Dashboard with domain cards
- Practice assessment by selected domain
- Live tests shown with start/end time and status
- Timed test flow
- Result summary and percentage calculation
- Previous test results page with test name and domain history
- Leaderboard and ranking after live test submission

### Admin Features
- Secure admin login
- Add, edit, and delete domains
- Add, view, and delete questions
- Filter questions by domain
- Schedule live tests
- Add questions to a live test
- Manage test start/end schedule
- View overall test performance

## Project Structure

```text
Apptitude_Management_System/
├── Apptitude_System/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── college/
│   │   │   │           └── aptitudesystem/
│   │   │   │               ├── controller/
│   │   │   │               ├── entity/
│   │   │   │               ├── repository/
│   │   │   │               └── AptitudeSystemApplication.java
│   │   │   ├── resources/
│   │   │   │   ├── application.properties
│   │   │   │   ├── static/
│   │   │   │   │   └── css/
│   │   │   │   └── templates/
│   │   │   │       ├── admin.html
│   │   │   │       ├── dashboard.html
│   │   │   │       ├── login.html
│   │   │   │       ├── result.html
│   │   │   │       ├── results.html
│   │   │   │       ├── test.html
│   │   │   │       └── admin-login.html
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── college/
│   │                   └── aptitudesystem/
│   │                       └── controller/
│   │                           └── AdminControllerTest.java
│   └── target/
│       └── generated build output
├── README.md
├── RUN_INSTRUCTIONS.txt
├── .gitignore
└── reset-aptitude-app.ps1
```

## Database Setup

The project uses MySQL and expects the following configuration:

- Database name: `aptitude_db`
- Username: `root`
- Password: `root`
- Host: `localhost`
- Port: `3306`

This is configured in:

`Apptitude_System/src/main/resources/application.properties`

If you want to use your own database, update the datasource URL and credentials there.

## Default Admin Credentials

The default admin account is:

- Username: `admin`
- Password: `Admin@123`

These values can be overridden with environment variables:

```bash
export APP_ADMIN_USERNAME=admin
export APP_ADMIN_PASSWORD=Admin@123
```

On Windows PowerShell:

```powershell
$env:APP_ADMIN_USERNAME = 'admin'
$env:APP_ADMIN_PASSWORD = 'Admin@123'
```

## Running the Application

### Prerequisites

- Java 17+
- Maven installed
- MySQL server running locally
- Database `aptitude_db` created or auto-created by Hibernate

### Run Steps

```bash
cd Apptitude_System
mvn spring-boot:run
```

Then open the app in your browser:

- Student portal: http://localhost:8080/
- Admin portal: http://localhost:8080/admin/login

## Main Modules

### Controller Layer
- `AdminController`: admin authentication, domain management, question management, live test scheduling
- `StudentController`: student login, dashboard, test flow, result pages, leaderboard

### Entity Layer
- `Question`: aptitude question data and correct answer
- `TestResult`: stores student performance data
- `LiveTest`: scheduled live exam details
- `AssessmentDomain`: domain/category definitions
- `AdminUser`: admin credentials and authentication data

### Repository Layer
- `QuestionRepository`: question lookup and category queries
- `TestResultRepository`: student history and ranking results
- `LiveTestRepository`: live test scheduling queries
- `AssessmentDomainRepository`: domain persistence
- `AdminUserRepository`: admin credentials lookup

## Notes

- The app uses Hibernate `ddl-auto=update`, so the database schema is created automatically when the application starts.
- Port 8080 is the default application port.
- If port 8080 is already occupied, stop the existing Java process or change the `server.port` value in `application.properties`.

## License

This project is provided for educational and demonstration purposes.

## Developer Note

This project was built as a full-stack aptitude assessment application aimed at academic and training use cases. It is structured to be easy to extend with more domains, advanced question types, or analytics dashboards.
