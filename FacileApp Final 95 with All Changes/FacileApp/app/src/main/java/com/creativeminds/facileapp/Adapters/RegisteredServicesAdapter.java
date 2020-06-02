package com.creativeminds.facileapp.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.creativeminds.facileapp.Models.Service;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.AdminActivities.UpdateServiceActivity;

import java.util.ArrayList;
import java.util.Locale;

public class RegisteredServicesAdapter extends RecyclerView.Adapter<RegisteredServicesAdapter.RegisteredServiceViewHolder> {

    private Context context;
    private ArrayList<com.creativeminds.facileapp.Models.Service> serviceListModel = new ArrayList<>();
    private ArrayList<com.creativeminds.facileapp.Models.Service> tempServiceListModel = new ArrayList<>();

    public RegisteredServicesAdapter(Context context, ArrayList<Service> serviceListModel) {
        this.context = context;
        this.serviceListModel = serviceListModel;
        this.tempServiceListModel = tempServiceListModel;
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
    public RegisteredServiceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.customer_service_item, viewGroup, false);
        return new RegisteredServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegisteredServiceViewHolder registeredServiceViewHolder, int i) {
        registeredServiceViewHolder.setData(serviceListModel.get(i));
    }

    @Override
    public int getItemCount() {
        return serviceListModel.size();
    }

    public class RegisteredServiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private ImageView image;
        private TextView title;
        com.creativeminds.facileapp.Misc.Misc misc;
        com.creativeminds.facileapp.SharedPref.SharedPref sharedPref;

        public RegisteredServiceViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.service_picture);
            title = itemView.findViewById(R.id.service_title);
            misc = new com.creativeminds.facileapp.Misc.Misc(context);
            sharedPref = new com.creativeminds.facileapp.SharedPref.SharedPref(context);

            itemView.setOnClickListener(this);
        }


        public void setData(Service service){
            title.setText(service.getServiceName());
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, UpdateServiceActivity.class);
            intent.putExtra("service_id", serviceListModel.get(getAdapterPosition()).getServiceId());
            intent.putExtra("service_name", serviceListModel.get(getAdapterPosition()).getServiceName());
            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }
}
