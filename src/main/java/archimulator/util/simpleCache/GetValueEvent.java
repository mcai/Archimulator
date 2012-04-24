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
package archimulator.util.simpleCache;

public class GetValueEvent<KeyT, ValueT, AccessTypeT extends SimpleCacheAccessType> extends CacheEvent {
    private KeyT key;
    private ValueT value;
    private AccessTypeT oldAccessType;
    private AccessTypeT newAccessType;
    private boolean hitInCache;
    private boolean eviction;
    private boolean writeback;

    public GetValueEvent(KeyT key, ValueT value, AccessTypeT oldAccessType, AccessTypeT newAccessType, boolean hitInCache, boolean eviction, boolean writeback) {
        this.key = key;
        this.value = value;
        this.oldAccessType = oldAccessType;
        this.newAccessType = newAccessType;
        this.hitInCache = hitInCache;
        this.eviction = eviction;
        this.writeback = writeback;
    }

    public KeyT getKey() {
        return key;
    }

    public ValueT getValue() {
        return value;
    }

    public AccessTypeT getOldAccessType() {
        return oldAccessType;
    }

    public AccessTypeT getNewAccessType() {
        return newAccessType;
    }

    public boolean isHitInCache() {
        return hitInCache;
    }

    public boolean isEviction() {
        return eviction;
    }

    public boolean isWriteback() {
        return writeback;
    }

    @Override
    public String toString() {
        return String.format("GetValueEvent{key=%s, value=%s, oldAccessType=%s, newAccessType=%s, hitInCache=%s, eviction=%s, writeBack=%s}", key, value, oldAccessType, newAccessType, hitInCache, eviction, writeback);
    }
}
