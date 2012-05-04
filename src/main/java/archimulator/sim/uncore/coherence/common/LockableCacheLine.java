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
package archimulator.sim.uncore.coherence.common;

import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.util.action.Action;
import archimulator.util.action.Function1X;
import archimulator.util.fsm.FiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachineFactory;

import java.util.ArrayList;
import java.util.List;

public class LockableCacheLine extends CacheLine<MESIState> {
    private FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition> replacementFsm;
    private int transientTag = -1;
    private List<Action> suspendedActions;

    public LockableCacheLine(Cache<?, ?> cache, int set, int way, MESIState initialState) {
        super(cache, set, way, initialState);

        this.replacementFsm = new FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition>(fsmFactory, "fsm", LockableCacheLineReplacementState.INVALID);
        this.suspendedActions = new ArrayList<Action>();
    }

    private boolean lock(Action action, int transientTag) {
        if (this.isLocked()) {
            this.suspendedActions.add(action);
            return false;
        } else {
            this.transientTag = transientTag;
            return true;
        }
    }

    public LockableCacheLine unlock() {
        if(this.replacementFsm.getState() == LockableCacheLineReplacementState.HITTING) {
            this.endHit();
        }
        else if(this.replacementFsm.getState() == LockableCacheLineReplacementState.FILLING) {
            this.endFill();
        }
        else {
            throw new IllegalArgumentException();
        }

        this.transientTag = -1;

        for (Action action : this.suspendedActions) {
            getCache().getCycleAccurateEventQueue().schedule(action, 0);
        }

        this.suspendedActions.clear();

        return this;
    }

    public boolean beginHit(Action action, int transientTag) {
        boolean result = lock(action, transientTag);
        if (result) {
            this.replacementFsm.fireTransition(LockableCacheLineReplacementCondition.BEGIN_HIT);
        }
        return result;
    }

    public void endHit() {
        this.replacementFsm.fireTransition(LockableCacheLineReplacementCondition.END_HIT);
    }

    public boolean beginEvict(Action action, int transientTag) {
        boolean result = lock(action, transientTag);
        if (result) {
            this.replacementFsm.fireTransition(LockableCacheLineReplacementCondition.BEGIN_EVICT);
        }
        return result;
    }

    public void endEvict() {
        this.replacementFsm.fireTransition(LockableCacheLineReplacementCondition.END_EVICT);
    }

    public boolean beginFill(Action action, int transientTag) {
        boolean result = lock(action, transientTag);
        if (result) {
            this.replacementFsm.fireTransition(LockableCacheLineReplacementCondition.BEGIN_FILL);
        }
        return result;
    }

    private void endFill() {
        this.replacementFsm.fireTransition(LockableCacheLineReplacementCondition.END_FILL);
    }

    public void invalidate() {
        this.replacementFsm.fireTransition(LockableCacheLineReplacementCondition.INVALIDATE);
        super.invalidate();
    }

    public int getTransientTag() {
        return transientTag;
    }

    public boolean isLocked() {
        return this.replacementFsm.getState().isLocked();
    }

    @Override
    public void setNonInitialState(MESIState state) {
        super.setNonInitialState(state);

        if(this.replacementFsm.getState() == LockableCacheLineReplacementState.INVALID) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return String.format("LockableCacheLine{set=%d, way=%d, tag=%s, transientTag=%s, state=%s}", getSet(), getWay(), getTag() == -1 ? "<INVALID>" : String.format("0x%08x", getTag()), transientTag == -1 ? "<INVALID>" : String.format("0x%08x", transientTag), getState());
    }

    private static FiniteStateMachineFactory<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition> fsmFactory;

    static {
        fsmFactory = new FiniteStateMachineFactory<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition>();

        fsmFactory.inState(LockableCacheLineReplacementState.INVALID)
                .onCondition(LockableCacheLineReplacementCondition.BEGIN_FILL, new Function1X<FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition>, LockableCacheLineReplacementState>() {
                    @Override
                    public LockableCacheLineReplacementState apply(FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition> param1, Object... otherParams) {
                        return LockableCacheLineReplacementState.FILLING;
                    }
                });

        fsmFactory.inState(LockableCacheLineReplacementState.VALID)
                .onCondition(LockableCacheLineReplacementCondition.BEGIN_EVICT, new Function1X<FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition>, LockableCacheLineReplacementState>() {
                    @Override
                    public LockableCacheLineReplacementState apply(FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition> param1, Object... otherParams) {
                        return LockableCacheLineReplacementState.EVICTING;
                    }
                })
                .onCondition(LockableCacheLineReplacementCondition.BEGIN_HIT, new Function1X<FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition>, LockableCacheLineReplacementState>() {
                    @Override
                    public LockableCacheLineReplacementState apply(FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition> param1, Object... otherParams) {
                        return LockableCacheLineReplacementState.HITTING;
                    }
                });

        fsmFactory.inState(LockableCacheLineReplacementState.HITTING)
                .onCondition(LockableCacheLineReplacementCondition.END_HIT, new Function1X<FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition>, LockableCacheLineReplacementState>() {
                    @Override
                    public LockableCacheLineReplacementState apply(FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition> param1, Object... otherParams) {
                        return LockableCacheLineReplacementState.VALID;
                    }
                })
                .onCondition(LockableCacheLineReplacementCondition.INVALIDATE, new Function1X<FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition>, LockableCacheLineReplacementState>() {
                    @Override
                    public LockableCacheLineReplacementState apply(FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition> param1, Object... otherParams) {
                        return LockableCacheLineReplacementState.INVALID;
                    }
                });

        fsmFactory.inState(LockableCacheLineReplacementState.EVICTING)
                .onCondition(LockableCacheLineReplacementCondition.END_EVICT, new Function1X<FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition>, LockableCacheLineReplacementState>() {
                    @Override
                    public LockableCacheLineReplacementState apply(FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition> param1, Object... otherParams) {
                        return LockableCacheLineReplacementState.EVICTED;
                    }
                });

        fsmFactory.inState(LockableCacheLineReplacementState.EVICTED)
                .onCondition(LockableCacheLineReplacementCondition.BEGIN_FILL, new Function1X<FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition>, LockableCacheLineReplacementState>() {
                    @Override
                    public LockableCacheLineReplacementState apply(FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition> param1, Object... otherParams) {
                        return LockableCacheLineReplacementState.FILLING;
                    }
                });

        fsmFactory.inState(LockableCacheLineReplacementState.FILLING)
                .onCondition(LockableCacheLineReplacementCondition.END_FILL, new Function1X<FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition>, LockableCacheLineReplacementState>() {
                    @Override
                    public LockableCacheLineReplacementState apply(FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition> param1, Object... otherParams) {
                        return LockableCacheLineReplacementState.VALID;
                    }
                });
    }

    public static void main(String[] args) {
        FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition> fsm =
                new FiniteStateMachine<LockableCacheLineReplacementState, LockableCacheLineReplacementCondition>(fsmFactory, "fsm", LockableCacheLineReplacementState.INVALID);

        fsm.fireTransition(LockableCacheLineReplacementCondition.BEGIN_FILL);
        fsm.fireTransition(LockableCacheLineReplacementCondition.END_FILL);

        fsm.fireTransition(LockableCacheLineReplacementCondition.BEGIN_EVICT);
        fsm.fireTransition(LockableCacheLineReplacementCondition.END_EVICT);

        fsm.fireTransition(LockableCacheLineReplacementCondition.BEGIN_FILL);
        fsm.fireTransition(LockableCacheLineReplacementCondition.END_FILL);
    }
}
