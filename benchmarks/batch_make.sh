BENCHMARKS_DIR=`pwd`

ACTION=-B
#~ ACTION=clean

cd $BENCHMARKS_DIR/CPU2006_Custom1/429.mcf/baseline;make $ACTION
cd $BENCHMARKS_DIR/CPU2006_Custom1/429.mcf/baseline;make -f Makefile.mips $ACTION

cd $BENCHMARKS_DIR/CPU2006_Custom1/429.mcf/ht;make $ACTION
cd $BENCHMARKS_DIR/CPU2006_Custom1/429.mcf/ht;make -f Makefile.mips $ACTION

cd $BENCHMARKS_DIR/CPU2006_Custom1/462.libquantum/baseline;make $ACTION
cd $BENCHMARKS_DIR/CPU2006_Custom1/462.libquantum/baseline;make -f Makefile.mips $ACTION

cd $BENCHMARKS_DIR/CPU2006_Custom1/462.libquantum/ht;make $ACTION
cd $BENCHMARKS_DIR/CPU2006_Custom1/462.libquantum/ht;make -f Makefile.mips $ACTION

cd $BENCHMARKS_DIR/Olden_Custom1/em3d/baseline;make $ACTION
cd $BENCHMARKS_DIR/Olden_Custom1/em3d/baseline;make -f Makefile.mips $ACTION

cd $BENCHMARKS_DIR/Olden_Custom1/em3d/ht;make $ACTION
cd $BENCHMARKS_DIR/Olden_Custom1/em3d/ht;make -f Makefile.mips $ACTION

cd $BENCHMARKS_DIR/Olden_Custom1/mst/baseline;make $ACTION
cd $BENCHMARKS_DIR/Olden_Custom1/mst/baseline;make -f Makefile.mips $ACTION

cd $BENCHMARKS_DIR/Olden_Custom1/mst/ht;make $ACTION
cd $BENCHMARKS_DIR/Olden_Custom1/mst/ht;make -f Makefile.mips $ACTION
