## 1. High-Level Relationships (unchanged structure)

* **app_user** 1 ─── * N **vehicle**
* **vehicle** 1 ─── * N **vehicle_upgrade**
* **upgrade_category** 1 ─── * N **vehicle_upgrade**
* **vehicle_upgrade** 1 ─── * N **part**
* **part** 1 ─── * N **sub_part**

Lookup tables:

* **upgrade_category**
* **part_category**
* **part_tier**
* **role**, **user_role**

---

## 2. Auth & User Tables (same as before)

### 2.1 `app_user`

**Table:** `app_user`

| Column          | Type           | Notes                             |
| --------------- | -------------- | --------------------------------- |
| `id`            | `uuid` PK      | `DEFAULT gen_random_uuid()`       |
| `username`      | `varchar(50)`  | Unique                            |
| `email`         | `varchar(255)` | Unique (optional but recommended) |
| `password_hash` | `varchar(255)` | BCrypt/Argon2                     |
| `display_name`  | `varchar(100)` |                                   |
| `is_active`     | `boolean`      | Default `true`                    |
| `created_at`    | `timestamptz`  | Default `now()`                   |
| `updated_at`    | `timestamptz`  |                                   |

### 2.2 `role` & `user_role`

Same as before; just there to support roles later.

---

## 3. Vehicles & Builds (with VIN optional now)

### 3.1 `vehicle`

**Key change:** VIN is now **optional**, and uniqueness is enforced only when present.

**Table:** `vehicle`

| Column        | Type                      | Notes                                         |
| ------------- | ------------------------- | --------------------------------------------- |
| `id`          | `uuid` PK                 | `DEFAULT gen_random_uuid()`                   |
| `owner_id`    | `uuid` FK → `app_user.id` | Not null                                      |
| `vin`         | `varchar(17)`             | **Nullable.** If not null, must be unique.    |
| `year`        | `integer`                 | Nullable                                      |
| `make`        | `varchar(100)`            | Nullable (can be blank for “unknown project”) |
| `model`       | `varchar(100)`            | Nullable                                      |
| `trim`        | `varchar(100)`            | Nullable                                      |
| `nickname`    | `varchar(100)`            | e.g. “First Gen Overlander”                   |
| `notes`       | `text`                    | Freeform notes                                |
| `is_archived` | `boolean`                 | Soft delete flag, default `false`             |
| `created_at`  | `timestamptz`             | Default `now()`                               |
| `updated_at`  | `timestamptz`             |                                               |

**Constraints / indexes:**

* **Partial unique index** on VIN:

  ```sql
  CREATE UNIQUE INDEX ux_vehicle_vin_not_null
    ON vehicle (vin)
    WHERE vin IS NOT NULL;
  ```

This lets you:

* Track full VIN-based vehicles.
* Also create **VIN-less** vehicles (e.g., “Future 80-Series build,” “Tube chassis buggy”) and still keep the schema clean.

---

### 3.2 `upgrade_category`

Same as before.

**Table:** `upgrade_category`

| Column        | Type           | Notes                                     |
| ------------- | -------------- | ----------------------------------------- |
| `id`          | `serial` PK    |                                           |
| `key`         | `varchar(50)`  | Unique, e.g. `OVERLANDING`, `PERFORMANCE` |
| `name`        | `varchar(100)` | Human label                               |
| `description` | `text`         | Optional                                  |
| `sort_order`  | `int`          | For UI ordering                           |
| `is_active`   | `boolean`      | Default `true`                            |

---

### 3.3 `vehicle_upgrade` (Build)

**Table:** `vehicle_upgrade`

| Column                    | Type                             | Notes                                           |
| ------------------------- | -------------------------------- | ----------------------------------------------- |
| `id`                      | `uuid` PK                        | `DEFAULT gen_random_uuid()`                     |
| `vehicle_id`              | `uuid` FK → `vehicle.id`         | Not null                                        |
| `upgrade_category_id`     | `int` FK → `upgrade_category.id` | Not null                                        |
| `name`                    | `varchar(150)`                   | e.g. “Overland Build v1”                        |
| `slug`                    | `varchar(150)`                   | Optional, URL-safe                              |
| `description`             | `text`                           | Build description/notes                         |
| `priority_level`          | `smallint`                       | 1–5 or 1–10 (numeric; lower = higher priority)  |
| `target_completion_date`  | `date`                           | When you want the build finished                |
| `status`                  | `varchar(30)`                    | e.g. `PLANNED`, `IN_PROGRESS`, `COMPLETED`      |
| `is_primary_for_category` | `boolean`                        | At most one per (vehicle, category) if enforced |
| `created_at`              | `timestamptz`                    | Default `now()`                                 |
| `updated_at`              | `timestamptz`                    |                                                 |

---

## 4. Part Categorization & Tiers

### 4.1 `part_category`

**Table:** `part_category`

| Column        | Type             | Notes                                    |
| ------------- | ---------------- | ---------------------------------------- |
| `code`        | `varchar(50)` PK | e.g. `SUSPENSION`, `ARMOR`, `DRIVETRAIN` |
| `label`       | `varchar(100)`   | Human label                              |
| `description` | `text`           | Optional                                 |
| `sort_order`  | `int`            | For grouped display                      |

### 4.2 `part_tier`

