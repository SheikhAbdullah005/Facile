package com.creativeminds.facileapp.Models;

public class Cart {
    private String cartId;
    private String customerId;
    private String datee;
    private String serviceName;
    private String serviceDes;
    private String servicePrice;
    private String deptName;
    private String vendorId;
    private String serviceId;
    private String departmentId;

    public Cart(String cartId, String customerId, String datee, String serviceName, String serviceDes, String servicePrice, String deptName, String vendorId, String serviceId, String departmentId) {
        this.cartId = cartId;
        this.customerId = customerId;
        this.datee = datee;
        this.serviceName = serviceName;
        this.serviceDes = serviceDes;
        this.servicePrice = servicePrice;
        this.deptName = deptName;
        this.vendorId = vendorId;
        this.serviceId = serviceId;
        this.departmentId = departmentId;
    }

    public String getCartId() {
        return cartId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getDatee() {
        return datee;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceDes() {
        return serviceDes;
    }

    public String getServicePrice() {
        return servicePrice;
    }

    public String getDeptName() {
        return deptName;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public String getServiceId() {
        return serviceId;
    }
}