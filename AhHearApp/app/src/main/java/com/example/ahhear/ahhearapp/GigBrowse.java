package com.example.ahhear.ahhearapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ViewComponents.Band;
import ViewComponents.BandListItem;
import ViewComponents.GigListItem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class GigBrowse extends AppCompatActivity {

    ArrayList<Band> bands;
    ListView listView;
    private static GigListItem listItem;
    ArrayList<Band> result = new ArrayList<>();

    private class DownloadBandsTask extends AsyncTask<URL, Integer, ArrayList<Band>> {
        private Activity activity;
        public DownloadBandsTask(Activity activity){
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
                            result.add(new Band(
                                    band.getInt("band_id"),
                                    band.getString("band_name"),
                                    numGigs,
                                    numSamples,
                                    avgSamples,
                                    band.getInt("gig_id")
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

            listItem = new GigListItem(result, getApplicationContext());
            listView.setAdapter(listItem);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.band_browse);

        listView = (ListView) findViewById(R.id.bandsList);
        DownloadBandsTask downloadBandsTask = new DownloadBandsTask(this);

        try {
            downloadBandsTask.execute(
                    new URL("http", "gavs.work", 8000, "todays_gigs"));

        } catch (MalformedURLException e) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }

        // Code to open the band score page after clicking one of the list view items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent myIntent = new Intent(view.getContext(), PickLocation.class);
                myIntent.putExtra("gig_id", result.get(position).getGigid());
                startActivityForResult(myIntent, 0);
            }
        });
    }
}
