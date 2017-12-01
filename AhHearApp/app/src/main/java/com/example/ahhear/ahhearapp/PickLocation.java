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
import android.support.v7.app.AppCompatActivity;
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

        gig_id = getIntent().getIntExtra("gig_id", 1);
//        int gig_id = 10;
        setContentView(R.layout.pick_location);
//        set dynamic image // uses picasso for better memory management

        PickLocation.DownloadGigData downloadGigData = new PickLocation.DownloadGigData(this);
        try {
            // this is the url to the API.
            // Static at the moment but its easy to add which gig were looking at.
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http");

            builder.encodedAuthority("gavs.work:8000");
            builder.appendPath("single_gig");

            builder.appendQueryParameter("gig_id", String.valueOf(gig_id));
            builder.build();
            downloadGigData.execute(new URL(builder.toString()));

        } catch (MalformedURLException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }

        ImageView imageView = (ImageView) findViewById(R.id.floorplan);

        mRecordButton  = findViewById(R.id.button_rec);
        mRecordLabel = findViewById(R.id.textView_record_label);
        mDecibels = findViewById(R.id.textView_sound);
//        mRecorder.setOutputFile("/dev/null");
//        mFileName = getExternalCacheDir().getAbsolutePath();
//        mFileName += "/hear_audio.3gp";

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

    private void startRecording() {
        mDecibels.setText("");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        mRecorder.setAudioSamplingRate(44100);
//        mRecorder.setAudioEncodingBitRate(96000);
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
//            Log.e("----------->>>>>>>>>>","MicInfoService amplitude: " + amplitudeDb);
            handler.post(new Runnable() {
                @Override
                public void run() {
//                    System.out.println("------------------*********_____________------------------");
                    mDecibels.setText(String.format("Amplitude : %s", amplitudeDb));
                }
            });
        }
    }

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
            if (color == Color.TRANSPARENT)
                return false;
            else {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

//                        pixels
                    float x = event.getX();
                    float y = event.getY();

                    System.out.println("Curser x and y");
                    System.out.println(x); // 1329
                    System.out.println(y);
                    System.out.println(" ");

                    String uri = "@drawable/red_pin";
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                    ImageView pinview = (ImageView) findViewById(R.id.pin);
                    ImageView floorplanview = (ImageView) findViewById(R.id.floorplan);


                    float floorplan_width = floorplanview.getHeight();
                    float floorplan_height = floorplanview.getWidth();

                    int[] location = new int[2];
                    floorplanview.getLocationOnScreen(location);

                    System.out.println("Floorplan View Location On Screen");
                    System.out.println(location[0]);
                    System.out.println(location[1]);
                    System.out.println(" ");

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    float screenheight = displayMetrics.heightPixels;
                    float screenwidth = displayMetrics.widthPixels;

                    float horizontaledge = (screenwidth - floorplan_width) / 2;

                    float pin_width = pinview.getMeasuredHeight();
                    float pin_height = pinview.getMeasuredHeight();

                    System.out.println("Floorplan Width and Height");
                    System.out.println(floorplan_width);
                    System.out.println(floorplan_height);  //1330
                    System.out.println(" ");

                    chosen_percent_width = (x / floorplan_height) * 100;
                    chosen_percent_height = (y / floorplan_width) * 100;
                    if (pinview.getDrawable() == null) {

                        Drawable res = getResources().getDrawable(imageResource);
                        pinview.setAdjustViewBounds(true);
                        pinview.setImageDrawable(res);
                    }

                    pinview.setX(x); // increase percentage to slip pin left
                    pinview.setY(y - ((pin_height / 100) * 85));

                    break;

                default:
                    break;
            }
            return true;

        }

        }
    };

    private class DownloadGigData extends AsyncTask<URL, Integer, JSONArray> {

        // record this activity as a variable for later.
        // used to get the width of the screen for the heatmap.
        private Activity activity;

        private DownloadGigData(Activity activity) {
            this.activity = activity;
        }

        // downloading the data has to be done asynchronous.
        // this is robbed from charlotte's venue menu.
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

                        // the data comes down as a string
                        // is parsed as json and added to a 2darray of doubles.

                        arr = new JSONArray(sb.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Escape early if cancel() is called
                if (isCancelled()) break;
            }

            return arr;
        }

        // when the asynchronous download is finished this runs.
        // displays the heatmap.
        protected void onPostExecute(JSONArray ReturnArray) {

            try {

                JSONObject gigJSON = ReturnArray.getJSONObject(0);
                String VenueId = gigJSON.getString("venue_id");
                System.out.println(VenueId);

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.encodedAuthority("gavs.work:8000");
                builder.appendPath("floorplan_image");
                builder.appendQueryParameter("id", VenueId);
                builder.build();

                ImageView imageView = (ImageView) findViewById(R.id.floorplan);
                Picasso.with(activity).load(builder.toString()).into(imageView);

                String BandName = gigJSON.getString("band_name");
                TextView bandView = (TextView)findViewById(R.id.LoactionBandName);
                bandView.setText(BandName);

                String VenueName = gigJSON.getString("venue_name");
                TextView venueView=(TextView)findViewById(R.id.LoactionVenueName);
                venueView.setText(VenueName);
                System.out.println(VenueName);

//                String GigDate = gigJSON.getString("gig_date");
//                TextView DateView =(TextView)findViewById(R.id.LoactionGigDate);
//                DateView.setText(GigDate);

            } catch (JSONException e) {

                System.out.println("Json error getting band_id");
            }

        }

    }
}
