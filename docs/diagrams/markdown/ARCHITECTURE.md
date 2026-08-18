# APU-ASC System Architecture & Modulith Overview

This documentation is **100% auto-generated** from Spring Modulith architectural analysis.

## 🌐 Global Architecture Map
```mermaid
flowchart TD
    Appointment["Appointment Module"]
    Audit["Audit Module"]
    Common["Common Module"]
    Config["Config Module"]
    Document["Document Module"]
    Feedback["Feedback Module"]
    Notification["Notification Module"]
    Payment["Payment Module"]
    Quotation["Quotation Module"]
    Scheduling["Scheduling Module"]
    Servicecatalog["Servicecatalog Module"]
    User["User Module"]
    Vehicle["Vehicle Module"]
    Workorder["Workorder Module"]
    Appointment -->|uses| Common
    Appointment -->|depends on| Notification
    Appointment -->|uses| Scheduling
    Appointment -->|uses| User
    Appointment -->|uses| Vehicle
    Audit -->|listens to| Common
    Config -->|listens to| Common
    Config -->|uses| User
    Document -->|uses| Common
    Document -->|uses| User
    Document -->|uses| Workorder
    Feedback -->|uses| Common
    Feedback -->|uses| User
    Feedback -->|uses| Workorder
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
    Scheduling -->|depends on| Common
    Servicecatalog -->|depends on| Common
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

## 📦 Domain Modules Index
| Domain Module | Architecture Diagram & Specification |
|---|---|
| **APPOINTMENT** | [View appointment Specs & Diagram](./module-appointment.md) |
| **AUDIT** | [View audit Specs & Diagram](./module-audit.md) |
| **COMMON** | [View common Specs & Diagram](./module-common.md) |
| **CONFIG** | [View config Specs & Diagram](./module-config.md) |
| **DOCUMENT** | [View document Specs & Diagram](./module-document.md) |
| **FEEDBACK** | [View feedback Specs & Diagram](./module-feedback.md) |
| **NOTIFICATION** | [View notification Specs & Diagram](./module-notification.md) |
| **PAYMENT** | [View payment Specs & Diagram](./module-payment.md) |
| **QUOTATION** | [View quotation Specs & Diagram](./module-quotation.md) |
| **SCHEDULING** | [View scheduling Specs & Diagram](./module-scheduling.md) |
| **SERVICECATALOG** | [View servicecatalog Specs & Diagram](./module-servicecatalog.md) |
| **USER** | [View user Specs & Diagram](./module-user.md) |
| **VEHICLE** | [View vehicle Specs & Diagram](./module-vehicle.md) |
| **WORKORDER** | [View workorder Specs & Diagram](./module-workorder.md) |

---
> Update these documents anytime by running `./manage.sh docs`.