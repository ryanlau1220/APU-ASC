package com.apu.asc.common;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.apu.asc.appointment.AppointmentStatus;
import com.apu.asc.payment.PaymentStatus;
import com.apu.asc.servicecatalog.ServiceStatus;
import com.apu.asc.user.UserStatus;
import com.apu.asc.workorder.WorkOrderStatus;
import org.junit.jupiter.api.Test;

class LifecycleStateMachineTest {

  @Test
  void acceptsOnlyTheDefinedAppointmentTransitions() {
    AppointmentStatus.PENDING.requireTransitionTo(AppointmentStatus.CONFIRMED);
    AppointmentStatus.CONFIRMED.requireTransitionTo(AppointmentStatus.CANCELLED);

    assertThatThrownBy(
            () -> AppointmentStatus.CONFIRMED.requireTransitionTo(AppointmentStatus.PENDING))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void preservesPaymentAndWorkOrderTerminalStates() {
    PaymentStatus.UNPAID.requireTransitionTo(PaymentStatus.PAID);
    WorkOrderStatus.IN_PROGRESS.requireTransitionTo(WorkOrderStatus.COMPLETED);

    assertThatThrownBy(() -> PaymentStatus.REFUNDED.requireTransitionTo(PaymentStatus.PAID))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> WorkOrderStatus.COMPLETED.requireTransitionTo(WorkOrderStatus.IN_PROGRESS))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsUnknownStatusValuesAndAllowsControlledReactivation() {
    assertThatThrownBy(() -> AppointmentStatus.fromString("in workshop"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ServiceStatus.fromString("archived"))
        .isInstanceOf(IllegalArgumentException.class);

    UserStatus.INACTIVE.requireTransitionTo(UserStatus.ACTIVE);
    ServiceStatus.ACTIVE.requireTransitionTo(ServiceStatus.INACTIVE);
  }
}
