/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.net.basic.routing.aco;

/**
 * Traffic.
 *
 * @author Min cai
 */
public class Traffic {
    private double mean;
    private double var;
    private double best;

    /**
     * Create the traffic.
     *
     * @param mean the mean value
     * @param var the var value
     * @param best the best value
     */
    public Traffic(double mean, double var, double best) {
        this.mean = mean;
        this.var = var;
        this.best = best;
    }

    /**
     * Get the mean value.
     *
     * @return the mean value
     */
    public double getMean() {
        return mean;
    }

    /**
     * Set the mean value.
     *
     * @param mean the mean value
     */
    public void setMean(double mean) {
        this.mean = mean;
    }

    /**
     * Get the var value.
     *
     * @return the var value
     */
    public double getVar() {
        return var;
    }

    /**
     * Set the var value.
     *
     * @param var the var value
     */
    public void setVar(double var) {
        this.var = var;
    }

    /**
     * Get the best value.
     *
     * @return the best value
     */
    public double getBest() {
        return best;
    }

    /**
     * Set the best value.
     *
     * @param best the best value
     */
    public void setBest(double best) {
        this.best = best;
    }
}
