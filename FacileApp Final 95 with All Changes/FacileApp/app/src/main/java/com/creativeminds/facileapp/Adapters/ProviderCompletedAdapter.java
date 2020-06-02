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
import android.widget.TextView;

import com.creativeminds.facileapp.CustomerCompletedJobDetailsActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Job;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.VendorCompletedJobDetailsActivity;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProviderCompletedAdapter extends RecyclerView.Adapter<ProviderCompletedAdapter.ProviderCompletedJobViewHolder> {

    private Context context;
    private ArrayList<Job> jobsListModel;
    Misc misc;
    String customerID,customerName,stars, jobID,serviceName,vendorID;

    public ProviderCompletedAdapter(Context context, ArrayList<Job> jobsListModel) {
        this.context = context;
        this.jobsListModel = jobsListModel;
        misc = new Misc(context);

    }

    @NonNull
    @Override
    public ProviderCompletedJobViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.completed_job_item, viewGroup, false);
        return new ProviderCompletedJobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderCompletedJobViewHolder providerCompletedJobViewHolder, int i) {
        providerCompletedJobViewHolder.setData(jobsListModel.get(i));
    }

    @Override
    public int getItemCount() {
        return jobsListModel.size();
    }

    public class ProviderCompletedJobViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView comp_image;
        private TextView comp_text, comp_service;

        public ProviderCompletedJobViewHolder(@NonNull View itemView) {
            super(itemView);

            comp_image = itemView.findViewById(R.id.com_image);
            comp_text = itemView.findViewById(R.id.com_text);
            comp_service = itemView.findViewById(R.id.com_service);
            itemView.setOnClickListener(this);

        }

        public void setData(Job job){
            ///Setting Vendor Name and Service Name
            comp_service.setText(job.getServiceName());
            fetchCustomerName(job.getJobCustomerId());
        }

        public void fetchCustomerName(String CID){
            Ion.with(context)
                    .load(misc.ROOT_PATH+"user_profile/"+CID)
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            if(e != null) {
                                misc.showToast("Internet Connection Problem");
                                return;
                            }
                            else{
                                try {
                                    JSONObject jsonObject = new JSONObject(result.getResult());
                                    customerName = jsonObject.getString("user_name");
                                    comp_text.setText(customerName + " rates your service");
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, VendorCompletedJobDetailsActivity.class);
            intent.putExtra("job_id", jobsListModel.get(getAdapterPosition()).getJobId());
            intent.putExtra("address", jobsListModel.get(getAdapterPosition()).getVendorAddress());
            intent.putExtra("city", jobsListModel.get(getAdapterPosition()).getVendorCity());
            intent.putExtra("vendor_id", jobsListModel.get(getAdapterPosition()).getJobVendorId());
            intent.putExtra("customer_id", jobsListModel.get(getAdapterPosition()).getJobCustomerId());
            intent.putExtra("phone", jobsListModel.get(getAdapterPosition()).getVendorPhone());
            intent.putExtra("service_name", jobsListModel.get(getAdapterPosition()).getServiceName());
            intent.putExtra("vendor_name", jobsListModel.get(getAdapterPosition()).getCustomerName());
            intent.putExtra("lat", jobsListModel.get(getAdapterPosition()).getServiceLat());
            intent.putExtra("lon", jobsListModel.get(getAdapterPosition()).getServiceLon());
            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }
}
