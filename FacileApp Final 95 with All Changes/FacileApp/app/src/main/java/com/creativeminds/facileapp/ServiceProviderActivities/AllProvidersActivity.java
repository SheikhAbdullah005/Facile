package com.creativeminds.facileapp.ServiceProviderActivities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.Adapters.VendorServiceAdapter;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Service;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.SharedPref.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AllProvidersActivity extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener {

    private RecyclerView serviceRecycler;
    private ArrayList<Service> serviceListModel;
    private SearchView searchService = null;
    private Context context;
    Misc misc;
    SharedPref sharedPref;
    VendorServiceAdapter vendorServiceAdapter;
    private SwipeRefreshLayout refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_providers);
        setTitle("Hire Service Providers");

        context = this;

        misc = new Misc(this);
        sharedPref = new SharedPref(this);

        serviceListModel = new ArrayList<>();

        searchService = findViewById(R.id.sv_search);
        refresh = findViewById(R.id.swipe);
        refresh.setColorSchemeResources(R.color.colorPrimary);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        serviceRecycler = findViewById(R.id.vendor_services);
        serviceRecycler.setLayoutManager(new GridLayoutManager(this, 2));

        searchService.setOnQueryTextListener(this);
        searchService.setOnClickListener(this);

        if(misc.isConnectedToInternet()){
            getServices();
        }
        else{
            misc.showToast("No Internet Connection");
        }
    }

    private void refreshContent() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getServices();
                refresh.setRefreshing(false);
            }
        });
    }

    public void getServices(){
        Ion.with(this)
                .load(misc.ROOT_PATH+"get_services")
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
                                    serviceListModel.add(new Service(service_id, service_name,department_name,service_des,service_price,""));
                                }
                                vendorServiceAdapter = new VendorServiceAdapter(context, serviceListModel);
                                vendorServiceAdapter.setTemp(serviceListModel);
                                serviceRecycler.setAdapter(vendorServiceAdapter);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == searchService.getId()) {
            searchService.onActionViewExpanded();
            searchService.setIconified(false);

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AllProvidersActivity.this, ServiceHomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if (vendorServiceAdapter != null) {
            vendorServiceAdapter.filter(s);
        }
        return true;

    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (vendorServiceAdapter != null) {
            vendorServiceAdapter.filter(s);
        }
        return true;

    }
}
