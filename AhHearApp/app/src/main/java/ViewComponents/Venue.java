package ViewComponents;

/**
 * Created by charlottehearne on 05/11/2017.
 */

public class Venue {

    String name;
    int numGigs, numSamples, decibels;

    public Venue(String name, int numGigs, int numSamples, int decibels) {
        this.name = name;
        this.numGigs = numGigs;
        this.numSamples = numSamples;
        this.decibels = decibels;
    }

    public String getVenueName() {
        return name;
    }

    public int getNumGigs() {
        return numGigs;
    }

    public int getNumSamples() {
        return numSamples;
    }

    public int getDecibels() {
        return decibels;
    }
}
