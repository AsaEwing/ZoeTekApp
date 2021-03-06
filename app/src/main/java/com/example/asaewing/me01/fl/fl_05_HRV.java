package com.example.asaewing.me01.fl;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asaewing.me01.BLE_data.BLEService;
import com.example.asaewing.me01.Others.HiDBHelper;
import com.example.asaewing.me01.R;

import java.util.ArrayList;


public class fl_05_HRV extends RootFragment implements View.OnClickListener {

    //宣告血壓流速相關變數
    //private int E_HR;
    private double PTT = 1,ET,SLP;

    //宣告心率變異性相關變數
    private int E_HR,P_HR,E_Stress,P_Stress;
    private double E_SDNN,P_SDNN,E_LF_HF,P_LF_HF,RMSSD;

    double BP = 0;      //血壓
    double SBP = 0;     //收縮壓131
    double DBP = 0;     //舒張壓68

    double setSBP = 0;
    double setDBP = 0;

    double HB = 0;      //心跳78
    double Height = 1.77;
    double Weight = 52.5;
    double A_SBP = 0;
    double B_SBP = 0;
    double A_DBP = 0;
    double B_DBP = 0;
    double Con_q = 0;
    double Con_h = 0;

    //關閉掃描時間處理
    private Handler mHandler;
    //掃描間隔
    private static final long SCAN_PERIOD = 10000;
    //要求開啟藍牙
    private static final int REQUEST_ENABLE_BT = 1;
    //藍牙接口
    private BluetoothAdapter mBluetoothAdapter;
    //用來是否在掃描裝置
    private boolean mScanning;
    //藍牙裝置列表顯示
    private ListView listBLEDevice;
    //BLE列表接口
    private LeDeviceListAdapter mLeDeviceListAdapter;
    //用來顯示處理進度
    private ProgressDialog pd;
    //掃描狀態文字顯示
    private TextView tv_scan;
    //藍牙掃描彈出視窗
    //private AlertDialog showList;
    //已選擇的裝置名稱
    private String ble_device_name = "";
    //已選擇的裝置MAC位址
    private String ble_device_address = "";
    //開始掃描按鈕
    private Button btn_connect,btn_send;
    //藍牙掃描視窗
    private View scanView;
    //BLE連線服務
    private BLEService bleService;
    //BLE資料回傳識別文字
    public static final String MY_BROADCAST_TAG = "com.zoetek.app";
    //廣播封包接收
    private MyReceiver receiver;
    //廣播封包識別
    private IntentFilter filter;
    //宣告顯示資料用的文字框
    private TextView hello_tv;


    public fl_05_HRV() {
        // Required empty public constructor
    }

    public static fl_05_HRV newInstance() {
        fl_05_HRV fragment = new fl_05_HRV();

        return fragment;
    }

    //TODO----Data----
    private void getBP_Value(){
        Con_h = 0.2;
        setConValue();

        double tmp = 1/PTT/PTT;

        SBP = A_SBP*tmp+B_SBP;
        DBP = A_DBP*tmp+B_DBP;
    }

    private void setConValue(){
        Con_h = 0.2;

        double tmp = 0.18/0.7*Math.sqrt(Height)/Math.sqrt(PTT)+9.8*Con_h;

        double Con_q_SBP = setSBP/tmp;
        A_SBP = Con_q_SBP*0.18/0.7*Math.sqrt(Height)/Math.sqrt(PTT);
        B_SBP = Con_q_SBP*9.8*Con_h;

        double Con_q_DBP = setDBP/tmp;
        A_DBP = Con_q_DBP*0.18/0.7*Math.sqrt(Height)/Math.sqrt(PTT);
        B_DBP = Con_q_DBP*9.8*Con_h;
    }


    //TODO----生命週期----
    @Override
    public void onAttach(Context context) {
        TAG = getClass().getSimpleName();

        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rootView = inflater.inflate(R.layout.fl_03_zoetek, container, false);

        //檢查是否支援BLE
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getActivity(), "裝置不支援BLE", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        //檢查是否支援藍牙
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "裝置不支援藍牙", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        //初始化文字框
        hello_tv = (TextView)rootView.findViewById(R.id.tv_helloworld);


        btn_connect = (Button)rootView.findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(this);

        btn_send = (Button)rootView.findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);

        //綁定服務
        doBindService();
        receiver = new MyReceiver();
        filter = new IntentFilter();
        filter.addAction(MY_BROADCAST_TAG);

        Button bt_setting = (Button)rootView.findViewById(R.id.btn_setting_bp);
        assert bt_setting != null;
        bt_setting.setOnClickListener(this);

