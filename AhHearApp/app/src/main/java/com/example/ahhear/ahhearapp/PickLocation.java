package com.example.ahhear.ahhearapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class PickLocation extends AppCompatActivity {
    public static final String EXTRA_DECIBEL = "com.example.ahhear.MESSAGE";
    public static final String EXTRA_WIDTH = "com.example.ahhear.WIDTH";
    public static final String EXTRA_HEIGHT = "com.example.ahhear.HEIGHT";
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
    private int gigId;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pick_location);
//        set dynamic image // uses picasso for better memory management
        ImageView imageView = (ImageView) findViewById(R.id.floorplan);

        Picasso.with(this)
                .load("https://s8.postimg.org/iqy8i4nsl/transparent_floorplan.png")
                .into(imageView);

        
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

                        float x = event.getX();
                        float y = event.getY();

                        String uri = "@drawable/red_pin";
                        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                        ImageView pinview = (ImageView) findViewById(R.id.pin);
                        ImageView floorplanview = (ImageView) findViewById(R.id.floorplan);

                        float floorplan_width = floorplanview.getMeasuredHeight();
                        float floorplan_height = floorplanview.getMeasuredWidth();
                        float pin_width = pinview.getMeasuredHeight();
                        float pin_height = pinview.getMeasuredHeight();

                        chosen_percent_width = (x*100)/floorplan_width;
                        chosen_percent_height = (y*100)/floorplan_height;

                        System.out.println(chosen_percent_width);
                        System.out.println(chosen_percent_height);



                        if(pinview.getDrawable() == null) {

                            Drawable res = getResources().getDrawable(imageResource);
                            pinview.setAdjustViewBounds(true);
                            pinview.setImageDrawable(res);
                        }

                        pinview.setX(x - ((pin_width / 100) * 26));
                        pinview.setY(y-((pin_height / 100) * 86));

                        break;

                    default:
                        break;
                }
                return true;

            }
        }
    };


}
