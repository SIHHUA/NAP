package com.isleepbetter.isleepbetter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RestarterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("restart", "Service Stops! Oooooooooooooppppssssss!!!!");
        context.startService(new Intent(context, MonitorService.class));
    }
}