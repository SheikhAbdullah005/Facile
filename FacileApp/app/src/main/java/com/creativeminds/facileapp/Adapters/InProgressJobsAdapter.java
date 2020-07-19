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
import com.creativeminds.facileapp.Models.Chat;
import com.creativeminds.facileapp.Models.Job;
import com.creativeminds.facileapp.Models.UploadPdf;
import com.creativeminds.facileapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class InProgressJobsAdapter extends RecyclerView.Adapter<InProgressJobsAdapter.InProgressJobsViewHolder> {

    private ArrayList<Job> jobsListModel = new ArrayList<>();
    private Context context;
    public String customerId;
    Misc misc;

    public InProgressJobsAdapter(Context context, ArrayList<Job> jobsListModel, String customerId){
        this.context = context;
        this.jobsListModel = jobsListModel;
        misc = new Misc(context);
        this.customerId = customerId;
    }

    @NonNull
    @Override
    public InProgressJobsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.in_progress_job_item, viewGroup, false);
        return new InProgressJobsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InProgressJobsViewHolder inProgressJobsViewHolder, int i) {
        inProgressJobsViewHolder.setData(jobsListModel.get(i));

    }

    @Override
    public int getItemCount() {
        return jobsListModel.size();
    }

    public class InProgressJobsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView text_item;
        private ImageView image_item;
        StorageReference storageReference;
        DatabaseReference databaseReference;

        public InProgressJobsViewHolder(@NonNull View itemView) {
            super(itemView);

            text_item = itemView.findViewById(R.id.ip_text);
            image_item = itemView.findViewById(R.id.ip_image);
            storageReference = FirebaseStorage.getInstance().getReference();
            databaseReference = FirebaseDatabase.getInstance().getReference();

            itemView.setOnClickListener(this);
        }

        public void setData(Job job){
            text_item.setText(job.getCustomerName() + " "  + job.getServiceName()+"'s"+ " Job is in progress " );
            //setting Firebase chat
            getVendorID(job.getJobVendorId(), job.getJobCustomerId(), job.getJobId());
        }

        public void getVendorID(String VID, final String CID, final String JID){
            Ion.with(context)
                    .load("GET",misc.ROOT_PATH+"user_profile/"+VID)
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
                                    JSONObject jsonObject = new JSONObject(result.getResult());
                                    final String vendorPhone = jsonObject.getString("user_phone");
                                    Query query = databaseReference.child("Users").orderByChild("phone").equalTo(vendorPhone);
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                                    String currentVendorID = issue.child("id").getValue(String.class);
                                                    getCustomerID(currentVendorID, CID, JID);
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            misc.showToast("Error");
                                        }
                                    });
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
        }

        public void getCustomerID(final String VID, final String CID, final String JID){
            Ion.with(context)
                    .load("GET",misc.ROOT_PATH+"user_profile/"+CID)
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
                                    JSONObject jsonObject = new JSONObject(result.getResult());
                                    final String cusPhone = jsonObject.getString("user_phone");
                                    Query query = databaseReference.child("Users").orderByChild("phone").equalTo(cusPhone);
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                                    String currentCustomerID = issue.child("id").getValue(String.class);
                                                    Chat chat = new Chat(VID,currentCustomerID, cusPhone, JID);
                                                    databaseReference.child("Chats").child(JID).setValue(chat);
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            misc.showToast("Error");
                                        }
                                    });
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, CustomerInProgressJobDetailsActivity.class);
            intent.putExtra("job_id", jobsListModel.get(getAdapterPosition()).getJobId());
            intent.putExtra("address", jobsListModel.get(getAdapterPosition()).getVendorAddress());
            intent.putExtra("city", jobsListModel.get(getAdapterPosition()).getVendorCity());
            intent.putExtra("phone", jobsListModel.get(getAdapterPosition()).getVendorPhone());
            intent.putExtra("service_name", jobsListModel.get(getAdapterPosition()).getServiceName());
            intent.putExtra("vendor_id", jobsListModel.get(getAdapterPosition()).getJobVendorId());
            intent.putExtra("vendor_name", jobsListModel.get(getAdapterPosition()).getCustomerName());
            intent.putExtra("lat", jobsListModel.get(getAdapterPosition()).getServiceLat());
            intent.putExtra("lon", jobsListModel.get(getAdapterPosition()).getServiceLon());
            intent.putExtra("customerId", jobsListModel.get(getAdapterPosition()).getJobCustomerId());
            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }
}
