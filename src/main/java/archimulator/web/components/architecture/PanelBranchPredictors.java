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
package archimulator.web.components.architecture;

import archimulator.model.Architecture;
import archimulator.sim.core.bpred.BranchPredictorType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import java.util.Arrays;

public class PanelBranchPredictors extends Panel {
    public PanelBranchPredictors(String id, final Architecture architecture) {
        super(id);

        final WebMarkupContainer divTwoBitBranchPredictor = new WebMarkupContainer("div_twoBitBranchPredictor") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getBranchPredictorType() == BranchPredictorType.TWO_BIT);

            this.add(new NumberTextField<Integer>("input_twoBitBranchPredictorBimodSize", new PropertyModel<Integer>(architecture, "twoBitBranchPredictorBimodSize")));
            this.add(new NumberTextField<Integer>("input_twoBitBranchPredictorBranchTargetBufferNumSets", new PropertyModel<Integer>(architecture, "twoBitBranchPredictorBranchTargetBufferNumSets")));
            this.add(new NumberTextField<Integer>("input_twoBitBranchPredictorBranchTargetBufferAssociativity", new PropertyModel<Integer>(architecture, "twoBitBranchPredictorBranchTargetBufferAssociativity")));
            this.add(new NumberTextField<Integer>("input_twoBitBranchPredictorReturnAddressStackSize", new PropertyModel<Integer>(architecture, "twoBitBranchPredictorReturnAddressStackSize")));
        }};
        this.add(divTwoBitBranchPredictor);

        final WebMarkupContainer divTwoLevelBranchPredictor = new WebMarkupContainer("div_twoLevelBranchPredictor") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getBranchPredictorType() == BranchPredictorType.TWO_LEVEL);

            this.add(new NumberTextField<Integer>("input_twoLevelBranchPredictorL1Size", new PropertyModel<Integer>(architecture, "twoLevelBranchPredictorL1Size")));
            this.add(new NumberTextField<Integer>("input_twoLevelBranchPredictorL2Size", new PropertyModel<Integer>(architecture, "twoLevelBranchPredictorL2Size")));
            this.add(new NumberTextField<Integer>("input_twoLevelBranchPredictorShiftWidth", new PropertyModel<Integer>(architecture, "twoLevelBranchPredictorShiftWidth")));
            this.add(new CheckBox("input_twoLevelBranchPredictorXor", new PropertyModel<Boolean>(architecture, "twoLevelBranchPredictorXor")));
            this.add(new NumberTextField<Integer>("input_twoLevelBranchPredictorBranchTargetBufferNumSets", new PropertyModel<Integer>(architecture, "twoLevelBranchPredictorBranchTargetBufferNumSets")));
            this.add(new NumberTextField<Integer>("input_twoLevelBranchPredictorBranchTargetBufferAssociativity", new PropertyModel<Integer>(architecture, "twoLevelBranchPredictorBranchTargetBufferAssociativity")));
            this.add(new NumberTextField<Integer>("input_twoLevelBranchPredictorReturnAddressStackSize", new PropertyModel<Integer>(architecture, "twoLevelBranchPredictorReturnAddressStackSize")));
        }};
        this.add(divTwoLevelBranchPredictor);

        final WebMarkupContainer divCombinedBranchPredictor = new WebMarkupContainer("div_combinedBranchPredictor") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getBranchPredictorType() == BranchPredictorType.COMBINED);

            this.add(new NumberTextField<Integer>("input_combinedBranchPredictorBimodSize", new PropertyModel<Integer>(architecture, "combinedBranchPredictorBimodSize")));
            this.add(new NumberTextField<Integer>("input_combinedBranchPredictorL1Size", new PropertyModel<Integer>(architecture, "combinedBranchPredictorL1Size")));
            this.add(new NumberTextField<Integer>("input_combinedBranchPredictorL2Size", new PropertyModel<Integer>(architecture, "combinedBranchPredictorL2Size")));
            this.add(new NumberTextField<Integer>("input_combinedBranchPredictorMetaSize", new PropertyModel<Integer>(architecture, "combinedBranchPredictorMetaSize")));
            this.add(new NumberTextField<Integer>("input_combinedBranchPredictorShiftWidth", new PropertyModel<Integer>(architecture, "combinedBranchPredictorShiftWidth")));
            this.add(new CheckBox("input_combinedBranchPredictorXor", new PropertyModel<Boolean>(architecture, "combinedBranchPredictorXor")));
            this.add(new NumberTextField<Integer>("input_combinedBranchPredictorBranchTargetBufferNumSets", new PropertyModel<Integer>(architecture, "combinedBranchPredictorBranchTargetBufferNumSets")));
            this.add(new NumberTextField<Integer>("input_combinedBranchPredictorBranchTargetBufferAssociativity", new PropertyModel<Integer>(architecture, "combinedBranchPredictorBranchTargetBufferAssociativity")));
            this.add(new NumberTextField<Integer>("input_combinedBranchPredictorReturnAddressStackSize", new PropertyModel<Integer>(architecture, "combinedBranchPredictorReturnAddressStackSize")));
        }};
        this.add(divCombinedBranchPredictor);

        this.add(new DropDownChoice<BranchPredictorType>("select_branch_predictor_type", new PropertyModel<BranchPredictorType>(architecture, "branchPredictorType"), Arrays.asList(BranchPredictorType.values())) {{
            add(new AjaxFormComponentUpdatingBehavior("onchange") {
                protected void onUpdate(AjaxRequestTarget target) {
                    divTwoBitBranchPredictor.setVisible(architecture.getBranchPredictorType() == BranchPredictorType.TWO_BIT);
                    divTwoLevelBranchPredictor.setVisible(architecture.getBranchPredictorType() == BranchPredictorType.TWO_LEVEL);
                    divCombinedBranchPredictor.setVisible(architecture.getBranchPredictorType() == BranchPredictorType.COMBINED);

                    target.add(divTwoBitBranchPredictor);
                    target.add(divTwoLevelBranchPredictor);
                    target.add(divCombinedBranchPredictor);
                }
            });
        }});
    }
}
