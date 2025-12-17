# Hexagonal Architecture + MCP Implementation Plan

> **Status**: Planned (not yet implemented)
> **Estimated Effort**: 3-5 days
> **Risk Level**: Medium (refactor + new feature)

## Executive Summary

Transform Car-Builder-VIN from its current layered architecture into a hexagonal (ports & adapters) architecture, then add MCP (Model Context Protocol) support. This enables:

- **REST API**: Standard HTTP interface for web/mobile clients
- **MCP API**: Tool interface for AI agents (ChatGPT, Claude, custom agents)

Both interfaces delegate to the same service layer, ensuring consistent behavior.

---

## Current State Analysis

### Existing Architecture (Layered)
```
controller/ → service/ → repository/ → database
     ↓           ↓
   dto/      entities/
```

### Key Assets (Already Well-Structured)
- **12 Services**: Clean business logic with authorization checks
- **10 Entities**: Well-defined domain model
- **26+ DTOs**: Request/response objects ready
- **8 MapStruct Mappers**: Entity ↔ DTO conversion
- **9 Repositories**: JPA with custom queries

### Issues to Address
- `exception/` and `exceptions/` packages are duplicated → consolidate
- `GlobalExceptionHandler` uses `@RestControllerAdvice` but catches web controller exceptions
- Some services mix infrastructure concerns (direct WebClient calls in VinDecodingService)

---

## Target Architecture

### Hexagonal Structure
```
src/main/java/com/sentinovo/carbuildervin/

├── domain/                    # CORE (no external dependencies)
│   ├── model/                 # Entities (unchanged)
│   ├── repository/            # Repository interfaces
│   └── exception/             # Domain exceptions
│
├── application/               # USE CASES
│   └── service/               # Business logic services (moved from service/)
│
├── infrastructure/            # ADAPTERS (outbound)
│   ├── persistence/           # JPA repository implementations
│   ├── external/              # External API clients (VIN decoder)
│   └── config/                # Spring configuration
│
└── interfaces/                # ADAPTERS (inbound)
    ├── rest/                  # REST controllers (moved from controller/)
    ├── web/                   # Thymeleaf controllers
    └── mcp/                   # MCP tools (NEW)
```

### Dependency Flow
```
interfaces/rest  ──┐
interfaces/web   ──┼──→ application/service ──→ domain/
interfaces/mcp   ──┘           │
                               ↓
                    infrastructure/persistence
                    infrastructure/external
```

---

## Implementation Phases

### Phase 1: Foundation (Day 1)
**Goal**: Add MCP without restructuring; validate it works.

#### 1.1 Add Spring AI MCP Dependency

**File**: `pom.xml`

```xml
<!-- Spring AI MCP Server for WebMVC -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-server-webmvc-spring-boot-starter</artifactId>
    <version>1.0.0-M6</version>
</dependency>
```

Add Spring AI BOM to `<dependencyManagement>`:

```xml
<dependencyManagement>
    <dependencies>
        <!-- Existing Sentry BOM -->
        <dependency>
            <groupId>io.sentry</groupId>
            <artifactId>sentry-bom</artifactId>
            <version>8.2.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- Spring AI BOM (NEW) -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.0.0-M6</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Add Spring Milestones repository (required for M6):

```xml
<repositories>
    <repository>
        <id>spring-milestones</id>
        <name>Spring Milestones</name>
        <url>https://repo.spring.io/milestone</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

#### 1.2 Configure MCP Server

**File**: `src/main/resources/application.properties`

```properties
# ================================
# MCP Server Configuration
# ================================
spring.ai.mcp.server.enabled=true
spring.ai.mcp.server.name=car-builder-vin
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.sse-message-endpoint=/mcp/messages
```

#### 1.3 Create First MCP Tool (Proof of Concept)

**File**: `src/main/java/com/sentinovo/carbuildervin/mcp/VinDecodingMcpTool.java`

