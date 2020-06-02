package com.creativeminds.facileapp.Models;

public class UploadPdf {

    private String ID;
    private String phone;
    private String url;

    public UploadPdf() {
    }

    public UploadPdf(String id, String phone, String url) {
        this.ID = id;
        this.phone = phone;
        this.url = url;
    }

    public String getID() {
        return ID;
    }

    public String getPhone() {
        return phone;
    }

    public String getUrl() {
        return url;
    }
}
