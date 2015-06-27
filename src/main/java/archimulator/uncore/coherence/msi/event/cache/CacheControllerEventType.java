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
package archimulator.uncore.coherence.msi.event.cache;

/**
 * L1 Cache controller event type.
 *
 * @author Min Cai
 */
public enum CacheControllerEventType {
    /**
     * Load.
     */
    LOAD,

    /**
     * Store.
     */
    STORE,

    /**
     * Replacement.
     */
    REPLACEMENT,

    /**
     * Forwarded GetS.
     */
    FWD_GETS,

    /**
     * Forwarded GetM.
     */
    FWD_GETM,

    /**
     * Invalidation.
     */
    INV,

    /**
     * Recall.
     */
    RECALL,

    /**
     * Put acknowledgement.
     */
    PUT_ACK,

    /**
     * Data from the directory controller where there is no pending acknowledgement expected.
     */
    DATA_FROM_DIR_ACKS_EQ_0,

    /**
     * Data from the directory controller where there are pending acknowledgements expected.
     */
    DATA_FROM_DIR_ACKS_GT_0,

    /**
     * Data from the owner L1 cache controller
     */
    DATA_FROM_OWNER,

    /**
     * Invalidation acknowledgement.
     */
    INV_ACK,

    /**
     * Last invalidation acknowledgement.
     */
    LAST_INV_ACK
}
