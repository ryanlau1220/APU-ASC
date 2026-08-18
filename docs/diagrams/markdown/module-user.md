# USER Module
## Visual Architecture Diagram
```mermaid
flowchart TD
    Common["Common Module"]
    User["User Module"]
    User -->|listens to| Common
    User -->|uses| Common
```
## Module Specifications & Boundaries
| Specification / Property | Details |
|---|---|
| **Base package** | `com.apu.asc.user` |
| **Spring components** | **Services**<br>• `c.a.a.u.CurrentUserService`<br>• `c.a.a.u.CustomOidcUserService`<br>• `c.a.a.u.UserApi` (via `c.a.a.u.internal.UserServiceImpl`) |
| **Bean references** | • `c.a.a.c.security.AccessPolicy` (in Common) |
| **Events listened to** | • `c.a.a.c.event.AppointmentReminderRequestedEvent`<br>• `c.a.a.c.event.EmployeeInvitationRequestedEvent` |
> Auto-generated from `docs/diagrams/puml/module-user.puml` and `docs/diagrams/canvases/module-user.adoc`.