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

public class SetValueEvent<KeyT, ValueT, AccessTypeT extends SimpleCacheAccessType> extends CacheEvent {
    private KeyT key;
    private ValueT oldValue;
    private ValueT newValue;
    private AccessTypeT oldAccessType;
    private AccessTypeT newAccessType;
    private boolean hitInCache;
    private boolean eviction;
    private boolean writeback;

    public SetValueEvent(KeyT key, ValueT oldValue, ValueT newValue, AccessTypeT oldAccessType, AccessTypeT newAccessType, boolean hitInCache, boolean eviction, boolean writeback) {
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.oldAccessType = oldAccessType;
        this.newAccessType = newAccessType;
        this.hitInCache = hitInCache;
        this.eviction = eviction;
        this.writeback = writeback;
    }

    public KeyT getKey() {
        return key;
    }

    public ValueT getOldValue() {
        return oldValue;
    }

    public ValueT getNewValue() {
        return newValue;
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
        return String.format("SetValueEvent{key=%s, oldValue=%s, newValue=%s, oldAccessType=%s, newAccessType=%s, hitInCache=%s, eviction=%s, writeback=%s}", key, oldValue, newValue, oldAccessType, newAccessType, hitInCache, eviction, writeback);
    }
}
