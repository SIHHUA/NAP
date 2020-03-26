package com.isleepbetter.isleepbetter;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class NormalClockActivity extends Activity {

    private Button close;
    private ImageView status;
    private PendingIntent pendingIntent;
    private Long time;
    private AlarmManager alarmManager;
    private List<Integer> data = new ArrayList<>(Arrays.asList(0,1,1,2,3));
    private final ArrayList<String> xLabel = new ArrayList<>(Arrays.asList("5", "10", "15", "20", "25"));
    private static Ringtone ringtone;

    private List<String> ylabel = new ArrayList<>(Arrays.asList("", "Deep", "Light", "Wake"));

    private BarChart bar_chart;
    private ArrayList<String> entries1 = new ArrayList<>();
    private ArrayList<BarEntry> entries2 = new ArrayList<>();
    private ArrayList<IBarDataSet> entries3 = new ArrayList<>();
    private XAxis bar_xAxis;


    private Timer timer;
    private Integer index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_clock);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        time = getSharedPreferences("name", MODE_PRIVATE)
                .getLong("time", 0);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("start");
        pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

        close = findViewById(R.id.close);
        status = findViewById(R.id.status);
        bar_chart = findViewById(R.id.line_chart);
        // line_chart configuration
        bar_chart.getAxisRight().setAxisMinValue(0); // set x-axis min-value and max-value
        bar_chart.getAxisRight().setAxisMaximum(4);
        bar_chart.getAxisRight().setEnabled(false);
        bar_chart.getAxisLeft().setAxisMinValue(0);
        bar_chart.getAxisLeft().setAxisMaximum(3);
        bar_chart.getAxisLeft().setAxisMinimum(0);
        bar_chart.getAxisLeft().setLabelCount(3);
        bar_chart.getAxisLeft().setDrawGridLines(false);
        bar_chart.getAxisRight().setDrawGridLines(false);
        bar_chart.getXAxis().setDrawGridLines(false);
        bar_chart.getDescription().setEnabled(false);// disable description right-down
        bar_chart.getLegend().setEnabled(false); // disable description left-down
        XAxis line_xAxis = bar_chart.getXAxis();
        line_xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        bar_xAxis = bar_chart.getXAxis();
//        bar_xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabel));

        bar_chart.setTouchEnabled(false);
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return ylabel.get((int) value);
            }
        };
        bar_chart.getAxisLeft().setValueFormatter(formatter);

        index = 0;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                NormalClockActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        get_data(index, data.get(index));
                        index++;
                        if(index==data.size())
                        {
                            status.setImageResource(R.drawable.wake);
                            close.setVisibility(Button.VISIBLE);
                            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                            if (alarmUri == null)
                            {
                                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            }
                            ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
                            ringtone.play();
                            timer.cancel();
                        }
                    }
                });

            }
        }, 0,10000/2);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ringtone.stop();
                SharedPreferences pref = getSharedPreferences("name", MODE_PRIVATE);
                pref.edit()
                        .putInt("ringtone", 1)
                        .commit();
                startActivity(new Intent(NormalClockActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    public void stop_alarm(View view)
    {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("stop");
        pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    public void get_data(final Integer index, final Integer data) {
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {

                // update linechart
                switch (data) {
                    case 0:
                        entries2.add(new BarEntry( index, 3));

                    case 1:
                        entries2.add(new BarEntry( index, 2));
                    case 2:
                        entries2.add(new BarEntry( index, 2));
                    case 3:
                        entries2.add(new BarEntry( index, 1));
                    break;
                }
                entries1.add("123");

                BarDataSet dataSet = new BarDataSet(entries2, "");
                dataSet.setDrawValues(false); // disable value display
                BarData lineData = new BarData(dataSet);
                lineData.setBarWidth(0.9f);

                bar_chart.setData(lineData);
                bar_chart.notifyDataSetChanged(); // let the chart know it's data changed
                bar_chart.invalidate(); // refresh

            }
        }));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent(NormalClockActivity.this, AlarmclockActivity.class));
        finish();
        return super.onKeyDown(keyCode, event);
    }

}