package com.isleepbetter.isleepbetter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NFTdisplayActivity extends AppCompatActivity {

    private BarChart bar_chart_history;
    private Boolean flag = true;
    private List<Entry> entries2 = new ArrayList<>();
    private Integer count;
    private Cursor c = null;
    private Button again,finish;

    private SQLiteDatabase db;
    private MyDBHelper dbHelper;

    private List<String> time = new ArrayList<>();
    private List<String> strategy = new ArrayList<>();
    private List<Integer> score = new ArrayList<>();
    private List<BarEntry> entries = new ArrayList<>();
    private List<Integer> color = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nftdisplay);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        dbHelper = new MyDBHelper(this);
        db = dbHelper.getWritableDatabase(); // 開啟資料庫

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        String day = dateFormat.format(date);
        String id = getSharedPreferences("name", MODE_PRIVATE)
                .getString("id", "");
        c=db.rawQuery("SELECT * FROM data where id = '"+id + "'" + "and date = '"+day+ "'",null);
        count = c.getCount();

        List<String> xLabel = new ArrayList<>();
        for(int i = 0;i<count;i++)
            xLabel.add(String.valueOf(i+1));

        bar_chart_history = (BarChart) findViewById(R.id.bar_chart_history);

        bar_chart_history.getAxisRight().setAxisMinValue(0.0f); // set x-axis min-value and max-value
        bar_chart_history.getAxisRight().setAxisMaximum(100.0f);
        bar_chart_history.getAxisLeft().setAxisMinValue(0.0f);
        bar_chart_history.getAxisLeft().setAxisMaximum(100.0f);
        XAxis bar_xAxis = bar_chart_history.getXAxis();
        bar_xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        bar_chart_history.getAxisRight().setEnabled(false);
        bar_chart_history.getDescription().setText("");
        bar_chart_history.getLegend().setEnabled(false); // disable description left-down
        bar_xAxis.setDrawGridLines(false);
        bar_chart_history.getAxisLeft().setDrawAxisLine(false);
        bar_chart_history.getAxisRight().setDrawAxisLine(false);
        bar_chart_history.getAxisLeft().setDrawGridLines(false);
        bar_chart_history.getAxisRight().setDrawGridLines(false);
        bar_xAxis.setDrawAxisLine(false);
        bar_xAxis.setGranularity(1);
//        bar_xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabel));
        bar_chart_history.setTouchEnabled(false);
        bar_chart_history.getAxisLeft().setDrawLabels(false);
        bar_chart_history.animateXY(1000,1000);




        again = findViewById(R.id.again);
        again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getSharedPreferences("name", MODE_PRIVATE);
                pref.edit()
                        .putInt("device_connect_status", 1)
                        .commit();
                startActivity(new Intent(NFTdisplayActivity.this, DeviceControlActivity.class));
                finish();
            }
        });

        finish = findViewById(R.id.finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NFTdisplayActivity.this, NFTActivity.class));
                finish();
            }
        });

        TableLayout tablelayout = (TableLayout) findViewById(R.id.tableLayout);

        c.moveToFirst();
        for (int i = 0;i<count;i++)
        {
            strategy.add(c.getString(3));
            score.add(c.getInt(4));
            entries.add(new BarEntry(i+1, c.getInt(4)));
            color.add(Color.rgb(168,204,250));
            c.moveToNext();
        }

        int max = 0;
        int score_tmp = 0;
        for (int i = 0; i < score.size(); i++) {
            if (score.get(i) > score_tmp) {
                max = i ;
                score_tmp = score.get(i);
            }
        }
        color.set(max,Color.rgb(250,207,168));

        createTableRow(tablelayout,getResources().getString(R.string.no),getResources().getString(R.string.strategy),0,0);

        for (int i = 0;i<count;i++)
        {
            int status = 0;
            if(i == max){
                status = 1;
            }
            createTableRow(tablelayout,String.valueOf(i+1),strategy.get(i),i+1,status);
        }

        CustomBarChartRender barChartRender = new CustomBarChartRender(bar_chart_history,bar_chart_history.getAnimator(), bar_chart_history.getViewPortHandler());
        barChartRender.setRadius(10);
        bar_chart_history.setRenderer(barChartRender);

        get_data();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent(NFTdisplayActivity.this, DeviceControlActivity.class));
        finish();
        return super.onKeyDown(keyCode, event);
    }

    public void get_data() {
        bar_chart_history.clear();


        bar_chart_history.clear();
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                BarDataSet set = new BarDataSet(entries, "BarDataSet");
                set.setColors(color);
                set.setDrawValues(false);
                BarData data = new BarData(set);
                bar_chart_history.setData(data);
                bar_chart_history.invalidate();
            }
        }));

//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
//        Date date = new Date();
//        String day = dateFormat.format(date);
//
//        String id = getSharedPreferences("name", MODE_PRIVATE)
//                .getString("id", "");
//        c=db.rawQuery("SELECT * FROM data where id = '"+id + "'" + "and date = '"+day+ "'",null);
//        count = c.getCount();
//        c.moveToFirst();
//        if(count >0) {
//            runOnUiThread(new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    List<BarEntry> entries = new ArrayList<>();
//                    for (int i = 1;i<=count;i++)
//                    {
//                        entries.add(new BarEntry(i, c.getFloat(4)));
//                        BarDataSet set = new BarDataSet(entries, "BarDataSet");
//                        set.setColor(Color.rgb(253, 201, 203));
//                        set.setDrawValues(false);
//                        BarData data = new BarData(set);
//                        bar_chart_history.setData(data);
//                        bar_chart_history.invalidate();
//                        c.moveToNext();
//                    }
//                }
//            }));
//        }
    }

    private void createTableRow(TableLayout tablelayout, String num,String strategy, Integer index,Integer status){
        int textSize = 22;
        TableRow row= new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        TextView number_tv = new TextView(this);
        TextView strategy_tv = new TextView(this);
        number_tv.setText(num);
        strategy_tv.setText(strategy);
        number_tv.setTextColor(Color.BLACK);
        strategy_tv.setTextColor(Color.BLACK);
        if(status == 1) {
            row.setBackgroundColor(Color.rgb(250, 207, 168));
        }

        number_tv.setWidth(150);
        strategy_tv.setWidth(1300);
        if(index == 0)
        {
            number_tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            strategy_tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            textSize = 24;
        }
        number_tv.setTextSize(textSize);
        strategy_tv.setTextSize(textSize);
        row.addView(number_tv);
        row.addView(strategy_tv);
        tablelayout.addView(row,index);

    }

}
