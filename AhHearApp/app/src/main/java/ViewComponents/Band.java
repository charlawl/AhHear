package ViewComponents;

/**
 * Created by reggiemurphy on 25/11/2017.
 */

public class Band {

    String name;
    int id, numGigs, numSamples, decibels;

    public Band(int id, String name, int numGigs, int numSamples, int decibels) {
        this.id = id;
        this.name = name;
        this.numGigs = numGigs;
        this.numSamples = numSamples;
        this.decibels = decibels;
    }

    public int getId(){ return id;}

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
