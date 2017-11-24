package com.example.ahhear.ahhearapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class DisplayLevel extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_levels);


        Intent intent = getIntent();
        String decibels = intent.getStringExtra(PickLocation.EXTRA_DECIBEL);
        TextView decibelDisplay = findViewById(R.id.textView_decibel);
        decibelDisplay.setText(decibels);
    }
}
