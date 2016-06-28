/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.cache;

import archimulator.util.StorageUnit;

/**
 * Cache geometry.
 *
 * @author Min Cai
 */
public class CacheGeometry {
    private int size;
    private int associativity;
    private int lineSize;

    private int lineSizeInLog2;
    private int numSets;
    private int numSetsInLog2;
    private int numLines;

    /**
     * Create a cache geometry.
     *
     * @param size          the size.
     * @param associativity the associativity
     * @param lineSize      the line size
     */
    public CacheGeometry(int size, int associativity, int lineSize) {
        this.size = size;
        this.associativity = associativity;
        this.lineSize = lineSize;
        this.lineSizeInLog2 = (int) (Math.log(this.lineSize) / Math.log(2));
        this.numSets = this.size / this.associativity / this.lineSize;
        this.numSetsInLog2 = (int) (Math.log(this.numSets) / Math.log(2));
        this.numLines = this.size / this.lineSize;
    }

    /**
     * Get the size.
     *
     * @return the size
     */
    public int getSize() {
        return size;
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
     * Get the line size.
     *
     * @return the line size
     */
    public int getLineSize() {
        return lineSize;
    }

    /**
     * Get the line size in Log2.
     *
     * @return the line size in Log2
     */
    public int getLineSizeInLog2() {
        return lineSizeInLog2;
    }

    /**
     * Get the number of sets.
     *
     * @return the number of sets
     */
    public int getNumSets() {
        return numSets;
    }

    /**
     * Get the number of sets in Log2.
     *
     * @return the number of sets in Log2.
     */
    public int getNumSetsInLog2() {
        return numSetsInLog2;
    }

    /**
     * Get the number of lines.
     *
     * @return the number of lines
     */
    public int getNumLines() {
        return numLines;
    }

    @Override
    public String toString() {
        return String.format("CacheGeometry{size=%s, associativity=%d, lineSize=%d, lineSizeInLog2=%d, numSets=%d, numSetsInLog2=%d, numLines=%d}", StorageUnit.toString(size), associativity, lineSize, lineSizeInLog2, numSets, numSetsInLog2, numLines);
    }

    /**
     * Get the displacement for the specified address and geometry.
     *
     * @param address       the address
     * @param cacheGeometry the geometry
     * @return the displacement for the specified address and geometry
     */
    public static int getDisplacement(int address, CacheGeometry cacheGeometry) {
        return address & (cacheGeometry.getLineSize() - 1);
    }

    /**
     * Get the tag for the specified address and geometry.
     *
     * @param address       the address
     * @param cacheGeometry the geometry
     * @return the tag for the specified address and geometry
     */
    public static int getTag(int address, CacheGeometry cacheGeometry) {
        return address & ~(cacheGeometry.getLineSize() - 1);
    }

    /**
     * Get the set index for the specified address and geometry.
     *
     * @param address       the address
     * @param cacheGeometry the geometry
     * @return the set index for the specified address and geometry
     */
    public static int getSet(int address, CacheGeometry cacheGeometry) {
        return getLineId(address, cacheGeometry) % cacheGeometry.getNumSets();
    }

    /**
     * Get the line ID for the specified address and geometry.
     *
     * @param address       the address
     * @param cacheGeometry the geometry
     * @return the line ID for the specified address and geometry
     */
    public static int getLineId(int address, CacheGeometry cacheGeometry) {
        return (address >> cacheGeometry.getLineSizeInLog2());
    }

    /**
     * Get a value indicating whether the specified address is aligned or not based on the specified geometry.
     *
     * @param address       the address
     * @param cacheGeometry the geometry
     * @return a value indicating whether the specified address is aligned or not based on the specified geometry
     */
    public static boolean isAligned(int address, CacheGeometry cacheGeometry) {
        return getDisplacement(address, cacheGeometry) == 0;
    }
}