```java
package com.sentinovo.carbuildervin.mcp;

import com.sentinovo.carbuildervin.dto.vin.VinDecodingResponse;
import com.sentinovo.carbuildervin.service.external.VinDecodingService;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VinDecodingMcpTool {

    private final VinDecodingService vinDecodingService;

    @Tool(name = "decode_vin", description = "Decode a Vehicle Identification Number (VIN) to get vehicle details including make, model, year, trim, engine, and transmission")
    public VinDecodingResponse decodeVin(
            @ToolParam(description = "17-character Vehicle Identification Number") String vin
    ) {
        log.info("MCP Tool: decode_vin called with VIN: {}", vin);
        return vinDecodingService.decodeVin(vin);
    }

    @Tool(name = "validate_vin", description = "Validate if a VIN has correct format and checksum")
    public VinValidationResult validateVin(
            @ToolParam(description = "VIN to validate") String vin
    ) {
        log.info("MCP Tool: validate_vin called with VIN: {}", vin);
        boolean isValid = vinDecodingService.isValidVin(vin);
        return new VinValidationResult(vin, isValid, isValid ? "Valid VIN format" : "Invalid VIN format");
    }

    public record VinValidationResult(String vin, boolean valid, String message) {}
}
```

#### 1.4 Test MCP Integration

After adding the above, start the app and verify:
1. App starts without errors
2. MCP endpoint is available at `/mcp/messages`
3. Tools are discoverable via MCP protocol

---

### Phase 2: Core MCP Tools (Day 2)
**Goal**: Expose primary business operations as MCP tools.

#### 2.1 Vehicle Management Tool

**File**: `src/main/java/com/sentinovo/carbuildervin/mcp/VehicleManagementMcpTool.java`

```java
package com.sentinovo.carbuildervin.mcp;

import com.sentinovo.carbuildervin.dto.vehicle.*;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleManagementMcpTool {

    private final VehicleService vehicleService;
    private final AuthenticationService authenticationService;

    @Tool(name = "list_vehicles", description = "List all vehicles for the current user")
    public List<VehicleDto> listVehicles() {
        UUID userId = authenticationService.getCurrentUserId();
        log.info("MCP Tool: list_vehicles for user {}", userId);
        return vehicleService.getUserVehicles(userId);
    }

    @Tool(name = "get_vehicle", description = "Get detailed information about a specific vehicle")
    public VehicleDto getVehicle(
            @ToolParam(description = "UUID of the vehicle") String vehicleId
    ) {
        log.info("MCP Tool: get_vehicle for ID {}", vehicleId);
        return vehicleService.getVehicleById(UUID.fromString(vehicleId));
    }

    @Tool(name = "create_vehicle", description = "Create a new vehicle entry. If VIN is provided, vehicle details will be auto-populated via VIN decoding.")
    public VehicleDto createVehicle(
            @ToolParam(description = "17-character VIN (optional for project vehicles)") String vin,
            @ToolParam(description = "Nickname for the vehicle (e.g., 'Daily Driver', 'Track Car')") String nickname,
            @ToolParam(description = "Additional notes about the vehicle") String notes
    ) {
        log.info("MCP Tool: create_vehicle with VIN {}", vin);
        VehicleCreateDto dto = new VehicleCreateDto();
        dto.setVin(vin);
        dto.setNickname(nickname);
        dto.setNotes(notes);
        return vehicleService.createVehicle(dto);
    }

    @Tool(name = "create_project_vehicle", description = "Create a vehicle entry without a VIN (for future builds or projects)")
    public VehicleDto createProjectVehicle(
            @ToolParam(description = "Vehicle year") Integer year,
            @ToolParam(description = "Vehicle make (e.g., Ford, Toyota)") String make,
            @ToolParam(description = "Vehicle model (e.g., Mustang, Camry)") String model,
            @ToolParam(description = "Vehicle trim (e.g., GT, XLE)") String trim,
            @ToolParam(description = "Nickname for the vehicle") String nickname
    ) {
        log.info("MCP Tool: create_project_vehicle {} {} {}", year, make, model);
        VehicleProjectCreateDto dto = new VehicleProjectCreateDto();
        dto.setYear(year);
        dto.setMake(make);
        dto.setModel(model);
        dto.setTrim(trim);
        dto.setNickname(nickname);
        return vehicleService.createProjectVehicle(dto);
    }

    @Tool(name = "archive_vehicle", description = "Archive a vehicle (soft delete)")
    public VehicleDto archiveVehicle(
            @ToolParam(description = "UUID of the vehicle to archive") String vehicleId
    ) {
        log.info("MCP Tool: archive_vehicle {}", vehicleId);
        return vehicleService.archiveVehicle(UUID.fromString(vehicleId));
    }
}
```

#### 2.2 Build Planning Tool

**File**: `src/main/java/com/sentinovo/carbuildervin/mcp/BuildPlanningMcpTool.java`

