/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.mem.ext.ht;

import archimulator.mem.MemoryHierarchyAccess;

public class HtRequest {
    private MemoryHierarchyAccess htAccess;

    private HtRequestQuality quality;

    private MemoryHierarchyAccess hitByAccess;
    private MemoryHierarchyAccess victimAccess;

    public HtRequest(MemoryHierarchyAccess htAccess) {
        this.htAccess = htAccess;

        this.quality = HtRequestQuality.UGLY;

        this.hitByAccess = null;
        this.victimAccess = null;
    }

    public MemoryHierarchyAccess getHtAccess() {
        return htAccess;
    }

    public void setHtAccess(MemoryHierarchyAccess htAccess) {
        this.htAccess = htAccess;
    }

    public HtRequestQuality getQuality() {
        return quality;
    }

    public void setQuality(HtRequestQuality quality) {
        this.quality = quality;
    }

    public MemoryHierarchyAccess getHitByAccess() {
        return hitByAccess;
    }

    public void setHitByAccess(MemoryHierarchyAccess hitByAccess) {
        this.hitByAccess = hitByAccess;
    }

    public MemoryHierarchyAccess getVictimAccess() {
        return victimAccess;
    }

    public void setVictimAccess(MemoryHierarchyAccess victimAccess) {
        this.victimAccess = victimAccess;
    }

    @Override
    public String toString() {
        return String.format("HtRequest{access.id=%d, quality=%s, hitByAccess=%s, victimAccess=%s}", htAccess.getId(), quality, hitByAccess, victimAccess);
    }
}
