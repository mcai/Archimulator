package archimulator.sim.uncore.coherence.msi.message;

public enum CoherenceMessageType {
    GETS,
    GETM,
    PUTS,
    PUTM_AND_DATA,

    FWD_GETS,
    FWD_GETM,
    INV,
    RECALL,

    PUT_ACK,
    DATA,
    INV_ACK,
    RECALL_ACK
}
