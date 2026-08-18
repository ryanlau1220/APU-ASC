# SERVICECATALOG Module
## Visual Architecture Diagram
```mermaid
flowchart TD
    Common["Common Module"]
    Servicecatalog["Servicecatalog Module"]
    Servicecatalog -->|depends on| Common
```
## Module Specifications & Boundaries
| Specification / Property | Details |
|---|---|
| **Base package** | `com.apu.asc.servicecatalog` |
| **Spring components** | **Services**<br>• `c.a.a.s.ServiceCatalogApi` (via `c.a.a.s.internal.ServiceCatalogServiceImpl`) |
> Auto-generated from `docs/diagrams/puml/module-servicecatalog.puml` and `docs/diagrams/canvases/module-servicecatalog.adoc`.