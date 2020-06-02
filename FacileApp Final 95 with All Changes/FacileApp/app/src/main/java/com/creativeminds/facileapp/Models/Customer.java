package com.creativeminds.facileapp.Models;

public class Customer {
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerCNIC;
    private String customerImage;
    private String customerStatus;

    public Customer(String customerId, String customerName, String customerPhone, String customerEmail, String customerCNIC, String customerImage, String customerStatus) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.customerCNIC = customerCNIC;
        this.customerImage = customerImage;
        this.customerStatus = customerStatus;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerCNIC() {
        return customerCNIC;
    }

    public String getCustomerImage() {
        return customerImage;
    }

    public String getCustomerStatus() {
        return customerStatus;
    }
}
