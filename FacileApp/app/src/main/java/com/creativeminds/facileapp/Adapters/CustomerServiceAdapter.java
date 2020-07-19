package com.creativeminds.facileapp.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.creativeminds.facileapp.AddtoCart;
import com.creativeminds.facileapp.AllServiceActivity;
import com.creativeminds.facileapp.Models.Vendor;
import com.creativeminds.facileapp.ProfileActivity;
import com.creativeminds.facileapp.RegisterActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.creativeminds.facileapp.AdminActivities.AllCustomersActivity;
import com.creativeminds.facileapp.MapsActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Service;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CustomerServiceAdapter extends RecyclerView.Adapter<CustomerServiceAdapter.CustomerServiceViewHolder> {

    private Context context;
    private ArrayList<Service> serviceListModel = new ArrayList<>();
    private ArrayList<Service> tempServiceListModel = new ArrayList<>();
    private String vendor_id,departmentName;

    public CustomerServiceAdapter(Context context, ArrayList<Service> serviceListModel ){
        this.context = context;
        this.serviceListModel = serviceListModel;
        this.tempServiceListModel = new ArrayList<Service>();
        this.tempServiceListModel.addAll(serviceListModel);
    }

    public void setTemp(ArrayList<Service> serviceListModel) {
        this.tempServiceListModel = new ArrayList<Service>();
        this.tempServiceListModel.addAll(serviceListModel);
    }


    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        serviceListModel.clear();
        if (charText.length() == 0) {
            serviceListModel.addAll(tempServiceListModel);
        } else {
            for (Service af : tempServiceListModel) {
                if (af.getServiceName().toLowerCase().contains(charText)) {
                    serviceListModel.add(af);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerServiceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.customer_service_item, viewGroup, false);
        return new CustomerServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerServiceViewHolder customerServiceViewHolder, int i) {
        customerServiceViewHolder.setData(serviceListModel.get(i));
    }

    @Override
    public int getItemCount() {
        return serviceListModel.size();
    }

    public class CustomerServiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView cart;
        private TextView title;
        private TextView des;
        private TextView price;
        private TextView deprt;
        private RatingBar ratingBar;
        Misc misc;
        SharedPref sharedPref;

        public CustomerServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cart = itemView.findViewById(R.id.cart_pic);
            title = itemView.findViewById(R.id.service_title);
            des = itemView.findViewById(R.id.service_des);
            price = itemView.findViewById(R.id.service_price);
            deprt = itemView.findViewById(R.id.dept);
            misc = new Misc(context);
            sharedPref = new SharedPref(context);

            itemView.setOnClickListener(this);
        }

        public void setData(Service service){
            title.setText(service.getServiceName());
            des.setText(service.getServiceDes());
            price.setText(service.getServicePrice());
            deprt.setText(service.getDeptName());
            departmentName = deprt.getText().toString();
            fetchVendor();
        }

        private void fetchVendor(){
            Ion.with(context)
                    .load(misc.ROOT_PATH+"fetch_vendors/"+serviceListModel.get(getAdapterPosition()).getDepartmentId())
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
                                    if(jsonArray.length() < 1) {
                                        misc.showToast("No Vendor had this service yet");
                                        return;
                                    }
                                    for(int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                        vendor_id = jsonObject.getString("user_id");
                                     }
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
        }

        @Override
        public void onClick(View v) {
            if(vendor_id == null) {
                misc.showToast("You can't add this service to cart because this service has no vendor.");
            }
            else {
                AddToCart();
            }
        }

        private void AddToCart() {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();

            JsonObject cart = new JsonObject();
            cart.addProperty("cart_created_date", dateFormat.format(date));
            cart.addProperty("service_name", serviceListModel.get(getAdapterPosition()).getServiceName());
            cart.addProperty("service_des", serviceListModel.get(getAdapterPosition()).getServiceDes());
            cart.addProperty("service_price", serviceListModel.get(getAdapterPosition()).getServicePrice());
            cart.addProperty("dept_name", serviceListModel.get(getAdapterPosition()).getDeptName());
            cart.addProperty("vendor_id", vendor_id);
            cart.addProperty("customer_id", sharedPref.getUserId());
            cart.addProperty("fk_service_id", serviceListModel.get(getAdapterPosition()).getServiceId());
            cart.addProperty("depart_id", serviceListModel.get(getAdapterPosition()).getDepartmentId());

            Ion.with(context)
                    .load(misc.ROOT_PATH+"new_cart")
                    .setJsonObjectBody(cart)
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            if(e != null) {
                                misc.showToast("Please check your connection");
                                return;
                            }
                            String response = result.getResult();
                            if (response.isEmpty()) {
                                misc.showToast("Already in Cart, Select another");
                                return;
                            }
                            else{
                                changeCartStatus();
                                misc.showToast("Added to Cart!");
                                Intent intent = new Intent(context, AddtoCart.class);
                                intent.putExtra("customer_id", sharedPref.getUserId());
                                context.startActivity(intent);
                                ((Activity) context).finish();
                            }
                        }
                    });
        }

        public void changeCartStatus(){
            JsonObject cart1 = new JsonObject();
            cart1.addProperty("service_cart", "carted");
            Ion.with(context)
                    .load("PUT",misc.ROOT_PATH + "update_cart_status/"+serviceListModel.get(getAdapterPosition()).getServiceId())
                    .setJsonObjectBody(cart1)
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            if(e != null) {
                                misc.showToast("Please check your connection");
                                return;
                            }
                            String response = result.getResult();
                            if (response.isEmpty()) {
                                return;
                            }
                            else{
                                misc.showToast("Status Changed!");
                            }
                        }
                    });
        }

    }
}
