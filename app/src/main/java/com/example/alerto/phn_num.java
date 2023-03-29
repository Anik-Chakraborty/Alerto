package com.example.alerto;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;


import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;

public class phn_num extends AppCompatActivity {
    TextInputEditText mobile_no;
    Button phn_no_btn;
    CountryCodePicker ccp;
    String name, email, password, dob, bloodGrp, gender, sos1, sos2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phn_num);

        ccp = findViewById(R.id.ccp);
        phn_no_btn = findViewById(R.id.phn_no_btn);
        mobile_no = findViewById(R.id.mobile_no);

        name = getIntent().getStringExtra("name").toString();
        email = getIntent().getStringExtra("email").toString();
        password = getIntent().getStringExtra("password").toString();
        dob = getIntent().getStringExtra("dob").toString();
        bloodGrp = getIntent().getStringExtra("bloodGrp").toString();
        gender = getIntent().getStringExtra("gender").toString();
        sos1 = getIntent().getStringExtra("sos1").toString();
        sos2 = getIntent().getStringExtra("sos2").toString();

        ccp.registerCarrierNumberEditText(mobile_no);

        phn_no_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phn_no = mobile_no.getText().toString();


                if(TextUtils.isEmpty(phn_no) || phn_no.isEmpty() || phn_no ==null || phn_no.equals("") || phn_no.equals(" ")){
                    mobile_no.setError("Enter Valid Mobile No");
                    mobile_no.requestFocus();
                }
                else{
                    Intent intent = new Intent(phn_num.this, verify_otp.class);
                    intent.putExtra("name", name);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    intent.putExtra("dob", dob);
                    intent.putExtra("bloodGrp", bloodGrp);
                    intent.putExtra("gender",gender);
                    intent.putExtra("sos1",sos1);
                    intent.putExtra("sos2",sos2);
                    intent.putExtra("phn_no", ccp.getFullNumberWithPlus().replace(" "," "));
                    startActivity(intent);
                }
            }
        });
    }
}