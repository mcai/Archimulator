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
package archimulator.web.component.experimentPack;

import archimulator.model.ExperimentPack;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * Input panel.
 *
 * @author Min Cai
 */
public class PanelInput extends Panel {
    /**
     * Create an input panel.
     *
     * @param id             the markup ID of the panel that is to be created
     * @param experimentPack the experiment pack
     */
    public PanelInput(String id, ExperimentPack experimentPack) {
        super(id);

        setDefaultModel(new CompoundPropertyModel<>(experimentPack));

        add(new Label("benchmarkTitle"));
        add(new Label("numMaxInstructions"));
        add(new Label("helperThreadLookahead"));
        add(new Label("helperThreadStride"));
        add(new Label("numMainThreadWaysInStaticPartitionedLRUPolicy"));
        add(new Label("numCores"));
        add(new Label("numThreadsPerCore"));
        add(new Label("l1ISize"));
        add(new Label("l1IAssociativity"));
        add(new Label("l1DSize"));
        add(new Label("l1DAssociativity"));
        add(new Label("l2Size"));
        add(new Label("l2Associativity"));
        add(new Label("l2ReplacementPolicyType"));
        add(new Label("dynamicSpeculativePrecomputationEnabled"));
    }
}