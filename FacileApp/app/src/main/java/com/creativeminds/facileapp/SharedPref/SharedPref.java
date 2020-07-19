package com.creativeminds.facileapp.SharedPref;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    Context context;
    private static final String PREF_NAME = "WIVAALog";
    private static final String IS_LOGIN = "IsLoggedIn";
    int PRIVATE_MODE = 0;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }
    public void createLoginSession(String userId, String userRole, String userOnline,String email) {
        editor.putBoolean("login", true);
        editor.putString("userId", userId);
        editor.putString("userRole", userRole);
        editor.putString("userOnline", userOnline);
        editor.putString("email", email);
        editor.commit();
    }

    // For Firebase Chat Messages
    public void createSenderID(String senderID){
        editor.putString("senderId", senderID);
        editor.commit();
    }
    public void firebaseSenderId(String id){
        editor.putString("firebaseSenderId", id);
        editor.commit();
    }
    public String getfirebaseSenderId() {
        String firebaseSenderId = sharedPreferences.getString("firebaseSenderId", null);
        return firebaseSenderId;
    }
    public void firebaseReceiverId(String id){
        editor.putString("firebaseReceiverId", id);
        editor.commit();
    }
    public String getfirebaseReceiverId() {
        String firebaseReceiverId = sharedPreferences.getString("firebaseReceiverId", null);
        return firebaseReceiverId;
    }
    public String getSenderId() {
        String senderId = sharedPreferences.getString("senderId", null);
        return senderId;
    }
    public String getEmail() {
        String currentEmail = sharedPreferences.getString("email", null);
        return currentEmail;
    }

    // For Firebase Chat Messages Sender Name
    public void createIDs(String venID, String cusID, String from) {
        editor.putString("venId", venID);
        editor.putString("cusId", cusID);
        editor.putString("from", from);
        editor.commit();
    }
    public String getVenId() {
        String venId = sharedPreferences.getString("venId", null);
        return venId;
    }
    public String getCusId() {
        String cusId = sharedPreferences.getString("cusId", null);
        return cusId;
    }
    public String getFromUser() {
        String from = sharedPreferences.getString("from", null);
        return from;
    }


    // Logged in User Values
    public String getUserId() {
        String userId = sharedPreferences.getString("userId", null);
        return userId;
    }

    public String getUserRole() {
        String userRole = sharedPreferences.getString("userRole", null);
        return userRole;
    }

    public String getUserOnline() {
        String userOnline = sharedPreferences.getString("userOnline", null);
        return userOnline;
    }

    public void updateUserOnline(String status) {
        editor.remove("userOnline");
        editor.putString("userOnline", status);
    }

    public void clearSession() {
        editor.remove("userId");
        editor.remove("userRole");
        editor.remove("userOnline");
        editor.remove("senderId");
        editor.remove("venId");
        editor.remove("cusId");
        editor.remove("from");
        editor.remove("email");
        editor.putBoolean("login", false);
        editor.remove("firebaseReceiverId");
        editor.remove("firebaseSenderId");
        editor.clear();
        editor.commit();
    }
}
