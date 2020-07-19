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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.creativeminds.facileapp.AdminActivities.AllCustomersActivity;
import com.creativeminds.facileapp.AllServiceActivity;
import com.creativeminds.facileapp.MapsActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Department;
import com.creativeminds.facileapp.Models.Service;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.SharedPref.SharedPref;

import java.util.ArrayList;
import java.util.Locale;

public class CustomerDepartmentAdapter extends RecyclerView.Adapter<CustomerDepartmentAdapter.CustomerDepartmentViewHolder> {

    private Context context;
    private ArrayList<Department> serviceListModel = new ArrayList<>();
    private ArrayList<Department> tempServiceListModel = new ArrayList<>();

    public CustomerDepartmentAdapter(Context context, ArrayList<Department> serviceListModel ){
        this.context = context;
        this.serviceListModel = serviceListModel;
        this.tempServiceListModel = new ArrayList<Department>();
        this.tempServiceListModel.addAll(serviceListModel);
    }

    public void setTemp(ArrayList<Department> serviceListModel) {
        this.tempServiceListModel = new ArrayList<Department>();
        this.tempServiceListModel.addAll(serviceListModel);
    }


    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        serviceListModel.clear();
        if (charText.length() == 0) {
            serviceListModel.addAll(tempServiceListModel);
        } else {
            for (Department af : tempServiceListModel) {
                if (af.getDepartmentName().toLowerCase().contains(charText)) {
                    serviceListModel.add(af);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerDepartmentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.customer_department_item, viewGroup, false);
        return new CustomerDepartmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerDepartmentViewHolder customerDepartmentViewHolder, int i) {
        customerDepartmentViewHolder.setData(serviceListModel.get(i));
    }

    @Override
    public int getItemCount() {
        return serviceListModel.size();
    }

    public class CustomerDepartmentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title;
        private RelativeLayout toolsView;
        Misc misc;
        SharedPref sharedPref;

        public CustomerDepartmentViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.service_title);
            misc = new Misc(context);
            sharedPref = new SharedPref(context);

            itemView.setOnClickListener(this);
        }

        public void setData(Department department){
            title.setText(department.getDepartmentName());
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(context, AllServiceActivity.class);
            intent.putExtra("department_id", serviceListModel.get(getAdapterPosition()).getDepartmentId());
            intent.putExtra("department_name", serviceListModel.get(getAdapterPosition()).getDepartmentName());
            context.startActivity(intent);
            ((Activity) context).finish();

        }
    }
}
