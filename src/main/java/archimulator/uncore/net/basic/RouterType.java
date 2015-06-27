package archimulator.uncore.net.basic;

/**
 * Router type.
 *
 * @author Min Cai
 */
public enum RouterType {
    /**
     * Processor core.
     */
    CORE,

    /**
     * L2 cache controller.
     */
    L2_CONTROLLER,

    /**
     * Memory controller.
     */
    MEMORY_CONTROLLER,

    /**
     * Dummy.
     */
    DUMMY
}
