package com.isleepbetter.isleepbetter;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.w3c.dom.Text;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DemoNFTActivity extends AppCompatActivity {

    private LineChart linechart;
    private HorizontalBarChart barchart;
    private Button start;
    private TextView DemoCounDown;
    private float[] data = {0f, 86.07f, 93.74f, 81.22f, 94.2f, 65.73f, 55.35f, 53.79f, 58f, 36.69f, 34.9f, 36.99f, 36.52f, 45.45f, 36.28f, 53.7f, 36.19f, 36.76f, 40.6f, 50.6f, 54.71f, 47.09f, 45.15f, 35.93f, 47.21f, 29.11f, 52.14f, 45.6f, 38.97f, 27.18f, 44.4f, 29.01f, 38.84f, 41.68f, 35.34f, 35.16f, 39.36f, 34.51f, 54.57f, 45f, 51.45f, 38.8f, 54.49f, 28.99f, 44.85f, 36.74f, 38.54f, 46.81f, 43.12f, 37.7f, 47.58f, 42.87f, 37.88f, 39.5f, 40.11f, 29.25f, 30.02f, 48.46f, 36.98f, 36.13f, 31.39f, 32.93f, 35.17f, 50.97f, 36.04f, 42.89f, 51.07f, 62.07f, 30.54f, 36.27f, 33.54f, 44.18f, 46.3f, 40.84f, 33.71f, 38.42f, 28.48f, 35.68f, 42.31f, 38.17f, 40.57f, 47.98f, 38.21f, 41.15f, 44.61f, 40.85f, 32.91f, 43.67f, 49.51f, 44.41f, 34.61f, 33.37f, 63.96f, 37.1f, 37.39f, 26.44f, 35.96f, 53.59f, 37.66f, 86.47f, 31.12f, 40.2f, 45.49f, 40.79f, 41.75f, 37.99f, 63.97f, 52.13f, 48.08f, 69.26f, 88.23f, 77.61f, 80.06f, 59.07f, 82.14f, 46.99f, 80.71f, 81.82f, 90.41f, 77.27f, 90.07f, 92.23f, 86.33f, 71.85f, 76.19f, 83.69f, 56.36f, 47.98f, 58.22f, 61.9f};
    private Timer timer;
    private Integer index;
    private ArrayList<Entry> LineData = new ArrayList<>();
    private SQLiteDatabase db;
    private MyDBHelper dbHelper;
    private ArrayList<String> strategy_list = new ArrayList<>();
    private String strategy_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_nft);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        linechart = findViewById(R.id.line_chart_demo);
        barchart = findViewById(R.id.bar_chart_demo);
        start = findViewById(R.id.demostart);
        DemoCounDown = findViewById(R.id.DemoCounDown);

        // bar_chart configuration
        barchart.setDrawBarShadow(true);
        barchart.getAxisRight().setAxisMinValue(0.0f); // set x-axis min-value and max-value
        barchart.getAxisRight().setAxisMaximum(100.0f);
        barchart.getAxisLeft().setAxisMinValue(0.0f);
        barchart.getAxisLeft().setAxisMaximum(100.0f);
        barchart.getAxisLeft().setEnabled(false);
        barchart.getAxisRight().setEnabled(false);
        barchart.getDescription().setEnabled(false); // disable description right-down
        barchart.getLegend().setEnabled(false); // disable description left-down
        barchart.getXAxis().setEnabled(false);
        barchart.setTouchEnabled(false);
        // bar_chart configuration end

        // line_chart configuration
        linechart.getAxisRight().setAxisMinValue(0.0f); // set x-axis min-value and max-value
        linechart.getAxisRight().setAxisMaximum(100.0f);
        linechart.getAxisRight().setEnabled(false);
        linechart.getAxisLeft().setAxisMinValue(0.0f);
        linechart.getAxisLeft().setAxisMaximum(100.0f);
        linechart.getDescription().setEnabled(false);// disable description right-down
        linechart.getLegend().setEnabled(false); // disable description left-down
        XAxis line_xAxis = linechart.getXAxis();
        line_xAxis.setAxisMinimum(0.0f);
        line_xAxis.setAxisMaximum(120.0f);
        line_xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        linechart.setTouchEnabled(false);
        // line_chart configuration end



        index = 0;

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        DemoNFTActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                get_data();
                                index++;
                                if(index == data.length+1)
                                {
                                    timer.cancel();
                                }
                            }
                        });

                    }
                }, 0,1000);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent(DemoNFTActivity.this, NFTActivity.class));
        finish();
        return super.onKeyDown(keyCode, event);
    }

    private void get_data() {
        linechart.clear();
        barchart.clear();

        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                if (index < 10)  //倒數
                {
                    DemoCounDown.setText("CountDown " + (10-index) + " seconds");
                    List<BarEntry> entries = new ArrayList<>();
                    entries.add(new BarEntry(0f, 100 - (index * 10)));
                    BarDataSet set = new BarDataSet(entries, "BarDataSet");
                    set.setColor(Color.rgb(253, 201, 203));
                    set.setBarShadowColor(Color.rgb(187, 213, 236));
                    set.setDrawValues(false);
                    BarData bdata = new BarData(set);
                    bdata.setBarWidth(1.0f);
                    barchart.setData(bdata);
                    barchart.invalidate();
                } else if(index < 130) {
                    DemoCounDown.setText("");
                    linechart.setVisibility(Chart.VISIBLE);
                    //get data and set data here --- barchart
                    List<BarEntry> entries = new ArrayList<>();
                    entries.add(new BarEntry(0f, data[index]));
                    BarDataSet set = new BarDataSet(entries, "BarDataSet");
                    set.setColor(Color.rgb(253, 201, 203));
                    set.setBarShadowColor(Color.rgb(187, 213, 236));
                    set.setDrawValues(false);
                    BarData bdata = new BarData(set);
                    bdata.setBarWidth(1.0f);
                    barchart.setData(bdata);
                    barchart.invalidate();
                    //end

                    // update linechart
                    LineData.add(new Entry(index-10, data[index]));
                    LineDataSet dataSet = new LineDataSet(LineData, "");
                    LineData lineData = new LineData(dataSet);
                    dataSet.setDrawCircles(false); // disable point(circle)
                    dataSet.setDrawValues(false); // disable value display
                    linechart.setData(lineData);
                    linechart.invalidate(); // refresh
                }
//                else {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(DemoNFTActivity.this);
//                    LayoutInflater inflater = getLayoutInflater();
//                    final View dialogView = inflater.inflate(R.layout.view_nft_strategy, null);
//
//                    final RadioGroup radiogroup = dialogView.findViewById(R.id.radioGroup);
//                    final TextView score_text = dialogView.findViewById(R.id.score);
//                    final TextView strategy_txt = dialogView.findViewById(R.id.textView21);
//                    final TextView strategy = dialogView.findViewById(R.id.strategy);
////                    score_text.setText(avg_score.toString());
//
//                    String id = getSharedPreferences("name", MODE_PRIVATE)
//                            .getString("id", "");
//                    dbHelper = new MyDBHelper(DemoNFTActivity.this);
//                    db = dbHelper.getWritableDatabase(); // 開啟資料庫
//                    Cursor c = db.rawQuery("SELECT * FROM data where id = '"+id + "'  GROUP BY strategy",null);
//                    c.moveToFirst();
//                    int i = 0;
//
//                    if(c.getCount() == 0)
//                    {
//                        strategy_list.add(getResources().getString(R.string.other));
//                        //radioButton
//                        RadioButton radioButton = new RadioButton(getApplicationContext());
//                        radioButton.setPadding(100,0,0,0);
//                        radioButton.setText(getResources().getString(R.string.other));
//                        radioButton.setTextColor(Color.rgb(0,0,0));
//                        //必须有ID，否则默认选中的选项会一直是选中状态
//                        radioButton.setId(i);
//                        radioButton.setChecked(true);
//                        //layoutParams 设置margin值
//                        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                                ViewGroup.LayoutParams.WRAP_CONTENT);
//                        if (i!=0){
//                            layoutParams.setMargins(50,0,0,0);
//                        }else {
//                            layoutParams.setMargins(50,0,0,0);
//                        }
//                        //注意这里addView()里传入layoutParams
//                        radiogroup.addView(radioButton,layoutParams);
//                    }
//                    else
//                    {
//                        for (i = 0; i < c.getCount() + 1; i++) {
//                            RadioButton radioButton = new RadioButton(getApplicationContext());
//                            if(i == c.getCount())
//                            {
//                                strategy_list.add(getResources().getString(R.string.other));
//                                radioButton.setText(getResources().getString(R.string.other));
//                                radioButton.setChecked(true);
//
//                            }
//                            else if(!c.getString(3).equals("")){
//                                strategy_list.add(c.getString(3).toString());
//                                radioButton.setText(c.getString(3));
//                            }
//
//                            //radioButton
//                            radioButton.setPadding(100,0,0,0);
//                            radioButton.setTextColor(Color.rgb(0,0,0));
//                            //必须有ID，否则默认选中的选项会一直是选中状态
//                            radioButton.setId(i);
//
//                            //layoutParams 设置margin值
//                            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                                    ViewGroup.LayoutParams.WRAP_CONTENT);
//                            if (i!=0){
//                                layoutParams.setMargins(50,0,0,0);
//                            }else {
//                                layoutParams.setMargins(50,0,0,0);
//                            }
//                            //注意这里addView()里传入layoutParams
//                            radiogroup.addView(radioButton,layoutParams);
//                            c.moveToNext();
//                        }
////                        radiogroup.getChildAt(1).
//
//
//                    }
//                    builder.setView(dialogView);
//                    builder.create();
//                    builder.setCancelable(false);
//                    builder.show();
//
//                    radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                        @Override
//                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                            if(strategy_list.get(i).equals(getResources().getString(R.string.other)))
//                            {
//                                strategy_txt.setVisibility(TextView.VISIBLE);
//                                strategy.setVisibility(EditText.VISIBLE);
//                            }
//                            else{
//                                strategy_txt.setVisibility(TextView.INVISIBLE);
//                                strategy.setVisibility(EditText.INVISIBLE);
//                            }
//                        }
//                    });
//
//                    Button done = dialogView.findViewById(R.id.done);
//                    done.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
//                            Date date = new Date();
//                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
//                            Date time = new Date();
//
//                            if(strategy_txt.getVisibility() == TextView.VISIBLE){
//                                strategy_text = strategy.getText().toString();
//                            }
//                            else
//                            {
//                                strategy_text = strategy_list.get(radiogroup.getCheckedRadioButtonId());
//
//                            }
//
//
//                            startActivity(new Intent(DemoNFTActivity.this, ConnectActivity.class));
//                            finish();
////                            }
//                        }
//                    });
//                }
            }
        }));
    }
}
