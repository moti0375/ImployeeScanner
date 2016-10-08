package com.bartovapps.employeescanner.model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by BartovMoti on 10/07/16.
 */
public class EmployeesProvider extends ContentProvider {

    public static final String AUTHORITY = "com.bartovapps.employeescanner.model.EmployeesProvider";
    public static final String BASE_PATH = "employees";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final int EMPLOYEES = 1;
    public static final int EMPLOYEES_ID = 2;

    public static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static {

        uriMatcher.addURI(AUTHORITY, BASE_PATH, EMPLOYEES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", EMPLOYEES_ID);

    }

    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        dbHelper = new EmployeesDbOpenHelper(getContext());
        database = dbHelper.getWritableDatabase();

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return database.query(EmployeesDbOpenHelper.TABLE_EMPLOYEES, EmployeesDbOpenHelper.allColumns, selection, null, null, null, EmployeesDbOpenHelper.COLUMN_ID + " DESC");
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long id = database.insert(EmployeesDbOpenHelper.TABLE_EMPLOYEES, null, contentValues);

        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(EmployeesDbOpenHelper.TABLE_EMPLOYEES, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        return database.update(EmployeesDbOpenHelper.TABLE_EMPLOYEES, contentValues, selection, selectionArgs);
    }
}
