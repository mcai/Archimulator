/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.core.speculativePrecomputation;

import archimulator.isa.ArchitecturalRegisterFile;
import archimulator.isa.StaticInstruction;
import archimulator.os.Context;
import archimulator.os.ContextState;
import archimulator.uncore.cache.CacheLine;

/**
 * Precomputation context.
 *
 * @author Min Cai
 */
public class PrecomputationContext extends Context {
    private DynamicSpeculativePrecomputationHelper dynamicSpeculativePrecomputationHelper;
    private int triggerPc;

    /**
     * Create a precomputation context.
     *
     * @param dynamicSpeculativePrecomputationHelper
     *                  the dynamic speculative precomputation helper
     * @param parent    the parent context
     * @param regs      the architectural register file
     * @param triggerPc the value of the trigger program counter (PC)
     */
    PrecomputationContext(DynamicSpeculativePrecomputationHelper dynamicSpeculativePrecomputationHelper, Context parent, ArchitecturalRegisterFile regs, int triggerPc) {
        super(parent, regs, 0);
        this.dynamicSpeculativePrecomputationHelper = dynamicSpeculativePrecomputationHelper;

        this.triggerPc = triggerPc;
    }

    @Override
    protected StaticInstruction decode(int pc) {
        CacheLine<Boolean> lineFound = this.dynamicSpeculativePrecomputationHelper.getSliceCache().findLine(this.triggerPc);
        DynamicSpeculativePrecomputationHelper.BooleanValueProvider stateProvider = (DynamicSpeculativePrecomputationHelper.BooleanValueProvider) lineFound.getStateProvider();

        if (stateProvider.getMachineInstructions().containsKey(pc)) {
            return super.decode(stateProvider.getMachineInstructions().get(pc));
        } else {
            if (this.getState() != ContextState.FINISHED) {
                this.finish();
            }
            return StaticInstruction.NOP;
        }
    }

    @Override
    public boolean useICache() {
        return false;
    }
}
