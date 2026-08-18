# FEEDBACK Module
## Visual Architecture Diagram
```mermaid
flowchart TD
    Common["Common Module"]
    Feedback["Feedback Module"]
    User["User Module"]
    Workorder["Workorder Module"]
    Feedback -->|uses| Common
    Feedback -->|uses| User
    Feedback -->|uses| Workorder
    User -->|listens to| Common
    User -->|uses| Common
    Workorder -->|listens to| Common
    Workorder -->|uses| Common
    Workorder -->|uses| User
```
## Module Specifications & Boundaries
| Specification / Property | Details |
|---|---|
| **Base package** | `com.apu.asc.feedback` |
| **Spring components** | **Services**<br>• `c.a.a.f.FeedbackApi` (via `c.a.a.f.internal.FeedbackServiceImpl`) |
| **Bean references** | • `c.a.a.w.WorkOrderApi` (in Workorder)<br>• `c.a.a.u.CurrentUserService` (in User)<br>• `c.a.a.c.security.AccessPolicy` (in Common) |
> Auto-generated from `docs/diagrams/puml/module-feedback.puml` and `docs/diagrams/canvases/module-feedback.adoc`.