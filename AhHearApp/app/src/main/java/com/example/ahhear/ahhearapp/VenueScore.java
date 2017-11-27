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
import ViewComponents.DownloadVenueImage;
import ViewComponents.Venue;
import ViewComponents.VenueListItem;

public class VenueScore extends AppCompatActivity{

    ArrayList<Band> bands;
    ListView listView;
    private static VenueListItem listItem;
    ArrayList<Band> result = new ArrayList<>();

    private class DownloadBandsTask extends AsyncTask<URL, Integer, ArrayList<Band>> {
        private Activity activity;

        public DownloadBandsTask(Activity activity) {
            this.activity = activity;
        }

        protected ArrayList<Band> doInBackground(URL... urls) {
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
                            JSONObject band = arr.getJSONObject(i);
                            System.out.println(band.getString("name"));
                            result.add(new Band(
                                    1,
                                    band.getString("name"),
                                    12,
                                    14,
                                    2
//                            result.add(new Band(
//                                    band.getInt("id"),
//                                    band.getString("name"),
//                                    band.getInt("numGigs"),
//                                    band.getInt("numSamples"),
//                                    band.getInt("decibels")
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
        setContentView(R.layout.venue_score);
        int id = getIntent().getIntExtra("venueId", 0);
        String name = getIntent().getStringExtra("venueName");
        int gigs = getIntent().getIntExtra("venueNumGigs", 0);
        int samples = getIntent().getIntExtra("venueNumSamples", 0);
        int db = getIntent().getIntExtra("venueDecibels", 0);

        TextView venueNameVenueScorePage = (TextView)findViewById(R.id.venueNameVenueScorePage);
        TextView venueGigVenueScorePage = (TextView)findViewById(R.id.venueGigCountVenueScorePage);
        TextView venueDbVenueScorePage = (TextView)findViewById(R.id.venueDbVenueScorePage);

        venueNameVenueScorePage.setText(name);
        venueGigVenueScorePage.setText(getString(R.string.gigs, gigs));
        venueDbVenueScorePage.setText(getString(R.string.decibelsAvg, db));

        DownloadVenueImage downloadVenueImage = new DownloadVenueImage((ImageView)findViewById(R.id.venueImageVenueScorePage));

        try {
            downloadVenueImage.execute(new URL("http", "10.0.2.2", 8000, "images?id="+id));
        } catch (MalformedURLException e) {
            Toast toasterr = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
            toasterr.show();
            e.printStackTrace();
        }

        listView = (ListView) findViewById(R.id.venuesListVenueScorePage);
        VenueScore.DownloadBandsTask downloadVenuesTask = new VenueScore.DownloadBandsTask(this);

        try {
            downloadVenuesTask.execute(
                    new URL("http", "10.0.2.2", 8000, "venuescore?venue_name="+name));

        } catch (MalformedURLException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }
    }
}