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
package archimulator.model;

import archimulator.service.ServiceManager;
import archimulator.util.serialization.ExperimentPackVariableArrayListJsonSerializableType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.action.Function1;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;
import net.pickapack.util.CollectionHelper;
import net.pickapack.util.CombinationHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Experiment pack.
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "ExperimentPack")
public class ExperimentPack implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private ExperimentType experimentType;

    @DatabaseField(persisterClass = ExperimentPackVariableArrayListJsonSerializableType.class)
    private ArrayList<ExperimentPackVariable> variables;

    private transient ExperimentSpec baselineExperimentSpec;

    /**
     * Create an experiment pack.
     */
    public ExperimentPack() {
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     * Get the experiment pack's ID.
     *
     * @return the experiment pack's ID
     */
    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getParentId() {
        return -1;
    }

    /**
     * Get the experiment pack's title.
     *
     * @return the experiment pack's title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Set the experiment pack's title.
     *
     * @param title the experiment pack's title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the time in ticks when the experiment pack is created.
     *
     * @return the time in ticks when the experiment pack is created
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get the string representation of the time when the experiment pack is created.
     *
     * @return the string representation of the time when the experiment pack is created
     */
    public String getCreateTimeAsString() {
        return DateHelper.toString(createTime);
    }

    /**
     * Get the experiment type.
     *
     * @return the experiment type
     */
    public ExperimentType getExperimentType() {
        return experimentType;
    }

    /**
     * Set the experiment type.
     *
     * @param experimentType the experiment type
     */
    public void setExperimentType(ExperimentType experimentType) {
        this.experimentType = experimentType;
    }

    /**
     * Get the baseline experiment specification.
     *
     * @return the baseline experiment specification
     */
    public ExperimentSpec getBaselineExperimentSpec() {
        if(baselineExperimentSpec == null) {
            return ServiceManager.getExperimentService().getExperimentSpecByParent(this);
        }

        return baselineExperimentSpec;
    }

    /**
     * Set the baseline experiment specification.
     *
     * @param baselineExperimentSpec the baseline experiment specification
     */
    public void setBaselineExperimentSpec(ExperimentSpec baselineExperimentSpec) {
        this.baselineExperimentSpec = baselineExperimentSpec;
    }

    /**
     * Get the variable property names.
     *
     * @return the variable property names
     */
    public List<String> getVariablePropertyNames() {
        return CollectionHelper.transform(this.variables, new Function1<ExperimentPackVariable, String>() {
            @Override
            public String apply(ExperimentPackVariable variable) {
                return variable.getName();
            }
        });
    }

    /**
     * Get the variable property values.
     *
     * @return the variable property values.
     */
    public List<String> getVariablePropertyValues() {
        return CollectionHelper.transform(getCombinations(), new Function1<List<String>, String>() {
            @Override
            public String apply(List<String> combination) {
                return StringUtils.join(combination, "_");
            }
        });
    }

    /**
     * Get the variables.
     *
     * @return the variables
     */
    public List<ExperimentPackVariable> getVariables() {
        return variables;
    }

    /**
     * Set the variables.
     *
     * @param variables the variables
     */
    public void setVariables(List<ExperimentPackVariable> variables) {
        this.variables = new ArrayList<ExperimentPackVariable>(variables);
    }

    /**
     * Get the experiment specifications.
     *
     * @return the experiment specifications
     */
    public List<ExperimentSpec> getExperimentSpecs() {
        try {
            List<ExperimentSpec> experimentSpecs = new ArrayList<ExperimentSpec>();

            if (!CollectionUtils.isEmpty(this.variables)) {
                for (List<String> combination : getCombinations()) {
                    ExperimentSpec experimentSpec = (ExperimentSpec) BeanUtils.cloneBean(this.getBaselineExperimentSpec());
                    int i = 0;
                    for (String value : combination) {
                        String name = this.variables.get(i++).getName();
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

    /**
     * Get the combinations from the variables.
     *
     * @return the combinations
     */
    private List<List<String>> getCombinations() {
        return CombinationHelper.getCombinations(CollectionHelper.transform(this.variables, new Function1<ExperimentPackVariable, List<String>>() {
            @Override
            public List<String> apply(ExperimentPackVariable variable) {
                return variable.getValues();
            }
        }));
    }
}
