package archimulator.view.page;

import archimulator.model.experiment.profile.ProcessorProfile;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ProcessorProfilesPage extends GenericForwardComposer<Window> {
    private Button buttonAddProcessorProfile;

    public void onClick$buttonAddProcessorProfile(Event event) throws SQLException {
        Map<String, Object> arg = new HashMap<String, Object>();

        arg.put("create", true);
        arg.put("processorProfile", new ProcessorProfile(2, 2, 1024 * 1024 * 4, 8));

        Window win = (Window) Executions.createComponents("/edit/editProcessorProfile.zul", null, arg);
        win.doModal();
    }
}
