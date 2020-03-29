package com.isleepbetter.isleepbetter;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainFunActivity extends AppCompatActivity {
    private Typeface fontType;
    private TextView glasses_txt1,glasses_txt2,glasses_txt3,glasses_txt4;
    private TextView headband_txt1,headband_txt2,headband_txt3,headband_txt4;
    private Button glasses,headband;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fun);

        init();

        glasses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(MainFunActivity.this, NFTActivity.class));
//                finish();
            }
        });

        headband.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainFunActivity.this, ConnectActivity.class));
                finish();
            }
        });

    }

    private void init()
    {
        fontType = Typeface.createFromAsset(this.getAssets(),"fonts/text.ttc");

        glasses_txt1 = findViewById(R.id.textView47);
        glasses_txt2 = findViewById(R.id.textView48);
        glasses_txt3 = findViewById(R.id.textView49);
        glasses_txt4 = findViewById(R.id.textView50);
        glasses = findViewById(R.id.button4);

        headband_txt1 = findViewById(R.id.textView51);
        headband_txt2 = findViewById(R.id.textView53);
        headband_txt3 = findViewById(R.id.textView54);
        headband_txt4 = findViewById(R.id.textView55);
        headband = findViewById(R.id.button5);

        TextView ar[] = {glasses_txt1,glasses_txt2,glasses_txt3,glasses_txt4,headband_txt1,headband_txt2,headband_txt3,headband_txt4};

        for(int i = 0;i<ar.length;i++)
        {
            ar[i].setTypeface(fontType);
        }
    }
}
