/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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

import archimulator.util.math.MathHelper;
import archimulator.util.trace.TraceTester;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Branch predictor trace tester.
 *
 * @author Min Cai
 */
public class BranchPredictorTraceTester extends TraceTester<BranchPredictorTraceRecord> {
    /**
     * Create a branch predictor trace tester.
     *
     * @param fileName the file name
     */
    public BranchPredictorTraceTester(String fileName) {
        super(fileName);
    }

    /**
     * Read the next record from the trace file.
     *
     * @return the next record
     */
    @Override
    protected BranchPredictorTraceRecord readNext() {
        byte[] data = new byte[9];

        try {
            if (buffer.read(data) > 0) {
                ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

                byte code = bb.get();
                int opcode = MathHelper.bits(code, 3, 0);
                int rawBranchFlags = MathHelper.bits(code, 7, 4);

                int branchFlags = 0;

                switch (rawBranchFlags) {
                    case 1: // taken conditional branch
                        branchFlags |= BranchInfo.BR_CONDITIONAL;
                        break;
                    case 2: // not taken conditional branch
                        branchFlags |= BranchInfo.BR_CONDITIONAL;
                        break;
                    case 3: // unconditional branch
                        break;
                    case 4: // indirect branch
                        branchFlags |= BranchInfo.BR_INDIRECT;
                        break;
                    case 5: // call
                        branchFlags |= BranchInfo.BR_CALL;
                        break;
                    case 6: // indirect call
                        branchFlags |= BranchInfo.BR_CALL | BranchInfo.BR_INDIRECT;
                        break;
                    case 7: // return
                        branchFlags |= BranchInfo.BR_RETURN;
                        break;
                }

//                System.out.printf("code: 0x%08x(%s), opcode: 0x%08x(%s), branchFlags: 0x%08x(%s)\n", code, Integer.toBinaryString(code), opcode, Integer.toBinaryString(opcode), branchFlags, Integer.toBinaryString(branchFlags));

                int address = bb.getInt();
                int target = bb.getInt();

                return new BranchPredictorTraceRecord(rawBranchFlags != 2, target, new BranchInfo(address, opcode, branchFlags));
            }

            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
