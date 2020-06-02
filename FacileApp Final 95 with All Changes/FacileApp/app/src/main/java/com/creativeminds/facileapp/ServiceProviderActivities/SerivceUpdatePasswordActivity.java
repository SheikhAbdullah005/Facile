package com.creativeminds.facileapp.ServiceProviderActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.SharedPref.SharedPref;

public class SerivceUpdatePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private String user_password;
    private EditText password, confirm_password;
    private Button submit;
    Misc misc;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serivce_update_password);

        misc = new Misc(this);
        sharedPref = new SharedPref(this);

        password = findViewById(R.id.service_up_password);
        confirm_password = findViewById(R.id.service_up_confirm_password);

        submit = findViewById(R.id.update_password);
        submit.setOnClickListener(this);

        Intent intent = getIntent();
        user_password = intent.getStringExtra("password");

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ServiceHomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == submit.getId()){
            if(misc.isConnectedToInternet()){
                updatePassword();
            }
            else{
                misc.showToast("No Internet connection");
            }
        }
    }

    private boolean validate(){
        String pass = password.getText().toString();
        String up_pass = confirm_password.getText().toString();

        if(pass.length() < 6) {
            password.setError("Password is short");
            return false;
        }
        if(!pass.equals(up_pass)){
            confirm_password.setError("Password Mismatch");
            return false;
        }
        if(pass.equals(user_password)){
            misc.showToast("Password is same to old password. Please try different");
            return false;
        }
        if(pass.isEmpty() || up_pass.isEmpty()) {
            misc.showToast("Password Required");
            return false;
        }

        return true;
    }

    private void updatePassword(){
        if(validate()) {
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("user_password", password.getText().toString());

            Ion.with(this)
                    .load("PUT", misc.ROOT_PATH+"update_password/"+sharedPref.getUserId())
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
                                onBackPressed();
                            }
                        }
                    });
        }
    }
}
