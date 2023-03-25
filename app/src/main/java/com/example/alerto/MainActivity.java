package com.example.alerto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView register_user = findViewById(R.id.register_user);
        EditText login_email = findViewById(R.id.login_email);
        EditText login_password = findViewById(R.id.login_password);
        Button login_btn = findViewById(R.id.login_btn);

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
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, user_home.class);
                                startActivity(intent);
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Login Failed, Try Again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}