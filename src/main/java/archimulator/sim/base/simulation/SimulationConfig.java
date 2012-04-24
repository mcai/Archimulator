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
package archimulator.sim.base.simulation;

import archimulator.sim.core.ProcessorConfig;

import java.io.File;
import java.util.List;

public class SimulationConfig {
    private String title;
    private ProcessorConfig processorConfig;
    private List<ContextConfig> contextConfigs;

    public SimulationConfig(String title, ProcessorConfig processorConfig, List<ContextConfig> contextConfigs) {
        this.title = title;
        this.processorConfig = processorConfig;
        this.contextConfigs = contextConfigs;
    }

    public String getTitle() {
        return title;
    }

    public String getCwd() {
        return "experiments" + File.separator + this.title;
    }

    public ProcessorConfig getProcessorConfig() {
        return processorConfig;
    }

    public List<ContextConfig> getContextConfigs() {
        return contextConfigs;
    }
}
