# APPOINTMENT Module
## Visual Architecture Diagram
```mermaid
flowchart TD
    Appointment["Appointment Module"]
    Common["Common Module"]
    Notification["Notification Module"]
    Scheduling["Scheduling Module"]
    User["User Module"]
    Vehicle["Vehicle Module"]
    Appointment -->|uses| Common
    Appointment -->|depends on| Notification
    Appointment -->|uses| Scheduling
    Appointment -->|uses| User
    Appointment -->|uses| Vehicle
    Notification -->|listens to| Common
    Notification -->|uses| User
    Scheduling -->|depends on| Common
    User -->|listens to| Common
    User -->|uses| Common
    Vehicle -->|uses| Common
    Vehicle -->|uses| User
```
## Module Specifications & Boundaries
| Specification / Property | Details |
|---|---|
| **Base package** | `com.apu.asc.appointment` |
| **Spring components** | **Services**<br>• `c.a.a.a.AppointmentApi` (via `c.a.a.a.internal.AppointmentServiceImpl`) |
| **Bean references** | • `c.a.a.v.VehicleApi` (in Vehicle)<br>• `c.a.a.u.CurrentUserService` (in User)<br>• `c.a.a.c.security.AccessPolicy` (in Common)<br>• `c.a.a.s.SchedulingApi` (in Scheduling) |
> Auto-generated from `docs/diagrams/puml/module-appointment.puml` and `docs/diagrams/canvases/module-appointment.adoc`.