````markdown
# External API Documentation – MarketCheck Basic VIN Decoder

This document describes how our VIN Build Planner service integrates with the **MarketCheck Basic VIN Decoder API** to retrieve core vehicle specifications from a VIN.

---

## 1. Overview

We use the **Basic VIN Decoder** from MarketCheck to:

- Validate 17-character VINs.
- Retrieve essential vehicle specs (year, make, model, trim, etc.).
- Pre-fill our internal `vehicle` records to reduce manual data entry.

**External Provider:** MarketCheck  
**API Doc:** https://docs.marketcheck.com/docs/api/cars/vehicle-specs/basic  
**Base URL:** `https://api.marketcheck.com`

---

## 2. Endpoint Summary

### 2.1 Decode Basic Vehicle Specs by VIN

**Method:** `GET`  
**Path:** `/v2/decode/car/{vin}/specs` :contentReference[oaicite:0]{index=0}  

This endpoint decodes a **17-character VIN** and returns a JSON object containing basic vehicle specifications.

#### Request URL Format

```http
GET https://api.marketcheck.com/v2/decode/car/{vin}/specs?api_key={API_KEY}
````

* `{vin}` – 17-character VIN (path parameter).
* `api_key` – MarketCheck API key (query parameter).

---

## 3. Authentication

The Basic VIN Decoder requires a valid **MarketCheck API key**.

* **Parameter:** `api_key`
* **Location:** Query string
* **Type:** `string`
* **Required:** Yes (unless OAuth is used)

Example:

```http
GET /v2/decode/car/1FAHP3F28CL148530/specs?api_key=YOUR_API_KEY
Host: api.marketcheck.com
Accept: application/json
```

Our service will inject the API key from secure configuration (e.g., environment variable) and never expose it to the frontend.

---

## 4. Request Details

### 4.1 Path Parameters

* `vin` (string, required) ([docs.marketcheck.com][1])

    * Must be exactly **17 characters**.
    * Case-insensitive.
    * No “squish VIN” support (must be full VIN).

### 4.2 Query Parameters

* `api_key` (string, required) – MarketCheck API key. ([docs.marketcheck.com][1])

No other query parameters are required for this endpoint.

---

## 5. Response Schema

On success, the endpoint returns JSON with the following structure: ([docs.marketcheck.com][1])

```ts
interface MarketCheckBasicVinResponse {
  is_valid: boolean;        // Whether the VIN is valid and decodable
  decode_mode: string;      // Mode used for decoding (e.g. "full")
  year: number;             // Model year of the vehicle
  make: string;             // Manufacturer name
  model: string;            // Model name
  trim: string;             // Trim level or variant
  body_type: string;        // Body style (sedan, SUV, etc.)
  vehicle_type: string;     // Vehicle classification
  transmission: string;     // Transmission type
  drivetrain: string;       // Drivetrain configuration (FWD, AWD, etc.)
  fuel_type: string;        // Fuel type (gasoline, diesel, etc.)
  engine: string;           // Engine description
  engine_size: number;      // Engine displacement in liters
  doors: number;            // Number of doors
  cylinders: number;        // Number of cylinders
  made_in: string;          // Country of manufacture
  overall_height: string;   // Vehicle height
  overall_length: string;   // Vehicle length
  overall_width: string;    // Vehicle width
  std_seating: string;      // Standard seating capacity
  highway_mpg: number;      // Highway fuel economy
  city_mpg: number;         // City fuel economy
  powertrain_type: string;  // Powertrain classification
}
```

> **Note:** Not all fields are guaranteed to be non-null for every VIN. Our service should handle missing fields gracefully.

---

## 6. HTTP Status Codes

The API may return the following status codes: ([docs.marketcheck.com][1])

* **200 OK** – VIN decoded successfully, response body contains specs.
* **400 Bad Request** – Invalid VIN format or missing required parameters.
* **401 Unauthorized** – Missing or invalid `api_key`.
* **403 Forbidden** – API key does not have access to this endpoint.
* **422 Unprocessable Entity** – VIN is invalid or cannot be decoded.
* **429 Too Many Requests** – Rate limit exceeded.
* **500 Internal Server Error** – Temporary server error on provider side.
* **502 Bad Gateway** – Upstream service error.
* **503 Service Unavailable** – Maintenance or downtime.

Our service should map these errors into internal error codes/messages suitable for the frontend (e.g., “Invalid VIN”, “VIN not found”, “External VIN service unavailable”, etc.).

---

## 7. Usage in Our Service

### 7.1 When We Call This Endpoint

We will call the MarketCheck Basic VIN Decoder when:

1. A user creates or edits a vehicle and **provides a VIN**.
2. The user explicitly requests to “Decode VIN” to auto-fill vehicle information.

If the VIN is missing or the user is creating a “VIN-less” vehicle (e.g., future project rig), this external API will **not** be called.

### 7.2 Mapping to Internal Model

Given a successful response, we will map fields as follows:

* `vehicle.vin` ← input VIN (not from response)
* `vehicle.year` ← `year`
* `vehicle.make` ← `make`
* `vehicle.model` ← `model`
* `vehicle.trim` ← `trim`
* (Optional fields kept for display only or future use in our schema):

    * `body_type`, `vehicle_type`, `transmission`, `drivetrain`,
      `fuel_type`, `engine`, `engine_size`, `doors`, `cylinders`,
      `made_in`, dimensions, seating, mpg, `powertrain_type`.

If `is_valid == false`, we treat this as a “failed decode” and return an error or warning to the user instead of auto-filling.

### 7.3 Example Request/Response

#### Request

```http
GET https://api.marketcheck.com/v2/decode/car/JTEVA5AR9S5004482/specs?api_key=YOUR_API_KEY
Accept: application/json
```

#### Example Response (simplified)

```json
{
  "is_valid": true,
  "decode_mode": "full",
  "year": 2021,
  "make": "Toyota",
  "model": "4Runner",
  "trim": "TRD Off Road",
  "body_type": "SUV",
  "vehicle_type": "Truck",
  "transmission": "Automatic",
  "drivetrain": "4WD",
  "fuel_type": "Gasoline",
  "engine": "4.0L V6",
  "engine_size": 4.0,
  "doors": 4,
  "cylinders": 6,
  "made_in": "Japan",
  "overall_height": "71.5 in",
  "overall_length": "191.3 in",
  "overall_width": "75.8 in",
  "std_seating": "5",
  "highway_mpg": 19,
  "city_mpg": 16,
  "powertrain_type": "ICE"
}
```

---

## 8. Error Handling Strategy (Internal)

Internally, we will:

* Validate VIN format (length 17, basic character set) **before** calling MarketCheck where possible, to avoid unnecessary requests.
* Handle HTTP errors from MarketCheck and convert them into high-level errors:

    * **Invalid VIN** (400 / 422, or `is_valid == false`)
    * **Auth/config issue** (401 / 403)
    * **Rate limiting** (429)
    * **Service unavailable** (5xx)

We will log the full error details server-side but only return user-friendly messages to the frontend.

---

## 9. Rate Limiting & Performance Considerations

* Calls to MarketCheck should be **limited** to:

    * When a user enters/updates a VIN and explicitly requests decoding.
* Consider adding:

    * Lightweight caching of decoded VINs (e.g., by VIN string) to reduce repeated calls.
    * A short timeout and retry/backoff policy for transient 5xx errors.

---

## 10. Security Considerations

* The MarketCheck `api_key` is stored securely (e.g., environment variable or secret manager).
* The frontend **never** sees the raw key.
* All requests to the external API must be made server-side over HTTPS only.
* Logs should not contain the raw API key.

---

```
::contentReference[oaicite:5]{index=5}
```

[1]: https://docs.marketcheck.com/docs/api/cars/vehicle-specs/basic "Basic VIN Decoder - Cars APIs | MarketCheck Documentation"
