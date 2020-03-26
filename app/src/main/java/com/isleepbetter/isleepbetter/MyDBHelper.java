package com.isleepbetter.isleepbetter;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;


public class MyDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "mydata.db";
    // 資料庫版本，資料結構改變的時候要更改這個數字，通常是加一
    public static final int VERSION = 1;
    // 資料庫物件，固定的欄位變數
    private static SQLiteDatabase database;

    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String member = "CREATE  TABLE IF NOT EXISTS " +
                "member" +
                "(id integer  primary key autoincrement, " +
                "name varchar(60), " +
                "password varchar(60))";

        String data = "CREATE  TABLE IF NOT EXISTS " +
                "data" +
                "(id integer , " +
                "date date, " +
                "time varchar(10), " +
                "strategy nvarchar(60), " +
                "avg_score float)";

        String sleep_diary = "CREATE  TABLE IF NOT EXISTS " +
                "sleep_diary" +
                "(id integer , " +
                "date date, " +
                "sleep_time varchar(10), " +
                "wake_time varchar(10), " +
                "sleep_latency integer, " +
                "wake_times integer, " +
                "note nvarchar(300))";
        db.execSQL(member);    // 建立資料表
        db.execSQL(data);    // 建立資料表
        db.execSQL(sleep_diary);    // 建立資料表
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS member");
        onCreate(db);
    }
}
