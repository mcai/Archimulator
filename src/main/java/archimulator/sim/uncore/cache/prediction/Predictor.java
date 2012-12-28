/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.cache.prediction;

/**
 * Predictor.
 *
 * @author Min Cai
 * @param <PredictableT> the predictable type
 */
public interface Predictor<PredictableT extends Comparable<PredictableT>> {
    /**
     * Predict the value for the specified address.
     *
     *
     * @param address the address
     * @return the predicted value for the specified address
     */
    PredictableT predict(int address);

    /**
     * Update the observed value for the specified address.
     *
     * @param address the address
     * @param observedValue the observed value
     */
    void update(int address, PredictableT observedValue);

    /**
     * Get the default value.
     *
     * @return the default value
     */
    PredictableT getDefaultValue();

    /**
     * Get the number of misses.
     *
     * @return the number of misses
     */
    long getNumHits();

    /**
     * Get the number of misses.
     *
     * @return the number of misses
     */
    long getNumMisses();
}
