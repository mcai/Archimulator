package archimulator.isa;

import archimulator.os.Context;
import archimulator.util.action.Action;

public class NativeEmulatorEnhancedMipsInstructionExecutor extends BasicMipsInstructionExecutor {
//    // @Override
//    public void add_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.add_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).add_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).add_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void addi_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.addi_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).addi_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).addi_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void addiu_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.addiu_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).addiu_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).addiu_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void addu_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.addu_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).addu_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).addu_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void and_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.and_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).and_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).and_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void andi_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.andi_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).andi_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).andi_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void div_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.div_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).div_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).div_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void divu_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.divu_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).divu_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).divu_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void lui_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.lui_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lui_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lui_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void madd_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.madd_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).madd_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).madd_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void mfhi_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.mfhi_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mfhi_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mfhi_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void mflo_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.mflo_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mflo_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mflo_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void msub_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.msub_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).msub_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).msub_impl(context.getId(), machInst);
//    }
//
//    //    // @Override //TODO
//    public void mthi_impl(final Context context, final int machInst) {
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mthi_impl(context.getId(), machInst);
//        throw new UnsupportedOperationException();
//    }
//
//    // @Override
//    public void mtlo_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.mtlo_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mtlo_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mtlo_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void mult_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.mult_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mult_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mult_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void multu_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.multu_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).multu_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).multu_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void nor_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.nor_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).nor_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).nor_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void or_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.or_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).or_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).or_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void ori_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.ori_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).ori_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).ori_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sll_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sll_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sll_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sll_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sllv_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sllv_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sllv_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sllv_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void slt_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.slt_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).slt_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).slt_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void slti_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.slti_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).slti_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).slti_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sltiu_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sltiu_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sltiu_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sltiu_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sltu_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sltu_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sltu_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sltu_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sra_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sra_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sra_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sra_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void srav_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.srav_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).srav_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).srav_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void srl_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.srl_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).srl_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).srl_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void srlv_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.srlv_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).srlv_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).srlv_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void sub_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sub_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void subu_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.subu_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).subu_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).subu_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void xor_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.xor_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).xor_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).xor_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void xori_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.xori_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).xori_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).xori_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void abs_d_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.abs_d_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).abs_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).abs_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void abs_s_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.abs_s_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).abs_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).abs_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void add_d_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.add_d_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._add_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._add_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void add_s_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.add_s_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._add_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._add_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void c_cond_d_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.c_cond_d_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).c_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).c_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void c_cond_s_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.c_cond_s_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).c_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).c_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void cvt_d_l_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.cvt_d_l_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_d_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_d_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void cvt_d_s_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.cvt_d_s_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_d_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_d_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void cvt_d_w_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.cvt_d_w_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_d_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_d_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void cvt_l_d_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_l_d_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void cvt_l_s_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_l_s_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void cvt_s_d_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.cvt_s_d_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_s_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_s_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void cvt_s_l_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.cvt_s_l_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_s_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_s_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void cvt_s_w_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.cvt_s_w_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_s_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_s_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void cvt_w_d_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.cvt_w_d_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_w_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_w_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void cvt_w_s_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.cvt_w_s_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_w_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cvt_w_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void div_d_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.div_d_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._div_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._div_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void div_s_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.div_s_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._div_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._div_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void mov_d_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.mov_d_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mov_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mov_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void mov_s_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.mov_s_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mov_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mov_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void movf_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.movf_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).movf_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).movf_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void _movf_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super._movf_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._movf_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._movf_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void movn_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.movn_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).movn_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).movn_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void _movn_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super._movn_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._movn_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._movn_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void _movt_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super._movt_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._movt_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._movt_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void movz_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.movz_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).movz_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).movz_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void _movz_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super._movz_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._movz_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._movz_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void mul_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.mul_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mul_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mul_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void trunc_w_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.trunc_w_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).trunc_w_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).trunc_w_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void mul_d_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.mul_d_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._mul_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._mul_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void mul_s_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.mul_s_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._mul_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._mul_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void neg_d_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.neg_d_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).neg_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).neg_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void neg_s_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.neg_s_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).neg_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).neg_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sqrt_d_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
////                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sqrt_d_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sqrt_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sqrt_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sqrt_s_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sqrt_s_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sqrt_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sqrt_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sub_d_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sub_d_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._sub_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._sub_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sub_s_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sub_s_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._sub_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst)._sub_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void j_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.j_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).j_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).j_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void jal_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.jal_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).jal_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).jal_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void jalr_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.jalr_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).jalr_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).jalr_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void jr_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.jr_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).jr_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).jr_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void b_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.b_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).b_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).b_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void bal_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.bal_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bal_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bal_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void bc1f_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.bc1f_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bc1f_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bc1f_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void bc1fl_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bc1fl_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void bc1t_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.bc1t_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bc1t_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bc1t_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void bc1tl_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bc1tl_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void beq_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.beq_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).beq_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).beq_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void beql_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).beql_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void bgez_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.bgez_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bgez_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bgez_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void bgezal_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.bgezal_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bgezal_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bgezal_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void bgezall_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bgezall_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void bgezl_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bgezl_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void bgtz_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.bgtz_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bgtz_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bgtz_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void bgtzl_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bgtzl_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void blez_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.blez_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).blez_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).blez_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void blezl_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).blezl_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void bltz_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.bltz_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bltz_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bltz_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void bltzal_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bltzal_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void bltzall_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bltzall_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void bltzl_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bltzl_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void bne_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.bne_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bne_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bne_impl(context.getId(), machInst);
//    }
//
//    // @Override //TODO
//    public void bnel_impl(final Context context, final int machInst) {
//        throw new UnsupportedOperationException();
////        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).bnel_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void lb_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.lb_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lb_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lb_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void lbu_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.lbu_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lbu_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lbu_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void ldc1_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.ldc1_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).ldc1_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).ldc1_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void lh_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.lh_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lh_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lh_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void lhu_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.lhu_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lhu_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lhu_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void ll_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.ll_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).ll_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).ll_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void lw_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.lw_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lw_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lw_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void lwc1_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.lwc1_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lwc1_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lwc1_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void lwl_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        NativeEmulatorEnhancedMipsInstructionExecutor.super.lwl_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lwl_impl(context.getId(), machInst);
//            }
//        }
//        );
//
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lwl_impl(context.getId(), machInst);
//    }

