package archimulator.common;

import java.util.Random;

/**
 * NoC environment.
 *
 * @author Min Cai
 */
public interface NoCEnvironment {
    /**
     * Get the config.
     *
     * @return the config
     */
    NoCConfig getConfig();

    /**
     * Get the random object.
     *
     * @return the random object
     */
    Random getRandom();

    /**
     * Get a boolean value indicating whether it is currently in the detailed simulation mode or not.
     *
     * @return a boolean value indicating whether it is currently in the detailed simulation mode or not
     */
    boolean isInDetailedSimulationMode();
}
