package archimulator.sim.base.event;

import net.pickapack.event.BlockingEvent;

import java.util.Map;

public class PollStatsEvent implements BlockingEvent {
    private Map<String, Object> stats;

    public PollStatsEvent(Map<String, Object> stats) {
        this.stats = stats;
    }

    public Map<String, Object> getStats() {
        return stats;
    }
}
