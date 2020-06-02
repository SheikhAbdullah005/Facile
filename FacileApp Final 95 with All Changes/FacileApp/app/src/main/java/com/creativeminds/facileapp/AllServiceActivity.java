package com.creativeminds.facileapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.creativeminds.facileapp.ServiceProviderActivities.PaymentActivity;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.Adapters.CustomerServiceAdapter;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Service;
import com.creativeminds.facileapp.SharedPref.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AllServiceActivity extends AppCompatActivity
        implements View.OnClickListener, SearchView.OnQueryTextListener {

    Misc misc;
    SharedPref sharedPref;
    private RecyclerView recyclerView;
    private TextView noItem;
    CustomerServiceAdapter customerServiceAdapter;
    private ArrayList<Service> serviceListModel;
    private SearchView searchService = null;
    private Context context;
    private SwipeRefreshLayout refresh;
    private String userOnline;
    String departmentId, departmentName;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.CALL_PHONE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_service);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Hire Service Providers");

        context = this;

        misc = new Misc(this);
        sharedPref = new SharedPref(this);

        serviceListModel = new ArrayList<>();
        refresh = findViewById(R.id.swipe);
        refresh.setColorSchemeResources(R.color.colorPrimary);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        searchService = findViewById(R.id.sv_search);
        noItem = findViewById(R.id.noItem);
        recyclerView = findViewById(R.id.customer_services);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        userOnline = sharedPref.getUserOnline();
        Intent intent = getIntent();
        departmentId = intent.getStringExtra("department_id");
        departmentName = intent.getStringExtra("department_name");

        searchService.setOnQueryTextListener(this);
        searchService.setOnClickListener(this);

        if(misc.isConnectedToInternet()){
            getServices();
        }
        else{
            misc.showToast("No Internet Connection");
        }
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
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
                .load(misc.ROOT_PATH+"fetch_single_dept_services/"+departmentId)
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
                                if(jsonArray.length() < 1) {
                                    misc.showToast("No Service Found");
                                }
                                else
                                {
                                    serviceListModel.clear();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                        String service_id = jsonObject.getString("service_id");
                                        String service_name = jsonObject.getString("service_name");
                                        String service_des = jsonObject.getString("service_des");
                                        String service_price = jsonObject.getString("service_price");
                                        String department_name = jsonObject.getString("department_name");
                                        serviceListModel.add(new Service(service_id, service_name, department_name, service_des, service_price, departmentId));
                                    }
                                    customerServiceAdapter = new CustomerServiceAdapter(context, serviceListModel);
                                    customerServiceAdapter.setTemp(serviceListModel);
                                    recyclerView.setAdapter(customerServiceAdapter);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(AllServiceActivity.this, AllDepartmentsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onClick(View v) {
        if(v.getId() == searchService.getId()) {
            searchService.onActionViewExpanded();
            searchService.setIconified(false);

        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if (customerServiceAdapter != null) {
            customerServiceAdapter.filter(s);
        }
        return true;

    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (customerServiceAdapter != null) {
            customerServiceAdapter.filter(s);
        }
        return true;

    }
}
