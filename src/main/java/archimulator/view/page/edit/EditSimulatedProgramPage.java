/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.view.page.edit;

import archimulator.sim.base.simulation.SimulatedProgram;
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
    private Textbox textboxTitle;
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
        this.textboxTitle.setValue(this.simulatedProgram.getTitle());
        this.textboxCwd.setValue(this.simulatedProgram.getCwd());
        this.textboxExe.setValue(this.simulatedProgram.getExe());
        this.textboxArgs.setValue(this.simulatedProgram.getArgs());
        this.textboxStdin.setValue(this.simulatedProgram.getStdin());

        if (this.create) {
            this.textboxId.setValue("N/A");
        }

        this.winEditSimulatedProgram.setTitle(this.create ? "Add Simulated Program - Archimulator" : "Edit Simulated Program - Archimulator");
    }

    public void onOK() throws SQLException {
        this.simulatedProgram.setTitle(this.textboxTitle.getValue());
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
