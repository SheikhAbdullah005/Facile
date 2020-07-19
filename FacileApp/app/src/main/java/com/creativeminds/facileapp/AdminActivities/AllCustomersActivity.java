package com.creativeminds.facileapp.AdminActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.Adapters.AllCustomersAdapter;
import com.creativeminds.facileapp.Adapters.AllVendorsAdapter;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Customer;
import com.creativeminds.facileapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AllCustomersActivity extends AppCompatActivity {

    private RecyclerView view;
    private ArrayList<Customer> customersListModel;
    private AllCustomersAdapter allCustomersAdapter;
    private AllVendorsAdapter allVendorsAdapter;
    Misc misc;
    private Context context;
    private String id;
    private int vendor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_customers);
        setTitle("Registered Users");

        context = this;

        misc = new Misc(this);
        customersListModel = new ArrayList<>();

        view = findViewById(R.id.all_customer_recycler_view);
        view.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        vendor = intent.getIntExtra("vendors", 0);
        String serviceName = intent.getStringExtra("service_name");
        id = intent.getStringExtra("service_id");

        if(misc.isConnectedToInternet()){
            if(vendor == 1) {
                setTitle("ALL " + serviceName.toUpperCase()+"S");
                fetchAllVendors();
            }
            else{
                fetchAllCustomers();
            }

        }
        else{
            misc.showToast("No Internet Connection");
        }
    }

    private void fetchAllCustomers(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load(misc.ROOT_PATH+"all_customers")
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
                        else{
                            try {
                                JSONArray jsonArray = new JSONArray(result.getResult());
                                if(jsonArray.length() < 1) {
                                    misc.showToast("No Customers Registered.");
                                    pd.dismiss();
                                    return;
                                }
                                else{
                                    pd.dismiss();
                                    customersListModel.clear();
                                    for(int i = 0; i < jsonArray.length(); i++){
                                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                        String user_name = jsonObject.getString("user_name");
                                        String user_id = jsonObject.getString("user_id");
                                        String user_email = jsonObject.getString("user_email");
                                        String user_phone = jsonObject.getString("user_phone");
                                        String user_cnic = jsonObject.getString("user_cnic");
                                        String user_status = jsonObject.getString("user_status");
                                        String user_image = jsonObject.getString("user_image").replace("\"","");

                                        customersListModel.add(new Customer(user_id, user_name, user_phone, user_email, user_cnic, user_image, user_status));

                                    }
                                    allCustomersAdapter = new AllCustomersAdapter(context, customersListModel);
                                    view.setAdapter(allCustomersAdapter);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void fetchAllVendors(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load(misc.ROOT_PATH+"all_service_providers/"+id)
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
                        else{
                            try {
                                JSONArray jsonArray = new JSONArray(result.getResult());
                                if(jsonArray.length() < 1) {
                                    misc.showToast("No Customers Registered.");
                                    pd.dismiss();
                                    return;
                                }
                                else{
                                    pd.dismiss();
                                    customersListModel.clear();
                                    for(int i = 0; i < jsonArray.length(); i++){
                                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                        String user_name = jsonObject.getString("user_name");
                                        String user_id = jsonObject.getString("user_id");
                                        String user_email = jsonObject.getString("user_email");
                                        String user_phone = jsonObject.getString("user_phone");
                                        String user_cnic = jsonObject.getString("user_cnic");
                                        String user_status = jsonObject.getString("user_status");
                                        String user_image = jsonObject.getString("user_image").replace("\"","");

                                        customersListModel.add(new Customer(user_id, user_name, user_phone, user_email, user_cnic, user_image, user_status));

                                    }
                                    allVendorsAdapter = new AllVendorsAdapter(context, customersListModel);
                                    view.setAdapter(allVendorsAdapter);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }
    @Override
    public void onBackPressed() {
        if(vendor == 1) {
            Intent intent = new Intent(this, AllServiceProvidersActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Intent intent = new Intent(this, AdminHomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
