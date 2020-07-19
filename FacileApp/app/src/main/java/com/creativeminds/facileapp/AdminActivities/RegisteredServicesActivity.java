package com.creativeminds.facileapp.AdminActivities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.Adapters.RegisteredServicesAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RegisteredServicesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnClickListener {

    com.creativeminds.facileapp.Misc.Misc misc;
    com.creativeminds.facileapp.SharedPref.SharedPref sharedPref;
    private RecyclerView recyclerView;
    RegisteredServicesAdapter registeredServicesAdapter;
    private ArrayList<com.creativeminds.facileapp.Models.Service> serviceListModel;
    private SearchView searchService = null;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_services);
        setTitle("Registered Services");


        context = this;

        misc = new com.creativeminds.facileapp.Misc.Misc(this);
        sharedPref = new com.creativeminds.facileapp.SharedPref.SharedPref(this);

        serviceListModel = new ArrayList<>();

        searchService = findViewById(R.id.sv_search);
        recyclerView = findViewById(R.id.customer_services);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        searchService.setOnQueryTextListener(this);
        searchService.setOnClickListener(this);

        if(misc.isConnectedToInternet()){
            getServices();
        }
        else{
            misc.showToast("No Internet Connection");
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, com.creativeminds.facileapp.AdminActivities.AdminHomeActivity.class);
        startActivity(intent);
        finish();
    }

    public void getServices(){
        Ion.with(this)
                .load(misc.ROOT_PATH+"fetch_dept_services")
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            misc.showToast("Please check your connection");
                            return;
                        }
                        else{
                            try {
                                JSONArray jsonArray = new JSONArray(result.getResult());
                                serviceListModel.clear();
                                for(int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                    String service_id = jsonObject.getString("service_id");
                                    String service_name = jsonObject.getString("service_name");
                                    String service_des = jsonObject.getString("service_des");
                                    String service_price = jsonObject.getString("service_price");
                                    String department_name = jsonObject.getString("department_name");
                                    String department_id = jsonObject.getString("department_id");
                                    serviceListModel.add(new com.creativeminds.facileapp.Models.Service(service_id, service_name,department_name,service_des,service_price,department_id));
                                }
                                registeredServicesAdapter = new RegisteredServicesAdapter(context, serviceListModel);
                                registeredServicesAdapter.setTemp(serviceListModel);
                                recyclerView.setAdapter(registeredServicesAdapter);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if (registeredServicesAdapter != null) {
            registeredServicesAdapter.filter(s);
        }
        return true;

    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (registeredServicesAdapter != null) {
            registeredServicesAdapter.filter(s);
        }
        return true;

    }

    @Override
    public void onClick(View v) {

    }
}