```java
package com.sentinovo.carbuildervin.mcp;

import com.sentinovo.carbuildervin.dto.build.*;
import com.sentinovo.carbuildervin.dto.upgrade.UpgradeCategoryDto;
import com.sentinovo.carbuildervin.service.vehicle.VehicleUpgradeService;
import com.sentinovo.carbuildervin.service.vehicle.UpgradeCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BuildPlanningMcpTool {

    private final VehicleUpgradeService vehicleUpgradeService;
    private final UpgradeCategoryService upgradeCategoryService;

    @Tool(name = "list_upgrade_categories", description = "List available upgrade categories (e.g., Engine, Suspension, Interior)")
    public List<UpgradeCategoryDto> listUpgradeCategories() {
        log.info("MCP Tool: list_upgrade_categories");
        return upgradeCategoryService.getActiveUpgradeCategories();
    }

    @Tool(name = "list_builds_for_vehicle", description = "List all upgrade/build plans for a specific vehicle")
    public List<VehicleUpgradeDto> listBuildsForVehicle(
            @ToolParam(description = "UUID of the vehicle") String vehicleId
    ) {
        log.info("MCP Tool: list_builds_for_vehicle {}", vehicleId);
        return vehicleUpgradeService.getVehicleUpgradesByVehicleId(UUID.fromString(vehicleId));
    }

    @Tool(name = "get_build_details", description = "Get detailed information about a specific build plan including all parts")
    public VehicleUpgradeDto getBuildDetails(
            @ToolParam(description = "UUID of the build/upgrade plan") String buildId
    ) {
        log.info("MCP Tool: get_build_details {}", buildId);
        return vehicleUpgradeService.getVehicleUpgradeById(UUID.fromString(buildId));
    }

    @Tool(name = "create_build_plan", description = "Create a new upgrade/build plan for a vehicle")
    public VehicleUpgradeDto createBuildPlan(
            @ToolParam(description = "UUID of the vehicle") String vehicleId,
            @ToolParam(description = "UUID of the upgrade category") String categoryId,
            @ToolParam(description = "Name of the build plan (e.g., 'Stage 2 Turbo Build')") String name,
            @ToolParam(description = "Description of what this build involves") String description,
            @ToolParam(description = "Target completion date (YYYY-MM-DD format)") String targetDate
    ) {
        log.info("MCP Tool: create_build_plan for vehicle {}", vehicleId);
        VehicleUpgradeCreateDto dto = new VehicleUpgradeCreateDto();
        dto.setUpgradeCategoryId(UUID.fromString(categoryId));
        dto.setName(name);
        dto.setDescription(description);
        if (targetDate != null && !targetDate.isEmpty()) {
            dto.setTargetCompletionDate(LocalDate.parse(targetDate));
        }
        return vehicleUpgradeService.createVehicleUpgrade(UUID.fromString(vehicleId), dto);
    }

    @Tool(name = "update_build_status", description = "Update the status of a build plan (PLANNED, IN_PROGRESS, COMPLETED, ON_HOLD)")
    public VehicleUpgradeDto updateBuildStatus(
            @ToolParam(description = "UUID of the build plan") String buildId,
            @ToolParam(description = "New status: PLANNED, IN_PROGRESS, COMPLETED, or ON_HOLD") String status
    ) {
        log.info("MCP Tool: update_build_status {} to {}", buildId, status);
        return vehicleUpgradeService.updateVehicleUpgradeStatusDto(UUID.fromString(buildId), status);
    }

    @Tool(name = "delete_build_plan", description = "Delete a build plan and all associated parts")
    public void deleteBuildPlan(
            @ToolParam(description = "UUID of the build plan to delete") String buildId
    ) {
        log.info("MCP Tool: delete_build_plan {}", buildId);
        vehicleUpgradeService.deleteUpgrade(UUID.fromString(buildId));
    }
}
```

#### 2.3 Part Management Tool

**File**: `src/main/java/com/sentinovo/carbuildervin/mcp/PartManagementMcpTool.java`

