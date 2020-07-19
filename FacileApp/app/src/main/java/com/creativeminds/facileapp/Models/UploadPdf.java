package com.creativeminds.facileapp.Models;

public class UploadPdf {

    private String ID;
    private String phone;
    private String url;
   // private String userId;
   private String email;

    public UploadPdf() {
    }

    public UploadPdf(String id, String phone, String url,String email) {
        this.ID = id;
        this.phone = phone;
        this.url = url;
        this.email = email;
        //this.userId = userId;
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
    public String getEmail() {
        return email;
    }
//    public String getUserId() {
//        return userId;
//    }
}
