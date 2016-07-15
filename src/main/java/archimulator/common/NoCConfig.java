package archimulator.common;

/**
 * NoC config.
 *
 * @author Min Cai
 */
public interface NoCConfig {
    /**
     * Get the random seed.
     *
     * @return the random seed
     */
    int getRandSeed();

    /**
     * Get the routing algorithm in the NoCs.
     *
     * @return the routing algorithm in the NoCs
     */
    String getRouting();

    /**
     * Get the selection policy in the NoCs.
     *
     * @return the selection policy in the NoCs
     */
    String getSelection();

    /**
     * Get the maximum size of the injection buffer.
     *
     * @return the maximum size of the injection buffer
     */
    int getMaxInjectionBufferSize();

    /**
     * Get the maximum size of the input buffer.
     *
     * @return the maximum size of the input buffer
     */
    int getMaxInputBufferSize();

    /**
     * Get the number of virtual channels.
     *
     * @return the number of virtual channels
     */
    int getNumVirtualChannels();

    /**
     * Get the link width.
     *
     * @return the link width
     */
    int getLinkWidth();

    /**
     * Get the link delay.
     *
     * @return the link delay
     */
    int getLinkDelay();

    /**
     * Get the ant packet traffic.
     *
     * @return the ant packet traffic
     */
    String getAntPacketTraffic();

    /**
     * Get the size of an ant packet.
     *
     * @return the size of an ant packet
     */
    int getAntPacketSize();

    /**
     * Get the ant packet injection rate.
     *
     * @return the ant packet injection rate
     */
    double getAntPacketInjectionRate();

    /**
     * Get the ACO selection alpha.
     *
     * @return the ACO selection alpha
     */
    double getAcoSelectionAlpha();

    /**
     * Get the reinforcement factor.
     *
     * @return the reinforcement factor
     */
    double getReinforcementFactor();
}
