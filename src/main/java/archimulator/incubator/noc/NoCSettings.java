package archimulator.incubator.noc;

import java.util.Random;

/**
 * NoC Settings.
 *
 * @author Min Cai
 */
public interface NoCSettings {
    Config getConfig();

    Random getRandom();
}
