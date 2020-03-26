package com.isleepbetter.isleepbetter;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SleepDiaryActivity extends AppCompatActivity {

    private TextView name,sleep_time_txt,wake_time_txt,date;
    private Button sleep_time,wake_time,save,edit;
    private TimePickerDialog sleep_timePicker;
    private EditText sleep_latency,wake_times,note;

    private Cursor c;
    private SQLiteDatabase db;
    private MyDBHelper dbHelper;

    private String id,select_day;
    private Integer data_status = 0,save_status = 0; //data_status  0 無資料 ， 1 有資料，save_status  0 儲存狀態 ， 1 編輯狀態

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_diary);

        // 建立SQLiteOpenHelper物件
        dbHelper = new MyDBHelper(this);
        db = dbHelper.getWritableDatabase(); // 開啟資料庫

        String user_name = getSharedPreferences("name", MODE_PRIVATE)
                .getString("user_name", "");
        name = findViewById(R.id.textView28);
        name.setText(user_name);

        date = findViewById(R.id.date);

        id = getSharedPreferences("name", MODE_PRIVATE)
                .getString("id", "");
        select_day = getSharedPreferences("name", MODE_PRIVATE)
                .getString("select_day", "");

        date.setText(select_day+"_睡眠日誌");

        sleep_time_txt = findViewById(R.id.textView30);
        wake_time_txt = findViewById(R.id.textView31);
        sleep_latency = findViewById(R.id.editText);
        wake_times = findViewById(R.id.editText5);
        note = findViewById(R.id.editText4);
        sleep_latency.setText("");

        sleep_time = findViewById(R.id.button2);
        sleep_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                set_time(0);
            }
        });
        wake_time = findViewById(R.id.button3);
        wake_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                set_time(1);
            }
        });
        save = findViewById(R.id.button);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check_data_and_save();
            }
        });

        edit = findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                change_componenet_status(1);
                save_status = 0;
            }
        });

        try{c = db.rawQuery("SELECT * FROM sleep_diary where id = '" + id + "'" + "and date = '" + select_day + "'", null);}
        catch(Exception e){
        }
        if(c.getCount()>0)//db_status == 0
        {
            Log.d("check_data","have data");

            c.moveToFirst();
            sleep_time_txt.setText(c.getString(2));
            wake_time_txt.setText(c.getString(3));
            sleep_latency.setText(String.valueOf(c.getInt(4)));
            wake_times.setText(String.valueOf(c.getInt(5)));
            note.setText(c.getString(6));
            data_status = 1;
            save_status = 0;
            change_componenet_status(0);

            Log.d("check_data",c.getString(2) + "  , " + c.getString(3) + " , "+c.getInt(4)+" , "+c.getInt(5)+" , "+c.getString(6));


        }
        else
        {
            save_status = 1;
            change_componenet_status(1);
        }
    }

    public void set_time(final Integer status) { // 0 sleep_time 1 wake_time
        Calendar calendar = Calendar.getInstance();
        Integer hour = calendar.get(Calendar.HOUR_OF_DAY);
        Integer minute = calendar.get(Calendar.MINUTE);
        sleep_timePicker = new TimePickerDialog(SleepDiaryActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                        if(status == 0)
                        {
                            sleep_time_txt.setText(sHour+" : "+sMinute);
                        }
                        else
                        {
                            wake_time_txt.setText(sHour+" : "+sMinute);
                        }
                    }
                }, hour, minute, true);
        sleep_timePicker.show();

    }

    public void check_data_and_save() { // 0 sleep_time 1 wake_time

        int check = 0;
        String message = "";
        if(sleep_time_txt.getText() == "")
            message = message + "請選擇上床時間\n";
        if(wake_time_txt.getText() == "")
            message = message + "請選擇隔日起床時間\n";
        if(sleep_latency.getText().toString().equals("")) {
            message = message + "請填寫多久入睡(分鐘)\n";
        }
        if(message != "")
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提醒");
            builder.setMessage(message);
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else
        {
            save_status = 0;
            //存入資料庫
            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("date", select_day);
            values.put("sleep_time", (String) sleep_time_txt.getText());
            values.put("wake_time", (String) wake_time_txt.getText());
            values.put("sleep_latency", String.valueOf(sleep_latency.getText()));
            values.put("wake_times", String.valueOf(wake_times.getText()));
            values.put("note", String.valueOf(note.getText()));
            if(data_status == 0){
                db.insert("sleep_diary",null,values);
            }
            else{
                db.update("sleep_diary", values,  "id ='" + id +"'"+" and date = '" + select_day + "'", null);
            }
            change_componenet_status(0);

            AlertDialog.Builder builder = new AlertDialog.Builder(SleepDiaryActivity.this);
            builder.setTitle("儲存");
            builder.setMessage("日誌儲存完畢!");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final Integer[] cancel = {0};
        if(save_status == 1) //為編輯狀態或資料填寫狀態，詢問是否儲存
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("儲存");
            builder.setMessage("是否儲存當前日誌?");
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    check_data_and_save();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(SleepDiaryActivity.this, NFTActivity.class));
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else
        {
            startActivity(new Intent(SleepDiaryActivity.this, NFTActivity.class));
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void change_componenet_status(Integer status) // 預設無資料或為編輯狀態， 0 表示有資料或為儲存狀態
    {
        Boolean enable = true;
        Integer txt_color = Color.BLACK;
        if(status == 0)
        {
            edit.setEnabled(true);
            enable = false;
            txt_color = Color.GRAY;
        }
        else
        {
            edit.setEnabled(false);
        }
        save.setEnabled(enable);
        sleep_time.setEnabled(enable);
        wake_time.setEnabled(enable);
        sleep_latency.setEnabled(enable);
        wake_times.setEnabled(enable);
        note.setEnabled(enable);
        sleep_time_txt.setTextColor(txt_color);
        wake_time_txt.setTextColor(txt_color);
    }
}
