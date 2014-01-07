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
package archimulator.sim.os;

/**
 * Function call context.
 *
 * @author Min Cai
 */
public class FunctionCallContext {
    private Context context;
    private int pc;
    private int targetPc;

    /**
     * Create a function call context.
     *
     * @param context  the context.
     * @param pc       the PC (program counter)'s value
     * @param targetPc the target PC (program counter)'s value
     */
    public FunctionCallContext(Context context, int pc, int targetPc) {
        this.context = context;
        this.pc = pc;
        this.targetPc = targetPc;
    }

    /**
     * Get the context.
     *
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Get the PC (program counter)'s value.
     *
     * @return the PC (program counter)'s value
     */
    public int getPc() {
        return pc;
    }

    /**
     * Get the target PC (program counter)'s value.
     *
     * @return the target PC (program counter)'s value
     */
    public int getTargetPc() {
        return targetPc;
    }

    @Override
    public String toString() {
        return String.format(
                "FunctionCallContext{context.name=%s, %s.pc=0x%08x, %s.targetPc=0x%08x}",
                context.getName(),
                context.getProcess().getFunctionNameFromPc(pc),
                pc,
                context.getProcess().getFunctionNameFromPc(targetPc),
                targetPc
        );
    }
}
