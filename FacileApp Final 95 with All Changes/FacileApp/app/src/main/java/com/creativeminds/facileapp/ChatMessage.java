package com.creativeminds.facileapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.creativeminds.facileapp.Adapters.MessageAdapter;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Message;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatMessage extends AppCompatActivity implements View.OnClickListener{

    SharedPref sharedPref;
    Misc misc;
    FirebaseAuth mAuth;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    String firebaseSenderID,firebaseReceiverID;
    String jobId,vendorId,customerId,phoneNum,vendorName,serviceName,price,city,current_latitude,current_longitude;
    String from;
    EditText message;
    ImageButton sendMsg;
    private ArrayList<Message> messagesListModel;
    MessageAdapter messageAdapter;
    private RecyclerView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);
        setTitle("Chat Activity");

        misc = new Misc(this);
        sharedPref = new SharedPref(this);
        message = findViewById(R.id.input_message);
        sendMsg = findViewById(R.id.send_message_btn);
        sendMsg.setOnClickListener(this);

        messagesListModel = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messagesListModel);
        view = findViewById(R.id.private_messages_list_of_user);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setAdapter(messageAdapter);

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        firebaseSenderID = intent.getStringExtra("FirebaseSenderID");

        // Setting sender id in local db
        sharedPref.createSenderID(firebaseSenderID);

        jobId = intent.getStringExtra("job_id");
        vendorId = intent.getStringExtra("vendor_id");
        customerId = intent.getStringExtra("customerId");
        from = intent.getStringExtra("from");

        // Setting both user values in temporary model class
        sharedPref.createIDs(vendorId,customerId,from);

        phoneNum = intent.getStringExtra("phone");
        vendorName = intent.getStringExtra("vendor_name");
        serviceName = intent.getStringExtra("service_name");
        price = intent.getStringExtra("service_price");
        city = intent.getStringExtra("city");
        current_latitude = intent.getStringExtra("lat");
        current_longitude = intent.getStringExtra("lon");

        //get firebase receiver ID
        if(from.equalsIgnoreCase("customer")) {
            getReceiverID(jobId);
        }
        else {
            getReceiverID2(jobId);
        }

    }

    public void getReceiverID(String JID){
        Query query = databaseReference.child("Chats").orderByChild("jobId").equalTo(JID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        firebaseReceiverID = issue.child("id").getValue(String.class);
                    }
                }
                readMessage(firebaseSenderID,firebaseReceiverID);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                misc.showToast("Error");
            }
        });
    }

    public void getReceiverID2(String JID){
        Query query = databaseReference.child("Chats").orderByChild("jobId").equalTo(JID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        firebaseReceiverID = issue.child("cid").getValue(String.class);
                    }
                }
                readMessage(firebaseSenderID,firebaseReceiverID);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                misc.showToast("Error");
            }
        });
    }

    @Override
    public void onClick(View v) {
        String msg = message.getText().toString();
        if(!msg.equals("")){
            sendMessage(firebaseSenderID,firebaseReceiverID,msg);
            message.setText("");
        }
        else{
            misc.showToast("You can't send empty message");
        }
    }

    public void sendMessage(String sender, String receiver, String message){
        DatabaseReference msgReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        msgReference.child("ChatMessage").push().setValue(hashMap);
    }

    private void readMessage(final String myID, final String userID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatMessage");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messagesListModel.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Message message = snapshot.getValue(Message.class);
                    if(message.getReceiver().equals(myID) && message.getSender().equals(userID) ||
                            message.getReceiver().equals(userID) && message.getSender().equals(myID)){
                        messagesListModel.add(message);
                    }
                    messageAdapter = new MessageAdapter(ChatMessage.this, messagesListModel);
                    view.setAdapter(messageAdapter);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {

        if(from.equalsIgnoreCase("customer")) {

            Intent chat = new Intent(this, CustomerInProgressJobDetailsActivity.class);
            chat.putExtra("job_id", jobId);
            chat.putExtra("vendor_id", vendorId);
            chat.putExtra("customerId", customerId);
            chat.putExtra("phone", phoneNum);
            chat.putExtra("vendor_name", vendorName);
            chat.putExtra("service_name", serviceName);
            chat.putExtra("city", city);
            chat.putExtra("lat", current_latitude);
            chat.putExtra("lon", current_longitude);
            startActivity(chat);
            finish();

        }
        else {
            Intent chat = new Intent(this, VendorInProgressJobDetailsActivity.class);
            chat.putExtra("job_id", jobId);
            chat.putExtra("vendor_id", vendorId);
            chat.putExtra("customerId", customerId);
            chat.putExtra("phone", phoneNum);
            chat.putExtra("vendor_name", vendorName);
            chat.putExtra("service_name", serviceName);
            chat.putExtra("service_price", price);
            chat.putExtra("city", city);
            chat.putExtra("lat", current_latitude);
            chat.putExtra("lon", current_longitude);
            startActivity(chat);
            finish();
        }

    }

}
