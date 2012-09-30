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
package archimulator.web.components.experimentPack;

import archimulator.model.ExperimentPack;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class PanelBaseline extends Panel {
    public PanelBaseline(String id, ExperimentPack experimentPack) {
        super(id);

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
    }
}
