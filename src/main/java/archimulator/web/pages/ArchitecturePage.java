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
package archimulator.web.pages;

import archimulator.model.Architecture;
import archimulator.service.ServiceManager;
import archimulator.sim.core.bpred.BranchPredictorType;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.dram.MainMemoryType;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.util.StorageUnitHelper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.Arrays;

@MountPath(value = "/", alt = "/architecture")
public class ArchitecturePage extends AuthenticatedBasePage {
    public ArchitecturePage(final PageParameters parameters) {
        super(PageType.ARCHITECTURE, parameters);

        final String action = parameters.get("action").toString();

        final Architecture architecture;

        if(action == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        }
        else if (action.equals("add")) {
            architecture = new Architecture(false, 2, 2,
                    (int) StorageUnitHelper.displaySizeToByteCount("32 KB"),
                    4,
                    (int) StorageUnitHelper.displaySizeToByteCount("32 KB"),
                    4,
                    (int) StorageUnitHelper.displaySizeToByteCount("96 KB"),
                    8,
                    CacheReplacementPolicyType.LRU);
        } else if (action.equals("edit")) {
            long architectureId = parameters.get("architecture_id").toLong(-1);
            architecture = ServiceManager.getArchitectureService().getArchitectureById(architectureId);
        } else {
            throw new IllegalArgumentException();
        }

        if (architecture == null) {
            setResponsePage(getApplication().getHomePage());
            return;
        }

        setTitle((action.equals("add") ? "Add" : "Edit") + " Architecture - Archimulator");

        this.add(new Label("section_header_architecture", String.format("%s Architecture '{%d} %s'", action.equals("add") ? "Add" : "Edit", architecture.getId(), architecture.getTitle())));

        add(new FeedbackPanel("span_feedback"));

        this.add(new Form("form_architecture") {{
            this.add(new TextField<String>("input_id", Model.of(architecture.getId() + "")));
            this.add(new TextField<String>("input_title", Model.of(architecture.getTitle())));

            this.add(new NumberTextField<Integer>("input_num_cores", new PropertyModel<Integer>(architecture, "numCores")));
            this.add(new NumberTextField<Integer>("input_num_threads_per_core", new PropertyModel<Integer>(architecture, "numThreadsPerCore")));

            this.add(new NumberTextField<Integer>("input_helperThreadPthreadSpawnIndex", new PropertyModel<Integer>(architecture, "helperThreadPthreadSpawnIndex")));
            this.add(new CheckBox(
                    "input_ht_llc_request_profiling_enabled",
                    new PropertyModel<Boolean>(architecture, "helperThreadL2CacheRequestProfilingEnabled")));

            this.add(new TextField<String>("input_create_time", Model.of(DateHelper.toString(architecture.getCreateTime()))));

            this.add(new NumberTextField<Integer>("input_physical_register_file_capacity", new PropertyModel<Integer>(architecture, "physicalRegisterFileCapacity")));
            this.add(new NumberTextField<Integer>("input_decode_width", new PropertyModel<Integer>(architecture, "decodeWidth")));
            this.add(new NumberTextField<Integer>("input_issue_width", new PropertyModel<Integer>(architecture, "issueWidth")));
            this.add(new NumberTextField<Integer>("input_commit_width", new PropertyModel<Integer>(architecture, "commitWidth")));
            this.add(new NumberTextField<Integer>("input_decode_buffer_capacity", new PropertyModel<Integer>(architecture, "decodeBufferCapacity")));
            this.add(new NumberTextField<Integer>("input_reorder_buffer_capacity", new PropertyModel<Integer>(architecture, "reorderBufferCapacity")));
            this.add(new NumberTextField<Integer>("input_load_store_queue_capacity", new PropertyModel<Integer>(architecture, "loadStoreQueueCapacity")));

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

            this.add(new RequiredTextField<String>("input_tlb_size", new PropertyModel<String>(architecture, "tlbSizeInStorageUnit")));
            this.add(new NumberTextField<Integer>("input_tlb_associativity", new PropertyModel<Integer>(architecture, "tlbAssociativity")));
            this.add(new NumberTextField<Integer>("input_tlb_line_size", new PropertyModel<Integer>(architecture, "tlbLineSize")));
            this.add(new NumberTextField<Integer>("input_tlb_hit_latency", new PropertyModel<Integer>(architecture, "tlbHitLatency")));
            this.add(new NumberTextField<Integer>("input_tlb_miss_latency", new PropertyModel<Integer>(architecture, "tlbMissLatency")));

            this.add(new RequiredTextField<String>("input_l1I_size", new PropertyModel<String>(architecture, "l1ISizeInStorageUnit")));
            this.add(new NumberTextField<Integer>("input_l1I_associativity", new PropertyModel<Integer>(architecture, "l1IAssociativity")));
            this.add(new NumberTextField<Integer>("input_l1I_line_size", new PropertyModel<Integer>(architecture, "l1ILineSize")));
            this.add(new NumberTextField<Integer>("input_l1I_hit_latency", new PropertyModel<Integer>(architecture, "l1IHitLatency")));
            this.add(new NumberTextField<Integer>("input_l1INumReadPorts", new PropertyModel<Integer>(architecture, "l1INumReadPorts")));
            this.add(new NumberTextField<Integer>("input_l1INumWritePorts", new PropertyModel<Integer>(architecture, "l1INumWritePorts")));
            this.add(new DropDownChoice<CacheReplacementPolicyType>(
                    "select_l1I_repl",
                    new PropertyModel<CacheReplacementPolicyType>(architecture, "l1IReplacementPolicyType"),
                    Arrays.asList(CacheReplacementPolicyType.values())));

            this.add(new RequiredTextField<String>("input_l1D_size", new PropertyModel<String>(architecture, "l1DSizeInStorageUnit")));
            this.add(new NumberTextField<Integer>("input_l1D_associativity", new PropertyModel<Integer>(architecture, "l1DAssociativity")));
            this.add(new NumberTextField<Integer>("input_l1D_line_size", new PropertyModel<Integer>(architecture, "l1DLineSize")));
            this.add(new NumberTextField<Integer>("input_l1D_hit_latency", new PropertyModel<Integer>(architecture, "l1DHitLatency")));
            this.add(new NumberTextField<Integer>("input_l1DNumReadPorts", new PropertyModel<Integer>(architecture, "l1DNumReadPorts")));
            this.add(new NumberTextField<Integer>("input_l1DNumWritePorts", new PropertyModel<Integer>(architecture, "l1DNumWritePorts")));
            this.add(new DropDownChoice<CacheReplacementPolicyType>(
                    "select_l1D_repl",
                    new PropertyModel<CacheReplacementPolicyType>(architecture, "l1DReplacementPolicyType"),
                    Arrays.asList(CacheReplacementPolicyType.values())));

            this.add(new RequiredTextField<String>("input_l2_size", new PropertyModel<String>(architecture, "l2SizeInStorageUnit")));
            this.add(new NumberTextField<Integer>("input_l2_associativity", new PropertyModel<Integer>(architecture, "l2Associativity")));
            this.add(new NumberTextField<Integer>("input_l2_line_size", new PropertyModel<Integer>(architecture, "l2LineSize")));
            this.add(new NumberTextField<Integer>("input_l2_hit_latency", new PropertyModel<Integer>(architecture, "l2HitLatency")));
            this.add(new DropDownChoice<CacheReplacementPolicyType>(
                    "select_l2_repl",
                    new PropertyModel<CacheReplacementPolicyType>(architecture, "l2ReplacementPolicyType"),
                    Arrays.asList(CacheReplacementPolicyType.values())));

            final WebMarkupContainer divFixedLatencyMainMemory = new WebMarkupContainer("div_fixedLatencyMainMemory") {{
                setOutputMarkupPlaceholderTag(true);
                setVisible(architecture.getMainMemoryType() == MainMemoryType.FIXED_LATENCY);

                this.add(new NumberTextField<Integer>("input_fixedLatencyMainMemoryLatency", new PropertyModel<Integer>(architecture, "fixedLatencyMainMemoryLatency")));
            }};
            this.add(divFixedLatencyMainMemory);

            final WebMarkupContainer divSimpleMainMemory = new WebMarkupContainer("div_simpleMainMemory") {{
                setOutputMarkupPlaceholderTag(true);
                setVisible(architecture.getMainMemoryType() == MainMemoryType.SIMPLE);

                this.add(new NumberTextField<Integer>("input_simpleMainMemoryMemoryLatency", new PropertyModel<Integer>(architecture, "simpleMainMemoryMemoryLatency")));
                this.add(new NumberTextField<Integer>("input_simpleMainMemoryMemoryTrunkLatency", new PropertyModel<Integer>(architecture, "simpleMainMemoryMemoryTrunkLatency")));
                this.add(new NumberTextField<Integer>("input_simpleMainMemoryBusWidth", new PropertyModel<Integer>(architecture, "simpleMainMemoryBusWidth")));
            }};
            this.add(divSimpleMainMemory);

            final WebMarkupContainer divBasicMainMemory = new WebMarkupContainer("div_basicMainMemory") {{
                setOutputMarkupPlaceholderTag(true);
                setVisible(architecture.getMainMemoryType() == MainMemoryType.BASIC);

                this.add(new NumberTextField<Integer>("input_basicMainMemoryToDramLatency", new PropertyModel<Integer>(architecture, "basicMainMemoryToDramLatency")));
                this.add(new NumberTextField<Integer>("input_basicMainMemoryFromDramLatency", new PropertyModel<Integer>(architecture, "basicMainMemoryFromDramLatency")));
                this.add(new NumberTextField<Integer>("input_basicMainMemoryPrechargeLatency", new PropertyModel<Integer>(architecture, "basicMainMemoryPrechargeLatency")));
                this.add(new NumberTextField<Integer>("input_basicMainMemoryClosedLatency", new PropertyModel<Integer>(architecture, "basicMainMemoryClosedLatency")));
                this.add(new NumberTextField<Integer>("input_basicMainMemoryConflictLatency", new PropertyModel<Integer>(architecture, "basicMainMemoryConflictLatency")));
                this.add(new NumberTextField<Integer>("input_basicMainMemoryBusWidth", new PropertyModel<Integer>(architecture, "basicMainMemoryBusWidth")));
                this.add(new NumberTextField<Integer>("input_basicMainMemoryNumBanks", new PropertyModel<Integer>(architecture, "basicMainMemoryNumBanks")));
                this.add(new NumberTextField<Integer>("input_basicMainMemoryRowSize", new PropertyModel<Integer>(architecture, "basicMainMemoryRowSize")));
            }};
            this.add(divBasicMainMemory);

            this.add(new DropDownChoice<MainMemoryType>("select_mainMemoryType", new PropertyModel<MainMemoryType>(architecture, "mainMemoryType"), Arrays.asList(MainMemoryType.values())) {{
                add(new AjaxFormComponentUpdatingBehavior("onchange") {
                    protected void onUpdate(AjaxRequestTarget target) {
                        divFixedLatencyMainMemory.setVisible(architecture.getMainMemoryType() == MainMemoryType.FIXED_LATENCY);
                        divSimpleMainMemory.setVisible(architecture.getMainMemoryType() == MainMemoryType.SIMPLE);
                        divBasicMainMemory.setVisible(architecture.getMainMemoryType() == MainMemoryType.BASIC);

                        target.add(divFixedLatencyMainMemory);
                        target.add(divSimpleMainMemory);
                        target.add(divBasicMainMemory);
                    }
                });
            }});

            this.add(new Button("button_save", Model.of(action.equals("add") ? "Add" : "Save")) {
                @Override
                public void onSubmit() {
                    if (action.equals("add")) {
                        architecture.updateTitle();

                        if(ServiceManager.getArchitectureService().getArchitectureByTitle(architecture.getTitle()) == null) {
                            ServiceManager.getArchitectureService().addArchitecture(architecture);
                        }
                    } else {
                        architecture.updateTitle();

                        Architecture architectureWithSameTitle = ServiceManager.getArchitectureService().getArchitectureByTitle(architecture.getTitle());
                        if(architectureWithSameTitle != null && architectureWithSameTitle.getId() != architecture.getId()) {
                            ServiceManager.getArchitectureService().removeArchitectureById(architecture.getId());
                        }
                        else {
                            ServiceManager.getArchitectureService().updateArchitecture(architecture);
                        }
                    }

                    back(parameters, ArchitecturesPage.class);
                }
            });

            this.add(new Button("button_cancel") {
                {
                    setDefaultFormProcessing(false);
                }

                @Override
                public void onSubmit() {
                    back(parameters, ArchitecturesPage.class);
                }
            });

            this.add(new Button("button_remove") {
                {
                    setDefaultFormProcessing(false);
                    setVisible(action.equals("edit"));
                }

                @Override
                public void onSubmit() {
                    ServiceManager.getArchitectureService().removeArchitectureById(architecture.getId());

                    back(parameters, ArchitecturesPage.class);
                }
            });
        }});
    }
}
