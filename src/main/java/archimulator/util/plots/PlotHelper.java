package archimulator.util.plots;

import java.io.IOException;

/**
 * Plot helper.
 */
public class PlotHelper {
    /**
     * Generate the plot.
     *
     * @param csvFileName the CSV file name
     * @param plotFileName the plot file name
     * @param x x
     * @param hue hue
     * @param y y
     */
    public static void generatePlot(String csvFileName, String plotFileName, String x, String hue, String y) {
        try {
            ProcessBuilder pb;

            if(hue != null) {
                pb = new ProcessBuilder(
                        "tools/plots/plots.sh",
                        "--csv_file_name", csvFileName,
                        "--plot_file_name", plotFileName,
                        "--x", x,
                        "--hue", hue,
                        "--y", y
                ).inheritIO();
            }
            else {
                pb = new ProcessBuilder(
                        "tools/plots/plots.sh",
                        "--csv_file_name", csvFileName,
                        "--plot_file_name", plotFileName,
                        "--x", x,
                        "--y", y
                ).inheritIO();
            }

            pb.start().waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
