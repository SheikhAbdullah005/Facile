package com.creativeminds.facileapp.AdminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.creativeminds.facileapp.LoginActivity;
import com.creativeminds.facileapp.R;

public class AdminHomeActivity extends AppCompatActivity implements View.OnClickListener {

    private CardView users, service_providers, complaints, add_new_service, job_history, view_service;
    private TextView logOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        getSupportActionBar().hide();

        users = findViewById(R.id.all_users);
        view_service = findViewById(R.id.view_services);
        service_providers = findViewById(R.id.all_service_providers);
        complaints = findViewById(R.id.all_complaints);
        add_new_service = findViewById(R.id.add_servive);
        job_history = findViewById(R.id.all_jobs);
        logOut = findViewById(R.id.logout);


        logOut.setOnClickListener(this);
        users.setOnClickListener(this);
        service_providers.setOnClickListener(this);
        complaints.setOnClickListener(this);
        add_new_service.setOnClickListener(this);
        job_history.setOnClickListener(this);
        view_service.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == logOut.getId()){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        if(v.getId() == users.getId()){
            Intent intent = new Intent(this, com.creativeminds.facileapp.AdminActivities.AllCustomersActivity.class);
            startActivity(intent);
            finish();
        }
        if(v.getId() == job_history.getId()){
            Intent intent = new Intent(this, com.creativeminds.facileapp.AdminActivities.AllJobsActivity.class);
            startActivity(intent);
            finish();
        }
        if(v.getId() == service_providers.getId()){
            Intent intent = new Intent(this, com.creativeminds.facileapp.AdminActivities.AllServiceProvidersActivity.class);
            startActivity(intent);
            finish();
        }
        if(v.getId() == complaints.getId()){
            Intent intent = new Intent(this, com.creativeminds.facileapp.AdminActivities.AllComplaintsActivity.class);
            startActivity(intent);
            finish();
        }
        if(v.getId() == add_new_service.getId()){
            Intent intent = new Intent(this, com.creativeminds.facileapp.AdminActivities.AddServiceActivity.class);
            startActivity(intent);
            finish();
        }
        if(v.getId() == view_service.getId()){
            Intent intent = new Intent(this, RegisteredServicesActivity.class);
            intent.putExtra("all", 1);
            startActivity(intent);
            finish();
        }
    }
}