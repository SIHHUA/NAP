package com.isleepbetter.isleepbetter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class sleeprecordActivity extends AppCompatActivity {

    private MaterialCalendarView calendarview;
    private TextView greeting,name;
    private Button analysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleeprecord);

        calendarview = findViewById(R.id.calendarView);
        calendarview.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE);
        calendarview.setTitleFormatter(new MonthArrayTitleFormatter(getResources().getTextArray(R.array.custom_months)));
        calendarview.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)));

        analysis = findViewById(R.id.analysis);

        name = findViewById(R.id.username);

        String user_name = getSharedPreferences("name", MODE_PRIVATE)
                .getString("user_name", "");
        name.setText(user_name);

        greeting  =findViewById(R.id.greetings);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (currentHour >= 6 && currentHour <=11)
            greeting.setText(R.string.Good_morning);
        else if(currentHour >= 12 && currentHour <= 16)
            greeting.setText(R.string.Good_afternoon);
        else if(currentHour >= 17 && currentHour <= 19)
            greeting.setText(R.string.Good_evening);
        else
            greeting.setText(R.string.Good_night);


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        try {
            Date date = sdf.parse("2020/01/01");
            calendarview.setDateSelected(date, true);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            Date date = sdf.parse("2020/01/03");
            calendarview.setDateSelected(date, true);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        calendarview.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd");

                String select_day = dateFormat.format(date.getDate());
                SharedPreferences pref = getSharedPreferences("name", MODE_PRIVATE);

                Log.d("datedtate",date.getDay()+"");
                calendarview.setDateSelected(date, !selected);
                if(date.getDay()==  01) {
                    pref.edit()
                            .putString("test_day", "1")
                            .commit();

                    startActivity(new Intent(sleeprecordActivity.this, SleepHistoryActivity.class));
                    finish();
                }
                if(date.getDay()==  03) {
                    pref.edit()
                            .putString("test_day", "2")
                            .commit();

                    startActivity(new Intent(sleeprecordActivity.this, SleepHistoryActivity.class));
                    finish();
                }
            }
        });

        analysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getSharedPreferences("name", MODE_PRIVATE);
                pref.edit()
                    .putInt("status",3)
                    .commit();
                startActivity(new Intent(sleeprecordActivity.this, ConnectActivity.class));
                finish();
            }
        });

;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent(sleeprecordActivity.this, MainActivity.class));
        finish();
        return super.onKeyDown(keyCode, event);
    }
}