        A_SBP = getMainActivity().getDataManager().mInfoMap.IMgetFloat(HiDBHelper.KEY_Con_A_SBP);
        B_SBP = getMainActivity().getDataManager().mInfoMap.IMgetFloat(HiDBHelper.KEY_Con_B_SBP);
        A_DBP = getMainActivity().getDataManager().mInfoMap.IMgetFloat(HiDBHelper.KEY_Con_A_DBP);
        B_DBP = getMainActivity().getDataManager().mInfoMap.IMgetFloat(HiDBHelper.KEY_Con_B_DBP);

        Height = getMainActivity().getDataManager().mInfoMap.IMgetFloat(HiDBHelper.KEY_Height);
        Weight = getMainActivity().getDataManager().mInfoMap.IMgetFloat(HiDBHelper.KEY_Weight);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //判斷是否選擇開啟藍牙，選擇否則關閉程式
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            getActivity().finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        //詢問是否開啟藍牙功能
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mLeDeviceListAdapter!=null) {
            scanLeDevice(false);
            mLeDeviceListAdapter.clear();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    @Override
    public void onStop() {
        getMainActivity().getDataManager().saveDataBP(A_SBP,B_SBP,A_DBP,B_DBP);
        getMainActivity().getDataManager().saveDataNormal(Height,Weight);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //斷開BLE連線
        bleService.disconnect();
        //斷開綁定BLE服務
        getActivity().unbindService(serviceConnection);
    }

    private void showBleScanDialog(){
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mHandler = new Handler();

        //清除BLE列表
        mLeDeviceListAdapter.clear();
        //開始BLE掃描
        scanLeDevice(true);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog alertDialog = null;
        builder.setTitle("請選擇BLE裝置");
        builder.setView(R.layout.list_device);

        alertDialog = builder.create();
        assert alertDialog != null;
        final AlertDialog finalAlertDialog = alertDialog;

        alertDialog.show();


        //mLeDeviceListAdapter = new LeDeviceListAdapter();
        //mHandler = new Handler();

        listBLEDevice = (ListView) alertDialog.findViewById(R.id.listBLEDevice);
        //將BLE列表連結到BLE列表接口
        assert listBLEDevice != null;
        listBLEDevice.setAdapter(mLeDeviceListAdapter);
        //設定BLE列表點擊後要處理的工作

        listBLEDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //判斷使否還在掃描，如果還在掃描則取消掃描
                mHandler.removeCallbacksAndMessages(null);
                if(mScanning) scanLeDevice(false);
                //取消列表顯示
                finalAlertDialog.cancel();
                //顯示處理狀態
                pd = ProgressDialog.show(getActivity(), "", "裝置連線中");
                //取得已選擇裝置的相關資訊
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);

                //儲存選擇的裝置名稱與MAC位址
                ble_device_name = device.getName();
                ble_device_address = device.getAddress();

                //顯示選擇的裝置
                Toast.makeText(getActivity(),device.getName() + " " + device.getAddress(),Toast.LENGTH_LONG).show();

                //關閉顯示處理狀態
                //pd.dismiss();

                //連線至所選的BLE裝置
                bleService.connect(device);
            }
        });
        //取得掃描狀態TextView的Layout
        tv_scan = (TextView) alertDialog.findViewById(R.id.tv_scan);

        //設定BLE掃描狀態
        assert tv_scan != null;
        tv_scan.setText("中止掃描");
        //設定點擊後要處理的工作
        tv_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView)v;
                if(tv.getText().toString().equals("重新掃描裝置")){
                    tv_scan.setText("中止掃描");
                    //清除BLE裝置列表
                    mLeDeviceListAdapter.clear();
                    //更新BLE列表顯示
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    //掃描BLE裝置
                    scanLeDevice(true);
                } else {
                    tv_scan.setText("重新掃描裝置");
                    //中止掃描BLE裝置
                    scanLeDevice(false);
                }
            }
        });

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                //判斷使否還在掃描，如果還在掃描則取消掃描
                mHandler.removeCallbacksAndMessages(null);
                if(mScanning) scanLeDevice(false);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (pd != null) pd.dismiss();
                    }
                },3000);
            }
        });

