package com.isleepbetter.isleepbetter;

import android.app.Application;
import android.media.MediaPlayer;

public class GlobalVariable extends Application {
    private int hourOfDay;
    private int minute;

    public void sethourOfDay(int hourOfDay){
        this.hourOfDay = hourOfDay;
    }
    public void setminute(int minute){
        this.minute = minute;
    }

    public int gethourOfDay(){ return hourOfDay; }
    public int getminute(){ return minute; }
}
