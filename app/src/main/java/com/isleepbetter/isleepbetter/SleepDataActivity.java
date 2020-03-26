package com.isleepbetter.isleepbetter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SleepDataActivity extends AppCompatActivity {

    private ArrayList<Integer> pic;
    private Timer timer;
    private TextView tv;
    private ImageView im1,im2;

    private Integer index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_data);

        pic = new ArrayList<>();
        tv = findViewById(R.id.textView19);
        im2 = findViewById(R.id.imageView6);
        im1 = findViewById(R.id.imageView5);
        pic.add(R.drawable.p1);
        pic.add(R.drawable.p2);
        pic.add(R.drawable.p3);
        pic.add(R.drawable.p4);
        pic.add(R.drawable.p5);
        pic.add(R.drawable.p6);
        pic.add(R.drawable.p7);
        pic.add(R.drawable.p8);
        index = 0;


        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SleepDataActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        im1.setImageResource(pic.get(index));
                        index++;

                        if (index == 8) {
                            tv.setVisibility(TextView.VISIBLE);
                            im1.setVisibility(ImageView.INVISIBLE);
                            im2.setVisibility(ImageView.VISIBLE);
                            timer.cancel();
                        }
                    }
                });
            }
        }, 0,375);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent(SleepDataActivity.this, sleeprecordActivity.class));
        finish();
        return super.onKeyDown(keyCode, event);
    }
}
