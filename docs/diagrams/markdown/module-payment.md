# PAYMENT Module
## Visual Architecture Diagram
```mermaid
flowchart TD
    Common["Common Module"]
    Notification["Notification Module"]
    Payment["Payment Module"]
    Quotation["Quotation Module"]
    User["User Module"]
    Workorder["Workorder Module"]
    Notification -->|listens to| Common
    Notification -->|uses| User
    Payment -->|uses| Common
    Payment -->|depends on| Notification
    Payment -->|uses| Quotation
    Payment -->|uses| User
    Payment -->|uses| Workorder
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
| **Base package** | `com.apu.asc.payment` |
| **Spring components** | **Services**<br>• `c.a.a.p.PaymentApi` (via `c.a.a.p.internal.PaymentServiceImpl`) |
| **Bean references** | • `c.a.a.w.WorkOrderApi` (in Workorder)<br>• `c.a.a.q.QuotationApi` (in Quotation)<br>• `c.a.a.u.CurrentUserService` (in User)<br>• `c.a.a.c.security.AccessPolicy` (in Common) |
> Auto-generated from `docs/diagrams/puml/module-payment.puml` and `docs/diagrams/canvases/module-payment.adoc`.