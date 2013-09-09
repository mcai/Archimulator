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
package archimulator.sim.uncore.helperThread;

import archimulator.sim.uncore.cache.CacheLine;

/**
 * Helper thread L2 cache request state.
 *
 * @author Min Cai
 */
public class HelperThreadL2CacheRequestState {
    private HelperThreadL2CacheRequestProfilingHelper helperThreadL2CacheRequestProfilingHelper;

    private int inFlightThreadId;
    private int threadId;
    private int pc;
    private boolean hitToTransientTag;

    private int victimThreadId;
    private int victimPc;
    private int victimTag;

    private HelperThreadL2CacheRequestQuality quality;

    /**
     * Create a helper thread L2 cache request state.
     *
     * @param helperThreadL2CacheRequestProfilingHelper the helper thread L2 cache request profiling helper
     */
    public HelperThreadL2CacheRequestState(HelperThreadL2CacheRequestProfilingHelper helperThreadL2CacheRequestProfilingHelper) {
        this.helperThreadL2CacheRequestProfilingHelper = helperThreadL2CacheRequestProfilingHelper;

        this.inFlightThreadId = -1;
        this.threadId = -1;
        this.pc = -1;

        this.victimThreadId = -1;
        this.victimPc = -1;
        this.victimTag = CacheLine.INVALID_TAG;

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

    /**
     * Get the victim's thread ID.
     *
     * @return the victim's thread ID
     */
    public int getVictimThreadId() {
        return victimThreadId;
    }

    /**
     * Set the victim's thread ID.
     *
     * @param victimThreadId the victim thread ID
     */
    public void setVictimThreadId(int victimThreadId) {
        this.victimThreadId = victimThreadId;
    }

    /**
     * Get the victim PC.
     *
     * @return the victim PC
     */
    public int getVictimPc() {
        return victimPc;
    }

    /**
     * Set the victim PC.
     *
     * @param victimPc the victim PC
     */
    public void setVictimPc(int victimPc) {
        this.victimPc = victimPc;
    }

    /**
     * Get the victim tag.
     *
     * @return the victim tag
     */
    public int getVictimTag() {
        return victimTag;
    }

    /**
     * Set the victim tag.
     *
     * @param victimTag the victim tag
     */
    public void setVictimTag(int victimTag) {
        this.victimTag = victimTag;
    }

    /**
     * Get the quality.
     *
     * @return the quality
     */
    public HelperThreadL2CacheRequestQuality getQuality() {
        return quality;
    }

    /**
     * Set the quality.
     *
     * @param quality the quality
     */
    public void setQuality(HelperThreadL2CacheRequestQuality quality) {
        if(quality == HelperThreadL2CacheRequestQuality.INVALID) {
            throw new IllegalArgumentException();
        }

        if(this.quality != HelperThreadL2CacheRequestQuality.INVALID && !this.quality.isModifiable()) {
            throw new IllegalArgumentException(this.quality + "->" + quality);
        }

        this.quality = quality;
    }

    /**
     * Invalidate.
     */
    public void invalidate() {
        if(this.quality != HelperThreadL2CacheRequestQuality.INVALID) {
            if(!HelperThreadingHelper.isHelperThread(this.threadId)) {
                throw new IllegalArgumentException(this.threadId + ":" + quality);
            }

            this.helperThreadL2CacheRequestProfilingHelper.updateStats(this.getQuality());
            this.quality = HelperThreadL2CacheRequestQuality.INVALID;
        }
    }

    @Override
    public String toString() {
        return String.format("HelperThreadL2CacheRequestState{inFlightThreadId=%d, threadId=%d, pc=0x%08x, hitToTransientTag=%s, victimThreadId=%d, victimPc=0x%08x, victimTag=0x%08x, quality=%s}", inFlightThreadId, threadId, pc, hitToTransientTag, victimThreadId, victimPc, victimTag, quality);
    }
}
