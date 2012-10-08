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
package archimulator.sim.uncore.coherence.msi.message;

/**
 *
 * @author Min Cai
 */
public enum CoherenceMessageType {
    /**
     *
     */
    GETS,
    /**
     *
     */
    GETM,
    /**
     *
     */
    PUTS,
    /**
     *
     */
    PUTM_AND_DATA,

    /**
     *
     */
    FORWARD_GETS,
    /**
     *
     */
    FORWARD_GETM,
    /**
     *
     */
    INVALIDATION,
    /**
     *
     */
    RECALL,

    /**
     *
     */
    PUT_ACKNOWLEDGEMENT,
    /**
     *
     */
    DATA,
    /**
     *
     */
    INVALIDATION_ACKNOWLEDGEMENT,
    /**
     *
     */
    RECALL_ACKNOWLEDGEMENT
}
