package com.example.ahhear.ahhearapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

import ViewComponents.DownloadVenueImage;

public class VenueScore extends AppCompatActivity{


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

    }
}
