package com.creativeminds.facileapp.ServiceProviderActivities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.JobHistoryActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Vendor;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.SharedPref.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class VendorMapActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private String service_id, user_image, service_name, user_name, user_rating, vendor_id, customer_id = null;
    Misc misc;
    SharedPref sharedPref;
    private Marker myMarker;
    private double current_latitude, current_longitude;
    private double service_lat, service_lon;
    private Location currentLocation;
    private GoogleMap mMap;
    private String u_rating;
    private ArrayList<Vendor> vendorList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_map);

        Intent intent = getIntent();
        service_id = intent.getStringExtra("service_id");
        service_name = intent.getStringExtra("service_name");

        setTitle(service_name);

        misc = new Misc(this);
        sharedPref = new SharedPref(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(VendorMapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(VendorMapActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

        fetchLastLocation();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.vendor_map);
        mapFragment.getMapAsync(VendorMapActivity.this);
    }

    private void fetchLastLocation() {
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
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    current_latitude = currentLocation.getLatitude();
                    current_longitude = currentLocation.getLongitude();
                    //Toast.makeText(MapsActivity.this,currentLocation.getLatitude()+" "+currentLocation.getLongitude(),Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(VendorMapActivity.this,"No Location recorded", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(VendorMapActivity.this, AllProvidersActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(myMarker)){
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.service_provider_info);
            TextView serviceName = dialog.findViewById(R.id.service);
            serviceName.setText(service_name);

            TextView agree = dialog.findViewById(R.id.sure);
            agree.setText("Are you Sure you want to hire " + vendorList.get(0).getUserName());

            TextView rating = dialog.findViewById(R.id.provider_rating);
            if(u_rating != null){
                rating.setText("Rating: " + user_rating+"/"+5);
            }
            else {
                rating.setText("Rating: " + user_rating);
            }
            TextView name = dialog.findViewById(R.id.provider_name);
            name.setText("Name: " + vendorList.get(0).getUserName());

            ImageView imageView = dialog.findViewById(R.id.provider_image);
            if(vendorList.get(0).getUserImage().isEmpty()) {
                imageView.setImageResource(R.drawable.serviceicon);
            }
            else{
                Ion.with(this)
                        .load(vendorList.get(0).getUserImage())
                        .intoImageView(imageView);
            }

            TextView hire_button = dialog.findViewById(R.id.hire);
            TextView cancel_button = dialog.findViewById(R.id.cancel);

            hire_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    postJob();
                }
            });

            cancel_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
        return false;
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMarkerClickListener(this);

        if(misc.isConnectedToInternet()){
            fetchVendor();
        }
        else{
            misc.showToast("No Internet Connection");
        }
    }

    private void fetchVendor(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Finding " + service_name);
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load(misc.ROOT_PATH+"fetch_vendor/"+service_id+"&vendor_id="+sharedPref.getUserId())
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
                                    misc.showToast("No Vendor Found");
                                    pd.dismiss();
                                    return;
                                }
                                vendorList.clear();
                                for(int i = 0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                    vendor_id = jsonObject.getString("user_id");
                                    user_name = jsonObject.getString("user_name");
                                    user_image = jsonObject.getString("user_image").replace("\"", "");
                                    service_lat = Double.parseDouble(jsonObject.getString("user_lat"));
                                    service_lon = Double.parseDouble(jsonObject.getString("user_lon"));
                                    String latitude = jsonObject.getString("user_lat");
                                    String longitude = jsonObject.getString("user_lon");
                                    int user_jobs = jsonObject.getInt("daily_jobs");

                                    if(getDistance(service_lat, service_lon) < 7) {
                                        vendorList.add(new Vendor(vendor_id, user_name, user_image, latitude, longitude, user_jobs));
                                    }
                                }
                                if(vendorList.isEmpty()){
                                    misc.showToast("No Nearby " + service_name + " Found. Please visit again");
                                    pd.dismiss();
                                    return;
                                }
                                LatLng serviceLocation = new LatLng(Double.parseDouble(vendorList.get(0).getUserLat()), Double.parseDouble(vendorList.get(0).getUserLon()));
                                mMap.setMinZoomPreference(15);
                                myMarker = mMap.addMarker(new MarkerOptions().position(serviceLocation).title("Service Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(serviceLocation));
                                pd.dismiss();

                                // double distance = getDistanceFromLatLonInKm(current_latitude, current_longitude, service_lat, service_lon);
                                //  misc.showToast(String.valueOf(getDistance(Double.parseDouble(vendorList.get(0).getUserLat()), Double.parseDouble(vendorList.get(0).getUserLon()))));
                                fetchRating(vendorList.get(0).getUserId());
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void fetchRating(String id) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load(misc.ROOT_PATH+"fetch_average_rating/"+id)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if (e != null) {
                            misc.showToast("Please check your connection");
                            pd.dismiss();
                            return;
                        } else {
                            try {
                                JSONArray jsonArray = new JSONArray(result.getResult());
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                    user_rating = jsonObject.getString("rating");
                                    if(user_rating.equals("null")){
                                        user_rating = "Not rated yet";
                                        u_rating = null;
                                        pd.dismiss();
                                        return;
                                    }
                                    u_rating = user_rating;
                                }

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                            pd.dismiss();

                        }
                    }
                });
    }

    private void postJob() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        JsonObject job = new JsonObject();
        job.addProperty("job_start_date", dateFormat.format(date));
        job.addProperty("vendor_id", vendor_id);
        job.addProperty("customer_id", sharedPref.getUserId());
        job.addProperty("fk_service_id", service_id);

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Hiring " + user_name);
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load(misc.ROOT_PATH+"new_job")
                .setJsonObjectBody(job)
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
                            updateUserJobs(pd);
                        }
                    }
                });
    }

    private void updateUserJobs(final ProgressDialog pd){

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("jobs", vendorList.get(0).getUserJobs()+1);

        Ion.with(this)
                .load("PUT", misc.ROOT_PATH+"update_daily_jobs/"+vendorList.get(0).getUserId())
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
                        else{

                            pd.dismiss();
                            misc.showToast(result.getResult());
                            Intent intent = new Intent(getApplicationContext(), JobHistoryActivity.class);
                            intent.putExtra("vendor", 1);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    private double getDistance(double lat, double lon){
        Location locationA = new Location("Point A");
        locationA.setLatitude(current_latitude);
        locationA.setLongitude(current_longitude);

        Location locationB = new Location("Point B");
        locationB.setLatitude(lat);
        locationB.setLongitude(lon);

        return locationA.distanceTo(locationB)/1000;   // in km
    }
}
