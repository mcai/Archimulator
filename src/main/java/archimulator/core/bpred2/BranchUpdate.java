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

/**
 * Branch update object.
 *
 * @author Min Cai
 */
public class BranchUpdate {
    private boolean predictedDirection;
    private int predictedTarget;

    /**
     * Create a branch update object.
     */
    public BranchUpdate() {
        this.predictedDirection = false;
        this.predictedTarget = 0;
    }

    /**
     * Get a value indicating whether the actual direction is the same as the predicted one or not.
     *
     * @return a value indicating whether the actual direction is the same as the predicted one or not
     */
    public boolean isPredictedDirection() {
        return predictedDirection;
    }

    /**
     * Set a value indicating whether the actual direction is the same as the predicted one or not.
     *
     * @param predictedDirection a value indicating whether the actual direction is the same as the predicted one or not
     */
    public void setPredictedDirection(boolean predictedDirection) {
        this.predictedDirection = predictedDirection;
    }

    /**
     * Get the predicted target address.
     *
     * @return the predicted target address
     */
    public int getPredictedTarget() {
        return predictedTarget;
    }

    /**
     * Set the predicted target address.
     *
     * @param predictedTarget the predicted target address
     */
    public void setPredictedTarget(int predictedTarget) {
        this.predictedTarget = predictedTarget;
    }
}
