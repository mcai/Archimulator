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

import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ProcessorProfile;
import archimulator.sim.base.simulation.ContextConfig;
import archimulator.sim.base.simulation.SimulatedProgram;
import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServiceImpl;
import archimulator.service.ArchimulatorServletContextListener;
import archimulator.util.DateHelper;
import com.itextpdf.text.*;
import com.itextpdf.text.List;
import com.itextpdf.text.pdf.PdfWriter;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.*;

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
        if(text != null && !text.isEmpty()) {
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
        }
        else {
            Messagebox.show("New password cannot be empty!", "Change Password", Messagebox.OK, Messagebox.EXCLAMATION);
        }
    }
    
    public void onClick$buttonDownloadReport(Event event) {
        Filedownload.save(generateReport(), "application/pdf", "archimulator_report_" + DateHelper.toFileNameString(new Date()) + ".pdf");
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

        try {
            Document document = new Document(PageSize.LETTER.rotate());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.addTitle("Archimulator Report");
            document.addCreator("Archimulator");

            document.open();

            document.add(new Paragraph("Simulated Programs"));

            List listSimulatedPrograms = new List(List.ORDERED, 20);

            for(SimulatedProgram simulatedProgram : archimulatorService.getSimulatedProgramsAsList()) {
                listSimulatedPrograms.add(new ListItem(simulatedProgram + ""));
            }

            document.add(listSimulatedPrograms);

            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Processor Profiles"));

            List listProcessorProfiles = new List(List.ORDERED, 20);

            for(ProcessorProfile processorProfile : archimulatorService.getProcessorProfilesAsList()) {
                listProcessorProfiles.add(new ListItem(processorProfile + ""));
            }

            document.add(listProcessorProfiles);

            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Experiment Profiles"));

            List listExperimentProfiles = new List(List.ORDERED, 10);

            for(ExperimentProfile experimentProfile : archimulatorService.getExperimentProfilesAsList()) {
                listExperimentProfiles.add(new ListItem(experimentProfile + ""));

                List list1 = new List(List.UNORDERED, 10);

                list1.add(new ListItem("Context Configs"));

                List listContextConfigs = new List(List.UNORDERED, 10);

                for(ContextConfig contextConfig : experimentProfile.getContextConfigs()) {
                    listContextConfigs.add(new ListItem(contextConfig + ""));
                }

                list1.add(listContextConfigs);

                list1.add(new ListItem("Stats"));

                List listExperimentStats = new List(List.UNORDERED, 10);

                Map<String,Object> experimentStats = archimulatorService.getExperimentStatsById(experimentProfile.getId());
                for(String key : experimentStats.keySet()) {
                    listExperimentStats.add(new ListItem(key + ": " + experimentStats.get(key)));
                }

                list1.add(listExperimentStats);

                listExperimentProfiles.add(list1);
            }

            document.add(listExperimentProfiles);

            document.add(Chunk.NEWLINE);

            Anchor anchorArchimulatorWebsite = new Anchor("For more details, please visit: http://www.archimulator.com/.");
            anchorArchimulatorWebsite.setReference("http://www.archimulator.com/");
            document.add(anchorArchimulatorWebsite);

            document.close();
            out.close();
            return out.toByteArray();
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
