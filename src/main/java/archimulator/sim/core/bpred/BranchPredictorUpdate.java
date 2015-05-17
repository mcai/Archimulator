/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.core.bpred;

import net.pickapack.math.SaturatingCounter;

/**
 * Branch predictor update.
 *
 * @author Min Cai
 */
public class BranchPredictorUpdate {
    private SaturatingCounter counterDir1;
    private SaturatingCounter counterDir2;
    private SaturatingCounter counterMeta;

    private boolean ras;
    private boolean bimod;
    private boolean twoLevel;
    private boolean meta;

    /**
     * Create a branch predictor update.
     */
    public BranchPredictorUpdate() {
    }

    /**
     * Get the counter dir 1.
     *
     * @return the counter dir 1
     */
    public SaturatingCounter getCounterDir1() {
        return counterDir1;
    }

    /**
     * Set the counter dir 1.
     *
     * @param counterDir1 the counter dir 1
     */
    public void setCounterDir1(SaturatingCounter counterDir1) {
        this.counterDir1 = counterDir1;
    }

    /**
     * Get the counter dir 2.
     *
     * @return the counter dir 2
     */
    public SaturatingCounter getCounterDir2() {
        return counterDir2;
    }

    /**
     * Set the counter dir 2.
     *
     * @param counterDir2 the counter dir 2
     */
    public void setCounterDir2(SaturatingCounter counterDir2) {
        this.counterDir2 = counterDir2;
    }

    /**
     * Get the counter meta.
     *
     * @return the counter meta
     */
    public SaturatingCounter getCounterMeta() {
        return counterMeta;
    }

    /**
     * Set the counter meta.
     *
     * @param counterMeta the counter meta
     */
    public void setCounterMeta(SaturatingCounter counterMeta) {
        this.counterMeta = counterMeta;
    }

    /**
     * Get a value indicating whether it is RAS or not.
     *
     * @return a value indicating whether it is RAS or not
     */
    public boolean isRas() {
        return ras;
    }

    /**
     * Set a value indicating whether it is RAS or not.
     *
     * @param ras a value indicating whether it is RAS or not
     */
    public void setRas(boolean ras) {
        this.ras = ras;
    }

    /**
     * Get a value indicating whether it is bimod or not.
     *
     * @return a value indicating whether it is bimod or not
     */
    public boolean isBimod() {
        return bimod;
    }

    /**
     * Set a value indicating whether it is bimod or not.
     *
     * @param bimod a value indicating whether it is bimod or not
     */
    public void setBimod(boolean bimod) {
        this.bimod = bimod;
    }

    /**
     * Get a value indicating whether it is two level or not.
     *
     * @return a value indicating whether it is two level or not
     */
    public boolean isTwoLevel() {
        return twoLevel;
    }

    /**
     * Set a value indicating whether it is two level or not.
     *
     * @param twoLevel a value indicating whether it is two level or not
     */
    public void setTwoLevel(boolean twoLevel) {
        this.twoLevel = twoLevel;
    }

    /**
     * Get a value indicating whether it is meta or not.
     *
     * @return a value indicating whether it is meta or not
     */
    public boolean isMeta() {
        return meta;
    }

    /**
     * Set a value indicating whether it is meta or not.
     *
     * @param meta a value indicating whether it is meta or not
     */
    public void setMeta(boolean meta) {
        this.meta = meta;
    }
}
