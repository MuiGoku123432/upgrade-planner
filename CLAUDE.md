# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Car-Builder-VIN is a Spring Boot 3.5.8 application built with Java 21 that provides a VIN build planner service. The application tracks vehicles by VIN (or without VIN for project builds), organizes upgrade plans per vehicle, and manages parts and sub-parts with pricing, categories, and tiers.

## Technology Stack

- **Framework**: Spring Boot 3.5.8 with Java 21
- **Database**: PostgreSQL with JPA/Hibernate and Flyway migrations
- **Security**: Spring Security 6
- **Frontend**: Thymeleaf templates with HTMX and Pico CSS
- **API Documentation**: SpringDoc OpenAPI 3.0.0
- **Build Tool**: Maven
- **Development Tools**: Spring Boot DevTools, Lombok
- **Container**: Docker Compose with PostgreSQL

## Common Development Commands

### Build and Run
```bash
# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Package the application
./mvnw package

# Run the application
./mvnw spring-boot:run

# Run with Docker Compose (includes PostgreSQL)
docker compose up
```

### Database Operations
The application uses Flyway for database migrations. Migration scripts should be placed in `src/main/resources/db/migration/` following the naming pattern `V{version}__{description}.sql`.

### Testing
```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=CarBuilderVinApplicationTests
```

## Architecture Overview

### Core Domain Model
The application is structured around these key entities:

1. **User Management**: `app_user`, `role`, `user_role` for authentication and authorization
2. **Vehicle Tracking**: `vehicle` (with optional VIN support for project builds)
3. **Build Planning**: `vehicle_upgrade` (upgrade plans per vehicle)
4. **Part Organization**: `part` and `sub_part` with hierarchical structure
5. **Classification**: `upgrade_category`, `part_category`, `part_tier` for organization

### Key Design Principles
- **Optional VIN Support**: Vehicles can exist without VIN for future/project builds
- **Hierarchical Parts**: Parts can contain sub-parts for detailed breakdown
- **Flexible Classification**: Parts have both category (functional) and tier (quality/cost) attributes
- **Numeric Priorities**: Parts include `priority_value` for easy sorting and filtering
- **External Integration**: VIN decoding via MarketCheck API

### Package Structure
```
com.sentinovo.carbuildervin/
├── CarBuilderVinApplication.java (main application class)
├── config/          (configuration classes)
├── controller/      (REST controllers)
├── dto/            (data transfer objects)
├── entity/         (JPA entities)
├── repository/     (JPA repositories)
├── service/        (business logic)
└── exception/      (custom exceptions)
```

## Database Schema Highlights

### Core Relationships
- **app_user** 1 → N **vehicle**
- **vehicle** 1 → N **vehicle_upgrade** (builds)
- **vehicle_upgrade** 1 → N **part**
- **part** 1 → N **sub_part**

### Key Features
- Partial unique index on vehicle VIN (allows null VINs)
- Numeric priority fields for easy sorting
- Status tracking for parts (PLANNED, ORDERED, INSTALLED, etc.)
- Cost calculations across categories and tiers

## API Design

The application exposes a RESTful JSON API documented with OpenAPI/Swagger. Key endpoint groups:

- `/auth/*` - Authentication and user management
- `/vin/decode` - VIN decoding via external service
- `/vehicles/*` - Vehicle CRUD operations
- `/builds/*` - Vehicle upgrade/build management
- `/parts/*` - Part management
- `/sub-parts/*` - Sub-part management
- Lookup endpoints for categories, tiers, etc.

## External Integrations

### MarketCheck VIN Decoder
- **Purpose**: Decode VIN to auto-populate vehicle details
- **API**: MarketCheck Basic VIN Decoder
- **Configuration**: API key stored in environment variables
- **Endpoint**: `/v2/decode/car/{vin}/specs`
- **Error Handling**: Graceful fallback for invalid VINs or service unavailability

## Frontend Architecture

The frontend uses a modern approach combining:
- **Thymeleaf** for server-side templating
- **HTMX** for dynamic interactions without heavy JavaScript
- **Pico CSS** for clean, semantic styling
- Static assets served from `/static/pico-main/`

## Configuration

### Application Properties
- Database configuration in `application.properties`
- Environment-specific settings via Spring profiles
- External service credentials via environment variables

### Docker Compose
- PostgreSQL service configured in `compose.yaml`
- Database: `mydatabase`, User: `myuser`, Password: `secret`
- Port: 5432 (containerized)

## Development Workflow

1. **Start PostgreSQL**: `docker compose up postgres -d`
2. **Run Application**: `./mvnw spring-boot:run`
3. **Access API Docs**: http://localhost:8080/swagger-ui.html (when implemented)
4. **Run Tests**: `./mvnw test`

## Important Notes

- The application currently contains only the main application class and basic configuration
- Most business logic, controllers, and entities are yet to be implemented
- Database schema is fully designed but migrations need to be created
- VIN decoding integration with MarketCheck is planned but not implemented
- Frontend templates and HTMX integration are planned but not implemented

## Documentation References

- **Project Design**: `projectDesign.md` - Comprehensive functional requirements
- **Database Design**: `databaseDesign.md` - Complete schema with relationships
- **API Documentation**: `internalWebDesgin.md` - REST API specification
- **External API**: `apiDocumentation.md` - MarketCheck integration details