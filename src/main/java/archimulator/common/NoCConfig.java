package archimulator.common;

/**
 * NoC config.
 *
 * @author Min Cai
 */
public interface NoCConfig {
    int getRandSeed();

    String getRouting();

    String getSelection();

    int getMaxInjectionBufferSize();

    int getMaxInputBufferSize();

    int getNumVirtualChannels();

    int getLinkWidth();

    int getLinkDelay();

    int getAntPacketSize();

    double getAntPacketInjectionRate();

    double getAcoSelectionAlpha();

    double getReinforcementFactor();
}
