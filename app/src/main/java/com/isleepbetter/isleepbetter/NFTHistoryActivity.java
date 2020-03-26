package com.isleepbetter.isleepbetter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
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

import java.util.ArrayList;
import java.util.List;

public class NFTHistoryActivity extends AppCompatActivity {

    private Integer count;
    private TextView date,no_data;

    private BarChart bar_chart_history;

    private Cursor c;
    private SQLiteDatabase db;
    private MyDBHelper dbHelper;

    private List<String> time = new ArrayList<>();
    private List<String> strategy = new ArrayList<>();
    private List<Integer> score = new ArrayList<>();
    private List<BarEntry> entries = new ArrayList<>();
    private List<Integer> color = new ArrayList<>();

    private Button sleepdairy;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfthistory);

        dbHelper = new MyDBHelper(this);
        db = dbHelper.getWritableDatabase(); // 開啟資料庫

        date = findViewById(R.id.date);

        String id = getSharedPreferences("name", MODE_PRIVATE)
                .getString("id", "");
        String select_day = getSharedPreferences("name", MODE_PRIVATE)
                .getString("select_day", "");

        date.setText(select_day);

        sleepdairy = findViewById(R.id.sleepdiary);
        sleepdairy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NFTHistoryActivity.this, SleepDiaryActivity.class));
                finish();
            }
        });

        Log.d("selecy_date",select_day);

        c = db.rawQuery("SELECT * FROM data where id = '" + id + "'" + "and date = '" + select_day + "'", null);
        count = c.getCount();
        bar_chart_history = findViewById(R.id.bar_chart_history);
        if (count != 0) {
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
            bar_chart_history.setTouchEnabled(false);
            bar_xAxis.setDrawAxisLine(false);
            bar_xAxis.setGranularity(1);
            bar_chart_history.getAxisLeft().setDrawLabels(false);
            bar_chart_history.animateXY(1000,1000);

            TableLayout tablelayout = (TableLayout) findViewById(R.id.tableLayout);
            DisplayMetrics metrics = new DisplayMetrics(); //取得手機畫面大小
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            c.moveToFirst();
            for (int i = 0;i<count;i++)
            {
                time.add(c.getString(2));
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

            createTableRow(tablelayout,metrics,getResources().getString(R.string.no),getResources().getString(R.string.time),getResources().getString(R.string.strategy),0,0);
            for (int i = 0;i<count;i++)
            {
                int status = 0;
                if(i == max){
                    status = 1;
                }
                createTableRow(tablelayout,metrics,String.valueOf(i+1),time.get(i),strategy.get(i),i+1,status);
            }

            get_data();
            CustomBarChartRender barChartRender = new CustomBarChartRender(bar_chart_history,bar_chart_history.getAnimator(), bar_chart_history.getViewPortHandler());
            barChartRender.setRadius(10);
            bar_chart_history.setRenderer(barChartRender);
        }
        else
        {
            bar_chart_history.setVisibility(BarChart.INVISIBLE);
            no_data = findViewById(R.id.textView34);
            no_data.setVisibility(TextView.VISIBLE);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent(NFTHistoryActivity.this, NFTActivity.class));
        finish();
        return super.onKeyDown(keyCode, event);
    }

    public void get_data() {
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
    }

    private void createTableRow(TableLayout tablelayout,DisplayMetrics metrics,String num,String time,String strategy,Integer index,Integer status){
        int textSize = 22;
        TableRow row= new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        TextView number_tv = new TextView(this);
        TextView time_tv = new TextView(this);
        TextView strategy_tv = new TextView(this);
        number_tv.setText(num);
        time_tv.setText(time);
        strategy_tv.setText(strategy);
        number_tv.setTextColor(Color.BLACK);
        time_tv.setTextColor(Color.BLACK);
        strategy_tv.setTextColor(Color.BLACK);
        if(status == 1) {
            row.setBackgroundColor(Color.rgb(250, 207, 168));
        }

        number_tv.setWidth(metrics.widthPixels/6);
        time_tv.setWidth(metrics.widthPixels/4);
        strategy_tv.setWidth(metrics.widthPixels/2);
        if(index == 0)
        {
            number_tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            time_tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            strategy_tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            textSize = 24;
        }
        number_tv.setTextSize(textSize);
        time_tv.setTextSize(textSize);
        strategy_tv.setTextSize(textSize);
        row.addView(number_tv);
        row.addView(time_tv);
        row.addView(strategy_tv);
        tablelayout.addView(row,index);
    }
}
