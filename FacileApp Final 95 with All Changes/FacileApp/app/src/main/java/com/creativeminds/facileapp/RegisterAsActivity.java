package com.creativeminds.facileapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;

import com.creativeminds.facileapp.ServiceProviderActivities.RegisterServiceActivity;
import com.creativeminds.facileapp.SharedPref.SharedPref;

public class RegisterAsActivity extends AppCompatActivity implements View.OnClickListener {

    private CardView customer, service;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_as);
        setTitle("Register As");

        customer = findViewById(R.id.register_customer);
        service = findViewById(R.id.register_service);

        sharedPref = new SharedPref(this);
        customer.setOnClickListener(this);
        service.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        String id = sharedPref.getUserId();

        if(v.getId() == customer.getId()){
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
        if(v.getId() == service.getId()){
            Intent intent = new Intent(this, RegisterServiceActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
