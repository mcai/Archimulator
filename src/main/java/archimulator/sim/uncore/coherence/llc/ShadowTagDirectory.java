package archimulator.sim.uncore.coherence.llc;

import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ShadowTagDirectory {
    private CacheGeometry geometry;
    private List<Set<Integer>> sets;

    public ShadowTagDirectory(FirstLevelCache l1Cache) {
        this.geometry = l1Cache.getCache().getGeometry();

        this.sets = new ArrayList<Set<Integer>>();
        for (int i = 0; i < this.geometry.getNumSets(); i++) {
            this.sets.add(new TreeSet<Integer>());
        }
    }

    public boolean containsTag(int addr) {
        return this.sets.get(this.getSet(addr)).contains(this.getTag(addr));
    }

    public void addTag(int addr) {
        this.sets.get(this.getSet(addr)).add(this.getTag(addr));
    }

    public void removeTag(int addr) {
        this.sets.get(this.getSet(addr)).remove(this.getTag(addr));
    }

    public int getTag(int addr) {
        return CacheGeometry.getTag(addr, this.geometry);
    }

    public int getSet(int addr) {
        return CacheGeometry.getSet(addr, this.geometry);
    }
}
