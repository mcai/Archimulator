package archimulator.uncore.net.noc.util;

/**
 * Function1.
 *
 * @author Min Cai
 */
public interface Function<T, K> {
    T apply(K arg);
}
