package com.creativeminds.facileapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.ServiceProviderActivities.ServiceHomeActivity;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class addNewCreditCard extends AppCompatActivity {
    Misc misc;
    Stripe stripe;
    Integer amount;
    String name,cardNumber;
    Card card;
    Token tok;
    setIpAddresses ip;
    String customerId,user;
    SharedPref sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_credit_card);
        misc = new Misc(this);
        setTitle("Add Credit Card");
        Intent getId = getIntent();
        customerId = getId.getStringExtra("customerId");
        ip = new setIpAddresses();
        name = "basic plan";
        try {
            stripe = new Stripe("pk_test_QZIZYjAJ4AQ9bH2XVyMA1cVx00znVdjm1z");
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

    }

    public void submitCard(final View view) {
        // TODO: replace with your own test key
        TextView cardNumberField = (TextView) findViewById(R.id.cardNumber);
        TextView monthField = (TextView) findViewById(R.id.month);
        TextView yearField = (TextView) findViewById(R.id.year);
        TextView cvcField = (TextView) findViewById(R.id.cvc);

        card = new Card(
                cardNumberField.getText().toString(),
                Integer.valueOf(monthField.getText().toString()),
                Integer.valueOf(yearField.getText().toString()),
                cvcField.getText().toString()
        );
        cardNumber = card.getNumber();
        card.setCurrency("pkr");
        card.setName("Abdullah Sheikh");
        card.setAddressZip("44000");

        stripe.createToken(card, "pk_test_QZIZYjAJ4AQ9bH2XVyMA1cVx00znVdjm1z", new TokenCallback() {
            public void onSuccess(Token token) {
                // TODO: Send Token information to your backend to initiate a charge
                // Toast.makeText(getApplicationContext(), "Token created: " + token.getId(), Toast.LENGTH_LONG).show();
                addCardCredentials(cardNumber,customerId,"0");
                tok = token;
                new addNewCreditCard.StripeCharge(token.getId()).execute();

            }

            private void addCardCredentials(String cardNumber, String customerId, String price) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", customerId);
                jsonObject.addProperty("cardNumber", cardNumber);
                jsonObject.addProperty("price",0);

                Ion.with(getApplicationContext())
                        .load(misc.ROOT_PATH+"addCardDetails")
                        .setJsonObjectBody(jsonObject)
                        .asString()
                        .withResponse()
                        .setCallback(new FutureCallback<Response<String>>() {
                            @Override
                            public void onCompleted(Exception e, Response<String> result) {
                                if(e != null) {
                                    //  misc.showToast("Please check internet connection");
                                }else{
                                    Log.e("Message","Card Added");
                                    // misc.showToast("Card Added");

                                }

                            }
                        });
            }


            public void onError(Exception error) {
                Toast.makeText(getApplicationContext(), "Error: " +  error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.d("Stripe", error.getLocalizedMessage());
            }

        });
    }

    public class StripeCharge extends AsyncTask<String, Void, String> {
        String token;

        public StripeCharge(String token) {
            this.token = token;
        }

        @Override
        protected String doInBackground(String... params) {
            new Thread() {
                @Override
                public void run() {
                    postData(name,token,""+amount);
                }
            }.start();
            return "Done";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(), "Card added", Toast.LENGTH_LONG).show();
            moveBack();
        }
    }

    public void postData(String description, String token,String amount) {
        // Create a new HttpClient and Post Header
        try {
            // Write the url where your php server files located
            URL url = new URL(ip.getUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            List<addNewCreditCard.NameValuePair> params = new ArrayList<addNewCreditCard.NameValuePair>();
            params.add(new addNewCreditCard.NameValuePair("method", "charge"));
            params.add(new addNewCreditCard.NameValuePair("description", description));
            params.add(new addNewCreditCard.NameValuePair("source", token));
            params.add(new addNewCreditCard.NameValuePair("amount", amount));

            OutputStream os = null;

            os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(params));
            writer.flush();
            writer.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getQuery(List<addNewCreditCard.NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (addNewCreditCard.NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));

        }
        Log.e("Query",result.toString());
        return result.toString();
    }

    public class NameValuePair{
        String name,value;

        public NameValuePair(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public void moveBack() {

        Intent wallet = new Intent(getApplicationContext(), wallet.class);
        wallet.putExtra("id", customerId);
        startActivity(wallet);
        finish();

    }
    public void getUserInfo(){
        Ion.with(this)
                .load("GET",misc.ROOT_PATH+"user_profile/"+customerId)
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

    @Override
    public void onBackPressed() {
        getUserInfo();
    }
}

