package com.creativeminds.facileapp;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.creativeminds.facileapp.Adapters.CustomerDepartmentAdapter;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Department;
import com.creativeminds.facileapp.ServiceProviderActivities.ServiceHomeActivity;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.zip.Inflater;

public class wallet extends AppCompatActivity {
    public Misc misc;
    String previous_amount,getUserAmount;
    String id;
    String newAmountString;
    int newAmount;
    String cardNumber,getAmount,user;
    Button addMoney,deleteCard;
    public TextView card ,amount;
    TextInputEditText added_money_field;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        setTitle("My Wallet");
        misc = new Misc(this);
        addMoney = findViewById(R.id.add_money);
        deleteCard = findViewById(R.id.deleteCard);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        getWalletDetails(id);
        addMoney.setEnabled(false);
        deleteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callDeleteCard();
            }
        });
    }
    public void callDeleteCard(){
        Ion.with(this)
                .load("DELETE",misc.ROOT_PATH+"delete_card/"+id)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if (e != null) {
                            misc.showToast("Please check your connection");
                            return;
                        } else {
                            misc.showToast("Wallet deleted.");
                            Intent wallet_1 = new Intent(getApplicationContext(), addNewCreditCard.class);
                            wallet_1.putExtra("customerId", id);
                            startActivity(wallet_1);
                            finish();
                            //finish();
                        }
                    }
                });
    }
    private void getWalletDetails(final String id){
        Ion.with(this)
                .load(misc.ROOT_PATH+"get_wallet/"+id)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            misc.showToast("Please check your connection");
                            return;
                        }
                        else{
                            try {
                                JSONArray jsonArray = new JSONArray(result.getResult());
                                if(jsonArray.length()>0) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                        cardNumber = jsonObject.getString("card_number");
                                        getAmount = jsonObject.getString("amount");
                                        card = findViewById(R.id.card_number);
                                        amount = findViewById(R.id.amount);
                                        card.setText(cardNumber);
                                        amount.setText("Rs: " + getAmount + "/-");
                                        Log.e("Number: ", cardNumber);
                                        addMoney = findViewById(R.id.add_money);
                                        addMoney.setEnabled(true);
                                        addMoney.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                addMoney(id);
                                            }
                                        });
                                    }
                                }else{
                                    card = findViewById(R.id.card_number);
                                    amount = findViewById(R.id.amount);
                                    card.setText("Add credit card");
                                    amount.setText("Rs: 0/-");
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }

                    }
                });
    }
    private void addMoney(final String id) {
        added_money_field = findViewById(R.id.added_money);
        getUserAmount = String.valueOf(added_money_field.getText());
        if( added_money_field.getText().toString().trim().length()==0|| toInteger(getUserAmount)<1000){
            Toast.makeText(this,"You can add minimun Rs: 1000/-",Toast.LENGTH_LONG).show();
            //misc.showToast("You can add minimun Rs: 1000/-");
        }else {
            Ion.with(this)
                    .load(misc.ROOT_PATH+"get_wallet/"+id)
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            if(e != null) {
                                misc.showToast("Please check your connection");
                                return;
                            }
                            else{

                                try {
                                    JSONArray jsonArray = new JSONArray(result.getResult());
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                            previous_amount = jsonObject.getString("amount");
                                        }
                                    getUserAmount = String.valueOf(added_money_field.getText());
                                    newAmount = toInteger(previous_amount) + toInteger(getUserAmount);
                                    newAmountString = String.valueOf(newAmount);
                                    JsonObject jsonObjectnew = new JsonObject();
                                    jsonObjectnew.addProperty("newAmountString", newAmountString);
                                    Ion.with(getApplicationContext())
                                            .load("PUT",misc.ROOT_PATH + "update_amount/" + id)
                                            .setJsonObjectBody(jsonObjectnew)
                                            .asString()
                                            .withResponse()
                                            .setCallback(new FutureCallback<Response<String>>() {
                                                @Override
                                                public void onCompleted(Exception e, Response<String> result) {
                                                    if (e != null) {
                                                        misc.showToast("Please check your connection");
                                                        return;
                                                    } else {
                                                        amount = findViewById(R.id.amount);
                                                        amount.setText("Rs: " + newAmountString + "/-");
                                                        misc.showToast("Amount Rs: " + getUserAmount + "/- added");
                                                        Log.e("Number: ", cardNumber);
                                                    }

                                                }
                                            });
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }

                        }
                    });
        }
    }
    public void getUserInfo(){
        Ion.with(this)
                .load("GET",misc.ROOT_PATH+"user_profile/"+id)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            misc.showToast("Please check internet connection");
                            return;
                        }
                        else{
                            try {
                                if(result.getResult().isEmpty()){
                                    misc.showToast("No data found");
                                    return;
                                }
                                JSONObject jsonObject1 = new JSONObject(result.getResult());
                                user = jsonObject1.getString("user_role");
                                if(user.equalsIgnoreCase("customer")){
                                    Intent intent = new Intent(getApplicationContext(), AllDepartmentsActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else if(user.equalsIgnoreCase("vendor") ){
                                    Intent intent = new Intent(getApplicationContext(), ServiceHomeActivity.class);
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
    public int toInteger(String number){
        return Integer.valueOf(number);
    }
    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(this, AllDepartmentsActivity.class);
//        startActivity(intent);
//        finish();
        getUserInfo();

    }
}
