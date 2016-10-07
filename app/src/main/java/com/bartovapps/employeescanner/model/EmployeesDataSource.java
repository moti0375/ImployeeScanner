package com.bartovapps.employeescanner.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by BartovMoti on 10/04/16.
 */
public class EmployeesDataSource {
    public static final String TAG = EmployeesDataSource.class.getSimpleName();

    SQLiteOpenHelper dbHelper;
    SQLiteDatabase database;

    private static final String[] allColumns = {
            EmployeesDbOpenHelper.COLUMN_ID,
            EmployeesDbOpenHelper.COLUMN_TAG_ID,
            EmployeesDbOpenHelper.COLUMN_NAME,
            EmployeesDbOpenHelper.COLUMN_ARRIVED,
            EmployeesDbOpenHelper.COLUMN_IMAGE_URI};

    public EmployeesDataSource(Context context) {
        dbHelper = new EmployeesDbOpenHelper(context);
    }

    public void open() {
//		Log.i(LOG_TAG, "Database opened");
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
//		Log.i(LOG_TAG, "Database closed");
        dbHelper.close();
    }

    public Employee insert(Employee employee) {
        ContentValues values = new ContentValues();
        values.put(EmployeesDbOpenHelper.COLUMN_TAG_ID, employee.getTag_id());
        values.put(EmployeesDbOpenHelper.COLUMN_NAME, employee.getName());
        values.put(EmployeesDbOpenHelper.COLUMN_ARRIVED, employee.isArrived() ? 1 : 0);
        values.put(EmployeesDbOpenHelper.COLUMN_IMAGE_URI, employee.getImageUri());
        long insertId = database.insert(EmployeesDbOpenHelper.TABLE_EMPLOYEES, null, values);
        employee.setId(insertId);
		Log.i(TAG, "inserted:  " + employee.toString());

        return employee;
    }


    public ArrayList<Employee> findAll() {
        ArrayList<Employee> employees = new ArrayList<>();
        Cursor cursor;
        try {
            cursor = database.query(EmployeesDbOpenHelper.TABLE_EMPLOYEES, allColumns,
                    null, null, null, null, EmployeesDbOpenHelper.COLUMN_ID + " DESC");
        } catch (SQLiteException e) {
            e.printStackTrace();
//            Log.i(LOG_TAG, "There was an SQLite exception: " + e.getMessage());

            return employees;
        }

        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                Employee employee = new Employee();
                employee.setId(cursor.getLong(cursor.getColumnIndex(EmployeesDbOpenHelper.COLUMN_ID)));
                employee.setTag_id(cursor.getString(cursor.getColumnIndex(EmployeesDbOpenHelper.COLUMN_TAG_ID)));
                employee.setName(cursor.getString(cursor.getColumnIndex(EmployeesDbOpenHelper.COLUMN_NAME)));
                employee.setArrived(cursor.getInt(cursor.getColumnIndex(EmployeesDbOpenHelper.COLUMN_ARRIVED)) > 0);
                employee.setImageUri(cursor.getString(cursor.getColumnIndex(EmployeesDbOpenHelper.COLUMN_IMAGE_URI)));
                employees.add(employee);

				Log.i(TAG, "employee added: " + employee.toString());
            }
        }
        if(cursor != null){
            cursor.close();
        }
        return employees;
    }

    public boolean delete(Employee employee) {
        String where = EmployeesDbOpenHelper.COLUMN_TAG_ID + "=\"" + employee.getTag_id() + "\"";
        int result = database.delete(EmployeesDbOpenHelper.TABLE_EMPLOYEES, where, null);

		Log.i(TAG, "Employee was removed...");

        return (result == 1);
    }

}
