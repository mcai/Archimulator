package archimulator.sim.uncore.new1;

public interface Cache {

    //Required load and store methods
    public void load(long address, boolean verbose);

    public void store(long address, boolean verbose);

    public void remoteLoad(long address, boolean hit, boolean verbose);

    public void remoteStore(long address, boolean hit, boolean verbose);

    //Required utility methods
    public void printCaches();

    public void setOtherCaches(Cache[] caches);

    public int getOperations();

    public int getHits();

    public float getHitRate();

    public int getInvalidations();
}