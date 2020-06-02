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

import com.creativeminds.facileapp.AddtoCart;
import com.creativeminds.facileapp.MapsActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Cart;
import com.creativeminds.facileapp.Models.Department;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.SharedPref.SharedPref;

import java.util.ArrayList;
import java.util.Locale;

public class CustomerCartAdapter extends RecyclerView.Adapter<CustomerCartAdapter.CustomerCartViewHolder> {

    private Context context;
    private ArrayList<Cart> cartListModel = new ArrayList<>();
    private ArrayList<Cart> tempCartListModel = new ArrayList<>();
    private String serviceId, serviceName, departmentId;

    public CustomerCartAdapter(Context context, ArrayList<Cart> cartListModel ){
        this.context = context;
        this.cartListModel = cartListModel;
        this.tempCartListModel = new ArrayList<Cart>();
        this.tempCartListModel.addAll(cartListModel);
    }

    public void setTemp(ArrayList<Cart> cartListModel) {
        this.tempCartListModel = new ArrayList<Cart>();
        this.tempCartListModel.addAll(cartListModel);
    }


    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        cartListModel.clear();
        if (charText.length() == 0) {
            cartListModel.addAll(tempCartListModel);
        } else {
            for (Cart af : tempCartListModel) {
                if (af.getServiceName().toLowerCase().contains(charText)) {
                    cartListModel.add(af);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerCartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.customer_cart_item, viewGroup, false);
        return new CustomerCartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerCartViewHolder customerCartViewHolder, int i) {
        customerCartViewHolder.setData(cartListModel.get(i));
    }

    @Override
    public int getItemCount() {
        return cartListModel.size();
    }

    public class CustomerCartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title;
        private TextView des;
        private TextView price;
        private TextView deprt;
        Misc misc;
        SharedPref sharedPref;


        public CustomerCartViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.service_title);
            des = itemView.findViewById(R.id.service_des);
            price = itemView.findViewById(R.id.service_price);
            deprt = itemView.findViewById(R.id.dept);
            misc = new Misc(context);
            sharedPref = new SharedPref(context);

            itemView.setOnClickListener(this);
        }

        public void setData(Cart cart){
            title.setText(cart.getServiceName());
            des.setText(cart.getServiceDes());
            price.setText(cart.getServicePrice());
            deprt.setText(cart.getDeptName());

            serviceId = cart.getServiceId();
            serviceName = cart.getServiceName();
            departmentId = cart.getDepartmentId();
        }

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(context, MapsActivity.class);
            intent.putExtra("service_id", serviceId);
            intent.putExtra("department_id", departmentId);
            intent.putExtra("cart_id", cartListModel.get(getAdapterPosition()).getCartId());
            misc.showToast(departmentId);
            intent.putExtra("service_name", serviceName);
            context.startActivity(intent);
            ((Activity) context).finish();

        }
    }
}
