package com.apu.asc.model;

public class Service {

    private String serviceId;
    private ServiceType type;
    private String serviceName;
    private double price;
    private boolean isActive;

    public Service(String serviceId, ServiceType type, String serviceName,
                   double price, boolean isActive) {
        this.serviceId = serviceId;
        this.type = type;
        this.serviceName = serviceName;
        this.price = price;
        this.isActive = isActive;
    }

    public String getServiceId()    { return serviceId; }
    public ServiceType getType()    { return type; }
    public String getServiceName()  { return serviceName; }
    public double getPrice()        { return price; }
    public boolean isActive()       { return isActive; }

    public void setPrice(double price)       { this.price = price; }
    public void setActive(boolean isActive)  { this.isActive = isActive; }

    public String toFileString() {
        return String.join("||", serviceId, type.name(), serviceName,
                String.valueOf(price), String.valueOf(isActive));
    }
}
