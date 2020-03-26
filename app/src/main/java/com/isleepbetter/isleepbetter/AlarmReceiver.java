package com.isleepbetter.isleepbetter;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

public  class AlarmReceiver extends BroadcastReceiver {

    private static MediaPlayer mPlayer;
    private GlobalVariable gv;
    private static Ringtone ringtone;

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        if(arg1.getAction().toString().equals("start")){
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null)
            {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(arg0, alarmUri);
            ringtone.play();

        } else {
            ringtone.play();
            ringtone.stop();
        }
    }

}
