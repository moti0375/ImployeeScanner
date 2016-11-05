package com.bartovapps.employeescanner.model;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by BartovMoti on 10/03/16.
 */
public class EmployeeSerializer implements JsonSerializer<Employee>{

    public static final String TAG = EmployeeSerializer.class.getSimpleName();
    @Override
    public JsonElement serialize(Employee src, Type typeOfSrc, JsonSerializationContext context) {
        Log.i(TAG, "serialize was called..");
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("No#", src.getItemNo());
        jsonObject.addProperty("tag_id", src.getTag_id());
        jsonObject.addProperty("name", src.getName());
        jsonObject.addProperty("arrived", src.isArrived() ? "Yes" : "No");
        jsonObject.addProperty("address", src.getAddress());
        jsonObject.addProperty("imageUri", src.getImageUri());
        return jsonObject;
    }
}
