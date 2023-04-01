package com.example.alerto;

import static androidx.core.app.ActivityCompat.requestPermissions;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class G_Force_Counter extends Service implements SensorEventListener {
    private static final String ACCIDENT_NOTIFICATION_CHANNEL = "Accident Detected Notification";
    private static final String Foreground_NOTIFICATION_CHANNEL = "Foreground Service Running Notification";
    private static final int NOTIFICATION_ID = 100;
    private static final int NOTIFICATION_ACCIDENT_DETECT_ID = 101;
    ArrayList<String> SMS_Numbers = new ArrayList<String>();
    private static final int REQ_CODE = 100;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    public static final String SERVICE_MESSAGE = "Service_Message";
    ArrayList<String> message = new ArrayList<>();
    Sensor accelerometerSensor;
    LocationManager locationManager;
    SensorManager sensorManager;
    Notification notification;
    NotificationManager notificationManager;
    boolean flagSensorOnChange;
    Thread gForceThread;
    private static MediaPlayer mediaPlayer;
    CountDownTimer countDownTimer;
    Vibrator vibrator;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notifyUser = new Intent(this, user_home.class);
        PendingIntent actionForeground = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionForeground = PendingIntent.getActivity(
                    this, 0, notifyUser, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        } else {
            actionForeground = PendingIntent.getActivity(
                    this, 0, notifyUser, PendingIntent.FLAG_UPDATE_CURRENT
            );
        }

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notificationForeground = new Notification.Builder(this, Foreground_NOTIFICATION_CHANNEL)
                    .setSmallIcon(R.drawable.splash_logo)
                    .setContentText("Service For Accident Detection is running")
                    .setSubText("Service Running")
                    .setChannelId(Foreground_NOTIFICATION_CHANNEL)
                    .setContentIntent(actionForeground)
                    .build();

            nm.createNotificationChannel(new NotificationChannel(Foreground_NOTIFICATION_CHANNEL, "Foreground Service Running", NotificationManager.IMPORTANCE_HIGH));

            startForeground(NOTIFICATION_ID, notificationForeground);
        }


        gForceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                calGForce();
            }
        });
        gForceThread.start();
        return Service.START_STICKY;
    }

    private void calGForce() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL & SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            sensorManager.unregisterListener(this);
        } catch (Exception e) {
            Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
        }
        stopRingtone();


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float gForce = 0;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate the magnitude of the acceleration vector
            float accelerationMagnitude = (float) Math.sqrt(x * x + y * y + z * z);

            // Convert the acceleration magnitude to G-force
            gForce = accelerationMagnitude / SensorManager.GRAVITY_EARTH;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("View_Visible", MODE_PRIVATE);
        flagSensorOnChange = sharedPreferences.getBoolean("accident_detect_flag", true);


        if (gForce > 30 && flagSensorOnChange) {
            startRingtone();

            Log.i("G Force", String.valueOf(gForce));
            sensorManager.unregisterListener(this);
            flagSensorOnChange = false;
            gForce = 0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("accident_detect_flag", flagSensorOnChange);
            editor.putBoolean("accident_dialog", true);
            editor.putBoolean("speed_dialog", false);
            editor.putBoolean("respond_dialog", false);
            editor.putBoolean("respond_time_left", false);
            editor.putString("accidentLevel", "High");
            editor.apply();
            //local broadcast to display accident dialog in ui
            Intent intent = new Intent(SERVICE_MESSAGE);
            intent.putExtra(user_home.Message_KEY, "High");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            //Notification
            callCounterTimerForNotification("High");
        } else if (gForce > 20 && gForce <= 30 && flagSensorOnChange) {
            startRingtone();
            Log.i("G Force", String.valueOf(gForce));
            sensorManager.unregisterListener(this);
            flagSensorOnChange = false;
            gForce = 0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("accident_detect_flag", flagSensorOnChange);
            editor.putBoolean("accident_dialog", true);
            editor.putBoolean("speed_dialog", false);
            editor.putBoolean("respond_dialog", false);
            editor.putBoolean("respond_time_left", false);
            editor.putString("accidentLevel", "Medium");
            editor.apply();
            //local broadcast to display accident dialog in ui
            Intent intent = new Intent(SERVICE_MESSAGE);
            intent.putExtra(user_home.Message_KEY, "Medium");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            //Notification
            callCounterTimerForNotification("Medium");
        } else if (gForce > 5.5 && gForce <= 20 && flagSensorOnChange) {
            startRingtone();
            Log.i("G Force", String.valueOf(gForce));
            sensorManager.unregisterListener(this);
            flagSensorOnChange = false;
            gForce = 0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("accident_detect_flag", flagSensorOnChange);
            editor.putBoolean("accident_dialog", true);
            editor.putBoolean("speed_dialog", false);
            editor.putBoolean("respond_dialog", false);
            editor.putBoolean("respond_time_left", false);
            editor.putString("accidentLevel", "Low");
            editor.apply();
            //local broadcast to display accident dialog in ui
            Intent intent = new Intent(SERVICE_MESSAGE);
            intent.putExtra(user_home.Message_KEY, "Low");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(user_home.ACTIVITY_MESSAGE));
            //Notification
            callCounterTimerForNotification("Low");
        }

    }

    private void startRingtone() {
        int ringtoneResId = R.raw.alert_sound;
        Uri ringtoneUri = Uri.parse("android.resource://" + getPackageName() + "/" + ringtoneResId);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {0, 1000, 2000, 1000};
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {

                vibrator.vibrate(pattern, 0);
            }
        }

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, ringtoneUri);
        }
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mediaPlayer.setVolume(maxVolume, maxVolume);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }


    private void callCounterTimerForNotification(String levelAccident) {

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long timeLeft = millisUntilFinished;
                displayNotification((int) timeLeft / 1000, levelAccident);

                //local broadcast to update accident dialog timer
                Intent intent = new Intent(SERVICE_MESSAGE);
                intent.putExtra(user_home.Message_KEY, "Time1 " + timeLeft / 1000);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                long timeLeft = 0;
                displayNotification((int) timeLeft / 1000, levelAccident);
                notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);

                Intent intent = new Intent(SERVICE_MESSAGE);
                intent.putExtra(user_home.Message_KEY, "Time1 " + timeLeft / 1000);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                notifySOS(levelAccident);

            }
        }.start();
    }

    private void displayNotification(int timeLeft, String levelAccident) {

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //Convert Drawable to bitmap for largeIcon in notification
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.alert_icon, null);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap largeIcon = bitmapDrawable.getBitmap();


        Intent iNotify = new Intent(getApplicationContext(), user_home.class);
        iNotify.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pi = PendingIntent.getActivity(this, REQ_CODE, iNotify, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pi = PendingIntent.getActivity(this, REQ_CODE, iNotify, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle()
                .addLine("\uD83D\uDD51 Sending Help Request To SOS")
                .addLine("Contacts In" + " " + (timeLeft) + " seconds");

        //Notification action button intents

        Intent actionActivityIntentHELP = new Intent(this, Receiver.class);
        actionActivityIntentHELP.putExtra(user_home.Message_KEY, "HELP");
        PendingIntent actionHELPPending = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionHELPPending = PendingIntent.getBroadcast(
                    this, 0, actionActivityIntentHELP, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        } else {
            actionHELPPending = PendingIntent.getBroadcast(
                    this, 0, actionActivityIntentHELP, PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        Notification.Action actionHELP = new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.help), "Need Help", actionHELPPending).build();

        Intent actionActivityIntentFINE = new Intent(this, Receiver.class);
        actionActivityIntentFINE.putExtra(user_home.Message_KEY, "FINE");
        PendingIntent actionFINEPending = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionFINEPending = PendingIntent.getBroadcast(
                    this, 1, actionActivityIntentFINE, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        } else {
            actionFINEPending = PendingIntent.getBroadcast(
                    this, 1, actionActivityIntentFINE, PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        Notification.Action actionFINE = new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ok), "I Am Ok", actionFINEPending).build();


        Notification.Builder builder = new Notification.Builder(this)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.splash_logo)
                .setContentTitle("Detected " + levelAccident + " Level Accident")
                .setStyle(inboxStyle)
                .setSubText("Accident Detected")
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(pi) // without pi for normal notification//
                .addAction(actionHELP)
                .addAction(actionFINE)
                .setColor(Color.RED)
                .setOnlyAlertOnce(true)
                .setShowWhen(true)                             //same as NotificationCompat.   default value is 'false'
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = builder.setChannelId(ACCIDENT_NOTIFICATION_CHANNEL).build();
            notificationManager.createNotificationChannel(new NotificationChannel(ACCIDENT_NOTIFICATION_CHANNEL, "Accident Detected Channel", NotificationManager.IMPORTANCE_HIGH));
        } else {
            notification = builder.build();
        }
        notificationManager.notify(NOTIFICATION_ACCIDENT_DETECT_ID, (Notification) notification);

    }

    private void notifySOS(String levelAccident) {
        Log.i("Msg", "Respond Send SOS");

        //Send message to Sos
        if (sendSMS("SOS Respond", levelAccident)) {
            //push notification

            if (!levelAccident.equals("Low")) {
                SharedPreferences sharedPreferences = getSharedPreferences("View_Visible", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("accident_dialog", false);
                editor.putBoolean("speed_dialog", false);
                editor.putBoolean("respond_dialog", true);
                editor.putBoolean("respond_time_left", true);
                editor.apply();

                Intent intent = new Intent(SERVICE_MESSAGE);
                intent.putExtra(user_home.Message_KEY, "TimeSecondFirst");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                countDownTimer = new CountDownTimer(45000, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        long timeLeft = millisUntilFinished;
                        displayNotificationSOS((int) timeLeft / 1000, levelAccident);

                        Intent intent = new Intent(SERVICE_MESSAGE);
                        intent.putExtra(user_home.Message_KEY, "Time2 " + timeLeft / 1000);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    }

                    @Override
                    public void onFinish() {
                        long timeLeft = 0;
                        displayNotificationSOS((int) timeLeft / 1000, levelAccident);
                        notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);
                        notifyEmergency(levelAccident);

                        flagSensorOnChange = true;

                        SharedPreferences sharedPreferences = getSharedPreferences("View_Visible", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("respond_dialog", true);
                        editor.putBoolean("respond_time_left", true);
                        editor.putBoolean("accident_dialog", false);
                        editor.putBoolean("speed_dialog", false);
                        editor.putBoolean("accident_detect_flag", flagSensorOnChange);
                        editor.apply();

                        Intent intent = new Intent(SERVICE_MESSAGE);
                        intent.putExtra(user_home.Message_KEY, "TimeEmergencyComplete");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                        stopRingtone();
                        sensorManager.registerListener(G_Force_Counter.this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL & SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

                    }
                }.start();
            } else {
                displayNotificationSOS(0, levelAccident);
                Intent intent = new Intent(SERVICE_MESSAGE);
                intent.putExtra(user_home.Message_KEY, "TimeSOSComplete");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                flagSensorOnChange = true;
                SharedPreferences sharedPreferences = getSharedPreferences("View_Visible", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("accident_dialog", false);
                editor.putBoolean("respond_dialog", true);
                editor.putBoolean("respond_time_left", false);
                editor.putBoolean("speed_dialog", false);
                editor.putBoolean("accident_detect_flag", flagSensorOnChange);
                editor.apply();
                stopRingtone();
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL & SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
            }

        }

    }

    private void stopRingtone() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private void displayNotificationSOS(int timeLeft, String levelAccident) {

        Intent iNotify = new Intent(getApplicationContext(), user_home.class);
        iNotify.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pi = PendingIntent.getActivity(this, REQ_CODE, iNotify, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pi = PendingIntent.getActivity(this, REQ_CODE, iNotify, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle()
                .addLine("✓ Message Delivered To SOS Contacts");


        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.alert_icon, null);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap largeIcon = bitmapDrawable.getBitmap();

        //Notification action button intents


        Intent actionActivityIntentHELP = new Intent(this, Receiver.class);
        actionActivityIntentHELP.putExtra(user_home.Message_KEY, "HELP");
        PendingIntent actionHELPPending = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionHELPPending = PendingIntent.getBroadcast(
                    this, 0, actionActivityIntentHELP, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        } else {
            actionHELPPending = PendingIntent.getBroadcast(
                    this, 0, actionActivityIntentHELP, PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        Notification.Action actionHELP = new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.help), "Need Help", actionHELPPending).build();


        Intent actionActivityIntentFINE = new Intent(this, Receiver.class);
        actionActivityIntentFINE.putExtra(user_home.Message_KEY, "FINE");
        PendingIntent actionFINEPending = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionFINEPending = PendingIntent.getBroadcast(
                    this, 1, actionActivityIntentFINE, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        } else {
            actionFINEPending = PendingIntent.getBroadcast(
                    this, 1, actionActivityIntentFINE, PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        Notification.Action actionFINE = new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ok), "I Am Ok", actionFINEPending).build();


        Notification.Builder builder = new Notification.Builder(this)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.splash_logo)
                .setContentTitle("Detected " + levelAccident + " Level Accident")
                .setStyle(inboxStyle)
                .setSubText("Accident Detected")
                .setContentIntent(pi) // without pi for normal notification//
                .addAction(actionFINE)
                .addAction(actionHELP)
                .setColor(Color.RED)
                .setOnlyAlertOnce(true)
                .setShowWhen(true)                             //same as NotificationCompat.   default value is 'false'
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        if (levelAccident.equals("Low") || timeLeft == 0) {
            builder.setAutoCancel(true).setOngoing(false);
        } else {
            builder.setAutoCancel(false).setOngoing(true);
            inboxStyle.addLine("\uD83D\uDD51 Sending Help Request To Emergency")
                    .addLine("Service In" + " " + (timeLeft) + " seconds");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = builder.setChannelId(ACCIDENT_NOTIFICATION_CHANNEL).build();
            notificationManager.createNotificationChannel(new NotificationChannel(ACCIDENT_NOTIFICATION_CHANNEL, "Accident Detected Channel", NotificationManager.IMPORTANCE_HIGH));
        } else {
            notification = builder.build();
        }
        notificationManager.notify(NOTIFICATION_ACCIDENT_DETECT_ID, (Notification) notification);

    }


    private void notifyEmergency(String levelAccident) {

        Log.i("Msg", "Respond Send Emergency");

        if (sendSMS("Emergency Respond", levelAccident)) {
            displayNotificationEmergency(levelAccident);
        }
    }

    private void displayNotificationEmergency(String levelAccident) {

        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.alert_icon, null);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap largeIcon = bitmapDrawable.getBitmap();


        Intent iNotify = new Intent(getApplicationContext(), user_home.class);
        iNotify.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pi = PendingIntent.getActivity(this, REQ_CODE, iNotify, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pi = PendingIntent.getActivity(this, REQ_CODE, iNotify, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle()
                .addLine("✓ Message Delivered To SOS Contacts")
                .addLine("✓ Message Delivered To Emergency Contacts");

        Notification.Builder builder = new Notification.Builder(this)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.splash_logo)
                .setContentTitle("Detected " + levelAccident + " Level Accident")
                .setStyle(inboxStyle)
                .setSubText("Accident Detected")
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentIntent(pi) // without pi for normal notification//
                .setColor(Color.RED)
                .setOnlyAlertOnce(true)
                .setShowWhen(true)                             //same as NotificationCompat.   default value is 'false'
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = builder.setChannelId(ACCIDENT_NOTIFICATION_CHANNEL).build();
            notificationManager.createNotificationChannel(new NotificationChannel(ACCIDENT_NOTIFICATION_CHANNEL, "Accident Detected Channel", NotificationManager.IMPORTANCE_HIGH));
        } else {
            notification = builder.build();
        }

        notificationManager.notify(NOTIFICATION_ACCIDENT_DETECT_ID, (Notification) notification);

    }

    private boolean sendSMS(String SMS_Type, String accidentLevel) {

        //police -> 6292286766, Ambulance -> 8617722517, Fire Service -> 6292286766

        SmsManager smsManager = SmsManager.getDefault();

        String  name, gender, bloodGrp, age, sos1, sos2, dob;
        SharedPreferences sharedPreferences = getSharedPreferences("User_Data",MODE_PRIVATE);
        dob = sharedPreferences.getString("dob","");
        name = sharedPreferences.getString("name","");
        gender = sharedPreferences.getString("gender","");
        sos1 = sharedPreferences.getString("sos1","");
        sos2 = sharedPreferences.getString("sos2","");
        bloodGrp = sharedPreferences.getString("bloodGrp","");
        age = calculateAge(dob);


        if(SMS_Type.equals("USER_FINE_SOS")){
            message.add("Glad to inform you that "+name+" has responded and that Alerto has determined his/her status as fine.");
            SMS_Numbers.add(sos1);
            SMS_Numbers.add(sos2);

        } else if (SMS_Type.equals("USER_FINE_Medium")) {
            message.add("Glad to inform you that "+name+" has responded and that Alerto has determined his/her status as fine.");
            SMS_Numbers.add(sos1);
            SMS_Numbers.add(sos2);
            SMS_Numbers.add("8617722517"); // ambulance

        } else if(SMS_Type.equals("USER_FINE_High")){
            message.add("Glad to inform you that "+name+" has responded and that Alerto has determined his/her status as fine.");

            SMS_Numbers.add(sos1);
            SMS_Numbers.add(sos2);
            SMS_Numbers.add("8617722517"); // ambulance
            SMS_Numbers.add("6292286766"); // police
            SMS_Numbers.add("6292286766"); // fire service

        } else if (SMS_Type.equals("SOS Respond")) {
            message.add("Alerto, an Android Accident Detection and Response App, detected "+name+" encountered a "+accidentLevel+" level accident.");
            message.add("It may be a minor accident or an error in detection, but it is not negligible. If necessary, please reach out to the user as soon as possible.");
            if(accidentLevel.equals("Medium") || accidentLevel.equals("High")){
                message.add("If "+name+" does not respond within 60 seconds, life support or emergency support will be contacted.");
            } else if (accidentLevel.equals("Low")) {
                message.add("Life support or emergency support will not be contacted since it is a minor accident.");
            }

            SMS_Numbers.add(sos1);
            SMS_Numbers.add(sos2);

        } else if (SMS_Type.equals("Emergency Respond")) {
            message.add("Alerto, an Android Accident Detection and Response App, detected "+name+" encountered a "+accidentLevel+" level accident.");
            if(accidentLevel.equals("Medium")){
                message.add("Accident is a moderate one and medical support is needed.");
                SMS_Numbers.add("8617722517"); // ambulance
            } else if (accidentLevel.equals("High")) {
                message.add("Accident is a fatal one and immediate medical support is needed.");

                SMS_Numbers.add("8617722517"); // ambulance
                SMS_Numbers.add("6292286766"); // police
                SMS_Numbers.add("6292286766"); // fire service
            }

        } else if (SMS_Type.contains("Message sent to")) {
            message.add("User: "+name+" replied through Alerto that he/she needs help.");

            if(SMS_Type.contains("Ambulance")){
                SMS_Numbers.add("8617722517"); // ambulance
            }
            if(SMS_Type.contains("Fire Service")){
                SMS_Numbers.add("6292286766"); // fire service
            }
            if(SMS_Type.contains("Police")){
                SMS_Numbers.add("6292286766"); // police
            }

        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i("location","location");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    Log.i("location","location 1");
                    if (location != null) {
                        Log.i("location","location 2");
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String userLocation = " current location is: http://maps.google.com/maps?q=" + latitude + "," + longitude;

                        for(String no : SMS_Numbers){
                            try {
                                smsManager.sendTextMessage(no, null, name+userLocation, null, null);
                                //Toast.makeText(MainActivity.this, "!!THE MESSAGE IS SENT SUCCESSFULLY!!", Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        SMS_Numbers.clear();
                        message.clear();
                        //message.add(name+userLocation);
                    }
                    else{
                        Log.i("error","error");
                    }
                    locationManager.removeUpdates(this);
                    return;
                }
            });
        }

        if(SMS_Type.equals("Emergency Respond") || SMS_Type.equals("USER_FINE_Medium") || SMS_Type.equals("USER_FINE_High") || SMS_Type.contains("Message sent to")){
            Log.i("emergency","emergency");
            message.add(name +" profile \nGender: "+gender+"\nAge: "+age+"\nBlood Group: "+bloodGrp+"\nSOS Contacts: "+sos1+" , "+sos2);
        }

        for(String phoneNo : SMS_Numbers){
            for(String sms : message){
                try {
                    smsManager.sendTextMessage(phoneNo, null, sms, null, null);
                    //Toast.makeText(MainActivity.this, "!!THE MESSAGE IS SENT SUCCESSFULLY!!", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        return true;
    }


    private String calculateAge(String dobString) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        try {
            Date dob = format.parse(dobString);
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(dob);
            Calendar todayCalendar = Calendar.getInstance();
            int age = todayCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);
            if (todayCalendar.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return String.valueOf(age);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Error";
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String Message = intent.getStringExtra(user_home.Message_KEY);

            if(Message.equals("USER_FINE")){
                countDownTimer.cancel();
                notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);
                stopRingtone();
                Toast.makeText(context, "well", Toast.LENGTH_SHORT).show();
            }
            else if (Message.equals("USER_FINE_SOS")) {
                countDownTimer.cancel();
                notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);
                stopRingtone();
                sendSMS("USER_FINE_SOS","");
            }
            else if (Message.equals("USER_FINE_Medium")) {
                countDownTimer.cancel();
                notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);
                stopRingtone();
                sendSMS("USER_FINE_Medium","");
            }
            else if (Message.equals("USER_FINE_High")) {
                countDownTimer.cancel();
                notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);
                stopRingtone();
                sendSMS("USER_FINE_High","");
            }
            else if(Message.contains("Message sent to")) {
                countDownTimer.cancel();
                notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);
                stopRingtone();
                sendSMS(Message,"");
            }

            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
            sensorManager.registerListener(G_Force_Counter.this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL & SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        }
    };

}
