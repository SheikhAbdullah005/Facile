package com.creativeminds.facileapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.creativeminds.facileapp.AdminActivities.AllJobsActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.ServiceProviderActivities.ProviderJobsActivity;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class VendorCompletedJobDetailsActivity extends AppCompatActivity {

    private TextView text1, text2, text3;
    private String job_id, vendor_id,customer_id,customerName;
    Misc misc;
    private RatingBar ratingBar;
    private EditText review;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_completed_job_details);
        setTitle("Job Ratings");

        misc = new Misc(this);
        sharedPref = new SharedPref(this);

        text3 = findViewById(R.id.rate_service);
        ratingBar = findViewById(R.id.rate_job);
        review = findViewById(R.id.feedback);
        text1 = findViewById(R.id.job_completed);
        text2 = findViewById(R.id.rate_service);

        Intent intent = getIntent();
        job_id = intent.getStringExtra("job_id");
        vendor_id = intent.getStringExtra("vendor_id");
        customer_id = intent.getStringExtra("customer_id");
        String name = intent.getStringExtra("vendor_name");
        String service = intent.getStringExtra("service_name");

        fetchCustomerName(customer_id);
        text2.setText(" Your Rating");

        if(sharedPref.getUserId() == null) {
            text3.setText("Job Rating");
            review.setHint("");
            review.setEnabled(false);
            ratingBar.setEnabled(false);
        }
        if(misc.isConnectedToInternet()) {
            findRating();
        }
        else{
            misc.showToast("No Internet Connection");
        }
    }

    public void fetchCustomerName(String CID){
        Ion.with(this)
                .load(misc.ROOT_PATH+"user_profile/"+CID)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            misc.showToast("Internet Connection Problem");
                            return;
                        }
                        else{
                            try {
                                JSONObject jsonObject = new JSONObject(result.getResult());
                                customerName = jsonObject.getString("user_name");
                                text1.setText(" Review given by " + customerName);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void findRating(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait... ");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load(misc.ROOT_PATH+"find_rating/"+job_id)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null){
                            misc.showToast("Please check your connection");
                            pd.dismiss();
                            return;
                        }
                        else{
                            if(!result.getResult().isEmpty()){
                                try {
                                    JSONObject jsonObject = new JSONObject(result.getResult());
                                    String ratingStars = jsonObject.getString("rating_stars");
                                    String comment = jsonObject.getString("feedback");

                                    ratingBar.setRating(Float.parseFloat(ratingStars));
                                    review.setText(comment);
                                    ratingBar.setEnabled(false);
                                    review.setEnabled(false);
                                    pd.dismiss();
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            else{
                                pd.dismiss();
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
            Intent intent = new Intent(this, ProviderJobsActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
