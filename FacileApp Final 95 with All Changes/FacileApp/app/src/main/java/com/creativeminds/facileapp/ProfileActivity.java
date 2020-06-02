package com.creativeminds.facileapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.SharedPref.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private EditText name, email, phone, password, confirm;
    private ImageView image;
    private Button update;
    private static String resultPath = null;
    private final int REQUEST_CODE = 1;
    Misc misc;
    SharedPref sharedPref;
    private File uploadFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("Profile");

        name = findViewById(R.id.up_full_name);
        email = findViewById(R.id.update_email);
        phone = findViewById(R.id.up_phone);
        password = findViewById(R.id.up_password);
        confirm = findViewById(R.id.up_confirm_password);

        email.setEnabled(false);

        update = findViewById(R.id.update_button);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!misc.isConnectedToInternet()){
                    misc.showToast("No Internet Connection");
                }
                else{
                    updateProfile();
                }
            }
        });

        image = findViewById(R.id.profile_image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions
                        (ProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        });

        misc = new Misc(this);
        sharedPref = new SharedPref(this);

        if(misc.isConnectedToInternet()) {
            fetchUserProfile();
        }
        else{
            misc.showToast("No Internet Connection");
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, AllDepartmentsActivity.class);
        startActivity(intent);
        finish();
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
        String user_password = password.getText().toString();
        String user_re_password = confirm.getText().toString();

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
        if(user_password.length() < 6 ) {
            misc.showToast("Password should be min 6 characters");
            password.setError("Password should be min 6 characters");
            return false;
        }
        if(!user_password.equalsIgnoreCase(user_re_password)) {
            misc.showToast("Password Mismatch");
            confirm.setError("Password Mismatch");
            return false;
        }

        return true;
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
                                if(user_image.isEmpty()) {
                                    image.setImageResource(R.drawable.usersicon);
                                }
                                else{
                                    Ion.with(getApplicationContext()).load(jsonObject.getString("user_image").replace("\"","")).intoImageView(image);
                                }

                                name.setText(jsonObject.getString("user_name"));
                                email.setText(jsonObject.getString("user_email"));
                                phone.setText(jsonObject.getString("user_phone"));
                                password.setText(jsonObject.getString("user_password"));
                                confirm.setText(jsonObject.getString("user_password"));

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void updateProfile(){
        if(validate()){

            if(resultPath != null) {
                uploadFile = new File(resultPath);
                updateWithImage();
            }
            else{
                updateWithoutImage();
            }
        }
    }

    private void updateWithImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Updating Profile...");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH + "update_customer_profile/"+sharedPref.getUserId())
                .setMultipartFile("user_image", uploadFile)
                .setMultipartParameter("user_name", name.getText().toString().trim())
                .setMultipartParameter("user_phone", phone.getText().toString().trim())
                .setMultipartParameter("user_password", password.getText().toString())
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if (e != null) {
                            pd.dismiss();
                            misc.showToast("Please check your connection");
                            pd.dismiss();
                            return;
                        }
                        pd.dismiss();
                        misc.showToast(result.getResult());
                        Intent intent = new Intent(ProfileActivity.this, AllServiceActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void updateWithoutImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Updating Profile...");
        pd.setCancelable(false);
        pd.show();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_name", name.getText().toString().trim());
        jsonObject.addProperty("user_phone", phone.getText().toString());
        jsonObject.addProperty("user_password", password.getText().toString());


        Ion.with(this)
                .load("PUT", misc.ROOT_PATH+"update_customer/"+sharedPref.getUserId())
                .setJsonObjectBody(jsonObject)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if (e != null) {
                            pd.dismiss();
                            misc.showToast("Please check your connection");
                            pd.dismiss();
                            return;
                        }
                        pd.dismiss();
                        misc.showToast(result.getResult());
                        Intent intent = new Intent(ProfileActivity.this, AllServiceActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

    }

    private void base64ToBitmap(String b64) {
        misc.showToast(b64);
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
        image.setImageBitmap(bitmap);
    }
}
