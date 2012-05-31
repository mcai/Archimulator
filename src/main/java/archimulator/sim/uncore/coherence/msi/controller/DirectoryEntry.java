package archimulator.sim.uncore.coherence.msi.controller;

import java.util.ArrayList;
import java.util.List;

public class DirectoryEntry {
    private CacheController owner;
    private List<CacheController> sharers;

    public DirectoryEntry() {
        this.sharers = new ArrayList<CacheController>();
    }

    public CacheController getOwner() {
        return owner;
    }

    public void setOwner(CacheController owner) {
        this.owner = owner;
    }

    public List<CacheController> getSharers() {
        return sharers;
    }
}
