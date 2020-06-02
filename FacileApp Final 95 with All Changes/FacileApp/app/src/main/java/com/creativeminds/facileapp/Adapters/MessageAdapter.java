package com.creativeminds.facileapp.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Message;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private ArrayList<Message> messagesListModel;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public String sender,receiverN;
    Misc misc;
    SharedPref sharedPref;
    StorageReference storageReference;
    DatabaseReference databaseReference;


    public MessageAdapter(Context context, ArrayList<Message> messagesListModel){
        this.context = context;
        this.messagesListModel = messagesListModel;

        misc = new Misc(context);
        sharedPref = new SharedPref(context);
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(i==MSG_TYPE_RIGHT) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View view = inflater.inflate(R.layout.chat_item_right, viewGroup, false);
            return new MessageViewHolder(view);
        }
        else {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View view = inflater.inflate(R.layout.chat_item_left, viewGroup, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {
        messageViewHolder.setData(messagesListModel.get(i));
    }

    @Override
    public int getItemCount() {
        return messagesListModel.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView show_message, receiverName;
        CircularImageView user_img;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            receiverName = itemView.findViewById(R.id.name);
            user_img = itemView.findViewById(R.id.userImage);
            itemView.setOnClickListener(this);
        }

        public void setData(Message message) {
            //Fetching FirebaseIDValues
            if(sharedPref.getFromUser().equalsIgnoreCase("customer")){
                Ion.with(context)
                        .load("GET",misc.ROOT_PATH+"user_profile/"+sharedPref.getVenId())
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
                                        receiverN = jsonObject.getString("user_name");
                                        receiverName.setText(receiverN);
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        });
            }
            else{
                Ion.with(context)
                        .load("GET",misc.ROOT_PATH+"user_profile/"+sharedPref.getCusId())
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
                                        receiverN = jsonObject.getString("user_name");
                                        receiverName.setText(receiverN);
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        });
            }
            receiverName.setText(receiverN);
            user_img.setImageResource(R.drawable.user);
            show_message.setText(message.getMessage());
        }

        @Override
        public void onClick(View v) {
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(messagesListModel.get(position).getSender().equals(sharedPref.getSenderId())){
            return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }
    }
}
