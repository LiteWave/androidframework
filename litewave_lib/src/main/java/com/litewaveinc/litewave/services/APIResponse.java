package com.litewaveinc.litewave.services;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by davidanderson on 10/10/15.
 */
public class APIResponse implements IAPIResponse {
    public void success(JSONArray content) {

    }

    public void success(JSONObject content) {

    }

    public void failure(JSONArray content, int statusCode) {
        System.out.println("Request error " + statusCode + ": " + content.toString());
    }

    public void failure(JSONObject content, int statusCode) {
        System.out.println("Request error " + statusCode + ": " + content.toString());
    }
}
