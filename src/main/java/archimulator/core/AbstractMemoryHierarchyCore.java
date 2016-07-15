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
package archimulator.core;

import archimulator.common.BasicSimulationObject;
import archimulator.common.CPUExperiment;
import archimulator.common.Simulation;

/**
 * Abstract memory hierarchy core.
 *
 * @author Min Cai
 */
public abstract class AbstractMemoryHierarchyCore
        extends BasicSimulationObject<CPUExperiment, Simulation>
        implements MemoryHierarchyCore {
    /**
     * The number of the core.
     */
    private int num;

    /**
     * The name of the core.
     */
    private String name;

    /**
     * The processor.
     */
    private Processor processor;

    /**
     * Create an abstract memory hierarchy core.
     *
     * @param processor the parent processor
     * @param num       the number of the core
     */
    public AbstractMemoryHierarchyCore(Processor processor, int num) {
        super(processor);

        this.num = num;
        this.name = "c" + this.num;

        this.processor = processor;
    }

    @Override
    public void doMeasurementOneCycle() {
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getNum() {
        return num;
    }

    @Override
    public Processor getProcessor() {
        return processor;
    }
}
