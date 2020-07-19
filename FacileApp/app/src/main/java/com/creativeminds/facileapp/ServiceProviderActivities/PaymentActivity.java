package com.creativeminds.facileapp.ServiceProviderActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.creativeminds.facileapp.R;

public class PaymentActivity extends AppCompatActivity {
    String up;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        setTitle("Make Payment");

    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, com.creativeminds.facileapp.ServiceProviderActivities.ServiceHomeActivity.class);
        startActivity(intent);
        finish();

    }
}
