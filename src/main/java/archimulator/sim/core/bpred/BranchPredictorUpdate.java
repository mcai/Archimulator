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
package archimulator.sim.core.bpred;

import archimulator.util.math.SaturatingCounter;

public class BranchPredictorUpdate {
    private SaturatingCounter counterDir1;
    private SaturatingCounter counterDir2;
    private SaturatingCounter counterMeta;

    private boolean ras;
    private boolean bimod;
    private boolean twoLevel;
    private boolean meta;

    public BranchPredictorUpdate() {
    }

    public SaturatingCounter getCounterDir1() {
        return counterDir1;
    }

    public void setCounterDir1(SaturatingCounter counterDir1) {
        this.counterDir1 = counterDir1;
    }

    public SaturatingCounter getCounterDir2() {
        return counterDir2;
    }

    public void setCounterDir2(SaturatingCounter counterDir2) {
        this.counterDir2 = counterDir2;
    }

    public SaturatingCounter getCounterMeta() {
        return counterMeta;
    }

    public void setCounterMeta(SaturatingCounter counterMeta) {
        this.counterMeta = counterMeta;
    }

    public boolean isRas() {
        return ras;
    }

    public void setRas(boolean ras) {
        this.ras = ras;
    }

    public boolean isBimod() {
        return bimod;
    }

    public void setBimod(boolean bimod) {
        this.bimod = bimod;
    }

    public boolean isTwoLevel() {
        return twoLevel;
    }

    public void setTwoLevel(boolean twoLevel) {
        this.twoLevel = twoLevel;
    }

    public boolean isMeta() {
        return meta;
    }

    public void setMeta(boolean meta) {
        this.meta = meta;
    }
}
