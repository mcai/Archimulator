package archimulator.util.plugin;

import archimulator.sim.common.Simulation;
import ro.fortsoft.pf4j.Extension;

/**
 * Sample simulation extension.
 *
 * @author Min Cai
 */
@Extension
public class SampleSimulationExtension implements SimulationExtensionPoint {
    public String getName() {
        return "Sample Simulation Extension";
    }

    @Override
    public void onInitialized(Simulation simulation) {
        System.out.printf("Simulation %s is initialized.%n", simulation.getTitle());
    }

    @Override
    public void onStarted(Simulation simulation) {
        System.out.printf("Simulation %s is started.%n", simulation.getTitle());
    }

    @Override
    public void onStopped(Simulation simulation) {
        System.out.printf("Simulation %s is stopped.%n", simulation.getTitle());
    }

    @Override
    public void onAborted(Simulation simulation) {
        System.out.printf("Simulation %s is aborted.%n", simulation.getTitle());
    }
}
