-- APU Automotive Service Centre (APU-ASC) Initial Baseline Seed Data

-- 1. Baseline Manager Account
INSERT INTO users (id, keycloak_id, username, email, full_name, contact_number, role, status)
VALUES (
    'USR-001',
    'admin-keycloak-id',
    'admin',
    'admin@apu-asc.com',
    'System Administrator',
    '+60123456789',
    'MANAGER',
    'ACTIVE'
) ON CONFLICT (id) DO NOTHING;

-- 2. Service Categories
INSERT INTO categories (id, name, description)
VALUES 
    ('CAT-101', 'Regular Maintenance', 'Scheduled engine oil changes, fluid flushes, and routine safety inspections'),
    ('CAT-102', 'Brake & Mechanical Repair', 'Brake pads, rotor resurfacing, suspension tuning, and mechanical repairs'),
    ('CAT-103', 'Air-Con & Electrical', 'Air conditioning gas refills, compressor leak tests, and electrical diagnostics'),
    ('CAT-104', 'Engine & Transmission', 'Transmission fluid exchange, timing belt replacement, and engine overhauls')
ON CONFLICT (id) DO NOTHING;

-- 3. Standard Service Packages
INSERT INTO services (id, category_id, name, description, duration_minutes, base_price, status)
VALUES
    ('SVC-101', 'CAT-101', 'Full Engine Synthetic Oil Service', 'Includes 100% synthetic engine oil replacement, oil filter change, and 21-point safety inspection.', 60, 180.00, 'ACTIVE'),
    ('SVC-102', 'CAT-102', 'Brake Disc & Pad Replacement', 'Replacement of front/rear ceramic brake pads and rotor resurfacing for high stopping power.', 90, 320.00, 'ACTIVE'),
    ('SVC-103', 'CAT-103', 'Air Conditioning Maintenance & Gas Refill', 'Full AC system flush, leak check, compressor oil top-up, and R134a refrigerant refill.', 60, 150.00, 'ACTIVE'),
    ('SVC-104', 'CAT-104', 'Transmission Fluid & Filter Flush', 'Automatic transmission fluid exchange and filter cleaning to ensure smooth gear shifts.', 75, 240.00, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;
