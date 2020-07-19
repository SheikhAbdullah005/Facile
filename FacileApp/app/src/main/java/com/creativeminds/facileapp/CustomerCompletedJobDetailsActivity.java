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

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.AdminActivities.AllJobsActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.SharedPref.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomerCompletedJobDetailsActivity extends AppCompatActivity {

    private TextView text1, text2, text3;
    private String job_id, vendor_id,customer_id;
    Misc misc;
    private RatingBar ratingBar;
    private EditText review;
    private Button submit;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_completed_job_details);
        setTitle("Job Details");

        misc = new Misc(this);
        sharedPref = new SharedPref(this);

        text3 = findViewById(R.id.rate_service);
        ratingBar = findViewById(R.id.rate_job);
        review = findViewById(R.id.feedback);
        submit = findViewById(R.id.rate);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(misc.isConnectedToInternet()){
                    postRating();
                }
                else{
                    misc.showToast("No Internet Connection");
                }

            }
        });

        text1 = findViewById(R.id.job_completed);
        text2 = findViewById(R.id.rate_service);

        Intent intent = getIntent();
        job_id = intent.getStringExtra("job_id");
        vendor_id = intent.getStringExtra("vendor_id");
        customer_id = intent.getStringExtra("customer_id");
        String name = intent.getStringExtra("vendor_name");
        String service = intent.getStringExtra("service_name");
        text1.setText("Job completed by " + name + " " + service);
        text2.setText("Rate " + name + " for his service " + service);

        if(sharedPref.getUserId() == null) {
            text3.setText("Job Rating");
            review.setHint("");
            submit.setVisibility(View.GONE);
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
                                    submit.setVisibility(View.GONE);
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

    private void postRating(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait... ");
        pd.setCancelable(false);
        pd.show();

        if(!validate()){
            misc.showToast("Rating and feedback required");
            return;
        }

        JsonObject rating = new JsonObject();
        rating.addProperty("rating_stars", ratingBar.getRating());
        rating.addProperty("feedback", review.getText().toString());
        rating.addProperty("job_id", job_id);
        rating.addProperty("vendor_id", vendor_id);

        Ion.with(this)
                .load(misc.ROOT_PATH+"post_rating")
                .setJsonObjectBody(rating)
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

    private boolean validate(){
        String ratings = String.valueOf(ratingBar.getRating());
        String comment = review.getText().toString();

        if(ratings.isEmpty()) {
            misc.showToast("Please Rate Service");
            return false;
        }
        if(comment.isEmpty()) {
            misc.showToast("Please enter feedback");
            return false;
        }
        return true;
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
}
