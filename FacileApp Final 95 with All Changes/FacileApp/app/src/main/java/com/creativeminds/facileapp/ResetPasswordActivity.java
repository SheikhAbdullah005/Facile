package com.creativeminds.facileapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.Misc.Misc;

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText pass1, pass2, email;
    private Button email_check, reset;
    Misc misc;
    private String user_email;
    private TextInputLayout pp1, pp2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        setTitle("Forgot Password");

        misc = new Misc(this);

        pp1 = findViewById(R.id.p1);
        pp2 = findViewById(R.id.p2);

        pass1 = findViewById(R.id.reset_password);
        pass2 = findViewById(R.id.reset_up_password);
        email = findViewById(R.id.reset_email);

        email_check = findViewById(R.id.check_email);
        reset = findViewById(R.id.reset_button);

        email_check.setOnClickListener(this);
        reset.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == email_check.getId()){
            verifyEmail();
        }
        if(v.getId() == reset.getId()){
            resetPassword();
        }
    }

    private void resetPassword() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        String password = pass1.getText().toString();
        String confirm = pass2.getText().toString();

        if(password.isEmpty() || confirm.isEmpty()){
            misc.showToast("Password Required");
            return;
        }
        if(!password.equals(confirm)){
            misc.showToast("Password Mismatch");
            return;
        }
        if(password.length() < 6) {
            misc.showToast("Password too short");
            return;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", user_email);
        jsonObject.addProperty("password", password);

        Ion.with(this)
                .load("PUT", misc.ROOT_PATH+"reset_password")
                .setJsonObjectBody(jsonObject)
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
                            misc.showToast(result.getResult());
                            pd.dismiss();
                            onBackPressed();
                        }
                    }
                });


    }

    private void verifyEmail() {
        final String mail = email.getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(!mail.matches(emailPattern)){
            email.setError("Invalid Email");
            misc.showToast("Invalid Email");
            return;
        }

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_email", mail);

        Ion.with(this)
                .load(misc.ROOT_PATH+"verify_email")
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
                            String res = result.getResult();
                            if(res.isEmpty()) {
                                misc.showToast("Nothing found");
                                pd.dismiss();
                            }
                            else{
                                user_email = mail;
                                pp1.setVisibility(View.VISIBLE);
                                pp2.setVisibility(View.VISIBLE);
                                reset.setVisibility(View.VISIBLE);
                                email.setVisibility(View.GONE);
                                email_check.setVisibility(View.GONE);
                                pd.dismiss();
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
