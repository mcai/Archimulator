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
package archimulator.view.page;

import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServiceImpl;
import archimulator.service.ArchimulatorServletContextListener;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ProcessorProfile;
import archimulator.sim.base.simulation.ContextConfig;
import archimulator.sim.base.simulation.SimulatedProgram;
import net.pickapack.DateHelper;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public class IndexPage extends GenericForwardComposer<Window> {
    Checkbox checkboxRunningExperimentEnabled;
    Textbox textboxChangePassword;

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);

        HttpSession httpSession = (HttpSession) session.getNativeSession();
        ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        this.checkboxRunningExperimentEnabled.setChecked(archimulatorService.isRunningExperimentEnabled());
    }

    public void onCheck$checkboxRunningExperimentEnabled(Event event) {
        HttpSession httpSession = (HttpSession) session.getNativeSession();
        ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());
        archimulatorService.setRunningExperimentEnabled(this.checkboxRunningExperimentEnabled.isChecked());
        Executions.sendRedirect(null);
    }

    public void onClick$buttonChangePassword(Event event) throws SQLException {
        String text = this.textboxChangePassword.getText();
        if (text != null && !text.isEmpty()) {
            HttpSession httpSession = (HttpSession) session.getNativeSession();
            ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());
            archimulatorService.setUserPassword(ArchimulatorServiceImpl.USER_ID_ADMIN, text);

            Messagebox.show("Password has been changed successfully！", "Change Password", Messagebox.OK, Messagebox.EXCLAMATION, new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                    switch ((Integer) event.getData()) {
                        case Messagebox.OK:
                            Executions.sendRedirect(null);
                            break;
                    }
                }
            });
        } else {
            Messagebox.show("New password cannot be empty!", "Change Password", Messagebox.OK, Messagebox.EXCLAMATION);
        }
    }

    public void onClick$buttonDownloadReport(Event event) {
        Filedownload.save(generateReport(), "text/plain", "archimulator_report_" + DateHelper.toFileNameString(new Date()) + ".txt");
    }

    public void onClick$buttonClearData(Event event) {
        Messagebox.show("Are you sure to clear data", "Clear Data", Messagebox.YES | Messagebox.NO, Messagebox.EXCLAMATION,
                new EventListener<Event>() {
                    public void onEvent(Event evt) throws SQLException {
                        switch ((Integer) evt.getData()) {
                            case Messagebox.YES:
                                clearData();
                                break;
                        }
                    }
                }
        );
    }

    private byte[] generateReport() {
        HttpSession httpSession = (HttpSession) session.getNativeSession();
        ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        StringBuilder sb = new StringBuilder();

        try {
            sb.append("Archimulator Report").append("\r\n");
            sb.append("-----------------------------------------------------------------------\r\n\r\n");

            java.util.List<SimulatedProgram> simulatedPrograms = archimulatorService.getSimulatedProgramsAsList();
            sb.append("a. Simulated Programs").append(" (total: ").append(simulatedPrograms.size()).append(")").append("\r\n");
            for (SimulatedProgram simulatedProgram : simulatedPrograms) {
                sb.append("  ").append(simulatedProgram).append("\r\n");
            }

            sb.append("\r\n");

            java.util.List<ProcessorProfile> processorProfiles = archimulatorService.getProcessorProfilesAsList();
            sb.append("b. Processor Profiles").append(" (total: ").append(processorProfiles.size()).append(")").append("\r\n");
            for (ProcessorProfile processorProfile : processorProfiles) {
                sb.append("  ").append(processorProfile).append("\r\n");
            }

            sb.append("\r\n");

            java.util.List<ExperimentProfile> experimentProfiles = archimulatorService.getExperimentProfilesAsList();
            sb.append("c. Experiment Profiles").append(" (total: ").append(experimentProfiles.size()).append(")").append("\r\n");
            for (ExperimentProfile experimentProfile : experimentProfiles) {
                sb.append("  ").append(experimentProfile).append("\r\n");

                sb.append("  ").append("  ").append("Context Configs").append("\r\n");

                for (ContextConfig contextConfig : experimentProfile.getContextConfigs()) {
                    sb.append("  ").append("  ").append("  ").append(contextConfig).append("\r\n");
                }

                sb.append("  ").append("  ").append("Stats").append("\r\n");

                Map<String, String> experimentStats = archimulatorService.getExperimentStatsById(experimentProfile.getId());
                for (String key : experimentStats.keySet()) {
                    sb.append("  ").append("  ").append("  ").append(key).append(": ").append(experimentStats.get(key)).append("\r\n");
                }
            }

            sb.append("\r\n");

            return sb.toString().getBytes();
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private void clearData() throws SQLException {
        HttpSession httpSession = (HttpSession) this.session.getNativeSession();
        ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext()).clearData();

        Executions.sendRedirect(null);
    }
}
