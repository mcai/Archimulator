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
package archimulator.web.pages;

import archimulator.model.Experiment;
import archimulator.model.ExperimentPack;
import archimulator.model.ExperimentPackVariable;
import archimulator.model.ExperimentType;
import archimulator.service.ServiceManager;
import archimulator.web.data.provider.ExperimentDataProvider;
import archimulator.web.data.view.ExperimentDataView;
import archimulator.web.data.view.ExperimentPackVariableListView;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import net.pickapack.dateTime.DateHelper;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.Arrays;

@MountPath(value = "/", alt = "/experiment_pack")
public class ExperimentPackPage extends AuthenticatedBasePage {
    public ExperimentPackPage(final PageParameters parameters) {
        super(parameters);

        final long experimentPackId = parameters.get("experiment_pack_id").toLong(-1);

        final ExperimentPack experimentPack = ServiceManager.getExperimentService().getExperimentPackById(experimentPackId);

        if (experimentPack == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        }

        setTitle((experimentPackId == -1 ? "Add" : "Edit") + " Experiment Pack - Archimulator");

        this.add(new Label("section_header_experiment_pack", String.format("%s Experiment Pack '{%d} %s'", "Edit", experimentPack.getId(), experimentPack.getTitle())));

        add(new Form("form_experiment_pack") {{
            add(new WebMarkupContainer("tab_general") {{
                add(new TextField<String>("input_id", Model.of(experimentPack.getId() + "")));
                add(new TextField<String>("input_title", Model.of(experimentPack.getTitle())));

                add(new DropDownChoice<ExperimentType>("select_type", new PropertyModel<ExperimentType>(experimentPack, "experimentType"), Arrays.asList(ExperimentType.values())));

                add(new TextField<String>("input_create_time", Model.of(DateHelper.toString(experimentPack.getCreateTime()))));
            }});

            add(new WebMarkupContainer("tab_page_baseline_experiment_spec") {{
                add(new TextField<String>("input_benchmarkTitle", Model.of(experimentPack.getBaselineExperimentSpec().getBenchmarkTitle())));
                add(new TextField<String>("input_benchmarkArguments", Model.of(experimentPack.getBaselineExperimentSpec().getBenchmarkArguments())));
                add(new TextField<String>("input_helperThreadLookahead", Model.of(experimentPack.getBaselineExperimentSpec().getHelperThreadLookahead() + "")));
                add(new TextField<String>("input_helperThreadStride", Model.of(experimentPack.getBaselineExperimentSpec().getHelperThreadStride() + "")));
                add(new TextField<String>("input_numCores", Model.of(experimentPack.getBaselineExperimentSpec().getNumCores() + "")));
                add(new TextField<String>("input_numThreadsPerCore", Model.of(experimentPack.getBaselineExperimentSpec().getNumThreadsPerCore() + "")));
                add(new TextField<String>("input_l1ISize", Model.of(experimentPack.getBaselineExperimentSpec().getL1ISize())));
                add(new TextField<String>("input_l1IAssociativity", Model.of(experimentPack.getBaselineExperimentSpec().getL1IAssociativity() + "")));
                add(new TextField<String>("input_l1DSize", Model.of(experimentPack.getBaselineExperimentSpec().getL1DSize())));
                add(new TextField<String>("input_l1DAssociativity", Model.of(experimentPack.getBaselineExperimentSpec().getL1DAssociativity() + "")));
                add(new TextField<String>("input_l2Size", Model.of(experimentPack.getBaselineExperimentSpec().getL2Size())));
                add(new TextField<String>("input_l2Associativity", Model.of(experimentPack.getBaselineExperimentSpec().getL2Associativity() + "")));
                add(new TextField<String>("input_l2ReplacementPolicyType", Model.of(experimentPack.getBaselineExperimentSpec().getL2ReplacementPolicyType())));
            }});

            add(new WebMarkupContainer("tab_page_variables") {{
                ListView<ExperimentPackVariable> rowExperiment = new ExperimentPackVariableListView(ExperimentPackPage.this, "row_variable", experimentPackId);

                WebMarkupContainer tableExperiments = new WebMarkupContainer("table_variables");
                add(tableExperiments);

                tableExperiments.add(rowExperiment);
            }});

            add(new WebMarkupContainer("tab_page_experiments") {{
                IDataProvider<Experiment> dataProvider = new ExperimentDataProvider(experimentPackId);

                final DataView<Experiment> rowExperiment = new ExperimentDataView(ExperimentPackPage.this, "row_experiment", dataProvider);
                rowExperiment.setItemsPerPage(10);

                final WebMarkupContainer tableExperiments = new WebMarkupContainer("table_experiments");
                add(tableExperiments);

                tableExperiments.add(rowExperiment);

                add(new BootstrapPagingNavigator("navigator_experiments", rowExperiment));
            }});

            add(new Button("button_cancel") {
                {
                    setDefaultFormProcessing(false);
                }

                @Override
                public void onSubmit() {
                    back(parameters, ExperimentPacksPage.class);
                }
            });
        }});
    }
}
