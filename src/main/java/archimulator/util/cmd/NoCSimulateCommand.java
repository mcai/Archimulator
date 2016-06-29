package archimulator.util.cmd;

import archimulator.uncore.net.noc.startup.Run;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = "noc_simulate", separators = "=")
public class NoCSimulateCommand {
    /**
     * Run the NoC simulate command.
     */
    public void run() {
        Run.main(new String[]{});
    }
}
