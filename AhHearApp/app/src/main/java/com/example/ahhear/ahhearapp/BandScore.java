package com.example.ahhear.ahhearapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;

import ViewComponents.Band;
import ViewComponents.BandListItem;
import ViewComponents.DownloadImage;
import ViewComponents.Venue;
import ViewComponents.VenueListItem;

public class BandScore extends AppCompatActivity{

    ArrayList<Venue> venues;
    ListView listView;
    private static VenueListItem listItem;
    ArrayList<Venue> result = new ArrayList<>();

    class DownloadVenuesTask extends AsyncTask<URL, Integer, ArrayList<Venue>> {
        private Activity activity;

        public DownloadVenuesTask(Activity activity) {
            this.activity = activity;
        }

        protected ArrayList<Venue> doInBackground(URL... urls) {
            int count = urls.length;

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
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject venue = arr.getJSONObject(i);
                            int numGigs = venue.isNull("num_gigs")? 0: venue.getInt("num_gigs");
                            int numSamples = venue.isNull("num_samples")? 0: venue.getInt("num_samples");
                            int avgSamples = venue.isNull("avg_samples")? 0: venue.getInt("avg_samples");
                            int gigId = venue.isNull("gig_id")? 0: venue.getInt("gig_id");
                            double locationLng = venue.isNull("location_lng")? 0: venue.getDouble("location_lng");
                            double locationLat = venue.isNull("location_lat")? 0: venue.getDouble("location_lat");

                            result.add(new Venue(
                                    venue.getInt("venue_id"),
                                    venue.getString("venue_name"),
                                    numGigs,
                                    numSamples,
                                    avgSamples,
                                    gigId,
                                    locationLng,
                                    locationLat
                            ));
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
            return result;
        }

        protected void onPostExecute(ArrayList<Venue> result) {
            Toast toast = Toast.makeText(getApplicationContext(), "Venues downloaded", Toast.LENGTH_SHORT);
            toast.show();

            listItem = new VenueListItem(result, getApplicationContext());
            listView.setAdapter(listItem);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.band_score);
        int id = getIntent().getIntExtra("bandId", 0);
        String name = getIntent().getStringExtra("bandName");
        int gigs = getIntent().getIntExtra("bandNumGigs", 0);
        int samples = getIntent().getIntExtra("bandNumSamples", 0);
        int db = getIntent().getIntExtra("bandDecibels", 0);

        TextView bandNameBandScorePage = (TextView)findViewById(R.id.bandNameBandScorePage);
        TextView bandGigCountBandScorePage = (TextView)findViewById(R.id.bandGigCountBandScorePage);
        TextView bandDbBandScorePage = (TextView)findViewById(R.id.bandDbBandScorePage);

        bandNameBandScorePage.setText(name);
        bandGigCountBandScorePage.setText(getString(R.string.gigs, gigs));
        bandDbBandScorePage.setText(getString(R.string.decibelsAvg, db));

        DownloadImage downloadBandImage = new DownloadImage((ImageView)findViewById(R.id.bandImageBandScorePage));

        try {
            downloadBandImage.execute(new URL("http", "gavs.work", 8000, "band_image?id="+id));
        } catch (MalformedURLException e) {
            Toast toasterr = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
            toasterr.show();
            e.printStackTrace();
        }

        listView = (ListView) findViewById(R.id.venuesListBandScorePage);
        DownloadVenuesTask downloadVenuesTask = new DownloadVenuesTask(this);

        try {
            downloadVenuesTask.execute(
                    new URL("http", "gavs.work", 8000, "gigs?band="+id));

        } catch (MalformedURLException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }

        // Code to open the venue score page after clicking one of the list view items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent myIntent = new Intent(view.getContext(), VenueHeatmap.class);
                myIntent.putExtra("gigId", result.get(position).getGigid());
                startActivityForResult(myIntent, 0);
            }
        });
    }
}