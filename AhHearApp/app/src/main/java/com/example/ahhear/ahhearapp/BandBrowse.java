package com.example.ahhear.ahhearapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class BandBrowse extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.band_browse);

        // Temp button to get to heatmap.
        Button heatmap = (Button) findViewById(R.id.goto_heatmap);
        heatmap.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(BandBrowse.this, VenueHeatmap.class));
            }
        });
    }
}