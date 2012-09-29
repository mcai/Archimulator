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
package archimulator.web.data.view;

import archimulator.model.ExperimentPack;
import archimulator.model.ExperimentState;
import archimulator.service.ServiceManager;
import archimulator.web.pages.ExperimentPackPage;
import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.text.NumberFormat;

public class ExperimentPackDataView extends DataView<ExperimentPack> {
    private IRequestablePage page;

    public ExperimentPackDataView(IRequestablePage page, String id, IDataProvider<ExperimentPack> dataProvider) {
        super(id, dataProvider);
        this.page = page;
    }

    protected void populateItem(Item<ExperimentPack> item) {
        final ExperimentPack experimentPack = item.getModelObject();

        long numTotal = ServiceManager.getExperimentService().getNumExperimentsByExperimentPack(experimentPack);
        long numPending = ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.PENDING);
        long numReadyToRun = ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.READY_TO_RUN);
        long numRunning = ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.RUNNING);
        long numCompleted = ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.COMPLETED);
        long numAborted = ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.ABORTED);

        item.add(new Label("cell_title", String.format("{%d} %s", experimentPack.getId(), experimentPack.getTitle())));
        item.add(new Label("cell_description", String.format(
                "%s; total: %d, pending: %d (%s), readyToRun: %d (%s), running: %d (%s), completed: %d (%s), aborted: %d (%s)",
                experimentPack.getExperimentType(),
                numTotal,
                numPending,
                NumberFormat.getPercentInstance().format((double) numPending / numTotal),
                numReadyToRun,
                NumberFormat.getPercentInstance().format((double) numReadyToRun / numTotal),
                numRunning,
                NumberFormat.getPercentInstance().format((double) numRunning / numTotal),
                numCompleted,
                NumberFormat.getPercentInstance().format((double) numCompleted / numTotal),
                numAborted,
                NumberFormat.getPercentInstance().format((double) numAborted / numTotal)
        )));

        item.add(new WebMarkupContainer("cell_operations_1") {{
            add(new Link<Void>("button_start") {
                {
                    if (!(ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.PENDING) > 0)) {
                        add(new CssClassNameAppender("disabled"));
                        setEnabled(false);
                    }
                }

                @Override
                public void onClick() {
                    ServiceManager.getExperimentService().startExperimentPack(experimentPack);
                }
            });

            add(new Link<Void>("button_stop") {
                {
                    if (!(ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.READY_TO_RUN) > 0)) {
                        add(new CssClassNameAppender("disabled"));
                        setEnabled(false);
                    }
                }

                @Override
                public void onClick() {
                    ServiceManager.getExperimentService().stopExperimentPack(experimentPack);
                }
            });
        }});

        item.add(new WebMarkupContainer("cell_operations_2") {{
            add(new Link<Void>("button_reset_completed_experiments") {
                {
                    if (ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.COMPLETED) == 0) {
                        add(new CssClassNameAppender("disabled"));
                        setEnabled(false);
                    }
                }

                @Override
                public void onClick() {
                    ServiceManager.getExperimentService().resetCompletedExperimentsByExperimentPack(experimentPack);
                }
            });

            add(new Link<Void>("button_reset_aborted_experiments") {
                {
                    if (ServiceManager.getExperimentService().getNumExperimentsByExperimentPackAndState(experimentPack, ExperimentState.ABORTED) == 0) {
                        add(new CssClassNameAppender("disabled"));
                        setEnabled(false);
                    }
                }

                @Override
                public void onClick() {
                    ServiceManager.getExperimentService().resetAbortedExperimentsByExperimentPack(experimentPack);
                }
            });
        }});

        item.add(new WebMarkupContainer("cell_operations_3") {{
            add(new BookmarkablePageLink<Object>("button_edit", ExperimentPackPage.class, new PageParameters() {{
                set("action", "edit");
                set("experiment_pack_id", experimentPack.getId());
                set("back_page_id", page.getId());
            }}));

            add(new Link<Void>("button_remove") {
                @Override
                public void onClick() {
                    ServiceManager.getExperimentService().removeExperimentPackById(experimentPack.getId());
                }
            });
        }});
    }
}
