package com.bartovapps.employeescanner.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by BartovMoti on 10/04/16.
 */
public class EmployeesDbOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = EmployeesDbOpenHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "employees.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_EMPLOYEES = "employees";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TAG_ID = "tag_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ARRIVED = "arrived";
    public static final String COLUMN_IMAGE_URI = "image_uri";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_EMPLOYEES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TAG_ID + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_NAME      + " TEXT, " +
                    COLUMN_ARRIVED      + " INTEGER, " +
                    COLUMN_IMAGE_URI  + " TEXT " +  ")";


    public EmployeesDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_EMPLOYEES;
        db.execSQL(DROP_TABLE);
//        Log.i(LOG_TAG, "Any previous table dropped");

        db.execSQL(TABLE_CREATE);
        Log.i(TAG, "Tours table has been created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
