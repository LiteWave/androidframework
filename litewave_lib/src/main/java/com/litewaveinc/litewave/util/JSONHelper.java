package com.litewaveinc.litewave.util;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by jonathan on 8/29/15.
 */
public class JSONHelper {
    public static JSONArray getJSONArray(String JSONString){
        try {
            return new JSONArray(JSONString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  null;
    }
}
