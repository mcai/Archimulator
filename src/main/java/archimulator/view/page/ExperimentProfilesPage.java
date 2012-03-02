package archimulator.view.page;

import archimulator.model.experiment.profile.ExperimentProfile;
import archimulator.model.experiment.profile.ProcessorProfile;
import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServletContextListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ExperimentProfilesPage extends GenericForwardComposer<Window> {
    private Button buttonAddExperimentProfile;

    public void onClick$buttonAddExperimentProfile(Event event) throws SQLException {
        HttpSession httpSession = (HttpSession) session.getNativeSession();
        ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        if(archimulatorService.getProcessorProfilesAsList().isEmpty()) {
            Messagebox.show("目标体系结构列表为空，请添加目标体系结构后重试！", "添加实验", Messagebox.OK, Messagebox.EXCLAMATION, new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                    switch ((Integer) event.getData()) {
                        case Messagebox.OK:
                            Executions.sendRedirect(null);
                            break;
                    }
                }
            });
        }
        else {
            ProcessorProfile processorProfile = archimulatorService.getProcessorProfilesAsList().get(0);

            Map<String, Object> arg = new HashMap<String, Object>();

            arg.put("create", true);
            ExperimentProfile experimentProfile = new ExperimentProfile(processorProfile);
            experimentProfile.setPthreadSpawnedIndex(3720);
            experimentProfile.setMaxInsts(2000000000);
            arg.put("experimentProfile", experimentProfile);

            Window win = (Window) Executions.createComponents("/edit/editExperimentProfile.zul", null, arg);
            win.doModal();
        }
    }
}
