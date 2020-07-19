package com.creativeminds.facileapp.AdminActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.R;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServiceProviderDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView name, cnic, phone, email, avgRating;
    private CircularImageView image;
    private Switch aSwitch;
    com.creativeminds.facileapp.Misc.Misc misc;
    private String id, status, user_rating, u_rating;
    private int vendor;
    private Button reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider_details);

        misc = new com.creativeminds.facileapp.Misc.Misc(this);

        name = findViewById(R.id.customer_name);
        email = findViewById(R.id.customer_email);
        phone = findViewById(R.id.customer_phone);
        cnic = findViewById(R.id.customer_cnic);
        avgRating = findViewById(R.id.customer_rating);
        reset = findViewById(R.id.reset_jobs);

        image = findViewById(R.id.customer_image);
        aSwitch = findViewById(R.id.change_status);

        reset.setOnClickListener(this);
        Intent intent = getIntent();
        vendor = intent.getIntExtra("vendors", 0);
        name.setText("Name: " + intent.getStringExtra("name"));
        email.setText("Email: " + intent.getStringExtra("email"));
        phone.setText("Phone: " + intent.getStringExtra("phone"));
        cnic.setText("CNIC: " + intent.getStringExtra("cnic"));
        String imageFile = intent.getStringExtra("image");

        setTitle(intent.getStringExtra("name").toUpperCase());

        if(imageFile.isEmpty()) {
            image.setImageResource(R.drawable.usersicon);
        }
        else{
            Picasso.get()
                    .load(imageFile)
                    .into(image);
        }

        id = intent.getStringExtra("id");
        status = intent.getStringExtra("status");

        fetchAverageRating();

        if(status.equals("enabled")){
            aSwitch.setChecked(true);
        }
        else{
            aSwitch.setChecked(false);
        }


        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String changeStatus = "";
                if(misc.isConnectedToInternet()){
                    if(((Switch) v).isChecked()) {
                        changeStatus = "enabled";
                    }
                    else{
                        changeStatus = "disabled";
                    }

                    updateStatus(changeStatus);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, com.creativeminds.facileapp.AdminActivities.AllServiceProvidersActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateStatus(String newStatus){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", newStatus);

        Ion.with(this)
                .load("PUT", misc.ROOT_PATH+"change_status/"+id)
                .setJsonObjectBody(jsonObject)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e!=null){
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

    private void fetchAverageRating() {
        Ion.with(this)
                .load("GET", misc.ROOT_PATH+"fetch_average_rating/"+id)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e!=null){
                            misc.showToast("Please check your connection");
                            return;
                        }
                        else{
                            JSONArray jsonArray = null;
                            try {
                                jsonArray = new JSONArray(result.getResult());
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                    user_rating = jsonObject.getString("rating");
                                    if(user_rating.equals("null")){
                                        u_rating = null;
                                        user_rating = "Not rated yet";
                                    }
                                    avgRating.setText("Average Rating: " + user_rating);
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
        if(v.getId() == reset.getId()) {
            resetJobs();
        }
    }

    private void resetJobs() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load("PUT",misc.ROOT_PATH+"reset_jobs/"+id)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            misc.showToast("Please check Internet Connection");
                            pd.dismiss();
                            return;
                        }
                        else {
                            pd.dismiss();
                            misc.showToast(result.getResult());
                        }
                    }
                });
    }
}
