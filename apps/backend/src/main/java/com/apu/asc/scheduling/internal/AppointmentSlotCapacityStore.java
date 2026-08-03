package com.apu.asc.scheduling.internal;

import com.apu.asc.scheduling.SlotAvailabilityDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Atomic PostgreSQL operations for capacity counters. The guarded UPSERT makes reservation safe
 * under concurrent booking requests without a check-then-insert race.
 */
@Repository
@RequiredArgsConstructor
class AppointmentSlotCapacityStore {

  private static final String RESERVE_SLOT =
      """
      INSERT INTO appointment_slot_capacities (appointment_date, time_slot, capacity, reserved_count)
      VALUES (?, ?, ?, 1)
      ON CONFLICT (appointment_date, time_slot) DO UPDATE
      SET reserved_count = appointment_slot_capacities.reserved_count + 1,
          updated_at = CURRENT_TIMESTAMP
      WHERE appointment_slot_capacities.reserved_count < appointment_slot_capacities.capacity
      RETURNING capacity, reserved_count
      """;

  private static final String UPDATE_CAPACITY =
      """
      INSERT INTO appointment_slot_capacities (appointment_date, time_slot, capacity, reserved_count)
      VALUES (?, ?, ?, 0)
      ON CONFLICT (appointment_date, time_slot) DO UPDATE
      SET capacity = EXCLUDED.capacity,
          updated_at = CURRENT_TIMESTAMP
      WHERE appointment_slot_capacities.reserved_count <= EXCLUDED.capacity
      RETURNING capacity, reserved_count
      """;

  private final JdbcTemplate jdbcTemplate;

  Optional<SlotAvailabilityDto> reserve(LocalDate date, String timeSlot, int defaultCapacity) {
    return jdbcTemplate.query(
        RESERVE_SLOT,
        statement -> {
          statement.setObject(1, date);
          statement.setString(2, timeSlot);
          statement.setInt(3, defaultCapacity);
        },
        resultSet ->
            resultSet.next()
                ? Optional.of(
                    toAvailability(date, timeSlot, resultSet.getInt(1), resultSet.getInt(2)))
                : Optional.empty());
  }

  void release(LocalDate date, String timeSlot) {
    int updated =
        jdbcTemplate.update(
            """
            UPDATE appointment_slot_capacities
            SET reserved_count = reserved_count - 1,
                updated_at = CURRENT_TIMESTAMP
            WHERE appointment_date = ? AND time_slot = ? AND reserved_count > 0
            """,
            date,
            timeSlot);
    if (updated == 0) {
      throw new IllegalStateException(
          "No active capacity reservation exists for this appointment slot.");
    }
  }

  Optional<SlotAvailabilityDto> updateCapacity(LocalDate date, String timeSlot, int capacity) {
    return jdbcTemplate.query(
        UPDATE_CAPACITY,
        statement -> {
          statement.setObject(1, date);
          statement.setString(2, timeSlot);
          statement.setInt(3, capacity);
        },
        resultSet ->
            resultSet.next()
                ? Optional.of(
                    toAvailability(date, timeSlot, resultSet.getInt(1), resultSet.getInt(2)))
                : Optional.empty());
  }

  List<SlotAvailabilityDto> findByDate(LocalDate date) {
    return jdbcTemplate.query(
        """
        SELECT time_slot, capacity, reserved_count
        FROM appointment_slot_capacities
        WHERE appointment_date = ?
        ORDER BY time_slot
        """,
        (resultSet, rowNumber) ->
            toAvailability(date, resultSet.getString(1), resultSet.getInt(2), resultSet.getInt(3)),
        date);
  }

  private SlotAvailabilityDto toAvailability(
      LocalDate date, String timeSlot, int capacity, int reserved) {
    int available = capacity - reserved;
    return new SlotAvailabilityDto(date, timeSlot, capacity, reserved, available, available > 0);
  }
}
