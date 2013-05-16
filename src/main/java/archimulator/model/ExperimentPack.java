/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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

import archimulator.util.PropertyArray;
import archimulator.util.serialization.StringArrayListJsonSerializableType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.action.Function1;
import net.pickapack.collection.CollectionHelper;
import net.pickapack.collection.CombinationHelper;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.WithCreateTime;
import net.pickapack.model.WithId;
import net.pickapack.model.WithTitle;
import org.apache.commons.beanutils.BeanUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Experiment pack.
 *
 * @author Min Cai
 */
@Root
@DatabaseTable(tableName = "ExperimentPack")
public class ExperimentPack implements WithId, WithTitle, WithCreateTime {
    @DatabaseField(generatedId = true)
    private long id;

    @Attribute
    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @Attribute
    @DatabaseField
    private ExperimentType experimentType;

    @ElementList
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> tags;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> benchmarkTitle;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> numMaxInstructions;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> helperThreadLookahead;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> helperThreadStride;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> numMainThreadWaysInStaticPartitionedLRUPolicy;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> numCores;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> numThreadsPerCore;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l1ISize;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l1IAssociativity;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l1DSize;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l1DAssociativity;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l2Size;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l2Associativity;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l2ReplacementPolicyType;

    @ElementList
    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> dynamicSpeculativePrecomputationEnabled;

    private transient List<ExperimentPackVariable> variables;

    /**
     * Create an experiment pack specification. Reserved for ORM only.
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
     * Get the list of tags.
     *
     * @return the list of tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Set the list of tags.
     *
     * @param tags the list of tags
     */
    public void setTags(List<String> tags) {
        this.tags = new ArrayList<String>(tags);
    }

    /**
     * Get the list of benchmark titles.
     *
     * @return the list of benchmark titles
     */
    public List<String> getBenchmarkTitle() {
        return benchmarkTitle;
    }

    /**
     * Set the list of benchmark titles.
     *
     * @param benchmarkTitle the list of benchmark titles
     */
    public void setBenchmarkTitle(List<String> benchmarkTitle) {
        this.benchmarkTitle = new ArrayList<String>(benchmarkTitle);
    }

    /**
     * Get the list of maximum number of instructions.
     *
     * @return the list of maximum number of instructions
     */
    public List<String> getNumMaxInstructions() {
        return numMaxInstructions;
    }

    /**
     * Set the list of maximum number of instructions.
     *
     * @param numMaxInstructions the list of maximum number of instructions
     */
    public void setNumMaxInstructions(List<String> numMaxInstructions) {
        this.numMaxInstructions = new ArrayList<String>(numMaxInstructions);
    }

    /**
     * Get the list of helper thread lookahead values.
     *
     * @return the list of helper thread lookahead values
     */
    public List<String> getHelperThreadLookahead() {
        return helperThreadLookahead;
    }

    /**
     * Set the list of helper thread lookahead values.
     *
     * @param helperThreadLookahead the list of helper thread lookahead values
     */
    public void setHelperThreadLookahead(List<String> helperThreadLookahead) {
        this.helperThreadLookahead = new ArrayList<String>(helperThreadLookahead);
    }

    /**
     * Get the list of helper thread stride values.
     *
     * @return the list of helper thread stride values
     */
    public List<String> getHelperThreadStride() {
        return helperThreadStride;
    }

    /**
     * Set the list of helper thread stride values.
     *
     * @param helperThreadStride the list of helper thread stride values
     */
    public void setHelperThreadStride(List<String> helperThreadStride) {
        this.helperThreadStride = new ArrayList<String>(helperThreadStride);
    }

    /**
     * Get the list of number of main thread ways used in the static partitioned LRU policy.
     *
     * @return the list of number of main thread ways used in the static partitioned LRU policy
     */
    public List<String> getNumMainThreadWaysInStaticPartitionedLRUPolicy() {
        return numMainThreadWaysInStaticPartitionedLRUPolicy;
    }

    /**
     * Set the list of number of main thread ways used in the static partitioned LRU policy.
     *
     * @param numMainThreadWaysInStaticPartitionedLRUPolicy the list of number of main thread ways used in the static partitioned LRU policy
     */
    public void setNumMainThreadWaysInStaticPartitionedLRUPolicy(List<String> numMainThreadWaysInStaticPartitionedLRUPolicy) {
        this.numMainThreadWaysInStaticPartitionedLRUPolicy = new ArrayList<String>(numMainThreadWaysInStaticPartitionedLRUPolicy);
    }

    /**
     * Get the list of number of cores.
     *
     * @return the list of number of cores
     */
    public List<String> getNumCores() {
        return numCores;
    }

    /**
     * Set the list of number of cores.
     *
     * @param numCores the list of number of cores
     */
    public void setNumCores(List<String> numCores) {
        this.numCores = new ArrayList<String>(numCores);
    }

    /**
     * Get the list of number of threads per core.
     *
     * @return the list of number of threads per core
     */
    public List<String> getNumThreadsPerCore() {
        return numThreadsPerCore;
    }

    /**
     * Set the list of number of threads per core.
     *
     * @param numThreadsPerCore the list of number of threads per core
     */
    public void setNumThreadsPerCore(List<String> numThreadsPerCore) {
        this.numThreadsPerCore = new ArrayList<String>(numThreadsPerCore);
    }

    /**
     * Get the list of L1I cache size values.
     *
     * @return the list of L1I cache size values
     */
    public List<String> getL1ISize() {
        return l1ISize;
    }

