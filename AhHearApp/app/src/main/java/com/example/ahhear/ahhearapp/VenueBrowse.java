package com.example.ahhear.ahhearapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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

import ViewComponents.Venue;
import ViewComponents.VenueListItem;

public class VenueBrowse extends AppCompatActivity {

    ArrayList<Venue> venues;
    ListView listView;
    private static VenueListItem listItem;
    ArrayList<Venue> result = new ArrayList<>();

    //  background task to connect to the API as cannot connect to the internet on the main thread - using the AsyncTask class
    class DownloadVenuesTask extends AsyncTask<URL, Integer, ArrayList<Venue>> {
        private Activity activity;

        public DownloadVenuesTask(Activity activity) {
            this.activity = activity;
        }

        protected ArrayList<Venue> doInBackground(URL... urls) {
            int count = urls.length;

//          connecting to the API and reading in the JSON
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

//                  populating the number of samples, number of gigs etc. by looping through the JSON from the API
                    try {
                        JSONArray arr = new JSONArray(sb.toString());
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject venue = arr.getJSONObject(i);
                            int numGigs = venue.isNull("num_gigs")? 0: venue.getInt("num_gigs");
                            int numSamples = venue.isNull("num_samples")? 0: venue.getInt("num_samples");
                            int avgSamples = venue.isNull("avg_samples")? 0: venue.getInt("avg_samples");
                            double locationLng = venue.isNull("location_lng")? 0: venue.getDouble("location_lng");
                            double locationLat = venue.isNull("location_lat")? 0: venue.getDouble("location_lat");

                            result.add(new Venue(
                                    venue.getInt("id"),
                                    venue.getString("name"),
                                    numGigs,
                                    numSamples,
                                    avgSamples,
                                    1,
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
//            Toast toast = Toast.makeText(getApplicationContext(), "Venues downloaded", Toast.LENGTH_SHORT);
//            toast.show();

            listItem = new VenueListItem(result, getApplicationContext());
            listView.setAdapter(listItem);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_browse);

        listView = (ListView) findViewById(R.id.venuesList);
        DownloadVenuesTask downloadVenuesTask = new DownloadVenuesTask(this);

        try {
            downloadVenuesTask.execute(
                    new URL("http", "gavs.work", 8000, "venues_list"));

        } catch (MalformedURLException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }

        // Code to open the venue score page after clicking one of the list view items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent myIntent = new Intent(view.getContext(), VenueScore.class);
                myIntent.putExtra("venueId", result.get(position).getId());
                myIntent.putExtra("venueName", result.get(position).getName());
                myIntent.putExtra("venueNumGigs", result.get(position).getNumGigs());
                myIntent.putExtra("venueNumSamples", result.get(position).getNumSamples());
                myIntent.putExtra("venueDecibels", result.get(position).getDecibels());
                startActivityForResult(myIntent, 0);
            }
        });
    }
}