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

import archimulator.util.Pair;
import archimulator.util.Reference;
import archimulator.util.event.BlockingEventDispatcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class SimpleCache<KeyT, ValueT, AccessTypeT extends SimpleCacheAccessType> {
    private List<CacheLine> lines;
    private LeastRecentlyUsedEvictionPolicy evictionPolicy;
    private BlockingEventDispatcher<CacheEvent> cacheEventDispatcher;

    public SimpleCache(int size) {
        this.lines = new ArrayList<CacheLine>();

        for(int i = 0; i < size; i++) {
            this.lines.add(new CacheLine(i));
        }

        this.evictionPolicy = new LeastRecentlyUsedEvictionPolicy();

        this.cacheEventDispatcher = new BlockingEventDispatcher<CacheEvent>();
    }

    @SuppressWarnings("unchecked")
    public KeyT[] getKeys() {
        List<KeyT> keys = new ArrayList<KeyT>();

        for(int i = 0; i < this.lines.size(); i++) {
            keys.add(this.evictionPolicy.getCacheLineInStackPosition(i).key);
        }

        return (KeyT[]) keys.toArray();
    }

    protected abstract void doWriteToNextLevel(KeyT key, ValueT value, boolean writeback);

    public void writeToNextLevel(CacheLine line, boolean writeback) {
        this.doWriteToNextLevel(line.key, line.value, writeback);
        line.key = null;
        line.value = null;
    }

    protected abstract Pair<ValueT, AccessTypeT> doReadFromNextLevel(KeyT key, ValueT oldValue);

    public ValueT get(KeyT key, AccessTypeT type) {
        Reference<ValueT> valueRef = new Reference<ValueT>();
        this.access(key, valueRef, type, false);
        return valueRef.get();
    }

    public void put(KeyT key, ValueT value, AccessTypeT type) {
        Reference<ValueT> valueRef = new Reference<ValueT>(value);
        this.access(key, valueRef, type, true);
    }

    private void access(KeyT key, Reference<ValueT> value, AccessTypeT type, boolean write) {
        CacheLine line = this.findLine(key);

        CacheAccess cacheAccess;

        if(line != null) {
            cacheAccess = this.newHit(key, value, type, write, line);
        }
        else {
            cacheAccess = this.newMiss(key,value, type, write);
        }

        if(cacheAccess.isHitInCache()) {
            cacheAccess.commit();
        }
        else {
            if(cacheAccess.isEviction()) {
                writeToNextLevel(cacheAccess.line, cacheAccess.isWriteback());
            }

            Pair<ValueT, AccessTypeT> pair = doReadFromNextLevel(key, cacheAccess.line.value);
            cacheAccess.line.value = pair.getFirst();
            cacheAccess.line.accessType = pair.getSecond();

            cacheAccess.commit();
        }
    }

    private CacheAccess newHit(KeyT key, Reference<ValueT> value, AccessTypeT type, boolean write, CacheLine line) {
        return new CacheHit(new CacheReference(key, value, type, write), line);
    }

    private CacheAccess newMiss(KeyT key, Reference<ValueT> value, AccessTypeT type, boolean write) {
        CacheReference reference = new CacheReference(key, value, type, write);

        for(int i = 0; i < lines.size(); i++) {
            CacheLine line = getLine(i);
            if(line.key == null) {
                return new CacheMiss(reference, line);
            }
        }

        return evictionPolicy.handleReplacement(reference);
    }

    private CacheLine getLine(int way) {
        return this.lines.get(way);
    }

    private CacheLine findLine(KeyT key) {
        for(CacheLine line : this.lines) {
            if(line.key != null && line.key.equals(key)) {
                return line;
            }
        }

        return null;
    }

    public BlockingEventDispatcher<CacheEvent> getCacheEventDispatcher() {
        return cacheEventDispatcher;
    }

    public class CacheLine {
        private int way;
        private KeyT key;
        private ValueT value;
        private AccessTypeT accessType;

        public CacheLine(int way) {
            this.way = way;
        }
    }

    public class CacheReference {
        private KeyT key;
        private Reference<ValueT> value;
        private AccessTypeT type;
        private boolean write;

        public CacheReference(KeyT key, Reference<ValueT> value, AccessTypeT type, boolean write) {
            this.key = key;
            this.value = value;
            this.type = type;
            this.write = write;
        }
    }

    public abstract class CacheAccess {
        CacheReference reference;
        CacheLine line;

        private boolean commmitted;

        public CacheAccess(CacheReference reference, CacheLine line) {
            this.reference = reference;
            this.line = line;
        }

        public CacheAccess commit() {
            if(this.commmitted) {
                throw new IllegalArgumentException();
            }

            this.commmitted = true;
            if(this.reference.write) {
                ValueT oldValue = this.line.value;
                AccessTypeT oldAccessType = this.line.accessType;

                this.line.value = this.reference.value.get();

                if(this.reference.type.isSetOnSetValue()) {
                    this.line.accessType = this.reference.type;
                }

                cacheEventDispatcher.dispatch(new SetValueEvent<KeyT, ValueT, AccessTypeT>(this.reference.key, oldValue, this.line.value, oldAccessType, this.reference.type, isHitInCache(), isEviction(), isWriteback()));
            }
            else {
                AccessTypeT oldAccessType = this.line.accessType;

                this.reference.value.set(this.line.value);

                if(this.reference.type.isSetOnGetValue()) {
                    this.line.accessType = this.reference.type;
                }

                cacheEventDispatcher.dispatch(new GetValueEvent<KeyT, ValueT, AccessTypeT>(this.reference.key, this.reference.value.get(), oldAccessType, this.reference.type, isHitInCache(), isEviction(), isWriteback()));
            }
            return this;
        }

        public abstract boolean isHitInCache();

        public abstract boolean isEviction();
        
        public abstract boolean isWriteback();
    }

    public class CacheHit extends CacheAccess {
        public CacheHit(CacheReference reference, CacheLine line) {
            super(reference, line);
        }

        @Override
        public boolean isHitInCache() {
            return true;
        }

        @Override
        public boolean isEviction() {
            return false;
        }

        @Override
        public boolean isWriteback() {
            return false;
        }

        @Override
        public CacheAccess commit() {
            evictionPolicy.handlePromotionOnHit(this);
            return super.commit();
        }
    }

    public class CacheMiss extends CacheAccess {
        private boolean eviction;
        private boolean writeback;

        public CacheMiss(CacheReference reference, CacheLine line) {
            super(reference, line);

            this.eviction = (line.key != null);
            this.writeback = (line.key != null && line.accessType.isDirty());
        }

        @Override
        public boolean isHitInCache() {
            return false;
        }

        @Override
        public boolean isEviction() {
            return eviction;
        }

        @Override
        public boolean isWriteback() {
            return writeback;
        }

        @Override
        public CacheAccess commit() {
            line.key = this.reference.key;
            evictionPolicy.handleInsertionOnMiss(this);
            return super.commit();
        }
    }

    public static interface EvictionPolicy<CacheReferenceT, CacheHitT, CacheMissT> {
        CacheMissT handleReplacement(CacheReferenceT reference);

        void handlePromotionOnHit(CacheHitT hit);

        void handleInsertionOnMiss(CacheMissT miss);
    }

    public abstract class StackBasedEvictionPolicy implements EvictionPolicy<CacheReference, CacheHit, CacheMiss> {
        private List<StackEntry> stackEntries;

        protected StackBasedEvictionPolicy() {
            this.stackEntries = new ArrayList<StackEntry>();

            for(int i = 0; i < lines.size(); i++) {
                this.stackEntries.add(new StackEntry(getLine(i)));
            }
        }

        public int getMRU() {
             return this.getCacheLineInStackPosition(0).way;
        }

        public int getLRU() {
            return this.getCacheLineInStackPosition(lines.size() - 1).way;
        }

        public void setMRU(int way) {
            this.setStackPosition(way, 0);
        }

        public void setLRU(int way) {
            this.setStackPosition(way, lines.size() - 1);
        }

        public int getWayInStackPosition(int stackPosition) {
            return this.getCacheLineInStackPosition(stackPosition).way;
        }

        public CacheLine getCacheLineInStackPosition(int stackPosition) {
            return this.stackEntries.get(stackPosition).line;
        }

        public int getStackPosition(int way) {
            StackEntry stackEntryFound = this.getStackEntry(way);
            return this.stackEntries.indexOf(stackEntryFound);
        }

        public void setStackPosition(int way, int newStackPosition) {
            StackEntry stackEntryFound = this.getStackEntry(way);
            this.stackEntries.remove(stackEntryFound);
            this.stackEntries.add(newStackPosition, stackEntryFound);
        }

        private StackEntry getStackEntry(int way) {
            for(StackEntry stackEntry : this.stackEntries) {
                if(stackEntry.line.way == way) {
                    return stackEntry;
                }
            }

            throw new IllegalArgumentException();
        }

        private class StackEntry implements Serializable {
            private CacheLine line;

            private StackEntry(CacheLine line) {
                this.line = line;
            }
        }
    }

    public class LeastRecentlyUsedEvictionPolicy extends StackBasedEvictionPolicy {
        public CacheMiss handleReplacement(CacheReference reference) {
            return new CacheMiss(reference, getLine(this.getLRU()));
        }

        public void handlePromotionOnHit(CacheHit hit) {
            this.setMRU(hit.line.way);
        }

        public void handleInsertionOnMiss(CacheMiss miss) {
            this.setMRU(miss.line.way);
        }
    }
}
