package archimulator.uncore.net.noc;

import java.util.Map;

/**
 * NoC stats.
 *
 * @author Min Cai
 */
public interface NoCStats {
    Map<String, Object> getStats();
}