////////////////////////////////////////////////////////////////
        /*
        //取得BLE掃描視窗Layout
        scanView = getActivity().getLayoutInflater().inflate(R.layout.list_device, null);
        //BLE列表接口初始化
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mHandler = new Handler();
        //BLE列表初始化
        listBLEDevice = (ListView) scanView.findViewById(R.id.listBLEDevice);
        //將BLE列表連結到BLE列表接口
        listBLEDevice.setAdapter(mLeDeviceListAdapter);
        //設定BLE列表點擊後要處理的工作
        listBLEDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //判斷使否還在掃描，如果還在掃描則取消掃描
                mHandler.removeCallbacksAndMessages(null);
                if(mScanning) scanLeDevice(false);
                //取消列表顯示
                showList.cancel();
                //顯示處理狀態
                pd = ProgressDialog.show(getActivity(), "", "裝置連線中");
                //取得已選擇裝置的相關資訊
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);

                //儲存選擇的裝置名稱與MAC位址
                ble_device_name = device.getName();
                ble_device_address = device.getAddress();

                //顯示選擇的裝置
                Toast.makeText(getActivity(),device.getName() + " " + device.getAddress(),Toast.LENGTH_LONG).show();

                //關閉顯示處理狀態
                //pd.dismiss();

                //連線至所選的BLE裝置
                bleService.connect(device);
            }
        });
        //取得掃描狀態TextView的Layout
        tv_scan = (TextView) scanView.findViewById(R.id.tv_scan);
        //設定點擊後要處理的工作
        tv_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView)v;
                if(tv.getText().toString().equals("重新掃描裝置")){
                    tv_scan.setText("中止掃描");
                    //清除BLE裝置列表
                    mLeDeviceListAdapter.clear();
                    //更新BLE列表顯示
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    //掃描BLE裝置
                    scanLeDevice(true);
                } else {
                    tv_scan.setText("重新掃描裝置");
                    //中止掃描BLE裝置
                    scanLeDevice(false);
                }
            }
        });
        //初始化BLE列表顯示Layout
        showList = new AlertDialog.Builder(
                getActivity()).setTitle("請選擇BLE裝置").setView(scanView).create();
        //設定關閉BLE掃描視窗後的工作
        showList.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                //判斷使否還在掃描，如果還在掃描則取消掃描
                mHandler.removeCallbacksAndMessages(null);
                if(mScanning) scanLeDevice(false);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                    }
                },3000);
            }
        });

        showList.show();*/
    }

    private void showSetBpDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog alertDialog = null;
        builder.setTitle("設定數值");
        builder.setView(R.layout.dialog_setting);

        builder.setPositiveButton("OK"
                ,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText ed_height = (EditText)((AlertDialog)dialog).findViewById(R.id.edit_Height);
                        EditText ed_SBP = (EditText)((AlertDialog)dialog).findViewById(R.id.edit_SBP);
                        EditText ed_DBP = (EditText)((AlertDialog)dialog).findViewById(R.id.edit_DBP);

                        assert ed_height != null;
                        if (ed_height.getText()!=null) {
                            Height = Double.valueOf(String.valueOf(ed_height.getText()));
                        }
                        Height = Height/100;
                        assert ed_SBP != null;
                        if (ed_SBP.getText()!=null) {
                            setSBP = Double.valueOf(String.valueOf(ed_SBP.getText()));
                        }
                        assert ed_DBP != null;
                        if (ed_DBP.getText()!=null) {
                            setDBP = Double.valueOf(String.valueOf(ed_DBP.getText()));
                        }

                        setConValue();
                    }
                });

        builder.setNegativeButton("Cancel"
                ,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //setBMI();
                        //MainActivity.fabMainClose();
                    }
                });

        alertDialog = builder.create();
        assert alertDialog != null;
        alertDialog.show();
        alertDialog.setCancelable(false);
    }

    //掃描BLE方法
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    tv_scan.setText("重新掃描裝置");
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            tv_scan.setText("重新掃描裝置");
        }
    }

    //綁定BLE服務
    private void doBindService(){
        Intent intent = new Intent(getActivity(), BLEService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    //BLE服務連接
    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService=((BLEService.LocalBinder)service).getService();
        }
    };

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.btn_setting_bp:
                showSetBpDialog();
                break;

            case R.id.btn_connect:


                showBleScanDialog();

                break;

            case R.id.btn_send:
                if(btn_send.getText().toString().equals("發送")) {
                    //發送menu.bp,指令
                    bleService.sendData("menu.bp,");
                    //修改顯示文字為「開始量測」
                    btn_send.setText("開始量測");
                } else {
                    if(btn_send.getText().toString().equals("停止量測")) {
                        //發送menu.bp,指令
                        bleService.sendData("menu.bp,");
                        //修改顯示文字為「開始量測」
                        btn_send.setText("開始量測");
                    } else {
                        //發送icon.bp,指令
                        bleService.sendData("icon.bp,");
                        //修改顯示文字為「停止量測」
                        btn_send.setText("停止量測");
                    }
                }
                break;
        }
    }

    //BLE裝置的接口
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflater;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflater = getActivity().getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflater.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
            } else {
                viewHolder.deviceName.setText("未知的裝置");
            }
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    //掃描到BLE裝置後的回傳方法
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mLeDeviceListAdapter.getCount()==0) {
                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                            } else {
                                boolean find = false;
                                for (int i = 0; i < mLeDeviceListAdapter.getCount(); i++) {
                                    if (mLeDeviceListAdapter.getDevice(i).equals(device)) {
                                        find = true;
                                        break;
                                    }
                                }
                                if(!find){
                                    mLeDeviceListAdapter.addDevice(device);
                                    mLeDeviceListAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
                }
            };

    private static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if(arg1.getStringExtra("Receiver")!=null) {
                //判斷ERR是否為3020
                if (arg1.getStringExtra("Receiver").equals("ERR=3020,")) {
                    //發送mode.hrv01,，可選模式30s、60s、120s分別為mode.hrv01,、mode.hrv02,、mode.hrv03,
                    bleService.sendData("mode.hrv01,");
                } else {
                    //以"="將文字切割，判斷資料為哪一個
                    switch (arg1.getStringExtra("Receiver").split("=")[0]) {
                        case "E_HR":
                            //以"="和","文字切割，將ECG HR資料部分取出並轉為Integer
                            E_HR = Integer.valueOf(arg1.getStringExtra("Receiver").split("=|,")[1]);
                            break;
                        case "P_HR":
                            //以"="和","文字切割，將PPG HR資料部分取出並轉為Integer
                            P_HR = Integer.valueOf(arg1.getStringExtra("Receiver").split("=|,")[1]);
                            break;
                        case "E_SD":
                            //以"="和","文字切割，將ECG SDNN資料部分取出並轉為Double
                            E_SDNN = Double.valueOf(arg1.getStringExtra("Receiver").split("=|,")[1]);
                            break;
                        case "P_SD":
                            //以"="和","文字切割，將PPG SDNN資料部分取出並轉為Double
                            P_SDNN = Double.valueOf(arg1.getStringExtra("Receiver").split("=|,")[1]);
                            break;
                        case "E_ANB":
                            //以"="和","文字切割，將ECG LF/HF資料部分取出並轉為Double
                            E_LF_HF = Double.valueOf(arg1.getStringExtra("Receiver").split("=|,")[1]);
                            break;
                        case "P_ANB":
                            //以"="和","文字切割，將PPG LF/HF資料部分取出並轉為Double
                            P_LF_HF = Double.valueOf(arg1.getStringExtra("Receiver").split("=|,")[1]);
                            break;
                        case "E_Stress":
                            //以"="和","文字切割，將ECG Stress資料部分取出並轉為Integer
                            E_Stress = Integer.valueOf(arg1.getStringExtra("Receiver").split("=|,")[1]);
                            break;
                        case "P_Stress":
                            //以"="和","文字切割，將PPG Stress資料部分取出並轉為Integer
                            P_Stress = Integer.valueOf(arg1.getStringExtra("Receiver").split("=|,")[1]);
                            break;
                        case "RMSSD":
                            //以"="和","文字切割，將RMSSD資料部分取出並轉為Double
                            RMSSD = Double.valueOf(arg1.getStringExtra("Receiver").split("=|,")[1]);
                            break;
                    }

                    getBP_Value();

                    //透過HelloWorld的文字框來顯示接收到的資料
                    String tmp;
                    tmp = "E_HR="+String.valueOf(E_HR)+"\n";    //
                    tmp += "PTT="+String.valueOf(PTT)+"\n";     //脈搏傳輸時間
                    tmp += "ET="+String.valueOf(ET)+"\n";       //
                    tmp += "SLP="+String.valueOf(SLP)+"\n";     //
                    tmp += "\n";
                    tmp += "SBP="+String.valueOf(SBP)+"\n";       //
                    tmp += "DBP="+String.valueOf(DBP)+"\n";     //
                    tmp += "\n";
                    tmp +="E_SDNN="+String.valueOf(E_SDNN)+"\n";
                    tmp +="E_LF/HF="+String.valueOf(E_LF_HF)+"\n";
                    tmp +="E_Stress="+String.valueOf(E_Stress)+"\n";
                    tmp +="P_HR="+String.valueOf(P_HR)+"\n";
                    tmp +="P_SDNN="+String.valueOf(P_SDNN)+"\n";
                    tmp +="P_LF/HF="+String.valueOf(P_LF_HF)+"\n";
                    tmp +="P_Stress="+String.valueOf(P_Stress)+"\n";
                    tmp +="RMSSD="+String.valueOf(RMSSD)+"\n";
                    hello_tv.setText(tmp);
                }
            }
        }
    }
}
