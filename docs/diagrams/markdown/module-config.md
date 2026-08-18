# CONFIG Module
## Visual Architecture Diagram
```mermaid
flowchart TD
    Common["Common Module"]
    Config["Config Module"]
    User["User Module"]
    Config -->|listens to| Common
    Config -->|uses| User
    User -->|listens to| Common
    User -->|uses| Common
```
## Module Specifications & Boundaries
| Specification / Property | Details |
|---|---|
| **Base package** | `com.apu.asc.config` |
| **Spring components** | **Controllers**<br>• `c.a.a.c.ScalarUiController`<br>• `c.a.a.c.SseController` listening to `c.a.a.c.event.LiveUpdateEvent`<br>**Others**<br>• `c.a.a.c.AppConfig`<br>• `c.a.a.c.CacheConfig`<br>• `c.a.a.c.HttpLoggingFilter`<br>• `c.a.a.c.RateLimiterFilter`<br>• `c.a.a.c.ScalarDocConfig`<br>• `c.a.a.c.SecurityConfig` |
| **Bean references** | • `c.a.a.u.CustomOidcUserService` (in User)<br>• `c.a.a.u.CurrentUserService` (in User) |
| **Events listened to** | • `c.a.a.c.event.LiveUpdateEvent` |
> Auto-generated from `docs/diagrams/puml/module-config.puml` and `docs/diagrams/canvases/module-config.adoc`.