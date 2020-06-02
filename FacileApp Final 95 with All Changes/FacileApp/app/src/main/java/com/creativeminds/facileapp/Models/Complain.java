package com.creativeminds.facileapp.Models;

public class Complain {
    private String complainId;
    private String complainMessage;
    private String againstUserName;
    private String againstUserID;
    private String userID;


    public Complain() {
    }

    public Complain(String complainId, String complainMessage, String againstUserName, String againstUserID, String userID) {
        this.complainId = complainId;
        this.complainMessage = complainMessage;
        this.againstUserName = againstUserName;
        this.againstUserID = againstUserID;
        this.userID = userID;
    }

    public String getComplainId() {
        return complainId;
    }

    public String getComplainMessage() {
        return complainMessage;
    }

    public String getAgainstUserName() {
        return againstUserName;
    }

    public String getAgainstUserID() {
        return againstUserID;
    }

    public String getUserID() {
        return userID;
    }
}
