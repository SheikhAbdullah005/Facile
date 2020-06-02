package com.creativeminds.facileapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.widget.TextView;

import com.creativeminds.facileapp.Adapters.ComplaintsAdapter;
import com.creativeminds.facileapp.Adapters.CompletedJobsAdapter;
import com.creativeminds.facileapp.Adapters.CustomerServiceAdapter;
import com.creativeminds.facileapp.AdminActivities.AdminHomeActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Complain;
import com.creativeminds.facileapp.Models.Job;
import com.creativeminds.facileapp.Models.Service;
import com.creativeminds.facileapp.ServiceProviderActivities.ServiceHomeActivity;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CustomerAllComplaints extends AppCompatActivity {

    private RecyclerView view;
    private Context context;
    private ArrayList<Complain> complainsListModel;
    ComplaintsAdapter complaintsAdapter;
    Misc misc;
    SharedPref sharedPref;
    String fromVendor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_all_complaints);
        setTitle("All Complaints");

        context = this;
        misc = new Misc(this);
        sharedPref = new SharedPref(this);
        complainsListModel = new ArrayList<>();
        view = findViewById(R.id.customer_comp);
        view.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        fromVendor = intent.getStringExtra("from");

        if(misc.isConnectedToInternet()) {
            fetchComplains();
        }
        else{
            misc.showToast("No Internet Connection");
        }
    }
    private void fetchComplains() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("Fetching Complaints...");
        pd.show();

        Ion.with(this)
                .load(misc.ROOT_PATH+"all_single_user_complaints/"+sharedPref.getUserId())
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
                                    misc.showToast("No Complaints Found");
                                    pd.dismiss();
                                    return;
                                }
                                else{
                                    pd.dismiss();
                                    complainsListModel.clear();
                                    for(int i = 0; i < jsonArray.length(); i++){
                                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                        String complain_id = jsonObject.getString("complaint_id");
                                        String complain_msg = jsonObject.getString("complaint_message");
                                        String against_user_id = jsonObject.getString("against_user_id");
                                        String against_user_name = jsonObject.getString("against_user_name");
                                        String fk_user_id = jsonObject.getString("fk_user_id");
                                        complainsListModel.add(new Complain(complain_id, complain_msg, against_user_name, against_user_id, fk_user_id));
                                    }
                                    complaintsAdapter = new ComplaintsAdapter(context, complainsListModel);
                                    view.setAdapter(complaintsAdapter);
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
        if(fromVendor.equalsIgnoreCase("ven")){
            Intent intent = new Intent(this, ServiceHomeActivity.class);
            startActivity(intent);
            finish();
        }
        else if(fromVendor.equalsIgnoreCase("cus")){
            Intent intent = new Intent(this, AllDepartmentsActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
