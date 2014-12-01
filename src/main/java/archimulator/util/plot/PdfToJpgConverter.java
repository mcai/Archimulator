package archimulator.util.plot;

import net.pickapack.io.cmd.CommandLineHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * PDF to JPG converter.
 *
 * @author Min Cai
 */
public class PdfToJpgConverter {
    /**
     * Entry point.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        convert("/home/itecgo/Archimulator.Ext/doc/ht_scheme_cn.pdf");
        convert("/home/itecgo/Archimulator.Ext/doc/cacheconfig_cn.pdf");
        convert("/home/itecgo/Archimulator.Ext/doc/prefetch_timeline_cn.pdf");

        List<String> benchmarks = new ArrayList<String>() {{
            add("mst");
            add("em3d");
            add("429.mcf");
        }};

        List<String> items = new ArrayList<String>() {{
            add("helperThreadedL2Size");
            add("helperThreadedLookahead");
            add("helperThreadedL2Replacement");
        }};

        for (String benchmark : benchmarks) {
            for (String item : items) {
                convert("/home/itecgo/Archimulator.Ext/experiment_plots/" + benchmark + "/" + item + "/speedup.eps");

                convert("/home/itecgo/Archimulator.Ext/experiment_plots/" + benchmark + "/" + item + "/helperThreadPrefetchCoverage.eps");
                convert("/home/itecgo/Archimulator.Ext/experiment_plots/" + benchmark + "/" + item + "/helperThreadPrefetchAccuracy.eps");
                convert("/home/itecgo/Archimulator.Ext/experiment_plots/" + benchmark + "/" + item + "/helperThreadPrefetchLateness.eps");
                convert("/home/itecgo/Archimulator.Ext/experiment_plots/" + benchmark + "/" + item + "/helperThreadPrefetchPollution.eps");
            }
        }
    }

    /**
     * Convert the file into JPEG format.
     *
     * @param fileName the format
     */
    public static void convert(String fileName) {
        CommandLineHelper.invokeShellCommand("convert -quality 100 -density 300x300 -colorspace Gray " + fileName + " " + fileName + ".jpg");
    }
}
