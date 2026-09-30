# SkillSwap

## Overview

SkillSwap is a backend application designed to connect users based on the skills they can offer and the skills they want to learn. The platform enables users to exchange knowledge through structured sessions while maintaining a credit-based system for fair usage.

This project is built using Spring Boot and follows a layered architecture with proper separation of concerns, including controllers, services, repositories, and DTOs.

---

## Features

### User Management

* User registration and authentication
* Role-based access control (Admin and User)
* JWT-based authentication and authorization

### Skill Management

* Add and manage skills
* Categorize skills as:

  * Offered
  * Wanted

### Skill Matching System

* Matches users based on:

  * Skills they offer
  * Skills they want
* Efficient querying for scalable matching

### Session Management

* Create and manage learning sessions between users
* Track session status

### Credit System

* Users are assigned credits
* Credits are used to book sessions
* Prevents misuse of the platform

### Review System

* Users can review each other after sessions
* Helps maintain quality and trust

### Notification System

* Event-based notifications for:

  * Session updates
  * Matching alerts

---

## Tech Stack

### Backend

* Java
* Spring Boot
* Spring Security
* JWT (JSON Web Token)

### Database

* MySQL
* JPA / Hibernate

### Tools

* Maven
* Postman (API Testing)
* Swagger UI (API Documentation)

---

## Project Structure

```
skillswap
│
├── controller        # REST Controllers
├── service           # Business Logic
├── repository        # Data Access Layer
├── entity            # Database Entities
├── dto               # Data Transfer Objects
├── security          # JWT & Security Config
├── exception         # Custom Exceptions & Handlers
└── config            # Application Configurations
```

---

## API Features

* RESTful APIs following standard conventions
* Global exception handling
* Validation for request data
* Standard API response structure:

  * success
  * message
  * data
  * status
  * timestamp

---

## Authentication Flow

1. User signs up or logs in
2. Server validates credentials
3. JWT token is generated
4. Token is used to access protected APIs
5. Role-based authorization is applied

---

## Matching Logic

The matching system works by:

1. Fetching skills a user wants
2. Finding users who offer those skills
3. Filtering and returning matched users

This ensures scalability for a growing number of users.

---

## Setup Instructions

### Prerequisites

* Java 21+
* Maven
* MySQL

### Steps

1. Clone the repository

```
git clone https://github.com/dharaneeT/SkillSwap.git
```

2. Navigate to project directory

```
cd skillswap
```

3. Configure database in `application.properties`

```
spring.datasource.url=jdbc:mysql://localhost:3306/skillswap
spring.datasource.username=your_username
spring.datasource.password=your_password
```

4. Build the project

```
mvn clean install
```

5. Run the application

```
mvn spring-boot:run
```

---

## API Testing

* Use Postman for testing endpoints
* Swagger UI available for API documentation

---

## Future Enhancements

* Frontend integration
* Real-time notifications
* Advanced recommendation system
* Session scheduling with calendar integration

---

## Author

Dharaneetharan
Software Developer
