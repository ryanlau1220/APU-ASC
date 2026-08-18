# DOCUMENT Module
## Visual Architecture Diagram
```mermaid
flowchart TD
    Common["Common Module"]
    Document["Document Module"]
    User["User Module"]
    Workorder["Workorder Module"]
    Document -->|uses| Common
    Document -->|uses| User
    Document -->|uses| Workorder
    User -->|listens to| Common
    User -->|uses| Common
    Workorder -->|listens to| Common
    Workorder -->|uses| Common
    Workorder -->|uses| User
```
## Module Specifications & Boundaries
| Specification / Property | Details |
|---|---|
| **Base package** | `com.apu.asc.document` |
| **Spring components** | **Services**<br>• `c.a.a.d.DocumentApi` (via `c.a.a.d.internal.DocumentServiceImpl`) |
| **Bean references** | • `c.a.a.w.WorkOrderApi` (in Workorder)<br>• `c.a.a.u.CurrentUserService` (in User)<br>• `c.a.a.c.security.AccessPolicy` (in Common)<br>• `c.a.a.c.storage.ObjectStorageApi` (in Common) |
> Auto-generated from `docs/diagrams/puml/module-document.puml` and `docs/diagrams/canvases/module-document.adoc`.