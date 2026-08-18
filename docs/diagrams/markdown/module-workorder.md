# WORKORDER Module
## Visual Architecture Diagram
```mermaid
flowchart TD
    Appointment["Appointment Module"]
    Common["Common Module"]
    Notification["Notification Module"]
    User["User Module"]
    Vehicle["Vehicle Module"]
    Workorder["Workorder Module"]
    Appointment -->|uses| Common
    Appointment -->|depends on| Notification
    Appointment -->|uses| User
    Appointment -->|uses| Vehicle
    Notification -->|listens to| Common
    Notification -->|uses| User
    User -->|listens to| Common
    User -->|uses| Common
    Vehicle -->|uses| Common
    Vehicle -->|uses| User
    Workorder -->|uses| Appointment
    Workorder -->|listens to| Common
    Workorder -->|uses| Common
    Workorder -->|depends on| Notification
    Workorder -->|uses| User
    Workorder -->|uses| Vehicle
```
## Module Specifications & Boundaries
| Specification / Property | Details |
|---|---|
| **Base package** | `com.apu.asc.workorder` |
| **Spring components** | **Services**<br>• `c.a.a.w.WorkOrderApi` (via `c.a.a.w.internal.WorkOrderServiceImpl`) |
| **Bean references** | • `c.a.a.a.AppointmentApi` (in Appointment)<br>• `c.a.a.v.VehicleApi` (in Vehicle)<br>• `c.a.a.u.UserApi` (in User)<br>• `c.a.a.u.CurrentUserService` (in User)<br>• `c.a.a.c.security.AccessPolicy` (in Common) |
| **Events listened to** | • `c.a.a.c.event.QuotationApprovedEvent` |
> Auto-generated from `docs/diagrams/puml/module-workorder.puml` and `docs/diagrams/canvases/module-workorder.adoc`.