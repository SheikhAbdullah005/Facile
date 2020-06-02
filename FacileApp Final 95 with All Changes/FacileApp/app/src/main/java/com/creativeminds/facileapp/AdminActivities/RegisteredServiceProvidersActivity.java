package com.creativeminds.facileapp.AdminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.creativeminds.facileapp.Adapters.RegisteredServiceProvidersAdapter;
import com.creativeminds.facileapp.R;

public class RegisteredServiceProvidersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_service_providers);
        setTitle("Electricians");

        RecyclerView view = findViewById(R.id.registered_providers_view);
        view.setLayoutManager(new LinearLayoutManager(this));

        String items[] = {"Java", "Nodejs", "SQL", "PHP", "JavaScript", "Python", "MySQL", "HTML", "CSS", "Bootstrap"};
        view.setAdapter(new RegisteredServiceProvidersAdapter(this, items));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, AllServiceProvidersActivity.class);
        startActivity(intent);
        finish();
    }
}
