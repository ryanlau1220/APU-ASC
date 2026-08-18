# VEHICLE Module
## Visual Architecture Diagram
```mermaid
flowchart TD
    Common["Common Module"]
    User["User Module"]
    Vehicle["Vehicle Module"]
    User -->|listens to| Common
    User -->|uses| Common
    Vehicle -->|uses| Common
    Vehicle -->|uses| User
```
## Module Specifications & Boundaries
| Specification / Property | Details |
|---|---|
| **Base package** | `com.apu.asc.vehicle` |
| **Spring components** | **Services**<br>• `c.a.a.v.VehicleApi` (via `c.a.a.v.internal.VehicleServiceImpl`) |
| **Bean references** | • `c.a.a.u.CurrentUserService` (in User)<br>• `c.a.a.c.security.AccessPolicy` (in Common) |
> Auto-generated from `docs/diagrams/puml/module-vehicle.puml` and `docs/diagrams/canvases/module-vehicle.adoc`.