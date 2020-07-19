package com.creativeminds.facileapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.AdminActivities.AdminHomeActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.ServiceProviderActivities.ServiceHomeActivity;
import com.creativeminds.facileapp.SharedPref.SharedPref;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button login;
    private TextView register, forgot;
    private EditText user_email, user_password;
    Misc misc;
    SharedPref sharedPref;
    String userOnline;

    private static final String EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");

        sharedPref = new SharedPref(this);
        misc = new Misc(this);

        user_email = findViewById(R.id.login_email);
        user_password = findViewById(R.id.login_password);
        forgot = findViewById(R.id.forgot_password);

      //  loginButton = findViewById(R.id.login_fb);

        forgot.setOnClickListener(this);

        login = findViewById(R.id.login_button);
        register = findViewById(R.id.create_account);
        register.setOnClickListener(this);
        login.setOnClickListener(this);

        checkLogin();
    }

    private void checkLogin(){
        String id = sharedPref.getUserId();
        String userRole = sharedPref.getUserRole();

        if(id != null && userRole != null) {
            if(userRole.equalsIgnoreCase("customer")) {
                Intent intent = new Intent(LoginActivity.this, AllDepartmentsActivity.class);
                startActivity(intent);
                finish();
            }
            if(userRole.equalsIgnoreCase("vendor")) {
                Intent intent = new Intent(LoginActivity.this, ServiceHomeActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == register.getId()){
            Intent intent = new Intent(this, RegisterAsActivity.class);
            startActivity(intent);
            finish();
        }
        if(v.getId() == forgot.getId()) {
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            startActivity(intent);
            finish();
        }
        if(v.getId() == login.getId()){
            String email = user_email.getText().toString().trim();
            String password = user_password.getText().toString().trim();
            if((email.equals("admin@facile.com")) && (password.equals("admin"))) {
                Intent intent = new Intent(this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                if(misc.isConnectedToInternet()){
                    if(email.isEmpty() || password.isEmpty()) {
                        misc.showToast("Email and Password required!");
                        return;
                    }
                    loginUser(email, password);
                }
            }
        }
    }

    private void loginUser(final String email, String password){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Logging in...");
        pd.setCancelable(false);
        pd.show();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_email", email);
        jsonObject.addProperty("user_password", password);

        Ion.with(this)
                .load(misc.ROOT_PATH+"login_user")
                .setJsonObjectBody(jsonObject)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            pd.dismiss();
                            misc.showToast("Please check internet connection");
                            return;
                        }
                        else{
                            try {
                                if(result.getResult().isEmpty()){
                                    pd.dismiss();
                                    misc.showToast("Invalid Email or Password!");
                                    return;
                                }

                                JSONObject jsonObject1 = new JSONObject(result.getResult());

                                String id = jsonObject1.getString("user_id");
                                String role = jsonObject1.getString("user_role");
                                userOnline = jsonObject1.getString("user_online");

                                if(role.equalsIgnoreCase("customer")){
                                    sharedPref.createLoginSession(id, role, userOnline,email);
                                    pd.dismiss();
                                    Intent intent = new Intent(LoginActivity.this, AllDepartmentsActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else if(role.equalsIgnoreCase("vendor") ){
                                    sharedPref.createLoginSession(id, role, userOnline,email);
                                    pd.dismiss();
                                    Intent intent = new Intent(LoginActivity.this, ServiceHomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void printKeyHash() {
        try {
            String packageName = getApplicationContext().getPackageName();
            PackageInfo info = getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            Log.e("KeyHash:", e.toString());
        }
    }

}
