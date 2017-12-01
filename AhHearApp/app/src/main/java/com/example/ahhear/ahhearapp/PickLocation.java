package com.example.ahhear.ahhearapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;


public class PickLocation extends GigBrowse {
    public static final String EXTRA_DECIBEL = "com.example.ahhear.MESSAGE";
    public static final String EXTRA_WIDTH = "com.example.ahhear.WIDTH";
    public static final String EXTRA_HEIGHT = "com.example.ahhear.HEIGHT";
    public static final String EXTRA_GIGID = "com.example.ahhear.GIGID";

    private Button mRecordButton;
    private TextView mRecordLabel;
    private TextView mDecibels;
    private MediaRecorder mRecorder;
    private double amplitudeDb;
    private Intent intent;
    private Timer timer;
    private static final String LOG_TAG = "Record Log";
    final Handler handler = new Handler();
    private float chosen_percent_width;
    public float chosen_percent_height;
    public LocalDataBaseManager localDataBaseManager;
    public int gig_id;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get gig_id from previous activity.
        gig_id = getIntent().getIntExtra("gig_id", 1);
        setContentView(R.layout.pick_location);

        // download data from the api asynchronously.
        PickLocation.DownloadGigData downloadGigData = new PickLocation.DownloadGigData(this);

        // try to download data of a single gig.
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http");

            builder.encodedAuthority("gavs.work:8000");
            builder.appendPath("single_gig");

            builder.appendQueryParameter("gig_id", String.valueOf(gig_id));
            builder.build();
            downloadGigData.execute(new URL(builder.toString()));

        } catch (MalformedURLException e) {

            // if the url is incorrect toast the error to the user.
            Toast toast = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }

        ImageView imageView = (ImageView) findViewById(R.id.floorplan);

        // button logic for the sampling of sound.
        mRecordButton  = findViewById(R.id.button_rec);
        mRecordLabel = findViewById(R.id.textView_record_label);
        mDecibels = findViewById(R.id.textView_sound);

        mRecordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    startRecording();
                    mRecordLabel.setText(R.string.start_record);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    mRecordLabel.setText(R.string.stop_record);
                    stopRecording();
                }
                return false;
            }
        });

        imageView.setDrawingCacheEnabled(true);
        imageView.setOnTouchListener(addPin);
    }

    // method which records audio.
    private void startRecording() {
        mDecibels.setText("");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile("/dev/null");
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mRecorder.start();

        timer = new Timer();
        timer.scheduleAtFixedRate(new RecorderTask(handler), 0, 1000);

    }

    // stops recording and inserts results into personal database.
    private void stopRecording() {
        intent = new Intent(this, DisplayLevel.class);
        intent.putExtra(EXTRA_DECIBEL, amplitudeDb);
        intent.putExtra(EXTRA_WIDTH, chosen_percent_width);
        intent.putExtra(EXTRA_HEIGHT, chosen_percent_height);
        intent.putExtra(EXTRA_GIGID, gig_id);

        timer.cancel();
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        localDataBaseManager = new LocalDataBaseManager(this, "", null, 1);
        localDataBaseManager.insert_gig_recording("Cold Play", "Olympia", (int) Math.round(amplitudeDb));

        startActivity(intent);
    }


    private class RecorderTask extends TimerTask {
        Handler myhandler;
        RecorderTask(Handler h)
        {
            myhandler=h;
        }

        public void run() {
            int amplitude = mRecorder.getMaxAmplitude();
            amplitudeDb = 20 * Math.log10((double)Math.abs(amplitude));
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mDecibels.setText(String.format("Amplitude : %s", String.format("%.2f", amplitudeDb)));
                }
            });
        }
    }

    // method to add pin to map.
    private final View.OnTouchListener addPin = new View.OnTouchListener() {

        @Override
        public boolean onTouch(final View v, MotionEvent event) {

            Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
            int color = 0;
            try {
                color = bmp.getPixel((int) event.getX(), (int) event.getY());
            } catch (Exception e) {
                // e.printStackTrace();
            }
            // don't allow the user to selct on alpha channel.
            if (color == Color.TRANSPARENT)
                return false;
            else {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    // get the selection coordinates of user.
                    float x = event.getX();
                    float y = event.getY();

                    String uri = "@drawable/red_pin";
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    ImageView pinview = (ImageView) findViewById(R.id.pin);
                    ImageView floorplanview = (ImageView) findViewById(R.id.floorplan);

                    // get the width and height of floorplan...
                    float floorplan_width = floorplanview.getHeight();
                    float floorplan_height = floorplanview.getWidth();

                    float pin_height = pinview.getMeasuredHeight();

                    chosen_percent_width = (x / floorplan_height) * 100;
                    chosen_percent_height = (y / floorplan_width) * 100;

                    if (pinview.getDrawable() == null) {
                        Drawable res = getResources().getDrawable(imageResource);
                        pinview.setAdjustViewBounds(true);
                        pinview.setImageDrawable(res);
                    }

                    // adjust the location of the pin based on its size
                    // the pin isn't in the center of the image.
                    pinview.setX(x);
                    pinview.setY(y - ((pin_height / 100) * 85));

                    break;

                default:
                    break;
            }
            return true;

        }

        }
    };

    // download from api asynchronously.
    private class DownloadGigData extends AsyncTask<URL, Integer, JSONArray> {

        // record this activity as a variable for later.
        // used to get the width of the screen for the heatmap.
        private Activity activity;

        private DownloadGigData(Activity activity) {
            this.activity = activity;
        }

        protected JSONArray doInBackground(URL... urls) {

            JSONArray arr = null;

            for (URL url : urls) {
                try {
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setConnectTimeout(1000);
                    BufferedInputStream is = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder sb = new StringBuilder();

                    String line;

                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append('\n');
                    }

                    try {
                        arr = new JSONArray(sb.toString());

                    } catch (JSONException e) {
                        Log.e("AhHere", "exception: " + e.getMessage());
                        System.out.println("Could not download data from API.");
                    }

                } catch (IOException e) {
                    Log.e("AhHere", "exception: " + e.getMessage());
                    System.out.println("Could not make url connection.");
                }

                // Escape early if cancel() is called
                if (isCancelled()) break;
            }

            return arr;
        }

        // when the asynchronous download is finished this runs.
        protected void onPostExecute(JSONArray ReturnArray) {

            try {

                JSONObject gigJSON = ReturnArray.getJSONObject(0);
                String VenueId = gigJSON.getString("venue_id");

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.encodedAuthority("gavs.work:8000");
                builder.appendPath("floorplan_image");
                builder.appendQueryParameter("id", VenueId);
                builder.build();

                // use picasso to download images asynchronously
                ImageView imageView = (ImageView) findViewById(R.id.floorplan);
                Picasso.with(activity).load(builder.toString()).into(imageView);

                // fill in band name and venue name.
                String BandName = gigJSON.getString("band_name");
                TextView bandView = (TextView)findViewById(R.id.LoactionBandName);
                bandView.setText(BandName);

                String VenueName = gigJSON.getString("venue_name");
                TextView venueView=(TextView)findViewById(R.id.LoactionVenueName);
                venueView.setText(VenueName);

            } catch (JSONException e) {
                Log.e("AhHere", "exception: " + e.getMessage());
                System.out.println("Malformed Url.");
            }
        }
    }
}
