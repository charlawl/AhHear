package com.example.ahhear.ahhearapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;
import java.util.Map;
import ca.hss.heatmaplib.HeatMap;


public class VenueHeatmap extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heatmap);

        HeatMap heatMap = findViewById(R.id.heatmap);
        heatMap.setMinimum(0.0);
        heatMap.setMaximum(100.0);

        // Make the colour gradient from green / yellow / red.
        Map<Float, Integer> colorStops = new ArrayMap<>();
        colorStops.put(0.0f, 0xff00ff00);
        colorStops.put(0.5f, 0xffffff00);
        colorStops.put(1.0f, 0xffff0000);
        heatMap.setColorStops(colorStops);
        heatMap.setRadius(500);

        double[][] multi = new double[][]{
                { 25.0, 0.0, 0, 0, 0, 0, 0, 0, 0, 0, 50.0, 25.0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 50.0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 80.0, 78.0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 80.0, 78.0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 80.0, 78.0 },
                { 75.0, 0, 0, 0, 0, 0, 0, 0, 0, 86.0, 74.0 }
        };

        for (int i = 0; i < multi.length; i++) {
            for (int j = 0; j < multi[i].length; j++) {
                Float x = (float) i / 20;
                Float y = (float) j / 20;

                System.out.println(x);
                System.out.println(y);

                HeatMap.DataPoint point = new HeatMap.DataPoint(x, y, multi[i][j]);
                heatMap.addData(point);
            }
        }

        // get the width of the current screen.
        DisplayMetrics displaymetrics = new DisplayMetrics();
        Activity activity = this;
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenWidth = displaymetrics.widthPixels;

        // multiply that by the size ratio of the layout image.
        Double heatmap_height_double = screenWidth * 0.61194;
        int heatmap_height = heatmap_height_double.intValue();

        // now set the heatmap Relative Layout container height.
        RelativeLayout rl = findViewById(R.id.heatmap_container);
        rl.getLayoutParams().height = heatmap_height;

    }
}