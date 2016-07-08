package archimulator.uncore.net.noc;

import java.util.Random;

/**
 * NoC Settings.
 *
 * @author Min Cai
 */
public interface NoCSettings {
    NoCConfig getConfig();

    Random getRandom();
}
