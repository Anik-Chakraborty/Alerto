package com.example.alerto;

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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class G_Force_Counter extends Service implements SensorEventListener {
    private static final String ACCIDENT_NOTIFICATION_CHANNEL = "Accident Detected Notification";
    private static final String Foreground_NOTIFICATION_CHANNEL = "Foreground Service Running Notification";
    private static final int NOTIFICATION_ID = 100;
    private static final int NOTIFICATION_ACCIDENT_DETECT_ID = 101;
    private static final int REQ_CODE = 100;
    public static final String SERVICE_MESSAGE = "Service_Message";
    Sensor accelerometerSensor;
    SensorManager sensorManager;
    Notification notification;
    NotificationManager notificationManager;
    boolean flagSensorOnChange;
    Thread gForceThread;

    CountDownTimer countDownTimer;

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
                    this, 0, notifyUser, PendingIntent.FLAG_UPDATE_CURRENT  | PendingIntent.FLAG_MUTABLE
            );
        }
        else{
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
        try{
            sensorManager.unregisterListener(this);
        }catch (Exception e){
            Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
        }


    }

        @Override
    public void onSensorChanged(SensorEvent event) {
        float gForce=0;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate the magnitude of the acceleration vector
            float accelerationMagnitude = (float) Math.sqrt(x * x + y * y + z * z);

            // Convert the acceleration magnitude to G-force
            gForce= accelerationMagnitude / SensorManager.GRAVITY_EARTH;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
        flagSensorOnChange = sharedPreferences.getBoolean("accident_detect_flag",true);


        if(gForce>30 && flagSensorOnChange){
            Log.i("G Force", String.valueOf(gForce));
            sensorManager.unregisterListener(this);
            flagSensorOnChange = false;
            gForce =0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("accident_detect_flag",flagSensorOnChange);
            editor.putBoolean("accident_dialog",true);
            editor.putBoolean("respond_dialog",false);
            editor.putBoolean("respond_time_left",false);
            editor.putString("accidentLevel","High");
            editor.apply();
            //local broadcast to display accident dialog in ui
            Intent intent = new Intent(SERVICE_MESSAGE);
            intent.putExtra(user_home.Message_KEY, "High");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            //Notification
            callCounterTimerForNotification("High");
        }
        else if(gForce>20 && gForce<=30 && flagSensorOnChange){
            Log.i("G Force", String.valueOf(gForce));
            sensorManager.unregisterListener(this);
            flagSensorOnChange = false;
            gForce =0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("accident_detect_flag",flagSensorOnChange);
            editor.putBoolean("accident_dialog",true);
            editor.putBoolean("respond_dialog",false);
            editor.putBoolean("respond_time_left",false);
            editor.putString("accidentLevel","Medium");
            editor.apply();
            //local broadcast to display accident dialog in ui
            Intent intent = new Intent(SERVICE_MESSAGE);
            intent.putExtra(user_home.Message_KEY, "Medium");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            //Notification
            callCounterTimerForNotification("Medium");
        }
        else if(gForce> 5.5 && gForce<=20 && flagSensorOnChange){
            Log.i("G Force", String.valueOf(gForce));
            sensorManager.unregisterListener(this);
            flagSensorOnChange = false;
            gForce =0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("accident_detect_flag",flagSensorOnChange);
            editor.putBoolean("accident_dialog",true);
            editor.putBoolean("respond_dialog",false);
            editor.putBoolean("respond_time_left",false);
            editor.putString("accidentLevel","Low");
            editor.apply();
            //local broadcast to display accident dialog in ui
            Intent intent = new Intent(SERVICE_MESSAGE);
            intent.putExtra(user_home.Message_KEY, "Low");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            LocalBroadcastManager.getInstance(this).registerReceiver(receiver,new IntentFilter(user_home.ACTIVITY_MESSAGE));
            //Notification
            callCounterTimerForNotification("Low");
        }

    }


    private void callCounterTimerForNotification(String levelAccident) {

         countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long timeLeft = millisUntilFinished;
                displayNotification((int)timeLeft/1000, levelAccident);

                //local broadcast to update accident dialog timer
                Intent intent = new Intent(SERVICE_MESSAGE);
                intent.putExtra(user_home.Message_KEY, "Time1 "+timeLeft/1000);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                long timeLeft =0;
                displayNotification((int) timeLeft/1000, levelAccident);
                notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);

                Intent intent = new Intent(SERVICE_MESSAGE);
                intent.putExtra(user_home.Message_KEY, "Time1 "+timeLeft/1000);
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
        }
        else{
            pi = PendingIntent.getActivity(this, REQ_CODE, iNotify, PendingIntent.FLAG_UPDATE_CURRENT );
        }

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle()
                .addLine("\uD83D\uDD51 Sending Help Request To SOS")
                .addLine("Contacts In"+" "+(timeLeft)+" seconds");

        //Notification action button intents

        Intent actionActivityIntentHELP = new Intent(this, Receiver.class);
        actionActivityIntentHELP.putExtra(user_home.Message_KEY, "HELP");
        PendingIntent actionHELPPending = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionHELPPending = PendingIntent.getBroadcast(
                    this, 0, actionActivityIntentHELP, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        }
        else{
            actionHELPPending = PendingIntent.getBroadcast(
                    this, 0, actionActivityIntentHELP, PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        Notification.Action actionHELP = new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.help),"Need Help", actionHELPPending).build();

        Intent actionActivityIntentFINE = new Intent(this, Receiver.class);
        actionActivityIntentFINE.putExtra(user_home.Message_KEY, "FINE");
        PendingIntent actionFINEPending = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionFINEPending = PendingIntent.getBroadcast(
                    this, 1, actionActivityIntentFINE, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        }
        else {
            actionFINEPending = PendingIntent.getBroadcast(
                    this, 1, actionActivityIntentFINE, PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        Notification.Action actionFINE = new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ok),"I Am Ok", actionFINEPending).build();


        Notification.Builder builder = new Notification.Builder(this)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.splash_logo)
                .setContentTitle("Detected "+levelAccident+" Level Accident")
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


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = builder.setChannelId(ACCIDENT_NOTIFICATION_CHANNEL).build();
            notificationManager.createNotificationChannel(new NotificationChannel(ACCIDENT_NOTIFICATION_CHANNEL, "Accident Detected Channel", NotificationManager.IMPORTANCE_HIGH));
        }
        else {
            notification = builder.build();
        }
        notificationManager.notify(NOTIFICATION_ACCIDENT_DETECT_ID, (Notification) notification);

    }

    private void notifySOS(String levelAccident) {
        Log.i("Msg", "Respond Send SOS");

        //Send message to Sos
        if(sendSMS("SOS Respond")){
            //push notification

            if(!levelAccident.equals("Low")){
                SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("accident_dialog",false);
                editor.putBoolean("respond_dialog",true);
                editor.putBoolean("respond_time_left",true);
                editor.apply();

                Intent intent = new Intent(SERVICE_MESSAGE);
                intent.putExtra(user_home.Message_KEY, "TimeSecondFirst");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                 countDownTimer = new CountDownTimer(45000, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        long timeLeft = millisUntilFinished;
                        displayNotificationSOS((int)timeLeft/1000, levelAccident);

                        Intent intent = new Intent(SERVICE_MESSAGE);
                        intent.putExtra(user_home.Message_KEY, "Time2 "+timeLeft/1000);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    }

                    @Override
                    public void onFinish() {
                        long timeLeft =0;
                        displayNotificationSOS((int) timeLeft/1000, levelAccident);
                        notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);
                        notifyEmergency(levelAccident);

                        flagSensorOnChange = true;

                        SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("respond_dialog",true);
                        editor.putBoolean("respond_time_left",true);
                        editor.putBoolean("accident_dialog",false);
                        editor.putBoolean("accident_detect_flag",flagSensorOnChange);
                        editor.apply();

                        Intent intent = new Intent(SERVICE_MESSAGE);
                        intent.putExtra(user_home.Message_KEY, "TimeEmergencyComplete");
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                        sensorManager.registerListener(G_Force_Counter.this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL & SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

                    }
                }.start();
            }
            else{
                displayNotificationSOS(0, levelAccident);
                Intent intent = new Intent(SERVICE_MESSAGE);
                intent.putExtra(user_home.Message_KEY, "TimeSOSComplete");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                flagSensorOnChange = true;
                SharedPreferences sharedPreferences = getSharedPreferences("View_Visible",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("accident_dialog",false);
                editor.putBoolean("respond_dialog",true);
                editor.putBoolean("respond_time_left",false);
                editor.putBoolean("accident_detect_flag",flagSensorOnChange);
                editor.apply();
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL & SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
            }

        }

    }

    private void displayNotificationSOS(int timeLeft, String levelAccident) {

        Intent iNotify = new Intent(getApplicationContext(), user_home.class);
        iNotify.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pi = PendingIntent.getActivity(this, REQ_CODE, iNotify, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        }
        else{
            pi = PendingIntent.getActivity(this, REQ_CODE, iNotify, PendingIntent.FLAG_UPDATE_CURRENT );
        }

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle()
                .addLine("✓ Message Delivered To SOS Contacts");


        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.alert_icon, null);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap largeIcon = bitmapDrawable.getBitmap();

        //Notification action button intents

        Intent actionActivityIntentFINE = new Intent(this, Receiver.class);
        actionActivityIntentFINE.putExtra(user_home.Message_KEY, "FINE");
        PendingIntent actionFINEPending = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            actionFINEPending = PendingIntent.getBroadcast(
                    this, 1, actionActivityIntentFINE, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );
        }
        else{
            actionFINEPending = PendingIntent.getBroadcast(
                    this, 1, actionActivityIntentFINE, PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
        Notification.Action actionFINE = new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ok),"I Am OK", actionFINEPending).build();


        Notification.Builder builder = new Notification.Builder(this)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.splash_logo)
                .setContentTitle("Detected "+levelAccident+" Level Accident")
                .setStyle(inboxStyle)
                .setSubText("Accident Detected")
                .setContentIntent(pi) // without pi for normal notification//
                .addAction(actionFINE)
                .setColor(Color.RED)
                .setOnlyAlertOnce(true)
                .setShowWhen(true)                             //same as NotificationCompat.   default value is 'false'
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        if(levelAccident.equals("Low") || timeLeft == 0){
            builder.setAutoCancel(true).setOngoing(false);
        }
        else{
            builder.setAutoCancel(false).setOngoing(true);
            inboxStyle.addLine("\uD83D\uDD51 Sending Help Request To Emergency")
                    .addLine("Service In"+" "+(timeLeft)+" seconds");
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = builder.setChannelId(ACCIDENT_NOTIFICATION_CHANNEL).build();
            notificationManager.createNotificationChannel(new NotificationChannel(ACCIDENT_NOTIFICATION_CHANNEL, "Accident Detected Channel", NotificationManager.IMPORTANCE_HIGH));
        }
        else {
            notification = builder.build();
        }
        notificationManager.notify(NOTIFICATION_ACCIDENT_DETECT_ID, (Notification) notification);

    }



    private void notifyEmergency(String levelAccident) {

        Log.i("Msg", "Respond Send Emergency");

        if(sendSMS("Emergency Respond")){
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
        }
        else{
            pi = PendingIntent.getActivity(this, REQ_CODE, iNotify, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle()
                .addLine("✓ Message Delivered To SOS Contacts")
                .addLine("✓ Message Delivered To Emergency Contacts");

        Notification.Builder builder = new Notification.Builder(this)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.splash_logo)
                .setContentTitle("Detected "+levelAccident+" Level Accident")
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


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = builder.setChannelId(ACCIDENT_NOTIFICATION_CHANNEL).build();
            notificationManager.createNotificationChannel(new NotificationChannel(ACCIDENT_NOTIFICATION_CHANNEL, "Accident Detected Channel", NotificationManager.IMPORTANCE_HIGH));
        }
        else {
            notification = builder.build();
        }

        notificationManager.notify(NOTIFICATION_ACCIDENT_DETECT_ID, (Notification) notification);

    }

    private boolean sendSMS(String SMS) {
        String phoneNo = "6292286766";
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, SMS, null, null);
            return true;
            //Toast.makeText(MainActivity.this, "!!THE MESSAGE IS SENT SUCCESSFULLY!!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
            //Toast.makeText(this, "!!FAILED TO SEND THE MESSAGE", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "well", Toast.LENGTH_SHORT).show();
            }
            else if (Message.equals("USER_FINE_SOS")) {
                countDownTimer.cancel();
                notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);
                sendSMS("USER_FINE_SOS");
            }
            else if (Message.equals("USER_FINE_Medium")) {
                countDownTimer.cancel();
                notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);
                sendSMS("USER_FINE_Medium");
            }
            else if (Message.equals("USER_FINE_High")) {
                countDownTimer.cancel();
                notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);
                sendSMS("USER_FINE_High");
            }
            else if(Message.contains("Checked")) {
                countDownTimer.cancel();
                notificationManager.cancel(NOTIFICATION_ACCIDENT_DETECT_ID);
                sendSMS("Help");
            }

            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
            sensorManager.registerListener(G_Force_Counter.this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL & SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        }
    };

}
