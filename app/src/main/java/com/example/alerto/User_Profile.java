package com.example.alerto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class User_Profile extends AppCompatActivity {
    String sos1, sos2, name, email, dob, phn_no, bloodGrp, gender, doc_Id;
    EditText user_name, user_email, user_dob, user_phoneNo, user_gender, user_blood_grp, user_sos_1, user_sos_2;
    Button update_sos_btn;
    ImageView btn_back_profile;
    FirebaseFirestore db;
    ProgressBar progressBar;
    LinearLayout linearLayout3;
    ArrayList<String> Numbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Numbers = new ArrayList<>();

        user_name = findViewById(R.id.user_name);
        user_email = findViewById(R.id.user_email);
        user_dob = findViewById(R.id.user_dob);
        user_phoneNo = findViewById(R.id.user_phoneNo);
        user_gender = findViewById(R.id.user_gender);
        user_blood_grp = findViewById(R.id.user_blood_grp);
        user_sos_1 = findViewById(R.id.user_sos_1);
        user_sos_2 = findViewById(R.id.user_sos_2);
        btn_back_profile = findViewById(R.id.btn_back_profile);
        update_sos_btn = findViewById(R.id.update_sos_btn);
        progressBar = findViewById(R.id.progressBar);
        linearLayout3 = findViewById(R.id.linearLayout3);



        btn_back_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        user_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_name.setError("Name can't be changed");
            }
        });
        user_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_email.setError("Email can't be changed");
            }
        });
        user_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_dob.setError("DOB can't be changed");
            }
        });
        user_phoneNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_phoneNo.setError("Phone No can't be changed");
            }
        });
        user_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_gender.setError("Gender can't be changed");
            }
        });
        user_blood_grp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_blood_grp.setError("Blood Group can't be changed");
            }
        });

        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences("User_Data", MODE_PRIVATE);
        String Id = sharedPreferences.getString("Id", "");

        setViewText();

        if (user_name.getText().toString().equals("") || user_email.getText().toString().equals("") || user_gender.getText().toString().equals("") ||
                user_dob.getText().toString().equals("") || user_blood_grp.getText().toString().equals("") || user_phoneNo.getText().toString().equals("") || user_sos_1.getText().toString().equals("")
                || user_sos_2.getText().toString().equals("") || Id.equals("") || Id.isEmpty() || Id == null) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            storeData(user.getEmail());
        }

        update_sos_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pattern pattern = Pattern.compile("^(\\+\\d{1,3}[- ]?)?\\d{10}$");
                if (sos1.equals(user_sos_1.getText().toString()) && sos2.equals(user_sos_2.getText().toString())) {
                    Toast.makeText(User_Profile.this, "Same Data", Toast.LENGTH_SHORT).show();
                } else if (!pattern.matcher(user_sos_1.getText().toString()).matches() || user_sos_1.getText().toString() == null || user_sos_1.getText().toString().isEmpty()) {
                    user_sos_1.setError("Enter Valid Phone Number");
                } else if (!pattern.matcher(user_sos_2.getText().toString()).matches() || user_sos_2.getText().toString() == null || user_sos_2.getText().toString().isEmpty()) {
                    user_sos_2.setError("Enter Valid Phone Number");
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    linearLayout3.setEnabled(false);
                    linearLayout3.setClickable(false);
                    user_sos_1.setClickable(false);
                    user_sos_2.setClickable(false);
                    update_sos_btn.setClickable(false);

                    sos1 = user_sos_1.getText().toString();
                    sos2 = user_sos_2.getText().toString();

                    updateData();

                    sos1 = "";
                    sos2 = "";
                }
            }
        });


    }

    private void setViewText() {
        SharedPreferences sharedPreferences = getSharedPreferences("User_Data", MODE_PRIVATE);
        user_name.setText(sharedPreferences.getString("name", ""));
        user_email.setText(sharedPreferences.getString("email", ""));
        user_gender.setText(sharedPreferences.getString("gender", ""));
        user_dob.setText(sharedPreferences.getString("dob", ""));
        user_blood_grp.setText(sharedPreferences.getString("bloodGrp", ""));
        user_phoneNo.setText(sharedPreferences.getString("phoneNo", ""));
        user_sos_1.setText(sharedPreferences.getString("sos1", ""));
        user_sos_2.setText(sharedPreferences.getString("sos2", ""));

        doc_Id = sharedPreferences.getString("Id","");

        sos1 = user_sos_1.getText().toString();
        sos2 = user_sos_2.getText().toString();
        email = sharedPreferences.getString("email","");
        name = sharedPreferences.getString("name","");

        progressBar.setVisibility(View.GONE);
        linearLayout3.setEnabled(true);
        linearLayout3.setClickable(true);
        user_sos_1.setClickable(true);
        user_sos_2.setClickable(true);
        update_sos_btn.setClickable(true);
    }

    private void storeData(String email) {


        SharedPreferences sharedPreferences = getSharedPreferences("User_Data", MODE_PRIVATE);
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
                                if (document != null) {
                                    editor.putString("name", (String) document.get("name"));
                                    editor.putString("email", (String) document.get("email"));
                                    editor.putString("phoneNo", (String) document.get("phoneNo"));
                                    editor.putString("dob", (String) document.get("dob"));
                                    editor.putString("gender", (String) document.get("gender"));
                                    editor.putString("bloodGrp", (String) document.get("bloodGrp"));
                                    editor.putString("sos1", (String) document.get("sos1"));
                                    editor.putString("sos2", (String) document.get("sos2"));
                                    editor.putString("Id",(String) document.getId());
                                    editor.apply();

                                    setViewText();

                                }

                            }
                        } else {
                            //Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    private void updateData() {

        SharedPreferences sharedPreferences = getSharedPreferences("User_Data", MODE_PRIVATE);
        if(!sos1.equals(sharedPreferences.getString("sos1",""))){
            Numbers.add(sos1);
        }

        if(!sos2.equals(sharedPreferences.getString("sos2",""))){
            Numbers.add(sos2);
        }



        Map<String, Object> user = new HashMap<>();
        user.put("sos1", sos1);
        user.put("sos2", sos2);

        db.collection("user_detail").document(doc_Id).update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(User_Profile.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferences = getSharedPreferences("User_Data", MODE_PRIVATE);

                sendSMS();
                storeData(email);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(User_Profile.this, "Something went wrong, Please Check Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendSMS() {
        for(String no : Numbers){
            try {
                SmsManager smsManager = SmsManager.getDefault();
                Log.i("phn",no);
                smsManager.sendTextMessage(no, null, "You have been added as one of "+name+"'s SOS contacts to an accident detection and response app called Alerto.", null, null);
                smsManager.sendTextMessage(no, null, " As a result, you will be automatically notified of any accidents that "+name+" encounters.", null, null);
                //Toast.makeText(MainActivity.this, "!!THE MESSAGE IS SENT SUCCESSFULLY!!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.i("error",e.getMessage());
                //Toast.makeText(this, "!!FAILED TO SEND THE MESSAGE", Toast.LENGTH_SHORT).show();
            }
        }

        Numbers.clear();
    }

}