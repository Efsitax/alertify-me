# Software Requirements Specification (SRS)

**Project Name:** Alertify.me
**Version:** 1.0.0
**Date:** December 18, 2025
**Status:** Approved

---

## 1. Introduction

### 1.1. Purpose
**Alertify.me** is a scalable backend system designed to track product prices on e-commerce websites. It notifies users via email when a product's price drops below a user-defined threshold.

### 1.2. Scope
The project will be developed using a **Microservices Architecture**. Initially, it will operate without a user interface (Frontend), managing all operations via REST APIs. The system encompasses user management, scheduled price tracking, web scraping, and notification processes.

---

## 2. General Architecture

### 2.1. Architectural Style
* **Architecture:** Microservices Architecture
* **Design Pattern:** Clean Architecture, Domain-Driven Design (DDD)
* **Repository Structure:** Monorepo (Gradle Multi-Module)
* **Communication:**
    * *Synchronous:* REST (Minimal usage, e.g., fetching user email).
    * *Asynchronous:* Event-Driven (Message Broker: RabbitMQ/Kafka).

### 2.2. Technology Stack
* **Language:** Java 21 (LTS)
* **Framework:** Spring Boot (Latest Stable Version)
* **Database:** PostgreSQL (Database per Service pattern)
* **Build Tool:** Gradle
* **Message Broker:** RabbitMQ
* **Containerization:** Docker & Docker Compose

---

## 3. System Components (Microservices)

The system consists of 4 fundamental microservices.

### 3.1. Identity Service
* **Responsibility:** Handles user registration, authentication, and token (JWT) management.
* **Database Table:** `users`
    * `id` (UUID, PK)
    * `email` (VARCHAR 255, Unique)
    * `password_hash` (VARCHAR 255)
    * `first_name` (VARCHAR 50)
    * `last_name` (VARCHAR 50)
    * `created_at` (TIMESTAMP)

### 3.2. Tracking Service
* **Responsibility:** Manages user tracking lists, triggers scheduled tasks (Scheduler), and executes core business logic.
* **Database Tables:**
    * **`tracked_products` (Main Table):**
        * `id` (UUID, PK)
        * `user_id` (UUID) - *Reference ID from Identity Service*
        * `url` (TEXT)
        * `product_name` (VARCHAR 255)
        * `current_price` (DECIMAL) - *Denormalized for read performance*
        * `in_stock` (BOOLEAN)
        * `currency` (VARCHAR 3)
        * `target_price` (DECIMAL)
        * `is_active` (BOOLEAN)
        * `last_checked_at` (TIMESTAMP)
    * **`price_histories` (Graph Data):**
        * `id` (UUID, PK)
        * `product_id` (UUID, FK)
        * `price` (DECIMAL)
        * `detected_at` (TIMESTAMP)

### 3.3. Scraper Service
* **Responsibility:** A stateless worker. It fetches the target URL, parses the HTML, extracts the price, and returns the result.
* **Database:** None (Stateless).

### 3.4. Notification Service
* **Responsibility:** Sends emails to users based on triggered events.
* **Database Table:** `notification_logs`
    * `id` (UUID, PK)
    * `recipient` (VARCHAR)
    * `message` (TEXT)
    * `status` (SENT/FAILED)

---

## 4. Business Flows

### 4.1. Add Product Scenario
1.  User sends a `POST /trackings` request.
2.  **Tracking Service** saves the product to the `tracked_products` table.
3.  Returns `201 Created` to the user.
4.  Immediately publishes a `PriceCheckRequested` event to the message queue.

### 4.2. Periodic Check & Notification Scenario
1.  The Scheduler in the **Tracking Service** runs every **30 minutes**.
2.  It identifies products due for a check and pushes `PriceCheckRequested` events to the Queue.
3.  **Scraper Service** consumes the event, visits the target site, and extracts the price.
4.  It publishes the result as a `PriceCheckCompleted` event back to the Queue.
5.  **Tracking Service** consumes the result:
    * Updates `current_price` in the `tracked_products` table.
    * Inserts a new record into `price_histories` (for historical graphs).
    * **Logic:** If `current_price <= target_price`, it publishes a `NotificationRequired` event.
6.  **Notification Service** consumes the event, retrieves the email address from Identity Service, and sends the email.

---

## 5. API Specifications

### 5.1. Authentication
* `POST /api/v1/auth/register`: Register a new user.
* `POST /api/v1/auth/login`: Login (Returns: JWT Token).

### 5.2. Tracking
* `GET /api/v1/trackings`: Get the tracking list (Supports Pagination).
    * *Response Data:* Product ID, URL, Current Price, Target Price.
* `POST /api/v1/trackings`: Start tracking a new product.
    * *Body:* `{ "url": "...", "targetPrice": 1500.00 }`
* `DELETE /api/v1/trackings/{id}`: Stop tracking (Soft delete: `is_active=false`).
* `GET /api/v1/trackings/{id}/history`: Get price history graph data.
    * *Response Data:* `[{ "price": 100, "date": "..." }, ...]`

### 5.3. Error Handling Standards

The system employs a consistent error handling strategy across all microservices and API endpoints. Errors returned to the client (Frontend or other services) follow a standardized JSON format.

**Standard Error Response Model:**
```json
{
  "timestamp": "2025-12-18T18:30:00",
  "status": 400,
  "error": "BAD_REQUEST",
  "code": "PRODUCT_LIMIT_EXCEEDED",
  "message": "Maximum tracking limit reached.",
  "path": "/api/v1/trackings"
}
```

---

## 6. Non-Functional Requirements

1.  **Consistency:** The system operates on **Eventual Consistency**. Price updates may have a slight delay depending on scraping queue depth.
2.  **Fault Tolerance:** If a target site (e.g., Amazon) is unreachable, the Scraper Service must implement a **Retry Mechanism** before moving the task to a Dead Letter Queue (DLQ).
3.  **Security:**
    * Passwords must be hashed using `BCrypt` or `Argon2`.
    * API endpoints must be secured using JWT (Stateless Authentication).
4.  **Logging:** System logs should be written to standard output (Stdout) in JSON format for future ELK Stack integration.