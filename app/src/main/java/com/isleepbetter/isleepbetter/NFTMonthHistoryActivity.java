package com.isleepbetter.isleepbetter;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NFTMonthHistoryActivity extends AppCompatActivity {

    private Cursor c = null;
    private SQLiteDatabase db;
    private MyDBHelper dbHelper;

    private String start_day,end_day;
    private Integer status = 0,year,month;
    private final ArrayList<String> xLabel = new ArrayList<>();

    private XAxis bar_xAxis;
    private BarChart bar_chart_history;
    private TextView month_tv,warning;
    private ImageView last,next;

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nftmonth_history);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        year = Calendar.getInstance().get(Calendar.YEAR);
        month = Calendar.getInstance().get(Calendar.MONTH)+1;

        month_tv = findViewById(R.id.textView10);
        warning = findViewById(R.id.textView22);
        last = findViewById(R.id.last);
        next = findViewById(R.id.next);
        month_tv.setText(year+"/"+month);

        last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastMonth();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextMonth();
            }
        });

        //1 初始化  手势识别器
        mGestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {// e1: 第一次按下的位置   e2   当手离开屏幕 时的位置  velocityX  沿x 轴的速度  velocityY： 沿Y轴方向的速度
                //判断竖直方向移动的大小
                if(Math.abs(e1.getRawY() - e2.getRawY())>100){
                    //Toast.makeText(getApplicationContext(), "动作不合法", 0).show();
                    return true;
                }
                if(Math.abs(velocityX)<150){
                    //Toast.makeText(getApplicationContext(), "移动的太慢", 0).show();
                    return true;
                }

                if((e1.getRawX() - e2.getRawX()) >200){// 表示 向右滑动表示下一页
                    //显示下個月
                    nextMonth();
                    return true;
                }

                if((e2.getRawX() - e1.getRawX()) >200){  //向左滑动 表示 上一页
                    //显示上個月
                    lastMonth();
                    return true;//消费掉当前事件  不让当前事件继续向下传递
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        dbHelper = new MyDBHelper(this);
        db = dbHelper.getWritableDatabase(); // 開啟資料庫

        bar_chart_history = (BarChart) findViewById(R.id.bar_chart_history);
        bar_chart_history.getAxisRight().setAxisMinValue(0.0f); // set x-axis min-value and max-value
        bar_chart_history.getAxisRight().setAxisMaximum(100.0f);
        bar_chart_history.getAxisLeft().setAxisMinValue(0.0f);
        bar_chart_history.getAxisLeft().setAxisMaximum(100.0f);
        bar_xAxis = bar_chart_history.getXAxis();
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
        bar_chart_history.setTouchEnabled(false);
        bar_chart_history.getAxisLeft().setDrawLabels(false);
        CustomBarChartRender barChartRender = new CustomBarChartRender(bar_chart_history,bar_chart_history.getAnimator(), bar_chart_history.getViewPortHandler());
        barChartRender.setRadius(10);
        bar_chart_history.setRenderer(barChartRender);
        bar_chart_history.animateXY(1000,1000);



        bar_xAxis.setDrawLabels(true);
        get_data();

        bar_xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabel));
    }

    public void nextMonth()
    {
        if(month == 12) {
            year = year + 1;
            month = 1;
        }
        else
        {
            month = month + 1;
        }
        xLabel.clear();
        Log.d("test111","  b");

        get_data();
        month_tv.setText(year+"/"+month);
        bar_xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabel));
     }

    public void lastMonth()
    {
        if(month == 1) {
            year = year - 1;
            month = 12;
        }
        else
        {
            month = month - 1;
        }
        xLabel.clear();
        Log.d("test111","   a");

        get_data();

        month_tv.setText(year+"/"+month);
        bar_xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabel));
    }

    //重写activity的触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //2.让手势识别器生效
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void get_data() {
        String id = getSharedPreferences("name", MODE_PRIVATE)
                .getString("id", "");
        start_day = String.format("%d/%02d/01", year, month);
        end_day = String.format("%d/%02d/31", year, month);
        Log.d("test111",start_day);
        try {
            c = db.rawQuery("SELECT id,date,sum(avg_score)AS total_score,COUNT(*) FROM data WHERE id = '"+id + "'" + "and (date >= '"+start_day+"' and date <= '"+end_day+"') GROUP BY date,id",null);
        }catch (Exception e)
        {
            status = 1;
        }
        Log.d("test111",c.getCount() +"   ac");

        if(c.getCount() != 0) //status == 0
        {
            warning.setVisibility(TextView.INVISIBLE);
            bar_chart_history.setVisibility(BarChart.VISIBLE);
            c.moveToFirst();
            xLabel.clear();
            xLabel.add("");
            for (int i = 1;i<=c.getCount();i++)
            {
                String [] temp = null;
                temp = c.getString(1).split( Calendar.getInstance().get(Calendar.YEAR)+"/");
                xLabel.add(temp[1]+"");
                c.moveToNext();
            }

            c.moveToFirst();
            bar_chart_history.clear();

            if(c.getCount() >0) {
                runOnUiThread(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<BarEntry> entries = new ArrayList<>();
                        for (int i = 1;i<=c.getCount();i++)
                        {
                            String [] temp = null;
                            temp = c.getString(1).split( Calendar.getInstance().get(Calendar.YEAR)+"/");

                            Float avg_score = c.getFloat(2)/c.getInt(3);
                            entries.add(new BarEntry(i, avg_score));

                            BarDataSet set = new BarDataSet(entries, "BarDataSet");
                            set.setColor(Color.rgb(253, 201, 203));
                            set.setDrawValues(false);
                            BarData data = new BarData(set);
                            bar_chart_history.setData(data);
                            bar_chart_history.invalidate();
                            c.moveToNext();
                        }
                    }
                }));
            }
        }
        else
        {
            warning.setVisibility(TextView.VISIBLE);
            bar_chart_history.setVisibility(BarChart.INVISIBLE);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent(NFTMonthHistoryActivity.this, NFTActivity.class));
        finish();
        return super.onKeyDown(keyCode, event);
    }
}
