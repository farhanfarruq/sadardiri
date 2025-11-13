// BootReceiver.java
package com.example.sadardiri;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent dailyIntent = new Intent(context, MainActivity.class);
            dailyIntent.putExtra("restart_alarm", true);
            dailyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(dailyIntent);
        }
    }
}