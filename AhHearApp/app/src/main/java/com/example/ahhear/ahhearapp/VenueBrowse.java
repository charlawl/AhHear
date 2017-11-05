package com.example.ahhear.ahhearapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

import ViewComponents.Venue;
import ViewComponents.VenueListItem;

public class VenueBrowse extends AppCompatActivity{

    ArrayList<Venue> venues;
    ListView listView;
    private static VenueListItem listItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_browse);

        listView = (ListView) findViewById(R.id.venuesList);
        venues = new ArrayList<>();
        venues.add(new Venue("Olympia Theatre", 25, 10, 96));
        venues.add(new Venue("Button Factory", 15, 6, 120));
        venues.add(new Venue("Twisted Pepper", 9, 3, 140));

        listItem = new VenueListItem(venues, getApplicationContext());
        listView.setAdapter(listItem);


    }
}
