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
import com.creativeminds.facileapp.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

public class UpdateServiceActivity extends AppCompatActivity {

    private ImageView preview;
    private EditText name;
    private Button add;
    private String bitmapTo64 = null;
    private static String resultPath = null;
    private final int REQUEST_CODE = 1;
    private File uploadFile = null;
    com.creativeminds.facileapp.Misc.Misc misc;
    private String serviceId, serviceName, serviceImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_service);

        misc = new com.creativeminds.facileapp.Misc.Misc(this);

        name = findViewById(R.id.add_service_name);
        add = findViewById(R.id.update_service);

        Intent intent = getIntent();
        serviceId = intent.getStringExtra("service_id");
        serviceName = intent.getStringExtra("service_name");

        name.setText(serviceName);

        if(misc.isConnectedToInternet()) {
            Ion.with(this)
                    .load(serviceImage)
                    .intoImageView(preview);
        }
        else {
            misc.showToast("Internet Connection Problem! Image failed to load");
        }


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(misc.isConnectedToInternet()){
                    updateService();
                }
            }
        });

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

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, RegisteredServicesActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateService(){
        if(validate()){

            if(resultPath != null) {
                update(true);

            }
            else{
                update(false);
            }
        }
    }

    private void update(boolean image) {
        if(image) {
            uploadFile = new File(resultPath);

            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
            Ion.with(this)
                    .load("PUT",misc.ROOT_PATH+"update_service/"+serviceId)
                    .setMultipartParameter("service_name", name.getText().toString().trim())
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
        else {
            JsonObject service = new JsonObject();
            service.addProperty("service_name", name.getText().toString().toLowerCase().trim());

            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
            Ion.with(this)
                    .load("PUT",misc.ROOT_PATH+"update_service/"+serviceId)
                    .setJsonObjectBody(service)
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
}
