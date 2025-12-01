# Car Builder VIN

A VIN Build Planner Service for tracking vehicles and planning upgrade builds with detailed part management and budget tracking.

## Overview

Car Builder VIN helps automotive enthusiasts plan and document upgrade paths for their vehicles. Whether you're building an overlanding rig, a performance machine, or optimizing for daily driving, this application helps you organize parts, track costs, and monitor installation progress.

## Features

- **Vehicle Tracking**: Add vehicles by VIN with automatic decoding of year, make, model, and trim
- **Build Planning**: Create multiple upgrade builds per vehicle (Overlanding, Performance, Towing, etc.)
- **Part Management**: Organize parts with categories, tiers (Budget/Mid/Premium), pricing, and product links
- **Sub-Part Support**: Break down kits into individual components with separate pricing
- **Budget Tracking**: View total costs by build, category, and tier with progress tracking
- **Status Tracking**: Monitor parts from planned to ordered to installed

## Tech Stack

- **Backend**: Spring Boot 3.5.8 with Java 21
- **Database**: PostgreSQL with JPA/Hibernate
- **Migrations**: Flyway
- **Frontend**: Thymeleaf + HTMX + Pico CSS
- **Security**: Spring Security 6
- **API Docs**: SpringDoc OpenAPI 3.0.0
- **VIN Decoding**: MarketCheck API integration

## Getting Started

### Prerequisites

- Java 21+
- Docker (for PostgreSQL)
- Maven

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Car-Builder-VIN
   ```

2. Start the database:
   ```bash
   docker compose up postgres -d
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Open http://localhost:8080 in your browser

### Configuration

Database connection is configured in `application.properties`. Default Docker Compose setup:
- Database: `mydatabase`
- User: `myuser`
- Password: `secret`
- Port: `5432`

## Usage

1. **Create an Account**: Sign up to start tracking your vehicles
2. **Add a Vehicle**: Enter a VIN to auto-populate vehicle details, or add manually for project builds
3. **Create a Build**: Start a new upgrade plan (e.g., "Overland Build v1")
4. **Add Parts**: Add parts with links, prices, categories, and tiers
5. **Track Progress**: Mark parts as ordered/installed and monitor your budget

## Development

```bash
# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Package the application
./mvnw package

# Run with Docker Compose (full stack)
docker compose up
```

## Project Structure

```
src/main/java/com/sentinovo/carbuildervin/
├── config/          # Configuration classes
├── controller/      # REST and web controllers
├── dto/             # Data transfer objects
├── entity/          # JPA entities
├── repository/      # JPA repositories
├── service/         # Business logic
└── exception/       # Custom exceptions
```

## License

Private project - All rights reserved
