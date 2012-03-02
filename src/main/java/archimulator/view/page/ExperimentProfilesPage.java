package archimulator.view.page;

import archimulator.model.experiment.profile.ExperimentProfile;
import archimulator.model.experiment.profile.ProcessorProfile;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ExperimentProfilesPage extends GenericForwardComposer<Window> {
    private Button buttonAddExperimentProfile;

    public void onClick$buttonAddExperimentProfile(Event event) throws SQLException {
        Map<String, Object> arg = new HashMap<String, Object>();

        arg.put("create", true);
        ExperimentProfile experimentProfile = new ExperimentProfile(new ProcessorProfile(2, 2, 1024 * 1024 * 4, 8));
        arg.put("experimentProfile", experimentProfile); //TODO: should set null or load from db

        Window win = (Window) Executions.createComponents("/edit/editExperimentProfile.zul", null, arg);
        win.doModal();
    }
}
