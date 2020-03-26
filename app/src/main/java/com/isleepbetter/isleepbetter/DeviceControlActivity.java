package com.isleepbetter.isleepbetter;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private LineChart line_chart;
    private HorizontalBarChart bar_chart;
    private Boolean flag = true;
    private Button start, stop, test;
    private int count = 1, times = 0;
    private List<Entry> entries2 = new ArrayList<>();
    private String save_score = "";
    private Float score, avg_score = 0.0f, total_score = 0.0f;
    private Integer count_num;
    private Handler handler;
    private Boolean check_channel = true;
    private Integer training_time = 370;
//    private List<String> countdown_str = new ArrayList<>();

    private RadioGroup radiogroup;
    private TextView score_text,strategy_txt;
    private Button done;
    private String id,strategy_text;
    private EditText strategy;
    private List<String> strategy_list = new ArrayList<>();


    private SQLiteDatabase db;
    private MyDBHelper dbHelper;

    private TextView mConnectionState, countdown;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGatt mBluetoothGatt;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private String a[],countdown_str;
    private AlertDialog dialog;

    private ImageView battery_img;
    private TextView battery_txt;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                Log.d("conect_status","connect");
                start.setVisibility(Button.VISIBLE);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                Log.d("conect_status","disconnected");
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        dbHelper = new MyDBHelper(this);
        db = dbHelper.getWritableDatabase(); // 開啟資料庫

        final Intent intent = getIntent();
        mDeviceName = getSharedPreferences("name", MODE_PRIVATE)
                .getString("device_name", "");
        mDeviceAddress = getSharedPreferences("name", MODE_PRIVATE)
                .getString("device_address", "");

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        start = (Button) findViewById(R.id.start_chart);
        line_chart = (LineChart) findViewById(R.id.line_chart);
        bar_chart = (HorizontalBarChart) findViewById(R.id.bar_chart);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = true;

                Log.d("channel_status",check_channel+"");
                if (mGattCharacteristics != null) {
                    //write 進入快閃

                    try {
                        Thread.sleep(1000); //1000為1秒
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mBluetoothLeService.writeCharacteristic();


                    //notify 收資料
                    mBluetoothLeService.setCharacteristicNotification();
                }
                start.setVisibility(Button.INVISIBLE);

            }

        });

        start.setVisibility(Button.INVISIBLE);

        battery_img = findViewById(R.id.battery_img);
        battery_txt = findViewById(R.id.battery_txt);

        // bar_chart configuration
        bar_chart.setDrawBarShadow(true);
        bar_chart.getAxisRight().setAxisMinValue(0.0f); // set x-axis min-value and max-value
        bar_chart.getAxisRight().setAxisMaximum(100.0f);
        bar_chart.getAxisLeft().setAxisMinValue(0.0f);
        bar_chart.getAxisLeft().setAxisMaximum(100.0f);
        bar_chart.getAxisLeft().setEnabled(false);
        bar_chart.getAxisRight().setEnabled(false);
        bar_chart.getDescription().setEnabled(false); // disable description right-down
        bar_chart.getLegend().setEnabled(false); // disable description left-down
        bar_chart.getXAxis().setEnabled(false);
        bar_chart.setTouchEnabled(false);
        // bar_chart configuration end

        // line_chart configuration
        line_chart.getAxisRight().setAxisMinValue(0.0f); // set x-axis min-value and max-value
        line_chart.getAxisRight().setAxisMaximum(100.0f);
        line_chart.getAxisRight().setEnabled(false);
        line_chart.getAxisLeft().setAxisMinValue(0.0f);
        line_chart.getAxisLeft().setAxisMaximum(100.0f);
        line_chart.getDescription().setEnabled(false);// disable description right-down
        line_chart.getLegend().setEnabled(false); // disable description left-down
        XAxis line_xAxis = line_chart.getXAxis();
        line_xAxis.setAxisMinimum(0.0f);
        line_xAxis.setAxisMaximum(360.0f);
        line_chart.getAxisLeft().setEnabled(false);
        line_chart.getAxisRight().setEnabled(false);
        line_xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        line_chart.setTouchEnabled(false);
        // line_chart configuration end
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        dialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String nft_data) {
        Log.d("testdata",nft_data);
//        if (!nft_data.equals(""))
//        {
//            String[] data1 = nft_data.split(",");
//            Log.d("testdata",data1[0]+"   666   "+data1[1]);
//        }




        if (!nft_data.equals("")) {
            battery_txt.setVisibility(TextView.VISIBLE);
            String[] data = nft_data.split(",");
            mDataField.setText(data[0]);
            Log.d("testdata",data[0]+"      "+data[1]+(Float.parseFloat(data[1])/4.2)*100);
            if(flag) {
                if(Float.parseFloat(data[1]) > 3.6) // green
                {
                    battery_img.setImageResource(R.drawable.full_battery);
                    battery_txt.setText((Float.parseFloat(data[1])/4.2)*100 + "");
                }
                else if(Float.parseFloat(data[1]) < 3.5) // red
                {
                    battery_img.setImageResource(R.drawable.low_battery);
                    battery_txt.setText((Float.parseFloat(data[1])/4.2)*100 + "");
                }
                else // orange
                {
                    battery_img.setImageResource(R.drawable.medium_battery);
                    battery_txt.setText((Float.parseFloat(data[1])/4.2)*100 + "");
                }
                Integer battery_power = (int)((Float.parseFloat(data[1])/4.2)*100);
                if (battery_power > 100)
                {
                    battery_txt.setText("100%");
                }
                else
                {
                    battery_txt.setText(battery_power + "%");
                    Log.d("testdata",battery_power + "%");
                }
                if (!nft_data.isEmpty() && times < training_time) {
                    score = Float.parseFloat(data[0]);
                    get_data();
                    if (times > 9) {
                        String tmp  =String.valueOf(times-9) +","+ Float.toString(score) + "\n";
                        save_score = save_score + tmp;
                        total_score = (total_score + score);
                    }
                    times = times + 1;
                } else if (times == training_time) {
                    mBluetoothLeService.writeStopCharacteristic();
                    avg_score = (total_score / (training_time-10));
                    times++;
                    AlertDialog.Builder builder = new AlertDialog.Builder(DeviceControlActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialogView = inflater.inflate(R.layout.view_nft_strategy, null);

                    radiogroup = dialogView.findViewById(R.id.radioGroup);
                    score_text = dialogView.findViewById(R.id.score);
                    strategy_txt = dialogView.findViewById(R.id.textView21);
                    strategy = dialogView.findViewById(R.id.strategy);
                    score_text.setText(avg_score.toString());

                    id = getSharedPreferences("name", MODE_PRIVATE)
                                    .getString("id", "");

                    Cursor c=db.rawQuery("SELECT * FROM data where id = '"+id + "'  GROUP BY strategy",null);
                    c.moveToFirst();
                    int i = 0;
                    if(c.getCount() == 0)
                    {
                        strategy_list.add(getResources().getString(R.string.other));
                        //radioButton
                        RadioButton radioButton = new RadioButton(getApplicationContext());
                        radioButton.setPadding(100,0,0,0);
                        radioButton.setText(getResources().getString(R.string.other));
                        radioButton.setTextColor(Color.rgb(0,0,0));
                        //必须有ID，否则默认选中的选项会一直是选中状态
                        radioButton.setId(i);
                        radioButton.setChecked(true);
                        //layoutParams 设置margin值
                        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        if (i!=0){
                            layoutParams.setMargins(50,0,0,0);
                        }else {
                            layoutParams.setMargins(50,0,0,0);
                        }
                        //注意这里addView()里传入layoutParams
                        radiogroup.addView(radioButton,layoutParams);
                    }
                    else
                    {
                        int fail = 0;
                        for (i = 0; i < c.getCount() + 1; i++) {
                            int check = 0;
                            RadioButton radioButton = new RadioButton(getApplicationContext());
                            if(i == c.getCount())
                            {
                                strategy_list.add(getResources().getString(R.string.other));
                                radioButton.setText(getResources().getString(R.string.other));
                                radioButton.setChecked(true);
                                check =1;
                            }
                            else if(!c.getString(3).equals("")){

                                strategy_list.add(c.getString(3).toString());
                                radioButton.setText(c.getString(3));
                                check =1;

                            }
                            else
                                fail = fail+1;
                            if(check == 1) {
                                //radioButton
                                radioButton.setPadding(100, 0, 0, 0);
                                radioButton.setTextColor(Color.rgb(0, 0, 0));
                                //必须有ID，否则默认选中的选项会一直是选中状态
                                radioButton.setId(i-fail);

                                //layoutParams 设置margin值
                                RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT);
                                if (i != 0) {
                                    layoutParams.setMargins(50, 0, 0, 0);
                                } else {
                                    layoutParams.setMargins(50, 0, 0, 0);
                                }
                                //注意这里addView()里传入layoutParams
                                radiogroup.addView(radioButton, layoutParams);
                            }
                            c.moveToNext();

                        }
                    }
                    builder.setView(dialogView);
                    builder.setCancelable(false);
                    dialog = builder.create();
                    dialog.show();

//                    builder.create();
//                    builder.show();

                    radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int i) {

                            if(strategy_list.get(i).equals(getResources().getString(R.string.other)))
                            {
                                strategy_txt.setVisibility(TextView.VISIBLE);
                                strategy.setVisibility(EditText.VISIBLE);
                            }
                            else{
                                strategy_txt.setVisibility(TextView.INVISIBLE);
                                strategy.setVisibility(EditText.INVISIBLE);
                            }
                        }
                    });

                    done = dialogView.findViewById(R.id.done);
                    done.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                            Date date = new Date();
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                            Date time = new Date();

                            if(strategy_txt.getVisibility() == TextView.VISIBLE){
                                strategy_text = strategy.getText().toString();
                            }
                            else
                            {
                                strategy_text = strategy_list.get(radiogroup.getCheckedRadioButtonId());
                            }

                            //存入資料庫
                            ContentValues values = new ContentValues();
                            values.put("id", id);
                            values.put("date", dateFormat.format(date));
                            values.put("time", timeFormat.format(time));
                            values.put("strategy", strategy_text);
                            values.put("avg_score", avg_score);
                            Log.d("strategy_t",strategy_text+ "   "+avg_score);
                            db.insert("data", null, values);
                            mBluetoothLeService.disconnect();

                            //寫入手機
                            SimpleDateFormat dateFormat_1 = new SimpleDateFormat("yyyyMMddHHmmss");
                            Date day = new Date();
                            String filename = id+"_"+dateFormat_1.format(day)+".txt";
                            File dir = getApplicationContext().getFilesDir();
                            save_score = strategy_text + '\n' + avg_score + '\n' + save_score;
                            File outFile = new File(dir, filename);
                            writeToFile(outFile, save_score);

                            startActivity(new Intent(DeviceControlActivity.this, NFTdisplayActivity.class));
                            finish();
                        }
                    });
                }
            }
        }
        Log.d("testdata","err");

    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void get_data() {
        countdown = findViewById(R.id.countdown);
        line_chart.clear();
        bar_chart.clear();
        a = getResources().getStringArray(R.array.countdown);

        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                count_num = times;
                if (count_num < 10)  //倒數
                {
                    countdown_str = a[count_num];
                    countdown.setVisibility(TextView.VISIBLE);
                    List<BarEntry> entries = new ArrayList<>();
                    countdown.setText(getResources().getString(R.string.countdown_1)+" "+countdown_str+" "+getResources().getString(R.string.countdown_2));
                    entries.add(new BarEntry(0f, 100 - (count_num * 10)));
                    Log.d("score", count_num + "   " + (10 - count_num) + "    " + (100 - (count_num * 10)) + "");
                    BarDataSet set = new BarDataSet(entries, "BarDataSet");
                    set.setColor(Color.rgb(253, 201, 203));
                    set.setBarShadowColor(Color.rgb(187, 213, 236));
                    set.setDrawValues(false);
                    BarData data = new BarData(set);
                    data.setBarWidth(1.0f);
                    bar_chart.setData(data);
                    bar_chart.invalidate();
                } else {
                    countdown.setVisibility(TextView.INVISIBLE);
                    line_chart.setVisibility(Chart.VISIBLE);
                    //get data and set data here --- barchart
                    List<BarEntry> entries = new ArrayList<>();
                    entries.add(new BarEntry(0f, score));
                    BarDataSet set = new BarDataSet(entries, "BarDataSet");
                    set.setColor(Color.rgb(253, 201, 203));
                    set.setBarShadowColor(Color.rgb(187, 213, 236));
                    set.setDrawValues(false);
                    BarData data = new BarData(set);
                    data.setBarWidth(1.0f);
                    bar_chart.setData(data);
                    bar_chart.invalidate();
                    //end

                    // update linechart
                    entries2.add(new Entry(times - 10, score));
                    LineDataSet dataSet = new LineDataSet(entries2, "");
                    LineData lineData = new LineData(dataSet);
                    dataSet.setDrawCircles(false); // disable point(circle)
                    dataSet.setDrawValues(false); // disable value display
                    line_chart.setData(lineData);
                    line_chart.invalidate(); // refresh


                }

            }
        }));
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
