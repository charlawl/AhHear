package com.example.ahhear.ahhearapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import ca.hss.heatmaplib.HeatMap;


public class VenueHeatmap extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private class DownloadHeatData extends AsyncTask<URL, Integer, JSONArray> {

        // record this activity as a variable for later.
        // used to get the width of the screen for the heatmap.
        private Activity activity;

        private DownloadHeatData(Activity activity) {
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

            setContentView(R.layout.heatmap);

            HeatMap heatMap = findViewById(R.id.heatmap);
            heatMap.setMinimum(0.0);
            heatMap.setMaximum(100.0);

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


            // Make the colour gradient from green / yellow / red.
            Map<Float, Integer> colorStops = new ArrayMap<>();
            colorStops.put(0.0f, 0xff00ff00);
            colorStops.put(0.5f, 0xffffff00);
            colorStops.put(1.0f, 0xffff0000);
            heatMap.setColorStops(colorStops);
            heatMap.setRadius(900);

            try {

                JSONObject bandJson = ReturnArray.getJSONObject(0);
                String BandId = bandJson.getString("band_id");

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.encodedAuthority("gavs.work:8000");
                builder.appendPath("band_image");
                builder.appendQueryParameter("id", BandId);
                builder.build();

                ImageView BandImage = (ImageView) findViewById(R.id.BandImage);
                Picasso.with(activity).load(builder.toString()).into(BandImage);

            } catch (JSONException e) {
                System.out.println("Json error getting band_id");
            }

            try {

                JSONObject bandJson = ReturnArray.getJSONObject(0);
                String VenueId = bandJson.getString("venue_id");
                System.out.println(VenueId);

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.encodedAuthority("gavs.work:8000");
                builder.appendPath("venue_image");
                builder.appendQueryParameter("id", VenueId);
                builder.build();

                ImageView VenueImage = (ImageView) findViewById(R.id.VenueImage);
                Picasso.with(activity).load(builder.toString()).into(VenueImage);

                Uri.Builder floorplanbuilder = new Uri.Builder();
                floorplanbuilder.scheme("http");
                floorplanbuilder.encodedAuthority("10.0.2.2:8000");
                floorplanbuilder.appendPath("floorplan_image");
                floorplanbuilder.appendQueryParameter("id", VenueId);
                floorplanbuilder.build();

                ImageView FloorplanImage = (ImageView) findViewById(R.id.heatmapFloorplan);
                Picasso.with(activity).load(floorplanbuilder.toString()).into(FloorplanImage);


            } catch (JSONException e) {
                System.out.println("Json error getting venue_id");
            }

            try {

                JSONObject bandJson = ReturnArray.getJSONObject(0);

                String BandName = bandJson.getString("band_name");
                TextView bandView = (TextView)findViewById(R.id.HeatmapBandName);
                bandView.setText(BandName);

                String VenueName = bandJson.getString("venue_name");
                TextView venueView=(TextView)findViewById(R.id.HeatmapVenueName);
                venueView.setText(VenueName);


                String gigDateString = bandJson.getString("datetime");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dt1 = new SimpleDateFormat("HH:mm dd-MM-yyyy");
                Date date;
                try {

                    date = df.parse(gigDateString);
                    String newDateString = dt1.format(date);
                    TextView dateView =(TextView)findViewById(R.id.HeatmapDate);
                    dateView.setText(newDateString);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                StringBuilder data = new StringBuilder();
                data.append("Average SPL: ");
                String AvgString = bandJson.getString("avg_samples");
                data.append(AvgString);

                data.append(" | Number Samples: ");
                String NumString = bandJson.getString("num_samples");
                data.append(NumString);

                TextView dataView =(TextView)findViewById(R.id.HeatmapData);
                dataView.setText(data.toString());

            } catch (JSONException e) {
                System.out.println("Json error getting venue_id");
            }

            for (int i = 0; i < ReturnArray.length(); i++) {
                HeatMap.DataPoint point = null;
                try {
                    JSONObject json = ReturnArray.getJSONObject(i);

                    System.out.println(json.getString("band_id"));
                    float xpercent_temp = (float) json.getDouble("xpercent");
                    float xpercent = xpercent_temp / 100;

                    float ypercent_temp = (float) json.getDouble("ypercent");
                    float ypercent = ypercent_temp / 100;

                    double spl = json.getDouble("spl");
                    point = new HeatMap.DataPoint(xpercent, ypercent, spl);
                    heatMap.addData(point);

                } catch (JSONException e) {
                    System.out.print("JSON Exception. (Line 137 of VenueHeatmap.java)");
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Intent intent = getIntent();
//        String gig_id = intent.getStringExtra("gig_id");

        setContentView(R.layout.heatmap);
        Button mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri searchUri = Uri.parse("geo:0,0?q=pharmacy");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, searchUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        DownloadHeatData downloadHeatData = new DownloadHeatData(this);
        try {

            // this is the url to the API.
            // Static at the moment but its easy to add which gig were looking at.
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http");

            builder.encodedAuthority("10.0.2.2:8000");
            builder.appendPath("gig_recordings");
//            builder.appendQueryParameter("gig_id", gig_id);
            builder.appendQueryParameter("gig_id", "2");
            builder.build();
            downloadHeatData.execute(new URL(builder.toString()));

        } catch (MalformedURLException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }
    }
}