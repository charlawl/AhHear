package ViewComponents;

/**
 * Created by charlottehearne on 26/11/2017.
 */

class ListData {

    private int id, numGigs, numSamples, decibels;
    private String name;

    ListData(int id, String name, int numGigs, int numSamples, int decibels) {
        this.id = id;
        this.numGigs = numGigs;
        this.numSamples = numSamples;
        this.decibels = decibels;
        this.name = name;
    }

    public int getId(){ return id;}

    public int getNumGigs() {
        return numGigs;
    }

    public int getNumSamples() {
        return numSamples;
    }

    public int getDecibels() {
        return decibels;
    }
    public String getName(){
        return name;
    }

}
