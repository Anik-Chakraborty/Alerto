package com.example.alerto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class user_home extends AppCompatActivity implements LocationListener {

    public static final  String Message_KEY = "message_key";
    Location mLastLocation;
    SwitchCompat detectAccidentSwitch, detectSpeedSwitch;
    TextView check,accident_dialog_timer_txt,accident_dialog_level_txt, respondTimeLeft;
    LocationManager locationManager;
    ConstraintLayout accident_dialog, speed_dialog;
    LinearLayout respondDialog;
    Button accident_dialog_help_btn;
    boolean checkSpeed=false;

    private BroadcastReceiver accReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

                    String Message = intent.getStringExtra(Message_KEY);

                    SharedPreferences sharedPreferences = getSharedPreferences("View_Visible", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    if(Message.equals("Low")){
                        Log.i("Accident Level", "Low");
                        accident_dialog_level_txt.setText("We Detected A Low Crash");
                        setView();
                    }
                    else if(Message.equals("Medium")){
                        Log.i("Accident Level", "Medium");
                        accident_dialog_level_txt.setText("We Detected A Medium Crash");
                        setView();
                    }
                    else if(Message.equals("High")){
                        Log.i("Accident Level", "High");
                        accident_dialog_level_txt.setText("We Detected A High Crash");
                        setView();
                    }
                    else if(Message.contains("Time1")&& accident_dialog.getVisibility() == View.VISIBLE){
                        Log.i("Time", "Time1");
                        String Time = Message.substring(6);
                        accident_dialog_timer_txt.setText("Calling for help in "+Time+" seconds");
                    }
                    else if (Message.equals("TimeSecondFirst")) {
                        setView();
                    }
                    else if(Message.contains("Time2")){
                        Log.i("Time", "Time2");
                        String Time = Message.substring(6);
                        respondTimeLeft.setText("\uD83D\uDD51 Sending Respond to Emergency Service in "+Time+" .");
                    }
                    else if(Message.equals("TimeEmergencyComplete")){
                        Log.i("Time", "TimeEmergencyComplete");
                        respondTimeLeft.setText("✓ Respond Sent To Emergency Contacts");
                    }
                    else if(Message.equals("TimeSOSComplete") && accident_dialog.getVisibility() == View.VISIBLE){
                        Log.i("Time", "TimeSOSComplete");
                        setView();
                    }

        }
    };


    private void iAmOkay() {
        Toast.makeText(user_home.this, "okay", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent("Activity_Message");
        intent.putExtra(user_home.Message_KEY, "USER_FINE");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

    }

    private void needHelp() {
        final String[] options = {"Ambulance ", "Fire Service", " Police"};

        // Create a boolean array to track the checked state of each option
        final boolean[] checkedOptions = {false, false, false};

        // Create the AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(user_home.this);

        // Set the title and message of the dialog
        builder.setTitle("Please Select At least One Service: ");

        // Add the checkboxes to the dialog
        builder.setMultiChoiceItems(options, checkedOptions, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedOptions[which] = isChecked;
                boolean isAnyOptionChecked = false;
                for (boolean checkedOption : checkedOptions) {
                    if (checkedOption) {
                        isAnyOptionChecked = true;
                        break;
                    }
                }
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(isAnyOptionChecked);
            }
        });

        // Add an "OK" button to the dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String text = "Checked";
                // Handle the "OK" button click
                // You can access the checked state of each option using the checkedOptions array
                // For example:
                if (checkedOptions[0]) {
                    // Option 1 is checked
                    text += "Ambulance";

                }
                if (checkedOptions[1]) {
                    // Option 2 is checked
                    text += "Fire Service";
                }
                if (checkedOptions[2]) {
                    // Option 3 is checked
                    text += "Police";
                }
                Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Disable the "OK" button by default
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(accReceiver,new IntentFilter(G_Force_Counter.SERVICE_MESSAGE));

        setView();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{android.Manifest.permission.SEND_SMS}, 1);
            }
        }

        try{

            String msg = getIntent().getStringExtra("Msg");
            if(msg.equals("HELP")){
                needHelp();
            }
            else if(msg.equals("FINE")){
                iAmOkay();
            }
        }
        catch (Exception e){

        }
    }


    private void setView() {

        SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
        boolean accident_dialog_visibility_flag = sharedPreferences.getBoolean("accident_dialog", false);
        boolean respond_dialog_visibility_flag = sharedPreferences.getBoolean("respond_dialog",false);
        boolean respond_dialog_time_left_visibility_flag = sharedPreferences.getBoolean("respond_time_left",false);

        if(accident_dialog_visibility_flag){
            accident_dialog.setVisibility(View.VISIBLE);
        }
        else{
            accident_dialog.setVisibility(View.GONE);
        }

        if(respond_dialog_visibility_flag){
            respondDialog.setVisibility(View.VISIBLE);
        }
        else{
            respondDialog.setVisibility(View.GONE);
        }

        if(respond_dialog_time_left_visibility_flag){
            respondTimeLeft.setVisibility(View.VISIBLE);
        }
        else{
            respondTimeLeft.setVisibility(View.GONE);
        }



        Log.i("Set View", "Update View");

    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(accReceiver);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        accident_dialog = findViewById(R.id.accident_dialog);
        speed_dialog = findViewById(R.id.spped_dialog);
        detectAccidentSwitch = findViewById(R.id.detectAccident);
        detectSpeedSwitch = findViewById(R.id.detectSpeed);
        accident_dialog_level_txt = findViewById(R.id.accident_dialog_level_txt);
        accident_dialog_timer_txt = findViewById(R.id.accident_dialog_timer_txt);
        respondDialog = findViewById(R.id.respondDialog);
        respondTimeLeft = findViewById(R.id.respondTimeLeft);
        accident_dialog_help_btn = findViewById(R.id.accident_dialog_help_btn);
        check = findViewById(R.id.check);

        if(isMyServiceRunning(G_Force_Counter.class)){
            detectAccidentSwitch.setChecked(true);
        }
        else{
            detectAccidentSwitch.setChecked(false);
        }


        accident_dialog_help_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needHelp();
            }
        });

        detectAccidentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent gForceService = new Intent(user_home.this, G_Force_Counter.class);
                SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
                if(isChecked){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        editor.putBoolean("accident_detect_flag",true);
                        editor.apply();
                        startForegroundService(gForceService);
                    }
                    else{
                        startService(gForceService);
                    }

                }
                else{
                    boolean flagSensorOnChange = sharedPreferences.getBoolean("accident_detect_flag",true);
                    if(!flagSensorOnChange){
                        Toast.makeText(user_home.this, "First Respond For Detected Accident", Toast.LENGTH_SHORT).show();
                        detectAccidentSwitch.setChecked(true);
                    }
                    else{
                        stopService(gForceService);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("accident_dialog",false);
                        editor.putBoolean("respond_dialog",false);
                        editor.putBoolean("respond_time_left",false);
                        editor.apply();
                        setView();
                    }
                }
            }
        });

        detectSpeedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    checkSpeed = true;
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(user_home.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(user_home.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        // Request permission to access the location
                        ActivityCompat.requestPermissions(user_home.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

                    } else {

                        // Register for location updates
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, user_home.this);

                    }

                }
                else{
                    speed_dialog.setVisibility(View.GONE);
                }
            }
        });


    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    @Override
    protected void onPause() {
        super.onPause();
        if(checkSpeed){
            locationManager.removeUpdates( user_home.this);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
        boolean flagSensorOnChange = sharedPreferences.getBoolean("accident_detect_flag",true);


        if(flagSensorOnChange){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("accident_dialog",false);
            editor.putBoolean("respond_dialog",false);
            editor.putBoolean("respond_time_left",false);
            editor.apply();
        }
        setView();
    }

    //location

    @Override
    public void onLocationChanged(Location pCurrentLocation) {

        double speed = 0;
        if (this.mLastLocation != null)
            speed = Math.sqrt(
                    Math.pow(pCurrentLocation.getLongitude() - mLastLocation.getLongitude(), 2)
                            + Math.pow(pCurrentLocation.getLatitude() - mLastLocation.getLatitude(), 2)
            ) / (pCurrentLocation.getTime() - this.mLastLocation.getTime());
        //if there is speed from location
        if (pCurrentLocation.hasSpeed())
            //get location speed
            speed = pCurrentLocation.getSpeed() * 3.6;
        this.mLastLocation = pCurrentLocation;


        if(speed > 10){
            speed_dialog.setVisibility(View.GONE);
        }
        else{
            check.setText("Emergency Call"+" "+speed);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
