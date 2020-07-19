package com.creativeminds.facileapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.UploadPdf;
import com.creativeminds.facileapp.ServiceProviderActivities.RegisterServiceActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class UploadPortfolio extends AppCompatActivity implements View.OnClickListener{

    private Button portfolio, next;
    private final int REQUEST_CODE = 1;
    private File uploadFile = null;
    private static String resultPath = null;
    Misc misc;
    private PDFView pdfView;
    Uri selectedImageUri;
    private String pdfFileUrl, ID;

    StorageReference storageReference;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_portfolio);
        setTitle("Registration Step 1");

        misc = new Misc(this);

        pdfView = (PDFView) findViewById(R.id.pdfView);
        portfolio = findViewById(R.id.port_file);
        next = findViewById(R.id.next_step);
        portfolio.setOnClickListener(this);
        next.setOnClickListener(this);

//        Intent intent = getIntent();
//        pdfFileUrl = intent.getStringExtra("PDFFileURL");

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("uploads");

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == portfolio.getId()){
            selectPDFFile();
        }
        if(v.getId() == next.getId()){
            Intent intent = new Intent(UploadPortfolio.this, RegisterServiceActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void selectPDFFile() {
        //creating an intent for file chooser
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF File"), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            selectedImageUri = data.getData();
            pdfView.fromUri(selectedImageUri)
                    .pages(0, 2, 1, 3, 3, 3) // all pages are displayed by default
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .password(null)
                    .scrollHandle(null)
                    .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                    .spacing(0)
                    .load();
            uploadPDF(data.getData());
        }
    }

    private void uploadPDF(Uri data) {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading...");
        pd.show();

        StorageReference reference = storageReference.child("uploads/"+System.currentTimeMillis()+".pdf");
        reference.putFile(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    Uri url = task.getResult().getDownloadUrl();
                    UploadPdf uploadPdf = new UploadPdf("1","03341721711", url.toString(),"");
                    databaseReference.child(databaseReference.push().getKey()).setValue(uploadPdf);
                    misc.showToast("File Uploaded");
                    pd.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                misc.showToast("Error...");
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double p = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                pd.setMessage("Uploaded: "+(int)p+"");
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, RegisterAsActivity.class);
        startActivity(intent);
        finish();
    }
}
