package archimulator.uncore.noc;

import archimulator.common.NoCConfig;

/**
 * NoC experiment config.
 *
 * @author Min Cai
 */
public class NoCExperimentConfig implements NoCConfig {
    private int randSeed;

    private String routing;

    private String selection;

    private int maxInjectionBufferSize;

    private int maxInputBufferSize;

    private int numVirtualChannels;

    private int linkWidth;
    private int linkDelay;

    private String antPacketTraffic;
    private int antPacketSize;
    private double antPacketInjectionRate;

    private double acoSelectionAlpha;
    private double reinforcementFactor;

    private String dataPacketTraffic;
    private double dataPacketInjectionRate;
    private int dataPacketSize;

    /**
     * Create a NoC experiment config.
     */
    public NoCExperimentConfig() {
        this.randSeed = 13;
        this.routing = "oddEven";
        this.selection = "aco";
        this.maxInjectionBufferSize = 32;
        this.maxInputBufferSize = 4;
        this.numVirtualChannels = 4;
        this.linkWidth = 4;
        this.linkDelay = 1;
        this.antPacketTraffic = "uniform";
        this.antPacketSize = 4;
        this.antPacketInjectionRate = 0.01;
        this.acoSelectionAlpha = 0.5;
        this.reinforcementFactor = 0.05;

        this.dataPacketTraffic = "uniform";
        this.dataPacketInjectionRate = 0.01;
        this.dataPacketSize = 16;
    }

    /**
     * Get the random seed.
     *
     * @return the random seed
     */
    @Override
    public int getRandSeed() {
        return randSeed;
    }

    /**
     * Set the random seed.
     *
     * @param randSeed the random seed
     */
    public void setRandSeed(int randSeed) {
        this.randSeed = randSeed;
    }

    /**
     * Get the routing algorithm in the NoCs.
     *
     * @return the routing algorithm in the NoCs
     */
    @Override
    public String getRouting() {
        return routing;
    }

    /**
     * Set the routing algorithm in the NoCs.
     *
     * @param routing the routing algorithm in the NoCs
     */
    public void setRouting(String routing) {
        this.routing = routing;
    }

    /**
     * Get the selection policy in the NoCs.
     *
     * @return the selection policy in the NoCs
     */
    @Override
    public String getSelection() {
        return selection;
    }

    /**
     * Set the selection policy in the NoCs.
     *
     * @param selection the selection policy in the NoCs
     */
    public void setSelection(String selection) {
        this.selection = selection;
    }

    /**
     * Get the maximum size of the injection buffer.
     *
     * @return the maximum size of the injection buffer
     */
    @Override
    public int getMaxInjectionBufferSize() {
        return maxInjectionBufferSize;
    }

    /**
     * Set the maximum size of the injection buffer.
     *
     * @param maxInjectionBufferSize the maximum size of the injection buffer
     */
    public void setMaxInjectionBufferSize(int maxInjectionBufferSize) {
        this.maxInjectionBufferSize = maxInjectionBufferSize;
    }

    /**
     * Get the maximum size of the input buffer.
     *
     * @return the maximum size of the input buffer
     */
    @Override
    public int getMaxInputBufferSize() {
        return maxInputBufferSize;
    }

    /**
     * Set the maximum size of the input buffer.
     *
     * @param maxInputBufferSize the maximum size of the input buffer
     */
    public void setMaxInputBufferSize(int maxInputBufferSize) {
        this.maxInputBufferSize = maxInputBufferSize;
    }

    /**
     * Get the number of virtual channels.
     *
     * @return the number of virtual channels
     */
    @Override
    public int getNumVirtualChannels() {
        return numVirtualChannels;
    }

    /**
     * Set the number of virtual channels.
     *
     * @param numVirtualChannels the number of virtual channels
     */
    public void setNumVirtualChannels(int numVirtualChannels) {
        this.numVirtualChannels = numVirtualChannels;
    }

    /**
     * Get the link width.
     *
     * @return the link width
     */
    @Override
    public int getLinkWidth() {
        return linkWidth;
    }

