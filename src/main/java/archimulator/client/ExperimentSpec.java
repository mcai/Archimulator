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
    private int htLookahead;
    private int htStride;
    private int numCores;
    private int numThreadsPerCore;
    private String l1ISize;
    private int l1IAssoc;
    private String l1DSize;
    private int l1DAssoc;
    private String l2Size;
    private int l2Assoc;
    private CacheReplacementPolicyType l2ReplacementPolicyType;

    public ExperimentSpec(String programTitle, int htLookahead, int htStride, int numCores, int numThreadsPerCore, String l1ISize, int l1IAssoc, String l1DSize, int l1DAssoc, String l2Size, int l2Assoc, CacheReplacementPolicyType l2ReplacementPolicyType) {
        this.programTitle = programTitle;
        this.htLookahead = htLookahead;
        this.htStride = htStride;
        this.numCores = numCores;
        this.numThreadsPerCore = numThreadsPerCore;
        this.l1ISize = l1ISize;
        this.l1IAssoc = l1IAssoc;
        this.l1DSize = l1DSize;
        this.l1DAssoc = l1DAssoc;
        this.l2Size = l2Size;
        this.l2Assoc = l2Assoc;
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;
    }

    public String getProgramTitle() {
        return programTitle;
    }

    public int getHtLookahead() {
        return htLookahead;
    }

    public int getHtStride() {
        return htStride;
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

    public int getL1IAssoc() {
        return l1IAssoc;
    }

    public int getL1DSize() {
        return (int) StorageUnitHelper.displaySizeToByteCount(l1DSize);
    }

    public int getL1DAssoc() {
        return l1DAssoc;
    }

    public int getL2Size() {
        return (int) StorageUnitHelper.displaySizeToByteCount(l2Size);
    }

    public int getL2Assoc() {
        return l2Assoc;
    }

    public CacheReplacementPolicyType getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }
}
