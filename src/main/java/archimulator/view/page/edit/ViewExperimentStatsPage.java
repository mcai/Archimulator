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
import archimulator.view.ServerPush;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;

import java.sql.SQLException;

public class ViewExperimentStatsPage extends GenericForwardComposer<Window> {
    private Textbox textboxId;
    private Vbox vboxStats;

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

        ServerPush.start(this.vboxStats);

        this.viewExperimentStats.setTitle("实验统计 - Archimulator用户后台");
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
