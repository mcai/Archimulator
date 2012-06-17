package archimulator.sim.uncore.cache;

/**
 * ****************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 * <p/>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p/>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */

public final class CacheGeometry {
    private int size;
    private int associativity;
    private int lineSize;

    private int lineSizeInLog2;
    private int numSets;
    private int numSetsInLog2;

    public CacheGeometry(int size, int associativity, int lineSize) {
        this.size = size;
        this.associativity = associativity;
        this.lineSize = lineSize;
        this.lineSizeInLog2 = (int) (Math.log(this.lineSize) / Math.log(2));
        this.numSets = this.size / this.associativity / this.lineSize;
        this.numSetsInLog2 = (int) (Math.log(this.numSets) / Math.log(2));
    }

    public int getSize() {
        return size;
    }

    public int getAssociativity() {
        return associativity;
    }

    public int getLineSize() {
        return lineSize;
    }

    public int getLineSizeInLog2() {
        return lineSizeInLog2;
    }

    public int getNumSets() {
        return numSets;
    }

    public int getNumSetsInLog2() {
        return numSetsInLog2;
    }

    public int getNumLines() {
        return this.size / this.lineSize;
    }

    public static int getDisplacement(int addr, CacheGeometry cacheGeometry) {
        return addr & (cacheGeometry.getLineSize() - 1);
    }

    public static int getTag(int addr, CacheGeometry cacheGeometry) {
        return addr & ~(cacheGeometry.getLineSize() - 1);
    }

    public static int getSet(int addr, CacheGeometry cacheGeometry) {
        return getLineId(addr, cacheGeometry) % cacheGeometry.getNumSets();
    }

    public static int getLineId(int addr, CacheGeometry cacheGeometry) {
        return (addr >> cacheGeometry.getLineSizeInLog2());
    }

    public static boolean isAligned(int addr, CacheGeometry cacheGeometry) {
        return getDisplacement(addr, cacheGeometry) == 0;
    }
}
