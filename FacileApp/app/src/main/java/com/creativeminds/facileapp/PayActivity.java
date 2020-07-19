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
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

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

public class PayActivity extends AppCompatActivity {
    Misc misc;
    Stripe stripe;
    Integer amount;
    String name,abc,cardNumber;
    Card card;
    setIpAddresses ip;
    Token tok;
    String jobId,vendorId,customerId,phoneNum,vendorName,serviceName,price,city,current_latitude,current_longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        misc = new Misc(this);
        setTitle("Add Credit Card");
        Intent intent_customer_in_progress;
        Intent intent = getIntent();
        abc = intent.getStringExtra("plan_price");
        jobId = intent.getStringExtra("job_id");
        vendorId = intent.getStringExtra("vendor_id");
        customerId = intent.getStringExtra("customerId");
        phoneNum = intent.getStringExtra("phone");
        vendorName = intent.getStringExtra("vendor_name");
        serviceName = intent.getStringExtra("service_name");
        ip = new setIpAddresses();
        price = intent.getStringExtra("service_price");
        city = intent.getStringExtra("city");
        current_latitude = intent.getStringExtra("lat");
        current_longitude = intent.getStringExtra("lon");
        amount = Integer.parseInt(price);
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
        Log.e("cardNumber",cardNumber);
        card.setCurrency("pkr");
        card.setName("Abdullah Sheikh");
        card.setAddressZip("44000");

        stripe.createToken(card, "pk_test_QZIZYjAJ4AQ9bH2XVyMA1cVx00znVdjm1z", new TokenCallback() {
            public void onSuccess(Token token) {
                // TODO: Send Token information to your backend to initiate a charge
               // Toast.makeText(getApplicationContext(), "Token created: " + token.getId(), Toast.LENGTH_LONG).show();
                addCardCredentials(cardNumber,customerId,"0");
                tok = token;
                new StripeCharge(token.getId()).execute();

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
            onBackPressed();
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

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new NameValuePair("method", "charge"));
            params.add(new NameValuePair("description", description));
            params.add(new NameValuePair("source", token));
            params.add(new NameValuePair("amount", amount));

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

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
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

    @Override
    public void onBackPressed() {

        Intent chat = new Intent(this, CustomerInProgressJobDetailsActivity.class);
        chat.putExtra("job_id", jobId);
        chat.putExtra("vendor_id", vendorId);
        chat.putExtra("customerId", customerId);
        chat.putExtra("phone", phoneNum);
        chat.putExtra("vendor_name", vendorName);
        chat.putExtra("service_name", serviceName);
        chat.putExtra("city", city);
        chat.putExtra("lat", current_latitude);
        chat.putExtra("lon", current_longitude);
        startActivity(chat);
        finish();

    }
}


