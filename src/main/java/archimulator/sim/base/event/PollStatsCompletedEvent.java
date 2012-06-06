package archimulator.sim.base.event;

import archimulator.sim.base.simulation.Simulation;

import java.util.Map;

public class PollStatsCompletedEvent extends SimulationEvent {
    private Map<String, Object> stats;

    public PollStatsCompletedEvent(Simulation simulation, Map<String, Object> stats) {
        super(simulation);
        this.stats = stats;
    }

    public Map<String, Object> getStats() {
        return stats;
    }
}
