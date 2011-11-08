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
package archimulator.mem.dram;

public class BasicMainMemoryConfig extends MainMemoryConfig {
    private int toDramLatency;
    private int fromDramLatency;
    private int prechargeLatency;
    private int closedLatency;
    private int conflictLatency;
    private int busWidth;
    private int numBanks;
    private int rowSize;

    public BasicMainMemoryConfig() {
        this(6, 12, 90, 90, 90, 4, 8, 2048);
    }

    public BasicMainMemoryConfig(int toDramLatency, int fromDramLatency, int prechargeLatency, int closedLatency, int conflictLatency, int busWidth, int numBanks, int rowSize) {
        super(MainMemoryType.BASIC, 64);

        this.toDramLatency = toDramLatency;
        this.fromDramLatency = fromDramLatency;
        this.prechargeLatency = prechargeLatency;
        this.closedLatency = closedLatency;
        this.conflictLatency = conflictLatency;
        this.busWidth = busWidth;
        this.numBanks = numBanks;
        this.rowSize = rowSize;
    }

    public int getToDramLatency() {
        return toDramLatency;
    }

    public int getFromDramLatency() {
        return fromDramLatency;
    }

    public int getPrechargeLatency() {
        return prechargeLatency;
    }

    public int getClosedLatency() {
        return closedLatency;
    }

    public int getConflictLatency() {
        return conflictLatency;
    }

    public int getBusWidth() {
        return busWidth;
    }

    public int getNumBanks() {
        return numBanks;
    }

    public int getRowSize() {
        return rowSize;
    }
}
