package com.isleepbetter.isleepbetter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;

public class SleepHistoryActivity extends AppCompatActivity {

    private ImageView imv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_history);

        String day = getSharedPreferences("name", MODE_PRIVATE)
                .getString("test_day", "");
        if(day == "1") {
            imv = findViewById(R.id.imageView4);
            imv.setImageResource(R.drawable.data);
        }
        if(day == "2") {
            imv = findViewById(R.id.imageView4);
            imv.setImageResource(R.drawable.data_2);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent(SleepHistoryActivity.this, sleeprecordActivity.class));
        finish();
        return super.onKeyDown(keyCode, event);
    }
}