```java
package com.sentinovo.carbuildervin.mcp;

import com.sentinovo.carbuildervin.dto.parts.*;
import com.sentinovo.carbuildervin.dto.parts.lookup.*;
import com.sentinovo.carbuildervin.service.parts.*;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PartManagementMcpTool {

    private final PartService partService;
    private final SubPartService subPartService;
    private final PartCategoryService partCategoryService;
    private final PartTierService partTierService;
    private final AuthenticationService authenticationService;

    @Tool(name = "list_part_categories", description = "List available part categories (e.g., Turbo, Exhaust, Wheels)")
    public List<PartCategoryDto> listPartCategories() {
        log.info("MCP Tool: list_part_categories");
        return partCategoryService.getAllPartCategories();
    }

    @Tool(name = "list_part_tiers", description = "List available part tiers/quality levels (e.g., Budget, OEM, Premium)")
    public List<PartTierDto> listPartTiers() {
        log.info("MCP Tool: list_part_tiers");
        return partTierService.getAllPartTiers();
    }

    @Tool(name = "list_parts_for_build", description = "List all parts in a specific build plan")
    public List<PartDto> listPartsForBuild(
            @ToolParam(description = "UUID of the build/upgrade plan") String buildId
    ) {
        log.info("MCP Tool: list_parts_for_build {}", buildId);
        return partService.getPartsByUpgradeIdPaged(UUID.fromString(buildId), PageRequest.of(0, 100)).getItems();
    }

    @Tool(name = "get_part_details", description = "Get detailed information about a specific part")
    public PartDto getPartDetails(
            @ToolParam(description = "UUID of the part") String partId
    ) {
        log.info("MCP Tool: get_part_details {}", partId);
        return partService.getPartById(UUID.fromString(partId));
    }

    @Tool(name = "add_part_to_build", description = "Add a new part to a build plan")
    public PartDto addPartToBuild(
            @ToolParam(description = "UUID of the build plan") String buildId,
            @ToolParam(description = "Name of the part") String name,
            @ToolParam(description = "Brand/manufacturer") String brand,
            @ToolParam(description = "UUID of the part category") String categoryId,
            @ToolParam(description = "UUID of the part tier") String tierId,
            @ToolParam(description = "Price of the part") Double price,
            @ToolParam(description = "URL to product page") String productUrl,
            @ToolParam(description = "Whether this part is required for the build") Boolean isRequired,
            @ToolParam(description = "Notes about this part") String notes
    ) {
        log.info("MCP Tool: add_part_to_build {} - {}", buildId, name);
        PartCreateDto dto = new PartCreateDto();
        dto.setName(name);
        dto.setBrand(brand);
        dto.setPartCategoryId(UUID.fromString(categoryId));
        dto.setPartTierId(UUID.fromString(tierId));
        dto.setPrice(price != null ? BigDecimal.valueOf(price) : null);
        dto.setProductUrl(productUrl);
        dto.setIsRequired(isRequired != null ? isRequired : false);
        dto.setNotes(notes);
        return partService.createPart(UUID.fromString(buildId), dto);
    }

    @Tool(name = "update_part_status", description = "Update the status of a part (PLANNED, ORDERED, SHIPPED, INSTALLED)")
    public PartDto updatePartStatus(
            @ToolParam(description = "UUID of the part") String partId,
            @ToolParam(description = "New status: PLANNED, ORDERED, SHIPPED, or INSTALLED") String status
    ) {
        log.info("MCP Tool: update_part_status {} to {}", partId, status);
        return partService.updatePartStatusDto(UUID.fromString(partId), status);
    }

    @Tool(name = "calculate_build_cost", description = "Calculate the total cost of all parts in a build")
    public BuildCostResult calculateBuildCost(
            @ToolParam(description = "UUID of the build plan") String buildId
    ) {
        log.info("MCP Tool: calculate_build_cost {}", buildId);
        BigDecimal totalCost = partService.calculateTotalCostByUpgrade(UUID.fromString(buildId));
        return new BuildCostResult(buildId, totalCost, "USD");
    }

    @Tool(name = "delete_part", description = "Remove a part from a build plan")
    public void deletePart(
            @ToolParam(description = "UUID of the part to delete") String partId
    ) {
        log.info("MCP Tool: delete_part {}", partId);
        partService.deletePart(UUID.fromString(partId));
    }

    public record BuildCostResult(String buildId, BigDecimal totalCost, String currency) {}
}
```

#### 2.4 Budget Analysis Tool

**File**: `src/main/java/com/sentinovo/carbuildervin/mcp/BudgetAnalysisMcpTool.java`

