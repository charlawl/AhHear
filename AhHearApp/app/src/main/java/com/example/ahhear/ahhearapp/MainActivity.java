package com.example.ahhear.ahhearapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    Button bandBrowse, venueBrowse, record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This makes the back button in the action bar
        // The parent category needs to be declared in the android manifest for it to work
        if(getActionBar() != null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // This section for each button identifies each button and says which
        // activity to open when they are clicked

        // Browse by bands button
        bandBrowse = (Button) findViewById(R.id.home_bands_btn);
        bandBrowse.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, BandBrowse.class));
            }
        });

        // Browse by venues button
        venueBrowse = (Button) findViewById(R.id.home_venues_btn);
        venueBrowse.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, VenueBrowse.class));
            }
        });

        // Record button
        record = (Button) findViewById(R.id.home_record_btn);
        record.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, GigBrowse.class));
            }
        });
    }

    // This just creates the menu which is designed in the menu.xml file
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    // This function says where each item in the menu should go to when clicked
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.menu_bands_btn:
                startActivity(new Intent(this, BandBrowse.class));
                return true;
            case R.id.menu_venues_btn:
                startActivity(new Intent(this, VenueBrowse.class));
                return true;
            case R.id.menu_record_btn:
                startActivity(new Intent(this, GigBrowse.class));
                return true;
            case R.id.menu_recordings_btn:
                startActivity(new Intent(this, MyRecordings.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
