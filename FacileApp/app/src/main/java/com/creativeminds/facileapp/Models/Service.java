package com.creativeminds.facileapp.Models;

public class Service {
    private String serviceId;
    private String serviceName;
    private String deptName;
    private String serviceDes;
    private String servicePrice;
    private String departmentId;

    public Service(String serviceId, String serviceName, String deptName, String serviceDes, String servicePrice, String departmentId) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.deptName = deptName;
        this.serviceDes = serviceDes;
        this.servicePrice = servicePrice;
        this.departmentId = departmentId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDeptName() {
        return deptName;
    }

    public String getServiceDes() {
        return serviceDes;
    }

    public String getServicePrice() {
        return servicePrice;
    }

    public String getDepartmentId() {
        return departmentId;
    }
}
