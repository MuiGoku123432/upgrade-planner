````markdown
# VIN Build Planner – Internal REST API Specification (v1)

This document describes the **RESTful backend API** for the VIN Build Planner microservice.

- The **backend** is a pure JSON REST API (no HTML rendering).
- The **frontend** (HTMX + Thymeleaf or other templates) is a separate app that consumes this API.
- Auth, vehicles, builds, parts, and sub-parts are all exposed via this API.

Base URL (example):  
`https://api.yourdomain.com/api/v1`

All JSON examples are illustrative.

---

## 1. Conventions

- **Content-Type:** `application/json`
- **Authentication:** `Authorization: Bearer <JWT>` (for authenticated routes)
- **Timestamps:** ISO 8601 strings (`"2025-11-22T15:30:00Z"`)

### 1.1 Standard Error Format

On error, API returns:

```json
{
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Vehicle not found",
    "details": {
      "vehicleId": "..."
    }
  }
}
````

Common error codes:

* `VALIDATION_ERROR`
* `UNAUTHORIZED`
* `FORBIDDEN`
* `RESOURCE_NOT_FOUND`
* `CONFLICT`
* `EXTERNAL_SERVICE_ERROR`
* `INTERNAL_ERROR`

---

## 2. Authentication & Users

### 2.1 Register

**POST** `/auth/register`

Create a new user account.

**Request**

```json
{
  "username": "connor",
  "email": "connor@example.com",
  "password": "StrongPassword123!",
  "displayName": "Connor"
}
```

**Response 201**

```json
{
  "id": "f2d9b6c3-8713-4fca-b33e-9c38e8d897aa",
  "username": "connor",
  "email": "connor@example.com",
  "displayName": "Connor",
  "createdAt": "2025-11-22T15:30:00Z"
}
```

---

### 2.2 Login

**POST** `/auth/login`

**Request**

```json
{
  "usernameOrEmail": "connor",
  "password": "StrongPassword123!"
}
```

**Response 200**

```json
{
  "accessToken": "jwt-token-here",
  "tokenType": "Bearer"
}
```

---

### 2.3 Current User

**GET** `/me`
Requires auth.

**Response 200**

```json
{
  "id": "f2d9b6c3-8713-4fca-b33e-9c38e8d897aa",
  "username": "connor",
  "email": "connor@example.com",
  "displayName": "Connor",
  "roles": ["ROLE_USER"],
  "createdAt": "2025-11-22T15:30:00Z"
}
```

---

## 3. VIN Decode (MarketCheck Integration)

This endpoint wraps the external MarketCheck Basic VIN Decoder.

### 3.1 Decode VIN

**POST** `/vin/decode`

**Request**

```json
{
  "vin": "JTEVA5AR9S5004482"
}
```

**Response 200 (simplified)**

```json
{
  "vin": "JTEVA5AR9S5004482",
  "isValid": true,
  "year": 2021,
  "make": "Toyota",
  "model": "4Runner",
  "trim": "TRD Off Road",
  "bodyType": "SUV",
  "vehicleType": "Truck",
  "transmission": "Automatic",
  "drivetrain": "4WD",
  "fuelType": "Gasoline",
  "engine": "4.0L V6",
  "engineSize": 4.0,
  "doors": 4,
  "cylinders": 6,
  "madeIn": "Japan"
}
```

**Error cases**

* Invalid VIN format → `400 VALIDATION_ERROR`
* VIN cannot be decoded → `422 VALIDATION_ERROR`
* External service error → `503 EXTERNAL_SERVICE_ERROR`

This endpoint **does not** create a vehicle by itself; the frontend can:

1. Call `/vin/decode` to get data.
2. Use that payload to pre-fill a **vehicle create** form.
3. Then call `POST /vehicles`.

---

## 4. Vehicles

Vehicles may or may not have a VIN (supports project/future rigs).

### Vehicle DTO

```json
{
  "id": "uuid",
  "ownerId": "uuid",
  "vin": "JTEVA5AR9S5004482",
  "year": 2021,
  "make": "Toyota",
  "model": "4Runner",
  "trim": "TRD Off Road",
  "nickname": "Trail Rig",
  "notes": "Primary overland rig",
  "isArchived": false,
  "createdAt": "2025-11-22T15:30:00Z",
  "updatedAt": "2025-11-22T15:30:00Z"
}
```

### 4.1 List Vehicles

**GET** `/vehicles`
Requires auth. Returns vehicles for the **current user**.

**Query params (optional):**

* `vin` – exact match.
* `make`, `model`, `year`.
* `includeArchived` – `true|false` (default `false`).

**Response 200**

```json
{
  "items": [ /* Vehicle DTOs */ ],
  "total": 3
}
```

---

### 4.2 Get Vehicle by ID

**GET** `/vehicles/{vehicleId}`

**Response 200**

```json
{
  "id": "uuid",
  "ownerId": "uuid",
  "vin": "JTEVA5AR9S5004482",
  "year": 2021,
  "make": "Toyota",
  "model": "4Runner",
  "trim": "TRD Off Road",
  "nickname": "Trail Rig",
  "notes": "Primary overland rig",
  "isArchived": false,
  "createdAt": "2025-11-22T15:30:00Z",
  "updatedAt": "2025-11-22T15:30:00Z",
  "builds": [ /* optional summary of vehicle_upgrade */ ]
}
```

---

### 4.3 Create Vehicle

**POST** `/vehicles`

VIN is **optional**.

**Request**

```json
{
  "vin": "JTEVA5AR9S5004482",
  "year": 2021,
  "make": "Toyota",
  "model": "4Runner",
  "trim": "TRD Off Road",
  "nickname": "Trail Rig",
  "notes": "Going full overland build"
}
```

OR a VIN-less project:

```json
{
  "vin": null,
  "year": null,
  "make": "Toyota",
  "model": "Land Cruiser",
  "trim": null,
  "nickname": "Future 80 Series",
  "notes": "Planning stage only"
}
```

**Response 201**

Returns the created Vehicle DTO.

---

### 4.4 Update Vehicle

**PATCH** `/vehicles/{vehicleId}`

Any subset of fields:

```json
{
  "vin": "JTEVA5AR9S5004482",
  "nickname": "Overlander v2",
  "notes": "Updated plans"
}
```

**Response 200**

Vehicle DTO after update.

---

### 4.5 Archive/Unarchive Vehicle

**PATCH** `/vehicles/{vehicleId}/archive`

**Request**

```json
{
  "archived": true
}
```

**Response 200**

Vehicle DTO with updated `isArchived`.

---

### 4.6 Delete Vehicle

**DELETE** `/vehicles/{vehicleId}`

* Either physical delete or soft-delete (implementation detail).
* Must also handle cascading builds/parts/sub-parts as per business rules.

**Response 204** – No content.

---

## 5. Upgrade Categories (Lookup)

These are global categories like Overlanding, Performance, etc.

### 5.1 List Upgrade Categories

**GET** `/upgrade-categories`

**Response 200**

```json
{
  "items": [
    {
      "id": 1,
      "key": "OVERLANDING",
      "name": "Overlanding",
      "description": "Camping and trail capability focused build",
      "sortOrder": 1,
      "isActive": true
    }
  ]
}
```

---

## 6. Vehicle Builds (vehicle_upgrade)

A “build” is a plan for a given vehicle & upgrade category.

### Build DTO

```json
{
  "id": "uuid",
  "vehicleId": "uuid",
  "upgradeCategory": {
    "id": 1,
    "key": "OVERLANDING",
    "name": "Overlanding"
  },
  "name": "Overland Build v1",
  "slug": "overland-build-v1",
  "description": "Mild overland setup.",
  "priorityLevel": 1,
  "targetCompletionDate": "2026-06-01",
  "status": "PLANNED",
  "isPrimaryForCategory": true,
  "createdAt": "2025-11-22T15:30:00Z",
  "updatedAt": "2025-11-22T15:30:00Z"
}
```

---

### 6.1 List Builds for a Vehicle

**GET** `/vehicles/{vehicleId}/builds`

**Response 200**

```json
{
  "items": [ /* Build DTOs */ ]
}
```

---

### 6.2 Get Build by ID

**GET** `/builds/{buildId}`

**Response 200**

```json
{
  "build": { /* Build DTO */ },
  "parts": [ /* Part DTOs (optional summary) */ ]
}
```

---

### 6.3 Create Build

**POST** `/vehicles/{vehicleId}/builds`

**Request**

```json
{
  "upgradeCategoryId": 1,
  "name": "Overland Build v1",
  "description": "Intro overland setup",
  "priorityLevel": 1,
  "targetCompletionDate": "2026-06-01",
  "status": "PLANNED",
  "isPrimaryForCategory": true
}
```

**Response 201**

Build DTO.

---

### 6.4 Update Build

**PATCH** `/builds/{buildId}`

```json
{
  "name": "Overland Build v2",
  "priorityLevel": 2,
  "status": "IN_PROGRESS"
}
```

**Response 200** – Build DTO.

---

### 6.5 Delete Build

**DELETE** `/builds/{buildId}`

**Response 204**

---

## 7. Part Categories & Tiers (Lookups)

### 7.1 List Part Categories

**GET** `/part-categories`

**Response 200**

```json
{
  "items": [
    {
      "code": "SUSPENSION",
      "label": "Suspension",
      "description": "Coils, shocks, leaf springs, control arms",
      "sortOrder": 1
    }
  ]
}
```

### 7.2 List Part Tiers

**GET** `/part-tiers`

```json
{
  "items": [
    {
      "code": "BUDGET",
      "label": "Budget",
      "rank": 1,
      "description": "Entry-level, cost-effective parts"
    },
    {
      "code": "PREMIUM",
      "label": "Premium",
      "rank": 3,
      "description": "High-end parts"
    }
  ]
}
```

---

## 8. Parts

Each part belongs to a **build** and has a numeric priority plus category and tier.

### Part DTO

```json
{
  "id": "uuid",
  "vehicleUpgradeId": "uuid",
  "name": "OME BP-51 Suspension Kit",
  "brand": "Old Man Emu",
  "categoryCode": "SUSPENSION",
  "tierCode": "PREMIUM",
  "productUrl": "https://example.com/ome-bp51",
  "price": 2800.00,
  "currencyCode": "USD",
  "isRequired": true,
  "status": "PLANNED",
  "priorityValue": 1,
  "targetPurchaseDate": "2025-03-01",
  "sortOrder": 10,
  "notes": "Need alignment after install",
  "createdAt": "2025-11-22T15:30:00Z",
  "updatedAt": "2025-11-22T15:30:00Z"
}
```

---

### 8.1 List Parts in a Build

**GET** `/builds/{buildId}/parts`

**Query filters (optional):**

* `categoryCode`
* `tierCode`
* `status`
* `minPriority` / `maxPriority`
* `required` (`true|false`)

**Response 200**

```json
{
  "items": [ /* Part DTOs */ ],
  "total": 5
}
```

---

### 8.2 Get Part by ID

**GET** `/parts/{partId}`

**Response 200**

```json
{
  "part": { /* Part DTO */ },
  "subParts": [ /* Sub-part DTOs (optional) */ ]
}
```

---

### 8.3 Create Part

**POST** `/builds/{buildId}/parts`

**Request**

```json
{
  "name": "Front Bumper",
  "brand": "C4 Fabrication",
  "categoryCode": "ARMOR",
  "tierCode": "PREMIUM",
  "productUrl": "https://example.com/c4-bumper",
  "price": 1500.00,
  "isRequired": true,
  "status": "PLANNED",
  "priorityValue": 2,
  "targetPurchaseDate": "2025-04-15",
  "notes": "Requires trimming plastic"
}
```

**Response 201** – Part DTO.

---

### 8.4 Update Part

**PATCH** `/parts/{partId}`

```json
{
  "status": "ORDERED",
  "priorityValue": 1,
  "targetPurchaseDate": "2025-03-10"
}
```

**Response 200** – Part DTO.

---

### 8.5 Delete Part

**DELETE** `/parts/{partId}`

**Response 204**

---

## 9. Sub-Parts

Sub-parts belong to a parent part and inherit context from that part & build.

### Sub-Part DTO

```json
{
  "id": "uuid",
  "parentPartId": "uuid",
  "name": "Rear Shock Assembly",
  "brand": "Old Man Emu",
  "categoryCode": "SUSPENSION",
  "tierCode": "PREMIUM",
  "productUrl": "https://example.com/rear-shock",
  "price": 600.00,
  "currencyCode": "USD",
  "isRequired": true,
  "status": "PLANNED",
  "priorityValue": 3,
  "targetPurchaseDate": "2025-05-01",
  "sortOrder": 1,
  "notes": "Comes as a pair",
  "createdAt": "2025-11-22T15:30:00Z",
  "updatedAt": "2025-11-22T15:30:00Z"
}
```

---

### 9.1 List Sub-Parts for a Part

**GET** `/parts/{partId}/sub-parts`

**Response 200**

```json
{
  "items": [ /* Sub-Part DTOs */ ]
}
```

---

### 9.2 Create Sub-Part

**POST** `/parts/{partId}/sub-parts`

**Request**

```json
{
  "name": "Hardware Kit",
  "brand": "C4 Fabrication",
  "categoryCode": "HARDWARE",
  "tierCode": "MID",
  "productUrl": "https://example.com/hardware-kit",
  "price": 120.00,
  "isRequired": false,
  "status": "PLANNED",
  "priorityValue": 4,
  "targetPurchaseDate": "2025-06-01",
  "notes": "Optional upgraded hardware"
}
```

**Response 201** – Sub-Part DTO.

---

### 9.3 Update Sub-Part

**PATCH** `/sub-parts/{subPartId}`

```json
{
  "status": "INSTALLED",
  "priorityValue": 5
}
```

**Response 200** – Sub-Part DTO.

---

### 9.4 Delete Sub-Part

**DELETE** `/sub-parts/{subPartId}`

**Response 204**

---

## 10. Aggregates & Summaries (Optional but Recommended)

### 10.1 Build Cost Summary

**GET** `/builds/{buildId}/summary`

**Response 200**

```json
{
  "buildId": "uuid",
  "totalRequiredCost": 7300.00,
  "totalOptionalCost": 1200.00,
  "currencyCode": "USD",
  "byCategory": [
    {
      "categoryCode": "SUSPENSION",
      "requiredCost": 3400.00,
      "optionalCost": 0.00
    },
    {
      "categoryCode": "ARMOR",
      "requiredCost": 2500.00,
      "optionalCost": 800.00
    }
  ],
  "byTier": [
    {
      "tierCode": "BUDGET",
      "cost": 500.00
    },
    {
      "tierCode": "PREMIUM",
      "cost": 6800.00
    }
  ]
}
```

This is ideal for your UI to show “what’s this build going to cost me” at a glance.

---

## 11. How HTMX Frontend Uses This Backend

* Frontend lives separately (e.g., `https://app.yourdomain.com`).
* HTMX requests hit the **frontend server**, which:

    * Calls this backend API.
    * Renders HTML fragments from the JSON responses.
* Alternatively:

    * HTMX can call the backend API directly (CORS-enabled).
    * A small piece of JavaScript or the frontend server transforms JSON → HTML.

Either way, **all state and operations** (vehicles, builds, parts, sub-parts, VIN decode) are owned by this REST API, and the HTMX frontend is just a consumer.

---

```
::contentReference[oaicite:0]{index=0}
```