    /**
     * Set the list of L1I cache size values.
     *
     * @param l1ISize the list of L1I cache size values
     */
    public void setL1ISize(List<String> l1ISize) {
        this.l1ISize = new ArrayList<String>(l1ISize);
    }

    /**
     * Get the list of L1I cache associativity values.
     *
     * @return the list of L1I cache associativity values
     */
    public List<String> getL1IAssociativity() {
        return l1IAssociativity;
    }

    /**
     * Set the list of L1I cache associativity values.
     *
     * @param l1IAssociativity the list of L1I cache associativity values
     */
    public void setL1IAssociativity(List<String> l1IAssociativity) {
        this.l1IAssociativity = new ArrayList<String>(l1IAssociativity);
    }

    /**
     * Get the list of L1D cache size values.
     *
     * @return the list of L1D cache size values
     */
    public List<String> getL1DSize() {
        return l1DSize;
    }

    /**
     * Set the list of L1D cache size values.
     *
     * @param l1DSize the list of L1D cache size values
     */
    public void setL1DSize(List<String> l1DSize) {
        this.l1DSize = new ArrayList<String>(l1DSize);
    }

    /**
     * Get the list of L1D cache associativity values.
     *
     * @return the list of L1D cache associativity values
     */
    public List<String> getL1DAssociativity() {
        return l1DAssociativity;
    }

    /**
     * Set the list of L1D cache associativity values.
     *
     * @param l1DAssociativity the list of L1D cache associativity values
     */
    public void setL1DAssociativity(List<String> l1DAssociativity) {
        this.l1DAssociativity = new ArrayList<String>(l1DAssociativity);
    }

    /**
     * Get the list of L2 cache size values.
     *
     * @return the list of L2 cache size values
     */
    public List<String> getL2Size() {
        return l2Size;
    }

    /**
     * Set the list of L2 cache size values.
     *
     * @param l2Size the list of L2 cache size values
     */
    public void setL2Size(List<String> l2Size) {
        this.l2Size = new ArrayList<String>(l2Size);
    }

    /**
     * Get the list of L2 cache associativity values.
     *
     * @return the list of L2 cache associativity values
     */
    public List<String> getL2Associativity() {
        return l2Associativity;
    }

    /**
     * Set the list of L2 cache associativity values.
     *
     * @param l2Associativity the list of L2 cache associativity values
     */
    public void setL2Associativity(List<String> l2Associativity) {
        this.l2Associativity = new ArrayList<String>(l2Associativity);
    }

    /**
     * Get the list of L2 cache replacement policy type values.
     *
     * @return the list of L2 cache replacement policy type values
     */
    public List<String> getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }

    /**
     * Set the list of L2 cache replacement policy type values.
     *
     * @param l2ReplacementPolicyType the list of L2 cache replacement policy type values
     */
    public void setL2ReplacementPolicyType(List<String> l2ReplacementPolicyType) {
        this.l2ReplacementPolicyType = new ArrayList<String>(l2ReplacementPolicyType);
    }

    /**
     * Get the list of boolean values indicating whether the dynamic speculative precomputation is enabled or not.
     * @return the list of boolean values indicating whether the dynamic speculative precomputation is enabled or not
     */
    public List<String> getDynamicSpeculativePrecomputationEnabled() {
        return dynamicSpeculativePrecomputationEnabled;
    }

    /**
     * Set the list of boolean values indicating whether the dynamic speculative precomputation is enabled or not.
     *
     * @param dynamicSpeculativePrecomputationEnabled the list of boolean values indicating whether the dynamic speculative precomputation is enabled or not
     */
    public void setDynamicSpeculativePrecomputationEnabled(List<String> dynamicSpeculativePrecomputationEnabled) {
        this.dynamicSpeculativePrecomputationEnabled = new ArrayList<String>(dynamicSpeculativePrecomputationEnabled);
    }

    /**
     * Get the experiment specifications.
     *
     * @return the experiment specifications
     */
    public List<ExperimentSpec> getExperimentSpecs() {
        try {
            List<ExperimentSpec> experimentSpecs = new ArrayList<ExperimentSpec>();

            for (List<String> combination : getCombinations()) {
                ExperimentSpec experimentSpec = new ExperimentSpec();
                int i = 0;
                for (String value : combination) {
                    String name = this.getVariables().get(i++).getName();
                    BeanUtils.setProperty(experimentSpec, name, value);
                }
                experimentSpecs.add(experimentSpec);
            }

            return experimentSpecs;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the variables.
     *
     * @return the variables
     */
    public List<ExperimentPackVariable> getVariables() {
        if (this.variables == null) {
            this.variables = new ArrayList<ExperimentPackVariable>();
        }

        if (this.variables.isEmpty()) {
            try {
                for (final Field field : this.getClass().getDeclaredFields()) {
                    PropertyArray propertyAnnotation = field.getAnnotation(PropertyArray.class);
                    if (propertyAnnotation != null) {
                        variables.add(new ExperimentPackVariable() {{
                            setName(field.getName());
                            setValues(Arrays.asList(BeanUtils.getArrayProperty(ExperimentPack.this, field.getName())));
                        }});
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return this.variables;
    }

    /**
     * Get the combinations from the variables.
     *
     * @return the combinations
     */
    private List<List<String>> getCombinations() {
        return CombinationHelper.getCombinations(CollectionHelper.transform(this.getVariables(), new Function1<ExperimentPackVariable, List<String>>() {
            @Override
            public List<String> apply(ExperimentPackVariable variable) {
                return variable.getValues();
            }
        }));
    }
}
