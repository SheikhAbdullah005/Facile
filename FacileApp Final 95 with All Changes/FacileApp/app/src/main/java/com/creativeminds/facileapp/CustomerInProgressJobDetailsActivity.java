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
import android.widget.TextView;
import android.widget.Toast;

import com.creativeminds.facileapp.Adapters.CustomerDepartmentAdapter;
import com.creativeminds.facileapp.Models.Department;
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
import com.creativeminds.facileapp.AdminActivities.AllJobsActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.SharedPref.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.view.View.GONE;

public class CustomerInProgressJobDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private Marker myMarker;
    private double current_latitude, current_longitude;
    private TextView name, service, phone, city, c_name, c_phone, c_email, c_cnic;
    private Location currentLocation;
    private String phoneNumber, jobId,vendorId;
    private Button complete,chat,complain, msg, call, c_msg, c_call, cancel;
    private EditText meesage;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_REQUEST_CODE = 101;
    SharedPref sharedPref;
    Misc misc;
    private boolean show = false;
    private String customerId, customerPhoneNumber;
    private CardView customerArea;

    FirebaseAuth mAuth;
    String abc,phoneNum,customerPhone;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    String currentUserID,totalPayment,serviceID,vendorName,serviceName,cityName,lat,lon;
    LatLng vendorLatLong;
    LatLng customerLatLong;
    String currentJobStatus;
    Double customerLat, customerLon;
    ArrayList<LatLng> listPoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_in_progress_job_details);
        setTitle("Job In Progress");

        misc = new Misc(this);
        sharedPref = new SharedPref(this);
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        name = findViewById(R.id.c_name);
        service = findViewById(R.id.c_service);
        phone = findViewById(R.id.c_phone);
        city = findViewById(R.id.c_city);
        call = findViewById(R.id.make_call);
        msg = findViewById(R.id.message);
        customerArea = findViewById(R.id.customer_details);

        c_name = findViewById(R.id.customer_name);
        c_email = findViewById(R.id.customer_email);
        c_phone = findViewById(R.id.customer_phone);
        c_cnic = findViewById(R.id.customer_cnic);
        c_msg = findViewById(R.id.customer_message);
        c_call = findViewById(R.id.customer_make_call);

        msg.setOnClickListener(this);
        complete = findViewById(R.id.complete_job);
        chat = findViewById(R.id.chat);
        complain = findViewById(R.id.complains);
        cancel = findViewById(R.id.cancel);
        complete.setOnClickListener(this);
        call.setOnClickListener(this);
        c_call.setOnClickListener(this);
        c_msg.setOnClickListener(this);
        chat.setOnClickListener(this);
        complain.setOnClickListener(this);
        cancel.setOnClickListener(this);

        Intent intent = getIntent();
        jobId = intent.getStringExtra("job_id");
        vendorId = intent.getStringExtra("vendor_id");
        customerId = intent.getStringExtra("customerId");
        phoneNum = intent.getStringExtra("phone");
        vendorName = intent.getStringExtra("vendor_name");
        serviceName = intent.getStringExtra("service_name");
        cityName = intent.getStringExtra("city");
        lat = intent.getStringExtra("lat");
        lon = intent.getStringExtra("lon");

        String id = sharedPref.getUserId();
        if(id == null) {
            customerDetails();
            complete.setVisibility(GONE);
        }
        else {
            customerArea.setVisibility(GONE);
        }
        name.setText("Name: " + vendorName);
        service.setText("Service: " + serviceName);
        phone.setText("Phone: " + phoneNum);
        city.setText("City: " + cityName);
        phoneNumber = intent.getStringExtra("phone");
        abc = mAuth.getCurrentUser().getUid();
        //Firebase Work
        getCurrentUserID();
        getCustomerLocation();

        //Service Provider/vendor lat and long
        current_latitude = Double.parseDouble(lat);
        current_longitude = Double.parseDouble(lon);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.ip_map);
        mapFragment.getMapAsync(CustomerInProgressJobDetailsActivity.this);

        // Checking the Work Price
        checkPaymentCompletionStatus();
        //For Checking if request is accepted then gone the visibility of cancel button
        checkAcceptedRequest();
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(current_latitude, current_longitude);
        mMap.setMinZoomPreference(11);
        myMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Vendor Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        // Here I'm using current lat long as vendor locaiton
        vendorLatLong = new LatLng(current_latitude, current_longitude);
        listPoints.add(0, vendorLatLong);
    }

    public void getCurrentUserID(){
        Ion.with(this)
                .load("GET",misc.ROOT_PATH+"user_profile/"+customerId)
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
                                customerPhone = jsonObject.getString("user_phone");
                                Query query = databaseReference.child("Users").orderByChild("phone").equalTo(customerPhone);
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
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if(sharedPref.getUserId() == null) {
            Intent intent = new Intent(this, AllJobsActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Intent intent = new Intent(this, JobHistoryActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void checkAcceptedRequest(){
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

                                if(currentJobStatus.equalsIgnoreCase("accepted")){
                                    cancel.setVisibility(GONE);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == cancel.getId()){
            cancelRequest();
            changeCartStatus();
            changeDailyJobStatus();
        }
        if(v.getId() == complete.getId()) {
            completeJob();
            changeCartStatus();
            changeDailyJobStatus();
        }
        if(v.getId() == chat.getId()){
            usersChat();
        }
        if(v.getId() == complain.getId()){
            Intent com = new Intent(this, ComplainActivity.class);
            com.putExtra("customer_id", customerId);
            com.putExtra("vendor_id", vendorId);
            com.putExtra("vendor_name", vendorName);
            startActivity(com);
            finish();
        }
        if(v.getId() == call.getId()){
            makeCall(phoneNumber);
        }
        if(v.getId() == call.getId()){
            makeCall(phoneNumber);
        }
        if(v.getId() == msg.getId()){
            sendSMS(phoneNumber);
        }
        if(v.getId() == c_call.getId()){
            makeCall(customerPhoneNumber);
        }
        if(v.getId() == c_msg.getId()){
            sendSMS(customerPhoneNumber);
        }
    }

    public void checkPaymentCompletionStatus(){
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
                                String cashStatus = jsonObject.getString("cash_status");
                                if(cashStatus.equalsIgnoreCase("HandReceived") || cashStatus.equalsIgnoreCase("CardReceived")){
                                    complete.setVisibility(View.VISIBLE);
                                }
                                else{
                                    checkingWorkPrice();
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    public void checkingWorkPrice(){
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
                                totalPayment = jsonObject.getString("total_bill");
                                if(!totalPayment.equalsIgnoreCase("")){
                                    paymentBox(totalPayment);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    public void cancelRequest(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Cancelling Jobs... ");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"cancel_job/"+jobId)
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
                            misc.showToast("Job has been Cancelled");
                            Intent cancel = new Intent(getApplication(), AllDepartmentsActivity.class);
                            startActivity(cancel);
                            finish();
                            pd.dismiss();
                        }
                    }
                });
    }

    public void usersChat(){
        Intent chat = new Intent(this, ChatMessage.class);
        chat.putExtra("FirebaseSenderID", currentUserID);
        chat.putExtra("job_id", jobId);
        chat.putExtra("vendor_id", vendorId);
        chat.putExtra("customerId", customerId);
        chat.putExtra("phone", phoneNum);
        chat.putExtra("vendor_name", vendorName);
        chat.putExtra("service_name", serviceName);
        chat.putExtra("service_price", "");
        chat.putExtra("city", cityName);
        chat.putExtra("lat", lat);
        chat.putExtra("lon", lon);
        chat.putExtra("from", "customer");
        startActivity(chat);
        finish();
    }

    private void makeCall(String phone){
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    private void sendSMS(String phone){
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.putExtra("address", phone);
        sendIntent.putExtra("sms_body", "Hi there!");
        sendIntent.setType("vnd.android-dir/mms-sms");
        startActivity(sendIntent);
    }

    private void getCustomerLocation(){
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
                                serviceID = jsonObject.getString("fk_service_id");
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
//                            distancee = legs1.getJSONObject("distance").getString("text");
//                            duration = legs1.getJSONObject("duration").getString("text");

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
                                String customerName = jsonObject.getString("user_name");
                                customerPhone = jsonObject.getString("user_phone");
                                String customerEmail = jsonObject.getString("user_email");
                                String customerCnic = jsonObject.getString("user_cnic");

                                customerPhoneNumber = customerPhone;

                                c_name.setText("Name: " + customerName);
                                c_email.setText("Email: " + customerEmail);
                                c_phone.setText("Phone: " + customerPhone);
                                c_cnic.setText("CNIC: " + customerCnic);

                                pd.dismiss();
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    public void paymentBox(String price){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.cash_calculation);

        TextView payment = dialog.findViewById(R.id.payment);
        payment.setText(price);
        TextView hand_button = dialog.findViewById(R.id.hand);
        TextView card_button = dialog.findViewById(R.id.card);

        hand_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //By Hand Code
                complete.setVisibility(View.VISIBLE);
                misc.showToast("Cash has been given to vendor by hand!");
                dialog.dismiss();
            }
        });
        card_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Write Code of Android Credit Card Payment Integration
                creditCardPayment();
                complete.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.setCancelable(false);
    }

    private void completeJob(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Completing Jobs... ");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"complete_job/"+jobId)
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
                            misc.showToast("Job has been Completed");
                            complete.setVisibility(GONE);
                            complain.setVisibility(View.VISIBLE);
                            pd.dismiss();
                        }
                    }
                });
    }

    public void changeCartStatus(){
        JsonObject cart1 = new JsonObject();
        cart1.addProperty("service_cart", "uncarted");
        Ion.with(this)
                .load("PUT",misc.ROOT_PATH + "update_cart_status/"+serviceID)
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

    public void changeDailyJobStatus(){
        Ion.with(this)
                .load("PUT",misc.ROOT_PATH +"reset_jobs/"+vendorId)
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
                        }
                    }
                });
    }

    public void creditCardPayment(){
        Intent card = new Intent(this, PayActivity.class);
        card.putExtra("service_price", totalPayment);
        card.putExtra("job_id", jobId);
        card.putExtra("vendor_id", vendorId);
        card.putExtra("customerId", customerId);
        card.putExtra("phone", phoneNum);
        card.putExtra("vendor_name", vendorName);
        card.putExtra("service_name", serviceName);
        card.putExtra("city", cityName);
        card.putExtra("lat", lat);
        card.putExtra("lon", lon);
        startActivity(card);
        finish();
    }
}