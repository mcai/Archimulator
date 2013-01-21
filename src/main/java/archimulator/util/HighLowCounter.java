package archimulator.util;

import net.pickapack.math.Quantizer;

/**
 * High/low counter.
 */
public class HighLowCounter {
    private Quantizer timestampQuantizer;

    private int lowCounter;
    private int highCounter;

    /**
     * Create a high/low counter.
     *
     * @param maxValue the maximum value
     * @param quantum the quantum
     */
    public HighLowCounter(int maxValue, int quantum) {
        this.timestampQuantizer = new Quantizer(maxValue, quantum);

        this.lowCounter = 0;
        this.highCounter = 1;
    }

    /**
     * Increment.
     */
    public void inc() {
        this.lowCounter++;
        if (this.lowCounter == this.timestampQuantizer.getQuantum()) {
            this.lowCounter = 0;
            this.highCounter++;
            if (this.highCounter > this.timestampQuantizer.getMaxValue()) {
                this.highCounter = 0;
            }
        }
    }

    /**
     * Get the timestamp quantizer.
     *
     * @return the timestamp quantizer
     */
    public Quantizer getTimestampQuantizer() {
        return timestampQuantizer;
    }

    /**
     * Get the low counter.
     *
     * @return the low counter
     */
    public int getLowCounter() {
        return lowCounter;
    }

    /**
     * Get the high counter.
     *
     * @return the high counter
     */
    public int getHighCounter() {
        return highCounter;
    }
}