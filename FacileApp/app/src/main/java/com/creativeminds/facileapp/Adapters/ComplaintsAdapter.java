package com.creativeminds.facileapp.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Complain;
import com.creativeminds.facileapp.R;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ComplaintsAdapter extends RecyclerView.Adapter<ComplaintsAdapter.ComplaintsViewHolder> {

    private Context context;
    private ArrayList<Complain> complainsListModel;


    public ComplaintsAdapter(Context context, ArrayList<Complain> complainsListModel){
        this.context = context;
        this.complainsListModel = complainsListModel;
    }

    @NonNull
    @Override
    public ComplaintsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.complaint_item, viewGroup, false);
        return new ComplaintsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintsViewHolder complaintsViewHolder, int i) {
        complaintsViewHolder.setData(complainsListModel.get(i));
    }

    @Override
    public int getItemCount() {
        return complainsListModel.size();
    }

    public class ComplaintsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView user_name, user_msg;
        private CircularImageView user_img;

        public ComplaintsViewHolder(@NonNull View itemView) {
            super(itemView);

            user_name = itemView.findViewById(R.id.complaint_name);
            user_img = itemView.findViewById(R.id.complaint_image);
            user_msg = itemView.findViewById(R.id.complaint_msg);

            itemView.setOnClickListener(this);
        }

        public void setData(Complain complain) {
            user_name.setText("Complaint of "+complain.getAgainstUserName());
            user_msg.setText(complain.getComplainMessage());
            user_img.setImageResource(R.drawable.usersicon);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
