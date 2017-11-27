package com.example.ahhear.ahhearapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class DisplayLevel extends AppCompatActivity {
    private PostSample postSample;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_levels);

        Intent intent = getIntent();
        //TODO: get gig id from intent
        final int gigId = 2;

        Bundle bundle = getIntent().getExtras();

        final double decibels = bundle.getDouble(PickLocation.EXTRA_DECIBEL);
        TextView decibelDisplay = findViewById(R.id.textView_decibel);
        decibelDisplay.setText(Double.toString(decibels));

        // print the percentage positions from the previous page.
        final float chosen_percent_width = bundle.getFloat(PickLocation.EXTRA_WIDTH);
        final float chosen_percent_height = bundle.getFloat(PickLocation.EXTRA_HEIGHT);

        System.out.println(chosen_percent_width);
        System.out.println(chosen_percent_height);
        Button button = (Button) findViewById(R.id.postButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postSample = new PostSample(decibels, chosen_percent_width, chosen_percent_height, gigId);

                try {
                    postSample.execute(
                            new URL("http", "10.0.2.2", 8000, "input_recording"));

                } catch (MalformedURLException e) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
                    toast.show();
                    e.printStackTrace();
                }
            }
        });

    }

    private class PostSample extends AsyncTask<URL, Integer, Integer> {
        private double amplitudeDb;
        private float x, y;
        private int gigId;

        public PostSample(double amplitude, float x, float y, int gigId){
            this.amplitudeDb = amplitude;
            this.x = x;
            this.y = y;
            this.gigId = gigId;

        }
        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
            StringBuilder feedback = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first)
                    first = false;
                else
                    feedback.append("&");

                feedback.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                feedback.append("=");
                feedback.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            return feedback.toString();
        }


        @Override
        protected Integer doInBackground(URL... urls) {
            for (URL url : urls) {
                HttpURLConnection conn;
                String response = "";
                try {
                     conn = (HttpURLConnection) url.openConnection();
                } catch (IOException e){
                    e.printStackTrace();
                    break;
                }

                try {
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject params = new JSONObject();
                    params.put("spl", Double.toString(amplitudeDb));
                    params.put("xpercent", Float.toString(x));
                    params.put("ypercent", Float.toString(y));
                    params.put("gig_id", Integer.toString(gigId));

                    Log.i("JSON", params.toString());

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(params.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e){
                    e.printStackTrace();
                } finally {
                    if (conn != null) {// Make sure the connection is not null.
                        Log.i("CONN", conn.toString());
                        int resp = 0;
                        try {
                             resp = conn.getResponseCode();
                        } catch (IOException e){
                            e.printStackTrace();
                        } finally {
                            conn.disconnect();

                        }

                        return resp;

                    }
                }
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result){
            Toast toast = Toast.makeText(getApplicationContext(), "Results posted " + String.valueOf(result), Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}
