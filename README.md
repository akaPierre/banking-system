# Online Banking System 💳

A complete **Online Banking System backend** built with **Java 21+** and **Spring Boot 3**. It provides secure REST APIs for **user registration/login (JWT)**, **account management**, **money transfers**, and **transaction history**, using **H2 in‑memory database** for development.

This project is ideal for practicing:

- Spring Boot REST API design
- Spring Security with JWT
- JPA/Hibernate with relational models
- Clean layered architecture (controller/service/repository)
- Practical financial-style business logic

---

## ✨ Features

- **User authentication**
    - Register new users with username, email, and password
    - Passwords stored using **BCrypt** hashing
    - Login endpoint issues **JWT** tokens
- **Account management**
    - Auto-creates a bank account for each user on first access
    - Starting balance (e.g. 1000.00) for testing
    - View current balance and account number
- **Money transfers**
    - Transfer funds between accounts by account number
    - Validation for:
        - User existence
        - Account existence
        - Sufficient balance
- **Transaction history**
    - Records each transfer as a `Transaction` entity
    - Query transactions for a given user’s account
- **Tech stack**
    - Java 21 (works on newer JDKs)
    - Spring Boot 3.3.x
    - Spring Web, Spring Data JPA, Spring Security
    - H2 in‑memory database (dev)
    - JWT (JSON Web Tokens) via JJWT

---

## 🛠 Tech Stack

- **Language:** Java 21+
- **Framework:** Spring Boot 3.3.x
- **Security:** Spring Security + JWT
- **Persistence:** Spring Data JPA (Hibernate)
- **Database:** H2 (in‑memory, console enabled)
- **Build Tool:** Maven

---

## 📋 Requirements

Make sure you have:

- **Java JDK 21+** installed  

```
java -version
```

- **Maven 3.8+** installed  

```
mvn -v
```


Everything else (Spring Boot, H2, JPA, JWT libraries) is managed via Maven dependencies in `pom.xml`.

---

## 📁 Project Structure

```
banking-system/
├── pom.xml
├── README.md
└── src/
└── main/
├── java/
│ └── com/
│ └── banking/
│ ├── BankingSystemApplication.java
│ ├── config/
│ │ └── SecurityConfig.java
│ ├── controller/
│ │ ├── AuthController.java
│ │ └── AccountController.java
│ ├── entity/
│ │ ├── User.java
│ │ ├── Account.java
│ │ └── Transaction.java
│ ├── repository/
│ │ ├── UserRepository.java
│ │ ├── AccountRepository.java
│ │ └── TransactionRepository.java
│ └── service/
│ └── JwtService.java
└── resources/
└── application.yml
```

---

## ⚙️ Configuration

### application.yml (development)

- Uses **H2 in‑memory DB** (`jdbc:h2:mem:testdb`)
- Enables H2 web console at `/h2-console`
- Configures JPA to create/update schema on startup

Login data for H2 console (dev):

- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

---

## 🚀 Building and Running

### From the project root:

### Clean and build
```
mvn clean package
```

### Run with Spring Boot plugin
```
mvn spring-boot:run
```

The server starts on:

- `http://localhost:8080`

You should see logs indicating:

- H2 console available at `/h2-console`
- Spring Boot started on port 8080

---

## 🔑 Authentication Flow

### 1. Register User

`POST /api/auth/register`

**Request body:**

```
{
"username": "alice",
"password": "pass123",
"email": "alice@example.com"
}
```

**Response (200):**

```
{
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
"userId": 1
}
```

- Password is stored hashed with BCrypt.
- A JWT token is returned for immediate authenticated use.

### 2. Login

`POST /api/auth/login`

**Request body:**

```
{
"username": "alice",
"password": "pass123"
}
```

**Response (200):**

```
{
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
"userId": 1
}
```

Use this `token` in the `Authorization` header:

```
Authorization: Bearer <token>
```

---

## 💳 Account APIs

All account endpoints require a valid **Bearer token**.

### Get Account (auto-create if needed)

`GET /api/accounts`

**Headers:**

```
Authorization: Bearer <user_jwt>
```

**Response (200):**

```
{
"accountNumber": "f128b102-ddb",
"balance": 1000.00
}
```

If the user has no account yet, a new one is created with a starting balance (e.g. 1000).

---

### Transfer Money

`POST /api/accounts/transfer`

**Headers:**

```
Authorization: Bearer <alice_jwt>
Content-Type: application/json
```

**Request body:**

```
{
"toAccountNumber": "bob-account-number-here",
"amount": 100.00
}
```

**Response (200):**

```
{
"message": "Transfer successful: $100.00 to bob-account-number-here",
"account": {
"accountNumber": "alice-account-number",
"balance": 900.00
}
}
```

Validation rules:

- Source user must exist.
- Source and destination accounts must exist.
- Source account must have sufficient balance.

---

### Transaction History

`GET /api/accounts/transactions`

**Headers:**

```
Authorization: Bearer <user_jwt>
```

**Response (200):**

```
[
{
"id": 1,
"fromAccount": "alice-account-number",
"toAccount": "bob-account-number",
"amount": 100.00,
"type": "TRANSFER",
"timestamp": "2025-12-22T23:40:12.123456"
}
]
```

---

## 🧪 Example Requests (PowerShell)

```
Register Alice
$body = @{username="alice"; password="pass123"; email="alice@example.com"} | ConvertTo-Json
$register = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
$aliceToken = ($register.Content | ConvertFrom-Json).token

Register Bob
$body = @{username="bob"; password="pass456"; email="bob@example.com"} | ConvertTo-Json
$register = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/register" -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
$bobToken = ($register.Content | ConvertFrom-Json).token

Get Bob's account number
$bobAccount = (Invoke-WebRequest -Uri "http://localhost:8080/api/accounts" -Method GET -Headers @{Authorization="Bearer $bobToken"} -UseBasicParsing).Content | ConvertFrom-Json
$bobAccountNumber = $bobAccount.accountNumber

Alice transfers 100 to Bob
$body = @{toAccountNumber=$bobAccountNumber; amount=100} | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8080/api/accounts/transfer" -Method POST -Body $body -ContentType "application/json" -Headers @{Authorization="Bearer $aliceToken"} -UseBasicParsing
```

---

## 🧱 Architecture

- **Controller layer**
    - `AuthController` – registration & login
    - `AccountController` – account info, transfers, history
- **Service layer**
    - `JwtService` – token generation and parsing
- **Repository layer**
    - `UserRepository`, `AccountRepository`, `TransactionRepository`
- **Security**
    - `SecurityConfig` configures HTTP security, CSRF, and CORS

---

## 🔮 Future Improvements

- Switch from H2 to PostgreSQL/MySQL for production
- Role-based authorization (ADMIN/USER)
- Multiple accounts per user (checking/savings)
- Deposit/withdraw endpoints
- Pagination & filtering for transactions
- OpenAPI/Swagger documentation
- Integration tests with Testcontainers

---

## 📄 License

MIT License

Copyright (c) 2025 Daniel Pierre Fachini de Toledo

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---

## 👤 Author

**Daniel Pierre Fachini de Toledo**  
GitHub: https://github.com/akaPierre

---

If you have suggestions or want to extend this project, feel free to fork it or open an issue 🙂