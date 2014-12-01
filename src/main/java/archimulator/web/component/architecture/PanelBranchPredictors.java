/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.web.component.architecture;

import archimulator.model.Architecture;
import archimulator.sim.core.bpred.BranchPredictorType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

import java.util.Arrays;

/**
 * Branch predictors panel.
 *
 * @author Min Cai
 */
public class PanelBranchPredictors extends Panel {
    /**
     * Create a branch predictors panel.
     *
     * @param id           the markup ID of the panel that is to be created
     * @param architecture the architecture
     */
    public PanelBranchPredictors(String id, final Architecture architecture) {
        super(id);

        setDefaultModel(new CompoundPropertyModel<>(architecture));

        final WebMarkupContainer divTwoBitBranchPredictor = new WebMarkupContainer("twoBitBranchPredictor") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getBranchPredictorType() == BranchPredictorType.TWO_BIT);

            add(new NumberTextField<Integer>("twoBitBranchPredictorBimodSize"));
            add(new NumberTextField<Integer>("twoBitBranchPredictorBranchTargetBufferNumSets"));
            add(new NumberTextField<Integer>("twoBitBranchPredictorBranchTargetBufferAssociativity"));
            add(new NumberTextField<Integer>("twoBitBranchPredictorReturnAddressStackSize"));
        }};
        add(divTwoBitBranchPredictor);

        final WebMarkupContainer divTwoLevelBranchPredictor = new WebMarkupContainer("twoLevelBranchPredictor") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getBranchPredictorType() == BranchPredictorType.TWO_LEVEL);

            add(new NumberTextField<Integer>("twoLevelBranchPredictorL1Size"));
            add(new NumberTextField<Integer>("twoLevelBranchPredictorL2Size"));
            add(new NumberTextField<Integer>("twoLevelBranchPredictorShiftWidth"));
            add(new CheckBox("twoLevelBranchPredictorXor"));
            add(new NumberTextField<Integer>("twoLevelBranchPredictorBranchTargetBufferNumSets"));
            add(new NumberTextField<Integer>("twoLevelBranchPredictorBranchTargetBufferAssociativity"));
            add(new NumberTextField<Integer>("twoLevelBranchPredictorReturnAddressStackSize"));
        }};
        add(divTwoLevelBranchPredictor);

        final WebMarkupContainer divCombinedBranchPredictor = new WebMarkupContainer("combinedBranchPredictor") {{
            setOutputMarkupPlaceholderTag(true);
            setVisible(architecture.getBranchPredictorType() == BranchPredictorType.COMBINED);

            add(new NumberTextField<Integer>("combinedBranchPredictorBimodSize"));
            add(new NumberTextField<Integer>("combinedBranchPredictorL1Size"));
            add(new NumberTextField<Integer>("combinedBranchPredictorL2Size"));
            add(new NumberTextField<Integer>("combinedBranchPredictorMetaSize"));
            add(new NumberTextField<Integer>("combinedBranchPredictorShiftWidth"));
            add(new CheckBox("combinedBranchPredictorXor"));
            add(new NumberTextField<Integer>("combinedBranchPredictorBranchTargetBufferNumSets"));
            add(new NumberTextField<Integer>("combinedBranchPredictorBranchTargetBufferAssociativity"));
            add(new NumberTextField<Integer>("combinedBranchPredictorReturnAddressStackSize"));
        }};
        add(divCombinedBranchPredictor);

        add(new DropDownChoice<BranchPredictorType>("branchPredictorType", Arrays.asList(BranchPredictorType.values())) {{
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
