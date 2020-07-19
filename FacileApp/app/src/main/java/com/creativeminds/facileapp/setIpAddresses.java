package com.creativeminds.facileapp;

public class setIpAddresses {
    String ip ;
    String url;

    public setIpAddresses() {
        this.ip = "http://192.168.1.107:3005/api/";
        this.url = "http://192.168.1.107/phpServer/charge.php";
    }

    public String getIp() {
        return ip;
    }

    public String getUrl() {
        return url;
    }
}
