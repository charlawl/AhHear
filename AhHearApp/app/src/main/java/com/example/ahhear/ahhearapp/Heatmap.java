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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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


public class Heatmap extends AppCompatActivity {

    // Class to download data from API asyncrosnally.
    // Returns a json array
    // takes an url and returns the result as a json array.
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

                        // try to parse the downloaded data.
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
        // use the downloaded data to display the heatmap and other downloaded information.
        protected void onPostExecute(final JSONArray ReturnArray) {

            setContentView(R.layout.heatmap);

            try {

                // Try to find the image of the band
                // using Picasso which is asynchronous.
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
                Log.e("AhHere", "exception: " + e.getMessage());
                System.out.println("Could not download band image.");
            }

            try {

                // Try to find the image of the venue.
                // using Picasso which is asynchronous.

                JSONObject bandJson = ReturnArray.getJSONObject(0);
                String VenueId = bandJson.getString("venue_id");

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
                floorplanbuilder.encodedAuthority("gavs.work:8000");
                floorplanbuilder.appendPath("floorplan_image");
                floorplanbuilder.appendQueryParameter("id", VenueId);
                floorplanbuilder.build();

                // When venue is finished downloading build the heatmap.
                ImageView FloorplanImage = (ImageView) findViewById(R.id.heatmapFloorplan);
                Picasso.with(activity)
                        .load(floorplanbuilder.toString())
                        .into(FloorplanImage, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {

                                // we need to find the height and width of the floorplan so the
                                // heatmap overlays correctly.
                                ImageView FloorplanImage = (ImageView) findViewById(R.id.heatmapFloorplan);
                                float image_height = FloorplanImage.getDrawable().getIntrinsicHeight();
                                float image_width = FloorplanImage.getDrawable().getIntrinsicWidth();
                                double ratio = image_height / image_width;

                                HeatMap heatMap = findViewById(R.id.heatmap);
                                // set max loudness (red) for the heatmap.
                                heatMap.setMinimum(0.0);
                                heatMap.setMaximum(100.0);

                                // get the width of the current screen.
                                DisplayMetrics displaymetrics = new DisplayMetrics();

                                activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                                int screenWidth = displaymetrics.widthPixels;

                                // multiply that by the size ratio of the layout image.
                                Double heatmap_height_double = screenWidth * ratio;
                                int heatmap_height = heatmap_height_double.intValue();

                                // now set the heatmap Relative Layout container height.
                                RelativeLayout rl = findViewById(R.id.heatmap_container);
                                rl.getLayoutParams().height = heatmap_height;
                                rl.getLayoutParams().height = heatmap_height;

                                // Make the colour gradient from green / yellow / red.
                                Map<Float, Integer> colorStops = new ArrayMap<>();
                                colorStops.put(0.0f, 0xff00ff00);
                                colorStops.put(0.5f, 0xffffff00);
                                colorStops.put(1.0f, 0xffff0000);
                                heatMap.setColorStops(colorStops);
                                heatMap.setRadius(900);

                                for (int i = 0; i < ReturnArray.length(); i++) {
                                    HeatMap.DataPoint point = null;
                                    try {
                                        JSONObject json = ReturnArray.getJSONObject(i);

                                        // get the downloaded values and convert them to a format that
                                        // the heatmap understands...
                                        // namely 0.1 == 10%.
                                        float xpercent_download = (float) json.getDouble("xpercent");
                                        float xpercent = xpercent_download / 100;

                                        float ypercent_download = (float) json.getDouble("ypercent");
                                        float ypercent = ypercent_download / 100;

                                        double spl = json.getDouble("spl");

                                        // add data to the heatmap point by point.
                                        point = new HeatMap.DataPoint(xpercent, ypercent, spl);
                                        heatMap.addData(point);

                                    } catch (JSONException e) {
                                        Log.e("AhHere", "exception: " + e.getMessage());
                                        System.out.print("JSON Exception. Error parsing heatmap data.");
                                    }
                                }

                            }

                            @Override
                            public void onError() {

                            }
                        });

            } catch (JSONException e) {
                Log.e("AhHere", "exception: " + e.getMessage());
                System.out.println("Could not get floorplan");
            }

            try {

                // add data downloaded from the API to views.
                JSONObject bandJson = ReturnArray.getJSONObject(0);

                String BandName = bandJson.getString("band_name");
                TextView bandView = (TextView)findViewById(R.id.HeatmapBandName);
                bandView.setText(BandName);

                String VenueName = bandJson.getString("venue_name");
                TextView venueView=(TextView)findViewById(R.id.HeatmapVenueName);
                venueView.setText(VenueName);

                // parse the downloaded datetime format.
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
                    Log.e("AhHere", "exception: " + e.getMessage());
                    System.out.println("Could not parse datetime from API.");
                }

                // do the math to get the average spl value.
                StringBuilder data = new StringBuilder();
                data.append("Average SPL: ");

                double val;
                float AvgString;
                try {
                    val = ((Number) bandJson.get("avg_samples")).doubleValue();
                    AvgString = (float) val;
                } catch (JSONException e){
                    val = 0.0;
                    AvgString = (float) val;
                }


                TextView volumeheading =(TextView)findViewById(R.id.gigvolume);
                volumeheading.setText("Gig Volume");

                // Limit the number of decimal places in the result to 2.
                data.append(String.format("%.2f", AvgString));

                data.append(" | Number Samples: ");
                String NumString = bandJson.getString("num_samples");
                data.append(NumString);

                TextView dataView =(TextView)findViewById(R.id.HeatmapData);
                dataView.setText(data.toString());

            } catch (JSONException e) {
                Log.e("AhHere", "exception: " + e.getMessage());
                System.out.println("Json error getting results from return array.");

                String VenueName = "No Samples Available for this Band.";
                TextView venueView=(TextView)findViewById(R.id.HeatmapVenueName);
                venueView.setText(VenueName);


            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the gig_id sent by the previous activity.
        int gig_id = getIntent().getIntExtra("gigId", 0);

        DownloadHeatData downloadHeatData = new DownloadHeatData(this);
        try {
            // this is the url to the API.
            // Static at the moment but its easy to add which gig were looking at.
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http");

            builder.encodedAuthority("gavs.work:8000");
            builder.appendPath("gig_recordings");

            builder.appendQueryParameter("gig_id", String.valueOf(gig_id));
            builder.build();
            downloadHeatData.execute(new URL(builder.toString()));

        } catch (MalformedURLException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
            toast.show();

            Log.e("AhHere", "exception: " + e.getMessage());
            System.out.println("Malformed Url.");
        }
    }

    // This method is taken from stackoverflow flow here:
    // https://stackoverflow.com/questions/23475788/how-to-set-multiple-parent-activities-for-using-android-back-button
    // It allows this activity to remember which activity called it.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return  true;
        }
        return super.onOptionsItemSelected(item);

    }

    // This method gets called when the find ear plugs button is clicked.
    // Opens google maps and searches for pharmacies.
    public void onClickBtn(View v) {
        Uri searchUri = Uri.parse("geo:0,0?q=pharmacy");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, searchUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }
}
