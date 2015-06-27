/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache.replacement.deadBlockPrediction;

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
        private int partialTag;
        private int trace;
        private boolean dead;
        private boolean valid;

        /**
         * Create a dead block prediction sampler entry.
         */
        public DeadBlockPredictionSamplerEntry(int lruStackPosition) {
            this.lruStackPosition = lruStackPosition;
            this.partialTag = 0;
            this.trace = 0;
            this.dead = false;
            this.valid = false;
        }
    }

    private static final int associativity = 12;

    private static final int modulus = 8;

    private static final int numTagBitsPerEntry = 16;

    private static final int counterWidth = 2;

    private static final int numPredictorIndexBits = 12;

    private int numSets;

    private List<List<DeadBlockPredictionSamplerEntry>> entries;

    private DeadBlockPredictor deadBlockPredictor;

    /**
     * Create a dead block prediction sampler.
     *
     * @param numSetsParentCache the number of sets in the parent cache
     */
    public DeadBlockPredictionSampler(int numSetsParentCache) {
        this.deadBlockPredictor = new DeadBlockPredictor(
                (1 << counterWidth) - 1, numPredictorIndexBits
        );

        this.numSets = numSetsParentCache / modulus;

        this.entries = new ArrayList<>();

        for (int i = 0; i < this.numSets; i++) {
            List<DeadBlockPredictionSamplerEntry> entriesPerSet = new ArrayList<>();
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
        int partialTag = tag & (1 << numTagBitsPerEntry) - 1;

        // this will be the way of the sampler entry we end up hitting or replacing
        int i;

        // search for a matching tag
        for (i = 0; i < associativity; i++)
            if (entriesPerSet.get(i).valid && entriesPerSet.get(i).partialTag == partialTag) {
                // we know this block is not dead; inform the predictor
                this.deadBlockPredictor.update(threadId, entriesPerSet.get(i).trace, false);
                break;
            }

        // did we find a match?
        if (i == associativity) {
            // look for an invalid block to replace
            for (i = 0; i < associativity; i++) if (!entriesPerSet.get(i).valid) break;

            // no invalid block?  look for a dead block.
            if (i == associativity) {
                // find the LRU dead block
                for (i = 0; i < associativity; i++) if (entriesPerSet.get(i).dead) break;
            }

            // no invalid or dead block?  use the LRU block
            if (i == associativity) {
                int j;
                for (j = 0; associativity > j; j++)
                    if (entriesPerSet.get(j).lruStackPosition == associativity - 1) break;
                i = j;
            }

            // previous trace leads to block being dead; inform the predictor
            this.deadBlockPredictor.update(threadId, entriesPerSet.get(i).trace, true);

            // fill the victim block
            entriesPerSet.get(i).partialTag = partialTag;
            entriesPerSet.get(i).valid = true;
        }

        // record the trace
        entriesPerSet.get(i).trace = DeadBlockPredictionBasedLRUPolicy.makeTrace(pc);

        // get the next prediction for this entry
        entriesPerSet.get(i).dead = this.deadBlockPredictor.predict(threadId, entriesPerSet.get(i).trace);

        // now the replaced entry should be moved to the MRU position
        int position = entriesPerSet.get(i).lruStackPosition;
        for (int way = 0; way < associativity; way++)
            if (entriesPerSet.get(way).lruStackPosition < position)
                entriesPerSet.get(way).lruStackPosition++;
        entriesPerSet.get(i).lruStackPosition = 0;
    }

    /**
     * Get the associativity.
     *
     * @return the associativity
     */
    public static int getAssociativity() {
        return associativity;
    }

    /**
     * Get the modulus value that determines which LLC sets are sampler sets.
     *
     * @return the modulus value that determines which LLC sets are sampler sets
     */
    public static int getModulus() {
        return modulus;
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
     * Get the dead block predictor.
     *
     * @return the dead block predictor
     */
    public DeadBlockPredictor getDeadBlockPredictor() {
        return deadBlockPredictor;
    }
}
