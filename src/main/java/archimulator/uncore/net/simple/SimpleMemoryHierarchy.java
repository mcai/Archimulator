package archimulator.uncore.net.simple;

import archimulator.common.Experiment;
import archimulator.common.Simulation;
import archimulator.common.SimulationEvent;
import archimulator.common.SimulationType;
import archimulator.uncore.AbstractMemoryHierarchy;
import archimulator.uncore.MemoryDevice;
import archimulator.uncore.cache.MemoryDeviceType;
import archimulator.uncore.net.Net;
import archimulator.uncore.net.visualization.NetVisualizer;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;

import java.util.Arrays;

public class SimpleMemoryHierarchy extends AbstractMemoryHierarchy {
    private L1SToL2Net l1sToL2Net;
    private L2ToMemNet l2ToMemNet;

    /**
     * Create a simple memory hierarchy.
     *
     * @param experiment              the experiment
     * @param simulation              the simulation
     * @param blockingEventDispatcher the blocking event dispatcher
     * @param cycleAccurateEventQueue the cycle accurate event queue
     */
    public SimpleMemoryHierarchy(Experiment experiment, Simulation simulation, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        super(experiment, simulation, blockingEventDispatcher, cycleAccurateEventQueue);

        this.l1sToL2Net = new L1SToL2Net(this);
        this.l2ToMemNet = new L2ToMemNet(this);

        if(this.getSimulation().getType() == SimulationType.MEASUREMENT) {
            NetVisualizer.run(Arrays.asList(this.l1sToL2Net, this.l2ToMemNet));
        }
    }

    @Override
    public Net getNet(MemoryDevice from, MemoryDevice to) {
        switch (from.getType()) {
            case L1I_CONTROLLER:
            case L1D_CONTROLLER:
                return this.l1sToL2Net;
            case L2_CONTROLLER:
                return to.getType() == MemoryDeviceType.MEMORY_CONTROLLER ? this.l2ToMemNet : this.l1sToL2Net;
            case MEMORY_CONTROLLER:
                return this.l2ToMemNet;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Get the net for the L1 cache controllers to the L2 cache controller.
     *
     * @return the net for the L1 cache controllers to the L2 cache controller.
     */
    public Net getL1sToL2Net() {
        return l1sToL2Net;
    }

    /**
     * Get the net for the L2 cache controller to the memory controller.
     *
     * @return the net for the L2 cache controller to the memory controller.
     */
    public L2ToMemNet getL2ToMemNet() {
        return l2ToMemNet;
    }
}
