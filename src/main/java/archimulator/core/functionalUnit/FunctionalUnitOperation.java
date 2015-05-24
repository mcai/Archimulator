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
package archimulator.core.functionalUnit;

/**
 * Functional unit operation.
 *
 * @author Min Cai
 */
public class FunctionalUnitOperation {
    private int operationLatency;
    private int issueLatency;

    /**
     * Create a functional unit operation.
     *
     * @param operationLatency the operation latency in cycles
     * @param issueLatency     the issue latency in cycles
     */
    public FunctionalUnitOperation(int operationLatency, int issueLatency) {
        this.operationLatency = operationLatency;
        this.issueLatency = issueLatency;
    }

    /**
     * Get the operation latency in cycles.
     *
     * @return the operation latency in cycles
     */
    public int getOperationLatency() {
        return operationLatency;
    }

    /**
     * Get the issue latency in cycles.
     *
     * @return the issue latency in cycles
     */
    public int getIssueLatency() {
        return issueLatency;
    }
}
