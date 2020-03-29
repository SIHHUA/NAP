package com.isleepbetter.isleepbetter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.charts.BarChart;
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
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private TextView mConnectionState,mDataField;
    private String mDeviceName,mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private boolean mConnected = false;
    private Boolean flag = false,alarm_flag = false;
;
    private Button start_alarm,stop_alarm;
    private Integer set_hour,set_minute;
    private static Ringtone ring;
    private TimePickerDialog timePickerDialog;

    private BarChart bar_chart;
    private List<String> ylabel = new ArrayList<>(Arrays.asList("", "Deep", "Light", "Wake"));
    private ArrayList<BarEntry> entries2 = new ArrayList<>();
    private List<Integer> data = new ArrayList<>(Arrays.asList(0,0,0,0,0,1,1,2,2,3,3,3,3,3,3,3,3));
    private List<Integer> color = new ArrayList<>();
    private int Window = 10;

    private Switch switch_btn;
    private ImageView people_img,clock_img;
    private GifImageView people_gif,clock_gif;

    private String ble_data; //ble data catch from handband
    private double [][] second_feature=new double[12][30]; //matrix to save data in 30 second
    private double [] epoch_feature=new double[24]; // matrix to predict stage
    private double [] feat_array1 =new double[24],feat_array2 =new double[24];
    private int second=0;   //0~29 count second
    private int stage=0;    //result
    private int epoch_count=0;  //current epoch num
    private int alarm_count=0;  //if value higher than 20 set alarm on
    private boolean system_on =true;//is ble on or not
    private  boolean alarm_on =false; //set alarm on or not

    private Timer timer;
    private Integer index;

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
        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                Log.d("conect_status","connect");
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

        init();

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
//        start = (Button) findViewById(R.id.start_chart);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
//        start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mGattCharacteristics != null) {
//                    //write 進入快閃
//                    try {
//                        Thread.sleep(1000); //1000為1秒
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    mBluetoothLeService.writeCharacteristic();
//                    //notify 收資料
//                    mBluetoothLeService.setCharacteristicNotification();
//                    flag = true;
//                }
//                start.setEnabled(false);
//            }
//
//        });

        start_alarm = findViewById(R.id.button6);
        start_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTimePickerDialog(false);
                alarm_flag = true;
                if(!flag) {
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
                        flag = true;
                    }
                }
                people_img.setVisibility(View.INVISIBLE);
                people_gif.setVisibility(View.VISIBLE);

            }
        });

        stop_alarm = findViewById(R.id.button7);
        stop_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ring.stop();
                alarm_flag = false;
                flag = false;
                unbindService(mServiceConnection);
                mBluetoothLeService = null;
                people_gif.setVisibility(View.INVISIBLE);
                people_img.setVisibility(View.VISIBLE);
                clock_img.setVisibility(View.VISIBLE);
                clock_gif.setVisibility(View.INVISIBLE);
            }
        });

        switch_btn = findViewById(R.id.switch1);
        switch_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    people_img.setImageResource(R.drawable.girl_wake);
                    people_gif.setImageResource(R.drawable.girl_sleep);
                }
                else
                {
                    people_img.setImageResource(R.drawable.boy_wake);
                    people_gif.setImageResource(R.drawable.boy_sleep);
                }
            }
        });

        index = 0;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                DeviceControlActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        get_data(index, data.get(index));
                        index++;
                        if(index==data.size())
                        {
                            timer.cancel();
                        }
                    }
                });

            }
        }, 0,1000);
    }

    private void init()
    {
        people_img = findViewById(R.id.imageView7);
        clock_img = findViewById(R.id.imageView8);
        people_gif = findViewById(R.id.activity_gif_giv);
        clock_gif = findViewById(R.id.activity_gif_giv1);
        people_img.setImageResource(R.drawable.boy_wake);
        people_gif.setImageResource(R.drawable.boy_sleep);

        bar_chart = findViewById(R.id.bar_chart);
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
        bar_chart.getXAxis().setEnabled(false);
        bar_chart.getDescription().setEnabled(false);// disable description right-down
        bar_chart.getLegend().setEnabled(false); // disable description left-down
        bar_chart.setTouchEnabled(false);

        XAxis bar_xAxis = bar_chart.getXAxis();
        bar_xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return ylabel.get((int) value);
            }
        };
        bar_chart.getAxisLeft().setValueFormatter(formatter);
        CustomBarChartRender barChartRender = new CustomBarChartRender(bar_chart,bar_chart.getAnimator(), bar_chart.getViewPortHandler());
        barChartRender.setRadius(10);
        bar_chart.setRenderer(barChartRender);
    }

    private void openTimePickerDialog(boolean is24r){
        Calendar calendar = Calendar.getInstance();
        timePickerDialog = new TimePickerDialog(
                DeviceControlActivity.this,
                onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                is24r);
        timePickerDialog.show();
    }

    TimePickerDialog.OnTimeSetListener onTimeSetListener
        = new TimePickerDialog.OnTimeSetListener(){
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        set_hour = hourOfDay;
        set_minute = minute;
    }};


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

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void displayData(String nft_data) {
        Log.d("testdata",nft_data);

        Calendar mCal = Calendar.getInstance();
        CharSequence s = DateFormat.format("kk:mm:ss", mCal.getTime());    // kk:24小時制, hh:12小時制
        String time = s.toString();
        String hour = time.split(":")[0];
        String minute = time.split(":")[1];
        String second_t = time.split(":")[2];

        if(alarm_flag && hour.equals(String.valueOf(set_hour)) && minute.equals(String.valueOf(set_minute)) && second_t.equals("00"))
        {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            ring = RingtoneManager.getRingtone(this.getApplicationContext(), notification);
            ring.setLooping(true);
            ring.play();
            clock_img.setVisibility(View.INVISIBLE);
            clock_gif.setVisibility(View.VISIBLE);
        }

        if (!nft_data.equals("") && flag && false) {
            epoch_count++;
            /////////////////////////////////////
            //catch ble data per second
            String [] str_feature=ble_data.split(",");
            double TP1= Double.valueOf(str_feature[5]);
            double TP2= Double.valueOf(str_feature[12]);
            for (int sub=0 ; sub<6 ;++ sub){
                second_feature[sub][second]=Double.valueOf(str_feature[sub+1]);
                second_feature[6+sub][second]=Double.valueOf(str_feature[sub+8]);
                if (sub != 4){
                    second_feature[sub][second]   /=TP1;
                    second_feature[6+sub][second] /=TP2;
                }
            }
            ///////////////////////////////////////////
            //if count to 30 second go to transfrom second feature data into epoch feature data
            if (second<29){
                ++second;
            }
            else{
                second=0;
                for (int featnum=0;featnum<6;++featnum){
                    feat_array1=second_feature[featnum];
                    feat_array2=second_feature[featnum+6];
                    epoch_feature[4*(featnum)-4]=mean(feat_array1);
                    epoch_feature[4*(featnum)-3]=calculateSD(feat_array1);
                    epoch_feature[4*(featnum)-2]=mean(feat_array2);
                    epoch_feature[4*(featnum)-1]=calculateSD(feat_array2);
                }
                /////////////////////////////////////////
                //pred stage 0->wake 1->light sleep 3->deep sleep
                stage=pred_stage_W(epoch_feature);
                if (stage==1){
                    stage=pred_stage_N3(epoch_feature);
                    ++alarm_count;
                    if ((stage==3 && epoch_count>=20) || alarm_count>=20){
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        ring = RingtoneManager.getRingtone(this.getApplicationContext(), notification);
                        ring.setLooping(true);
                        ring.play();
                        clock_img.setVisibility(View.INVISIBLE);
                        clock_gif.setVisibility(View.VISIBLE);
                    }
                }else{
                    alarm_count=0;
                }
            }

        }

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

    public void get_data(final Integer index, final Integer data) {

        bar_chart.clear();

        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                // update linechart
                switch (data) {
                    case 0:
                        color.add(Color.rgb(168,230,250));
                        entries2.add(new BarEntry( index, 3));
                        break;
                    case 1:
                        color.add(Color.rgb(250,219,216));
                        entries2.add(new BarEntry( index, 2));
                        break;
                    case 2:
                        color.add(Color.rgb(250,219,216));
                        entries2.add(new BarEntry( index, 2));
                        break;
                    case 3:
                        color.add(Color.rgb(252, 243, 207));
                        entries2.add(new BarEntry( index, 1));
                        break;
                    default:
                        break;
                }
                if(entries2.size() > Window) {
                    entries2.remove(0);
                    color.remove(0);
                }
                BarDataSet dataSet = new BarDataSet(entries2, "");
                dataSet.setDrawValues(false); // disable value display
                BarData lineData = new BarData(dataSet);
                lineData.setBarWidth(0.9f);
                dataSet.setColors(color);

                bar_chart.setData(lineData);
                bar_chart.notifyDataSetChanged(); // let the chart know it's data changed
                bar_chart.invalidate(); // refresh

            }
        }));
    }


    public static int pred_stage_W(double[] feat) {
        if (feat[0] < 0.277887){
            if (feat[23] < 0.785843){
                return 0;
            }
            else{
                if (feat[16] <50.9515)
                    return 1;
                else
                    return 0;
            }

        }
        else{
            if (feat[21] < 1.8613){
                if(feat[10]<0.263237){
                    return 1;
                }else{
                    return 0;
                }

            }else{
                return 0;
            }
        }

    }

    public static int pred_stage_N3(double[] feat) {
        if (feat[20] < 8.53719){
            if (feat[9] < 0.0471531){
                return 3;
            }else{
                if (feat[12] <0.068237){
                    return 3;
                }else{
                    return 1;
                }

            }

        }else{
            return 1;
        }

    }

    public static double mean(double[] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }


    public static double calculateSD(double numArray[]){
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;
        for(double num : numArray) {
            sum += num;
        }
        double mean = sum/length;
        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        return Math.sqrt(standardDeviation/length);
    }
}
