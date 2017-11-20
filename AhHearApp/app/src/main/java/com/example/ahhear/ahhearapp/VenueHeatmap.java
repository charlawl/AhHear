package com.example.ahhear.ahhearapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import ca.hss.heatmaplib.HeatMap;


public class VenueHeatmap extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private class DownloadHeatData extends AsyncTask<URL, Integer, double[][]> {

        private Activity activity;

        private DownloadHeatData(Activity activity) {
            this.activity = activity;
        }

        protected double[][] doInBackground(URL... urls) {

            double[][]ReturnArray = new double[11][22];

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
                        JSONArray arr = new JSONArray(sb.toString());
                        for (int j = 0; j < arr.length(); j++) {
                            JSONArray temp = arr.getJSONArray(j);
                            for (int b = 0; b < temp.length(); b++) {
                                ReturnArray[j][b] = temp.getDouble(b);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Escape early if cancel() is called
                if (isCancelled()) break;
            }

            return ReturnArray;
        }

        protected void onPostExecute(double[][] ReturnArray) {

            Toast toast = Toast.makeText(getApplicationContext(), "HeatData Downloaded", Toast.LENGTH_SHORT);
            toast.show();

            setContentView(R.layout.heatmap);

            HeatMap heatMap = findViewById(R.id.heatmap);
            heatMap.setMinimum(0.0);
            heatMap.setMaximum(100.0);

            // Make the colour gradient from green / yellow / red.
            Map<Float, Integer> colorStops = new ArrayMap<>();
            colorStops.put(0.0f, 0xff00ff00);
            colorStops.put(0.5f, 0xffffff00);
            colorStops.put(1.0f, 0xffff0000);
            heatMap.setColorStops(colorStops);
            heatMap.setRadius(500);

            for (int i = 0; i < ReturnArray.length; i++) {
                for (int j = 0; j < ReturnArray[i].length; j++) {
                    Float x = (float) i / 20;
                    Float y = (float) j / 20;

                    HeatMap.DataPoint point = new HeatMap.DataPoint(x, y, ReturnArray[i][j]);
                    heatMap.addData(point);
                }
            }

            // get the width of the current screen.
            DisplayMetrics displaymetrics = new DisplayMetrics();

            activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int screenWidth = displaymetrics.widthPixels;

            // multiply that by the size ratio of the layout image.
            Double heatmap_height_double = screenWidth * 0.61194;
            int heatmap_height = heatmap_height_double.intValue();

            // now set the heatmap Relative Layout container height.
            RelativeLayout rl = findViewById(R.id.heatmap_container);
            rl.getLayoutParams().height = heatmap_height;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DownloadHeatData downloadHeatData = new DownloadHeatData(this);
        try {

            Uri.Builder builder = new Uri.Builder();

            builder.scheme("http");
            builder.encodedAuthority("10.0.2.2:8000");
            builder.appendPath("heatmap");
            builder.appendQueryParameter("gig_id","2");
            builder.build();

            downloadHeatData.execute(new URL(builder.toString()));

        } catch (MalformedURLException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }

    }
}

//        double[][] multi = new double[][]{
//                { 25.0, 0.0, 0, 0, 0, 0, 0, 0, 0, 0, 50.0, 25.0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 50.0 },
//                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
//                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
//                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
//                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
//                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
//                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
//                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
//                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 80.0, 78.0 },
//                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 80.0, 78.0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 80.0, 78.0 },
//                { 75.0, 0, 0, 0, 0, 0, 0, 0, 0, 86.0, 74.0 }
//        };