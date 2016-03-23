package com.litewaveinc.litewave.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidanderson on 10/24/15.
 */
public class Config {

    private static Map<String, Object> data = new HashMap<String, Object>();
    private static Map<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();

    public static Object get(String name) {
        return data.get(name);
    }

    public static Object set(String name, Object value) {
        data.put(name, value);
        return value;
    }

    public static String getPreference(String name, String defaultValue, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        return preferences.getString(name, defaultValue);
    }

    public static String setPreference(String name, String value, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(name, value);
        editor.commit();

        return value;
    }

}
