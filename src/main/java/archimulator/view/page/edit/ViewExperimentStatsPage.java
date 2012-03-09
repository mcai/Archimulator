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

import archimulator.model.experiment.profile.ExperimentProfile;
import archimulator.service.ArchimulatorService;
import archimulator.service.ArchimulatorServletContextListener;
import archimulator.util.Pair;
import archimulator.util.action.Action1;
import archimulator.view.ServerPush;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewExperimentStatsPage extends GenericForwardComposer<Window> {
    private Textbox textboxId;
    private Listbox listboxStats;

//    private Image imageStats;
//    private Image imageStats2;

    private Button buttonOk;
    private Button buttonCancel;

    private ExperimentProfile experimentProfile;

    private Window viewExperimentStats;

    public ViewExperimentStatsPage() {
    }

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);

        this.experimentProfile = (ExperimentProfile) arg.get("experimentProfile");

        this.textboxId.setValue(this.experimentProfile.getId() + "");

//        this.imageStats.setContent(ChartHelper.render("fig1", 300, 200)); //TODO
//        this.imageStats2.setContent(ChartHelper.render("fig2", 300, 200)); //TODO

        try {
            HttpSession httpSession = (HttpSession) session.getNativeSession();
            ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

            Map<String,Object> stats = archimulatorService.getExperimentStatsById(experimentProfile.getId());

            List<Pair<String, String>> statsList = new ArrayList<Pair<String, String>>();
            for(String key : stats.keySet()) {
                statsList.add(new Pair<String, String>(key, stats.get(key) + ""));
            }

            this.listboxStats.setModel(new ListModelList<Pair<String, String>>(statsList));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ServerPush.start(this.listboxStats, new Action1<Listbox>() {
            @Override
            public void apply(Listbox info) {
                try {
                    HttpSession httpSession = (HttpSession) session.getNativeSession();
                    ArchimulatorService archimulatorService = ArchimulatorServletContextListener.getArchimulatorService(httpSession.getServletContext());

                    Map<String,Object> stats = archimulatorService.getExperimentStatsById(experimentProfile.getId());

                    List<Pair<String, String>> statsList = new ArrayList<Pair<String, String>>();
                    for(String key : stats.keySet()) {
                        statsList.add(new Pair<String, String>(key, stats.get(key) + ""));
                    }

                    info.setModel(new ListModelList<Pair<String, String>>(statsList));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        this.viewExperimentStats.setTitle("Experiment Stats - Archimulator");
    }

    public void onOK() throws SQLException, InterruptedException {
        ServerPush.stop();
        Executions.sendRedirect("/experimentProfiles.zul");
    }

    public void onCancel() throws InterruptedException {
        ServerPush.stop();
        Executions.sendRedirect("/experimentProfiles.zul");
    }
}
