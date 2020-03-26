package com.isleepbetter.isleepbetter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

public class NFTActivity extends AppCompatActivity {

    private MaterialCalendarView calendarview;
    private Button train,month_history;

    private Integer count;
    private TextView greeting,name;
    private String id;

    private Cursor c;
    private SQLiteDatabase db;
    private MyDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nft);

        dbHelper = new MyDBHelper(this);
        db = dbHelper.getWritableDatabase(); // 開啟資料庫

        calendarview = findViewById(R.id.calendarView);
        calendarview.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE);
        calendarview.setTitleFormatter(new MonthArrayTitleFormatter(getResources().getTextArray(R.array.custom_months)));
        calendarview.setWeekDayFormatter(new ArrayWeekDayFormatter(getResources().getTextArray(R.array.custom_weekdays)));

        greeting  =findViewById(R.id.greetings);
        name = findViewById(R.id.username);

        String user_name = getSharedPreferences("name", MODE_PRIVATE)
                .getString("user_name", "");

        name.setText(user_name);

        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (currentHour >= 6 && currentHour <=11)
            greeting.setText(R.string.Good_morning);
        else if(currentHour >= 12 && currentHour <= 16)
            greeting.setText(R.string.Good_afternoon);
        else if(currentHour >= 17 && currentHour <= 19)
            greeting.setText(R.string.Good_evening);
        else
            greeting.setText(R.string.Good_night);

        id = getSharedPreferences("name", MODE_PRIVATE)
                .getString("id", "");
        c=db.rawQuery("SELECT * FROM data where id = '"+id + "'",null);
        count = c.getCount();
        c.moveToFirst();
        for(int i = 0;i<count;i++)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            try {
                Date date = sdf.parse(String.valueOf(c.getString(1)));
                calendarview.setDateSelected(date, true);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            c.moveToNext();
        }

        calendarview.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

                String select_day = dateFormat.format(date.getDate());
                SharedPreferences pref = getSharedPreferences("name", MODE_PRIVATE);
                pref.edit()
                        .putString("select_day", dateFormat.format(date.getDate()))
                        .commit();
                calendarview.setDateSelected(date, !selected);
                Log.d("selecy_date",select_day);

//                c = db.rawQuery("SELECT * FROM data where id = '" + id + "'" + "and date = '" + select_day + "'", null);
//                if(c.getCount() != 0) {
                    startActivity(new Intent(NFTActivity.this, NFTHistoryActivity.class));
                    finish();
//                }
            }
        });

        train = findViewById(R.id.train);
        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getSharedPreferences("name", MODE_PRIVATE);
                pref.edit()
                        .putInt("status",2)
                        .commit();
                startActivity(new Intent(NFTActivity.this, ConnectActivity.class));
                finish();
            }
        });

        month_history = findViewById(R.id.month_history);
        month_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NFTActivity.this, NFTMonthHistoryActivity.class));
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent(NFTActivity.this, MainActivity.class));
        finish();
        return super.onKeyDown(keyCode, event);
    }



}
