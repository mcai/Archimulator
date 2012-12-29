package archimulator.model;

import archimulator.util.serialization.StringArrayListJsonSerializableType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.action.Function1;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.WithCreateTime;
import net.pickapack.model.WithId;
import net.pickapack.model.WithTitle;
import net.pickapack.util.CollectionHelper;
import net.pickapack.util.CombinationHelper;
import org.apache.commons.beanutils.BeanUtils;

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
@DatabaseTable(tableName = "ExperimentPack")
public class ExperimentPack implements WithId, WithTitle, WithCreateTime {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private ExperimentType experimentType;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> benchmarkTitle;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> numMaxInstructions;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> helperThreadLookahead;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> helperThreadStride;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> numCores;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> numThreadsPerCore;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l1ISize;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l1IAssociativity;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l1DSize;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l1DAssociativity;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l2Size;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l2Associativity;

    @PropertyArray
    @DatabaseField(persisterClass = StringArrayListJsonSerializableType.class)
    private ArrayList<String> l2ReplacementPolicyType;

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

    public List<String> getBenchmarkTitle() {
        return benchmarkTitle;
    }

    public void setBenchmarkTitle(List<String> benchmarkTitle) {
        this.benchmarkTitle = new ArrayList<String>(benchmarkTitle);
    }

    public List<String> getNumMaxInstructions() {
        return numMaxInstructions;
    }

    public void setNumMaxInstructions(List<String> numMaxInstructions) {
        this.numMaxInstructions = new ArrayList<String>(numMaxInstructions);
    }

    public List<String> getHelperThreadLookahead() {
        return helperThreadLookahead;
    }

    public void setHelperThreadLookahead(List<String> helperThreadLookahead) {
        this.helperThreadLookahead = new ArrayList<String>(helperThreadLookahead);
    }

    public List<String> getHelperThreadStride() {
        return helperThreadStride;
    }

    public void setHelperThreadStride(List<String> helperThreadStride) {
        this.helperThreadStride = new ArrayList<String>(helperThreadStride);
    }

    public List<String> getNumCores() {
        return numCores;
    }

    public void setNumCores(List<String> numCores) {
        this.numCores = new ArrayList<String>(numCores);
    }

    public List<String> getNumThreadsPerCore() {
        return numThreadsPerCore;
    }

    public void setNumThreadsPerCore(List<String> numThreadsPerCore) {
        this.numThreadsPerCore = new ArrayList<String>(numThreadsPerCore);
    }

    public List<String> getL1ISize() {
        return l1ISize;
    }

    public void setL1ISize(List<String> l1ISize) {
        this.l1ISize = new ArrayList<String>(l1ISize);
    }

    public List<String> getL1IAssociativity() {
        return l1IAssociativity;
    }

    public void setL1IAssociativity(List<String> l1IAssociativity) {
        this.l1IAssociativity = new ArrayList<String>(l1IAssociativity);
    }

    public List<String> getL1DSize() {
        return l1DSize;
    }

    public void setL1DSize(List<String> l1DSize) {
        this.l1DSize = new ArrayList<String>(l1DSize);
    }

    public List<String> getL1DAssociativity() {
        return l1DAssociativity;
    }

    public void setL1DAssociativity(List<String> l1DAssociativity) {
        this.l1DAssociativity = new ArrayList<String>(l1DAssociativity);
    }

    public List<String> getL2Size() {
        return l2Size;
    }

    public void setL2Size(List<String> l2Size) {
        this.l2Size = new ArrayList<String>(l2Size);
    }

    public List<String> getL2Associativity() {
        return l2Associativity;
    }

    public void setL2Associativity(List<String> l2Associativity) {
        this.l2Associativity = new ArrayList<String>(l2Associativity);
    }

    public ArrayList<String> getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }

    public void setL2ReplacementPolicyType(ArrayList<String> l2ReplacementPolicyType) {
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;
    }

    public ArrayList<String> getDynamicSpeculativePrecomputationEnabled() {
        return dynamicSpeculativePrecomputationEnabled;
    }

    public void setDynamicSpeculativePrecomputationEnabled(ArrayList<String> dynamicSpeculativePrecomputationEnabled) {
        this.dynamicSpeculativePrecomputationEnabled = dynamicSpeculativePrecomputationEnabled;
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
        if(this.variables == null) {
            this.variables = new ArrayList<ExperimentPackVariable>();
        }

        if(this.variables.isEmpty()) {
            try {
                for(final Field field : this.getClass().getDeclaredFields()) {
                    PropertyArray propertyAnnotation = field.getAnnotation(PropertyArray.class);
                    if(propertyAnnotation != null) {
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
