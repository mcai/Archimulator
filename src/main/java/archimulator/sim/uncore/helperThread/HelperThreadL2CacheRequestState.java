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
package archimulator.sim.uncore.helperThread;

/**
 * Helper thread L2 cache request state.
 *
 * @author Min Cai
 */
public class HelperThreadL2CacheRequestState {
    private int inFlightThreadId;
    private int threadId;
    private int pc;
    private HelperThreadL2CacheRequestQuality quality;
    private boolean hitToTransientTag;

    /**
     * Create a helper thread L2 cache request state.
     */
    public HelperThreadL2CacheRequestState() {
        this.inFlightThreadId = -1;
        this.threadId = -1;
        this.pc = -1;
        this.quality = HelperThreadL2CacheRequestQuality.INVALID;
    }

    /**
     * Get the in-flight thread ID.
     *
     * @return the in-flight thread ID
     */
    public int getInFlightThreadId() {
        return inFlightThreadId;
    }

    /**
     * Set the in-flight thread ID.
     *
     * @param inFlightThreadId the in-flight thread ID
     */
    public void setInFlightThreadId(int inFlightThreadId) {
        this.inFlightThreadId = inFlightThreadId;
    }

    /**
     * Get the thread ID.
     *
     * @return the thread ID
     */
    public int getThreadId() {
        return threadId;
    }

    /**
     * Set the thread ID.
     *
     * @param threadId the thread ID
     */
    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    /**
     * Get the virtual address of the program counter (PC).
     *
     * @return the virtual address of the program counter (PC)
     */
    public int getPc() {
        return pc;
    }

    /**
     * Set the virtual address of the program counter (PC).
     *
     * @param pc the virtual address of the program counter (PC)
     */
    public void setPc(int pc) {
        this.pc = pc;
    }

    /**
     * Set the quality of the helper thread L2 cache request.
     *
     * @param quality the quality of the helper thread L2 cache request
     */
    public void setQuality(HelperThreadL2CacheRequestQuality quality) {
        if (this.quality != HelperThreadL2CacheRequestQuality.INVALID && quality != HelperThreadL2CacheRequestQuality.INVALID && !this.quality.isModifiable()) {
            throw new IllegalArgumentException();
        }

        this.quality = quality;
    }

    /**
     * Get the quality of the helper thread L2 cache request.
     *
     * @return the quality of the helper thread L2 cache request
     */
    public HelperThreadL2CacheRequestQuality getQuality() {
        return quality;
    }

    /**
     * Set a value indicating whether there is a hit to the transient tag.
     *
     * @param hitToTransientTag a value indicating whether there is a hit to the transient tag
     */
    public void setHitToTransientTag(boolean hitToTransientTag) {
        this.hitToTransientTag = hitToTransientTag;
    }

    /**
     * Get a value indicating whether there is a hit to the transient tag.
     *
     * @return a value indicating whether there is a hit to the transient tag
     */
    public boolean isHitToTransientTag() {
        return hitToTransientTag;
    }

    @Override
    public String toString() {
        return String.format("HelperThreadL2CacheRequestState{inFlightThreadId=%d, threadId=%d, pc=0x%08x, quality=%s, hitToTransientTag=%s}", inFlightThreadId, threadId, pc, quality, hitToTransientTag);
    }
}
