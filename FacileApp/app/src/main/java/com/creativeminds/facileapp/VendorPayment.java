package com.creativeminds.facileapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.creativeminds.facileapp.Adapters.PaymentsAdapter;
import com.creativeminds.facileapp.Adapters.VendorPaymentsAdapter;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Payment;
import com.creativeminds.facileapp.ServiceProviderActivities.ServiceHomeActivity;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VendorPayment extends AppCompatActivity {

    private RecyclerView view;
    private Context context;
    private ArrayList<Payment> vendorPaymentsListModel;
    VendorPaymentsAdapter vendorPaymentsAdapter;
    Misc misc;
    SharedPref sharedPref;
   // String fromVendor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_payment);
        setTitle("All Payments");
        context = this;
        misc = new Misc(this);
        sharedPref = new SharedPref(this);
        vendorPaymentsListModel = new ArrayList<>();
        view = findViewById(R.id.customer_payment);
        view.setLayoutManager(new LinearLayoutManager(this));

        if(misc.isConnectedToInternet()) {
            fetchPayments();
        }
        else{
            misc.showToast("No Internet Connection");
        }
    }

    private void fetchPayments() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("Fetching Vendor Payments...");
        pd.show();

        Ion.with(this)
                .load(misc.ROOT_PATH+"all_vendor_payments/"+sharedPref.getUserId())
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
                                    misc.showToast("No Payments Found");
                                    pd.dismiss();
                                    return;
                                }
                                else{
                                    pd.dismiss();
                                    vendorPaymentsListModel.clear();
                                    for(int i = 0; i < jsonArray.length(); i++){
                                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                        String job_id = jsonObject.getString("job_id");
                                        String total_bill = jsonObject.getString("total_bill");
                                        String customer_id = jsonObject.getString("customer_id");
                                        String vendor_id = jsonObject.getString("vendor_id");
                                        String cash_status = jsonObject.getString("cash_status");
                                        vendorPaymentsListModel.add(new Payment(job_id, total_bill, vendor_id, customer_id, cash_status));
                                    }
                                    vendorPaymentsAdapter = new VendorPaymentsAdapter(context, vendorPaymentsListModel);
                                    view.setAdapter(vendorPaymentsAdapter);
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
        Intent intent = new Intent(this, ServiceHomeActivity.class);
        startActivity(intent);
        finish();

    }
}
