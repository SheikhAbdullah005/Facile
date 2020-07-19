package com.creativeminds.facileapp;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.creativeminds.facileapp.Misc.Misc;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.facebook.FacebookSdk.getApplicationContext;

public class WalletDeductionHandler {
    Misc misc;
    String amount;
    String newAmount;
    String id;

    public WalletDeductionHandler() {
        this.misc = new Misc(getApplicationContext());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void deductMoney(){
        //misc.showToast(getId());
        Ion.with(getApplicationContext())
                .load("GET",misc.ROOT_PATH+"get_wallet/"+getId())
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
                                    amount = jsonObject.getString("amount");
                                }
//                                misc.showToast(amount);
                                newAmount = String.valueOf(Integer.valueOf(amount)-100);
                                JsonObject jsonObjectnew = new JsonObject();
                                jsonObjectnew.addProperty("newAmountString", newAmount);
                                Ion.with(getApplicationContext())
                                        .load("PUT",misc.ROOT_PATH + "update_amount/" + getId())
                                        .setJsonObjectBody(jsonObjectnew)
                                        .asString()
                                        .withResponse()
                                        .setCallback(new FutureCallback<Response<String>>() {
                                            @Override
                                            public void onCompleted(Exception e, Response<String> result) {
                                                if(e != null) {
                                                    misc.showToast("Please check your connection");
                                                    return;
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
