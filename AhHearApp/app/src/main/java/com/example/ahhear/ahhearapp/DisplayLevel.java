package com.example.ahhear.ahhearapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import de.nitri.gauge.Gauge;

public class DisplayLevel extends AppCompatActivity {
    private PostSample postSample;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_levels);

        Intent intent = getIntent();

        Bundle bundle = getIntent().getExtras();
//        various levels and their meanings
        final double decibels = bundle.getDouble(PickLocation.EXTRA_DECIBEL);
        TextView decibelDisplay = findViewById(R.id.value_info);
        String micValueMeaning;
        if (decibels <= 10)
            micValueMeaning = "Hearing threshold";
        else if(decibels <= 20 && decibels > 10)
            micValueMeaning = "Rustling leaves in the distance";
        else if(decibels <=30 && decibels > 20)
            micValueMeaning = "Background in TV studio";
        else if(decibels <= 40 && decibels > 30)
            micValueMeaning = " Quiet bedroom at night";
        else if(decibels <= 50 && decibels > 40)
            micValueMeaning = "Quiet library";
        else if(decibels <= 60 && decibels > 50)
            micValueMeaning = "Average home";
        else if(decibels <= 70 && decibels > 60)
            micValueMeaning = "Conversational speech, 1 m";
        else if(decibels <= 80 && decibels > 70)
            micValueMeaning = "Vacuum cleaner, distance 1 m";
        else if(decibels <= 90 && decibels > 80)
            micValueMeaning = "Kerbside of busy road, 5 m";
        else if(decibels <= 100 && decibels > 90)
            micValueMeaning = "Diesel truck, 10 m away";
        else if(decibels <= 110 && decibels > 100)
            micValueMeaning = "Disco, 1 m from speaker";
        else if(decibels <= 120 && decibels > 110)
            micValueMeaning = "Chainsaw, 1 m distance";
        else if(decibels <= 130 && decibels > 120)
            micValueMeaning = "Threshold of discomfort";
        else if(decibels <= 140 && decibels > 130)
            micValueMeaning = "Threshold of pain";
        else if(decibels <= 150)
            micValueMeaning = "Jet aircraft, 50 m away";
        else
            micValueMeaning = "An Error Occurred";

        decibelDisplay.setText(micValueMeaning);
//        feed the guage with read value
        final Gauge gauge = (Gauge) findViewById(R.id.gauge);
        gauge.moveToValue((float) decibels);

        // print the percentage positions from the previous page.
        final int gigId = bundle.getInt(PickLocation.EXTRA_GIGID);
//        final int gigId = 2;
        final float chosen_percent_width = bundle.getFloat(PickLocation.EXTRA_WIDTH);
        final float chosen_percent_height = bundle.getFloat(PickLocation.EXTRA_HEIGHT);

        System.out.println(chosen_percent_width);
        System.out.println(chosen_percent_height);
        Button button = (Button) findViewById(R.id.postButton);
//        posts to remote api
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postSample = new PostSample(decibels, chosen_percent_width, chosen_percent_height, gigId);

                try {
                    postSample.execute(
                            new URL("http", "gavs.work", 8000, "input_recording"));

                } catch (MalformedURLException e) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
                    toast.show();
                    e.printStackTrace();
                }
            }
        });

    }
//    opens google maps
    public void onClickBtn(View view) {
        Uri searchUri = Uri.parse("geo:0,0?q=pharmacy");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, searchUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
// schema for sample db
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
            Toast toast = Toast.makeText(getApplicationContext(), "Results submitted! " , Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}
