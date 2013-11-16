/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.*;
import net.pickapack.util.Reference;
import net.pickapack.util.ValueProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Dead block prediction based least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class DeadBlockPredictionBasedLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    /**
     * Boolean value provider.
     */
    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        boolean dead;

        /**
         * Create a boolean value provider.
         */
        public BooleanValueProvider() {
            this.state = true;
            this.dead = false;
        }

        /**
         * Get the state.
         *
         * @return the state
         */
        @Override
        public Boolean get() {
            return state;
        }

        /**
         * Get the initial state.
         *
         * @return the initial state
         */
        @Override
        public Boolean getInitialValue() {
            return true;
        }
    }

    /**
     * Dead block prediction sampler entry.
     */
    private class DeadBlockPredictionSamplerEntry {
        private int lruStackPosition;
        private int tag;
        private int trace;
        private boolean dead;
        private boolean valid;

        /**
         * Create a dead block prediction sampler entry.
         */
        private DeadBlockPredictionSamplerEntry() {
            lruStackPosition = 0;
            valid = false;
            tag = 0;
            trace = 0;
            dead = false;
        }
    }

    /**
     * Dead block prediction sampler set.
     */
    private class DeadBlockPredictionSamplerSet {
        private List<DeadBlockPredictionSamplerEntry> blocks;

        /**
         * Create a dead block prediction sampler set.
         */
        private DeadBlockPredictionSamplerSet() {
            this.blocks = new ArrayList<>();

            // initialize the LRU replacement algorithm for these entries
            for (int i = 0; i < danSamplerAssociativity; i++) {
                DeadBlockPredictionSamplerEntry block = new DeadBlockPredictionSamplerEntry();
                block.lruStackPosition = i;
                this.blocks.add(block);
            }
        }
    }

    /**
     * Dead block prediction sampler.
     */
    private class DeadBlockPredictionSampler {
        private List<DeadBlockPredictionSamplerSet> sets;

        private int samplerNumSets;   // number of sampler sets
        private int samplerModulus; // determines which LLC sets are sampler sets

        private DeadBlockPredictor deadBlockPredictor;

        /**
         * Create a dead block prediction sampler.
         *
         * @param numSets the number of sets in the parent cache
         */
        private DeadBlockPredictionSampler(int numSets) {
            // figure out number of entries in each table
            danPredictionTableEntries = 1 << danPredictorIndexBits;

            // compute the maximum saturating counter value; predictor constructor
            // needs this so we do it here
            danCounterMax = (1 << danCounterWidth) - 1;

            // make a predictor
            this.deadBlockPredictor = new DeadBlockPredictor();

            // figure out what should divide evenly into a set index to be
            // considered a sampler set
            this.samplerModulus = 8;

            // maximum number of sampler of sets we can afford with the space left over
            this.samplerNumSets = numSets / this.samplerModulus;

            // make the sampler sets
            this.sets = new ArrayList<>();
            for (int i = 0; i < samplerNumSets; i++) {
                this.sets.add(new DeadBlockPredictionSamplerSet());
            }
        }

        //TODO: to be refactored!!!
        /**
         * Access the sampler with an LLC tag.
         *
         * @param samplerSet the sampler set index
         * @param threadId the thread ID
         * @param pc the virtual PC address
         * @param tag the tag
         */
        void access(int samplerSet, int threadId, int pc, int tag) {
            if (samplerSet < 0 && samplerSet >= deadBlockPredictionSampler.samplerNumSets){
                throw new IllegalArgumentException(samplerSet + "");
            }

            // get a pointer to this samplerSet's sampler entries
            List<DeadBlockPredictionSamplerEntry> blocks = sets.get(samplerSet).blocks;

            // get a partial tag to search for
            int partialTag = tag & (1 << danSamplerTagBitsPerEntry) - 1;

            // this will be the way of the sampler entry we end up hitting or replacing
            int i;

            // search for a matching tag
            for (i = 0; i < danSamplerAssociativity; i++)
                if (blocks.get(i).valid && (blocks.get(i).tag == partialTag)) {
                    // we know this block is not dead; inform the predictor
                    deadBlockPredictor.update(threadId, blocks.get(i).trace, false);
                    break;
                }

            // did we find a match?
            if (i == danSamplerAssociativity) {
                // look for an invalid block to replace
                for (i = 0; i < danSamplerAssociativity; i++) if (!blocks.get(i).valid) break;

                // no invalid block?  look for a dead block.
                if (i == danSamplerAssociativity) {
                    // find the LRU dead block
                    for (i = 0; i < danSamplerAssociativity; i++) if (blocks.get(i).dead) break;
                }

                // no invalid or dead block?  use the LRU block
                if (i == danSamplerAssociativity) {
                    int j;
                    for (j = 0; j < danSamplerAssociativity; j++)
                        if (blocks.get(j).lruStackPosition == danSamplerAssociativity - 1) break;
                    i = j;
                }

                // previous trace leads to block being dead; inform the predictor
                deadBlockPredictor.update(threadId, blocks.get(i).trace, true);

                // fill the victim block
                blocks.get(i).tag = partialTag;
                blocks.get(i).valid = true;
            }

            // record the trace
            blocks.get(i).trace = makeTrace(pc);

            // get the next prediction for this entry
            blocks.get(i).dead = deadBlockPredictor.predict(threadId, blocks.get(i).trace);

            // now the replaced entry should be moved to the MRU position
            int position = blocks.get(i).lruStackPosition;
            for (int way = 0; way < danSamplerAssociativity; way++)
                if (blocks.get(way).lruStackPosition < position)
                    blocks.get(way).lruStackPosition++;
            blocks.get(i).lruStackPosition = 0;
        }
    }

    /**
     * Dead block predictor.
     */
    private class DeadBlockPredictor {
        private List<List<Reference<Integer>>> tables;

        /**
         * Create a dead block predictor.
         */
        private DeadBlockPredictor() {
            this.tables = new ArrayList<>();

            for (int i = 0; i < danNumPredictionTables; i++) {
                List<Reference<Integer>> table = new ArrayList<>();

                for (int j = 0; j < danPredictionTableEntries; j++) {
                    table.add(new Reference<>(0));
                }

                this.tables.add(table);
            }
        }

        /**
         * Hash a trace, thread ID, and prediction table number into a predictor table index.
         *
         * @param threadId the thread ID
         * @param trace the trace
         * @param table the prediction table number
         * @return the corresponding table index for the specified thread ID, trace and prediction table number
         */
        int getTableIndex(int threadId, int trace, int table) {
            int x = fi(trace ^ (threadId << 2), table);
            return x & ((1 << danPredictorIndexBits) - 1);
        }

        /**
         * Update the predictor when a block, either dead or not, is encountered.
         *
         * @param threadId the thread ID
         * @param trace the trace
         * @param dead a value indicating whether the block is dead or not
         */
        void update(int threadId, int trace, boolean dead) {
            // for each predictor table...
            for (int i = 0; i < danNumPredictionTables; i++) {
                // ...get a pointer to the corresponding entry in that table
                Reference<Integer> c = tables.get(i).get(getTableIndex(threadId, trace, i));

                // if the block is dead, increment the counter
                if (dead) {
                    if (c.get() < danCounterMax) {
                        c.set(c.get() + 1);
                    }
                } else {
                    // otherwise, decrease the counter
                    if (i % 2 == 1) {
                        // odd numbered tables decrease exponentially
                        c.set(c.get() >> 1);
                    } else {
                        // even numbered tables decrease by one
                        if (c.get() > 0) {
                            c.set(c.get() - 1);
                        }
                    }
                }
            }
        }

        /**
         * Get a value indicating whether the specified block is predicted to be dead or not.
         *
         * @param threadId the thread ID
         * @param trace the trace
         * @return a value indicating whether the specified block is predicted to be dead or not
         */
        boolean predict(int threadId, int trace) {
            // start the confidence sum as 0
            int conf = 0;

            // for each table...
            for (int i = 0; i < danNumPredictionTables; i++) {
                // ...get the counter value for that table...
                int val = tables.get(i).get(getTableIndex(threadId, trace, i)).get();

                // and add it to the running total
                conf += val;
            }

            // if the counter is at least the threshold, the block is predicted dead
            return conf >= danThreshold;
        }
    }

    private Cache<Boolean> mirrorCache;

    private DeadBlockPredictionSampler deadBlockPredictionSampler;

    // sampler associativity
    private static int danSamplerAssociativity = 12;

    // number of bits used to index predictor; determines number of
    // entries in prediction tables
    private static int danPredictorIndexBits = 12;

    // number of prediction tables
    private static int danNumPredictionTables = 3;

    // width of prediction saturating counters
    private static int danCounterWidth = 2;

    // predictor must meet this threshold to predict a block is dead
    private static int danThreshold = 8;

    // number of partial tag bits kept per sampler entry
    private static int danSamplerTagBitsPerEntry = 16;

    // number of trace (partial PC) bits kept per sampler entry
    private static int danSamplerTraceBitsPerEntry = 16;

    // number of entries in prediction table; derived from # of index bits
    private int danPredictionTableEntries;

    // maximum value of saturating counter; derived from counter width
    private int danCounterMax;

    /**
     * Create a dead block prediction based least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public DeadBlockPredictionBasedLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new BasicCache<>(
                cache,
                getCache().getName() + ".rereferenceIntervalPredictionPolicy.mirrorCache",
                cache.getGeometry(),
                args -> new BooleanValueProvider()
        );

        this.deadBlockPredictionSampler = new DeadBlockPredictionSampler(cache.getNumSets());
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        // select a victim using default LRU policy
        int victimWay = super.handleReplacement(access, set, tag).getWay();

        // look for a predicted dead block
        for (int i = 0; i < this.getCache().getAssociativity(); i++) {
            CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, i);
            BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

            if (stateProvider.dead) {
                // found a predicted dead block; this is our new victim
                victimWay = i;
                break;
            }
        }

        // return the selected victim
        return new CacheAccess<>(this.getCache(), access, set, victimWay, tag);
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        this.updateSampler(set, way, access.getThread().getId(), access.getVirtualPc(), access.getPhysicalTag());
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        this.updateSampler(set, way, access.getThread().getId(), access.getVirtualPc(), access.getPhysicalTag());
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }

    /**
     * Update the sampler when there is an access to an LLC cache block.
     *
     * @param set the set index
     * @param way the way
     * @param threadId the thread ID
     * @param pc the virtual PC address
     * @param tag the tag
     */
    private void updateSampler(int set, int way, int threadId, int pc, int tag) {
        // determine if this is a sampler set
        if (set % deadBlockPredictionSampler.samplerModulus == 0) {
            // this is a sampler set.  access the sampler.
            int samplerSet = set / deadBlockPredictionSampler.samplerModulus;
            deadBlockPredictionSampler.access(samplerSet, threadId, pc, tag);
        }

        // make the trace
        int trace = makeTrace(pc);

        // predict whether this block is "dead on arrival"
        boolean dead = deadBlockPredictionSampler.deadBlockPredictor.predict(threadId, trace);

        if (dead) {
            // if block is predicted dead, then the block should be put in the LRU position
            this.setLRU(set, way);
        }
        else {
            // use default LRU replacement policy
            this.setMRU(set, way);
        }

        // get the next prediction for this block using that trace
        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        stateProvider.dead = dead;
    }

    /***
     * make a trace from the specified PC address (just extract some bits).
     *
     * @param pc the virtual PC address
     * @return a trace made from the specified PC address
     */
    private static int makeTrace(int pc) {
        return pc & ((1 << danSamplerTraceBitsPerEntry) - 1);
    }

    /***
     * hash three numbers into one.
     *
     * @param a the a
     * @param b the b
     * @param c the c
     * @return the hash of the specified three numbers
     */
    private static int mix(int a, int b, int c) {
        a = a - b;
        a = a - c;
        a = a ^ (c >> 13);
        b = b - c;
        b = b - a;
        b = b ^ (a << 8);
        c = c - a;
        c = c - b;
        c = c ^ (b >> 13);
        return c;
    }

    /**
     * The first hash function.
     *
     * @param x the x
     * @return the hash of the x
     */
    private static int f1(int x) {
        return mix(0xfeedface, 0xdeadb10c, x);
    }

    /**
     * The second hash function.
     *
     * @param x the x
     * @return the hash of the x
     */
    private static int f2(int x) {
        return mix(0xc001d00d, 0xfade2b1c, x);
    }

    /**
     * The generalized hash function.
     * @param x the x
     * @param i the i
     * @return the generalized hash of x with i
     */
    private static int fi(int x, int i) {
        return f1(x) + (f2(x) >> i);
    }
}
