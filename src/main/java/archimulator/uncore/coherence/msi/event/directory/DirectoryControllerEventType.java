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
package archimulator.uncore.coherence.msi.event.directory;

/**
 * Directory controller event type.
 *
 * @author Min Cai
 */
public enum DirectoryControllerEventType {
    /**
     * GetS.
     */
    GETS,

    /**
     * GetM.
     */
    GETM,

    /**
     * Replacement.
     */
    REPLACEMENT,

    /**
     * Recall acknowledgement.
     */
    RECALL_ACK,

    /**
     * Last recall acknowledgement.
     */
    LAST_RECALL_ACK,

    /**
     * PutS (not last).
     */
    PUTS_NOT_LAST,

    /**
     * PutS (last).
     */
    PUTS_LAST,

    /**
     * PutM and data (from the owner).
     */
    PUTM_AND_DATA_FROM_OWNER,

    /**
     * PutM and data (not from the owner).
     */
    PUTM_AND_DATA_FROM_NONOWNER,

    /**
     * Data from an L1 cache controller.
     */
    DATA,

    /**
     * Data from the memory controller
     */
    DATA_FROM_MEM
}
