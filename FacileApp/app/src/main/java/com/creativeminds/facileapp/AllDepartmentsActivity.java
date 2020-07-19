package com.creativeminds.facileapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.creativeminds.facileapp.Adapters.CustomerDepartmentAdapter;
import com.creativeminds.facileapp.Adapters.CustomerServiceAdapter;
import com.creativeminds.facileapp.Adapters.InProgressJobsAdapter;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Department;
import com.creativeminds.facileapp.Models.Job;
import com.creativeminds.facileapp.Models.Service;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AllDepartmentsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    Misc misc;
    boolean doubleBackToExitPressedOnce = false;
    SharedPref sharedPref;
    private RecyclerView recyclerView;
    CustomerDepartmentAdapter customerDepartmentAdapter;
    private ArrayList<Department> serviceListModel;
    private SearchView searchService = null;
    DrawerLayout drawercheck;
    private Context context;
    private String userOnline,customerId;
    String department_id, getAbhiKiDate;


    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_department);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Departments");

        context = this;

        misc = new Misc(this);
        sharedPref = new SharedPref(this);
        customerId = sharedPref.getUserId();
        //Toast.makeText(this,customerId,Toast.LENGTH_LONG).show();
        serviceListModel = new ArrayList<>();

        recyclerView = findViewById(R.id.customer_departments);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        userOnline = sharedPref.getUserOnline();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        drawercheck = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(misc.isConnectedToInternet()){
            getServices();
        }
        else{
            misc.showToast("No Internet Connection");
        }
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        getAbhiKiDate = dateFormat.format(date);
        // Check if you have any scheduled booking of the current date.
        checkScheduleBooking();
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

    public void checkScheduleBooking(){
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Please wait... ");
        pd.setCancelable(false);
        pd.show();
        Ion.with(context)
                .load(misc.ROOT_PATH+"schedule_jobs/"+sharedPref.getUserId())
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
                                    pd.dismiss();
                                    return;
                                }
                                for(int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                    String job_id = jsonObject.getString("job_id");
                                    String job_start_date = jsonObject.getString("job_start_date");
                                    String vendor_id = jsonObject.getString("vendor_id");

                                    // Scheduled Booking Logic
                                    if(getAbhiKiDate.equalsIgnoreCase(job_start_date)){
                                        updateJobStatus(job_id);
                                        updateUserJobs(vendor_id);
                                    }else{
                                    }
                                   }

                                pd.dismiss();
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                            pd.dismiss();
                        }
                    }
                });
    }

    private void updateJobStatus(String jobId){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"schedule_job_status/"+jobId)
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
                            pd.dismiss();
                        }
                    }
                });
    }

    private void updateUserJobs(String vendorId){

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("jobs", 0+1);

        Ion.with(this)
                .load("PUT", misc.ROOT_PATH+"update_daily_jobs/"+vendorId)
                .setJsonObjectBody(jsonObject)
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
                            startActivity(new Intent(getApplicationContext(), com.creativeminds.facileapp.JobHistoryActivity.class));
                            finish();
                        }
                    }
                });
    }

    public void getServices(){
        Ion.with(this)
                .load(misc.ROOT_PATH+"get_department")
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
                                    department_id = jsonObject.getString("department_id");
                                    String department_name = jsonObject.getString("department_name");
                                    serviceListModel.add(new Department(department_id,department_name));
                                }
                                customerDepartmentAdapter = new CustomerDepartmentAdapter(context, serviceListModel);
                                customerDepartmentAdapter.setTemp(serviceListModel);
                                recyclerView.setAdapter(customerDepartmentAdapter);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.customer_home) {
            Intent home = new Intent(this, AllDepartmentsActivity.class);
            startActivity(home);
            finish();
        } else if (id == R.id.customer_profile) {
            Intent profile = new Intent(this, ProfileActivity.class);
            startActivity(profile);
            finish();
        }else if(id== R.id.wallet){
            Ion.with(this)
                    .load(misc.ROOT_PATH+"get_wallet/"+customerId)
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
                                    int l =0;
                                    JSONArray jsonArray = new JSONArray(result.getResult());
                                    for (int i = 0; i < jsonArray.length(); i++) {l++;}
                                    if(l>0) {
                                        Intent wallet = new Intent(getApplicationContext(), wallet.class);
                                        wallet.putExtra("id", customerId);
                                        startActivity(wallet);
                                        finish();
                                    }else{
                                        Intent wallet_1 = new Intent(getApplicationContext(), addNewCreditCard.class);
                                        wallet_1.putExtra("customerId", customerId);

                                        startActivity(wallet_1);
                                        finish();
                                    }
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }

                        }
                    });
        }
        else if (id == R.id.cart) {
            Intent cart = new Intent(this, AddtoCart.class);
            cart.putExtra("customer_id", customerId);
            startActivity(cart);
            finish();
        }
        else if (id == R.id.customer_history) {
            Intent job = new Intent(this, JobHistoryActivity.class);
            startActivity(job);
            finish();
        }
        else if (id == R.id.make_payment) {
            Intent pay = new Intent(this, CustomerPayment.class);
            startActivity(pay);
            finish();
        }
        else if (id == R.id.customer_complaints) {
            Intent complain = new Intent(this, CustomerAllComplaints.class);
            complain.putExtra("from", "cus");
            startActivity(complain);
            finish();
        } else if (id == R.id.customer_help) {
            Intent help = new Intent(this, HelpActivity.class);
            help.putExtra("provider", "no");
            startActivity(help);
            finish();
        } else if (id == R.id.customer_logout) {
            sharedPref.clearSession();
            Intent logout = new Intent(this, LoginActivity.class);
            startActivity(logout);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
//        if(v.getId() == searchService.getId()) {
//            searchService.onActionViewExpanded();
//            searchService.setIconified(false);
//
//        }
    }
    @Override
    public void onBackPressed() {
        if (drawercheck.isDrawerOpen(GravityCompat.START)) {
            drawercheck.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Snackbar.make(this.getWindow().getDecorView().findViewById(android.R.id.content), "Press back again to exit", Snackbar.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }
}
