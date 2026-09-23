# Food Delivery App - Backend REST API

A Spring Boot backend application for a food delivery service. It supports user authentication with JWT, role-based access for customers, restaurant owners, and admins, cart management, and order placement.

---

## Tech Stack

- **Java 21**
- **Spring Boot 4.1.0**
- **Spring Security & JWT** (authentication & authorization)
- **Spring Data JPA / Hibernate**
- **MySQL**
- **JUnit 5 & Mockito** (unit testing)
- **GitHub Actions** (CI pipeline)

---

## Features

- **JWT Auth & RBAC**: Role-based access for `CUSTOMER`, `RESTAURANT_OWNER`, and `ADMIN`.
- **Secure User Context**: Cart and order endpoints pull user identity directly from the JWT token.
- **DTO Pattern**: API responses return clean DTOs instead of exposing database entities.
- **Business Rules**: Prevents mixing foods from multiple restaurants in a cart and checks stock at checkout.
- **Tests & CI**: Service unit tests using JUnit 5 & Mockito, automated with GitHub Actions.

---

## API Endpoints

### Auth (`/api/auth`)
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and receive JWT token

### Cart (`/api/cart`) - *Customer only*
- `GET /api/cart` - View current user's cart and total
- `POST /api/cart/add` - Add an item to cart (body: `foodId`, `quantity`)
- `DELETE /api/cart` - Clear all items from cart

### Orders (`/api/orders`) - *Customer only*
- `POST /api/orders/place` - Place order from cart
- `GET /api/orders` - View past orders

### Restaurants & Foods
- `GET /api/restaurants` - List restaurants
- `POST /api/restaurants` - Add restaurant (*Admin, Owner*)
- `GET /api/foods` - List food items
- `POST /api/foods` - Add food item (*Admin, Owner*)

---

## How to Run Locally

### 1. Prerequisites
- Java 21 or higher installed
- MySQL running locally

### 2. Configure Database
Update `src/main/resources/application.properties` with your MySQL credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/food_delivery?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### 3. Build and Run
```bash
./mvnw clean spring-boot:run
```
The server will start on port `8080`.

### 4. Run Tests
```bash
./mvnw clean test
```
