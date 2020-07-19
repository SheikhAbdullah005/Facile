package com.creativeminds.facileapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.creativeminds.facileapp.Adapters.MessageAdapter;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.Message;
import com.creativeminds.facileapp.SharedPref.SharedPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.koushikdutta.ion.builder.Builders;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatMessage extends AppCompatActivity implements View.OnClickListener{
    FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    SharedPref sharedPref;
    Misc misc;
    FirebaseUser mAuth;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    String firebaseSenderID,firebaseReceiverID;
    String jobId,vendorId,customerId,phoneNum,vendorName,serviceName,price,city,current_latitude,current_longitude;
    String from;
    String toEmail;
    String currentEmail;
    String FSI,FRI;
    EditText message;
    Intent chat;
    ImageButton sendMsg;
    private ArrayList<Message> messagesListModel;
    MessageAdapter messageAdapter;
    private RecyclerView view;
    FirebaseDatabase fbsnew;
    DatabaseReference ref,readMessages;
    ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);
        setTitle("Chat");

        misc = new Misc(this);
        sharedPref = new SharedPref(this);
        currentEmail=sharedPref.getEmail();
        message = findViewById(R.id.input_message);
        sendMsg = findViewById(R.id.send_message_btn);
        sendMsg.setOnClickListener(this);

        messagesListModel = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messagesListModel);
        view = findViewById(R.id.private_messages_list_of_user);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.setAdapter(messageAdapter);
        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        //firebaseSenderID = intent.getStringExtra("FirebaseSenderID");

        // Setting sender id in local db
        //sharedPref.createSenderID(firebaseSenderID);

        jobId = intent.getStringExtra("job_id");
        vendorId = intent.getStringExtra("vendor_id");
        customerId = intent.getStringExtra("customerId");
        from = intent.getStringExtra("from");
        toEmail = intent.getStringExtra("receiverEmail");
        // Setting both user values in temporary model class
        sharedPref.createIDs(vendorId,customerId,from);

        phoneNum = intent.getStringExtra("phone");
        vendorName = intent.getStringExtra("vendor_name");
        serviceName = intent.getStringExtra("service_name");
        price = intent.getStringExtra("service_price");
        city = intent.getStringExtra("city");
        current_latitude = intent.getStringExtra("lat");
        current_longitude = intent.getStringExtra("lon");
        getFirebaseFRI();
        getFirebaseFSI();
       // Toast.makeText(this,firebaseSenderID,Toast.LENGTH_LONG).show();
        Query q1 = FirebaseDatabase.getInstance().getReference("ChatMessage");
        q1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                readMessage(firebaseSenderID,firebaseReceiverID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        seenMessage(firebaseReceiverID);
        setIntents();
    }

    public void getFirebaseFRI(){
        Query query = databaseReference.child("Users").orderByChild("email").equalTo(toEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        firebaseReceiverID = issue.child("id").getValue(String.class);
                        sharedPref.firebaseReceiverId(firebaseReceiverID);
                        Log.e("Email",firebaseReceiverID);
                    }
                }
                //readMessage(firebaseSenderID,firebaseReceiverID);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                misc.showToast("Error");
            }
        });
    }

    public void getFirebaseFSI(){
        Query query = databaseReference.child("Users").orderByChild("email").equalTo(currentEmail);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        firebaseSenderID = issue.child("id").getValue(String.class);
                        sharedPref.firebaseSenderId(firebaseSenderID);
                        Log.e("Email1",firebaseSenderID);
                    }
                }
                //readMessage(firebaseSenderID,firebaseReceiverID);
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
    private  void seenMessage(String userId){
        Query reference = FirebaseDatabase.getInstance().getReference("ChatMessage");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Message message = snapshot.getValue(Message.class);
                    if(message.getReceiver().equals(firebaseSenderID) && message.getSender().equals(firebaseReceiverID)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void sendMessage(String sender, String receiver, String message){
        DatabaseReference msgReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        msgReference.child("ChatMessage").push().setValue(hashMap);
    }

    private void readMessage(final String myID, final String userID) throws  NullPointerException{
        Query reference = FirebaseDatabase.getInstance().getReference("ChatMessage");
        reference.addValueEventListener(new ValueEventListener() {
            @NonNull
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
                    view.scrollToPosition(messageAdapter.getItemCount()-1);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        final String msg = message.getText().toString();
        //Query query4 = databaseReference.child("ChatMessage").orderByChild("receiver").equalTo(firebaseSenderID);
        readMessages = FirebaseDatabase.getInstance().getReference("ChatMessage");
        Query query4 = readMessages.limitToLast(1);
        query4.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    Message message = data.getValue(Message.class);
                    if (message.getSender() == myID) {
                        //misc.showToast("I am the sender myself");
                    } else{
                        notification();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void notification(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("n","n",NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, uniqueInt, chat, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"n")
                .setContentTitle("New Message")
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText("You have a new message.")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_send_black_24dp)
                .setAutoCancel(true);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, StopScript.class), PendingIntent.FLAG_UPDATE_CURRENT);


//        builder.setContentIntent(contentIntent);
        NotificationManagerCompat managerCompact = NotificationManagerCompat.from(this);
        managerCompact.notify(999,builder.build());
    }

    @Override
    public void onBackPressed() {
        //setIntents();
        startActivity(chat);
        finish();

    }
    public void setIntents(){
        if(from.equalsIgnoreCase("customer")) {
            chat = new Intent(this, CustomerInProgressJobDetailsActivity.class);
            chat.putExtra("job_id", jobId);
            chat.putExtra("vendor_id", vendorId);
            chat.putExtra("customerId", customerId);
            chat.putExtra("phone", phoneNum);
            chat.putExtra("vendor_name", vendorName);
            chat.putExtra("service_name", serviceName);
            chat.putExtra("city", city);
            chat.putExtra("lat", current_latitude);
            chat.putExtra("lon", current_longitude);

        }
        else {
            chat = new Intent(this, VendorInProgressJobDetailsActivity.class);
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

        }

    }

}
