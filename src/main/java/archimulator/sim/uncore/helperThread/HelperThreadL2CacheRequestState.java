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
     *
     */
    public HelperThreadL2CacheRequestState() {
        this.inFlightThreadId = -1;
        this.threadId = -1;
        this.pc = -1;
        this.quality = HelperThreadL2CacheRequestQuality.INVALID;
    }

    /**
     *
     * @return
     */
    public int getInFlightThreadId() {
        return inFlightThreadId;
    }

    /**
     *
     * @param inFlightThreadId
     */
    public void setInFlightThreadId(int inFlightThreadId) {
        this.inFlightThreadId = inFlightThreadId;
    }

    /**
     *
     * @return
     */
    public int getThreadId() {
        return threadId;
    }

    /**
     *
     * @param threadId
     */
    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    /**
     *
     * @return
     */
    public int getPc() {
        return pc;
    }

    /**
     *
     * @param pc
     */
    public void setPc(int pc) {
        this.pc = pc;
    }

    /**
     *
     * @param quality
     */
    public void setQuality(HelperThreadL2CacheRequestQuality quality) {
        if (this.quality != HelperThreadL2CacheRequestQuality.INVALID && quality != HelperThreadL2CacheRequestQuality.INVALID && !this.quality.isModifiable()) {
            throw new IllegalArgumentException();
        }

        this.quality = quality;
    }

    /**
     *
     * @return
     */
    public HelperThreadL2CacheRequestQuality getQuality() {
        return quality;
    }

    /**
     *
     * @param hitToTransientTag
     */
    public void setHitToTransientTag(boolean hitToTransientTag) {
        this.hitToTransientTag = hitToTransientTag;
    }

    /**
     *
     * @return
     */
    public boolean isHitToTransientTag() {
        return hitToTransientTag;
    }
}
