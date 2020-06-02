package com.creativeminds.facileapp.AdminActivities;

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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

public class AddServiceActivity extends AppCompatActivity {

    private ImageView preview;
    private EditText name,dec, price;
    private Button add;
    private String bitmapTo64 = null;
    private static String resultPath = null;
    private final int REQUEST_CODE = 1;
    private File uploadFile = null;
    Misc misc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);
        setTitle("Add Service");

        misc = new Misc(this);


        name = findViewById(R.id.add_service_name);
        dec = findViewById(R.id.add_service_dec);
        price = findViewById(R.id.add_service_price);
        add = findViewById(R.id.add_new_service);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(misc.isConnectedToInternet()){
                    uploadService();
                }
            }
        });
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
            preview.setImageBitmap(bitmap);
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
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    private boolean validate(){
        String regex = "[A-Za-z A-Za-z]+";
        String serviceName = name.getText().toString().toLowerCase().trim();

        if(!serviceName.matches(regex) || serviceName.length() < 3) {
            misc.showToast("Service name is not valid");
            name.setError("service name is not valid");
            return false;
        }

        return true;
    }

    private void uploadService(){
        if(validate()){
            JsonObject service = new JsonObject();
            service.addProperty("service_name", name.getText().toString().toLowerCase().trim());
            service.addProperty("service_des", dec.getText().toString().toLowerCase().trim());
            service.addProperty("service_price", price.getText().toString().toLowerCase().trim());
            if(resultPath != null) {
                uploadFile = new File(resultPath);
            }
            else{
                misc.showToast("Please upload service image");
                return;
            }

            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
            Ion.with(this)
                    .load(misc.ROOT_PATH+"add_service")
                    .setMultipartParameter("service_name", name.getText().toString().trim())
                    .setMultipartParameter("service_des", dec.getText().toString().trim())
                    .setMultipartParameter("service_price", price.getText().toString().trim())
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            if(e != null) {
                                pd.dismiss();
                                misc.showToast("Please check your connection");
                                return;
                            }
                            misc.showToast(result.getResult());
                            pd.dismiss();
                            onBackPressed();
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, AdminHomeActivity.class);
        startActivity(intent);
        finish();
    }
}
