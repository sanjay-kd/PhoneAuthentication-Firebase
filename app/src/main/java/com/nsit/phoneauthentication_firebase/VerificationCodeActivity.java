package com.nsit.phoneauthentication_firebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerificationCodeActivity extends AppCompatActivity {

    private EditText otpEditText;
    private Button verifyPhoneNumberBtn;
    private String phoneNumber;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationID;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private void createProgressDailog(){
        progressDialog = new ProgressDialog(VerificationCodeActivity.this);
        progressDialog.setTitle("Verification in Progress");
        progressDialog.setMessage("Please wait while we verify your mobile number");
        progressDialog.setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);
        createProgressDailog();

        phoneNumber = getIntent().getStringExtra("phoneNumber");
        mAuth = FirebaseAuth.getInstance();

        otpEditText = findViewById(R.id.otpEditText);
        verifyPhoneNumberBtn = findViewById(R.id.verifyPhoneNumberBtn);
        progressDialog.show();
        // Step-2) Callback method object creation
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // Called when the OTP ise sent on the user mobile number
            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationID = s;
            }

            // Called when Verification is completed automatically, this method calls signInWithPhoneAuthCredentials func with phoneAuthCredentials as paramter
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                System.out.println("OnVerification Credentials : "+phoneAuthCredential.getSmsCode());
                otpEditText.setText(phoneAuthCredential.getSmsCode());
                signInWithPhoneAuthCredentials(phoneAuthCredential);
            }

            // Called if verfication is failed due to some reason.
            @Override
            public void onVerificationFailed(FirebaseException e) {
                System.out.println("Verification Failed : "+e);
            }
        };

        // Step-3) (OPTIONAL) this is done if onVerificationCompleted method is not called means user need to do insert OTP in editText and we need to create a PhoneAuthCredentials oject with the verificationID and OTP sent to user.
        verifyPhoneNumberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String OTP = otpEditText.getText().toString();
                System.out.println("OTP is :"+OTP+"\n"+"VerificationID is : "+verificationID);
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationID,OTP);
                signInWithPhoneAuthCredentials(phoneAuthCredential);
            }
        });

        // Step-1) Send A verification cod eon user mbile number
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91-"+phoneNumber,   // Phone number to send otp
                60,                 // timeout after which otp need to be send again
                TimeUnit.SECONDS,      // unit of timeout
                this,           // context
                mCallbacks              // CallBack methods object
        );

    }

    // Step-4) Create a function which signInUser with PhoneAuth Credentials and direct user to homeScreen of The APP.
    private void signInWithPhoneAuthCredentials(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    System.out.println("SignUP successfull");
                    progressDialog.dismiss();
                    Intent intent = new Intent(VerificationCodeActivity.this,MainScreen.class);
                    startActivity(intent);
                }
                else {
                    // Sign in failed, display a message and update the UI
                    Log.w("GotError", "signInWithCredential:failure", task.getException());
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        System.out.println("Code is invalid");
                    }
                }
            }
        });
    }

}
