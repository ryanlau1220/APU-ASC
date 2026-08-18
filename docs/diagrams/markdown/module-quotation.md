# QUOTATION Module
## Visual Architecture Diagram
```mermaid
flowchart TD
    Common["Common Module"]
    Notification["Notification Module"]
    Quotation["Quotation Module"]
    User["User Module"]
    Workorder["Workorder Module"]
    Notification -->|listens to| Common
    Notification -->|uses| User
    Quotation -->|uses| Common
    Quotation -->|depends on| Notification
    Quotation -->|uses| User
    Quotation -->|uses| Workorder
    User -->|listens to| Common
    User -->|uses| Common
    Workorder -->|listens to| Common
    Workorder -->|uses| Common
    Workorder -->|depends on| Notification
    Workorder -->|uses| User
```
## Module Specifications & Boundaries
| Specification / Property | Details |
|---|---|
| **Base package** | `com.apu.asc.quotation` |
| **Spring components** | **Services**<br>• `c.a.a.q.QuotationApi` (via `c.a.a.q.internal.QuotationServiceImpl`) |
| **Bean references** | • `c.a.a.w.WorkOrderApi` (in Workorder)<br>• `c.a.a.u.CurrentUserService` (in User)<br>• `c.a.a.c.security.AccessPolicy` (in Common) |
> Auto-generated from `docs/diagrams/puml/module-quotation.puml` and `docs/diagrams/canvases/module-quotation.adoc`.