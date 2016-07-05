package archimulator.uncore.net.noc;

/**
 * Config.
 *
 * @author Min Cai
 */
public class Config {
    private boolean noDrain;

    private int randSeed;

    private String resultDir;

    private int numNodes;

    private long maxCycles;

    private long maxPackets;

    private String routing;

    private String selection;

    private String traffic;

    private int maxInjectionBufferSize;

    private int maxInputBufferSize;

    private int numVirtualChannels;

    private int linkWidth;
    private int linkDelay;

    private int dataPacketSize;
    private double dataPacketInjectionRate;

    private int antPacketSize;
    private double antPacketInjectionRate;

    private double acoSelectionAlpha;
    private double reinforcementFactor;

    public Config() {
        this.noDrain = true;

        this.randSeed = 13;

        this.resultDir = "results/";

        this.numNodes = 8 * 8;
        this.maxCycles = 20000;
        this.maxPackets = -1;
        this.routing = "odd_even";
        this.selection = "aco";
        this.traffic = "uniform";

        this.maxInjectionBufferSize = 32;
        this.maxInputBufferSize = 4;
        this.numVirtualChannels = 4;
        this.linkWidth = 4;
        this.linkDelay = 1;

        this.dataPacketSize = 16;
        this.dataPacketInjectionRate = 0.01;

        this.antPacketSize = 4;
        this.antPacketInjectionRate = 0.01;

        this.acoSelectionAlpha = 0.5;
        this.reinforcementFactor = 0.05;
    }

    public boolean isNoDrain() {
        return noDrain;
    }

    public void setNoDrain(boolean noDrain) {
        this.noDrain = noDrain;
    }

    public int getRandSeed() {
        return randSeed;
    }

    public void setRandSeed(int randSeed) {
        this.randSeed = randSeed;
    }

    public String getResultDir() {
        return resultDir;
    }

    public void setResultDir(String resultDir) {
        this.resultDir = resultDir;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }

    public long getMaxCycles() {
        return maxCycles;
    }

    public void setMaxCycles(long maxCycles) {
        this.maxCycles = maxCycles;
    }

    public long getMaxPackets() {
        return maxPackets;
    }

    public void setMaxPackets(long maxPackets) {
        this.maxPackets = maxPackets;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String getTraffic() {
        return traffic;
    }

    public void setTraffic(String traffic) {
        this.traffic = traffic;
    }

    public int getMaxInjectionBufferSize() {
        return maxInjectionBufferSize;
    }

    public void setMaxInjectionBufferSize(int maxInjectionBufferSize) {
        this.maxInjectionBufferSize = maxInjectionBufferSize;
    }

    public int getMaxInputBufferSize() {
        return maxInputBufferSize;
    }

    public void setMaxInputBufferSize(int maxInputBufferSize) {
        this.maxInputBufferSize = maxInputBufferSize;
    }

    public int getNumVirtualChannels() {
        return numVirtualChannels;
    }

    public void setNumVirtualChannels(int numVirtualChannels) {
        this.numVirtualChannels = numVirtualChannels;
    }

    public int getLinkWidth() {
        return linkWidth;
    }

    public void setLinkWidth(int linkWidth) {
        this.linkWidth = linkWidth;
    }

    public int getLinkDelay() {
        return linkDelay;
    }

    public void setLinkDelay(int linkDelay) {
        this.linkDelay = linkDelay;
    }

    public int getDataPacketSize() {
        return dataPacketSize;
    }

    public void setDataPacketSize(int dataPacketSize) {
        this.dataPacketSize = dataPacketSize;
    }

    public double getDataPacketInjectionRate() {
        return dataPacketInjectionRate;
    }

    public void setDataPacketInjectionRate(double dataPacketInjectionRate) {
        this.dataPacketInjectionRate = dataPacketInjectionRate;
    }

    public int getAntPacketSize() {
        return antPacketSize;
    }

    public void setAntPacketSize(int antPacketSize) {
        this.antPacketSize = antPacketSize;
    }

    public double getAntPacketInjectionRate() {
        return antPacketInjectionRate;
    }

    public void setAntPacketInjectionRate(double antPacketInjectionRate) {
        this.antPacketInjectionRate = antPacketInjectionRate;
    }

    public double getAcoSelectionAlpha() {
        return acoSelectionAlpha;
    }

    public void setAcoSelectionAlpha(double acoSelectionAlpha) {
        this.acoSelectionAlpha = acoSelectionAlpha;
    }

    public double getReinforcementFactor() {
        return reinforcementFactor;
    }

    public void setReinforcementFactor(double reinforcementFactor) {
        this.reinforcementFactor = reinforcementFactor;
    }

    public void dump() {
        System.out.println(String.format("  %s: %s", "noDrain", noDrain));
        System.out.println(String.format("  %s: %s", "randSeed", randSeed));
        System.out.println(String.format("  %s: %s", "resultDir", resultDir));
        System.out.println(String.format("  %s: %s", "numNodes", numNodes));
        System.out.println(String.format("  %s: %s", "maxCycles", maxCycles));
        System.out.println(String.format("  %s: %s", "maxPackets", maxPackets));
        System.out.println(String.format("  %s: %s", "routing", routing));
        System.out.println(String.format("  %s: %s", "selection", selection));
        System.out.println(String.format("  %s: %s", "traffic", traffic));
        System.out.println(String.format("  %s: %s", "maxInjectionBufferSize", maxInjectionBufferSize));
        System.out.println(String.format("  %s: %s", "maxInputBufferSize", maxInputBufferSize));
        System.out.println(String.format("  %s: %s", "numVirtualChannels", numVirtualChannels));
        System.out.println(String.format("  %s: %s", "linkWidth", linkWidth));
        System.out.println(String.format("  %s: %s", "linkDelay", linkDelay));
        System.out.println(String.format("  %s: %s", "dataPacketSize", dataPacketSize));
        System.out.println(String.format("  %s: %s", "dataPacketInjectionRate", dataPacketInjectionRate));
        System.out.println(String.format("  %s: %s", "antPacketSize", antPacketSize));
        System.out.println(String.format("  %s: %s", "antPacketInjectionRate", antPacketInjectionRate));
        System.out.println(String.format("  %s: %s", "acoSelectionAlpha", acoSelectionAlpha));
        System.out.println(String.format("  %s: %s", "reinforcementFactor", reinforcementFactor));
    }
}