```java
package com.sentinovo.carbuildervin.mcp;

import com.sentinovo.carbuildervin.dto.budget.*;
import com.sentinovo.carbuildervin.service.parts.PartService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleService;
import com.sentinovo.carbuildervin.service.vehicle.VehicleUpgradeService;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetAnalysisMcpTool {

    private final PartService partService;
    private final VehicleUpgradeService vehicleUpgradeService;
    private final AuthenticationService authenticationService;

    @Tool(name = "get_total_spending", description = "Get total spending across all vehicles and builds for the current user")
    public TotalSpendingResult getTotalSpending() {
        UUID userId = authenticationService.getCurrentUserId();
        log.info("MCP Tool: get_total_spending for user {}", userId);
        BigDecimal total = partService.calculateTotalCostByUser(userId);
        return new TotalSpendingResult(total, "USD");
    }

    @Tool(name = "get_build_budget_breakdown", description = "Get a cost breakdown for a specific build by category and tier")
    public BuildBudgetBreakdown getBuildBudgetBreakdown(
            @ToolParam(description = "UUID of the build plan") String buildId
    ) {
        log.info("MCP Tool: get_build_budget_breakdown {}", buildId);
        // This would aggregate costs by category and tier
        // Implementation depends on existing service methods
        BigDecimal totalCost = partService.calculateTotalCostByUpgrade(UUID.fromString(buildId));
        return new BuildBudgetBreakdown(buildId, totalCost, "USD");
    }

    public record TotalSpendingResult(BigDecimal totalSpent, String currency) {}
    public record BuildBudgetBreakdown(String buildId, BigDecimal totalCost, String currency) {}
}
```

---

### Phase 3: Security & Auth for MCP (Day 3)
**Goal**: Secure MCP endpoints properly.

#### 3.1 MCP Security Configuration

**File**: `src/main/java/com/sentinovo/carbuildervin/config/McpSecurityConfig.java`

```java
package com.sentinovo.carbuildervin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class McpSecurityConfig {

    @Bean
    @Order(1) // Higher priority than main security config
    public SecurityFilterChain mcpSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/mcp/**")
            .authorizeHttpRequests(authz -> authz
                // Option 1: API Key authentication
                // .requestMatchers("/mcp/**").hasAuthority("MCP_ACCESS")

                // Option 2: Allow from internal network only (configure in firewall)
                .requestMatchers("/mcp/**").permitAll()
            )
            .csrf(csrf -> csrf.disable()) // MCP uses its own security
            .sessionManagement(session -> session.disable());

        return http.build();
    }
}
```

#### 3.2 API Key Authentication (Optional but Recommended)

**File**: `src/main/java/com/sentinovo/carbuildervin/config/McpApiKeyFilter.java`

```java
package com.sentinovo.carbuildervin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class McpApiKeyFilter extends OncePerRequestFilter {

    @Value("${mcp.api.key:}")
    private String expectedApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getRequestURI().startsWith("/mcp/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip if no API key configured (development mode)
        if (expectedApiKey == null || expectedApiKey.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader("X-MCP-API-Key");
        if (expectedApiKey.equals(providedKey)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or missing MCP API key");
        }
    }
}
```

Add to `.env.example`:
```properties
# MCP API Key (for securing MCP endpoints)
MCP_API_KEY=your-secure-mcp-api-key
```

---

### Phase 4: Hexagonal Restructure (Day 4-5)
**Goal**: Reorganize packages for cleaner architecture.

#### 4.1 Package Migration Plan

This is a refactoring task that can be done incrementally:

| Current Location | New Location | Notes |
|-----------------|--------------|-------|
| `entities/**` | `domain/model/**` | Rename package |
| `repository/**` | `domain/repository/**` (interfaces) + `infrastructure/persistence/**` (impl) | Split interfaces from JPA |
| `service/**` | `application/service/**` | Move business logic |
| `controller/auth/**` | `interfaces/rest/auth/**` | REST controllers |
| `controller/vehicle/**` | `interfaces/rest/vehicle/**` | REST controllers |
| `controller/parts/**` | `interfaces/rest/parts/**` | REST controllers |
| `controller/lookup/**` | `interfaces/rest/lookup/**` | REST controllers |
| `controller/web/**` | `interfaces/web/**` | Thymeleaf controllers |
| `mcp/**` | `interfaces/mcp/**` | MCP tools |
| `config/**` | `infrastructure/config/**` | Spring configs |
| `exception/**` + `exceptions/**` | `domain/exception/**` | Consolidate |
| `dto/**` | `application/dto/**` | Keep with services |
| `mapper/**` | `infrastructure/persistence/mapper/**` | Persistence layer |
| `validation/**` | `domain/validation/**` | Domain validation |

#### 4.2 Migration Steps (Safe Incremental Approach)

