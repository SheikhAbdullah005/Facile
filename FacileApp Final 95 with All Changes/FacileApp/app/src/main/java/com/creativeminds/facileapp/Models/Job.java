package com.creativeminds.facileapp.Models;

public class Job {
    private String jobId;
    private String jobStatus;
    private String jobStartTime;
    private String jobCustomerId;
    private String jobVendorId;
    private String jobServiceId;
    private String customerName;
    private String serviceName;
    private String servicePrice;
    private String vendorPhone;
    private String vendorAddress;
    private String vendorCity;
    private String serviceLat;
    private String serviceLon;

    public Job(String jobId, String jobStatus, String jobStartTime, String jobCustomerId, String jobVendorId, String jobServiceId, String customerName, String serviceName, String servicePrice, String vendorPhone, String vendorAddress, String vendorCity, String serviceLat, String serviceLon) {
        this.jobId = jobId;
        this.jobStatus = jobStatus;
        this.jobStartTime = jobStartTime;
        this.jobCustomerId = jobCustomerId;
        this.jobVendorId = jobVendorId;
        this.jobServiceId = jobServiceId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
        this.vendorPhone = vendorPhone;
        this.vendorAddress = vendorAddress;
        this.vendorCity = vendorCity;
        this.serviceLat = serviceLat;
        this.serviceLon = serviceLon;
    }

    public String getJobId() {
        return jobId;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public String getJobStartTime() {
        return jobStartTime;
    }

    public String getJobCustomerId() {
        return jobCustomerId;
    }

    public String getJobVendorId() {
        return jobVendorId;
    }

    public String getJobServiceId() {
        return jobServiceId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServicePrice() {
        return servicePrice;
    }

    public String getVendorPhone() {
        return vendorPhone;
    }

    public String getVendorAddress() {
        return vendorAddress;
    }

    public String getVendorCity() {
        return vendorCity;
    }

    public String getServiceLat() {
        return serviceLat;
    }

    public String getServiceLon() {
        return serviceLon;
    }
}