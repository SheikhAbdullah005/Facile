package com.creativeminds.facileapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.creativeminds.facileapp.Misc.Misc;
import com.creativeminds.facileapp.Models.UploadPdf;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.StorageReference;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerOTP extends AppCompatActivity implements View.OnClickListener{

    private EditText customer_otp,phone;
    private Button register,reSend, start;
    private File imageFile = null;
    private String name, email, cnic, password,image,  phoneFinal, gender;

    Misc misc;
    private static final String TAG = "PhoneAuthActivity";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    // [START declare_auth]
    private FirebaseAuth mAuth;

    StorageReference storageReference;
    DatabaseReference databaseReference;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_otp);
        setTitle("Register As Customer");


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

        register = findViewById(R.id.register_button);
        reSend = findViewById(R.id.reSendCode);
        start = findViewById(R.id.startCode);

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
                            String currentUser  = mAuth.getCurrentUser().getUid();
                           // misc.showToast(currentUser);
                            UploadPdf uploadPdf = new UploadPdf(currentUser, phone.getText().toString(),"No PDF for Customer",email);
                            databaseReference.child("Users").child(currentUser).setValue(uploadPdf);
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
                registerCustomer();

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

        if(user_phone.length() != 11) {
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

    private void registerCustomer(){
        if(validate()) {
            registerWithImage();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                registerCustomer();
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
                .load(misc.ROOT_PATH + "create_user")
                .setMultipartFile("user_image", imageFile)
                .setMultipartParameter("user_name", name)
                .setMultipartParameter("user_email", email)
                .setMultipartParameter("user_phone", phone.getText().toString().trim())
                .setMultipartParameter("user_cnic", cnic)
                .setMultipartParameter("user_password", password)
                .setMultipartParameter("user_role", "customer")
                .setMultipartParameter("user_gender", gender)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        if (e != null) {
                            pd.dismiss();
                            misc.showToast("Please check your connection");
                            pd.dismiss();
                            return;
                        }
                        String response = result.getResult();
                        if (response.isEmpty()) {
                            pd.dismiss();
                            misc.showToast("Email, Phone, or CNIC already exists");
                            return;
                        } else {
                            pd.dismiss();
                            misc.showToast(result.getResult());
                            Intent intent = new Intent(CustomerOTP.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
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
