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
    private List<List<CacheLine>> lines;
    private LeastRecentlyUsedEvictionPolicy evictionPolicy;
    private BlockingEventDispatcher<CacheEvent> cacheEventDispatcher;
    private int numSets;
    private int associativity;

    public SimpleCache(int numSets, int associativity) {
        this.numSets = numSets;
        this.associativity = associativity;

        this.lines = new ArrayList<List<CacheLine>>();

        for (int i = 0; i < numSets; i++) {
            ArrayList<CacheLine> linesPerSet = new ArrayList<CacheLine>();
            this.lines.add(linesPerSet);

            for (int j = 0; j < associativity; j++) {
                linesPerSet.add(new CacheLine(i, j));
            }
        }

        this.evictionPolicy = new LeastRecentlyUsedEvictionPolicy();

        this.cacheEventDispatcher = new BlockingEventDispatcher<CacheEvent>();
    }

    @SuppressWarnings("unchecked")
    public KeyT[] getKeys(int set) {
        List<KeyT> keys = new ArrayList<KeyT>();

        for (int i = 0; i < this.associativity; i++) {
            keys.add(this.evictionPolicy.getCacheLineInStackPosition(set, i).key);
        }

        return (KeyT[]) keys.toArray();
    }

    protected abstract Pair<ValueT, AccessTypeT> doReadFromNextLevel(KeyT key, ValueT oldValue);

    protected abstract void doWriteToNextLevel(KeyT key, ValueT value, boolean writeback);

    private void invalidate(CacheLine line) {
        this.doWriteToNextLevel(line.key, line.value, line.isDirty());
        line.key = null;
        line.value = null;
    }

    public Pair<KeyT, ValueT> getLRU(int set) {
        int way = this.evictionPolicy.getLRU(set);

        CacheLine line = this.getLine(set, way);

        return new Pair<KeyT, ValueT>(line.key, line.value);
    }

    public void setLRU(int set, KeyT key) {
        CacheLine line = this.findLine(set, key);

        if (line == null) {
            throw new IllegalArgumentException();
        }

        this.evictionPolicy.setLRU(set, line.way);
    }

    public void removeLRU(int set) {
        int way = this.evictionPolicy.getLRU(set);

        CacheLine line = this.getLine(set, way);

        this.invalidate(line);
    }

    public ValueT get(int set, KeyT key, AccessTypeT type) {
        Reference<ValueT> valueRef = new Reference<ValueT>();
        this.access(set, key, valueRef, type, false);
        return valueRef.get();
    }

    public void put(int set, KeyT key, ValueT value, AccessTypeT type) {
        Reference<ValueT> valueRef = new Reference<ValueT>(value);
        this.access(set, key, valueRef, type, true);
    }

    private void access(int set, KeyT key, Reference<ValueT> value, AccessTypeT type, boolean write) {
        CacheLine line = this.findLine(set, key);

        CacheAccess cacheAccess;

        if (line != null) {
            cacheAccess = this.newHit(set, key, value, type, write, line);
        } else {
            cacheAccess = this.newMiss(set, key, value, type, write);
        }

        if (cacheAccess.isHitInCache()) {
            cacheAccess.commit();
        } else {
            if (cacheAccess.isEviction()) {
                this.invalidate(cacheAccess.line);
            }

            Pair<ValueT, AccessTypeT> pair = this.doReadFromNextLevel(key, cacheAccess.line.value);
            cacheAccess.line.value = pair.getFirst();
            cacheAccess.line.accessType = pair.getSecond();

            cacheAccess.commit();
        }
    }

    private CacheAccess newHit(int set, KeyT key, Reference<ValueT> value, AccessTypeT type, boolean write, CacheLine line) {
        return new CacheHit(new CacheReference(set, key, value, type, write), line);
    }

    private CacheAccess newMiss(int set, KeyT key, Reference<ValueT> value, AccessTypeT type, boolean write) {
        CacheReference reference = new CacheReference(set, key, value, type, write);

        for (int i = 0; i < this.associativity; i++) {
            CacheLine line = getLine(set, i);
            if (line.key == null) {
                return new CacheMiss(reference, line);
            }
        }

        return evictionPolicy.handleReplacement(reference);
    }

    private CacheLine getLine(int set, int way) {
        return this.lines.get(set).get(way);
    }

    private CacheLine findLine(int set, KeyT key) {
        for (CacheLine line : this.lines.get(set)) {
            if (line.key != null && line.key.equals(key)) {
                return line;
            }
        }

        return null;
    }

    public BlockingEventDispatcher<CacheEvent> getCacheEventDispatcher() {
        return cacheEventDispatcher;
    }

    private class CacheLine {
        private int set;
        private int way;
        private KeyT key;
        private ValueT value;
        private AccessTypeT accessType;

        private CacheLine(int set, int way) {
            this.set = set;
            this.way = way;
        }

        public boolean isEviction() {
            return this.key != null;
        }

        public boolean isDirty() {
            return this.isEviction() && this.accessType.isDirty();
        }
    }

    private class CacheReference {
        private int set;
        private KeyT key;
        private Reference<ValueT> value;
        private AccessTypeT type;
        private boolean write;

        private CacheReference(int set, KeyT key, Reference<ValueT> value, AccessTypeT type, boolean write) {
            this.set = set;
            this.key = key;
            this.value = value;
            this.type = type;
            this.write = write;
        }
    }

    private abstract class CacheAccess {
        CacheReference reference;
        CacheLine line;

        private boolean commmitted;

        public CacheAccess(CacheReference reference, CacheLine line) {
            this.reference = reference;
            this.line = line;
        }

        public CacheAccess commit() {
            if (this.commmitted) {
                throw new IllegalArgumentException();
            }

            this.commmitted = true;
            if (this.reference.write) {
                ValueT oldValue = this.line.value;
                AccessTypeT oldAccessType = this.line.accessType;

                this.line.value = this.reference.value.get();

                if (this.reference.type.isSetOnSetValue()) {
                    this.line.accessType = this.reference.type;
                }

                cacheEventDispatcher.dispatch(new SetValueEvent<KeyT, ValueT, AccessTypeT>(this.reference.key, oldValue, this.line.value, oldAccessType, this.reference.type, isHitInCache(), isEviction(), isWriteback()));
            } else {
                AccessTypeT oldAccessType = this.line.accessType;

                this.reference.value.set(this.line.value);

                if (this.reference.type.isSetOnGetValue()) {
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

    private class CacheHit extends CacheAccess {
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

    private class CacheMiss extends CacheAccess {
        private boolean eviction;
        private boolean writeback;

        public CacheMiss(CacheReference reference, CacheLine line) {
            super(reference, line);

            this.eviction = line.isEviction();
            this.writeback = line.isDirty();
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

    private static interface EvictionPolicy<CacheReferenceT, CacheHitT, CacheMissT> {
        CacheMissT handleReplacement(CacheReferenceT reference);

        void handlePromotionOnHit(CacheHitT hit);

        void handleInsertionOnMiss(CacheMissT miss);
    }

    private abstract class StackBasedEvictionPolicy implements EvictionPolicy<CacheReference, CacheHit, CacheMiss> {
        private List<List<StackEntry>> stackEntries;

        protected StackBasedEvictionPolicy() {
            this.stackEntries = new ArrayList<List<StackEntry>>();

            for (int i = 0; i < numSets; i++) {
                ArrayList<StackEntry> stackEntriesPerSet = new ArrayList<StackEntry>();
                this.stackEntries.add(stackEntriesPerSet);

                for (int j = 0; j < associativity; j++) {
                    stackEntriesPerSet.add(new StackEntry(getLine(i, j)));

                }
            }
        }

        public int getMRU(int set) {
            return this.getCacheLineInStackPosition(set, 0).way;
        }

        public int getLRU(int set) {
            return this.getCacheLineInStackPosition(set, associativity - 1).way;
        }

        public void setMRU(int set, int way) {
            this.setStackPosition(set, way, 0);
        }

        public void setLRU(int set, int way) {
            this.setStackPosition(set, way, associativity - 1);
        }

        public int getWayInStackPosition(int set, int stackPosition) {
            return this.getCacheLineInStackPosition(set, stackPosition).way;
        }

        public CacheLine getCacheLineInStackPosition(int set, int stackPosition) {
            return this.stackEntries.get(set).get(stackPosition).line;
        }

        public int getStackPosition(int set, int way) {
            StackEntry stackEntryFound = this.getStackEntry(set, way);
            return this.stackEntries.get(set).indexOf(stackEntryFound);
        }

        public void setStackPosition(int set, int way, int newStackPosition) {
            StackEntry stackEntryFound = this.getStackEntry(set, way);
            this.stackEntries.get(set).remove(stackEntryFound);
            this.stackEntries.get(set).add(newStackPosition, stackEntryFound);
        }

        private StackEntry getStackEntry(int set, int way) {
            for (StackEntry stackEntry : this.stackEntries.get(set)) {
                if (stackEntry.line.way == way) {
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

    private class LeastRecentlyUsedEvictionPolicy extends StackBasedEvictionPolicy {
        public CacheMiss handleReplacement(CacheReference reference) {
            return new CacheMiss(reference, getLine(reference.set, this.getLRU(reference.set)));
        }

        public void handlePromotionOnHit(CacheHit hit) {
            this.setMRU(hit.line.set, hit.line.way);
        }

        public void handleInsertionOnMiss(CacheMiss miss) {
            this.setMRU(miss.line.set, miss.line.way);
        }
    }
}
