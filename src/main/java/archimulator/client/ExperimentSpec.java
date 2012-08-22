/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.client;

import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.util.StorageUnitHelper;

import java.io.Serializable;

public class ExperimentSpec implements Serializable {
    private String programTitle;

    private int helperThreadLookahead;
    private int helperThreadStride;

    private int numCores;
    private int numThreadsPerCore;

    private String l1ISize;
    private int l1IAssociativity;

    private String l1DSize;
    private int l1DAssociativity;

    private String l2Size;
    private int l2Associativity;
    private CacheReplacementPolicyType l2ReplacementPolicyType;

    public ExperimentSpec(String programTitle, int helperThreadLookahead, int helperThreadStride, int numCores, int numThreadsPerCore, String l1ISize, int l1IAssociativity, String l1DSize, int l1DAssociativity, String l2Size, int l2Associativity, CacheReplacementPolicyType l2ReplacementPolicyType) {
        this.programTitle = programTitle;

        this.helperThreadLookahead = helperThreadLookahead;
        this.helperThreadStride = helperThreadStride;

        this.numCores = numCores;
        this.numThreadsPerCore = numThreadsPerCore;

        this.l1ISize = l1ISize;
        this.l1IAssociativity = l1IAssociativity;

        this.l1DSize = l1DSize;
        this.l1DAssociativity = l1DAssociativity;

        this.l2Size = l2Size;
        this.l2Associativity = l2Associativity;
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;
    }

    public String getProgramTitle() {
        return programTitle;
    }

    public int getHelperThreadLookahead() {
        return helperThreadLookahead;
    }

    public int getHelperThreadStride() {
        return helperThreadStride;
    }

    public int getNumCores() {
        return numCores;
    }

    public int getNumThreadsPerCore() {
        return numThreadsPerCore;
    }

    public int getL1ISize() {
        return (int) StorageUnitHelper.displaySizeToByteCount(l1ISize);
    }

    public int getL1IAssociativity() {
        return l1IAssociativity;
    }

    public int getL1DSize() {
        return (int) StorageUnitHelper.displaySizeToByteCount(l1DSize);
    }

    public int getL1DAssociativity() {
        return l1DAssociativity;
    }

    public int getL2Size() {
        return (int) StorageUnitHelper.displaySizeToByteCount(l2Size);
    }

    public int getL2Associativity() {
        return l2Associativity;
    }

    public CacheReplacementPolicyType getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }
}
