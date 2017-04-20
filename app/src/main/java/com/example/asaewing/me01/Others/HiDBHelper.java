package com.example.asaewing.me01.Others;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.asaewing.me01.R;


public class HiDBHelper extends SQLiteOpenHelper{

    private final static String DATABASE_NAME = "hi_database";
    private final static int DATABASE_VERSION = 1;
    private SQLiteDatabase database;
    private Context context;

    //HiApp Info
    public final static String TABLE_NAME_Hi = "Hi_table";
    public final static String KEY_Hi_Index = "Hi_index";
    public final static String KEY_Con_A_SBP = "Con_A_SBP";
    public final static String KEY_Con_B_SBP = "Con_B_SBP";
    public final static String KEY_Con_A_DBP = "Con_A_DBP";
    public final static String KEY_Con_B_DBP = "Con_B_DBP";


    //Create Table
    //0=>False , 1=>True
    private String CREATE_TABLE_Hi =
            "CREATE TABLE IF NOT EXISTS "+TABLE_NAME_Hi+"("+
                    KEY_Hi_Index+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    KEY_Con_A_SBP+" TEXT,"+
                    KEY_Con_B_SBP+" TEXT,"+
                    KEY_Con_A_DBP+" TEXT,"+
                    KEY_Con_B_DBP+" TEXT"+

                    ")";

    public HiDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        database = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_Hi);

        //SAID_jsonUpdate();
        //database = this.getWritableDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_Hi);
        onCreate(db);
    }

    public SQLiteDatabase getDatabase(){
        return database;
    }

    public Cursor HiSelect(){
        Cursor cursor = database.query(TABLE_NAME_Hi, null, null, null, null, null, null);
        return cursor;
    }

    public void HiUpdate(int id, ContentValues values){
        database.update(TABLE_NAME_Hi, values, KEY_Hi_Index + "=" + Integer.toString(id), null);
    }

    public void HiInsert(ContentValues values){

        database.insert(TABLE_NAME_Hi, null, values);
    }

    public void close(){
        database.close();
    }
}
