package com.creativeminds.facileapp.Models;

public class Vendor {
    private String userId;
    private String userName;
    private String userImage;
    private String userLat;
    private String userLon;
    private int userJobs;

    public Vendor(String userId, String userName, String userImage, String userLat, String userLon, int userJobs) {
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;
        this.userLat = userLat;
        this.userLon = userLon;
        this.userJobs = userJobs;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public String getUserLat() {
        return userLat;
    }

    public String getUserLon() {
        return userLon;
    }

    public int getUserJobs() {
        return userJobs;
    }
}
