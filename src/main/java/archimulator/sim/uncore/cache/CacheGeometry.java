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
package archimulator.sim.uncore.cache;

public class CacheGeometry {
    private int size;
    private int associativity;
    private int lineSize;

    private int lineSizeInLog2;
    private int numSets;
    private int numSetsInLog2;
    private int numLines;

    public CacheGeometry(int size, int associativity, int lineSize) {
        this.size = size;
        this.associativity = associativity;
        this.lineSize = lineSize;
        this.lineSizeInLog2 = (int) (Math.log(this.lineSize) / Math.log(2));
        this.numSets = this.size / this.associativity / this.lineSize;
        this.numSetsInLog2 = (int) (Math.log(this.numSets) / Math.log(2));
        this.numLines = this.size / this.lineSize;
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
        return numLines;
    }

    public static int getDisplacement(int address, CacheGeometry cacheGeometry) {
        return address & (cacheGeometry.getLineSize() - 1);
    }

    public static int getTag(int address, CacheGeometry cacheGeometry) {
        return address & ~(cacheGeometry.getLineSize() - 1);
    }

    public static int getSet(int address, CacheGeometry cacheGeometry) {
        return getLineId(address, cacheGeometry) % cacheGeometry.getNumSets();
    }

    public static int getLineId(int address, CacheGeometry cacheGeometry) {
        return (address >> cacheGeometry.getLineSizeInLog2());
    }

    public static boolean isAligned(int address, CacheGeometry cacheGeometry) {
        return getDisplacement(address, cacheGeometry) == 0;
    }
}
