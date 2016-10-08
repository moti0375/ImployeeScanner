package com.bartovapps.employeescanner.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.bartovapps.employeescanner.R;
import com.bartovapps.employeescanner.model.Employee;
import com.bartovapps.employeescanner.model.EmployeeSerializer;
import com.bartovapps.employeescanner.model.EmployeesDbOpenHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by BartovMoti on 10/03/16.
 */
public class EmployeeScannerUtils {

    public static final String TAG = EmployeeScannerUtils.class.getSimpleName();

    public static File realmDataListToCsv(Context context, ArrayList<Employee> data) throws JSONException {

        File csvFile = null;
        Gson gson = new GsonBuilder().registerTypeAdapter(Employee.class, new EmployeeSerializer()).create();
        JsonElement element = gson.toJsonTree(data, new TypeToken<List<Employee>>() {}.getType());

        if (! element.isJsonArray()) {
            Log.e(TAG, "Error occurred when trying to convert ArrayList<Employee> to jsonArray");
            return null;
        }

        JSONArray jsonArray = new JSONArray(element.getAsJsonArray().toString());
        Log.i(TAG, "Output json array: " + jsonArray.toString());


        String csv = CDL.toString(jsonArray);

        File employeesScannerDir = EmployeeScannerUtils.getEmployeeScannerStorageDir(context);
        String csvFileName = EmployeeScannerUtils.getFileNameWithDate(context);


        try {
            if (employeesScannerDir != null & csvFileName != null) {
                csvFile = new File(employeesScannerDir, csvFileName);
                Log.i(TAG, "New CSV File name:  " + csvFile.toString());
                FileUtils.writeStringToFile(csvFile, csv);
                Log.i(TAG, "Csv written successfully");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return csvFile;
    }

    public static JSONObject EmployeesJsonSerializer(Employee employee) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("tag_id", employee.getTag_id());
        obj.put("name", employee.getName());
        obj.put("address", employee.getAddress());
        obj.put("arrived", employee.isArrived() ? "Yes" : "No" );
        obj.put("image_uri", employee.getImageUri());
        //if you have more fields you continue
        return obj;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getEmployeeScannerStorageDir(Context context) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), context.getString(R.string.app_name));
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }


    public static String getFileNameWithDate(Context context) {
        long now = System.currentTimeMillis();

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String dateString = formatter.format(new Date(now));

        return context.getString(R.string.app_name) + "_" + dateString + ".csv";

    }

    public static ArrayList<Employee> getEmployeesFromCursor(Cursor cursor){
        ArrayList<Employee> employees = new ArrayList<>();

        if (cursor.getCount() > 0) {

            cursor.moveToFirst();
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
        }else{
            return null;
        }

        return employees;

    }

}
