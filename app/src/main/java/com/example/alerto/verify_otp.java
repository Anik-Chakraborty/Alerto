package com.example.alerto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class verify_otp extends AppCompatActivity {
    String name, email, password, dob, bloodGrp, phn_no, otpId;
    FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        TextInputEditText otp_editText = findViewById(R.id.otp_editText);
        Button btn_otp_verify= findViewById(R.id.btn_otp_verify);

        name = getIntent().getStringExtra("name").toString();
        email = getIntent().getStringExtra("email").toString();
        password = getIntent().getStringExtra("password").toString();
        dob = getIntent().getStringExtra("dob").toString();
        bloodGrp = getIntent().getStringExtra("bloodGrp").toString();
        phn_no = getIntent().getStringExtra("phn_no").toString();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toast.makeText(verify_otp.this, phn_no, Toast.LENGTH_SHORT).show();
        initiateOtp();

        btn_otp_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(otp_editText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(),"Blank Field cannot be processed", Toast.LENGTH_LONG).show();
                else if (otp_editText.getText().toString().length()!=6)
                    Toast.makeText(getApplicationContext(),"Invalid OTP",Toast.LENGTH_LONG).show();
                else{
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpId,otp_editText.getText().toString());
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

    }

    private void initiateOtp() {



        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                otpId = s;
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_LONG).show();

            }

        };

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phn_no)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)       // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //create account using email and password
                            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        mAuth.getCurrentUser().sendEmailVerification();
                                        Toast.makeText(verify_otp.this, "user created successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(verify_otp.this, "create user code error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            //Store user info in firebase firestore
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("email", email);
                            user.put("password", password);
                            user.put("dob", dob);
                            user.put("bloodGrp",bloodGrp);
                            user.put("phoneNo",phn_no);

                            db.collection("user_detail")
                                    .add(user)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(verify_otp.this, "User registration successful", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(verify_otp.this, "Error during upload data", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            Intent intent = new Intent(verify_otp.this, MainActivity.class);
                            startActivity(intent);


                        } else {
                            Toast.makeText(getApplicationContext(),"Sign in Code Error",Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }
}