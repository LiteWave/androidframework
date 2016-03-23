package com.litewaveinc.litewave.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class ReadyActivity extends AppCompatActivity {

    public int STROKE_WIDTH = 6;

    public Context context;
    public ReadyActivity self;

    public TextView eventStatusTextView;
    public ImageView backgroundImage;
    public Button joinButton;
    public ProgressBar spinner;

    public ImageView levelImageView;
    public TextView levelTextView;
    public TextView levelNameTextView;

    public ImageView sectionImageView;
    public TextView sectionTextView;
    public TextView sectionNameTextView;

    public ImageView rowImageView;
    public TextView rowTextView;
    public TextView rowNameTextView;

    public ImageView seatImageView;
    public TextView seatTextView;
    public TextView seatNameTextView;

    public View view;

    public JSONObject currentShow;

    Timer pollTimer;

    public class LeaveEventResponse extends APIResponse {

        @Override
        public void success(JSONObject content) {
            clearSeat();

            Intent parentActivityIntent = new Intent(ReadyActivity.this, ViewStack.pop());
            parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(parentActivityIntent);
        }

        @Override
        public void failure(JSONArray content, int statusCode) {
            Helper.showDialog("Error", "Sorry, could not leave event at this time.", self);
        }
    }

    public class GetShowsResponse extends APIResponse {

        @Override
        public void success(JSONArray content) {
            Date currentDate;
            TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
            DateFormat gmtFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            gmtFormat.setTimeZone(gmtTimeZone);

            try {
                int offset = Integer.parseInt((String)Config.get("MobileOffset"));
                currentDate = gmtFormat.parse(Calendar.getInstance().getTime().toString());
                currentDate = new Date(currentDate.getTime() - offset);
            } catch (ParseException e) {e.printStackTrace(); return;}

            if (content.length() > 0) {
                for (int i = 0 ; i < content.length(); i++) {
                    JSONObject show = null;
                    String startAt;

                    try {
                        show = content.getJSONObject(i);
                        startAt = show.getString("startAt");
                        if (startAt.equals("null")) {
                            continue;
                        }
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                        dateFormat.setTimeZone(gmtTimeZone);
                        Date startDate;
                        try {
                            startDate = dateFormat.parse(startAt);
                        } catch (ParseException e) {e.printStackTrace(); return;}

                        if (currentDate.before(startDate)) {
                            currentShow = show;
                            enableJoin();
                            stopPolling();
                            return;
                        }

                    } catch (JSONException e) {e.printStackTrace();return;}
                }
            }
            disableJoin();
        }
    }

    public class JoinShowResponse extends APIResponse {

        @Override
        public void success(JSONObject content) {
            ViewStack.push(ReadyActivity.class);

            String mobileOffset = "";
            try {
                mobileOffset = content.getString("mobileTimeOffset");
            } catch (JSONException e) {return;}

            saveOffset(mobileOffset);
            Config.set("ShowData", content);
            Config.set("Show", currentShow);

            Intent intent = new Intent(ReadyActivity.this, ShowActivity.class);
            startActivity(intent);
            finish();
        }

        public void failure(JSONObject content, int statusCode) {
            Helper.showDialog("Show", "Sorry, but the show has expired.", self);

            disableJoin();
            beginPolling();
        }

    }

    public void clearSeat() {
        Config.setPreference("UserLocationID", null, context);
        Config.setPreference("LevelID", null, context);
        Config.setPreference("SectionID", null, context);
        Config.setPreference("RowID", null, context);
        Config.setPreference("SeatID", null, context);
    }

    public void leaveEvent() {
        API.leaveEvent((String) Config.get("UserLocationID"), new LeaveEventResponse());
    }

    protected void saveOffset(String mobileOffset) {
        Config.setPreference("MobileOffset", (String) Config.set("MobileOffset", mobileOffset), context);
    }


    protected void getImage() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = (Bitmap) Config.get("logoBitmap");
                        if (bitmap != null) {
                            timer.cancel();
                            backgroundImage.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        }, 0, 50);
    }

    protected void enableJoin() {

        int color = Helper.getColor((String)Config.get("highlightColor"));
        joinButton.setBackgroundColor(color);
        joinButton.setTextColor(Color.parseColor("#FFFFFF"));
        joinButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                joinShow();
            }
        });

        eventStatusTextView.setText("Join the event to begin");
    }

    protected void disableJoin() {
        int color = Helper.getColor((String) Config.get("highlightColor"));
        joinButton.setBackgroundColor(ContextCompat.getColor(context, R.color.ltw_disabled_button_background));
        joinButton.setTextColor(ContextCompat.getColor(context, R.color.ltw_disabled_button_text));
        joinButton.setOnClickListener(null);

        eventStatusTextView.setText("Waiting for the event to begin");
    }

    public void joinShow() {
        String mobileStart = Helper.getNowInGMT();
        JSONObject params = new JSONObject();
        try {
            params.put("mobileTime", mobileStart);
        } catch (JSONException e) {e.printStackTrace();}

        API.joinShow((String) Config.get("UserLocationID"), params, new JoinShowResponse());
    }

    public void beginPolling() {
        int pollInterval = Integer.parseInt((String)Config.get("pollInterval"));

        if (pollTimer != null) {
            stopPolling();
        }
        pollTimer = new Timer();
        pollTimer.schedule(new TimerTask() {

            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getShow();
                    }
                });
            }
        }, 0, pollInterval);
    }

    public void stopPolling() {
        if (pollTimer != null) {
            pollTimer.cancel();
            pollTimer = null;
        }
    }

    public void getShow() {
        API.getShows((String) Config.get("EventID"), new GetShowsResponse());
    }

    public void drawCircle(ImageView imageView, TextView textView, TextView textViewLabel) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int borderColor = Helper.getColor((String)Config.get("highlightColor"));
        int backgroundColor = Helper.getColor((String)Config.get("highlightColor"));
        int radius = (metrics.widthPixels/4) - 50;
        Helper.drawCircle(imageView, radius, STROKE_WIDTH, borderColor, backgroundColor);

        textView.setHeight(radius);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, radius / 4);

        textViewLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, radius / 4);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        self = this;
        int highlightColor = Helper.getColor((String)Config.get("highlightColor"));
        int textColor = Helper.getColor((String) Config.get("textColor"));
        int backgroundColor = Helper.getColor((String)Config.get("backgroundColor"));

        setContentView(R.layout.ltw_activity_ready);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle((String) Config.get("EventName"));
            actionBar.setBackgroundDrawable(new ColorDrawable(highlightColor));
        }

        view = findViewById(R.id.view);
        view.setBackgroundColor(backgroundColor);

        backgroundImage = (ImageView) this.findViewById(R.id.backgroundImage);
        backgroundImage.setAlpha((float) 0.05);
        getImage();

        joinButton = (Button)findViewById(R.id.joinButton);

        eventStatusTextView = (TextView)findViewById(R.id.eventStatusTextView);
        eventStatusTextView.setTextColor(textColor);

        spinner = (ProgressBar)findViewById(R.id.spinner);
        spinner.getIndeterminateDrawable().setColorFilter(highlightColor,
                android.graphics.PorterDuff.Mode.MULTIPLY);

        levelImageView = (ImageView)findViewById(R.id.levelCircleImageView);

        levelTextView = (TextView)findViewById(R.id.levelTextView);
        levelTextView.setTextColor(Helper.getColor((String) Config.get("textColor")));

        levelNameTextView = (TextView)findViewById(R.id.levelNameTextView);
        levelNameTextView.setTextColor(Helper.getColor((String) Config.get("textSelectedColor")));
        levelNameTextView.setText((String) Config.get("LevelID"));
        drawCircle(levelImageView, levelNameTextView, levelTextView);

        sectionImageView = (ImageView)findViewById(R.id.sectionCircleImageView);

        sectionTextView = (TextView)findViewById(R.id.sectionTextView);
        sectionTextView.setTextColor(Helper.getColor((String) Config.get("textColor")));

        sectionNameTextView = (TextView)findViewById(R.id.sectionNameTextView);
        sectionNameTextView.setTextColor(Helper.getColor((String) Config.get("textSelectedColor")));
        sectionNameTextView.setText((String) Config.get("SectionID"));
        drawCircle(sectionImageView, sectionNameTextView, sectionTextView);

        rowImageView = (ImageView)findViewById(R.id.rowCircleImageView);

        rowTextView = (TextView)findViewById(R.id.rowTextView);
        rowTextView.setTextColor(Helper.getColor((String) Config.get("textColor")));

        rowNameTextView = (TextView)findViewById(R.id.rowNameTextView);
        rowNameTextView.setTextColor(Helper.getColor((String) Config.get("textSelectedColor")));
        rowNameTextView.setText((String) Config.get("RowID"));
        drawCircle(rowImageView, rowNameTextView, rowTextView);

        seatImageView = (ImageView)findViewById(R.id.seatCircleImageView);

        seatTextView = (TextView)findViewById(R.id.seatTextView);
        seatTextView.setTextColor(Helper.getColor((String) Config.get("textColor")));

        seatNameTextView = (TextView)findViewById(R.id.seatNameTextView);
        seatNameTextView.setTextColor(Helper.getColor((String) Config.get("textSelectedColor")));
        seatNameTextView.setText((String) Config.get("SeatID"));
        drawCircle(seatImageView, seatNameTextView, seatTextView);

        disableJoin();

        beginPolling();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                leaveEvent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