    //    // @Override
//    public void lwr_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        NativeEmulatorEnhancedMipsInstructionExecutor.super.lwr_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lwr_impl(context.getId(), machInst);
//            }
//        }
//        );
//
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).lwr_impl(context.getId(), machInst);
//    }
//
//
//    // @Override
//    public void sb_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sb_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sb_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sb_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sc_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sc_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sc_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sc_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sdc1_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sdc1_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sdc1_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sdc1_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sh_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sh_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sh_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sh_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void sw_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.sw_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sw_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).sw_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void swc1_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.swc1_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).swc1_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).swc1_impl(context.getId(), machInst);
//    }
//
    // @Override
//    public void swl_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        NativeEmulatorEnhancedMipsInstructionExecutor.super.swl_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).swl_impl(context.getId(), machInst);
//            }
//        }
//        );
//
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).swl_impl(context.getId(), machInst);
//    }

    // @Override
//    public void swr_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        NativeEmulatorEnhancedMipsInstructionExecutor.super.swr_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).swr_impl(context.getId(), machInst);
//            }
//        }
//        );
//
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).swr_impl(context.getId(), machInst);
//    }

    //
//
//    // @Override
//    public void cfc1_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.cfc1_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cfc1_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).cfc1_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void ctc1_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.ctc1_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).ctc1_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).ctc1_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void mfc1_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.mfc1_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mfc1_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mfc1_impl(context.getId(), machInst);
//    }
//
//    // @Override
//    public void mtc1_impl(final Context context, final int machInst) {
//        this.checkImplementations(context, new Action() {
//                    public void apply() {
//                        // NativeEmulatorEnhancedMipsInstructionExecutor.super.mtc1_impl(context, machInst);
//                    }
//                }, new Action() {
//            public void apply() {
//                context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mtc1_impl(context.getId(), machInst);
//            }
//        }
//        );
//        context.getKernel().getCapability(NativeMipsIsaEmulatorCapability.class).decode(machInst).mtc1_impl(context.getId(), machInst);
//    }
//
    private void checkImplementations(Context context, Action parentImplementation, Action childImplementation) {
        try {
            ArchitecturalRegisterFile oldRegs = (ArchitecturalRegisterFile) context.getRegs().clone();

            parentImplementation.apply();

            ArchitecturalRegisterFile resultRegs1 = (ArchitecturalRegisterFile) context.getRegs().clone();

            context.setRegs(oldRegs);

            childImplementation.apply();

            assert resultRegs1.equals(context.getRegs());

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
