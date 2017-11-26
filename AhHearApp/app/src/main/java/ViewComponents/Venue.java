package ViewComponents;

import android.graphics.Bitmap;

/**
 * Created by charlottehearne on 05/11/2017.
 */

public class Venue extends ListData {

    private double locationLng, locationLat;

    public Venue(int id, String name, int numGigs, int numSamples, int decibels, double locationLng, double locationLat) {
        super(id, name, numGigs, numSamples, decibels);
        this.locationLng = locationLng;
        this.locationLat = locationLat;
    }


}
