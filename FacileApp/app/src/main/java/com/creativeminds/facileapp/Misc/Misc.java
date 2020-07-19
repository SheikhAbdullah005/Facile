package com.creativeminds.facileapp.Misc;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.creativeminds.facileapp.setIpAddresses;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class Misc {

    private Context context;
    private boolean connected;
    private double lat, lon;
    setIpAddresses ip = new setIpAddresses();
    public final String ROOT_PATH = ip.getIp();

    public Misc(Context context) {
        this.context = context;
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else {
            Toast.makeText(context, "Internet Connection not available!", Toast.LENGTH_SHORT).show();
            connected = false;
        }
        return connected;
    }

    public LatLng getCoordinates(String location) {
        Geocoder gc = new Geocoder(context);
        LatLng latLng = null;
        try {
            List<Address> address = gc.getFromLocationName(location, 1);
            Address add = address.get(0);
            lat = add.getLatitude();
            lon = add.getLongitude();
            latLng = new LatLng(lat, lon);
        } catch (IOException e) {
            showToast("Service Location not found");
            e.printStackTrace();
        }
        return latLng;
    }

    public void showToast(String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
