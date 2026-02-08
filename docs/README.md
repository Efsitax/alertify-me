<!--
Copyright 2026 Alertify.me Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Alertify.me - Smart Price Tracking System

**Alertify.me** is a scalable, microservices-based backend system designed to track product prices across various e-commerce platforms. It automatically monitors price changes and notifies users when a product's price drops below their specified target threshold.

## 🚀 Architecture Overview

The system is built using **Clean Architecture** and **Domain-Driven Design (DDD)** principles, consisting of four core microservices:

- **Identity Service**: Manages user registration, authentication, and JWT-based security.

- **Tracking Service**: Handles user tracking lists, manages price history, and coordinates scheduled scan tasks.

- **Scraper Service**: A stateless worker that uses Playwright to navigate and extract real-time data from sites like Amazon, Trendyol, Hepsiburada, and N11.

- **Notification Service**: Responsible for sending alerts (e.g., via email) when price drop events are triggered.

## 🛠 Tech Stack

- **Language**: Java 21 (LTS)

- **Framework**: Spring Boot 4.0.1

- **Database**: PostgreSQL (Database-per-service pattern)

- **Message Broker**: RabbitMQ (Asynchronous, event-driven communication)

- **Web Scraping**: Microsoft Playwright & Jsoup

- **Security**: Spring Security with stateless JWT

- **Build Tool**: Gradle (Multi-module setup)

## 🏗 Project Structure

```
alertify-me/
├── common-shared/          # Shared events, exceptions, and RabbitMQ configs
├── identity-service/       # Auth and User management
├── scraper-service/        # Playwright-based scraping engine
├── tracking-service/       # Core logic, Scheduler, and Price History
└── notification-service/   # Alert delivery engine
```

## 🔄 Business Workflow

1. **Product Entry**: A user submits a URL and a target price. The Tracking Service stores the request and triggers an initial scrape event.

2. **Periodic Scanning**: A scheduler in the Tracking Service identifies products due for a check (e.g., every 30 minutes) and publishes a ScrapeRequestEvent to RabbitMQ.

3. **Real-time Scraping**: The Scraper Service consumes the event, visits the e-commerce site using a headless browser, parses the price, and publishes a PriceScrapeCompletedEvent.

4. **Price Evaluation**: The Tracking Service updates the database. If currentPrice <= targetPrice, a notification event is dispatched to the Notification Service to alert the user.

## ⚙️ Getting Started

### Prerequisites

- JDK 21

- Docker & Docker Compose (for PostgreSQL and RabbitMQ)

### Installation

1. **Build the project**:

    ```bash
    ./gradlew build
    ```

2. **Start infrastructure**:

    ```bash
    docker-compose up -d
    ```
3. **Run Services**: You can run each service individually using `./gradlew :<service-name>:bootRun.

## 🔒 Standard Error Handling

The system uses a standardized JSON format for all API errors:

```json
{
   "timestamp": "2025-12-18T18:30:00",
   "status": 404,
   "error": "Not Found",
   "message": "Product not found with id : ...",
   "path": "/api/v1/trackings/..."
}
```

## 📄 License

This project is licensed under the Apache License 2.0. See the [LICENSE](../LICENSE) file for the full text.