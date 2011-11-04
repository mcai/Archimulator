EXTRA_CFLAGS = -fno-strict-aliasing -fPIC
EXTRA_LDFLAGS = -shared
EXTRA_LIBS = -lm -lbfd -lpthread

PROG = libmips_isa_emulator.so
#OBJS = isa_emulator.o misc.o
OBJS = isa_emulator.o buffer.o config.o eval.o hash.o kernel.o list.o loader.o machine.o memory.o mhandle.o misc.o options.o regs.o stats.o syscall.o system.o mips_isa_emulator.o

include Makefile.include
