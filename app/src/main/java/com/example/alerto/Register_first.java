package com.example.alerto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.Calendar;
import java.util.regex.Pattern;


public class Register_first extends AppCompatActivity {
    String name, email, password, confirm_password, dob, gender, bloodGrp="none", sos1, sos2;
    RadioButton rb_btn_gender_selected;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_first);

        EditText DOB_EditText = findViewById(R.id.DOB_EditText);
        Button btn_back = findViewById(R.id.btn_back);
        Button sign_up_proceed = findViewById(R.id.sign_up_proceed);
        RadioButton rb_a_pos = findViewById(R.id.rb_a_pos);
        RadioButton rb_a_neg = findViewById(R.id.rb_a_neg);
        RadioButton rb_b_pos = findViewById(R.id.rb_b_pos);
        RadioButton rb_b_neg = findViewById(R.id.rb_b_neg);
        RadioButton rb_ab_pos = findViewById(R.id.rb_ab_pos);
        RadioButton rb_ab_neg = findViewById(R.id.rb_ab_neg);
        RadioButton rb_o_pos = findViewById(R.id.rb_o_pos);
        RadioButton rb_o_neg = findViewById(R.id.rb_o_neg);
        RadioGroup rb_grp_gender = findViewById(R.id.rb_gender);

        rb_grp_gender.clearCheck();

        EditText sign_up_name = findViewById(R.id.sign_up_name);
        EditText sign_up_email = findViewById(R.id.sign_up_email);
        EditText sign_up_password = findViewById(R.id.sign_up_password);
        EditText sign_up_confirm_password = findViewById(R.id.sign_up_confirm_password);
        EditText sos_no_1 = findViewById(R.id.sos_no_1);
        EditText sos_no_2 = findViewById(R.id.sos_no_2);

        auth = FirebaseAuth.getInstance();


        Pattern pattern = Pattern.compile("^(\\+\\d{1,3}[- ]?)?\\d{10}$");

        sign_up_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int selectedGenderId = rb_grp_gender.getCheckedRadioButtonId();
                rb_btn_gender_selected = findViewById(selectedGenderId);


                name = sign_up_name.getText().toString();
                email = sign_up_email.getText().toString();
                password = sign_up_password.getText().toString();
                confirm_password = sign_up_confirm_password.getText().toString();
                dob = DOB_EditText.getText().toString();
                sos1 = sos_no_1.getText().toString();
                sos2 = sos_no_2.getText().toString();

                if(TextUtils.isEmpty(name)){
                    sign_up_name.setError("Please Enter Your Name");
                    sign_up_name.requestFocus();
                }
                else if(TextUtils.isEmpty(email)){
                    sign_up_email.setError("Please Enter Your Email");
                    sign_up_email.requestFocus();
                }
                else if(TextUtils.isEmpty(password)){
                    sign_up_password.setError("Please Enter Your Password");
                    sign_up_password.requestFocus();
                }
                else if(TextUtils.isEmpty(confirm_password)){
                    sign_up_confirm_password.setError("Please Enter Your Password to Confirm");
                    sign_up_confirm_password.requestFocus();
                }
                else if(TextUtils.equals(password, confirm_password)==false){
                    sign_up_confirm_password.setError("Password is not matching");
                    sign_up_confirm_password.requestFocus();
                }
                else if(TextUtils.isEmpty(dob)){
                    DOB_EditText.setError("Select Your DOB");
                }
                else {
                    if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                        sign_up_email.setError("Enter Valid Email");
                        sign_up_email.requestFocus();
                    }
                    else if(password.length()<6){
                        sign_up_password.setError("Password length should be greater than 5");
                        sign_up_password.requestFocus();
                    }
                    else if(!password.matches( "^(?=.*[0-9])(?=.*[a-z])(?=.*[!@#$%^&*+=?-]).{8,15}$")){
                        sign_up_password.setError("Password need to be strong");
                        sign_up_password.requestFocus();
                    } else if (!pattern.matcher(sos1).matches() || sos1 == null || sos1.isEmpty()) {
                        sos_no_1.setError("Enter Valid Phone Number");
                    } else if (!pattern.matcher(sos2).matches() || sos2 == null || sos2.isEmpty()) {
                        sos_no_2.setError("Enter Valid Phone Number");
                    } else{
                        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener( new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                try{
                                    if(task.getResult().getSignInMethods().isEmpty()==false){
                                        sign_up_email.setError("Email is already registered");
                                        sign_up_email.requestFocus();
                                    }
                                    else{
                                        try{
                                            gender = rb_btn_gender_selected.getText().toString();

                                            if(bloodGrp.equals("none")){
                                                Toast.makeText(Register_first.this, "Please Select Your Blood Group", Toast.LENGTH_SHORT).show();
                                            } else if (gender.equals("") || gender == null || gender.isEmpty()) {
                                                Toast.makeText(Register_first.this, "Please Select Your Gender", Toast.LENGTH_SHORT).show();

                                            } else{
                                                Intent intent = new Intent(Register_first.this, phn_num.class);
                                                intent.putExtra("name", name);
                                                intent.putExtra("email", email);
                                                intent.putExtra("password", password);
                                                intent.putExtra("dob", dob);
                                                intent.putExtra("bloodGrp", bloodGrp);
                                                intent.putExtra("gender",gender);
                                                intent.putExtra("sos1",sos1);
                                                intent.putExtra("sos2",sos2);
                                                startActivity(intent);
                                            }

                                        }
                                        catch (Exception e){
                                            Toast.makeText(Register_first.this, "Select Your Gender", Toast.LENGTH_SHORT).show();
                                        }


                                    }
                                }
                                catch(Exception e){
                                    Toast.makeText(Register_first.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }


                }

            }
        });


        //back to previous activity
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Date Picker

        DOB_EditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calender = Calendar.getInstance();
                int day = calender.get(Calendar.DAY_OF_MONTH);
                int month = calender.get(Calendar.MONTH);
                int year = calender.get(Calendar.YEAR);

                //Date picker Dialog
                DatePickerDialog picker =new DatePickerDialog(Register_first.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        DOB_EditText.setText(i2+"/"+(i1+1)+"/"+i);
                    }
                },year,month,day);
                picker.show();
            }
        });

        //blood_grp_radio_btn
        rb_a_pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bloodGrp = rb_a_pos.getText().toString();
                rb_a_pos.setChecked(true);
                rb_a_neg.setChecked(false);
                rb_b_pos.setChecked(false);
                rb_b_neg.setChecked(false);
                rb_ab_pos.setChecked(false);
                rb_ab_neg.setChecked(false);
                rb_o_pos.setChecked(false);
                rb_o_neg.setChecked(false);
            }
        });
        rb_a_neg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bloodGrp = rb_a_neg.getText().toString();
                rb_a_pos.setChecked(false);
                rb_a_neg.setChecked(true);
                rb_b_pos.setChecked(false);
                rb_b_neg.setChecked(false);
                rb_ab_pos.setChecked(false);
                rb_ab_neg.setChecked(false);
                rb_o_pos.setChecked(false);
                rb_o_neg.setChecked(false);
            }
        });
        rb_b_pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bloodGrp = rb_b_pos.getText().toString();
                rb_a_pos.setChecked(false);
                rb_a_neg.setChecked(false);
                rb_b_pos.setChecked(true);
                rb_b_neg.setChecked(false);
                rb_ab_pos.setChecked(false);
                rb_ab_neg.setChecked(false);
                rb_o_pos.setChecked(false);
                rb_o_neg.setChecked(false);
            }
        });
        rb_b_neg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bloodGrp = rb_b_neg.getText().toString();
                rb_a_pos.setChecked(false);
                rb_a_neg.setChecked(false);
                rb_b_pos.setChecked(false);
                rb_b_neg.setChecked(true);
                rb_ab_pos.setChecked(false);
                rb_ab_neg.setChecked(false);
                rb_o_pos.setChecked(false);
                rb_o_neg.setChecked(false);
            }
        });
        rb_ab_pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bloodGrp = rb_ab_pos.getText().toString();
                rb_a_pos.setChecked(false);
                rb_a_neg.setChecked(false);
                rb_b_pos.setChecked(false);
                rb_b_neg.setChecked(false);
                rb_ab_pos.setChecked(true);
                rb_ab_neg.setChecked(false);
                rb_o_pos.setChecked(false);
                rb_o_neg.setChecked(false);
            }
        });
        rb_ab_neg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bloodGrp = rb_ab_neg.getText().toString();
                rb_a_pos.setChecked(false);
                rb_a_neg.setChecked(false);
                rb_b_pos.setChecked(false);
                rb_b_neg.setChecked(false);
                rb_ab_pos.setChecked(false);
                rb_ab_neg.setChecked(true);
                rb_o_pos.setChecked(false);
                rb_o_neg.setChecked(false);
            }
        });
        rb_o_pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bloodGrp = rb_o_pos.getText().toString();
                rb_a_pos.setChecked(false);
                rb_a_neg.setChecked(false);
                rb_b_pos.setChecked(false);
                rb_b_neg.setChecked(false);
                rb_ab_pos.setChecked(false);
                rb_ab_neg.setChecked(false);
                rb_o_pos.setChecked(true);
                rb_o_neg.setChecked(false);
            }
        });
        rb_o_neg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bloodGrp = rb_o_neg.getText().toString();
                rb_a_pos.setChecked(false);
                rb_a_neg.setChecked(false);
                rb_b_pos.setChecked(false);
                rb_b_neg.setChecked(false);
                rb_ab_pos.setChecked(false);
                rb_ab_neg.setChecked(false);
                rb_o_pos.setChecked(false);
                rb_o_neg.setChecked(true);
            }
        });



    }
}