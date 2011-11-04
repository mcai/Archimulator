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
package archimulator.core.bpred;

import archimulator.core.Thread;
import archimulator.isa.Mnemonic;
import archimulator.util.action.Action1;
import archimulator.util.Reference;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.ResetStatEvent;

public abstract class BranchPredictor {
    private String name;
    private BranchPredictorType type;

    protected int accesses;
    protected int hits;
    protected int misses;

    private Thread thread;

    public BranchPredictor(Thread thread, String name, BranchPredictorType type) {
        this.thread = thread;
        this.name = name;
        this.type = type;

        this.thread.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                BranchPredictor.this.accesses = 0;
                BranchPredictor.this.hits = 0;
            }
        });

        this.thread.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    event.getStats().put(BranchPredictor.this.name + ".type", BranchPredictor.this.type.toString());
                    event.getStats().put(BranchPredictor.this.name + ".accesses", String.valueOf(BranchPredictor.this.accesses));
                    event.getStats().put(BranchPredictor.this.name + ".hits", String.valueOf(BranchPredictor.this.hits));
                    event.getStats().put(BranchPredictor.this.name + ".misses", String.valueOf(BranchPredictor.this.misses));
                    event.getStats().put(BranchPredictor.this.name + ".hitRatio", String.valueOf(BranchPredictor.this.getHitRatio()));
                }
            }
        });
    }

    public abstract int predict(int baddr, int btarget, Mnemonic mnemonic, BranchPredictorUpdate dirUpdate, Reference<Integer> returnAddressStackRecoverIndex);

    public void update(int baddr, int btarget, boolean taken, boolean predTaken, boolean correct, Mnemonic mnemonic, BranchPredictorUpdate dirUpdate) {
        this.accesses++;

        if (correct) {
            this.hits++;
        } else {
            this.misses++;
        }
    }

    public abstract boolean isDynamic();

    public String getName() {
        return name;
    }

    public BranchPredictorType getType() {
        return type;
    }

    public int getAccesses() {
        return accesses;
    }

    public int getHits() {
        return hits;
    }

    public int getMisses() {
        return misses;
    }

    public double getHitRatio() {
        return this.accesses > 0 ? (double) this.hits / this.accesses : 0.0;
    }

    public Thread getThread() {
        return thread;
    }

    public static final int BRANCH_SHIFT = 2;
}
