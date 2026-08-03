CREATE TABLE appointment_slot_capacities (
    appointment_date DATE NOT NULL,
    time_slot VARCHAR(32) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    reserved_count INTEGER NOT NULL DEFAULT 0 CHECK (reserved_count >= 0 AND reserved_count <= capacity),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (appointment_date, time_slot)
);

-- Existing pending and confirmed bookings already consume workshop capacity. The default is three
-- bays, but no existing schedule is invalidated if it already has more active reservations.
INSERT INTO appointment_slot_capacities (
    appointment_date, time_slot, capacity, reserved_count
)
SELECT
    appointment_date,
    time_slot,
    GREATEST(3, COUNT(*)::INTEGER),
    COUNT(*)::INTEGER
FROM appointments
WHERE status IN ('PENDING', 'CONFIRMED')
GROUP BY appointment_date, time_slot;
