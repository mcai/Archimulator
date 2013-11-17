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

import archimulator.util.NoThresholdSaturatingCounter;

import java.util.ArrayList;
import java.util.List;

/**
 * Dead block predictor.
 *
 * @author Min Cai
 */
public class DeadBlockPredictor {
    // number of prediction tables
    private static int numPredictionTables = 3;

    // predictor must meet this threshold to predict a block is dead
    private static int threshold = 8;

    // number of bits used to index predictor; determines number of
    // entries in prediction tables
    private int numPredictorIndexBits;

    private List<List<NoThresholdSaturatingCounter>> tables;

    /**
     * Create a dead block predictor.
     */
    public DeadBlockPredictor(int numEntriesPerPredictionTable, int counterMax, int numPredictorIndexBits) {
        this.numPredictorIndexBits = numPredictorIndexBits;

        this.tables = new ArrayList<>();

        for (int i = 0; i < numPredictionTables; i++) {
            List<NoThresholdSaturatingCounter> table = new ArrayList<>();

            for (int j = 0; j < numEntriesPerPredictionTable; j++) {
                table.add(new NoThresholdSaturatingCounter(0, counterMax, 0));
            }

            this.tables.add(table);
        }
    }

    /**
     * Hash a trace, thread ID, and prediction table number into a predictor table index.
     *
     * @param threadId the thread ID
     * @param trace    the trace
     * @param table    the prediction table number
     * @return the corresponding table index for the specified thread ID, trace and prediction table number
     */
    private int getTableIndex(int threadId, int trace, int table) {
        int x = fi(trace ^ (threadId << 2), table);
        return x & ((1 << this.numPredictorIndexBits) - 1);
    }

    /**
     * Update the predictor when a block, either dead or not, is encountered.
     *
     * @param threadId the thread ID
     * @param trace    the trace
     * @param dead     a value indicating whether the block is dead or not
     */
    public void update(int threadId, int trace, boolean dead) {
        // for each predictor table...
        for (int i = 0; i < numPredictionTables; i++) {
            // ...get a pointer to the corresponding entry in that table
            NoThresholdSaturatingCounter c = tables.get(i).get(getTableIndex(threadId, trace, i));

            if (dead) {
                c.increment();
            } else {
                if (i % 2 == 1) {
                    c.decrementExponentially();
                } else {
                    c.decrement();
                }
            }
        }
    }

    /**
     * Get a value indicating whether the specified block is predicted to be dead or not.
     *
     * @param threadId the thread ID
     * @param trace    the trace
     * @return a value indicating whether the specified block is predicted to be dead or not
     */
    public boolean predict(int threadId, int trace) {
        return this.tables.stream().mapToInt(
                table -> table.get(getTableIndex(threadId, trace, this.tables.indexOf(table))).get()
        ).sum() >= threshold;
    }

    /**
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
     *
     * @param x the x
     * @param i the i
     * @return the generalized hash of x with i
     */
    private static int fi(int x, int i) {
        return f1(x) + (f2(x) >> i);
    }
}
