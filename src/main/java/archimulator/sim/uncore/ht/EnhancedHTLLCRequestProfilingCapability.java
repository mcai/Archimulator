package archimulator.sim.uncore.ht;

import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;

public class EnhancedHTLLCRequestProfilingCapability extends HTLLCRequestProfilingCapability {
    public EnhancedHTLLCRequestProfilingCapability(Simulation simulation) {
        super(simulation);
    }

    public EnhancedHTLLCRequestProfilingCapability(final DirectoryController llc) {
        super(llc);
    }
}
