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

import com.creativeminds.facileapp.AdminActivities.ServiceProviderDetailsActivity;
import com.creativeminds.facileapp.R;

public class RegisteredServiceProvidersAdapter extends RecyclerView.Adapter<RegisteredServiceProvidersAdapter.RegisteredServiceProvidersViewHolder> {

    private Context context;
    private String[] data;

    public RegisteredServiceProvidersAdapter(Context context, String[] data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public RegisteredServiceProvidersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.registered_service_provider_item, viewGroup, false);
        return new RegisteredServiceProvidersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegisteredServiceProvidersViewHolder registeredServiceProvidersViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class RegisteredServiceProvidersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView image;
        private TextView name;

        public RegisteredServiceProvidersViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.service_image);
            name = itemView.findViewById(R.id.service_provider_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ServiceProviderDetailsActivity.class);
            context.startActivity(intent);
            ((Activity) context).finish();
        }
    }
}