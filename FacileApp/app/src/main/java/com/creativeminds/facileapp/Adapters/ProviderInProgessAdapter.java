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

import com.creativeminds.facileapp.CustomerInProgressJobDetailsActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Job;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.VendorInProgressJobDetailsActivity;

import java.util.ArrayList;

public class ProviderInProgessAdapter extends RecyclerView.Adapter<ProviderInProgessAdapter.ProviderInProgressViewHolder> {

    private ArrayList<Job> jobsListModel = new ArrayList<>();
    private Context context;
    Misc misc;

    public ProviderInProgessAdapter(Context context, ArrayList<Job> jobsListModel){
        this.context = context;
        this.jobsListModel = jobsListModel;
        misc = new Misc(context);
    }

    @NonNull
    @Override
    public ProviderInProgressViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.in_progress_job_item, viewGroup, false);
        return new ProviderInProgressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderInProgressViewHolder providerInProgressViewHolder, int i) {
        providerInProgressViewHolder.setData(jobsListModel.get(i));
    }

    @Override
    public int getItemCount() {
        return jobsListModel.size();
    }

    public class ProviderInProgressViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView text_item;
        private ImageView image_item;
        public ProviderInProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            text_item = itemView.findViewById(R.id.ip_text);
            image_item = itemView.findViewById(R.id.ip_image);

            itemView.setOnClickListener(this);
        }
        public void setData(Job job){
            text_item.setText("Your "  + job.getServiceName()+"'s" +" is in progress " );
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(context, VendorInProgressJobDetailsActivity.class);
            intent.putExtra("job_id", jobsListModel.get(getAdapterPosition()).getJobId());
            intent.putExtra("address", jobsListModel.get(getAdapterPosition()).getVendorAddress());
            intent.putExtra("city", jobsListModel.get(getAdapterPosition()).getVendorCity());
            intent.putExtra("phone", jobsListModel.get(getAdapterPosition()).getVendorPhone());
            intent.putExtra("service_name", jobsListModel.get(getAdapterPosition()).getServiceName());
            intent.putExtra("service_price", jobsListModel.get(getAdapterPosition()).getServicePrice());
            intent.putExtra("vendor_id", jobsListModel.get(getAdapterPosition()).getJobVendorId());
            intent.putExtra("vendor_name", jobsListModel.get(getAdapterPosition()).getCustomerName());
            intent.putExtra("lat", jobsListModel.get(getAdapterPosition()).getServiceLat());
            intent.putExtra("lon", jobsListModel.get(getAdapterPosition()).getServiceLon());
            intent.putExtra("customerId", jobsListModel.get(getAdapterPosition()).getJobCustomerId());
            intent.putExtra("serviceId", jobsListModel.get(getAdapterPosition()).getJobServiceId());
            intent.putExtra("datee", jobsListModel.get(getAdapterPosition()).getJobStartTime());
            context.startActivity(intent);
            ((Activity) context).finish();

        }
    }
}
