package com.creativeminds.facileapp.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creativeminds.facileapp.AdminActivities.CustomerDetailsActivity;
import com.creativeminds.facileapp.Models.Customer;
import com.creativeminds.facileapp.R;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AllCustomersAdapter extends RecyclerView.Adapter<AllCustomersAdapter.AllCustomersViewHolder> {

    private Context context;
    private ArrayList<Customer> customersListModel;

    public AllCustomersAdapter(Context context, ArrayList<Customer> customersListModel){
        this.context = context;
        this.customersListModel = customersListModel;
    }

    @NonNull
    @Override
    public AllCustomersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.customer_item, viewGroup, false);
        return new AllCustomersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllCustomersViewHolder allCustomersViewHolder, int i) {
        allCustomersViewHolder.setData(customersListModel.get(i));
    }

    @Override
    public int getItemCount() {
        return customersListModel.size();
    }

    public class AllCustomersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CircularImageView customer_img;
        private TextView customer_nam;

        public AllCustomersViewHolder(@NonNull View itemView) {
            super(itemView);

            customer_img = itemView.findViewById(R.id.customer_image);
            customer_nam = itemView.findViewById(R.id.customer_name);

            itemView.setOnClickListener(this);
        }

        public void setData(Customer customer){
            customer_nam.setText(customer.getCustomerName());
            if(customer.getCustomerImage().isEmpty()){
                customer_img.setImageResource(R.drawable.usersicon);
            }
            else{
                Picasso.get()
                        .load(customer.getCustomerImage())
                        .into(customer_img);
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, CustomerDetailsActivity.class);
            intent.putExtra("name", customersListModel.get(getAdapterPosition()).getCustomerName());
            intent.putExtra("email", customersListModel.get(getAdapterPosition()).getCustomerEmail());
            intent.putExtra("phone", customersListModel.get(getAdapterPosition()).getCustomerPhone());
            intent.putExtra("cnic", customersListModel.get(getAdapterPosition()).getCustomerCNIC());
            intent.putExtra("image", customersListModel.get(getAdapterPosition()).getCustomerImage());
            intent.putExtra("status", customersListModel.get(getAdapterPosition()).getCustomerStatus());
            intent.putExtra("id", customersListModel.get(getAdapterPosition()).getCustomerId());

            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }
}
