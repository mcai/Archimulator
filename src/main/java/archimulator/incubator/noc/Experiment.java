package archimulator.incubator.noc;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Experiment.
 *
 * @author Min Cai
 */
public class Experiment {
    private Config config;
    private Map<String, Object> stats;
    private Random random;

    public Experiment() {
        this.config = new Config();
        this.stats = new HashMap<>();
        this.random = this.config.getRandSeed() != -1 ? new Random(this.config.getRandSeed()) : new Random();
    }

    public Config getConfig() {
        return config;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public Random getRandom() {
        return random;
    }
}
