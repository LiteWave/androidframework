package com.litewaveinc.litewave.services;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by davidanderson on 10/10/15.
 */
public interface IAPIResponse {
    void success(JSONArray content);
    void success(JSONObject content);
    void failure(JSONArray content, int statusCode);
    void failure(JSONObject content, int statusCode);
}
