package com.litewaveinc.litewave.services;

import android.content.Context;
import android.util.Log;

import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;

import com.litewaveinc.litewave.R;
import org.apache.http.entity.StringEntity;
import org.apache.http.Header;


/**
 * Created by jonathan on 10/10/15.
 */


public final class API {

    static protected Context appContext;

    public static void init(Context context) {
        API.appContext = context;
    }

      /* CLIENTS */

    public static void getClient(String clientID, IAPIResponse response) {
        API.get("clients/" + clientID, response);
    }

      /* EVENTS */

    public static void getEvents(String clientID, IAPIResponse response) {
        API.get("clients/" + clientID + "/events", response);
    }

    public static void getEvent(String clientID, String eventID, IAPIResponse response) {
        API.get("clients/" + clientID + "/events/" + eventID, response);
    }

    public static void joinEvent(String eventID, JSONObject params, IAPIResponse response) {
        API.post("events/" + eventID + "/user_locations", params, response);
    }

    public static void leaveEvent(String userLocationID, IAPIResponse response) {
        API.delete("user_locations/" + userLocationID, response);
    }

      /* SHOWS */

    public static void getShows(String eventID, IAPIResponse response) {
        API.get("events/" + eventID + "/shows", response);
    }

    public static void getShow(String eventID, String showID, IAPIResponse response) {
        API.get("events/" + eventID + "/shows/" + showID, response);
    }

    public static void joinShow(String userLocationID, JSONObject params, IAPIResponse response) {
        API.post("user_locations/" + userLocationID + "/event_joins", params, response);
    }

      /* SEATS */

    public static void getLevels(String stadiumID, IAPIResponse response) {
        API.get("stadiums/" + stadiumID + "/levels", response);
    }

    public static void getSeats(String stadiumID, String level, IAPIResponse response) {
        API.get("stadiums/" + stadiumID + "/levels/" + level, response);
    }

    private static void get(String url, IAPIResponse response) {
        API.request(url, "GET", new JSONObject(), response);
    }

    private static void post(String url, JSONObject params, IAPIResponse response) {
        API.request(url, "POST", params, response);
    }

    private static void put(String url, JSONObject params, IAPIResponse response) {
        API.request(url, "PUT", params, response);
    }

    private static void delete(String url, IAPIResponse response) {
        API.request(url, "DELETE", new JSONObject(), response);
    }

    private static void request(String url, String method, JSONObject params, final IAPIResponse apiResponse) {
        if (appContext == null) {
            Log.d("Debug", "Need to call init first.");
            throw new UnsupportedOperationException();
        }

        StringBuilder apiURL = new StringBuilder();
        apiURL.append(appContext.getString(R.string.ltw_apiURL));
        apiURL.append(url);

        ResponseHandlerInterface responseHandler = new JsonHttpResponseHandler() {

            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                apiResponse.success(response);
            }

            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                apiResponse.success(response);
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("Failed: ", "" + statusCode);
                Log.d("Error : ", "" + throwable);
                apiResponse.failure(new JSONArray(), statusCode);
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("Failed: ", ""+statusCode);
                Log.d("Error : ", "" + throwable);
                apiResponse.failure(errorResponse, statusCode);
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("Failed: ", ""+statusCode);
                Log.d("Error : ", "" + throwable);
                apiResponse.failure(errorResponse, statusCode);
            }

        };

        StringEntity entity = null;
        try {
            entity = new StringEntity(params.toString());
        } catch (Exception e) {
            return;
        }
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        Log.d("Debug", "Requesting: " + method + ":" + apiURL.toString());

        AsyncHttpClient client = new AsyncHttpClient();

        switch (method) {
            case "GET":
                client.get(appContext, apiURL.toString(), responseHandler);
                break;
            case "POST":
                client.post(appContext, apiURL.toString(), entity, "application/json", responseHandler);
                break;
            case "PUT":
                client.put(appContext, apiURL.toString(), entity, "application/json", responseHandler);
                break;
            case "DELETE":
                client.delete(appContext, apiURL.toString(), responseHandler);
                break;

        }
    }
}
