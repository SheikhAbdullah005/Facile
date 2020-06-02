package com.creativeminds.facileapp;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.creativeminds.facileapp.Adapters.CustomerDepartmentAdapter;
import com.creativeminds.facileapp.AdminActivities.AllJobsActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Department;
import com.creativeminds.facileapp.Models.UploadPdf;
import com.creativeminds.facileapp.ServiceProviderActivities.ProviderJobsActivity;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.view.View.GONE;

public class VendorInProgressJobDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private Marker myMarker;
    private double current_latitude, current_longitude;
    private Location currentLocation;
    private String phoneNumber, jobId,vendorId, name, service, phone, city, price,estimatedTime, totalDistance;
    private Button jobDetails, chat, complaints, acceptReq, calculatePrice,startFinalPrice, calculateFinalPrice;
    private EditText meesage;
    private TextView esTime, distance, sPrice,finalPr;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_REQUEST_CODE = 101;
    SharedPref sharedPref;
    Misc misc;
    private boolean show = false;
    private String customerId,customerPhoneNumber;
    private CardView jobArea;

    //Distance and time variables
    LatLng customerLatLong;
    LatLng vendorLatLong;
    ArrayList<LatLng> listPoints = new ArrayList<>();
    Double customerLat, customerLon;
    FirebaseAuth mAuth;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    String currentUserID,currentJobStatus,finalPrice,customerName,currentJobFinalStartTime;
    String lat, lon;
    public String initialJobStartTime,initialJobEndTime,initialTimeFromDB,finalJobStartTime,finalTimeFromDB,finalJobEndTime;
    String distancee,duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_in_progress_job_details);
        setTitle("Job In Progress");

        misc = new Misc(this);
        sharedPref = new SharedPref(this);

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        esTime = findViewById(R.id.es_time);
        distance = findViewById(R.id.distance);
        sPrice = findViewById(R.id.s_price);
        finalPr = findViewById(R.id.finalP);
        jobDetails = findViewById(R.id.job_details);
        jobArea = findViewById(R.id.jo_details);
        chat = findViewById(R.id.chat);
        acceptReq = findViewById(R.id.acceptRequest);
        calculatePrice = findViewById(R.id.calculatePrice);
        calculateFinalPrice = findViewById(R.id.calculateFinalPrice);
        startFinalPrice = findViewById(R.id.startFinalTime);
        complaints = findViewById(R.id.complaints);
        jobDetails.setOnClickListener(this);
        chat.setOnClickListener(this);
        acceptReq.setOnClickListener(this);
        calculatePrice.setOnClickListener(this);
        calculateFinalPrice.setOnClickListener(this);
        complaints.setOnClickListener(this);
        startFinalPrice.setOnClickListener(this);

        Intent intent = getIntent();
        jobId = intent.getStringExtra("job_id");
        vendorId = intent.getStringExtra("vendor_id");
        customerId = intent.getStringExtra("customerId");
        name = intent.getStringExtra("vendor_name");
        service = intent.getStringExtra("service_name");
        phone = intent.getStringExtra("phone");
        city =  intent.getStringExtra("city");
        price =  intent.getStringExtra("service_price");
        phoneNumber = intent.getStringExtra("phone");
        lat = intent.getStringExtra("lat");
        lon = intent.getStringExtra("lon");

        //Service Provider/vendor lat and long
        current_latitude = Double.parseDouble(lat);
        current_longitude = Double.parseDouble(lon);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ip_map);
        mapFragment.getMapAsync(VendorInProgressJobDetailsActivity.this);

        customerDetails();
        getCurrentJobDetails();

        //Firebase User Current ID Code
        Query query = databaseReference.child("Users").orderByChild("phone").equalTo(phone);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        currentUserID = issue.child("id").getValue(String.class);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                misc.showToast("Error");
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try{
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

            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(current_latitude, current_longitude);
            mMap.setMinZoomPreference(11);
            myMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Vendor Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            // Here I'm using current lat long as vendor locaiton
            vendorLatLong = new LatLng(current_latitude, current_longitude);
            listPoints.add(0, vendorLatLong);

        }
        catch(Exception e){
            Toast.makeText(this, "Network Problem Please Enable GPS", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if(sharedPref.getUserId() == null) {
            Intent intent = new Intent(this, AllJobsActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Intent intent = new Intent(this, ProviderJobsActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == jobDetails.getId()) {
            jobDetailss();
        }
        if(v.getId() == chat.getId()) {
            usersChat();
        }
        if(v.getId() == acceptReq.getId()) {
            acceptRequest();
        }

        if(v.getId() == calculatePrice.getId()) {
            timeLogic();
        }
        if(v.getId() == startFinalPrice.getId()) {
            setFinalStartTime();
        }
        if(v.getId() == calculateFinalPrice.getId()) {
            finalTimeLogic();
        }
        if(v.getId() == complaints.getId()){
            Intent com = new Intent(this, ComplainVendorActivity.class);
            com.putExtra("customer_id", customerId);
            com.putExtra("vendor_id", vendorId);
            com.putExtra("customer_name", customerName);
            startActivity(com);
            finish();
        }
    }

    private void customerDetails() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait... ");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load("GET",misc.ROOT_PATH+"user_profile/"+customerId)
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
                                JSONObject jsonObject = new JSONObject(result.getResult());
                                customerName = jsonObject.getString("user_name");
                                String customerPhone = jsonObject.getString("user_phone");
                                String customerEmail = jsonObject.getString("user_email");
                                String customerCnic = jsonObject.getString("user_cnic");

                                customerPhoneNumber = customerPhone;

//                                c_name.setText("Name: " + customerName);
//                                c_email.setText("Email: " + customerEmail);
//                                c_phone.setText("Phone: " + customerPhone);
//                                c_cnic.setText("CNIC: " + customerCnic);

                                pd.dismiss();
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void getCurrentJobDetails(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait... ");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load("GET",misc.ROOT_PATH+"job_details/"+jobId)
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
                                JSONObject jsonObject = new JSONObject(result.getResult());

                                initialJobStartTime = jsonObject.getString("job_start_time");
                                customerLat = Double.parseDouble(jsonObject.getString("cus_lat"));
                                customerLon = Double.parseDouble(jsonObject.getString("cus_lon"));

                                //Calling map function to set custoemr location marker
                                mapFunction();

                                pd.dismiss();
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void jobDetailss(){
        jobArea.setVisibility(View.VISIBLE);
        //Setting values
        sPrice.setText("Initial Price: " + price);
        esTime.setText("Estimated Time to Reach: " + duration);
        distance.setText("Distance: " + distancee);

        //get Current Job Status
        Ion.with(this)
                .load("GET",misc.ROOT_PATH+"job_details/"+jobId)
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
                                JSONObject jsonObject = new JSONObject(result.getResult());
                                currentJobStatus = jsonObject.getString("job_status");
                                currentJobFinalStartTime = jsonObject.getString("job_final_start_time");
                                misc.showToast(currentJobFinalStartTime);

                                if(currentJobStatus.equalsIgnoreCase("accepted")){
                                    if(currentJobFinalStartTime.equalsIgnoreCase("")) {
                                        acceptReq.setVisibility(GONE);
                                        calculatePrice.setVisibility(View.VISIBLE);
                                    }
                                    else{
                                        acceptReq.setVisibility(GONE);
                                        calculateFinalPrice.setVisibility(View.VISIBLE);
                                    }
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void usersChat(){
        Intent chat = new Intent(this, ChatMessage.class);
        chat.putExtra("FirebaseSenderID", currentUserID);
        chat.putExtra("job_id", jobId);
        chat.putExtra("vendor_id", vendorId);
        chat.putExtra("customerId", customerId);
        chat.putExtra("phone", phone);
        chat.putExtra("vendor_name", name);
        chat.putExtra("service_name", service);
        chat.putExtra("service_price", price);
        chat.putExtra("city",city);
        chat.putExtra("lat", lat);
        chat.putExtra("lon", lon);
        chat.putExtra("from", "vendor");
        startActivity(chat);
        finish();
    }

    private void acceptRequest(){

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss");
        initialJobStartTime =  mdformat.format(calendar.getTime());

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Accepting Jobs... ");
        pd.setCancelable(false);
        pd.show();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("job_start_time", initialJobStartTime);

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"accept_job/"+jobId)
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
                        else {
                            pd.dismiss();
                            misc.showToast("Job has been accepted & continue...");
                            // All time Logic
                            acceptReq.setVisibility(GONE);
                            calculatePrice.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    public void timeLogic(){
        // Fetching Initial Job Start Time from Database
        Ion.with(this)
                .load("GET",misc.ROOT_PATH+"job_details/"+jobId)
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
                                JSONObject jsonObject = new JSONObject(result.getResult());
                                initialTimeFromDB = jsonObject.getString("job_start_time");

                                //Initial Time
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                                initialJobEndTime =  format.format(calendar.getTime());

                                Time t1 = Time.valueOf(initialTimeFromDB);
                                Time t2 = Time.valueOf(initialJobEndTime);

                                long diff = t2.getTime() - t1.getTime();
                                long diffMinutes = diff / (60 * 1000) % 60;
                                long diffHours = diff / (60 * 60 * 1000) % 24;

                                long totalTime = (diffHours * 60) + diffMinutes;
                                String totalTimes = ""+totalTime;
                                Integer finalInitialTotalTime = Integer.valueOf(totalTimes);

                                String[] data = duration.split(" ", 2);
                                Integer finalDuration = Integer.parseInt(data[0]) + 15;

                                if(finalInitialTotalTime > finalDuration){
                                    misc.showToast("Time Exceed! Starting New Calculations Now! " );
                                    startFinalPrice.setVisibility(View.VISIBLE);
                                    calculatePrice.setVisibility(GONE);
                                }else {
                                    //Sending finalTime and Total Bill into the database without adding final time price,
                                    updateJobFinalValues(initialJobEndTime,price);
                                }

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    public void setFinalStartTime(){

        Calendar calendar1 = Calendar.getInstance();
        SimpleDateFormat format1 = new SimpleDateFormat("HH:mm:ss");
        finalJobStartTime =  format1.format(calendar1.getTime());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("job_finish_time", initialJobEndTime);
        jsonObject.addProperty("job_final_start_time", finalJobStartTime);

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"start_final_time_job/"+jobId)
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
                        else {
                            startFinalPrice.setVisibility(GONE);
                            calculateFinalPrice.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    public void finalTimeLogic(){
        //Final Time
        Ion.with(this)
                .load("GET",misc.ROOT_PATH+"job_details/"+jobId)
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
                                JSONObject jsonObject = new JSONObject(result.getResult());
                                finalTimeFromDB = jsonObject.getString("job_final_start_time");

                                //Initial Time
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                                finalJobEndTime =  format.format(calendar.getTime());

                                Time t3 = Time.valueOf(finalTimeFromDB);
                                Time t4 = Time.valueOf(finalJobEndTime);

                                long diff1 = t4.getTime() - t3.getTime();
                                long diffMinutes1 = diff1 / (60 * 1000) % 60;
                                long diffHours1 = diff1 / (60 * 60 * 1000) % 24;

                                long hoursPrice = diffHours1 * 300;
                                long minutesPrice = diffMinutes1 * 5;
                                long totalInitialPrice = hoursPrice + minutesPrice + Long.parseLong(price);
                                String finalTotalPrice = String.valueOf(totalInitialPrice);
                                updateJobFinalTimeValues(finalJobEndTime,finalTotalPrice);


                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    public void updateJobFinalValues(String jobEndTime, String finalPrice){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("job_finish_time", jobEndTime);
        jsonObject.addProperty("total_bill", finalPrice);

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"job_final_values_updates/"+jobId)
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
                        else {
                            calculatePrice.setVisibility(GONE);
                            //Update Final Value + Price
                            fetchFinalPrice();
                        }
                    }
                });
    }

    public void updateJobFinalTimeValues(String jobEndTime, String finalPrice){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("job_final_finish_time", jobEndTime);
        jsonObject.addProperty("total_bill", finalPrice);

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"job_final_time_values_updates/"+jobId)
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
                        else {
                            calculateFinalPrice.setVisibility(GONE);
                            //Update Final Value + Price
                            fetchFinalPrice();
                        }
                    }
                });
    }

    public void fetchFinalPrice(){
        Ion.with(this)
                .load("GET",misc.ROOT_PATH+"job_details/"+jobId)
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
                                JSONObject jsonObject = new JSONObject(result.getResult());
                                finalPrice = jsonObject.getString("total_bill");
                                finalPr.setText("Final Price: " + finalPrice);
                                finalPr.setVisibility(View.VISIBLE);
                                jobDetails.setVisibility(GONE);
                                complaints.setVisibility(View.VISIBLE);
                                // Making Payment Card
                                paymentBox(finalPrice);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    public void paymentBox(String price){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.cash_calculation_vendor);

        TextView payment = dialog.findViewById(R.id.payment);
        payment.setText(price);
        TextView hand_button = dialog.findViewById(R.id.hand);
        TextView card_button = dialog.findViewById(R.id.card);

        hand_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //By Hand Code
                receivedByHand();
                dialog.dismiss();
            }
        });
        card_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //By Card Code
                receivedByCard();
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.setCancelable(false);
    }

    public void receivedByHand(){
        String cash = "HandReceived";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cash_status", cash);

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"job_cash_status_update/"+jobId)
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
                        else {
                            misc.showToast("Cash has been collected from the customer by hand!");
                        }
                    }
                });
    }

    public void receivedByCard(){
        String cash = "CardReceived";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cash_status", cash);

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"job_cash_status_update/"+jobId)
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
                        else {
                            misc.showToast("Cash has been collected from the customer by credit card!");
                        }
                    }
                });
    }

    private void mapFunction(){

        // Add a marker in Sydney and move the camera
        LatLng sydney1 = new LatLng(customerLat, customerLon);
        mMap.setMinZoomPreference(11);
        myMarker = mMap.addMarker(new MarkerOptions().position(sydney1).title("Customer Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney1));

        customerLatLong = new LatLng(customerLat, customerLon);
        listPoints.add(1, customerLatLong);

        if(listPoints.size() == 2){
            getTimeDist();
        }
        else{
            Toast.makeText(this, "No Directions", Toast.LENGTH_SHORT).show();
        }
    }

    private void getTimeDist(){
        Ion.with(this)
                .load("https://maps.googleapis.com/maps/api/directions/json?origin="+current_latitude+","+current_longitude+"&destination="+customerLat+","+customerLon+"&sensor-false&mode-driving&key=AIzaSyDWif1QwHKIxsv0bcl8lk6mJ9TuzsuMcrk")
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, com.koushikdutta.ion.Response<String> result) {
                        try {
                            JSONObject jsonObject = new JSONObject(result.getResult());

                            JSONArray routes = jsonObject.getJSONArray("routes");
                            JSONObject routes1 = routes.getJSONObject(0);
                            JSONArray legs = routes1.getJSONArray("legs");
                            JSONObject legs1 = legs.getJSONObject(0);
                            distancee = legs1.getJSONObject("distance").getString("text");
                            duration = legs1.getJSONObject("duration").getString("text");

                            //misc.showToast(distancee.toString() + " and " + duration.toString());
                            JSONArray jsonArray = legs1.getJSONArray("steps");
                            showPath(jsonArray);

                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }

    public String[] showPath(JSONArray path){
        int count = path.length();
        String[] polylines = new String[count];
        for(int i = 0; i < count; i++){
            try {
                polylines[i] = getPath(path.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return polylines;
    }

    public String getPath(JSONObject polylines){
        String polyline = "";
        try {
            polyline = polylines.getJSONObject("polyline").getString("points");
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(PolyUtil.decode(polyline));
            polylineOptions.color(Color.RED);
            polylineOptions.width(8);

            if(polylineOptions!=null){
                mMap.addPolyline(polylineOptions);
            }
            else{
                Toast.makeText(this, "Direction not found. Network Problem", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polyline;
    }

}