package archimulator.common;

import java.util.Random;

/**
 * NoC environment.
 */
public interface NoCEnvironment {
    NoCConfig getConfig();

    Random getRandom();

    boolean isInDetailedSimulationMode();
}
