package com.apu.asc.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public class Service {

  private final String serviceId;
  private final ServiceType type;

  @NotBlank(message = "err.validation.serviceNameRequired")
  private final String serviceName;

  @DecimalMin(value = "0.01", message = "Price must be greater than zero")
  private double price;

  private boolean isActive;

  public Service(
      String serviceId, ServiceType type, String serviceName, double price, boolean isActive) {
    this.serviceId = serviceId;
    this.type = type;
    this.serviceName = serviceName;
    this.price = price;
    this.isActive = isActive;
  }

  public String getServiceId() {
    return serviceId;
  }

  public ServiceType getType() {
    return type;
  }

  public String getServiceName() {
    return serviceName;
  }

  public double getPrice() {
    return price;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  public String toFileString() {
    return String.join(
        "||", serviceId, type.name(), serviceName, String.valueOf(price), String.valueOf(isActive));
  }
}
