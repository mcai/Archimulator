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
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
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
        boolean prediction;

        /**
         * Create a boolean value provider.
         */
        public BooleanValueProvider() {
            this.state = true;
            this.prediction = false;
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
        private boolean prediction;
        private boolean valid;

        /**
         * Create a dead block prediction sampler entry.
         */
        private DeadBlockPredictionSamplerEntry() {
            lruStackPosition = 0;
            valid = false;
            tag = 0;
            trace = 0;
            prediction = false;
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
         * @param associativity the associativity of the parent cache
         */
        private DeadBlockPredictionSampler(int numSets, int associativity) {
            // four-core version gets slightly different parameters
            if (numSets == 4096) {
                danSamplerAssociativity = 13;
                danPredictorIndexBits = 14;
            }

            // here, we figure out the total number of bits used by the various
            // structures etc.  along the way we will figure out how many
            // sampler sets we have room for

            // figure out number of entries in each table
            danPredictionTableEntries = 1 << danPredictorIndexBits;

            // compute the total number of bits used by the replacement policy

            // total number of bits available for the contest
            int numBitsTotal = (numSets * associativity * 8 + 1024);

            // the real LRU policy consumes log(associativity) bits per block
            int numBitsLRU = associativity * numSets * (int) (Math.log(associativity) / Math.log(2));

            // the dead block predictor consumes (counter width) * (number of tables)
            // * (entries per table) bits
            int numBitsPredictor =
                    danCounterWidth * danNumPredictionTables * danPredictionTableEntries;

            // one prediction bit per cache block.
            int numBitsCache = 1 * numSets * associativity;

            // some extra bits we account for to be safe; figure we need about 85 bits
            // for the various run-time constants and variables the CRC guys might want
            // to charge us for.  in reality we leave a bigger surplus than this so we
            // should be safe.
            int numBitsExtra = 85;

            // number of bits left over for the sampler sets
            int numBitsLeftOver =
                    numBitsTotal - (numBitsPredictor + numBitsCache + numBitsLRU + numBitsExtra);

            // number of bits in one sampler set: associativity of sampler * bits per sampler block entry
            int numBitsPerSet =
                    danSamplerAssociativity
                            // tag bits, valid bit, prediction bit, trace bits, lru stack position bits
                            * (danSamplerTagBitsPerEntry + 1 + 1 + 4 + danSamplerTraceBitsPerEntry);

            // maximum number of sampler of sets we can afford with the space left over
            samplerNumSets = numBitsLeftOver / numBitsPerSet;

            // compute the maximum saturating counter value; predictor constructor
            // needs this so we do it here
            danCounterMax = (1 << danCounterWidth) - 1;

            // make a predictor
            deadBlockPredictor = new DeadBlockPredictor();

            // we should have at least one sampler set
            assert (samplerNumSets >= 0);

            // make the sampler sets
            this.sets = new ArrayList<>();
            for (int i = 0; i < samplerNumSets; i++) {
                this.sets.add(new DeadBlockPredictionSamplerSet());
            }

            // figure out what should divide evenly into a set index to be
            // considered a sampler set
            samplerModulus = numSets / samplerNumSets;

            // compute total number of bits used; we can print this out to validate
            // the computation in the paper
            totalBitsUsed = (numBitsTotal - numBitsLeftOver) + (numBitsPerSet * samplerNumSets);
        }

        /**
         * Access the sampler with an LLC tag.
         *
         * @param set the set index
         * @param threadId the thread ID
         * @param pc the virtual PC address
         * @param tag the tag
         */
        void access(int set, int threadId, int pc, int tag) {
            // get a pointer to this set's sampler entries
            List<DeadBlockPredictionSamplerEntry> blocks = sets.get(set).blocks;

            // get a partial tag to search for
            int partialTag = tag & ((1 << danSamplerTagBitsPerEntry) - 1);

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
                    for (i = 0; i < danSamplerAssociativity; i++) if (blocks.get(i).prediction) break;
                }

                // no invalid or dead block?  use the LRU block
                if (i == danSamplerAssociativity) {
                    int j;
                    for (j = 0; j < danSamplerAssociativity; j++)
                        if (blocks.get(j).lruStackPosition == danSamplerAssociativity - 1) break;
                    assert (j < danSamplerAssociativity);
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
            blocks.get(i).prediction = deadBlockPredictor.predict(threadId, blocks.get(i).trace);

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

    // sampler associativity (changed for 4MB cache)
    private int danSamplerAssociativity = 12;

    // number of bits used to index predictor; determines number of
    // entries in prediction tables (changed for 4MB cache)
    private int danPredictorIndexBits = 12;

    // number of prediction tables
    private int danNumPredictionTables = 3;

    // width of prediction saturating counters
    private int danCounterWidth = 2;

    // predictor must meet this threshold to predict a block is dead
    private int danThreshold = 8;

    // number of partial tag bits kept per sampler entry
    private int danSamplerTagBitsPerEntry = 16;

    // number of trace (partial PC) bits kept per sampler entry
    private int danSamplerTraceBitsPerEntry = 16;

    // number of entries in prediction table; derived from # of index bits
    private int danPredictionTableEntries;

    // maximum value of saturating counter; derived from counter width
    private int danCounterMax;

    // total number of bits used by all structures; computed in sampler::sampler
    private int totalBitsUsed;

    /**
     * Create a dead block prediction based least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public DeadBlockPredictionBasedLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new Cache<>(
                cache,
                getCache().getName() + ".rereferenceIntervalPredictionPolicy.mirrorCache",
                cache.getGeometry(),
                args -> new BooleanValueProvider()
        );

        this.deadBlockPredictionSampler = new DeadBlockPredictionSampler(cache.getNumSets(), cache.getAssociativity());
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        // select a victim using default LRU policy
        int victimWay = super.handleReplacement(access, set, tag).getWay();

        // look for a predicted dead block
        for (int i = 0; i < this.getCache().getAssociativity(); i++) {
            CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, i);
            BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

            if (stateProvider.prediction) {
                // found a predicted dead block; this is our new victim
                victimWay = i;
                break;
            }
        }

        // predict whether this block is "dead on arrival"
        int trace = makeTrace(access.getVirtualPc());
        boolean prediction = deadBlockPredictionSampler.deadBlockPredictor.predict(access.getThread().getId(), trace);

        // if block is predicted dead, then it should put the block in the LRU position
        if (prediction) victimWay = getLRU(set);

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
            int set1 = set / deadBlockPredictionSampler.samplerModulus;
            if (set1 >= 0 && set1 < deadBlockPredictionSampler.samplerNumSets)
                deadBlockPredictionSampler.access(set1, threadId, pc, tag);
        }

        // update default replacement policy
        setMRU(set, way);

        // make the trace
        int trace = makeTrace(pc);

        // get the next prediction for this block using that trace
        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        stateProvider.prediction = deadBlockPredictionSampler.deadBlockPredictor.predict(threadId, trace);
    }

    /***
     * make a trace from the specified PC address (just extract some bits).
     *
     * @param pc the virtual PC address
     * @return a trace made from the specified PC address
     */
    private int makeTrace(int pc) {
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
    private int mix(int a, int b, int c) {
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
    private int f1(int x) {
        return mix(0xfeedface, 0xdeadb10c, x);
    }

    /**
     * The second hash function.
     *
     * @param x the x
     * @return the hash of the x
     */
    private int f2(int x) {
        return mix(0xc001d00d, 0xfade2b1c, x);
    }

    /**
     * The generalized hash function.
     * @param x the x
     * @param i the i
     * @return the generalized hash of x with i
     */
    private int fi(int x, int i) {
        return f1(x) + (f2(x) >> i);
    }
}
