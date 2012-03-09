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

import archimulator.model.experiment.profile.ProcessorProfile;
import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServletContextListener;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;

public class EditProcessorProfilePage extends GenericForwardComposer<Window> {
    private Textbox textboxId;
    private Textbox textboxNumCores;
    private Textbox textboxNumThreadsPerCore;
    private Textbox textboxL2Size;
    private Textbox textboxL2Associativity;

    private Button buttonOk;
    private Button buttonCancel;

    private boolean create;

    private ProcessorProfile processorProfile;

    private Window winEditProcessorProfile;

    public EditProcessorProfilePage() {
    }

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);

        this.create = (Boolean) arg.get("create");

        this.processorProfile = (ProcessorProfile) arg.get("processorProfile");

        this.textboxId.setValue(this.processorProfile.getId() + "");
        this.textboxNumCores.setValue(this.processorProfile.getNumCores() + "");
        this.textboxNumThreadsPerCore.setValue(this.processorProfile.getNumThreadsPerCore() + "");
        this.textboxL2Size.setValue(this.processorProfile.getL2Size() + "");
        this.textboxL2Associativity.setValue(this.processorProfile.getL2Associativity() + "");

        if (this.create) {
            this.textboxId.setValue("N/A");
        }

        this.winEditProcessorProfile.setTitle(this.create ? "Add Processor Profile - Archimulator" : "Edit Processor Profile - Archimulator");
    }

    public void onOK() throws SQLException {
        this.processorProfile.setNumCores(Integer.parseInt(this.textboxNumCores.getValue()));
        this.processorProfile.setNumThreadsPerCore(Integer.parseInt(this.textboxNumThreadsPerCore.getValue()));
        this.processorProfile.setL2Size(Integer.parseInt(this.textboxL2Size.getValue()));
        this.processorProfile.setL2Associativity(Integer.parseInt(this.textboxL2Associativity.getValue()));

        HttpSession httpSession = (HttpSession) this.session.getNativeSession();
        final ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        if (this.create) {
            archimulatorService.addProcessorProfile(this.processorProfile);
        } else {
            archimulatorService.updateProcessorProfile(this.processorProfile);
        }

        Executions.sendRedirect("/processorProfiles.zul");
    }

    public void onCancel() {
        Executions.sendRedirect("/processorProfiles.zul");
    }
}
