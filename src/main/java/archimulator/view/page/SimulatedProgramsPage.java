package archimulator.view.page;

import archimulator.model.simulation.SimulatedProgram;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SimulatedProgramsPage extends GenericForwardComposer<Window> {
    private Button buttonAddSimulatedProgram;

    public void onClick$buttonAddSimulatedProgram(Event event) throws SQLException {
        Map<String, Object> arg = new HashMap<String, Object>();

        arg.put("create", true);
        arg.put("simulatedProgram", new SimulatedProgram("", "", ""));

        Window win = (Window) Executions.createComponents("/edit/editSimulatedProgram.zul", null, arg);
        win.doModal();
    }
}
