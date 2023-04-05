package com.example.alerto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button login_btn;
    ProgressBar progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView register_user = findViewById(R.id.register_user);
        EditText login_email = findViewById(R.id.login_email);
        EditText login_password = findViewById(R.id.login_password);
        login_btn = findViewById(R.id.login_btn);
        progress = findViewById(R.id.progress);

        auth = FirebaseAuth.getInstance();

        register_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent register_first_page = new Intent(MainActivity.this,Register_first.class);
                startActivity(register_first_page);

            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_btn.setEnabled(false);
                String email = login_email.getText().toString();
                String password = login_password.getText().toString();


                if(TextUtils.isEmpty(email)){
                    login_email.setError("Enter Your Email");
                    login_email.requestFocus();
                }
                else if(TextUtils.isEmpty(password)){
                    login_password.setError("Enter Your Password");
                    login_password.requestFocus();
                }
                else{
                    progress.setVisibility(View.VISIBLE);
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                storeData(email);
                                progress.setVisibility(View.GONE);
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Login Failed, Try Again", Toast.LENGTH_SHORT).show();
                                login_btn.setEnabled(true);
                                progress.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void storeData(String email) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("User_Data",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        db.collection("user_detail")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Log.d(TAG, document.getId() + " => " + document.getData());
                                if(document !=null){
                                    editor.putString("name",(String)document.get("name"));
                                    editor.putString("email",(String)document.get("email"));
                                    editor.putString("phoneNo",(String)document.get("phoneNo"));
                                    editor.putString("dob",(String)document.get("dob"));
                                    editor.putString("gender",(String) document.get("gender"));
                                    editor.putString("bloodGrp",(String)document.get("bloodGrp"));
                                    editor.putString("sos1",(String) document.get("sos1"));
                                    editor.putString("sos2", (String) document.get("sos2"));
                                    editor.putString("Id",(String) document.getId());
                                    editor.putBoolean("Login",true);
                                    editor.apply();

                                    login_btn.setEnabled(true);
                                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, user_home.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }

                            }
                        } else {
                            //Log.w(TAG, "Error getting documents.", task.getException());
                            login_btn.setEnabled(true);
                        }
                    }
                });

    }
}