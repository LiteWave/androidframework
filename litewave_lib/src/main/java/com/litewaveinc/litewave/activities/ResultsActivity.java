package com.litewaveinc.litewave.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.litewaveinc.litewave.R;
import com.litewaveinc.litewave.services.Config;
import com.litewaveinc.litewave.services.ViewStack;
import com.litewaveinc.litewave.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;


public class ResultsActivity extends AppCompatActivity {

    public Context context;
    public ResultsActivity self;

    public Button returnButton;
    public ImageView backgroundImage;
    public View view;

    public JSONObject show;

    public String winnerID;
    public String winnerURL;
    public String winnerImageURL;

    public TextView thanksView;
    public TextView textPoweredBy;

    private void returnReady()
    {
        // popping the current view
        ViewStack.pop();
        // popping the show view
        Intent parentActivityIntent = new Intent(ResultsActivity.this, ViewStack.pop());
        parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(parentActivityIntent);

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        self = this;

        setContentView(R.layout.ltw_activity_results);

        int backgroundColor = Helper.getColor((String)Config.get("backgroundColor"));
        int textColor = Helper.getColor((String)Config.get("textColor"));
        int highlightColor = Helper.getColor((String) Config.get("highlightColor"));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        view = (View) this.findViewById(R.id.view);
        view.setBackgroundColor(backgroundColor);

        backgroundImage = (ImageView) this.findViewById(R.id.backgroundImage);
        backgroundImage.setAlpha((float) 0.05);
        Bitmap bitmap = (Bitmap)Config.get("logoBitmap");
        if (bitmap != null) {
            backgroundImage.setImageBitmap(bitmap);
        }

        returnButton = (Button)findViewById(R.id.returnButton);
        returnButton.setBackgroundColor(highlightColor);
        returnButton.setTextColor(Color.parseColor("#FFFFFF"));
        returnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                returnReady();
            }
        });

        thanksView = (TextView)findViewById(R.id.textThanks);
        thanksView.setTextColor(textColor);

        textPoweredBy =( TextView)findViewById(R.id.textPoweredBy);
        textPoweredBy.setTextColor(textColor);

        show = (JSONObject)Config.get("Show");
        try {
            winnerID = show.getString("_winnerId");
            winnerURL = show.getString("winnerUrl");
            winnerImageURL = show.getString("winnerImageUrl");
        } catch (JSONException e) {return;}

        if (((String)Config.get("UserLocationID")).equals(winnerID)) {
            showWinner();
        }
    }

    public void openWebURL(String url) {
        Intent browse = new Intent(Intent.ACTION_VIEW , Uri.parse(url));
        startActivity(browse);
    }

    private void showWinner() {
        thanksView.setVisibility(View.INVISIBLE);

        textPoweredBy.setVisibility(View.INVISIBLE);

        ImageView lwLogo = (ImageView) findViewById(R.id.lwLogo);
        lwLogo.setVisibility(View.INVISIBLE);

        ImageView winnerView = (ImageView) findViewById(R.id.imageViewWinner);
        winnerView.setVisibility(View.VISIBLE);

        ImageView backgroundImage = (ImageView) findViewById(R.id.backgroundImage);
        backgroundImage.setVisibility(View.INVISIBLE);

        view.setBackgroundColor(Color.BLACK);
        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openWebURL(winnerURL);
            }
        });

        new DownloadImageTask((ImageView) findViewById(R.id.imageViewWinner))
                .execute(winnerImageURL);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
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
            bmImage.setImageBitmap(result);
        }
    }
}