**Table:** `part_tier`

| Column        | Type             | Notes                                     |
| ------------- | ---------------- | ----------------------------------------- |
| `code`        | `varchar(50)` PK | e.g. `BUDGET`, `MID`, `PREMIUM`, `RACE`   |
| `label`       | `varchar(100)`   | Human label                               |
| `rank`        | `smallint`       | Numeric order (e.g., 1=Budget, 3=Premium) |
| `description` | `text`           | Optional                                  |

This gives you **category** (functional grouping) and **tier** (quality/cost level), both filterable and sortable.

---

## 5. Parts & Sub-Parts (with clear numeric priority)

### 5.1 `part`

Core points you asked for:

* Each part has a **numeric priority** that is easy to sort/filter on.
* Each part also has a **category** and **tier** (for grouping).

**Table:** `part`

| Column                 | Type                                    | Notes                                                                 |
| ---------------------- | --------------------------------------- | --------------------------------------------------------------------- |
| `id`                   | `uuid` PK                               | `DEFAULT gen_random_uuid()`                                           |
| `vehicle_upgrade_id`   | `uuid` FK → `vehicle_upgrade.id`        | Not null                                                              |
| `name`                 | `varchar(200)`                          | Part name                                                             |
| `brand`                | `varchar(100)`                          | Optional                                                              |
| `category_code`        | `varchar(50)` FK → `part_category.code` | Can be null initially                                                 |
| `tier_code`            | `varchar(50)` FK → `part_tier.code`     | Optional                                                              |
| `product_url`          | `varchar(500)`                          | URL to part                                                           |
| `price`                | `numeric(10,2)`                         | Optional                                                              |
| `currency_code`        | `char(3)`                               | Default `'USD'`                                                       |
| `is_required`          | `boolean`                               | Core vs optional part                                                 |
| `status`               | `varchar(30)`                           | `PLANNED`, `ORDERED`, `INSTALLED`, etc.                               |
| `priority_value`       | `smallint`                              | **Numeric priority (e.g., 1–10) for sorting. Lower = higher priority. |
| `target_purchase_date` | `date`                                  | When you want to buy it                                               |
| `sort_order`           | `int`                                   | For manual ordering within the build                                  |
| `notes`                | `text`                                  | Install notes, vendor notes, etc.                                     |
| `created_at`           | `timestamptz`                           |                                                                       |
| `updated_at`           | `timestamptz`                           |                                                                       |

**Key numeric field for you:** `priority_value`
That’s the one you can use for simple “show me all parts for this build sorted by priority.”

**Indexes:**

* `(vehicle_upgrade_id)`
* `(vehicle_upgrade_id, priority_value)`
* `(vehicle_upgrade_id, category_code)`
* `(vehicle_upgrade_id, target_purchase_date)`

---

### 5.2 `sub_part`

Mirrors `part` semantics but always tied to a `parent_part_id`.

**Table:** `sub_part`

| Column                 | Type                                    | Notes                                                       |
| ---------------------- | --------------------------------------- | ----------------------------------------------------------- |
| `id`                   | `uuid` PK                               | `DEFAULT gen_random_uuid()`                                 |
| `parent_part_id`       | `uuid` FK → `part.id`                   | Not null                                                    |
| `name`                 | `varchar(200)`                          | Sub-part or kit component name                              |
| `brand`                | `varchar(100)`                          | Optional                                                    |
| `category_code`        | `varchar(50)` FK → `part_category.code` | Optional                                                    |
| `tier_code`            | `varchar(50)` FK → `part_tier.code`     | Optional                                                    |
| `product_url`          | `varchar(500)`                          | Optional                                                    |
| `price`                | `numeric(10,2)`                         | Optional                                                    |
| `currency_code`        | `char(3)`                               | Default `'USD'`                                             |
| `is_required`          | `boolean`                               | Required vs optional add-on                                 |
| `status`               | `varchar(30)`                           | `PLANNED`, `ORDERED`, `INSTALLED`, etc.                     |
| `priority_value`       | `smallint`                              | **Numeric priority for sub-parts** (same semantics as part) |
| `target_purchase_date` | `date`                                  | Optional                                                    |
| `sort_order`           | `int`                                   | Ordering within the parent part                             |
| `notes`                | `text`                                  |                                                             |
| `created_at`           | `timestamptz`                           |                                                             |
| `updated_at`           | `timestamptz`                           |                                                             |

**Indexes:**

* `(parent_part_id)`
* `(parent_part_id, priority_value)`

---

## 6. Planning & Filtering Scenarios (What this enables)

With **optional VIN** and **numeric priority + category** on parts/sub-parts, you can:

* Track **real** vehicles (with valid VINs) and **planned/future rigs** (no VIN yet).
* Filter parts by:

    * Build (`vehicle_upgrade_id`)
    * Category (`category_code`)
    * Tier (`tier_code`)
    * Priority (`priority_value`)
    * Status
* Sort parts cleanly:

    * First by `priority_value`
    * Then by `target_purchase_date`
    * Or by `category_code` then `priority_value`
* View **cost & planning**:

    * “All required parts with priority_value ≤ 3 for this build.”
    * “All planned purchases for March” using `target_purchase_date`.
    * “Show suspenders and armor only” by `category_code`.