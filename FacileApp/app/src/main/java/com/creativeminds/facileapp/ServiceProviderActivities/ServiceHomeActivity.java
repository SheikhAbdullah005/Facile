package com.creativeminds.facileapp.ServiceProviderActivities;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.creativeminds.facileapp.ComplainActivity;
import com.creativeminds.facileapp.CustomerAllComplaints;
import com.creativeminds.facileapp.JobHistoryActivity;
import com.creativeminds.facileapp.VendorPayment;
import com.creativeminds.facileapp.addNewCreditCard;
import com.creativeminds.facileapp.wallet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.HelpActivity;
import com.creativeminds.facileapp.LoginActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServiceHomeActivity extends AppCompatActivity
        implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private Marker myMarker;
    private double current_latitude, current_longitude;
    private TextView txt1, txt2;
    private CircularImageView img1;
    Misc misc;
    String venderId;
    DrawerLayout drawercheck;
    boolean doubleBackToExitPressedOnce=false;
    TextView cancel,see;
    SharedPref sharedPref;
    private String password, userOnline;
    Dialog dialog;

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
        setContentView(R.layout.activity_service_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Home");

        sharedPref = new SharedPref(this);
        misc = new Misc(this);
        venderId = sharedPref.getUserId();
        userOnline = sharedPref.getUserOnline();
        drawercheck = (DrawerLayout) findViewById(R.id.drawer_layout);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
       // txt1 = headerView.findViewById(R.id.textView);
        txt2 = headerView.findViewById(R.id.textView1);
      //  img1 = headerView.findViewById(R.id.imageView);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().findItem(R.id.nav_item1)
                .setActionView(new Switch(this));

        Switch drawerSwitch = (Switch) navigationView.getMenu().findItem(R.id.nav_item1).getActionView();

        if(userOnline.equals("yes")) {
            ((Switch) navigationView.getMenu().findItem(R.id.nav_item1).getActionView()).setChecked(true);
        }else {
            ((Switch) navigationView.getMenu().findItem(R.id.nav_item1).getActionView()).setChecked(false);
        }

        drawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPref.updateUserOnline("yes");
                    updateUserOnlineStatus("yes");
                } else {
                    sharedPref.updateUserOnline("no");
                    updateUserOnlineStatus("no");
                }
            }
        });
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        syncMap();
        ifJobExists();
    }

    private void ifJobExists() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.new_request);
        cancel = dialog.findViewById(R.id.cancel);
        see = dialog.findViewById(R.id.see);
        //misc.showToast("No job found");
        Ion.with(this)
                .load(misc.ROOT_PATH+"all_in_progress_jobs")
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
                                if(l==0){
                                   //misc.showToast("No job found");
                                    //dialog.dismiss();
                                }else{
                                    notification();
                                    dialog.show();
                                    dialog.setCancelable(false);
                                   // misc.showToast("No");
                                    cancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.dismiss();
                                        }
                                    });
                                    see.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            dialog.dismiss();
                                            Intent providers = new Intent(getApplicationContext(), ProviderJobsActivity.class);
                                            startActivity(providers);
                                            finish();
                                        }
                                    });
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }
    public void notification(){
        Intent intent = new Intent(this, ProviderJobsActivity.class);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("n","n", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, uniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"n")
                .setContentTitle("New Service")
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText("You have a new service.")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_send_black_24dp)
                .setAutoCancel(true);
        NotificationManagerCompat managerCompact = NotificationManagerCompat.from(this);
        managerCompact.notify(999,builder.build());
    }
    private void updateUserOnlineStatus(String status) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Updating status");
        pd.setCancelable(false);
        pd.show();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", status);

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"update_status/"+sharedPref.getUserId())
                .setJsonObjectBody(jsonObject)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            misc.showToast("Please Check your connection");
                            pd.dismiss();
                            return;
                        }
                        else{
                            misc.showToast(result.getResult());
                            pd.dismiss();
                        }
                    }
                });

    }

    private void syncMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.service_map);
        mapFragment.getMapAsync(ServiceHomeActivity.this);
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.service_profile) {
            Intent profile = new Intent(this, ServiceProfileActivity.class);
            startActivity(profile);
            finish();
        } else if (id == R.id.service_home) {
            Intent home = new Intent(this, ServiceHomeActivity.class);
            startActivity(home);
            finish();
        }else if (id == R.id.wallet) {
            Ion.with(this)
                    .load(misc.ROOT_PATH+"get_wallet/"+venderId)
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
                                    if(jsonArray.length()>0) {
                                        Intent wallet = new Intent(getApplicationContext(), com.creativeminds.facileapp.wallet.class);
                                        wallet.putExtra("id", venderId);
                                        startActivity(wallet);
                                        finish();
                                    }else{
                                        Intent wallet_1 = new Intent(getApplicationContext(), addNewCreditCard.class);
                                        wallet_1.putExtra("customerId", venderId);
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
        else if (id == R.id.service_jobs) {
            Intent providers = new Intent(this, ProviderJobsActivity.class);
            startActivity(providers);
            finish();
        }else if (id == R.id.complaint) {
            Intent update = new Intent(this, CustomerAllComplaints.class);
            update.putExtra("from", "ven");
            startActivity(update);
            finish();
        } else if (id == R.id.service_help) {
            Intent help = new Intent(this, HelpActivity.class);
            help.putExtra("provider", "yes");
            startActivity(help);
            finish();
        } else if (id == R.id.logout) {
            sharedPref.clearSession();
            Intent logout = new Intent(this, LoginActivity.class);
            startActivity(logout);
            finish();
        }
        else if (id == R.id.up_password) {
            Intent updatePassword = new Intent(this, com.creativeminds.facileapp.ServiceProviderActivities.SerivceUpdatePasswordActivity.class);
            updatePassword.putExtra("password", password);
            startActivity(updatePassword);
            finish();
        }
        else if (id == R.id.make_payment) {
            Intent payment = new Intent(this, VendorPayment.class);
            startActivity(payment);
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        if(misc.isConnectedToInternet()) {
            fetchUserProfile();
        }
        else{
            misc.showToast("No Internet Connection");
        }
    }

    private void fetchUserProfile(){

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        String id = sharedPref.getUserId();
        Ion.with(this)
                .load(misc.ROOT_PATH+"user_profile/"+id)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            pd.dismiss();
                            misc.showToast("Internet Connection Problem");
                            return;
                        }
                        else{
                            try {
                                pd.dismiss();
                                JSONObject jsonObject = new JSONObject(result.getResult());
                                current_latitude = Double.parseDouble(jsonObject.getString("user_lat"));
                                current_longitude = Double.parseDouble(jsonObject.getString("user_lon"));
                                String user_image = jsonObject.getString("user_image");

                                if(user_image.isEmpty()){
                                   // img1.setImageResource(R.drawable.serviceicon);
                                }
                                else{
                                  //  Ion.with(getApplicationContext()).load(jsonObject.getString("user_image").replace("\"","")).intoImageView(img1);
                                }

                                password = jsonObject.getString("user_password");

                                LatLng service = new LatLng(current_latitude, current_longitude);
                                mMap.setMinZoomPreference(13);
                                myMarker = mMap.addMarker(new MarkerOptions().position(service).title("Service Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(service));
                                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                                mMap.getUiSettings().setMapToolbarEnabled(true);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
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
