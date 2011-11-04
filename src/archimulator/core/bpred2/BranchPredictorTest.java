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
package archimulator.core.bpred2;

import archimulator.util.action.Action1;
import archimulator.util.math.Counter;

public class BranchPredictorTest {
    public static void main(String[] args) {
        final BranchPredictor2 branchPredictor = new MyBranchPredictor();

        final Counter counterDirectionMispredictions = new Counter(0);
        final Counter counterTargetMispredictions = new Counter(0);

        BranchPredictorTraceTester traceReader = new BranchPredictorTraceTester("/opt/itecgo/Tools/cbp2-infrastructure-v2/src/compress/gzip.trace");

        traceReader.run(new Action1<BranchPredictorTraceRecord>() {
            public void apply(BranchPredictorTraceRecord traceRecord) {
//            System.out.printf("taken: %s, target: 0x%08x, address: 0x%08x, opcode: %d, brFlags: %d\n", traceRecord.isTaken(), traceRecord.getTarget(), traceRecord.getBranchInfo().getAddress(), traceRecord.getBranchInfo().getOpcode(), traceRecord.getBranchInfo().getBranchFlags());

                BranchUpdate branchUpdate = branchPredictor.predict(traceRecord.getBranchInfo());

                if ((traceRecord.getBranchInfo().getBranchFlags() & BranchInfo.BR_CONDITIONAL) != 0) {
                    if (branchUpdate.isPredictedDirection() != traceRecord.isTaken()) {
                        counterDirectionMispredictions.inc();
                    }

                    if (branchUpdate.getPredictedTarget() != traceRecord.getTarget()) {
                        counterTargetMispredictions.inc();
                    }

                    branchPredictor.update(traceRecord.getBranchInfo(), branchUpdate, traceRecord.isTaken(), traceRecord.getTarget());
                }
            }
        });

        System.out.printf("targetMispredictions: %f MPKI, directionMispredictions: %f MPKI\n", 1000.0 * (counterTargetMispredictions.getValue() / 1e8), 1000.0 * (counterDirectionMispredictions.getValue() / 1e8));
    }
}
