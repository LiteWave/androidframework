package com.litewaveinc.litewave.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.litewaveinc.litewave.R;
import com.litewaveinc.litewave.services.API;
import com.litewaveinc.litewave.services.APIResponse;
import com.litewaveinc.litewave.services.Config;
import com.litewaveinc.litewave.services.ViewStack;
import com.litewaveinc.litewave.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    public Context context;
    public MainActivity self;

    public View view;
    public TextView noEventsTextView;
    public TextView poweredByTextView;
    public ImageView backgroundImage;
    public ImageView logoImage;

    public class GetEventsResponse extends APIResponse {

        @Override
        public void success(JSONArray content) {
            Date currentDate = Calendar.getInstance().getTime();

            if (content.length() > 0) {
                for(int i = 0 ; i < content.length(); i++) {
                    JSONObject event = null;
                    JSONObject settings = null;
                    String eventDate = null;
                    try {
                        event = content.getJSONObject(i);
                        eventDate = event.getString("date");
                        settings = event.getJSONObject("settings");
                    } catch (JSONException e) {
                        showError(e);
                        return;
                    }

                    eventDate = eventDate.substring(0, eventDate.indexOf('T'));
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("y-MM-dd");
                    String formattedCurrentDate = simpleDateFormat.format(currentDate);

                    if (eventDate.compareTo(formattedCurrentDate) == 0) {
                        Log.d("MainActivity", "Event found for: " + formattedCurrentDate.toString());
                        saveSettings(settings);
                        saveLogo();
                        showEvent(event);
                        return;
                    }
                }
            }
            Log.d("MainActivity", "No Available event found for: " + currentDate.toString());
            getClient();
            clearSeat();
        }

        @Override
        public void failure(JSONArray content, int statusCode) {
            Helper.showDialog("Whoops", "Sorry, an error has occurred.", self);
        }
    }

    public class GetClientResponse extends APIResponse {
        @Override
        public void success(JSONObject content) {
            JSONObject settings = null;
            try {
                settings = content.getJSONObject("settings");
            } catch (JSONException e) {
                showError(e);
                return;
            }

            saveSettings(settings);
            showNoEvents();
        }

        @Override
        public void failure(JSONArray content, int statusCode) {
            Helper.showDialog("Whoops", "Sorry, an error has occurred.", self);
        }

    }

    private class DownloadLogoTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadLogoTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            double height = metrics.heightPixels*1.5;
            double width = (result.getWidth()*height)/result.getHeight();
            Bitmap resized = Bitmap.createScaledBitmap(result, (int)width, (int)height, true);
            bmImage.setImageBitmap(resized);
            bmImage.setAlpha((float) 0.05);

            Config.set("logoBitmap", resized);
        }
    }

    protected void loadPreferences() {
        if (Config.getPreference("UserID", "", context) == "") {
            Config.setPreference("UserID", UUID.randomUUID().toString(), context);
        }

        Config.set("UserLocationID", Config.getPreference("UserLocationID", "", context));
        Config.set("MobileOffset", Config.getPreference("MobileOffset", "", context));

        Config.set("UserID", Config.getPreference("UserID", "", context));
        Config.set("LevelID", Config.getPreference("LevelID", "", context));
        Config.set("SectionID", Config.getPreference("SectionID", "", context));
        Config.set("RowID", Config.getPreference("RowID", "", context));
        Config.set("SeatID", Config.getPreference("SeatID", "", context));
    }

    protected void clearSeat() {
        Config.setPreference("UserLocationID", null, context);
        Config.setPreference("LevelID", null, context);
        Config.setPreference("SectionID", null, context);
        Config.setPreference("RowID", null, context);
        Config.setPreference("SeatID", null, context);
    }

    protected void checkEvents() {
        API.getEvents(getString(R.string.ltw_clientID_Blazers), new GetEventsResponse());
    }

    protected void getClient() {
        API.getClient(getString(R.string.ltw_clientID_Blazers), new GetClientResponse());
    }

    protected void showNoEvents() {
        int backgroundColor = Helper.getColor((String)Config.get("backgroundColor"));
        int textColor = Helper.getColor((String)Config.get("textColor"));
        view.setBackgroundColor(backgroundColor);

        noEventsTextView.setText(R.string.ltw_noEventsToday);
        noEventsTextView.setTextColor(textColor);
        noEventsTextView.setVisibility(View.VISIBLE);

        poweredByTextView.setVisibility(View.VISIBLE);
        poweredByTextView.setTextColor(textColor);

        logoImage.setVisibility(View.VISIBLE);
        saveLogo();
    }

    protected void saveSettings(JSONObject settings) {
        try {
            Config.set("backgroundColor", settings.getString("backgroundColor"));
            Config.set("borderColor", settings.getString("borderColor"));
            Config.set("highlightColor", settings.getString("highlightColor"));
            Config.set("textColor", settings.getString("textColor"));
            Config.set("textSelectedColor", settings.getString("textSelectedColor"));

            Config.set("logoUrl", settings.getString("logoUrl"));

            if (settings.has("pollInterval")) {
                Config.set("pollInterval", settings.getString("pollInterval"));
            } else {
                Config.set("pollInterval", "5000");
            }
        } catch (JSONException e) {
            showError(e);
        }
    }

    protected void saveLogo() {
        new DownloadLogoTask((ImageView) findViewById(R.id.backgroundImage))
                .execute((String) Config.get("logoUrl"));
    }

    protected void showEvent(JSONObject event) {
        try {
            Config.set("StadiumID", event.getString("_stadiumId"));
            Config.set("EventID", event.getString("_id"));
            Config.set("EventName", event.getString("name"));
            Config.set("EventDate", event.getString("date"));
        } catch (JSONException e) {
            showError(e);
            return;
        }

        Intent intent;
        if (Config.getPreference("UserLocationID", "", context) == "") {
            intent = new Intent(MainActivity.this, LevelActivity.class);
        } else {
            intent = new Intent(MainActivity.this, ReadyActivity.class);
        }

        ViewStack.push(MainActivity.class);
        startActivity(intent);
        finish();
    }

    protected void showError(Exception e) {
        e.printStackTrace();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        self = this;

        setContentView(R.layout.ltw_activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        view = findViewById(R.id.view);

        noEventsTextView = (TextView) this.findViewById(R.id.noEventsTextView);
        poweredByTextView = (TextView) this.findViewById(R.id.poweredByTextView);
        backgroundImage = (ImageView) this.findViewById(R.id.backgroundImage);
        logoImage = (ImageView) this.findViewById(R.id.logoImage);

        API.init(context);

        loadPreferences();
        checkEvents();
    }
}
