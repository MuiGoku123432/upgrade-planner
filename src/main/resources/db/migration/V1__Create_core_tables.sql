-- Car Builder VIN Database Schema - Core Tables
-- Phase 1: Create all core tables for the application

-- Enable UUID extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================================
-- User Authentication & Authorization
-- ================================

-- User table for authentication
CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at TIMESTAMPTZ
);

-- Role table for authorization (future-ready)
CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- User-role mapping table
CREATE TABLE user_role (
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- ================================
-- Vehicle Management
-- ================================

-- Vehicle table with optional VIN support
CREATE TABLE vehicle (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    vin VARCHAR(17),
    year INTEGER,
    make VARCHAR(100),
    model VARCHAR(100),
    trim VARCHAR(100),
    nickname VARCHAR(100),
    notes TEXT,
    is_archived BOOLEAN DEFAULT false NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at TIMESTAMPTZ
);

-- ================================
-- Build/Upgrade Management
-- ================================

-- Upgrade category lookup table
CREATE TABLE upgrade_category (
    id SERIAL PRIMARY KEY,
    key VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true NOT NULL
);

-- Vehicle upgrade (build) table
CREATE TABLE vehicle_upgrade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicle(id) ON DELETE CASCADE,
    upgrade_category_id INTEGER NOT NULL REFERENCES upgrade_category(id),
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150),
    description TEXT,
    priority_level INTEGER DEFAULT 1,
    target_completion_date DATE,
    status VARCHAR(30) DEFAULT 'PLANNED',
    is_primary_for_category BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at TIMESTAMPTZ
);

-- ================================
-- Part Classification
-- ================================

-- Part category lookup table
CREATE TABLE part_category (
    code VARCHAR(50) PRIMARY KEY,
    label VARCHAR(100) NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0
);

-- Part tier lookup table
CREATE TABLE part_tier (
    code VARCHAR(50) PRIMARY KEY,
    label VARCHAR(100) NOT NULL,
    rank INTEGER NOT NULL,
    description TEXT
);

-- ================================
-- Parts Management
-- ================================

-- Main parts table
CREATE TABLE part (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_upgrade_id UUID NOT NULL REFERENCES vehicle_upgrade(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    brand VARCHAR(100),
    category_code VARCHAR(50) REFERENCES part_category(code),
    tier_code VARCHAR(50) REFERENCES part_tier(code),
    product_url VARCHAR(500),
    price NUMERIC(10,2),
    currency_code VARCHAR(3) DEFAULT 'USD',
    is_required BOOLEAN DEFAULT true,
    status VARCHAR(30) DEFAULT 'PLANNED',
    priority_value INTEGER DEFAULT 5,
    target_purchase_date DATE,
    sort_order INTEGER DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at TIMESTAMPTZ
);

-- Sub-parts table (components of main parts)
CREATE TABLE sub_part (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_part_id UUID NOT NULL REFERENCES part(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    brand VARCHAR(100),
    category_code VARCHAR(50) REFERENCES part_category(code),
    tier_code VARCHAR(50) REFERENCES part_tier(code),
    product_url VARCHAR(500),
    price NUMERIC(10,2),
    currency_code VARCHAR(3) DEFAULT 'USD',
    is_required BOOLEAN DEFAULT true,
    status VARCHAR(30) DEFAULT 'PLANNED',
    priority_value INTEGER DEFAULT 5,
    target_purchase_date DATE,
    sort_order INTEGER DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at TIMESTAMPTZ
);