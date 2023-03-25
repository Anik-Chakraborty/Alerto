package com.example.alerto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String Message = intent.getStringExtra(user_home.Message_KEY);

        if(Message.equals("FINE")){
            Intent move = new Intent(context.getApplicationContext(), user_home.class);
            move.putExtra("Msg","FINE");
            move.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(move);

        } else if (Message.equals("HELP")) {
            Intent move = new Intent(context.getApplicationContext(), user_home.class);
            move.putExtra("Msg","HELP");
            move.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(move);
        }
    }
}
