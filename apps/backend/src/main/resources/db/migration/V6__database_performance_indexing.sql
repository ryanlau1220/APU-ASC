-- Migration: V6__database_performance_indexing.sql
-- Description: Composite B-Tree indexes for high-concurrency database queries

-- 1. Appointments Indexing Strategy
CREATE INDEX IF NOT EXISTS idx_appointments_customer_status ON appointments(customer_id, status);
CREATE INDEX IF NOT EXISTS idx_appointments_tech_date ON appointments(technician_id, appointment_date);

-- 2. Vehicles Indexing Strategy
CREATE INDEX IF NOT EXISTS idx_vehicles_customer ON vehicles(customer_id, license_plate);

-- 3. Payments & Invoices Indexing Strategy
CREATE INDEX IF NOT EXISTS idx_payments_customer_status ON payments(customer_id, payment_status);

-- 4. Service Catalog Indexing Strategy
CREATE INDEX IF NOT EXISTS idx_services_category_status ON services(category_id, status);

-- 5. Users Identity Lookup Indexing Strategy
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_keycloak_id ON users(keycloak_id);
