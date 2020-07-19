package com.creativeminds.facileapp.Models;

public class wallet {
    private String User_ID;
    private String card_number;
    private String money;

    public String getUser_ID() {
        return User_ID;
    }

    public void setUser_ID(String user_ID) {
        User_ID = user_ID;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public wallet(String user_ID, String card_number, String money) {
        User_ID = user_ID;
        this.card_number = card_number;
        this.money = money;
    }
}
