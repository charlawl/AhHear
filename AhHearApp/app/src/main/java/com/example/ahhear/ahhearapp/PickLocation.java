package com.example.ahhear.ahhearapp;

import android.content.Intent;
import android.media.MediaRecorder;
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

public class PickLocation extends AppCompatActivity {
    public static final String EXTRA_DECIBEL = "com.example.ahhear.MESSAGE";
    private Button mRecordButton;
    private TextView mRecordLabel;
    private TextView mDecibels;
    private MediaRecorder mRecorder;
    private double amplitudeDb;
    private Intent intent;
    private Timer timer = new Timer();
    private static final String LOG_TAG = "Record Log";
    final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_location);
//        set dynamic image // uses picasso for better memory management
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

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
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    mRecordLabel.setText(R.string.stop_record);
                    stopRecording();
                }
                return false;
            }
        });
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
        timer.scheduleAtFixedRate(new RecorderTask(handler), 0, 1000);

    }
    private void stopRecording() {
        intent = new Intent(this, DisplayLevel.class);
        intent.putExtra(EXTRA_DECIBEL, Double.toString(Math.round(amplitudeDb)));
        timer.cancel();
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
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
            Log.e("----------->>>>>>>>>>","MicInfoService amplitude: " + amplitudeDb);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    System.out.println("------------------*********_____________------------------");
                    mDecibels.setText(String.format("Amplitude : %s", amplitudeDb));
                }
            });
        }
    }

}
