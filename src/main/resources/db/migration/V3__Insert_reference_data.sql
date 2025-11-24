-- Car Builder VIN Database Schema - Reference Data
-- Phase 3: Insert essential lookup data and default values

-- ================================
-- User Roles
-- ================================

INSERT INTO role (name, description) VALUES 
('ROLE_USER', 'Standard user with access to personal vehicles and builds'),
('ROLE_ADMIN', 'Administrator with full system access'),
('ROLE_MODERATOR', 'Moderator with limited administrative capabilities');

-- ================================
-- Upgrade Categories
-- ================================

INSERT INTO upgrade_category (key, name, description, sort_order, is_active) VALUES 
('OVERLANDING', 'Overlanding', 'Camping and trail capability focused builds for off-road adventures', 1, true),
('PERFORMANCE', 'Performance', 'Engine, handling, and speed focused modifications', 2, true),
('TOWING', 'Towing', 'Heavy-duty upgrades for trailer and equipment hauling', 3, true),
('DAILY_DRIVER', 'Daily Driver', 'Comfort, efficiency, and reliability improvements', 4, true),
('ROCK_CRAWLING', 'Rock Crawling', 'Extreme off-road modifications for technical terrain', 5, true),
('RACING', 'Racing', 'Track and competition focused modifications', 6, true),
('RESTORATION', 'Restoration', 'Classic vehicle restoration and preservation', 7, true),
('SHOW_CAR', 'Show Car', 'Aesthetic and presentation focused modifications', 8, true),
('WINTER_SETUP', 'Winter Setup', 'Cold weather and snow/ice driving preparations', 9, true),
('UTILITY_WORK', 'Utility/Work', 'Commercial and work-focused vehicle modifications', 10, true);

-- ================================
-- Part Categories
-- ================================

INSERT INTO part_category (code, label, description, sort_order) VALUES 
-- Drivetrain & Engine
('ENGINE', 'Engine', 'Engine internals, ECU tuning, intake, exhaust systems', 1),
('TRANSMISSION', 'Transmission', 'Transmission, transfer case, and related components', 2),
('DRIVETRAIN', 'Drivetrain', 'Axles, differentials, driveshafts, and related components', 3),
('FUEL_SYSTEM', 'Fuel System', 'Fuel tanks, pumps, lines, and delivery systems', 4),

-- Suspension & Handling  
('SUSPENSION', 'Suspension', 'Shocks, springs, coilovers, and suspension components', 5),
('STEERING', 'Steering', 'Steering wheels, columns, racks, and handling upgrades', 6),
('BRAKES', 'Brakes', 'Brake pads, rotors, calipers, and brake system upgrades', 7),

-- Exterior & Protection
('ARMOR', 'Armor/Protection', 'Bumpers, rock sliders, skid plates, and protection systems', 8),
('WHEELS_TIRES', 'Wheels & Tires', 'Wheels, tires, and related mounting hardware', 9),
('BODY_EXTERIOR', 'Body & Exterior', 'Body panels, trim, exterior accessories, and modifications', 10),
('LIGHTING', 'Lighting', 'Headlights, fog lights, light bars, and auxiliary lighting', 11),

-- Interior & Comfort
('INTERIOR', 'Interior', 'Seats, trim, dashboard, and interior accessories', 12),
('COMFORT', 'Comfort & Convenience', 'Climate control, ergonomic, and convenience upgrades', 13),

-- Electrical & Electronics
('ELECTRICAL', 'Electrical', 'Wiring, alternators, batteries, and electrical systems', 14),
('ELECTRONICS', 'Electronics', 'Radios, navigation, communication, and electronic accessories', 15),

-- Recovery & Utility
('RECOVERY', 'Recovery', 'Winches, recovery straps, shackles, and recovery equipment', 16),
('STORAGE', 'Storage', 'Roof racks, cargo systems, and storage solutions', 17),
('CAMPING', 'Camping & Overlanding', 'Camping equipment, awnings, and overland-specific gear', 18),

-- Maintenance & Tools
('MAINTENANCE', 'Maintenance', 'Fluids, filters, maintenance items, and service parts', 19),
('TOOLS', 'Tools', 'Specialty tools, diagnostic equipment, and installation hardware', 20),
('HARDWARE', 'Hardware', 'Bolts, nuts, brackets, and general mounting hardware', 21);

-- ================================
-- Part Tiers
-- ================================

INSERT INTO part_tier (code, label, rank, description) VALUES 
('BUDGET', 'Budget', 1, 'Entry-level parts focusing on affordability and basic functionality'),
('MID', 'Mid-Range', 2, 'Good balance of quality and value with solid performance'),
('PREMIUM', 'Premium', 3, 'High-quality parts with excellent performance and durability'),
('RACE', 'Race/Competition', 4, 'Top-tier parts designed for racing and extreme performance'),
('OEM_PLUS', 'OEM+', 2, 'Factory replacement parts with minor improvements'),
('CUSTOM', 'Custom/Fabricated', 3, 'Custom-built or fabricated solutions');

-- ================================
-- Default Admin User (for initial setup)
-- Note: This should be changed/removed in production
-- Password is 'admin123' - bcrypt hash with strength 12
-- ================================

INSERT INTO app_user (username, email, password_hash, display_name, is_active) VALUES 
('admin', 'admin@carbuilder.local', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqyc/Zo1G.7HhmONfvd4xta', 'System Administrator', true);

-- Assign admin role to default admin user
INSERT INTO user_role (user_id, role_id) 
SELECT u.id, r.id 
FROM app_user u, role r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';