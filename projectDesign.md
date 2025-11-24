## 1. Service Name & Purpose

**Name (working):** VIN Build Planner Service

**Purpose:**
Provide a backend service that:

* Tracks vehicles by VIN.
* Organizes one or more “builds” (upgrade plans) per vehicle.
* Stores parts and nested sub-parts for each build, with links, pricing, categories, and tiers.
* Exposes this data in a way that a simple web UI (HTMX + Pico) can use to view and manage everything.

---

## 2. Users & Use Cases (High-Level)

**Primary user:**

* Enthusiast/owner or builder who wants to plan and document upgrade paths for specific vehicles.

**Core use cases:**

1. Add a vehicle by VIN and basic info.
2. Create one or more upgrade “builds” for that vehicle (e.g., Overlanding, Performance).
3. Add parts to each build, with links, prices, and type information.
4. Add sub-parts under a part for more granular breakdown.
5. View and compare cost and composition of each build.
6. Optionally leverage VIN decoding to auto-fill vehicle details.

---

## 3. Core Domain Concepts

1. **Vehicle**

    * Identified by VIN.
    * Has decoded or manually entered details (year, make, model, trim).
    * Can have multiple associated “builds.”

2. **Upgrade Category**

    * A label representing the purpose/role of an upgrade (e.g., Overlanding, Performance, Towing, Daily Driver).
    * May be global (shared across all vehicles) or defined per vehicle.

3. **Vehicle Build (Vehicle Upgrade)**

    * A specific upgrade plan tied to a vehicle and a category.
    * Example: “Overland Build v1” for a specific 2012 BMW 328i.
    * Contains a list of parts and their sub-parts.
    * Has notes and an overall purpose/description.

4. **Part**

    * A major component in a build (e.g., suspension kit, bumper, wheels, clutch).
    * Has:

        * Name, brand
        * Category (e.g., Suspension, Armor, Drivetrain, Interior, Electronics)
        * Tier (e.g., Budget, Mid, Premium, Race)
        * Product link (URL)
        * Price and currency
        * Optional notes

5. **Sub-Part**

    * A child component under a part, used to break down kits or optional add-ons.
    * Has the same basic attributes as a part (name, link, price, category, tier, notes).
    * Exists purely in the context of its parent part.

---

## 4. Functional Responsibilities

### 4.1 Vehicle Management

The service **must**:

1. Allow creation of a vehicle by VIN, with optional year/make/model/trim.
2. Allow updating basic vehicle info (if VIN decode changes or manual corrections are needed).
3. Allow listing all vehicles.
4. Allow retrieving a single vehicle and all associated builds.
5. Prevent duplicate VINs or handle them deterministically (e.g., one canonical record per VIN).

**In an ideal world, the service also:**

6. Integrates with an external VIN decoding API to fetch year/make/model/trim and populate those fields automatically (while allowing manual override).
7. Flags invalid VINs and returns validation errors.

---

### 4.2 Upgrade Category & Build Management

The service **must**:

1. Maintain a set of **upgrade categories** (e.g., Overlanding, Performance, Towing).
2. Allow associating one or more **builds** with a vehicle:

    * Each build is tied to exactly one upgrade category.
3. Allow:

    * Creating a build for a vehicle (category + optional nickname + notes).
    * Listing all builds for a given vehicle.
    * Viewing a single build, including all its parts and sub-parts.
    * Renaming builds and updating notes.
    * Deleting builds (with clear handling of associated parts/sub-parts).

**Ideal behavior:**

4. A vehicle can have multiple builds for the same category (e.g., “Overland v1”, “Overland v2”).
5. The system can mark one build as a “primary” or “active” build per category for that vehicle.

---

### 4.3 Part Management

For a given vehicle build, the service **must**:

1. Allow adding a part with:

    * Name (required)
    * Brand (optional)
    * Category
    * Tier
    * Product URL
    * Price (optional)
    * Notes (optional)
2. Allow viewing all parts in a build.
3. Allow viewing a single part with its sub-parts.
4. Allow editing a part (update all above fields).
5. Allow deleting a part and automatically handling its sub-parts (delete or reassign, as defined).
6. Preserve insertion order or provide a sort order (e.g., custom index or alphabetical/category order).

**Ideal behavior:**

7. Support filtering and grouping parts within a build by category and/or tier.
8. Provide calculated totals:

    * Total price of all parts in the build.
    * Total price per category.
    * Total price per tier.

---

### 4.4 Sub-Part Management

For a given part, the service **must**:

1. Allow adding sub-parts with:

    * Name (required)
    * Category
    * Tier
    * URL
    * Price
    * Notes
2. Allow listing all sub-parts for a part.
3. Allow editing and deleting sub-parts.
4. Include sub-parts in cost calculations for the build.

**Ideal behavior:**

5. Allow marking sub-parts as:

    * Required vs. optional.
    * Included vs. add-on.
6. Include these flags in cost summaries (e.g., “Base cost” vs. “Max cost with all add-ons”).

---

### 4.5 Cost & Summary Computation

The service **must**:

1. Compute and expose:

    * Total cost of a build = sum(parts) + sum(sub-parts).
2. Make it possible to query per-build and per-vehicle:

    * Total cost by category.
    * Total cost by tier.

**Ideal behavior:**

3. Support “scenario” calculations:

    * Optionally include/exclude certain parts or sub-parts (e.g., optional accessories) and recompute totals.
4. Provide breakdowns suitable for a UI:

    * Lists of parts grouped by category.
    * Aggregated totals at the bottom.

---

### 4.6 Data Access & Query Capabilities

The service **must** provide operations (via HTTP endpoints or equivalent) to:

1. Query vehicles by VIN or partial VIN.
2. Query vehicles by make/model/year.
3. Retrieve all builds for a specific vehicle.
4. Retrieve all parts for a specific build.
5. Retrieve all sub-parts for a specific part.

**Ideal behavior:**

6. Support search/filter on:

    * Parts by category, tier, brand.
    * Builds by category (e.g., “Show all Overlanding builds for all vehicles”).

---

### 4.7 Integrity & Validation

The service **must**:

1. Enforce that:

    * Parts belong to an existing build.
    * Sub-parts belong to an existing parent part.
    * Build belongs to an existing vehicle.
2. Validate:

    * VIN format (length, allowed characters).
    * URL format for product links (basic validation).
    * Prices are non-negative.

**Ideal behavior:**

3. Provide clear validation messages that a UI can display directly
   (e.g., “VIN must be 17 characters”).

---

## 5. Explicit Non-Goals (for this microservice)

To keep the scope tight, this microservice **is not responsible for**:

1. User authentication/authorization (can be delegated to another service or added later on top).
2. Payment processing or ordering parts.
3. Real-time inventory or stock checks.
4. Vendor integrations beyond simple product links (e.g., no scraping or API calls to vendors).
5. Complex multi-vehicle comparison logic beyond simple queries (though the data allows it later).

---