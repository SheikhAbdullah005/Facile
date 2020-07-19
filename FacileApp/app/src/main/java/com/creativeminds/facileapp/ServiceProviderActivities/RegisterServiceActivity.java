package com.creativeminds.facileapp.ServiceProviderActivities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.creativeminds.facileapp.CustomerOTP;
import com.creativeminds.facileapp.Models.Department;
import com.creativeminds.facileapp.RegisterActivity;
import com.creativeminds.facileapp.UploadPortfolio;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.LoginActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Service;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.RegisterAsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterServiceActivity extends AppCompatActivity implements View.OnClickListener {

    private Button register;
    private EditText name, email, password, re_password, address, city, cnic;
    private CircleImageView image;
    private String bitmapTo64, selectedGender = null;
    private static String resultPath = null;
    private final int REQUEST_CODE = 1;
    private String user_role = "vendor";
    Misc misc;
    private ArrayList<String> selectedServices = new ArrayList<>();
    private ArrayList<Department> allServices = new ArrayList<>();
    private ArrayAdapter<String> serviceAdapter;
    private RadioButton male, female;
    private File uploadFile = null;
    private File finalPDFUrl = null;
    private String pdfFileUrl, ID;

    private double lat, lon;
    private GridLayout checkBoxLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_service);
        setTitle("Registration Step 1");

        misc = new Misc(this);

        checkBoxLayout = findViewById(R.id.services);

        name = findViewById(R.id.provider_name);
        email = findViewById(R.id.provider_register_email);
        address = findViewById(R.id.provider_address);
        city = findViewById(R.id.provider_city);
        re_password = findViewById(R.id.provider_confirm_password);
        password = findViewById(R.id.provider_password);
        cnic = findViewById(R.id.reg_cnic);

        male = findViewById(R.id.male);
        female = findViewById(R.id.female);

        male.setOnClickListener(this);
        female.setOnClickListener(this);

        if(male.isChecked()) {
            selectedGender = male.getText().toString();
        }
        if(female.isChecked()) {
            female.getText().toString();
        }

        image = findViewById(R.id.cnic_pic_service);
        image.setOnClickListener(this);

        register = findViewById(R.id.provider_register_button);
        register.setOnClickListener(this);

        if(misc.isConnectedToInternet()){
            fetchServices();
        }
        else{
            misc.showToast("No Internet Connection");
        }
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == male.getId()){
            if(male.isChecked()){
                selectedGender = male.getText().toString();
            }
        }
        if(v.getId() == female.getId()){
            if(female.isChecked()) {
                selectedGender = female.getText().toString();
            }
        }

        if(v.getId() == image.getId()) {
            ActivityCompat.requestPermissions
                    (RegisterServiceActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
        if(v.getId() == register.getId()){
            if(misc.isConnectedToInternet()){
                registerVendor();
            }
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
            Toast.makeText(getApplicationContext(), "Please Select Service Image", Toast.LENGTH_LONG).show();
            return;
        }

        Uri selectedImageUri = data.getData( );
        String picturePath = getPath(getApplicationContext(), selectedImageUri );

        try{

            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmapTo64 = bitmapToBase64(bitmap);
            Log.d("Converted Image", bitmapTo64);
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

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void fetchServices(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading Departments");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load(misc.ROOT_PATH+"get_department")
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            misc.showToast("Please check your connection");
                            pd.dismiss();
                            return;
                        }

                        try {
                            JSONArray serviceArray = new JSONArray(result.getResult());
                            if(serviceArray.length() < 1) {
                                misc.showToast("No Department Found");
                                pd.dismiss();
                            }
                            else{
                                allServices.clear();
                                for (int i = 0; i < serviceArray.length(); i++) {
                                    JSONObject serviceObject = (JSONObject) serviceArray.get(i);
                                    String departmentName = serviceObject.getString("department_name");
                                    String departmentId = serviceObject.getString("department_id");


                                    allServices.add(new Department(departmentId,departmentName));

                                    final CheckBox cb = new CheckBox(getApplicationContext());
                                    cb.setText(departmentName.toUpperCase());
                                    cb.setTextColor(R.color.colorPrimary);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        cb.setButtonTintList(ColorStateList.valueOf(R.color.colorPrimary));
                                    }
                                    cb.setId(Integer.parseInt(departmentId));
                                    cb.setTextSize(10);
                                    cb.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String selectedItem = String.valueOf(cb.getId());
                                            if(selectedServices.contains(selectedItem)){
                                                selectedServices.remove(selectedItem);
                                            }
                                            else{
                                                selectedServices.add(selectedItem);
                                            }
                                        }
                                    });

                                    checkBoxLayout.addView(cb);
                                    pd.dismiss();
                                }
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private boolean validate(){

        String user_name = name.getText().toString().toLowerCase().trim();
        String user_email = email.getText().toString().toLowerCase().trim();
        String user_password = password.getText().toString();
        String user_re_password = re_password.getText().toString();
        String user_address = address.getText().toString();
        String user_city = city.getText().toString();
        String user_cnic = cnic.getText().toString();
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
        if(user_password.length() < 6 ) {
            misc.showToast("Password should be min 6 characters");
            password.setError("Password should be min 6 characters");
            return false;
        }
        if(!user_password.equals(user_re_password)) {
            misc.showToast("Password Mismatch");
            re_password.setError("Password Mismatch");
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
        if(user_cnic.length() < 13) {
            misc.showToast("Invalid CNIC");
            cnic.setError("Invalid CNIC");
            return false;
        }
        if(latLng == null) {
            misc.showToast("Service Location Not Found");
            return false;
        }
        if(selectedServices.size() < 1) {
            misc.showToast("Please select atleast one service");
            return false;
        }

        if(selectedServices.size() > 5) {
            misc.showToast("You can select maximum 5 services");
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
        } catch (IOException e) {
            misc.showToast("Service Location not found");
            e.printStackTrace();
        }
    }


    private void registerVendor(){
        if(validate()) {
            if(resultPath != null) {
                uploadFile = new File(resultPath);
                registerWithImage();
            }
            else{
                misc.showToast("Please upload an image...");
            }
        }
    }

    private void registerWithImage(){

        String items = "";
        for(String item : selectedServices){
            items += item+",";
        }
        Intent intent = new Intent(RegisterServiceActivity.this, VendorOTP.class);
        intent.putExtra("image", resultPath);
        intent.putExtra("name", name.getText().toString().trim());
        intent.putExtra("email", email.getText().toString().trim());
        intent.putExtra("cnic", cnic.getText().toString().trim());
        intent.putExtra("password", password.getText().toString().trim());
        intent.putExtra("gender", selectedGender);
        intent.putExtra("address", address.getText().toString().trim());
        intent.putExtra("city", city.getText().toString().trim());
        intent.putExtra("lat", String.valueOf(lat));
        intent.putExtra("lon", String.valueOf(lon));
        intent.putExtra("department", items);
        intent.putExtra("pdf", pdfFileUrl);
     //   intent.putExtra("ID", ID);
     //   misc.showToast(pdfFileUrl);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, RegisterAsActivity.class);
     //   intent.putExtra("PDFFileURL", pdfFileUrl);
        startActivity(intent);
        finish();
    }

}
