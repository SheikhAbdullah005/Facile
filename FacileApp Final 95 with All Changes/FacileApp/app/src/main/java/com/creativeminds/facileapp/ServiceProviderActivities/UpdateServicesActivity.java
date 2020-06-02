package com.creativeminds.facileapp.ServiceProviderActivities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.SharedPref.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UpdateServicesActivity extends AppCompatActivity {

    private GridLayout up_services;
    Misc misc;
    SharedPref sharedPref;
    private ArrayList<String> selectedServices = new ArrayList<>();
    private ArrayList<String> allServices = new ArrayList<>();
    private ArrayList<String> vendorServices = new ArrayList<>();
    private Button update_services;
    private String items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_services);

        misc = new Misc(this);
        sharedPref = new SharedPref(this);

        update_services = findViewById(R.id.update);
        update_services.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedServices.size() < 6){
                    updateServices();
                }
                else{
                    misc.showToast("You can select maximum 5 services");
                }

            }
        });

        up_services = findViewById(R.id.update_services);

        if(misc.isConnectedToInternet()) {
            fetchVendorServices();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, com.creativeminds.facileapp.ServiceProviderActivities.ServiceHomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateServices() {
        items = "";
        for(String item : selectedServices){
            items += item+",";
        }

        if(items.isEmpty()){
            misc.showToast("Please select atleast one service");
            return;
        }

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load("DELETE", misc.ROOT_PATH+"delete_vendor_services/"+sharedPref.getUserId())
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            misc.showToast("Please check your connection");
                            pd.dismiss();
                            return;
                        }
                        else {
                            String res = result.getResult();
                            if(res.equals("ok")){
                                update();
                            }
                        }
                    }
                });

        misc.showToast(items);
    }

    private void update(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("services", items);

        Ion.with(this)
                .load("PUT", misc.ROOT_PATH+"update_vendor_services/"+sharedPref.getUserId())
                .setJsonObjectBody(jsonObject)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            misc.showToast("Please check your connection");
                            pd.dismiss();
                            return;
                        }
                        else {
                            String res = result.getResult();
                            if(res.equals("ok")){
                                misc.showToast(result.getResult());
                                pd.dismiss();
                                onBackPressed();
                            }
                            else{
                                misc.showToast(res);
                            }
                        }
                    }
                });

    }

    private void fetchVendorServices(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();


        Ion.with(this)
                .load(misc.ROOT_PATH+"vendor_services/"+sharedPref.getUserId())
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if( e != null) {
                            misc.showToast("Please check your connection");
                            pd.dismiss();
                            return;
                        }
                        else{
                            try {
                                pd.dismiss();
                                vendorServices.clear();
                                JSONArray jsonArray = new JSONArray(result.getResult());
                                if(jsonArray.length() > 0) {
                                    for(int i=0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                        String service = jsonObject.getString("fk_service_id");
                                        vendorServices.add(service);
                                    }
                                    fetchServices();
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void fetchServices(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading Services");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load(misc.ROOT_PATH+"fetch_dept_services")
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            misc.showToast("Please check your connection");
                            pd.dismiss();
                            return;
                        }

                        try {
                            JSONArray serviceArray = new JSONArray(result.getResult());
                            if(serviceArray.length() < 1) {
                                misc.showToast("No Service Found");
                            }
                            else{
                                allServices.clear();
                                for (int i = 0; i < serviceArray.length(); i++) {
                                    JSONObject serviceObject = (JSONObject) serviceArray.get(i);
                                    String serviceName = serviceObject.getString("service_name");
                                    String serviceId = serviceObject.getString("service_id");
                                    allServices.add(serviceId);

                                    final CheckBox cb = new CheckBox(getApplicationContext());
                                    cb.setText(serviceName.toUpperCase());
                                    cb.setTextColor(R.color.colorPrimary);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        cb.setButtonTintList(ColorStateList.valueOf(R.color.colorPrimary));
                                    }
                                    if(vendorServices.contains(serviceId)){
                                        selectedServices.add(serviceId);
                                        cb.setChecked(true);
                                    }
                                    cb.setId(Integer.parseInt(serviceId));
                                    cb.setTextSize(10);
                                    cb.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String selectedItem = String.valueOf(cb.getId());
                                            if(selectedServices.contains(selectedItem)){
                                                selectedServices.remove(selectedItem);
                                            }
                                            else{
                                                selectedServices.add(selectedItem);
                                            }
                                        }
                                    });

                                    up_services.addView(cb);
                                    pd.dismiss();
                                }
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }
}
