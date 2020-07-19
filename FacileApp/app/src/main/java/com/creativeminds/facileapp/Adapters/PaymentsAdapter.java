package com.creativeminds.facileapp.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Complain;
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

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.PaymentsViewHolder> {

    private Context context;
    private ArrayList<Payment> paymentsListModel;


    public PaymentsAdapter(Context context, ArrayList<Payment> paymentsListModel){
        this.context = context;
        this.paymentsListModel = paymentsListModel;
    }

    @NonNull
    @Override
    public PaymentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.cus_payment_item, viewGroup, false);
        return new PaymentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentsViewHolder paymentsViewHolder, int i) {
        paymentsViewHolder.setData(paymentsListModel.get(i));
    }

    @Override
    public int getItemCount() {
        return paymentsListModel.size();
    }

    public class PaymentsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView payment_msg;
        private CircularImageView user_img;
        Misc misc;
        SharedPref sharedPref;
        String vendorName;

        public PaymentsViewHolder(@NonNull View itemView) {
            super(itemView);

            payment_msg = itemView.findViewById(R.id.payment_message);
            user_img = itemView.findViewById(R.id.complaint_image);
            misc = new Misc(context);
            sharedPref = new SharedPref(context);
            itemView.setOnClickListener(this);
        }

        public void setData(Payment payment) {
            user_img.setImageResource(R.drawable.sendpay);
            fetchUserName(payment.getVendorID(), payment.getTotalPayment());
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
                                    vendorName = jsonObject.getString("user_name");
                                    payment_msg.setText("You have payed Rs: "+Bill+ " to "+ vendorName+" for successfully completed your job.");
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
