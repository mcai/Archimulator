package archimulator.sim.uncore.coherence.msi.event.cache;

public enum CacheControllerEventType {
    LOAD,
    STORE,
    REPLACEMENT,
    FWD_GETS,
    FWD_GETM,
    INV,
    RECALL,
    PUT_ACK,
    DATA_FROM_DIR_ACK_EQ_0,
    DATA_FROM_DIR_ACK_GT_0,
    DATA_FROM_OWNER,
    INV_ACK,
    LAST_INV_ACK
}
