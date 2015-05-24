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
package archimulator.isa;

/**
 * A pseudocall.
 *
 * @author Min Cai
 */
public class PseudoCall {
    private int rs;
    private int imm;

    /**
     * Create a pseudocall.
     *
     * @param rs  the "rs" register value
     * @param imm the immediate value
     */
    public PseudoCall(int rs, int imm) {
        this.rs = rs;
        this.imm = imm;
    }

    /**
     * Get the "rs" register value.
     *
     * @return the "rs" register value
     */
    public int getRs() {
        return rs;
    }

    /**
     * Get the immediate value.
     *
     * @return the immediate value
     */
    public int getImm() {
        return imm;
    }

    /**
     * The immediate value of a pseudocall instruction indicating the spawning of a helper thread.
     */
    public static final int PSEUDOCALL_HELPER_THREAD_SPAWN = 3720;

    /**
     * The immediate value of a pseudocall instruction indicating the beginning of a hotspot function.
     */
    public static final int PSEUDOCALL_HOTSPOT_FUNCTION_BEGIN = 3721;

    /**
     * The immediate value of a pseudocall instruction indicating the end of a hotspot function.
     */
    public static final int PSEUDOCALL_HOTSPOT_FUNCTION_END = 3723;
}
