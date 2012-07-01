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

import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServletContextListener;
import archimulator.sim.base.experiment.capability.ExperimentCapabilityFactory;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ExperimentProfileState;
import archimulator.sim.base.experiment.profile.ExperimentProfileType;
import archimulator.sim.base.experiment.profile.ProcessorProfile;
import archimulator.sim.base.simulation.ContextConfig;
import archimulator.sim.base.simulation.SimulatedProgram;
import net.pickapack.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EditExperimentProfilePage extends GenericForwardComposer<Window> {
    private Textbox textboxId;
    private Textbox textboxTitle;
    private Combobox comboboxProcessorProfiles;
    private Grid gridContextConfigs;
    private Radiogroup radioGroupExperimentProfileTypes;
    private Textbox textboxPthreadSpawnedIndex;
    private Textbox textboxMaxInsts;

    private Listbox listboxSimulationCapabilities;

    private ListModelList<Pair<Class<? extends SimulationCapability>, Boolean>> listModelSimulationCapabilities;

    private Label labelState;

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

        HttpSession httpSession = (HttpSession) this.session.getNativeSession();
        final ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        this.create = (Boolean) arg.get("create");

        this.experimentProfile = (ExperimentProfile) arg.get("experimentProfile");

        this.textboxId.setValue(this.experimentProfile.getId() + "");
        this.textboxTitle.setValue(this.experimentProfile.getTitle());

        this.comboboxProcessorProfiles.setModel(new ListModelList<ProcessorProfile>(archimulatorService.getProcessorProfilesAsList()));

        if (this.experimentProfile.getProcessorProfile() != null) {
            this.comboboxProcessorProfiles.setText(this.experimentProfile.getProcessorProfile() + "");
            this.populateContextConfigs(this.experimentProfile.getProcessorProfile(), this.experimentProfile.getContextConfigs());
        }

        for (Radio radio : this.radioGroupExperimentProfileTypes.getItems()) {
            if (radio.getLabel().equals(this.experimentProfile.getType() + "")) {
                this.radioGroupExperimentProfileTypes.setSelectedItem(radio);
                break;
            }
        }

        this.textboxPthreadSpawnedIndex.setValue(this.experimentProfile.getPthreadSpawnedIndex() + "");
        this.textboxMaxInsts.setValue(this.experimentProfile.getMaxInsts() + "");

        this.populateListSimulatonCapabilities();

        this.populateExperimentProfileTypes();

        this.labelState.setValue(this.experimentProfile.getState() + "");

        if (this.create) {
            this.textboxId.setValue("N/A");
        }

        this.winEditExperimentProfile.setTitle(this.create ? "Add Experiment Profile - Archimulator" : "Edit Experiment Profile - Archimulator");
    }

    private void populateListSimulatonCapabilities() {
        List<Pair<Class<? extends SimulationCapability>, Boolean>> listSimulationCapabilityClasses = new ArrayList<Pair<Class<? extends SimulationCapability>, Boolean>>();
        List<Class<? extends SimulationCapability>> allSimulationCapabilityClasses = ExperimentCapabilityFactory.getSimulationCapabilityClasses();
        List<Class<? extends SimulationCapability>> simulationCapabilityClasses = this.experimentProfile.getSimulationCapabilityClasses();
        for (Class<? extends SimulationCapability> clz : allSimulationCapabilityClasses) {
            listSimulationCapabilityClasses.add(new Pair<Class<? extends SimulationCapability>, Boolean>(clz, simulationCapabilityClasses.contains(clz)));
        }

        this.listModelSimulationCapabilities = new ListModelList<Pair<Class<? extends SimulationCapability>, Boolean>>(listSimulationCapabilityClasses);
        this.listboxSimulationCapabilities.setModel(this.listModelSimulationCapabilities);
    }

    public void onSelect$comboboxProcessorProfiles(SelectEvent event) throws SQLException {
        if (this.comboboxProcessorProfiles.getSelectedIndex() != -1) {
            ProcessorProfile processorProfile = this.comboboxProcessorProfiles.getSelectedItem().getValue();
            populateContextConfigs(processorProfile, null);
        }
    }

    public void onCheck$radioGroupExperimentProfileTypes(Event event) {
        populateExperimentProfileTypes();
    }

    public void onOK() throws SQLException {
        if (this.experimentProfile.getState() == ExperimentProfileState.RUNNING) {
            Messagebox.show("Cannot save changes because selected experiment profile is running!", "Edit Experiment Profile", Messagebox.OK, Messagebox.EXCLAMATION, new EventListener<Event>() {
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
            this.experimentProfile.setTitle(this.textboxTitle.getValue());
            this.experimentProfile.setPthreadSpawnedIndex(Integer.parseInt(this.textboxPthreadSpawnedIndex.getValue()));
            this.experimentProfile.setMaxInsts(Integer.parseInt(this.textboxMaxInsts.getValue()));

            if (this.comboboxProcessorProfiles.getSelectedIndex() != -1) {
                ProcessorProfile processorProfile = this.comboboxProcessorProfiles.getSelectedItem().getValue();
                this.experimentProfile.setProcessorProfile(processorProfile);

                this.experimentProfile.getContextConfigs().clear();

                int i = 0;

                for (Component component : this.gridContextConfigs.getRows().getChildren()) {
                    Row row = (Row) component;
                    Combobox comboboxSimulatedProgram = (Combobox) row.getChildren().get(1);

                    if (comboboxSimulatedProgram.getSelectedIndex() != -1) {
                        SimulatedProgram simulatedProgram = comboboxSimulatedProgram.getSelectedItem().getValue();
                        this.experimentProfile.getContextConfigs().add(new ContextConfig(simulatedProgram, i));
                    }

                    i++;
                }
            } else {
                Messagebox.show("Selected processor profile is empty, please select one and try again!", "Edit Experiment Profile", Messagebox.OK, Messagebox.EXCLAMATION, new EventListener<Event>() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        switch ((Integer) event.getData()) {
                            case Messagebox.OK:
                                Executions.sendRedirect(null);
                                break;
                        }
                    }
                });

                return;
            }

            this.experimentProfile.getSimulationCapabilityClasses().clear();
            for (Pair<Class<? extends SimulationCapability>, Boolean> entry : this.listModelSimulationCapabilities.getInnerList()) {
                if (entry.getSecond()) {
                    this.experimentProfile.getSimulationCapabilityClasses().add(entry.getFirst());
                }
            }

            HttpSession httpSession = (HttpSession) this.session.getNativeSession();
            final ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

            if (this.create) {
                archimulatorService.addExperimentProfile(this.experimentProfile);
            } else {
                archimulatorService.updateExperimentProfile(this.experimentProfile);
            }

            Executions.sendRedirect("/experimentProfiles.zul");
        }
    }

    public void onCancel() {
        Executions.sendRedirect("/experimentProfiles.zul");
    }

    private void populateContextConfigs(ProcessorProfile processorProfile, List<ContextConfig> contextConfigs) throws SQLException {
        HttpSession httpSession = (HttpSession) this.session.getNativeSession();
        final ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        this.gridContextConfigs.getRows().getChildren().clear();

        for (int i = 0; i < processorProfile.getNumCores() * processorProfile.getNumThreadsPerCore(); i++) {
            Row row = new Row();

            Label labelThreadId = new Label("Thread " + i + ": ");
            labelThreadId.setWidth("60px");
            row.appendChild(labelThreadId);

            Combobox comboboxSimulatedProgram = new Combobox();
            comboboxSimulatedProgram.setWidth("460px");
            comboboxSimulatedProgram.setModel(new ListModelList<SimulatedProgram>(archimulatorService.getSimulatedProgramsAsList()));
            row.appendChild(comboboxSimulatedProgram);

            if (contextConfigs != null) {
                for (ContextConfig contextConfig : contextConfigs) {
                    if (contextConfig.getThreadId() == i) {
                        if (contextConfig.getSimulatedProgram() != null) {
                            comboboxSimulatedProgram.setText(contextConfig.getSimulatedProgram() + "");
                        }
                        break;
                    }
                }
            }

            this.gridContextConfigs.getRows().appendChild(row);
        }
    }

    private void populateExperimentProfileTypes() {
        if (this.radioGroupExperimentProfileTypes.getSelectedIndex() != -1) {
            ExperimentProfileType experimentProfileType;

            String selectedLabel = this.radioGroupExperimentProfileTypes.getSelectedItem().getLabel();
            if (selectedLabel.equals("Functional Experiment")) {
                experimentProfileType = ExperimentProfileType.FUNCTIONAL_EXPERIMENT;
                this.textboxMaxInsts.setReadonly(true);
                this.textboxPthreadSpawnedIndex.setReadonly(true);
            } else if (selectedLabel.equals("Detailed Experiment")) {
                experimentProfileType = ExperimentProfileType.DETAILED_EXPERIMENT;
                this.textboxMaxInsts.setReadonly(true);
                this.textboxPthreadSpawnedIndex.setReadonly(true);
            } else if (selectedLabel.equals("Checkpointed Experiment")) {
                experimentProfileType = ExperimentProfileType.CHECKPOINTED_EXPERIMENT;
                this.textboxMaxInsts.setReadonly(false);
                this.textboxPthreadSpawnedIndex.setReadonly(false);
            } else {
                throw new IllegalArgumentException();
            }

            this.experimentProfile.setType(experimentProfileType);
        }
    }
}
