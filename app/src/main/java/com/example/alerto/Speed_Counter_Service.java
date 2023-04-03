package com.example.alerto;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class Speed_Counter_Service extends Service implements LocationListener{
    private static final String Foreground_NOTIFICATION_CHANNEL = "Foreground Service Running Notification";
    private static final int NOTIFICATION_ID_SPEED = 100;
    private static final String ACCIDENT_NOTIFICATION_CHANNEL = "Accident Detected Notification";
    LocationManager locationManager;
    private Location mLastLocation;
    private static final int NOTIFICATION_ACCIDENT_DETECT_ID = 101;
    private static final int REQ_CODE = 100;
    Notification notification;
    Vibrator vibrator;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Hi","Speed");
        Intent notifyUser = new Intent(this, user_home.class);
        PendingIntent actionForeground = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionForeground = PendingIntent.getActivity(
                    this, 10, notifyUser, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        } else {
            actionForeground = PendingIntent.getActivity(
                    this, 10, notifyUser, PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notificationForeground = new Notification.Builder(this, Foreground_NOTIFICATION_CHANNEL)
                    .setSmallIcon(R.drawable.splash_logo)
                    .setContentText("Service For Over Speed Detection is running")
                    .setSubText("Service Running")
                    .setChannelId(Foreground_NOTIFICATION_CHANNEL)
                    .setContentIntent(actionForeground)
                    .build();

            nm.createNotificationChannel(new NotificationChannel(Foreground_NOTIFICATION_CHANNEL, "Foreground Service Running", NotificationManager.IMPORTANCE_LOW));

            startForeground(NOTIFICATION_ID_SPEED, notificationForeground);
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                registerLocationManager();
            }
        });

        thread.start();


        return Service.START_STICKY;


    }

    private void registerLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager !=null){
            locationManager.removeUpdates((LocationListener) this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location pCurrentLocation) {
        double speed = 0;
        if (this.mLastLocation != null){
            speed = Math.sqrt(
                    Math.pow(pCurrentLocation.getLongitude() - mLastLocation.getLongitude(), 2)
                            + Math.pow(pCurrentLocation.getLatitude() - mLastLocation.getLatitude(), 2)
            ) / (pCurrentLocation.getTime() - this.mLastLocation.getTime());
        }

        //if there is speed from location
        if (pCurrentLocation.hasSpeed()){
            //get location speed
            speed = pCurrentLocation.getSpeed() * 3.6;
        }

        this.mLastLocation = pCurrentLocation;

        SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
        boolean flagSensorOnChange = sharedPreferences.getBoolean("accident_detect_flag",true);


        if(speed > 10 && flagSensorOnChange){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("speed_dialog",true);
            editor.apply();

            Intent intent = new Intent(G_Force_Counter.SERVICE_MESSAGE);
            intent.putExtra("OverSpeed ",speed);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            vibarte();

            createNotification(speed);
        }
    }

    private void vibarte() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {0, 2000,10,2000};
        if(vibrator.hasVibrator()){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                vibrator.vibrate(VibrationEffect.createWaveform(pattern,-1));
            }
            else{

                vibrator.vibrate(pattern,-1);
            }
        }
    }

    private void createNotification(double speed) {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.speed_icon, null);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap largeIcon = bitmapDrawable.getBitmap();

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent iNotify = new Intent(getApplicationContext(), user_home.class);
        iNotify.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pi = PendingIntent.getActivity(this, REQ_CODE, iNotify, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        }
        else{
            pi = PendingIntent.getActivity(this, REQ_CODE, iNotify, PendingIntent.FLAG_UPDATE_CURRENT );
        }

        //notification action
        Intent actionHurryIntent = new Intent(this, Receiver.class);
        actionHurryIntent.putExtra(user_home.Message_KEY, "Hurry");
        PendingIntent actionHurryPendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionHurryPendingIntent = PendingIntent.getBroadcast(
                    this, 0, actionHurryIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        }
        else{
            actionHurryPendingIntent = PendingIntent.getBroadcast(
                    this, 0, actionHurryIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        Notification.Action actionHurry = new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.speed_icon),"In A Hurry", actionHurryPendingIntent).build();

        Notification.Builder builder = new Notification.Builder(this)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.splash_logo)
                .setContentTitle("Speed : "+speed+" Km/h")
                .setSubText("Over Speed Detected")
                .setContentIntent(pi) // without pi for normal notification//
                .addAction(actionHurry)
                .setColor(Color.RED)
                .setOnlyAlertOnce(true)
                .setShowWhen(true)                             //same as NotificationCompat.   default value is 'false'
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setOngoing(false);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = builder.setChannelId(ACCIDENT_NOTIFICATION_CHANNEL).build();
            nm.createNotificationChannel(new NotificationChannel(ACCIDENT_NOTIFICATION_CHANNEL, "Accident Detected Channel", NotificationManager.IMPORTANCE_HIGH));
        }
        else {
            notification = builder.build();
        }
        nm.notify(NOTIFICATION_ACCIDENT_DETECT_ID, (Notification) notification);

    }
}


