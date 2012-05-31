package archimulator.sim.uncore.coherence.msi.event.dir;

public enum DirectoryControllerEventType {
    GETS,
    GETM,
    REPLACEMENT,
    RECALL_ACK,
    LAST_RECALL_ACK,
    PUTS_NOT_LAST,
    PUTS_LAST,
    PUTM_AND_DATA_FROM_OWNER,
    PUTM_AND_DATA_FROM_NONOWNER,
    DATA
}
