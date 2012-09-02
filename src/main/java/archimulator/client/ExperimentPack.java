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
import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ExperimentPack implements Serializable {
    private String title;

    private ExperimentType experimentType;

    private ExperimentSpec baselineExperiment;

    private String variablePropertyName;
    private List<String> variablePropertyValues;

    private transient List<Experiment> experiments;

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

    public ExperimentSpec getBaselineExperiment() {
        if(baselineExperiment != null && baselineExperiment.getParent() == null) {
            baselineExperiment.setParent(this);
        }

        return baselineExperiment;
    }

    public void setBaselineExperiment(ExperimentSpec baselineExperiment) {
        this.baselineExperiment = baselineExperiment;
    }

    public String getVariablePropertyName() {
        return variablePropertyName;
    }

    public void setVariablePropertyName(String variablePropertyName) {
        this.variablePropertyName = variablePropertyName;
    }

    public List<String> getVariablePropertyValues() {
        return variablePropertyValues;
    }

    public void setVariablePropertyValues(List<String> variablePropertyValues) {
        this.variablePropertyValues = variablePropertyValues;
    }

    public List<ExperimentSpec> getExperimentSpecs() {
        try {
            List<ExperimentSpec> experiments = new ArrayList<ExperimentSpec>();

            if(variablePropertyName != null) {
                for(Object variablePropertyValue : variablePropertyValues) {
                    ExperimentSpec experimentSpec = (ExperimentSpec) BeanUtils.cloneBean(this.getBaselineExperiment());
                    BeanUtils.setProperty(experimentSpec, this.variablePropertyName, variablePropertyValue);
                    experiments.add(experimentSpec);
                }
            }
            else {
                experiments.add(this.getBaselineExperiment());
            }

            return experiments;
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

    public List<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<Experiment> experiments) {
        this.experiments = experiments;
    }
}
