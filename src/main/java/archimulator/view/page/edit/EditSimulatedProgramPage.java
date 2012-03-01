package archimulator.view.page.edit;

import archimulator.model.simulation.SimulatedProgram;
import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServletContextListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;

public class EditSimulatedProgramPage extends GenericForwardComposer<Window> {
    private Textbox textboxId;
    private Textbox textboxCwd;
    private Textbox textboxExe;
    private Textbox textboxArgs;
    private Textbox textboxStdin;

    private Button buttonOk;
    private Button buttonCancel;

    private boolean create;

    private SimulatedProgram simulatedProgram;

    private Window winEditSimulatedProgram;

    public EditSimulatedProgramPage() {
    }

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);

        this.create = (Boolean) arg.get("create");

        this.simulatedProgram = (SimulatedProgram) arg.get("simulatedProgram");

        this.textboxId.setValue(this.simulatedProgram.getId() + "");
        this.textboxCwd.setValue(this.simulatedProgram.getCwd());
        this.textboxExe.setValue(this.simulatedProgram.getExe());
        this.textboxArgs.setValue(this.simulatedProgram.getArgs());
        this.textboxStdin.setValue(this.simulatedProgram.getStdin());

        if (this.create) {
            this.textboxId.setValue("N/A");
        }

        this.winEditSimulatedProgram.setTitle(this.create ? "添加评测程序 - Archimulator用户后台" : "编辑评测程序 - Archimulator用户后台");
    }

    public void onOK() throws SQLException {
        this.simulatedProgram.setCwd(this.textboxCwd.getValue());
        this.simulatedProgram.setExe(this.textboxExe.getValue());
        this.simulatedProgram.setArgs(this.textboxArgs.getValue());
        this.simulatedProgram.setStdin(this.textboxStdin.getValue());

        HttpSession httpSession = (HttpSession) this.session.getNativeSession();
        final ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        if (this.create) {
            archimulatorService.addSimulatedProgram(this.simulatedProgram);
        } else {
            archimulatorService.updateSimulatedProgram(this.simulatedProgram);
        }

        Executions.sendRedirect("/simulatedPrograms.zul");
    }

    public void onCancel() {
        Executions.sendRedirect("/simulatedPrograms.zul");
    }
}
