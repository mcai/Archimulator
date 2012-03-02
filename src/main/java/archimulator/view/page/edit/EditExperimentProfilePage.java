package archimulator.view.page.edit;

import archimulator.model.experiment.profile.ExperimentProfile;
import archimulator.model.experiment.profile.ProcessorProfile;
import archimulator.model.simulation.ContextConfig;
import archimulator.model.simulation.SimulatedProgram;
import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServletContextListener;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.List;

public class EditExperimentProfilePage extends GenericForwardComposer<Window> {
    private Textbox textboxId;
    private Combobox comboboxProcessorProfiles;
    private Grid gridContextConfigs;
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

        HttpSession httpSession = (HttpSession) this.session.getNativeSession();
        final ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        this.create = (Boolean) arg.get("create");

        this.experimentProfile = (ExperimentProfile) arg.get("experimentProfile");

        this.textboxId.setValue(this.experimentProfile.getId() + "");

        this.comboboxProcessorProfiles.setModel(new ListModelList<ProcessorProfile>(archimulatorService.getProcessorProfilesAsList()));

        if(this.experimentProfile.getProcessorProfile() != null) {
            this.comboboxProcessorProfiles.setText(this.experimentProfile.getProcessorProfile() + "");
            this.populateContextConfigs(this.experimentProfile.getProcessorProfile(), this.experimentProfile.getContextConfigs());
        }

        this.textboxPthreadSpawnedIndex.setValue(this.experimentProfile.getPthreadSpawnedIndex() + "");
        this.textboxMaxInsts.setValue(this.experimentProfile.getMaxInsts() + "");

        if (this.create) {
            this.textboxId.setValue("N/A");
        }

        this.winEditExperimentProfile.setTitle(this.create ? "添加实验 - Archimulator用户后台" : "编辑实验 - Archimulator用户后台");
    }
    
    public void onSelect$comboboxProcessorProfiles(SelectEvent event) throws SQLException {
        if(this.comboboxProcessorProfiles.getSelectedIndex() != -1) {
            ProcessorProfile processorProfile = this.comboboxProcessorProfiles.getSelectedItem().getValue();
            populateContextConfigs(processorProfile, null);
        }
    }

    public void onOK() throws SQLException {
        this.experimentProfile.setPthreadSpawnedIndex(Integer.parseInt(this.textboxPthreadSpawnedIndex.getValue()));
        this.experimentProfile.setMaxInsts(Integer.parseInt(this.textboxMaxInsts.getValue()));

        if(this.comboboxProcessorProfiles.getSelectedIndex() != -1) {
            ProcessorProfile processorProfile = this.comboboxProcessorProfiles.getSelectedItem().getValue();
            this.experimentProfile.setProcessorProfile(processorProfile);
            
            this.experimentProfile.getContextConfigs().clear();

            int i = 0;
            
            for(Component component : this.gridContextConfigs.getRows().getChildren()) {
                Row row = (Row) component;
                Combobox comboboxSimulatedProgram = (Combobox) row.getChildren().get(3);

                if(comboboxSimulatedProgram.getSelectedIndex() != -1) {
                    SimulatedProgram simulatedProgram = comboboxSimulatedProgram.getSelectedItem().getValue();
                    this.experimentProfile.getContextConfigs().add(new ContextConfig(simulatedProgram, i));
                }

                i++;
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

    public void onCancel() {
        Executions.sendRedirect("/experimentProfiles.zul");
    }

    private void populateContextConfigs(ProcessorProfile processorProfile, List<ContextConfig> contextConfigs) throws SQLException {
        HttpSession httpSession = (HttpSession) this.session.getNativeSession();
        final ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

        this.gridContextConfigs.getRows().getChildren().clear();

        for(int i = 0; i < processorProfile.getNumCores() * processorProfile.getNumThreadsPerCore(); i++) {
            Row row = new Row();

            Label labelThreadId = new Label("线程");
            labelThreadId.setWidth("50px");
            row.appendChild(labelThreadId);

            Textbox textboxThreadId = new Textbox();
            textboxThreadId.setWidth("80px");
            textboxThreadId.setText(i + "");
            textboxThreadId.setReadonly(true);
            row.appendChild(textboxThreadId);

            Label labelSimulatedProgram = new Label("负载");
            labelSimulatedProgram.setWidth("50px");
            row.appendChild(labelSimulatedProgram);

            Combobox comboboxSimulatedProgram = new Combobox();
            comboboxSimulatedProgram.setWidth("330px");
            comboboxSimulatedProgram.setModel(new ListModelList<SimulatedProgram>(archimulatorService.getSimulatedProgramsAsList()));
            row.appendChild(comboboxSimulatedProgram);

            if(contextConfigs != null) {
                for(ContextConfig contextConfig : contextConfigs) {
                    if(contextConfig.getThreadId() == i) {
                        if(contextConfig.getSimulatedProgram() != null) {
                            comboboxSimulatedProgram.setText(contextConfig.getSimulatedProgram() + "");
                        }
                        break;
                    }
                }
            }

            this.gridContextConfigs.getRows().appendChild(row);
        }
    }
}
