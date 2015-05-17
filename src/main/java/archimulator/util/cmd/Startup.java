package archimulator.util.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Startup.
 *
 * @author Min Cai
 */
public class Startup {
    /**
     * Entry point.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        SimulateCommand simulateCommand = new SimulateCommand();

        JCommander commander = new JCommander();
        commander.addCommand(simulateCommand);

        try {
            commander.parse(args);

            if(commander.getParsedCommand() == null) {
                commander.usage();
            }
            else {
                switch (commander.getParsedCommand()) {
                    case "simulate":
                        String benchmarkTitle = simulateCommand.getBenchmarkTitle();
                        System.out.printf("simulating %s...\n", benchmarkTitle);
                        simulateCommand.run();
                        break;
                    default:
                        commander.usage();
                        break;
                }
            }
        } catch (ParameterException e) {
            commander.usage();
        }
    }
}
