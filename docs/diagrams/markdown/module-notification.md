# NOTIFICATION Module
## Visual Architecture Diagram
```mermaid
flowchart TD
    Common["Common Module"]
    Notification["Notification Module"]
    User["User Module"]
    Notification -->|listens to| Common
    Notification -->|uses| User
    User -->|listens to| Common
    User -->|uses| Common
```
## Module Specifications & Boundaries
| Specification / Property | Details |
|---|---|
| **Base package** | `com.apu.asc.notification` |
| **Spring components** | **Services**<br>• `c.a.a.n.NotificationApi` (via `c.a.a.n.internal.NotificationServiceImpl`) |
| **Bean references** | • `c.a.a.u.CurrentUserService` (in User)<br>• `c.a.a.u.UserApi` (in User) |
| **Events listened to** | • `c.a.a.c.event.AppointmentReminderRequestedEvent` |
> Auto-generated from `docs/diagrams/puml/module-notification.puml` and `docs/diagrams/canvases/module-notification.adoc`.