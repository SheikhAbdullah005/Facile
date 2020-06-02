package com.creativeminds.facileapp;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.creativeminds.facileapp.Adapters.CustomerCartAdapter;
import com.creativeminds.facileapp.Adapters.CustomerDepartmentAdapter;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Cart;
import com.creativeminds.facileapp.Models.Department;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddtoCart extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{

    Misc misc;
    SharedPref sharedPref;
    String customerId;
    private ImageView cart;
    private TextView title;
    private TextView des;
    private TextView price;
    private TextView deprt;
    private RecyclerView recyclerView;
    CustomerCartAdapter customerCartAdapter;
    private ArrayList<Cart> cartListModel;
    private Context context;
    private String userOnline;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addto_cart);
        setTitle("Cart Activity");

        context = this;
        misc = new Misc(this);
        sharedPref = new SharedPref(this);
        cart = findViewById(R.id.cart_pic);
        title = findViewById(R.id.service_title);
        des = findViewById(R.id.service_des);
        price = findViewById(R.id.service_price);
        deprt = findViewById(R.id.dept);

        Intent intent = getIntent();
        customerId = intent.getStringExtra("customer_id");
        cartListModel = new ArrayList<>();

        recyclerView = findViewById(R.id.customer_carts);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        userOnline = sharedPref.getUserOnline();

        if(misc.isConnectedToInternet()){
            getAllCarts();
        }
        else{
            misc.showToast("No Internet Connection");
        }
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void getAllCarts(){
        Ion.with(this)
                .load(misc.ROOT_PATH+"get_carts/"+customerId)
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
                                cartListModel.clear();
                                if(jsonArray.length() < 1) {
                                    misc.showToast("Nothing Found");
                                    return;
                                }
                                for(int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                    String cart_id = jsonObject.getString("cart_id");
                                    String datee = jsonObject.getString("cart_created_date");
                                    String service_name = jsonObject.getString("service_name");
                                    String service_des = jsonObject.getString("service_des");
                                    String service_price = jsonObject.getString("service_price");
                                    String dept_name = jsonObject.getString("dept_name");
                                    String vendor_id = jsonObject.getString("vendor_id");
                                    String customer_id = jsonObject.getString("customer_id");
                                    String service_id = jsonObject.getString("fk_service_id");
                                    String department_id = jsonObject.getString("depart_id");
                                    cartListModel.add(new Cart(cart_id,customer_id, datee, service_name, service_des, service_price, dept_name,vendor_id,service_id,department_id));
                                }
                                customerCartAdapter = new CustomerCartAdapter(context, cartListModel);
                                customerCartAdapter.setTemp(cartListModel);
                                recyclerView.setAdapter(customerCartAdapter);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, com.creativeminds.facileapp.AllDepartmentsActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onClick(View v) {

    }
}
