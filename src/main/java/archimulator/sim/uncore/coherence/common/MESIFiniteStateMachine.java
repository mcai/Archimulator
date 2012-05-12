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

import archimulator.util.action.Function1X;
import archimulator.util.fsm.FiniteStateMachineFactory;
import archimulator.util.fsm.SimpleFiniteStateMachine;

public class MESIFiniteStateMachine extends SimpleFiniteStateMachine<MESIState, MESICondition> {
    public MESIFiniteStateMachine() {
        super(MESIState.INVALID);
    }

    protected void notifyDirectory() {
        System.out.println("notifyDirectory()");
    }

    protected void ackToDirectory() {
        System.out.println("ackToDirectory()");
    }

    protected void copyBackToDirectory() {
        System.out.println("copyBackToDirectory()");
    }

    protected void writeBackToDirectory() {
        System.out.println("writeBackToDirectory()");
    }

    protected void peerTransfer(String peer) {
        System.out.println("peerTransfer(" + peer + ")");
    }

    public void fireTransition(MESICondition condition, Object... params) {
        fsmFactory.fireTransition(this, condition, params);
    }

    private static FiniteStateMachineFactory<MESIState, MESICondition, MESIFiniteStateMachine> fsmFactory;

    static {
        fsmFactory = new FiniteStateMachineFactory<MESIState, MESICondition, MESIFiniteStateMachine>();

        fsmFactory.inState(MESIState.MODIFIED)
                .onConditions(MESICondition.READ_WRITE, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        return MESIState.MODIFIED;
                    }
                })
                .onCondition(MESICondition.REPLACEMENT, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        fsm.writeBackToDirectory();
                        return MESIState.INVALID;
                    }
                })
                .onCondition(MESICondition.EXTERNAL_READ, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        String peer = (String) params[0];
                        fsm.peerTransfer(peer);
                        fsm.copyBackToDirectory();
                        return MESIState.SHARED;
                    }
                })
                .onCondition(MESICondition.EXTERNAL_WRITE, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        String peer = (String) params[0];
                        fsm.peerTransfer(peer);
                        fsm.ackToDirectory();
                        return MESIState.INVALID;
                    }
                });

        fsmFactory.inState(MESIState.EXCLUSIVE)
                .onConditions(MESICondition.READ, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        return MESIState.EXCLUSIVE;
                    }
                })
                .onCondition(MESICondition.WRITE, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        return MESIState.MODIFIED;
                    }
                })
                .onCondition(MESICondition.REPLACEMENT, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        fsm.notifyDirectory();
                        return MESIState.INVALID;
                    }
                })
                .onCondition(MESICondition.EXTERNAL_READ, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        String peer = (String) params[0];
                        fsm.peerTransfer(peer);
                        fsm.ackToDirectory();
                        return MESIState.SHARED;
                    }
                })
                .onCondition(MESICondition.EXTERNAL_WRITE, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        String peer = (String) params[0];
                        fsm.peerTransfer(peer);
                        fsm.ackToDirectory();
                        return MESIState.INVALID;
                    }
                });

        fsmFactory.inState(MESIState.SHARED)
                .onConditions(MESICondition.READ, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        return MESIState.SHARED;
                    }
                })
                .onCondition(MESICondition.WRITE, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        return MESIState.MODIFIED;
                    }
                })
                .onCondition(MESICondition.REPLACEMENT, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        return MESIState.INVALID;
                    }
                })
                .onCondition(MESICondition.EXTERNAL_WRITE, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        fsm.ackToDirectory();
                        return MESIState.INVALID;
                    }
                });

        fsmFactory.inState(MESIState.INVALID)
                .onCondition(MESICondition.READ_WITH_SHARERS, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        return MESIState.SHARED;
                    }
                })
                .onCondition(MESICondition.READ_NO_SHARERS, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        return MESIState.EXCLUSIVE;
                    }
                })
                .onCondition(MESICondition.WRITE, new Function1X<MESIFiniteStateMachine, MESIState>() {
                    @Override
                    public MESIState apply(MESIFiniteStateMachine fsm, Object... params) {
                        return MESIState.MODIFIED;
                    }
                });
    }

    public static FiniteStateMachineFactory<MESIState, MESICondition, MESIFiniteStateMachine> getFsmFactory() {
        return fsmFactory;
    }

    public static void main(String[] args) {
        MESIFiniteStateMachine fsm = new MESIFiniteStateMachine();

        fsm.fireTransition(MESICondition.WRITE);
        fsm.fireTransition(MESICondition.READ_NO_SHARERS);
        fsm.fireTransition(MESICondition.READ_NO_SHARERS);
        fsm.fireTransition(MESICondition.READ_NO_SHARERS);
        fsm.fireTransition(MESICondition.READ_NO_SHARERS);

        fsm.fireTransition(MESICondition.EXTERNAL_READ, "peer");
        fsm.fireTransition(MESICondition.EXTERNAL_WRITE, "peer");
        fsm.fireTransition(MESICondition.READ_NO_SHARERS);
        fsm.fireTransition(MESICondition.EXTERNAL_READ, "peer");
        fsm.fireTransition(MESICondition.REPLACEMENT);
    }
}
