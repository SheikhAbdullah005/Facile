package com.creativeminds.facileapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CardView electrician;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Customer Dashboard");

        electrician = findViewById(R.id.electricians);
        electrician.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == electrician.getId()){
            startActivity(new Intent(MainActivity.this, MapsActivity.class));
        }
    }
}
