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
package archimulator.sim.uncore.cache.replacement.deadBlockPrediction;

import java.util.ArrayList;
import java.util.List;

/**
 * Dead block prediction sampler.
 *
 * @author Min Cai
 */
public class DeadBlockPredictionSampler {
    /**
     * Dead block prediction sampler entry.
     */
    public class DeadBlockPredictionSamplerEntry {
        private int lruStackPosition;
        private int tag;
        private int trace;
        private boolean dead;
        private boolean valid;

        /**
         * Create a dead block prediction sampler entry.
         */
        public DeadBlockPredictionSamplerEntry(int lruStackPosition) {
            this.lruStackPosition = lruStackPosition;
            valid = false;
            tag = 0;
            trace = 0;
            dead = false;
        }
    }

    private int associativity;

    private int numTagBitsPerEntry;

    private int numTraceBitsPerEntry;

    private int counterWidth;

    private int numPredictorIndexBits;

    private int numSets;

    private int modulus;

    private List<List<DeadBlockPredictionSamplerEntry>> entries;

    private DeadBlockPredictor deadBlockPredictor;

    /**
     * Create a dead block prediction sampler.
     *
     * @param numSets the number of sets in the parent cache
     */
    public DeadBlockPredictionSampler(int numSets) {
        this.associativity = 12;

        this.numTagBitsPerEntry = 16;

        this.numTraceBitsPerEntry = 16;

        this.counterWidth = 2;

        this.numPredictorIndexBits = 12;

        this.deadBlockPredictor = new DeadBlockPredictor(
                1 << this.numPredictorIndexBits, (1 << this.counterWidth) - 1, this.numPredictorIndexBits
        );

        this.modulus = 8;

        this.numSets = numSets / this.modulus;

        this.entries = new ArrayList<>();

        for (int i = 0; i < this.numSets; i++) {
            ArrayList<DeadBlockPredictionSamplerEntry> entriesPerSet = new ArrayList<>();
            this.entries.add(entriesPerSet);

            for (int j = 0; j < associativity; j++) {
                entriesPerSet.add(new DeadBlockPredictionSamplerEntry(j));
            }
        }
    }

    /**
     * Access with an LLC tag.
     *
     * @param set the sampler set index
     * @param threadId the thread ID
     * @param pc the virtual PC address
     * @param tag the tag
     */
    public void access(int set, int threadId, int pc, int tag) {
        List<DeadBlockPredictionSamplerEntry> entriesPerSet = this.entries.get(set);

        // get a partial tag to search for
        int partialTag = tag & (1 << this.numTagBitsPerEntry) - 1;

        // this will be the way of the sampler entry we end up hitting or replacing
        int i;

        // search for a matching tag
        for (i = 0; i < this.associativity; i++)
            if (entriesPerSet.get(i).valid && entriesPerSet.get(i).tag == partialTag) {
                // we know this block is not dead; inform the predictor
                this.deadBlockPredictor.update(threadId, entriesPerSet.get(i).trace, false);
                break;
            }

        // did we find a match?
        if (i == this.associativity) {
            // look for an invalid block to replace
            for (i = 0; i < this.associativity; i++) if (!entriesPerSet.get(i).valid) break;

            // no invalid block?  look for a dead block.
            if (i == this.associativity) {
                // find the LRU dead block
                for (i = 0; i < this.associativity; i++) if (entriesPerSet.get(i).dead) break;
            }

            // no invalid or dead block?  use the LRU block
            if (i == this.associativity) {
                int j;
                for (j = 0; j < this.associativity; j++)
                    if (entriesPerSet.get(j).lruStackPosition == this.associativity - 1) break;
                i = j;
            }

            // previous trace leads to block being dead; inform the predictor
            this.deadBlockPredictor.update(threadId, entriesPerSet.get(i).trace, true);

            // fill the victim block
            entriesPerSet.get(i).tag = partialTag;
            entriesPerSet.get(i).valid = true;
        }

        // record the trace
        entriesPerSet.get(i).trace = DeadBlockPredictionBasedLRUPolicy.makeTrace(pc, this.getNumTraceBitsPerEntry());

        // get the next prediction for this entry
        entriesPerSet.get(i).dead = this.deadBlockPredictor.predict(threadId, entriesPerSet.get(i).trace);

        // now the replaced entry should be moved to the MRU position
        int position = entriesPerSet.get(i).lruStackPosition;
        for (int way = 0; way < this.associativity; way++)
            if (entriesPerSet.get(way).lruStackPosition < position)
                entriesPerSet.get(way).lruStackPosition++;
        entriesPerSet.get(i).lruStackPosition = 0;
    }

    /**
     * Get the associativity.
     *
     * @return the associativity
     */
    public int getAssociativity() {
        return associativity;
    }

    /**
     * Get the number of partial tag bits kept per sampler entry.
     *
     * @return the number of partial tag bits kept per sampler entry
     */
    public int getNumTagBitsPerEntry() {
        return numTagBitsPerEntry;
    }

    /**
     * Get the number of trace (partial PC) bits kept per sampler entry.
     *
     * @return the number of trace (partial PC) bits kept per sampler entry
     */
    public int getNumTraceBitsPerEntry() {
        return numTraceBitsPerEntry;
    }

    /**
     * Get the width of prediction saturating counters.
     *
     * @return the width of prediction saturating counters
     */
    public int getCounterWidth() {
        return counterWidth;
    }

    /**
     * Get the number of bits used to index predictor; determines the number of entries in prediction tables.
     *
     * @return the number of bits used to index predictor; determines the number of entries in prediction tables
     */
    public int getNumPredictorIndexBits() {
        return numPredictorIndexBits;
    }

    /**
     * Get the number of sampler sets.
     *
     * @return the number of sampler sets
     */
    public int getNumSets() {
        return numSets;
    }

    /**
     * Get the modulus value that determines which LLC sets are sampler sets.
     *
     * @return the modulus value that determines which LLC sets are sampler sets
     */
    public int getModulus() {
        return modulus;
    }

    /**
     * Get the dead block predictor.
     *
     * @return the dead block predictor
     */
    public DeadBlockPredictor getDeadBlockPredictor() {
        return deadBlockPredictor;
    }
}
