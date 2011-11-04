action=-B
#~ action=clean

cd /home/itecgo/FleximJ/benchmarks/CPU2006_Custom1/429.mcf/baseline;make $action
cd /home/itecgo/FleximJ/benchmarks/CPU2006_Custom1/429.mcf/baseline;make -f Makefile.mips $action

cd /home/itecgo/FleximJ/benchmarks/CPU2006_Custom1/429.mcf/ht;make $action
cd /home/itecgo/FleximJ/benchmarks/CPU2006_Custom1/429.mcf/ht;make -f Makefile.mips $action

cd /home/itecgo/FleximJ/benchmarks/CPU2006_Custom1/462.libquantum/baseline;make $action
cd /home/itecgo/FleximJ/benchmarks/CPU2006_Custom1/462.libquantum/baseline;make -f Makefile.mips $action

cd /home/itecgo/FleximJ/benchmarks/CPU2006_Custom1/462.libquantum/ht;make $action
cd /home/itecgo/FleximJ/benchmarks/CPU2006_Custom1/462.libquantum/ht;make -f Makefile.mips $action

cd /home/itecgo/FleximJ/benchmarks/Olden_Custom1/em3d/baseline;make $action
cd /home/itecgo/FleximJ/benchmarks/Olden_Custom1/em3d/baseline;make -f Makefile.mips $action

cd /home/itecgo/FleximJ/benchmarks/Olden_Custom1/em3d/ht;make $action
cd /home/itecgo/FleximJ/benchmarks/Olden_Custom1/em3d/ht;make -f Makefile.mips $action

cd /home/itecgo/FleximJ/benchmarks/Olden_Custom1/mst/baseline;make $action
cd /home/itecgo/FleximJ/benchmarks/Olden_Custom1/mst/baseline;make -f Makefile.mips $action

cd /home/itecgo/FleximJ/benchmarks/Olden_Custom1/mst/ht;make $action
cd /home/itecgo/FleximJ/benchmarks/Olden_Custom1/mst/ht;make -f Makefile.mips $action
