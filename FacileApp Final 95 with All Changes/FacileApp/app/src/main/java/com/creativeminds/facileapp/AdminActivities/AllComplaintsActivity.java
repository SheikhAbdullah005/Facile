package com.creativeminds.facileapp.AdminActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.creativeminds.facileapp.Adapters.ComplaintsAdapter;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Complain;
import com.creativeminds.facileapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AllComplaintsActivity extends AppCompatActivity {

    private RecyclerView view;
    private Context context;
    private ArrayList<Complain> complainsListModel;
    ComplaintsAdapter complaintsAdapter;
    Misc misc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_complaints);
        setTitle("Complaints");

        context = this;
        misc = new Misc(this);

        complainsListModel = new ArrayList<>();

        view = findViewById(R.id.complaints_recycler_view);
        view.setLayoutManager(new LinearLayoutManager(this));

        if(misc.isConnectedToInternet()) {
            fetchComplains();
        }
        else{
            misc.showToast("No Internet Connection");
        }
    }

    private void fetchComplains() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("Please wait...");
        pd.show();

        Ion.with(this)
                .load(misc.ROOT_PATH+"complaint_details")
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null) {
                            misc.showToast("Please check your connection");
                            pd.dismiss();
                            return;
                        }
                        else{
                            try {
                                JSONArray jsonArray = new JSONArray(result.getResult());
                                if(jsonArray.length() < 1) {
                                    misc.showToast("No Complaints Found");
                                    pd.dismiss();
                                    return;
                                }
                                else{
                                    pd.dismiss();
                                    complainsListModel.clear();
                                    for(int i = 0; i < jsonArray.length(); i++){
                                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                        String complain_id = jsonObject.getString("complaint_id");
                                        String complain_msg = jsonObject.getString("complaint_message");
                                        String user_name = jsonObject.getString("user_name");
                                        String user_image = jsonObject.getString("user_image").replace("\"", "");

                                        complainsListModel.add(new Complain(complain_id, complain_msg, complain_msg,complain_msg,user_name));
                                    }
                                    complaintsAdapter = new ComplaintsAdapter(context, complainsListModel);
                                    view.setAdapter(complaintsAdapter);
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
        Intent intent = new Intent(this, AdminHomeActivity.class);
        startActivity(intent);
        finish();
    }
}
