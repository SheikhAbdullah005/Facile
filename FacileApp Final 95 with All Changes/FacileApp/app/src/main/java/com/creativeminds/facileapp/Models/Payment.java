package com.creativeminds.facileapp.Models;

public class Payment {
    private String paymentID;
    private String totalPayment;
    private String vendorID;
    private String customerID;
    private String paymentStatus;

    public Payment() {
    }

    public Payment(String paymentID, String totalPayment, String vendorID, String customerID, String paymentStatus) {
        this.paymentID = paymentID;
        this.totalPayment = totalPayment;
        this.vendorID = vendorID;
        this.customerID = customerID;
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public String getTotalPayment() {
        return totalPayment;
    }

    public String getVendorID() {
        return vendorID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
