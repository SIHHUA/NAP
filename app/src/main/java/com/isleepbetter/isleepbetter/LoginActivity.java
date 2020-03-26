package com.isleepbetter.isleepbetter;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private String username,password;

    private EditText username_edit,password_edit;

    private static String DATABASE_TABLE = "member";
    private SQLiteDatabase db;
    private MyDBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 建立SQLiteOpenHelper物件
        dbHelper = new MyDBHelper(this);
        db = dbHelper.getWritableDatabase(); // 開啟資料庫

        Button login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                username_edit = findViewById(R.id.editText2);
                password_edit = findViewById(R.id.editText3);
                username = username_edit.getText().toString();
                password = password_edit.getText().toString();

                if(username.equals("") || password.equals("") ) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("iSleepBetter")
                            .setMessage(getResources().getString(R.string.login_blank)).show();
                }
                else
                    search_member(username,password);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close(); // 關閉資料庫
    }

    public void search_member(final String account, final String password){
        Cursor c=db.rawQuery("SELECT * FROM member where name = '"+account + "'",null);
        int check = 0;
        if (c.getCount()!=0)
        {
            c.moveToFirst();
            for(int i = 0;i<c.getCount();i++) {
                if (password.equals(c.getString(2))) {
                    //跳轉業面
                    SharedPreferences pref = getSharedPreferences("name", MODE_PRIVATE);
                    pref.edit()
                            .putString("id", c.getString(0))
                            .commit();
                    pref.edit()
                            .putString("user_name", c.getString(1))
                            .commit();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                    check = 1;
                    break;
                }
                c.moveToNext();
            }
            if(check == 0) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("iSleepBetter")
                        .setMessage(getResources().getString(R.string.password_wrong)).show();
            }
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage(getResources().getString(R.string.register_que));
            builder.setTitle(getResources().getString(R.string.register));
            builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ContentValues values = new ContentValues();
                    values.put("name",account);
                    values.put("password",password);
                    db.insert("member",null,values);
                    search_member(account,password);
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }

    }
}
