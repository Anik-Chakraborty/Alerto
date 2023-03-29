package com.example.alerto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;

public class splash_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences sharedPreferences = getSharedPreferences("User_Data",MODE_PRIVATE);

       // Intent home_activity =new Intent(splash_activity.this, MainActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                startActivity(home_activity);
                if(sharedPreferences.getBoolean("Login",false) && FirebaseAuth.getInstance().getCurrentUser() !=null){
                    Intent move  = new Intent(splash_activity.this, user_home.class);
                    startActivity(move);
                    finish();
                }
                else{
                    Intent move  = new Intent(splash_activity.this, MainActivity.class);
                    startActivity(move);
                    finish();
                }


            }
        }, 2000);
    }
}