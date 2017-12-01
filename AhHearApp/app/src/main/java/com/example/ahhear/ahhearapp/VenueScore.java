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

public class VenueScore extends AppCompatActivity{

    ArrayList<Band> bands;
    ListView listView;
    private static BandListItem listItem;
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
                        for(int i = 0; i < arr.length(); i++){
                            JSONObject band = arr.getJSONObject(i);
                            int numGigs = band.isNull("num_gigs")? 0: band.getInt("num_gigs");
                            int numSamples = band.isNull("num_samples")? 0: band.getInt("num_samples");
                            int avgSamples = band.isNull("avg_samples")? 0: band.getInt("avg_samples");
                            int gigId = band.isNull("gig_id")? 0: band.getInt("gig_id");
                            result.add(new Band(
                                    band.getInt("band_id"),
                                    band.getString("band_name"),
                                    numGigs,
                                    numSamples,
                                    avgSamples,
                                    gigId
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

        protected void onPostExecute(ArrayList<Band> result) {
//            Toast toast = Toast.makeText(getApplicationContext(), "Bands downloaded", Toast.LENGTH_SHORT);
//            toast.show();

            listItem = new BandListItem(result, getApplicationContext());
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
        System.out.println("ID IS: " + id);

        TextView venueNameVenueScorePage = (TextView)findViewById(R.id.venueNameVenueScorePage);
        TextView venueGigVenueScorePage = (TextView)findViewById(R.id.venueGigCountVenueScorePage);
        TextView venueDbVenueScorePage = (TextView)findViewById(R.id.venueDbVenueScorePage);

        venueNameVenueScorePage.setText(name);
        venueGigVenueScorePage.setText(getString(R.string.gigs, gigs));
        venueDbVenueScorePage.setText(getString(R.string.decibelsAvg, db));

        DownloadImage downloadVenueImage = new DownloadImage((ImageView)findViewById(R.id.venueImageVenueScorePage));

        try {
            downloadVenueImage.execute(new URL("http", "gavs.work", 8000, "venue_image?id="+id));
        } catch (MalformedURLException e) {
            Toast toasterr = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
            toasterr.show();
            e.printStackTrace();
        }

        listView = (ListView) findViewById(R.id.venuesListVenueScorePage);
        DownloadBandsTask downloadBandsTask = new DownloadBandsTask(this);

        try {
            downloadBandsTask.execute(
                    new URL("http", "gavs.work", 8000, "gigs?venue=" + id));

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