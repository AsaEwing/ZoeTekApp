package com.example.asaewing.me01.Others;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.asaewing.me01.MainActivity;

public class DataManager implements Parcelable {

    private MainActivity mContext;
    private String mTAG;
    public InfoMap mInfoMap;
    public HiDBHelper helper;

    public DataManager(MainActivity context, 
                       String TAG){
        //, InfoMap infoMap, HiDBHelper hiDBHelper
        mContext = context;
        mTAG = TAG+" , DataManager";
        helper = new HiDBHelper(context.getApplicationContext());
        mInfoMap = new InfoMap();
    }

    public void onRestart() {
        helper = new HiDBHelper(mContext.getApplicationContext());
        updateData();
        Log.d(mTAG,"**Yes_onRestart**");
    }

    public void onDestroy() {
        saveDataSP();
        helper.close();
    }

    public boolean updateData(){
        Log.d(mTAG, "**" + mTAG + "**upDialog");

        final int[] CA_Count = {0};
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                double tmpDouble = 0;

                Cursor cursorAll = helper.HiSelect();
                CA_Count[0] = cursorAll.getCount();
                Log.d(mTAG,"**InfoMap**"+cursorAll.getCount());
                if (CA_Count[0] >0) {
                    cursorAll.moveToFirst();
                    tmpDouble = Double.parseDouble(
                            cursorAll.getString(cursorAll.getColumnIndex(HiDBHelper.KEY_Height)));
                    mInfoMap.IMput(HiDBHelper.KEY_Height, tmpDouble);
                    tmpDouble = Double.parseDouble(
                            cursorAll.getString(cursorAll.getColumnIndex(HiDBHelper.KEY_Weight)));
                    mInfoMap.IMput(HiDBHelper.KEY_Weight, tmpDouble);

                    mInfoMap.IMput(HiDBHelper.KEY_Hi_Index
                            , cursorAll.getInt(cursorAll.getColumnIndex(HiDBHelper.KEY_Hi_Index)));
                    tmpDouble = Double.parseDouble(
                            cursorAll.getString(cursorAll.getColumnIndex(HiDBHelper.KEY_Con_A_SBP)));
                    mInfoMap.IMput(HiDBHelper.KEY_Con_A_SBP, tmpDouble);
                    tmpDouble = Double.parseDouble(
                            cursorAll.getString(cursorAll.getColumnIndex(HiDBHelper.KEY_Con_B_SBP)));
                    mInfoMap.IMput(HiDBHelper.KEY_Con_B_SBP, tmpDouble);

                    tmpDouble = Double.parseDouble(
                            cursorAll.getString(cursorAll.getColumnIndex(HiDBHelper.KEY_Con_A_DBP)));
                    mInfoMap.IMput(HiDBHelper.KEY_Con_A_DBP, tmpDouble);
                    tmpDouble = Double.parseDouble(
                            cursorAll.getString(cursorAll.getColumnIndex(HiDBHelper.KEY_Con_B_DBP)));
                    mInfoMap.IMput(HiDBHelper.KEY_Con_B_DBP, tmpDouble);

                } else {
                    mInfoMap.IMput(HiDBHelper.KEY_Hi_Index, 1);
                    mInfoMap.IMput(HiDBHelper.KEY_Height, 1.77);
                    mInfoMap.IMput(HiDBHelper.KEY_Weight, 53);
                    mInfoMap.IMput(HiDBHelper.KEY_Con_A_SBP, 0);
                    mInfoMap.IMput(HiDBHelper.KEY_Con_B_SBP, 0);
                    mInfoMap.IMput(HiDBHelper.KEY_Con_A_DBP, 0);
                    mInfoMap.IMput(HiDBHelper.KEY_Con_B_DBP, 0);

                    ContentValues values = new ContentValues();
                    values.put(HiDBHelper.KEY_Hi_Index, 1);
                    values.put(HiDBHelper.KEY_Height, 0);
                    values.put(HiDBHelper.KEY_Weight, 0);
                    values.put(HiDBHelper.KEY_Con_A_SBP, 0);
                    values.put(HiDBHelper.KEY_Con_B_SBP, 0);
                    values.put(HiDBHelper.KEY_Con_A_DBP, 0);
                    values.put(HiDBHelper.KEY_Con_B_DBP, 0);

                    helper.HiInsert(values);
                }
                cursorAll.close();
            }
        });

        thread.start();

        try {
            thread.join();
            Log.d(mTAG,"**Thread**join**");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (thread.isAlive()){
            Log.d(mTAG,"**Alive**");

        }

        return true;
    }

    public void saveDataSP() {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {

                ContentValues values = new ContentValues();
                values.put(HiDBHelper.KEY_Height,mInfoMap.IMgetBoolean(HiDBHelper.KEY_Height));
                values.put(HiDBHelper.KEY_Weight,mInfoMap.IMgetBoolean(HiDBHelper.KEY_Weight));
                values.put(HiDBHelper.KEY_Con_A_SBP,mInfoMap.IMgetBoolean(HiDBHelper.KEY_Con_A_SBP));
                values.put(HiDBHelper.KEY_Con_B_SBP,mInfoMap.IMgetBoolean(HiDBHelper.KEY_Con_B_SBP));
                values.put(HiDBHelper.KEY_Con_A_DBP,mInfoMap.IMgetBoolean(HiDBHelper.KEY_Con_A_DBP));
                values.put(HiDBHelper.KEY_Con_B_DBP,mInfoMap.IMgetBoolean(HiDBHelper.KEY_Con_B_DBP));

                helper.HiUpdate(mInfoMap.IMgetInt(HiDBHelper.KEY_Hi_Index),values);
            }
        });
        thread.start();
        try {
            thread.join();
            Log.d(mTAG,"**Thread2**join**");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (thread.isAlive()){
            Log.d(mTAG,"**Alive2**");
            try {
                this.wait(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Cursor cursor = helper.HiSelect();
        cursor.moveToFirst();
        int ii=0,jj=0;
        Log.d(mTAG,"**Thread**cursor.getCount()**"+cursor.getCount()+"**"+cursor.getColumnCount());
        if (cursor.getCount()>0){
            for (ii=0;ii<cursor.getColumnCount();ii++) {
                Log.d(mTAG,"**AliveNo2**"+ii+"**"+cursor.getColumnName(ii)+"***"+cursor.getString(ii));
                if (cursor.getString(ii).length()==0) jj++;
            }
        }

        cursor.close();
    }

    public void saveDataBP(double SBP_A,double SBP_B,double DBP_A,double DBP_B){
        mInfoMap.IMput(HiDBHelper.KEY_Con_A_SBP, SBP_A);
        mInfoMap.IMput(HiDBHelper.KEY_Con_B_SBP, SBP_B);
        mInfoMap.IMput(HiDBHelper.KEY_Con_A_DBP, DBP_A);
        mInfoMap.IMput(HiDBHelper.KEY_Con_B_DBP, DBP_B);
    }

    public void saveDataNormal(double height,double weight){
        mInfoMap.IMput(HiDBHelper.KEY_Height, height);
        mInfoMap.IMput(HiDBHelper.KEY_Weight, weight);
    }

    public static final Creator<DataManager> CREATOR
            = new Creator<DataManager>() {
        public DataManager createFromParcel(Parcel in) {
            return new DataManager(in);
        }

        public DataManager[] newArray(int size) {
            return new DataManager[size];
        }
    };

    private DataManager(Parcel in) {
        //mData = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
