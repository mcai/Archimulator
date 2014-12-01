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
package archimulator.web.component.experiment;

import archimulator.model.Experiment;
import archimulator.sim.uncore.BasicMemoryHierarchy;
import net.pickapack.fsm.FiniteStateMachine;
import net.pickapack.fsm.FiniteStateMachineFactory;
import net.pickapack.fsm.StateTransitions;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

import java.util.*;

/**
 * Cache controller finite state machine factory panel.
 *
 * @author Min Cai
 */
public class PanelCacheControllerFiniteStateMachineFactory extends Panel {
    /**
     * Create a cache controller finite state machine factory panel.
     *
     * @param id                  the markup ID of the panel that is to be created
     * @param experiment          the experiment
     * @param cacheControllerName the cache controller name
     * @param fsmFactory          the finite state machine factory
     * @param conditionClz        the condition class
     * @param <ConditionT>        the type of the condition
     */
    public <ConditionT extends Enum<ConditionT>> PanelCacheControllerFiniteStateMachineFactory(String id, final Experiment experiment, final String cacheControllerName, FiniteStateMachineFactory<?, ConditionT, ?> fsmFactory, final Class<ConditionT> conditionClz) {
        super(id);

        Set<? extends Map.Entry<?, ? extends StateTransitions<?, ?, ? extends FiniteStateMachine<?, ?>>>> entries = fsmFactory.getTransitions().entrySet();

        add(new ListView<ConditionT>("event", new ArrayList<>(EnumSet.allOf(conditionClz))) {
            @Override
            protected void populateItem(ListItem<ConditionT> item) {
                ConditionT entry = item.getModelObject();

                item.setDefaultModel(new CompoundPropertyModel<>(entry));

                item.add(new Label("key", Model.of(entry)));
            }
        });

        add(new ListView<Map.Entry<?, ? extends StateTransitions<?, ?, ? extends FiniteStateMachine<?, ?>>>>("stat", new ArrayList<>(entries)) {
            @Override
            protected void populateItem(ListItem<Map.Entry<?, ? extends StateTransitions<?, ?, ? extends FiniteStateMachine<?, ?>>>> item) {
                final Map.Entry<?, ? extends StateTransitions<?, ?, ? extends FiniteStateMachine<?, ?>>> entry = item.getModelObject();

                item.setDefaultModel(new CompoundPropertyModel<Map.Entry<?, ? extends StateTransitions<?, ?, ? extends FiniteStateMachine<?, ?>>>>(entry));

                item.add(new Label("key"));

                Map<?, ? extends StateTransitions<?, ?, ? extends FiniteStateMachine<?, ?>>.StateTransition> perStateTransitions = entry.getValue().getPerStateTransitions();

                List<Object> transitions = new ArrayList<>();

                for (ConditionT event : EnumSet.allOf(conditionClz)) {
                    transitions.add(!perStateTransitions.containsKey(event) ? "" : perStateTransitions.get(event));
                }

                item.add(new ListView<Object>("transition", transitions) {
                    @Override
                    @SuppressWarnings("unchecked")
                    protected void populateItem(ListItem<Object> item) {
                        Object entryTransition = item.getModelObject();

                        item.setDefaultModel(new CompoundPropertyModel<>(entryTransition));

                        if (entryTransition instanceof StateTransitions.StateTransition) {
                            StateTransitions<?, ?, ? extends FiniteStateMachine<?, ?>>.StateTransition transition = (StateTransitions.StateTransition) entryTransition;

                            String key = BasicMemoryHierarchy.PREFIX_CC_FSM + cacheControllerName + "." + transition.getState() + "." + transition.getCondition();
                            String statValue = experiment.getStatValue(experiment.getMeasurementTitlePrefix(), key);

                            item.add(new Label("key", Model.of(transition.getNewState() + " [" + (statValue == null ? "0" : statValue) + "]")));
                        } else {
                            item.add(new Label("key", Model.of(entryTransition + ""))).add(new AttributeAppender("style", Model.of("background:#ee5f5b")).setSeparator(";"));
                        }
                    }
                });
            }
        });
    }
}
