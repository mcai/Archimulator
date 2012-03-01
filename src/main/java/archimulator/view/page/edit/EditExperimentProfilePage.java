package archimulator.view.page.edit;

import archimulator.model.experiment.profile.ExperimentProfile;
import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServletContextListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;

public class EditExperimentProfilePage extends GenericForwardComposer<Window> {
    private Textbox textboxId;
    private Textbox textboxPthreadSpawnedIndex;
    private Textbox textboxMaxInsts;

    private Button buttonOk;
    private Button buttonCancel;

    private boolean create;

    private ExperimentProfile experimentProfile;

    private Window winEditExperimentProfile;

    public EditExperimentProfilePage() {
    }

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);

        this.create = (Boolean) arg.get("create");

        this.experimentProfile = (ExperimentProfile) arg.get("experimentProfile");

        this.textboxId.setValue(this.experimentProfile.getId() + "");
        this.textboxPthreadSpawnedIndex.setValue(this.experimentProfile.getPthreadSpawnedIndex() + "");
        this.textboxMaxInsts.setValue(this.experimentProfile.getMaxInsts() + "");

        if (this.create) {
            this.textboxId.setValue("N/A");
        }

        this.winEditExperimentProfile.setTitle(this.create ? "添加实验 - Archimulator用户后台" : "编辑实验 - Archimulator用户后台");
    }

    public void onOK() throws SQLException {
        this.experimentProfile.setPthreadSpawnedIndex(Integer.parseInt(this.textboxPthreadSpawnedIndex.getValue()));
        this.experimentProfile.setMaxInsts(Integer.parseInt(this.textboxMaxInsts.getValue()));

        HttpSession httpSession = (HttpSession) this.session.getNativeSession();
        final ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        if (this.create) {
            archimulatorService.addExperimentProfile(this.experimentProfile);
        } else {
            archimulatorService.updateExperimentProfile(this.experimentProfile);
        }

        Executions.sendRedirect("/experimentProfiles.zul");
    }

    public void onCancel() {
        Executions.sendRedirect("/experimentProfiles.zul");
    }
}
