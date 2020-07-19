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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.Misc.Misc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    private EditText name, email, password, re_password, cnic;
    private RadioButton male, female;
    private Button register;
    private CircleImageView image;
    private String bitmapTo64, selectedGender = null;
    private static String resultPath = null;
    private final int REQUEST_CODE = 1;
    private String user_role = "customer", receivedCode;
    private File uploadFile = null;
    Misc misc;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Register As Customer");


        misc = new Misc(this);

        image = findViewById(R.id.profile_pic_customer);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions
                        (RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        });

        name = findViewById(R.id.full_name);
        email = findViewById(R.id.register_email);
        cnic = findViewById(R.id.cus_reg_cnic);
        password = findViewById(R.id.reg_password);
        re_password = findViewById(R.id.confirm_password);

        male = findViewById(R.id.cus_male);
        female = findViewById(R.id.cus_female);

        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(male.isChecked()){
                    selectedGender = male.getText().toString();
                }
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(female.isChecked()) {
                    selectedGender = female.getText().toString();
                }
            }
        });

        if(male.isChecked()){
            selectedGender = male.getText().toString();
        }
        if(female.isChecked()) {
            selectedGender = female.getText().toString();
        }

        register = findViewById(R.id.register_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(misc.isConnectedToInternet()){
                    registerCustomer();
                }
            }
        });
    }

    private boolean validate(){

        String user_name = name.getText().toString().trim();
        String user_email = email.getText().toString().trim();
        String user_password = password.getText().toString();
        String user_cnic = cnic.getText().toString();
        String user_re_password = re_password.getText().toString();

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
        if(!user_password.equalsIgnoreCase(user_re_password)) {
            misc.showToast("Password Mismatch");
            re_password.setError("Password Mismatch");
            return false;
        }

        if(user_cnic.length() < 13) {
            misc.showToast("Invalid CNIC");
            cnic.setError("Invalid CNIC");
            return false;
        }

        return true;
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

    private void registerCustomer(){

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

        Intent intent = new Intent(RegisterActivity.this, CustomerOTP.class);
        intent.putExtra("image", resultPath);
        intent.putExtra("name", name.getText().toString().trim());
        intent.putExtra("email", email.getText().toString().trim());
        intent.putExtra("cnic", cnic.getText().toString().trim());
        intent.putExtra("password", password.getText().toString().trim());
        intent.putExtra("gender", selectedGender);
        startActivity(intent);
        finish();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, RegisterAsActivity.class);
        startActivity(intent);
        finish();
    }
}
