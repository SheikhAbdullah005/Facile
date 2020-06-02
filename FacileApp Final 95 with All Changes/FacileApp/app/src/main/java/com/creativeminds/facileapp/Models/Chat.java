package com.creativeminds.facileapp.Models;

public class Chat {

    private String ID;
    private String CID;
    private String phoneNumber;
    private String JobId;

    public Chat() {
    }

    public Chat(String ID, String CID, String phoneNumber, String jobId) {
        this.ID = ID;
        this.CID = CID;
        this.phoneNumber = phoneNumber;
        JobId = jobId;
    }

    public String getID() {
        return ID;
    }

    public String getCID() {
        return CID;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getJobId() {
        return JobId;
    }
}
