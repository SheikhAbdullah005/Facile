package com.creativeminds.facileapp.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Payment;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VendorPaymentsAdapter extends RecyclerView.Adapter<VendorPaymentsAdapter.VendorPaymentsViewHolder> {

    private Context context;
    private ArrayList<Payment> vendorPaymentsListModel;


    public VendorPaymentsAdapter(Context context, ArrayList<Payment> vendorPaymentsListModel){
        this.context = context;
        this.vendorPaymentsListModel = vendorPaymentsListModel;
    }

    @NonNull
    @Override
    public VendorPaymentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.cus_payment_item, viewGroup, false);
        return new VendorPaymentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VendorPaymentsViewHolder vendorPaymentsViewHolder, int i) {
        vendorPaymentsViewHolder.setData(vendorPaymentsListModel.get(i));
    }

    @Override
    public int getItemCount() {
        return vendorPaymentsListModel.size();
    }

    public class VendorPaymentsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView payment_msg;
        private CircularImageView user_img;
        Misc misc;
        SharedPref sharedPref;
        String customerName;

        public VendorPaymentsViewHolder(@NonNull View itemView) {
            super(itemView);

            payment_msg = itemView.findViewById(R.id.payment_message);
            user_img = itemView.findViewById(R.id.complaint_image);
            misc = new Misc(context);
            sharedPref = new SharedPref(context);
            itemView.setOnClickListener(this);
        }

        public void setData(Payment payment) {
            user_img.setImageResource(R.drawable.receivepay);
            fetchUserName(payment.getCustomerID(), payment.getTotalPayment());
        }

        public void fetchUserName(String UID,final String Bill){
            Ion.with(context)
                    .load(misc.ROOT_PATH+"user_profile/"+UID)
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
                                    payment_msg.setText("You received Rs: "+Bill+ " from "+ customerName+" for successful compilation of his job.");
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
        }

        @Override
        public void onClick(View v) {

        }
    }
}
