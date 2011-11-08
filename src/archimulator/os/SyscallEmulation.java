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
package archimulator.os;

import archimulator.isa.ArchitecturalRegisterFile;
import archimulator.isa.SyscallExecutedEvent;
import archimulator.sim.BasicSimulationObject;
import archimulator.sim.SimulationObject;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public abstract class SyscallEmulation extends BasicSimulationObject implements SimulationObject, Serializable {
    public static final int EPERM = 1;
    public static final int ENOENT = 2;
    public static final int ESRCH = 3;
    public static final int EINTR = 4;
    public static final int EIO = 5;
    public static final int ENXIO = 6;
    public static final int E2BIG = 7;
    public static final int ENOEXEC = 8;
    public static final int EBADF = 9;
    public static final int ECHILD = 10;
    public static final int EAGAIN = 11;
    public static final int ENOMEM = 12;
    public static final int EACCES = 13;
    public static final int EFAULT = 14;
    public static final int ENOTBLK = 15;
    public static final int EBUSY = 16;
    public static final int EEXIST = 17;
    public static final int EXDEV = 18;
    public static final int ENODEV = 19;
    public static final int ENOTDIR = 20;
    public static final int EISDIR = 21;
    public static final int EINVAL = 22;
    public static final int ENFILE = 23;
    public static final int EMFILE = 24;
    public static final int ENOTTY = 25;
    public static final int ETXTBSY = 26;
    public static final int EFBIG = 27;
    public static final int ENOSPC = 28;
    public static final int ESPIPE = 29;
    public static final int EROFS = 30;
    public static final int EMLINK = 31;
    public static final int EPIPE = 32;
    public static final int EDOM = 33;
    public static final int ERANGE = 34;

    private static Map<Integer, SyscallHandler> handlers = new TreeMap<Integer, SyscallHandler>();
    private static int errno;

    protected boolean error;

    protected SyscallEmulation(Kernel kernel) {
        super(kernel);

        registerHandler(new SyscallHandler(1, "exit") {
            @Override
            public void run(Context context) {
                exit_impl(context);
            }
        });
        registerHandler(new SyscallHandler(3, "read") {
            @Override
            public void run(Context context) {
                read_impl(context);
            }
        });
        registerHandler(new SyscallHandler(4, "write") {
            @Override
            public void run(Context context) {
                write_impl(context);
            }
        });
        registerHandler(new SyscallHandler(5, "open") {
            @Override
            public void run(Context context) {
                open_impl(context);
            }
        });
        registerHandler(new SyscallHandler(6, "close") {
            @Override
            public void run(Context context) {
                close_impl(context);
            }
        });
        registerHandler(new SyscallHandler(7, "waitpid") {
            @Override
            public void run(Context context) {
                waitpid_impl(context);
            }
        });
        registerHandler(new SyscallHandler(20, "getpid") {
            @Override
            public void run(Context context) {
                getpid_impl(context);
            }
        });
        registerHandler(new SyscallHandler(24, "getuid") {
            @Override
            public void run(Context context) {
                getuid_impl(context);
            }
        });
        registerHandler(new SyscallHandler(37, "kill") {
            @Override
            public void run(Context context) {
                kill_impl(context);
            }
        });
        registerHandler(new SyscallHandler(42, "pipe") {
            @Override
            public void run(Context context) {
                pipe_impl(context);
            }
        });
        registerHandler(new SyscallHandler(45, "brk") {
            @Override
            public void run(Context context) {
                brk_impl(context);
            }
        });
        registerHandler(new SyscallHandler(47, "getgid") {
            @Override
            public void run(Context context) {
                getgid_impl(context);
            }
        });
        registerHandler(new SyscallHandler(49, "geteuid") {
            @Override
            public void run(Context context) {
                geteuid_impl(context);
            }
        });
        registerHandler(new SyscallHandler(50, "getegid") {
            @Override
            public void run(Context context) {
                getegid_impl(context);
            }
        });
        registerHandler(new SyscallHandler(54, "ioctl") {
            @Override
            public void run(Context context) {
                ioctl_impl(context);
            }
        });
        registerHandler(new SyscallHandler(64, "getppid") {
            @Override
            public void run(Context context) {
                getppid_impl(context);
            }
        });
        registerHandler(new SyscallHandler(75, "setrlimit") {
            @Override
            public void run(Context context) {
                setrlimit_impl(context);
            }
        });
        registerHandler(new SyscallHandler(76, "getrlimit") {
            @Override
            public void run(Context context) {
                getrlimit_impl(context);
            }
        });
        registerHandler(new SyscallHandler(90, "mmap") {
            @Override
            public void run(Context context) {
                mmap_impl(context);
            }
        });
        registerHandler(new SyscallHandler(91, "munmap") {
            @Override
            public void run(Context context) {
                munmap_impl(context);
            }
        });
        registerHandler(new SyscallHandler(120, "clone") {
            @Override
            public void run(Context context) {
                clone_impl(context);
            }
        });
        registerHandler(new SyscallHandler(122, "uname") {
            @Override
            public void run(Context context) {
                uname_impl(context);
            }
        });
        registerHandler(new SyscallHandler(125, "mprotect") {
            @Override
            public void run(Context context) {
                mprotect_impl(context);
            }
        });
        registerHandler(new SyscallHandler(140, "_llseek") {
            @Override
            public void run(Context context) {
                _llseek_impl(context);
            }
        });
        registerHandler(new SyscallHandler(153, "_sysctl") {
            @Override
            public void run(Context context) {
                _sysctl_impl(context);
            }
        });
        registerHandler(new SyscallHandler(166, "nanosleep") {
            @Override
            public void run(Context context) {
                nanosleep_impl(context);
            }
        });
        registerHandler(new SyscallHandler(167, "mremap") {
            @Override
            public void run(Context context) {
                mremap_impl(context);
            }
        });
        registerHandler(new SyscallHandler(188, "poll") {
            @Override
            public void run(Context context) {
                poll_impl(context);
            }
        });
        registerHandler(new SyscallHandler(194, "rt_sigaction") {
            @Override
            public void run(Context context) {
                rt_sigaction_impl(context);
            }
        });
        registerHandler(new SyscallHandler(195, "rt_sigprocmask") {
            @Override
            public void run(Context context) {
                rt_sigprocmask_impl(context);
            }
        });
        registerHandler(new SyscallHandler(199, "rt_sigsuspend") {
            @Override
            public void run(Context context) {
                rt_sigsuspend_impl(context);
            }
        });
        registerHandler(new SyscallHandler(215, "fstat64") {
            @Override
            public void run(Context context) {
                fstat64_impl(context);
            }
        });
        registerHandler(new SyscallHandler(246, "exit_group") {
            @Override
            public void run(Context context) {
                exit_group_impl(context);
            }
        });
    }

    public void doSyscall(int callNum, Context context) {
        int syscallIndex = callNum - 4000;

        if (!findAndCallSyscallHandler(syscallIndex, context)) {
            throw new RuntimeException(String.format("ctx-%d: Syscall %d (%d) not implemented", context.getId(), callNum, syscallIndex));
        }
    }

    protected boolean checkSyscallError(Context context) {
        if (context.getRegs().getGpr(ArchitecturalRegisterFile.REG_V0) != -1) {
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
            return false;
        } else {
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, errno);
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 1);
            return true;
        }
    }

    private boolean findAndCallSyscallHandler(int syscallIndex, Context context) {
        if (handlers.containsKey(syscallIndex)) {
            SyscallHandler syscallHandler = handlers.get(syscallIndex);

            syscallHandler.run(context);

            this.getBlockingEventDispatcher().dispatch(new SyscallExecutedEvent(syscallHandler.getName(), context));

//            Logger.infof(Logger.SYSCALL, "%s 0x%08x: syscall %s (a0=0x%08x, a1=0x%08x, a2=0x%08x); %s", context.getThread().getName(), context.getRegs().getPc(), syscallHandler.getName(),
//                    context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0),
//                    context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1),
//                    context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2),
//                    syscallHandler.isError() ? String.format("errorno=0x%08x", errno) : String.format("ret=0x%08x", context.getRegs().getGpr(ArchitecturalRegisterFile.REG_V0)));

//            System.out.printf("%s\n", context.getRegs().dump());

            context.getKernel().processSystemEvents();
            context.getKernel().processSignals();

            return true;
        }

        return false;
    }

    protected void registerHandler(SyscallHandler handler) {
        handlers.put(handler.getIndex(), handler);
    }

    protected abstract void exit_group_impl(Context context);

    protected abstract void fstat64_impl(Context context);

    protected abstract void rt_sigsuspend_impl(Context context);

    protected abstract void rt_sigprocmask_impl(Context context);

    protected abstract void rt_sigaction_impl(Context context);

    protected abstract void poll_impl(Context context);

    protected abstract void nanosleep_impl(Context context);

    protected abstract void mremap_impl(Context context);

    protected abstract void _sysctl_impl(Context context);

    protected abstract void _llseek_impl(Context context);

    protected abstract void mprotect_impl(Context context);

    protected abstract void uname_impl(Context context);

    protected abstract void clone_impl(Context context);

    protected abstract void munmap_impl(Context context);

    protected abstract void mmap_impl(Context context);

    protected abstract void getrlimit_impl(Context context);

    protected abstract void setrlimit_impl(Context context);

    protected abstract void getppid_impl(Context context);

    protected abstract void ioctl_impl(Context context);

    protected abstract void getegid_impl(Context context);

    protected abstract void geteuid_impl(Context context);

    protected abstract void getgid_impl(Context context);

    protected abstract void brk_impl(Context context);

    protected abstract void pipe_impl(Context context);

    protected abstract void kill_impl(Context context);

    protected abstract void getuid_impl(Context context);

    protected abstract void getpid_impl(Context context);

    protected abstract void waitpid_impl(Context context);

    protected abstract void close_impl(Context context);

    protected abstract void open_impl(Context context);

    protected abstract void write_impl(Context context);

    protected abstract void read_impl(Context context);

    protected abstract void exit_impl(Context context);
}
