/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache.replacement;

import archimulator.common.report.ReportNode;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.*;
import archimulator.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.uncore.cache.prediction.Predictor;
import archimulator.uncore.helperThread.HelperThreadingHelper;
import archimulator.util.ValueProvider;

import java.io.Serializable;

//TODO: to be refactored out

/**
 * Test policy #2.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class TestPolicy2<StateT extends Serializable> extends LRUPolicy<StateT> {
    private Cache<Boolean> mirrorCache;
    private Predictor<Boolean> replacementOwnershipPredictor;

    /**
     * Create a test policy #2 for the specified cache.
     *
     * @param cache the parent cache
     */
    @SuppressWarnings("unchecked")
    public TestPolicy2(EvictableCache<StateT> cache) {
        super(cache);

        this.mirrorCache = new BasicCache<>(
                cache,
                getCache().getName() + ".testEvictionPolicy2.mirrorCache",
                cache.getGeometry(),
                args -> new BooleanValueProvider()
        );

        this.replacementOwnershipPredictor = new CacheBasedPredictor<>(
                cache,
                cache.getName() + ".replacementOwnershipPredictor",
                16,
                1,
                3,
                false
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        if (HelperThreadingHelper.isMainThread(access.getThread().getId())) {
            return super.handleReplacement(access, set, tag);
        } else {
            for (int i = this.getCache().getAssociativity() - 1; i >= 0; i--) {
                int way = this.getWayInStackPosition(set, i);
                CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
                BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
                if (!stateProvider.ownedByMainThread) {
                    return new CacheAccess<>(this.getCache(), access, set, way, tag);
                }
            }

            return new CacheAccess<>(this.getCache(), access, set, getCache().getAssociativity() - 1, tag); //TODO: or just bypass? i'm not sure the performance impact!
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        super.handlePromotionOnHit(access, set, way);

        this.handleLineReference(set, way, this.getCache().getLine(set, way).getAccess().getThread().getId());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        super.handleInsertionOnMiss(access, set, way);

        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();
        stateProvider.pc = access.getVirtualPc();

        this.handleLineReference(set, way, access.getThread().getId());
    }

    /**
     * Handle line reference.
     *
     * @param set      the set index
     * @param way      the way
     * @param threadId the ID of the thread
     */
    @SuppressWarnings("unchecked")
    private void handleLineReference(int set, int way, int threadId) {
        CacheLine<Boolean> mirrorLine = this.mirrorCache.getLine(set, way);
        BooleanValueProvider stateProvider = (BooleanValueProvider) mirrorLine.getStateProvider();

        int pc = stateProvider.pc;

//        this.replacementOwnershipPredictor.update(pc, BasicThread.isMainThread(threadId));

        if (HelperThreadingHelper.isMainThread(threadId)) {
            this.replacementOwnershipPredictor.update(pc, true);
        }

        stateProvider.ownedByMainThread = this.replacementOwnershipPredictor.predict(pc);
    }

    /**
     * Boolean value provider.
     */
    private class BooleanValueProvider implements ValueProvider<Boolean> {
        private boolean state;
        private int pc;
        private boolean ownedByMainThread;

        /**
         * Create a boolean value provider.
         */
        private BooleanValueProvider() {
            this.state = true;
        }

        /**
         * Get the value.
         *
         * @return the value
         */
        @Override
        public Boolean get() {
            return state;
        }

        /**
         * Get the initial value.
         *
         * @return the initial value
         */
        @Override
        public Boolean getInitialValue() {
            return true;
        }
    }
}