1. **Create new package structure** (empty packages first)
2. **Move domain layer** (entities, exceptions) - no dependencies on other layers
3. **Move application layer** (services, DTOs) - depends only on domain
4. **Move infrastructure layer** (repos, mappers, configs, external services)
5. **Move interfaces layer** (controllers, MCP tools)
6. **Update imports** throughout
7. **Run tests** after each step

#### 4.3 IDE Refactoring Commands

Using IntelliJ IDEA:
- Right-click package → Refactor → Move Package
- This automatically updates all imports

---

## MCP Tools Summary

| Tool Name | Service | Operations |
|-----------|---------|------------|
| `decode_vin` | VinDecodingService | Decode VIN to vehicle specs |
| `validate_vin` | VinDecodingService | Check VIN format validity |
| `list_vehicles` | VehicleService | Get user's vehicles |
| `get_vehicle` | VehicleService | Get vehicle details |
| `create_vehicle` | VehicleService | Create with VIN decode |
| `create_project_vehicle` | VehicleService | Create without VIN |
| `archive_vehicle` | VehicleService | Soft delete |
| `list_upgrade_categories` | UpgradeCategoryService | Get categories |
| `list_builds_for_vehicle` | VehicleUpgradeService | Get builds |
| `get_build_details` | VehicleUpgradeService | Build info |
| `create_build_plan` | VehicleUpgradeService | New build |
| `update_build_status` | VehicleUpgradeService | Status change |
| `delete_build_plan` | VehicleUpgradeService | Remove build |
| `list_part_categories` | PartCategoryService | Get categories |
| `list_part_tiers` | PartTierService | Get tiers |
| `list_parts_for_build` | PartService | Get parts |
| `get_part_details` | PartService | Part info |
| `add_part_to_build` | PartService | Add part |
| `update_part_status` | PartService | Status change |
| `calculate_build_cost` | PartService | Cost aggregation |
| `delete_part` | PartService | Remove part |
| `get_total_spending` | PartService | User totals |
| `get_build_budget_breakdown` | PartService | Cost breakdown |

---

## Environment Configuration

### Development `.env`
```properties
# MCP Configuration
MCP_API_KEY=dev-key-for-testing

# Existing configs...
```

### Production `.env`
```properties
# MCP Configuration (use strong key)
MCP_API_KEY=<generate-secure-random-key>

# Existing configs...
```

### Docker Compose Addition

**File**: `compose.prod.yaml`

Add to environment section:
```yaml
- MCP_API_KEY=${MCP_API_KEY:}
```

---

## Testing Strategy

### 1. Unit Tests for MCP Tools
```java
@ExtendWith(MockitoExtension.class)
class VehicleManagementMcpToolTest {
    @Mock VehicleService vehicleService;
    @Mock AuthenticationService authService;
    @InjectMocks VehicleManagementMcpTool tool;

    @Test
    void listVehicles_returnsUserVehicles() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(authService.getCurrentUserId()).thenReturn(userId);
        when(vehicleService.getUserVehicles(userId)).thenReturn(List.of(...));

        // Act
        List<VehicleDto> result = tool.listVehicles();

        // Assert
        assertThat(result).hasSize(1);
    }
}
```

### 2. Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
class McpIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void mcpEndpoint_isAvailable() throws Exception {
        mockMvc.perform(get("/mcp/messages"))
            .andExpect(status().isOk());
    }
}
```

### 3. Manual Testing with MCP Client
Use the MCP Inspector or a custom client to test tool discovery and execution.

---

## Deployment Considerations

### Network Security
- **Recommended**: Keep MCP endpoint internal (VPC/private network)
- **If public**: Use strong API key + rate limiting + IP allowlisting

### Monitoring
- Add Sentry spans for MCP tool executions
- Log all MCP tool calls with user context

### Scaling
- MCP tools are stateless - horizontal scaling works
- Consider caching for lookup operations (categories, tiers)

---

## Rollback Plan

If MCP integration causes issues:
1. Set `spring.ai.mcp.server.enabled=false`
2. Remove `@Component` from MCP tool classes
3. Redeploy

The hexagonal restructure is purely organizational - rollback by reverting package moves if needed.

---

## Success Criteria

- [ ] MCP server starts and exposes tools
- [ ] VIN decoding works via MCP
- [ ] Vehicle CRUD works via MCP
- [ ] Build planning works via MCP
- [ ] Part management works via MCP
- [ ] All existing REST endpoints still work
- [ ] All existing web UI still works
- [ ] Authorization is enforced (users only see their own data)
- [ ] Tests pass