    /**
     * Set the link width.
     *
     * @param linkWidth the link width
     */
    public void setLinkWidth(int linkWidth) {
        this.linkWidth = linkWidth;
    }

    /**
     * Get the link delay.
     *
     * @return the link delay
     */
    @Override
    public int getLinkDelay() {
        return linkDelay;
    }

    /**
     * Set the link delay.
     *
     * @param linkDelay the link delay
     */
    public void setLinkDelay(int linkDelay) {
        this.linkDelay = linkDelay;
    }

    /**
     * Get the ant packet traffic.
     *
     * @return the ant packet traffic
     */
    @Override
    public String getAntPacketTraffic() {
        return antPacketTraffic;
    }

    /**
     * Set the ant packet traffic.
     *
     * @param antPacketTraffic the ant packet traffic
     */
    public void setAntPacketTraffic(String antPacketTraffic) {
        this.antPacketTraffic = antPacketTraffic;
    }

    /**
     * Get the size of an ant packet.
     *
     * @return the size of an ant packet
     */
    @Override
    public int getAntPacketSize() {
        return antPacketSize;
    }

    /**
     * Set the size of an ant packet.
     *
     * @param antPacketSize the size of an ant packet
     */
    public void setAntPacketSize(int antPacketSize) {
        this.antPacketSize = antPacketSize;
    }

    /**
     * Get the ant packet injection rate.
     *
     * @return the ant packet injection rate
     */
    @Override
    public double getAntPacketInjectionRate() {
        return antPacketInjectionRate;
    }

    /**
     * Set the ant packet injection rate.
     *
     * @param antPacketInjectionRate the ant packet injection rate
     */
    public void setAntPacketInjectionRate(double antPacketInjectionRate) {
        this.antPacketInjectionRate = antPacketInjectionRate;
    }

    /**
     * Get the ACO selection alpha.
     *
     * @return the ACO selection alpha
     */
    @Override
    public double getAcoSelectionAlpha() {
        return acoSelectionAlpha;
    }

    /**
     * Set the ACO selection alpha.
     *
     * @param acoSelectionAlpha the ACO selection alpha
     */
    public void setAcoSelectionAlpha(double acoSelectionAlpha) {
        this.acoSelectionAlpha = acoSelectionAlpha;
    }

    /**
     * Get the reinforcement factor.
     *
     * @return the reinforcement factor
     */
    @Override
    public double getReinforcementFactor() {
        return reinforcementFactor;
    }

    /**
     * Set the reinforcement factor.
     *
     * @param reinforcementFactor the reinforcement factor
     */
    public void setReinforcementFactor(double reinforcementFactor) {
        this.reinforcementFactor = reinforcementFactor;
    }

    /**
     * Get the data packet traffic.
     *
     * @return the data packet traffic
     */
    public String getDataPacketTraffic() {
        return dataPacketTraffic;
    }

    /**
     * Set the data packet traffic.
     *
     * @param dataPacketTraffic the data packet traffic
     */
    public void setDataPacketTraffic(String dataPacketTraffic) {
        this.dataPacketTraffic = dataPacketTraffic;
    }

    /**
     * Get the data packet injection rate.
     *
     * @return the data packet injection rate
     */
    public double getDataPacketInjectionRate() {
        return dataPacketInjectionRate;
    }

    /**
     * Set the data packet injection rate.
     *
     * @param dataPacketInjectionRate the data packet injection rate
     */
    public void setDataPacketInjectionRate(double dataPacketInjectionRate) {
        this.dataPacketInjectionRate = dataPacketInjectionRate;
    }

    /**
     * Get the size of a data packet
     *
     * @return the size of a data packet
     */
    public int getDataPacketSize() {
        return dataPacketSize;
    }

    /**
     * Set the size of a data packet.
     *
     * @param dataPacketSize the size of a data packet
     */
    public void setDataPacketSize(int dataPacketSize) {
        this.dataPacketSize = dataPacketSize;
    }
}
