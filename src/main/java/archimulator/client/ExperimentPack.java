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
package archimulator.client;

import archimulator.model.Experiment;
import archimulator.model.ExperimentType;
import net.pickapack.action.Function1;
import net.pickapack.util.CollectionHelper;
import net.pickapack.util.CombinationHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ExperimentPack implements Serializable {
    private String title;

    private ExperimentType experimentType;

    private ExperimentSpec baselineExperimentSpec;

    private List<ExperimentPackVariable> variables;

    private transient List<Experiment> experiments;

    private transient List<String> variablePropertyNames;
    private transient List<String> variablePropertyValues;

    public ExperimentPack(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public ExperimentType getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(ExperimentType experimentType) {
        this.experimentType = experimentType;
    }

    public ExperimentSpec getBaselineExperimentSpec() {
        if (baselineExperimentSpec != null && baselineExperimentSpec.getParent() == null) {
            baselineExperimentSpec.setParent(this);
        }

        return baselineExperimentSpec;
    }

    public void setBaselineExperimentSpec(ExperimentSpec baselineExperimentSpec) {
        this.baselineExperimentSpec = baselineExperimentSpec;
    }

    public List<String> getVariablePropertyNames() {
        if (variablePropertyNames == null && !CollectionUtils.isEmpty(this.variables)) {
            variablePropertyNames = CollectionHelper.transform(this.variables, new Function1<ExperimentPackVariable, String>() {
                @Override
                public String apply(ExperimentPackVariable variable) {
                    return variable.getName();
                }
            });
        }

        return variablePropertyNames;
    }

    public List<String> getVariablePropertyValues() {
        if (variablePropertyValues == null) {
            variablePropertyValues = CollectionHelper.transform(getCombinations(), new Function1<List<String>, String>() {
                @Override
                public String apply(List<String> combination) {
                    return StringUtils.join(combination, "_");
                }
            });
        }

        return variablePropertyValues;
    }

    public List<ExperimentPackVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<ExperimentPackVariable> variables) {
        this.variables = variables;
    }

    public List<ExperimentSpec> getExperimentSpecs() {
        try {
            List<ExperimentSpec> experimentSpecs = new ArrayList<ExperimentSpec>();

            if (!CollectionUtils.isEmpty(this.variables)) {
                for (List<String> combination : getCombinations()) {
                    ExperimentSpec experimentSpec = (ExperimentSpec) BeanUtils.cloneBean(this.getBaselineExperimentSpec());
                    int i = 0;
                    for (String value : combination) {
                        String name = this.getVariables().get(i++).getName();
                        BeanUtils.setProperty(experimentSpec, name, value);
                    }
                    experimentSpecs.add(experimentSpec);
                }
            } else {
                experimentSpecs.add(this.getBaselineExperimentSpec());
            }

            return experimentSpecs;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private List<List<String>> getCombinations() {
        return CombinationHelper.getCombinations(CollectionHelper.transform(this.variables, new Function1<ExperimentPackVariable, List<String>>() {
            @Override
            public List<String> apply(ExperimentPackVariable variable) {
                return variable.getValues();
            }
        }));
    }

    public List<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<Experiment> experiments) {
        this.experiments = experiments;
    }
}
