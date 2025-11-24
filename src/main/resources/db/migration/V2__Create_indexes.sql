-- Car Builder VIN Database Schema - Indexes and Constraints
-- Phase 2: Create performance indexes and additional constraints

-- ================================
-- Vehicle Constraints & Indexes
-- ================================

-- Partial unique index on VIN (only when VIN is not null)
-- This allows multiple records with NULL VIN but enforces uniqueness when VIN is provided
CREATE UNIQUE INDEX ux_vehicle_vin_not_null 
    ON vehicle (vin) 
    WHERE vin IS NOT NULL;

-- Index for vehicle ownership queries
CREATE INDEX ix_vehicle_owner_id ON vehicle (owner_id);

-- Index for archived vehicle filtering
CREATE INDEX ix_vehicle_owner_archived ON vehicle (owner_id, is_archived);

-- Index for vehicle make/model searches
CREATE INDEX ix_vehicle_make_model ON vehicle (make, model) WHERE make IS NOT NULL AND model IS NOT NULL;

-- ================================
-- Vehicle Upgrade Indexes
-- ================================

-- Index for vehicle upgrade lookups
CREATE INDEX ix_vehicle_upgrade_vehicle_id ON vehicle_upgrade (vehicle_id);

-- Index for upgrade category filtering
CREATE INDEX ix_vehicle_upgrade_category ON vehicle_upgrade (upgrade_category_id);

-- Index for build status queries
CREATE INDEX ix_vehicle_upgrade_status ON vehicle_upgrade (vehicle_id, status);

-- Index for primary build queries
CREATE INDEX ix_vehicle_upgrade_primary ON vehicle_upgrade (vehicle_id, upgrade_category_id, is_primary_for_category);

-- Index for target completion date sorting
CREATE INDEX ix_vehicle_upgrade_target_date ON vehicle_upgrade (target_completion_date) WHERE target_completion_date IS NOT NULL;

-- ================================
-- Part Management Indexes
-- ================================

-- Primary index for parts by build
CREATE INDEX ix_part_vehicle_upgrade_id ON part (vehicle_upgrade_id);

-- Index for priority-based sorting
CREATE INDEX ix_part_priority ON part (vehicle_upgrade_id, priority_value);

-- Index for category-based filtering
CREATE INDEX ix_part_category ON part (vehicle_upgrade_id, category_code);

-- Index for tier-based filtering  
CREATE INDEX ix_part_tier ON part (vehicle_upgrade_id, tier_code);

-- Index for status tracking
CREATE INDEX ix_part_status ON part (vehicle_upgrade_id, status);

-- Index for purchase date planning
CREATE INDEX ix_part_target_date ON part (target_purchase_date) WHERE target_purchase_date IS NOT NULL;

-- Composite index for priority and category queries
CREATE INDEX ix_part_category_priority ON part (vehicle_upgrade_id, category_code, priority_value);

-- ================================
-- Sub-Part Indexes
-- ================================

-- Primary index for sub-parts by parent part
CREATE INDEX ix_sub_part_parent_id ON sub_part (parent_part_id);

-- Index for sub-part priority sorting
CREATE INDEX ix_sub_part_priority ON sub_part (parent_part_id, priority_value);

-- Index for sub-part status tracking
CREATE INDEX ix_sub_part_status ON sub_part (parent_part_id, status);

-- ================================
-- User & Role Indexes
-- ================================

-- Index for user login queries
CREATE INDEX ix_app_user_username ON app_user (username);
CREATE INDEX ix_app_user_email ON app_user (email) WHERE email IS NOT NULL;

-- Index for active user filtering
CREATE INDEX ix_app_user_active ON app_user (is_active);

-- ================================
-- Additional Constraints
-- ================================

-- Add check constraints for valid data ranges
ALTER TABLE part ADD CONSTRAINT chk_part_priority_range 
    CHECK (priority_value >= 1 AND priority_value <= 10);

ALTER TABLE sub_part ADD CONSTRAINT chk_sub_part_priority_range 
    CHECK (priority_value >= 1 AND priority_value <= 10);

ALTER TABLE vehicle_upgrade ADD CONSTRAINT chk_upgrade_priority_range 
    CHECK (priority_level >= 1 AND priority_level <= 10);

-- Add check constraints for price validation
ALTER TABLE part ADD CONSTRAINT chk_part_price_positive 
    CHECK (price IS NULL OR price >= 0);

ALTER TABLE sub_part ADD CONSTRAINT chk_sub_part_price_positive 
    CHECK (price IS NULL OR price >= 0);

-- Add check constraint for VIN format (17 characters when provided)
ALTER TABLE vehicle ADD CONSTRAINT chk_vehicle_vin_length 
    CHECK (vin IS NULL OR length(vin) = 17);

-- Add check constraint for valid year range
ALTER TABLE vehicle ADD CONSTRAINT chk_vehicle_year_range 
    CHECK (year IS NULL OR (year >= 1900 AND year <= EXTRACT(YEAR FROM CURRENT_DATE) + 5));

-- ================================
-- Update Timestamp Triggers
-- ================================

-- Function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply update triggers to all tables with updated_at columns
CREATE TRIGGER update_app_user_updated_at 
    BEFORE UPDATE ON app_user 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vehicle_updated_at 
    BEFORE UPDATE ON vehicle 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vehicle_upgrade_updated_at 
    BEFORE UPDATE ON vehicle_upgrade 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_part_updated_at 
    BEFORE UPDATE ON part 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sub_part_updated_at 
    BEFORE UPDATE ON sub_part 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();