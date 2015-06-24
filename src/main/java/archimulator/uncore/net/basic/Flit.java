package archimulator.uncore.net.basic;

/**
 * Flit.
 *
 * @author Min Cai
 */
public class Flit {
    private long id;
    private int source = -1;
    private int destination = -1;
    private int direction;
    private boolean header;
    private boolean tail;
    private FlitState state = FlitState.INIT;
    private long timestamp;
    private long readyCycle;
    private boolean vcaBatch;
    private boolean swaBatch;


}
