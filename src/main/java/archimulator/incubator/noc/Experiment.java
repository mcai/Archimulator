package archimulator.incubator.noc;

import java.util.HashMap;
import java.util.Map;

/**
 * Experiment.
 *
 * @author Min Cai
 */
public class Experiment {
    private Config config;
    private Map<String, Object> stats;

    public Experiment() {
        this.config = new Config();
        this.stats = new HashMap<>();
    }

    public Config getConfig() {
        return config;
    }

    public Map<String, Object> getStats() {
        return stats;
    }
}
