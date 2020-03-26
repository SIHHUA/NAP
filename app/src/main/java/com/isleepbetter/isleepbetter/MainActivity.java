package com.isleepbetter.isleepbetter;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Button connectbtn;
    private ImageView alarm_clock,nft,sleep_record;
    private NavigationView nav;
    private Button done_edit;
    private EditText name_edit;
    private SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.Open,R.string.Close);
        drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        alarm_clock = findViewById(R.id.alarm_clock);
        nft = findViewById(R.id.nft);
        sleep_record = findViewById(R.id.sleep_record);
        nav = findViewById(R.id.drawer_nav);
        nav.setNavigationItemSelectedListener(this);

        alarm_clock.setAlpha(0.6f);
        nft.setAlpha(0.6f);
        sleep_record.setAlpha(0.6f);

        pref = getSharedPreferences("name", MODE_PRIVATE);
        alarm_clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("status","1");

                startActivity(new Intent(MainActivity.this, AlarmclockActivity.class));
                finish();
            }
        });

        nft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("status","2");

                startActivity(new Intent(MainActivity.this, NFTActivity.class));
                finish();
            }
        });

        sleep_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, sleeprecordActivity.class));
                finish();

                Log.d("status","3");//connectBLE();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    public void connectBLE()
    {
        startActivity(new Intent(MainActivity.this, ConnectActivity.class));
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);

        switch (menuItem.getItemId()) {
            case R.id.username:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.view_editname, null);
                builder.setView(dialogView);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();
                done_edit = dialogView.findViewById(R.id.done);
                done_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        name_edit = dialogView.findViewById(R.id.username_edit);
                        SharedPreferences pref = getSharedPreferences("name", MODE_PRIVATE);
                        pref.edit()
                                .putString("user_name", name_edit.getText().toString())
                                .commit();
                        alertDialog.cancel();
                    }
                });

            case R.id.trainingDiary:
                // do you click actions for the second selection
                break;
            case R.id.logout:
                // do you click actions for the third selection
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
        return super.onKeyDown(keyCode, event);
    }

    private void writeToFile(File fout, String data) {
        FileOutputStream osw = null;
        try {
            osw = new FileOutputStream(fout);
            osw.write(data.getBytes());
            osw.flush();
        } catch (Exception e) {
            ;
        } finally {
            try {
                osw.close();
            } catch (Exception e) {
                ;
            }
        }
    }
}
