package com.creativeminds.facileapp;

import android.Manifest;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Vendor;
import com.creativeminds.facileapp.SharedPref.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener{

    private GoogleMap mMap;
    private Marker myMarker;
    private double current_latitude, current_longitude;
    private double service_lat = 0, service_lon = 0.0;
    private String provider = "no";
    private String service_id, user_image, service_name, user_name, user_rating, vendor_id, customer_id = null;
    Misc misc;
    SharedPref sharedPref;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_REQUEST_CODE = 101;
    private String u_rating;
    private Toolbar toolbar;
    private ArrayList<Vendor> vendorList = new ArrayList<>();
    private TextView serviceTitle;
    private String departmentId,cartID,getCurrentDate,getCurrentTime;
    private Button datePick,timePick,confirmScheduleBooking;
    private ArrayList<String> fetchedDates = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //toolbar = findViewById(R.id.bar);

        misc = new Misc(this);
        sharedPref = new SharedPref(this);
        //misc.showToast(sharedPref.getUserId());

        Intent intent = getIntent();
        service_id = intent.getStringExtra("service_id");
        service_name = intent.getStringExtra("service_name");
        departmentId = intent.getStringExtra("department_id");
        cartID = intent.getStringExtra("cart_id");

        serviceTitle = findViewById(R.id.title);
        confirmScheduleBooking = findViewById(R.id.confirmScheduleBooking);
        datePick = findViewById(R.id.datePicker);
        timePick = findViewById(R.id.timePicker);
        serviceTitle.setText(service_name);

        confirmScheduleBooking.setOnClickListener(this);
        datePick.setOnClickListener(this);
        timePick.setOnClickListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        fetchLastLocation();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

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
                    Toast.makeText(MapsActivity.this,"No Location recorded", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                .load(misc.ROOT_PATH+"fetch_vendors/"+departmentId)
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
                                    misc.showToast("No " + service_name + " Found");
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

                                    vendorList.add(new Vendor(vendor_id, user_name, user_image, latitude, longitude, user_jobs));
//                                    if(getDistance(service_lat, service_lon) < 7) {
//                                        vendorList.add(new Vendor(vendor_id, user_name, user_image, latitude, longitude, user_jobs));
//                                    }
//                                    else {
//                                       misc.showToast("No service found within 7 KM area...");
//                                    }
                                }
                                if(vendorList.isEmpty()){
                                    //misc.showToast("No Nearby " + service_name + " Found. Please visit again");
                                    pd.dismiss();
                                    return;
                                }
                                LatLng serviceLocation = new LatLng(Double.parseDouble(vendorList.get(0).getUserLat()), Double.parseDouble(vendorList.get(0).getUserLon()));
                                mMap.setMinZoomPreference(13);
                                myMarker = mMap.addMarker(new MarkerOptions().position(serviceLocation).title("Service Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(serviceLocation));
                                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                                mMap.getUiSettings().setMapToolbarEnabled(true);

                                pd.dismiss();

                                // double distance = getDistanceFromLatLonInKm(current_latitude, current_longitude, service_lat, service_lon);
                                //  misc.showToast(String.valueOf(getDistance(Double.parseDouble(vendorList.get(0).getUserLat()), Double.parseDouble(vendorList.get(0).getUserLon()))));

                                //Fetching rating ...
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
                                        u_rating = null;
                                        user_rating = "Not rated yet";
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(myMarker)){
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.service_provider_info);

            TextView serviceName = dialog.findViewById(R.id.service);
            serviceName.setText(service_name);

            TextView agree = dialog.findViewById(R.id.sure);
            agree.setText("Are you sure you want to hire " + vendorList.get(0).getUserName());

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
            TextView schedule_button = dialog.findViewById(R.id.schedule);
            TextView cancel_button = dialog.findViewById(R.id.cancel);

            hire_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    deleteCart();
                    postJob();

                }
            });

            schedule_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    datePick.setVisibility(View.VISIBLE);
                    timePick.setVisibility(View.VISIBLE);
                    checkExistingBooking();
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
    public void onClick(View v) {
        if(v.getId() == datePick.getId()){
            //write date picking code here...
            showDatePicker();
        }
        if(v.getId() == timePick.getId()){
            //write time picking code here...
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "time picker");
        }
        if(v.getId() == confirmScheduleBooking.getId()){
            //write confirm booking code here...
         //   misc.showToast(getCurrentDate + " & " +getCurrentTime + " & " + service_name);
            confirmScheduleBooking.setVisibility(View.GONE);
            scheduleBooking();
            getJobId();
            deleteCart();
            changeCartStatus();
        }
    }

    public void checkExistingBooking(){
        Ion.with(this)
                .load("GET", misc.ROOT_PATH+"fetch_existing_booking/"+vendor_id)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(result.getResult() == null){

                        }
                        else{
                            try {
                                JSONArray jsonArray = new JSONArray(result.getResult());
                                for(int i = 0; i < jsonArray.length(); i++){
                                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                    String fetchDate = jsonObject.getString("job_start_date");
                                    fetchedDates.add(fetchDate);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dpd.show(this.getFragmentManager(), "DatePickerDialog");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = null;

        for(int a = 0; a < fetchedDates.size(); a++){
            try {
                date = sdf.parse(fetchedDates.get(a));

            } catch (ParseException e) {
                e.printStackTrace();
            }
            calendar = dateToCalendar(date);
            List<Calendar> dates = new ArrayList<>();
            dates.add(calendar);
            Calendar[] disabledDays1 = dates.toArray(new Calendar[dates.size()]);
            dpd.setDisabledDays(disabledDays1);
        }
    }

    private Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd");
        getCurrentDate  = simpleDate.format(calendar.getTime());

        try {
            if(!checkDates()){
                Toast.makeText(this, "Please Pick Valid Date", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public boolean checkDates() throws ParseException {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd");
        String getAbhiKiDate = simpleDate.format(calendar.getTime());

        String d1 = getCurrentDate;
        String d2 = getAbhiKiDate;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = sdf.parse(d1);
        Date current = sdf.parse(d2);

        if (current.compareTo(date1) >= 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        getCurrentTime = "Hours: " + hourOfDay + " Minutes: "+ minute;
        if(!getCurrentDate.equalsIgnoreCase("") && !getCurrentTime.equalsIgnoreCase("")){
            datePick.setVisibility(View.GONE);
            timePick.setVisibility(View.GONE);
            confirmScheduleBooking.setVisibility(View.VISIBLE);
        }
        else{
            misc.showToast("Please Select Date & Time to Confirm Scheduling");
        }
    }

    public void scheduleBooking(){

        JsonObject job = new JsonObject();
        job.addProperty("job_start_date", getCurrentDate);
        job.addProperty("vendor_id", vendorList.get(0).getUserId());
        job.addProperty("customer_id", sharedPref.getUserId());
        job.addProperty("fk_service_id", service_id);
        job.addProperty("cus_lat", current_latitude);
        job.addProperty("cus_lon", current_longitude);

        final ProgressDialog pd = new ProgressDialog(this);
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
                            misc.showToast("Your job has been scheduled");
                            pd.dismiss();
                        }
                    }
                });
    }

    public void getJobId(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load("GET", misc.ROOT_PATH+"get_job_id/"+vendorList.get(0).getUserId())
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
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                    String job_id = jsonObject.getString("job_id");
                                    updateJobStatus(job_id);
                                    pd.dismiss();
                                }
                            }
                            catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    public void updateJobStatus(String jobIdd){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"schedule_job/"+jobIdd)
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
                            startActivity(new Intent(getApplicationContext(), AllDepartmentsActivity.class));
                            finish();
                        }
                    }
                });
    }

    private void postJob() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        JsonObject job = new JsonObject();
        job.addProperty("job_start_date", dateFormat.format(date));
        job.addProperty("vendor_id", vendorList.get(0).getUserId());
        job.addProperty("customer_id", sharedPref.getUserId());
        job.addProperty("fk_service_id", service_id);
        job.addProperty("cus_lat", current_latitude);
        job.addProperty("cus_lon", current_longitude);

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
                            deleteCart();
                            pd.dismiss();
                            startActivity(new Intent(getApplicationContext(), com.creativeminds.facileapp.JobHistoryActivity.class));
                            finish();
                        }
                    }
                });
    }

    public void deleteCart(){
        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"cancel_cart/"+cartID)
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
                           // misc.showToast("Cart Deleted");
                        }
                    }
                });
    }

    public void changeCartStatus(){
        JsonObject cart1 = new JsonObject();
        cart1.addProperty("service_cart", "uncarted");
        Ion.with(this)
                .load("PUT",misc.ROOT_PATH + "update_cart_status/"+service_id)
                .setJsonObjectBody(cart1)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            misc.showToast("Please check your connection");
                            return;
                        }
                        String response = result.getResult();
                        if (response.isEmpty()) {
                            return;
                        }
                        else{
                            //misc.showToast("Status Changed!");
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, AllDepartmentsActivity.class);
        intent.putExtra("department_id", departmentId);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLastLocation();
                } else {
                    Toast.makeText(MapsActivity.this,"Location permission missing", Toast.LENGTH_SHORT).show();
                }
                break;
        }
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

    public double getDistanceFromLatLonInKm(double startLat, double startLong, double endLat, double endLong) {
        try {
            final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

            double dLat = Math.toRadians((endLat - startLat));
            double dLong = Math.toRadians((endLong - startLong));

            startLat = Math.toRadians(startLat);
            endLat = Math.toRadians(endLat);

            double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return EARTH_RADIUS * c; // <-- d
        } catch (Exception ex) {
            ex.printStackTrace();

            return 0;
        }
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

}
