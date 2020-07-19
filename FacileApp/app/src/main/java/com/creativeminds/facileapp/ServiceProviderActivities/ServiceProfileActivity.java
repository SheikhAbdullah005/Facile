package com.creativeminds.facileapp.ServiceProviderActivities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.SharedPref.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ServiceProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private Button update;
    private ImageView image;
    private EditText name, email, phone, address, city;
    private double lat, lon;
    private Switch status;
    private String user_online;
    private File uploadFile = null;
    private static String resultPath = null;
    private final int REQUEST_CODE = 1;
    private boolean isOnline = false;
    Misc misc;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_profile);
        setTitle("Update Profile");

        misc = new Misc(this);
        sharedPref = new SharedPref(this);

        image = findViewById(R.id.service_profile_image);
        name = findViewById(R.id.up_full_name);
        email = findViewById(R.id.update_email);
        phone = findViewById(R.id.up_phone);
        address = findViewById(R.id.serivce_up_address);
        city = findViewById(R.id.service_up_city);
        status = findViewById(R.id.update_status);

        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String changeStatus = "";
                if(misc.isConnectedToInternet()){
                    if(((Switch) v).isChecked()) {
                        changeStatus = "yes";
                    }
                    else{
                        changeStatus = "no";
                    }

                    updateStatus(changeStatus);
                }
            }
        });

        email.setEnabled(false);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions
                        (ServiceProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);

            }
        });

        update = findViewById(R.id.update_button);
        update.setOnClickListener(this);

        if(misc.isConnectedToInternet()) {
            fetchUserProfile();
        }
        else{
            misc.showToast("No Internet Connection!");
        }
    }

    private void updateStatus(String status) {
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


    private void fetchUserProfile(){

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading Profile");
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
                                String user_image = jsonObject.getString("user_image");
                                if(user_image.isEmpty()){
                                    image.setImageResource(R.drawable.serviceicon);
                                }
                                else{
                                    Ion.with(getApplicationContext()).load(jsonObject.getString("user_image").replace("\"","")).intoImageView(image);
                                }

                                name.setText(jsonObject.getString("user_name"));
                                address.setText(jsonObject.getString("user_address"));
                                city.setText(jsonObject.getString("user_city"));
                                email.setText(jsonObject.getString("user_email"));
                                phone.setText(jsonObject.getString("user_phone"));
                                user_online = jsonObject.getString("user_online");

                                if(user_online.equalsIgnoreCase("yes")){
                                    status.setChecked(true);
                                }
                                else{
                                    status.setChecked(false);
                                }

                              //  fetchVendorServices();

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void updateVendor(){
        if(validate()) {
            if(resultPath != null) {
                uploadFile = new File(resultPath);
                updateVendorProfileWithImage();
            }
            else{
                updateVendorWithoutImage();
            }
        }
    }

    private void updateVendorWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_name", name.getText().toString().trim());
        jsonObject.addProperty("user_email", email.getText().toString().trim());
        jsonObject.addProperty("user_phone", phone.getText().toString().trim());
        jsonObject.addProperty("user_address", address.getText().toString().trim());
        jsonObject.addProperty("user_city", city.getText().toString().trim());
        jsonObject.addProperty("user_lat", lat);
        jsonObject.addProperty("user_lon", lon);

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"update_vendor/"+sharedPref.getUserId())
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
                            misc.showToast(result.getResult());
                            pd.dismiss();
                            onBackPressed();
                        }
                    }
                });

    }

    private void updateVendorProfileWithImage(){
        if(validate()){
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();

            Ion.with(this)
                    .load("PUT",misc.ROOT_PATH+"update_vendor_profile/"+sharedPref.getUserId())
                    .setMultipartFile("user_image", uploadFile)
                    .setMultipartParameter("user_name", name.getText().toString().trim())
                    .setMultipartParameter("user_email", email.getText().toString().trim())
                    .setMultipartParameter("user_phone", phone.getText().toString().trim())
                    .setMultipartParameter("user_address", address.getText().toString().trim())
                    .setMultipartParameter("user_city", city.getText().toString().trim())
                    .setMultipartParameter("user_lat", String.valueOf(lat))
                    .setMultipartParameter("user_lon", String.valueOf(lon))
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            if(e != null) {
                                misc.showToast("Please check your connection");
                                pd.dismiss();
                                update.setEnabled(true);
                                return;
                            }
                            else{
                                misc.showToast(result.getResult());
                                pd.dismiss();
                                update.setEnabled(true);
                                onBackPressed();
                            }
                        }
                    });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
            else{
                Toast.makeText(this, "You don't have permissions", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null){
            Toast.makeText(getApplicationContext(), "Please Select Profile Image", Toast.LENGTH_LONG).show();
            return;
        }

        Uri selectedImageUri = data.getData( );
        String picturePath = getPath(getApplicationContext(), selectedImageUri );

        try{

            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            image.setImageBitmap(bitmap);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getPath(Context context, Uri uri ) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                resultPath = cursor.getString( column_index );

            }
            cursor.close( );
        }
        if(resultPath == null) {
            resultPath = null;
        }
        return resultPath;
    }

    private boolean validate(){

        String user_name = name.getText().toString().trim();
        String user_email = email.getText().toString().trim();
        String user_phone = phone.getText().toString().trim();
        String user_address = address.getText().toString();
        String user_city = city.getText().toString();
        LatLng latLng = misc.getCoordinates(user_address);

        getCoordinates(user_address);

        String regex = "[A-Za-z A-Za-z]+";
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if(user_name.length() < 3 && !user_name.matches(regex)){
            misc.showToast("Invalid Name");
            name.setError("Invalid Name");
            return false;
        }
        if(!user_email.matches(emailPattern) && user_email.isEmpty()) {
            misc.showToast("Invalid Email");
            email.setError("Invalid Email");
            return false;
        }
        if(user_phone.length() < 11) {
            misc.showToast("Invalid Phone Number");
            phone.setError("Invalid Phone Number");
            return false;
        }
        if(user_city.length() < 3) {
            misc.showToast("Invalid City");
            city.setError("Invalid City");
            return false;
        }
        if(user_address.length() < 5) {
            misc.showToast("Please Enter Full Address");
            address.setError("Please Enter Full Address");
            return false;
        }
        if(latLng == null) {
            misc.showToast("Service Location Not Found");
            return false;
        }

        return true;
    }

    public void getCoordinates(String location) {
        Geocoder gc = new Geocoder(this);
        LatLng latLng = null;
        try {
            List<Address> address = gc.getFromLocationName(location, 1);
            Address add = address.get(0);
            lat = add.getLatitude();
            lon = add.getLongitude();
            misc.showToast("Lat : " + lat + " Lon : " + lon);
        } catch (IOException e) {
            misc.showToast("Service Location not found");
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ServiceHomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == update.getId()){
            if(misc.isConnectedToInternet()){
                update.setEnabled(false);
                updateVendor();
            }
            else{
                misc.showToast("No Internet Connection");
            }
        }
    }
}
