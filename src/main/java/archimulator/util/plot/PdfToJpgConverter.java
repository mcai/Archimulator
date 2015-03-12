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

        convert("/home/itecgo/Archimulator.Boot/2.Research/0.proposals/2015/自然科学基金项目申请/archimulator_architecture_cn.pdf");
        convert("/home/itecgo/Archimulator.Boot/2.Research/0.proposals/2015/自然科学基金项目申请/archimulator_module_relationships_cn.pdf");
        convert("/home/itecgo/Archimulator.Boot/2.Research/0.proposals/2015/自然科学基金项目申请/cacheconfig_cn.pdf");
        convert("/home/itecgo/Archimulator.Boot/2.Research/0.proposals/2015/自然科学基金项目申请/ht_scheme_cn.pdf");
        convert("/home/itecgo/Archimulator.Boot/2.Research/0.proposals/2015/自然科学基金项目申请/landscape_partitioned_llc_ht_cn.pdf");
        convert("/home/itecgo/Archimulator.Boot/2.Research/0.proposals/2015/自然科学基金项目申请/prefetch_timeline_cn.pdf");
        convert("/home/itecgo/Archimulator.Boot/2.Research/0.proposals/2015/自然科学基金项目申请/technical_route.pdf");

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
