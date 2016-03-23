package com.litewaveinc.litewave.util;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import android.os.AsyncTask;

/**
 * Created by jonathan on 8/28/15.
 */
public class RESTClientHelper {


    public static String callRESTService(String endpointURL) {
        String response = null;

        try {
            URL url = new URL(endpointURL);
            HttpURLConnection conn;
            BufferedReader rd;
            String line;
            StringBuilder result = new StringBuilder();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            response = result.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }
}
