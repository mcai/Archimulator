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
     *
     */
    public ExperimentPack() {
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     *
     * @return
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     *
     * @return
     */
    @Override
    public long getParentId() {
        return -1;
    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     *
     * @return
     */
    public String getCreateTimeAsString() {
        return DateHelper.toString(createTime);
    }

    /**
     *
     * @return
     */
    public ExperimentType getExperimentType() {
        return experimentType;
    }

    /**
     *
     * @param experimentType
     */
    public void setExperimentType(ExperimentType experimentType) {
        this.experimentType = experimentType;
    }

    /**
     *
     * @return
     */
    public ExperimentSpec getBaselineExperimentSpec() {
        if(baselineExperimentSpec == null) {
            return ServiceManager.getExperimentService().getExperimentSpecByParent(this);
        }

        return baselineExperimentSpec;
    }

    /**
     *
     * @param baselineExperimentSpec
     */
    public void setBaselineExperimentSpec(ExperimentSpec baselineExperimentSpec) {
        this.baselineExperimentSpec = baselineExperimentSpec;
    }

    /**
     *
     * @return
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
     *
     * @return
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
     *
     * @return
     */
    public List<ExperimentPackVariable> getVariables() {
        return variables;
    }

    /**
     *
     * @param variables
     */
    public void setVariables(List<ExperimentPackVariable> variables) {
        this.variables = new ArrayList<ExperimentPackVariable>(variables);
    }

    /**
     *
     * @return
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

    private List<List<String>> getCombinations() {
        return CombinationHelper.getCombinations(CollectionHelper.transform(this.variables, new Function1<ExperimentPackVariable, List<String>>() {
            @Override
            public List<String> apply(ExperimentPackVariable variable) {
                return variable.getValues();
            }
        }));
    }
}
