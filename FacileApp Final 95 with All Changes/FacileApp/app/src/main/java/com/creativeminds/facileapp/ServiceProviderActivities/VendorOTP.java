package com.creativeminds.facileapp.ServiceProviderActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.creativeminds.facileapp.LoginActivity;
import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.UploadPdf;
import com.creativeminds.facileapp.R;
import com.creativeminds.facileapp.RegisterAsActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class VendorOTP extends AppCompatActivity implements View.OnClickListener {

    private EditText customer_otp,phone;
    private Button register,reSend, start,portfolio;
    private File imageFile = null;
    private String name, email, cnic, password,image, gender, lat, lon, address,city, department;

    private final int REQUEST_CODE = 1;
    private File uploadFile = null;
    private static String resultPath = null;
    Uri selectedImageUri;
    private String pdfFileUrl, ID;

    StorageReference storageReference;
    DatabaseReference databaseReference;

    Misc misc;
    private static final String TAG = "PhoneAuthActivity";
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    private int fileUploadedCode;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_otp);
        setTitle("Register As Vendor");

        misc = new Misc(this);
        phone = findViewById(R.id.reg_phone);
        customer_otp = findViewById(R.id.cusOTP);

        // getting data from previous class
        Intent intent = getIntent();
        image = intent.getStringExtra("image");
        imageFile = new File(image);
        name = intent.getStringExtra("name");
        email = intent.getStringExtra("email");
        cnic = intent.getStringExtra("cnic");
        password = intent.getStringExtra("password");
        gender = intent.getStringExtra("gender");
        address = intent.getStringExtra("address");
        lat = intent.getStringExtra("lat");
        lon = intent.getStringExtra("lon");
        city = intent.getStringExtra("city");
        department = intent.getStringExtra("department");

        register = findViewById(R.id.register_button);
        reSend = findViewById(R.id.reSendCode);
        start = findViewById(R.id.startCode);
        portfolio = findViewById(R.id.port_file);

        portfolio.setOnClickListener(this);
        reSend.setOnClickListener(this);
        start.setOnClickListener(this);
        register.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                Log.d(TAG, "onVerificationCompleted:" + credential);
                mVerificationInProgress = false;
                updateUI(STATE_VERIFY_SUCCESS, credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    phone.setError("Invalid phone number.");

                } else if (e instanceof FirebaseTooManyRequestsException) {

                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                }
                updateUI(STATE_VERIFY_FAILED);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
                updateUI(STATE_CODE_SENT);
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(phone.getText().toString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    private void startPhoneNumberVerification(String phoneNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            StorageReference reference = storageReference.child("uploads/"+System.currentTimeMillis()+".pdf");
                            reference.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful()){
                                        Uri url = task.getResult().getDownloadUrl();
                                        String currentUser  = mAuth.getCurrentUser().getUid();
                                        misc.showToast(currentUser);
                                        UploadPdf uploadPdf = new UploadPdf(currentUser,phone.getText().toString(), url.toString());
                                        databaseReference.child("Users").child(currentUser).setValue(uploadPdf);
                                        misc.showToast("File Uploaded");

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
                                   misc.showToast("Uploaded: "+(int)p+"%");
                                }
                            });

                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                customer_otp.setError("Invalid code...");
                            }
                        }
                    }
                });
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {

            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                enableViews(register, reSend, phone,customer_otp);
                disableViews(start);
                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                enableViews(start,register, reSend, phone,customer_otp);
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
                registerVendor();

                break;
        }
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = phone.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            phone.setError("Invalid phone number.");
            return false;
        }
        return true;
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    private boolean validate(){

        String user_phone = phone.getText().toString().trim();

        if(user_phone.length() < 12) {
            misc.showToast("Invalid Phone Number");
            phone.setError("Invalid Phone Number");
            return false;
        }
        if(user_phone.isEmpty()) {
            misc.showToast("Invalid Phone Number");
            phone.setError("Invalid Phone Number");
            return false;
        }

        return true;
    }

    private void registerVendor(){
        if(validate()) {
            registerWithImage();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.port_file:
                selectPDFFile();
                break;
            case R.id.startCode:
                startPhoneNumberVerification(phone.getText().toString());
                break;
            case R.id.register_button:
                String code = customer_otp.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    customer_otp.setError("Cannot be empty.");
                    return;
                }
                verifyPhoneNumberWithCode(mVerificationId, code);
                registerVendor();
                break;
            case R.id.reSendCode:
                resendVerificationCode(phone.getText().toString(), mResendToken);
                break;
        }
    }

    private void registerWithImage(){

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Registration in process...");
        pd.setCancelable(false);
        pd.show();

        Ion.with(this)
                .load(misc.ROOT_PATH+"create_user")
                .setMultipartFile("user_image", imageFile)
                .setMultipartParameter("user_name", name)
                .setMultipartParameter("user_email", email)
                .setMultipartParameter("user_phone", phone.getText().toString().trim())
                .setMultipartParameter("user_cnic", cnic)
                .setMultipartParameter("user_password", password)
                .setMultipartParameter("user_address", address)
                .setMultipartParameter("user_city", city)
                .setMultipartParameter("user_role", "vendor")
                .setMultipartParameter("user_gender", gender)
                .setMultipartParameter("user_lat", String.valueOf(lat))
                .setMultipartParameter("user_lon", String.valueOf(lon))
                .setMultipartParameter("departments", department)
                .setMultipartParameter("charges", "50")
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if(e != null){
                            misc.showToast("Internet Connection Problem");
                            pd.dismiss();
                            return;
                        }
                        String response = result.getResult();
                        if(response.isEmpty()){
                            pd.dismiss();
                            misc.showToast("Email, Phone, or CNIC already exists");
                            return;
                        }
                        else{
                            pd.dismiss();
                            misc.showToast(result.getResult());
                            Intent intent = new Intent(VendorOTP.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                });
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
            portfolio.setText(selectedImageUri.toString());
        }
    }

    private void uploadPDF(Uri data) {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Uploading PDF...");
        pd.show();

        StorageReference reference = storageReference.child("uploads/"+System.currentTimeMillis()+".pdf");
        reference.putFile(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    Uri url = task.getResult().getDownloadUrl();
                    String currentUser  = mAuth.getCurrentUser().getUid();
                   // UploadPdf uploadPdf = new UploadPdf(mAuth.getCurrentUser().getUid(), url.toString());
                    databaseReference.child("users").child(currentUser).setValue(url.toString());
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
                pd.setMessage("Uploaded: "+(int)p+"%");
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
