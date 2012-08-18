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
package archimulator.sim.os;

import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.common.Logger;
import archimulator.sim.common.SimulationObject;
import archimulator.sim.isa.ArchitecturalRegisterFile;
import archimulator.sim.isa.SystemCallExecutedEvent;
import archimulator.sim.os.event.*;
import archimulator.sim.os.signal.SignalMask;
import net.pickapack.action.Predicate;
import net.pickapack.io.buffer.CircularByteBuffer;
import org.jruby.ext.posix.FileStat;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SystemCallEmulation extends BasicSimulationObject implements SimulationObject {
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

    private static final int MAX_BUFFER_SIZE = 1024;

    private static Map<Integer, SystemCallHandler> handlers = new TreeMap<Integer, SystemCallHandler>();
    private static int errno;

    private static int stackLimit = 0x800000;

    private boolean error;

    private List<OpenFlagMapping> openFlagMappings;

    public SystemCallEmulation(Kernel kernel) {
        super(kernel);

        this.openFlagMappings = new ArrayList<OpenFlagMapping>();
        this.openFlagMappings.add(new OpenFlagMapping(TargetOpenFlags.O_RDONLY, OpenFlags.O_RDONLY));
        this.openFlagMappings.add(new OpenFlagMapping(TargetOpenFlags.O_WRONLY, OpenFlags.O_WRONLY));
        this.openFlagMappings.add(new OpenFlagMapping(TargetOpenFlags.O_RDWR, OpenFlags.O_RDWR));
        this.openFlagMappings.add(new OpenFlagMapping(TargetOpenFlags.O_APPEND, OpenFlags.O_APPEND));
        this.openFlagMappings.add(new OpenFlagMapping(TargetOpenFlags.O_SYNC, OpenFlags.O_SYNC));
        this.openFlagMappings.add(new OpenFlagMapping(TargetOpenFlags.O_CREAT, OpenFlags.O_CREAT));
        this.openFlagMappings.add(new OpenFlagMapping(TargetOpenFlags.O_TRUNC, OpenFlags.O_TRUNC));
        this.openFlagMappings.add(new OpenFlagMapping(TargetOpenFlags.O_EXCL, OpenFlags.O_EXCL));
        this.openFlagMappings.add(new OpenFlagMapping(TargetOpenFlags.O_NOCTTY, OpenFlags.O_NOCTTY));
        this.openFlagMappings.add(new OpenFlagMapping(0x2000, 0));

        registerHandler(new SystemCallHandler(1, "exit") {
            @Override
            public void run(Context context) {
                exit_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(3, "read") {
            @Override
            public void run(Context context) {
                read_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(4, "write") {
            @Override
            public void run(Context context) {
                write_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(5, "open") {
            @Override
            public void run(Context context) {
                open_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(6, "close") {
            @Override
            public void run(Context context) {
                close_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(7, "waitpid") {
            @Override
            public void run(Context context) {
                waitpid_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(20, "getpid") {
            @Override
            public void run(Context context) {
                getpid_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(24, "getuid") {
            @Override
            public void run(Context context) {
                getuid_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(37, "kill") {
            @Override
            public void run(Context context) {
                kill_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(42, "pipe") {
            @Override
            public void run(Context context) {
                pipe_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(45, "brk") {
            @Override
            public void run(Context context) {
                brk_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(47, "getgid") {
            @Override
            public void run(Context context) {
                getgid_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(49, "geteuid") {
            @Override
            public void run(Context context) {
                geteuid_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(50, "getegid") {
            @Override
            public void run(Context context) {
                getegid_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(54, "ioctl") {
            @Override
            public void run(Context context) {
                ioctl_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(64, "getppid") {
            @Override
            public void run(Context context) {
                getppid_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(75, "setrlimit") {
            @Override
            public void run(Context context) {
                setrlimit_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(76, "getrlimit") {
            @Override
            public void run(Context context) {
                getrlimit_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(90, "mmap") {
            @Override
            public void run(Context context) {
                mmap_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(91, "munmap") {
            @Override
            public void run(Context context) {
                munmap_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(120, "clone") {
            @Override
            public void run(Context context) {
                clone_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(122, "uname") {
            @Override
            public void run(Context context) {
                uname_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(125, "mprotect") {
            @Override
            public void run(Context context) {
                mprotect_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(140, "_llseek") {
            @Override
            public void run(Context context) {
                _llseek_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(153, "_sysctl") {
            @Override
            public void run(Context context) {
                _sysctl_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(166, "nanosleep") {
            @Override
            public void run(Context context) {
                nanosleep_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(167, "mremap") {
            @Override
            public void run(Context context) {
                mremap_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(188, "poll") {
            @Override
            public void run(Context context) {
                poll_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(194, "rt_sigaction") {
            @Override
            public void run(Context context) {
                rt_sigaction_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(195, "rt_sigprocmask") {
            @Override
            public void run(Context context) {
                rt_sigprocmask_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(199, "rt_sigsuspend") {
            @Override
            public void run(Context context) {
                rt_sigsuspend_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(215, "fstat64") {
            @Override
            public void run(Context context) {
                fstat64_impl(context);
            }
        });
        registerHandler(new SystemCallHandler(246, "exit_group") {
            @Override
            public void run(Context context) {
                exit_group_impl(context);
            }
        });
    }

    public void doSystemCall(int callNum, Context context) {
        int systemCallIndex = callNum - 4000;

        if (!findAndCallSystemCallHandler(systemCallIndex, context)) {
            throw new RuntimeException(String.format("ctx-%d: system call %d (%d) not implemented", context.getId(), callNum, systemCallIndex));
        }
    }

    private boolean checkSystemCallError(Context context) {
        if (context.getRegs().getGpr(ArchitecturalRegisterFile.REG_V0) != -1) {
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
            return false;
        } else {
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, errno);
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 1);
            return true;
        }
    }

    private boolean findAndCallSystemCallHandler(int systemCallIndex, Context context) {
        if (handlers.containsKey(systemCallIndex)) {
            SystemCallHandler systemCallHandler = handlers.get(systemCallIndex);

            systemCallHandler.run(context);

            this.getBlockingEventDispatcher().dispatch(new SystemCallExecutedEvent(context, systemCallHandler.getName()));

//            Logger.infof(Logger.SYSTEM_CALL, "%s 0x%08x: system call %s (a0=0x%08x, a1=0x%08x, a2=0x%08x); %s", context.getThread().getName(), context.getRegs().getPc(), systemCallHandler.getName(),
//                    context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0),
//                    context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1),
//                    context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2),
//                    systemCallHandler.isError() ? String.format("errorno=0x%08x", errno) : String.format("ret=0x%08x", context.getRegs().getGpr(ArchitecturalRegisterFile.REG_V0)));

//            System.out.printf("%s\n", context.getRegs().dump());

            context.getKernel().processSystemEvents();
            context.getKernel().processSignals();

            return true;
        }

        return false;
    }

    private void registerHandler(SystemCallHandler handler) {
        handlers.put(handler.getIndex(), handler);
    }


    private void exit_group_impl(Context context) {
        context.finish();
    }

    public static FileDescriptor getFD(int fd) {
        try {
            return new FileInputStream("/proc/self/fd/" + fd).getFD();
        } catch (IOException e) {
            return null;
        }
    }

    private void fstat64_impl(Context context) {
        int fd = context.getProcess().translateFileDescriptor(context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0));
        int bufAddr = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);

        FileStat fstat = PosixUtil.current().fstat(fd);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);

        error = checkSystemCallError(context);

        if (!error) {
            int sizeOfDataToWrite = 104;
            byte[] dataToWrite = new byte[sizeOfDataToWrite];

            ByteBuffer bb = ByteBuffer.wrap(dataToWrite).order(context.getProcess().isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

            bb.putInt(0, (int) fstat.dev());
            bb.putInt(16, (int) fstat.ino());
            bb.putInt(24, fstat.mode());
            bb.putInt(28, fstat.nlink());
            bb.putInt(32, fstat.uid());
            bb.putInt(36, fstat.gid());
            bb.putInt(40, (int) fstat.rdev());
            bb.putLong(56, fstat.st_size());
            bb.putInt(64, (int) fstat.atime());
            bb.putInt(72, (int) fstat.mtime());
            bb.putInt(80, (int) fstat.ctime());
            bb.putInt(88, (int) fstat.blockSize());
            bb.putLong(96, fstat.blocks());

            context.getProcess().getMemory().writeBlock(bufAddr, sizeOfDataToWrite, dataToWrite);
        }
    }

    private void rt_sigsuspend_impl(Context context) {
        int pmask = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);

        if (pmask == 0) {
            throw new IllegalArgumentException("sigsuspend with mask being null");
        }

        try {
            context.getSignalMasks().setBackup((SignalMask) context.getSignalMasks().getBlocked().clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        context.getSignalMasks().getBlocked().loadFrom(context.getProcess().getMemory(), pmask);
        context.suspend();

        SigSuspendEvent e = new SigSuspendEvent(context);
        context.getKernel().scheduleSystemEvent(e);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    private void rt_sigprocmask_impl(Context context) {
        int how = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int pset = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);
        int poset = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2);

        if (poset != 0) {
            context.getSignalMasks().getBlocked().saveTo(context.getProcess().getMemory(), poset);
        }

        if (pset != 0) {
            SignalMask set = new SignalMask();
            set.loadFrom(context.getProcess().getMemory(), pset);

            if (how == 1) {
                for (int i = 1; i <= Kernel.MAX_SIGNAL; i++) {
                    if (set.contains(i)) {
                        context.getSignalMasks().getBlocked().set(i);
                    }
                }
            } else if (how == 2) {
                for (int i = 1; i <= Kernel.MAX_SIGNAL; i++) {
                    if (set.contains(i)) {
                        context.getSignalMasks().getBlocked().clear(i);
                    }
                }
            } else if (how == 3) {
                try {
                    context.getSignalMasks().setBlocked((SignalMask) set.clone());
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    private void rt_sigaction_impl(Context context) {
        int signum = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int pact = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);
        int poact = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2);

        if (poact != 0) {
            context.getKernel().getSignalActions().get(signum - 1).saveTo(context.getProcess().getMemory(), poact);
        }
        if (pact != 0) {
            context.getKernel().getSignalActions().get(signum - 1).loadFrom(context.getProcess().getMemory(), pact);
        }

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    private void poll_impl(Context context) {
        int pufds = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int nfds = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);
        int timeout = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2);
        if (nfds < 1) {
            throw new IllegalArgumentException("system call poll: nfds < 1");
        }

        for (int i = 0; i < nfds; i++, pufds += 8) {
            int fd = context.getProcess().getMemory().readWord(pufds);
            short events = context.getProcess().getMemory().readHalfWord(pufds + 4);
            if (events != 1) {
                throw new IllegalArgumentException("system call poll: ufds.events != POLLIN");
            }

            PollEvent e = new PollEvent(context);
            e.getTimeCriterion().setWhen(NativeSystemCalls.clock(context.getKernel().getCurrentCycle()) + timeout * NativeSystemCalls.CLOCKS_PER_SEC / 1000);
            e.getWaitFdCriterion().setBuffer(context.getKernel().getReadBuffer(fd));
            if (e.getWaitFdCriterion().getBuffer() == null) {
                throw new IllegalArgumentException("system call poll: fd does not belong to a pipe read buffer");
            }
            e.getWaitFdCriterion().setPufds(pufds);
            context.getKernel().scheduleSystemEvent(e);
        }
        context.suspend();
    }

    private void nanosleep_impl(Context context) {
        int preq = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int sec = context.getProcess().getMemory().readWord(preq);
        int nsec = context.getProcess().getMemory().readWord(preq + 4);

        int total = (int) (sec * NativeSystemCalls.CLOCKS_PER_SEC + nsec / 1e9 * NativeSystemCalls.CLOCKS_PER_SEC);

        ResumeEvent e = new ResumeEvent(context);
        e.getTimeCriterion().setWhen(NativeSystemCalls.clock(context.getKernel().getCurrentCycle()) + total);
        context.getKernel().scheduleSystemEvent(e);
        context.suspend();
    }

    private void mremap_impl(Context context) {
        int oldAddr = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int oldSize = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);
        int newSize = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2);

        int start = context.getProcess().getMemory().remap(oldAddr, oldSize, newSize);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, start);

        error = checkSystemCallError(context);
    }

    private void _sysctl_impl(Context context) {
        byte[] buf = context.getProcess().getMemory().readBlock(context.getRegs().getGpr(4), 4 * 6);
        ByteBuffer bb = ByteBuffer.wrap(buf).order(context.getProcess().isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

        SysctrlArgs args = new SysctrlArgs();
        args.name = bb.getInt();
        args.nlen = bb.getInt();
        args.oldval = bb.getInt();
        args.oldlenp = bb.getInt();
        args.newval = bb.getInt();
        args.newlen = bb.getInt();

        byte[] buf2 = context.getProcess().getMemory().readBlock(args.name, 4 * 10);
        ByteBuffer bb2 = ByteBuffer.wrap(buf2).order(context.getProcess().isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

        int[] name = new int[10];
        for (int i = 0; i < name.length; i++) {
            name[i] = bb2.getInt();
        }

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);

        name[0] = 0; //TODO: hack for the moment

        if (name[0] != 0) {
            throw new RuntimeException("system call sysctl still not implemented for name[0] != 0");
        }
    }

    private void _llseek_impl(Context context) {
        int fd = context.getProcess().translateFileDescriptor(context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0));
        int offset = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);
        int whence = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2);

        int ret = NativeSystemCalls.LIBC.lseek(fd, offset, whence);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, ret);

        error = checkSystemCallError(context);
    }

    private void mprotect_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    private void uname_impl(Context context) {
        Utsname un = new Utsname();
        un.sysname = "Linux";
        un.nodename = "sim";
        un.release = "2.6";
        un.version = "Tue Apr 5 12:21:57 UTC 2005";
        un.machine = "mips";

        byte[] un_buf = un.getBytes(context.getProcess().isLittleEndian());
        context.getProcess().getMemory().writeBlock(context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0), un_buf.length, un_buf);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    private void clone_impl(Context context) {
        int cloneFlags = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int newsp = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);

        Context newContext;
        try {
            newContext = new Context(context, (ArchitecturalRegisterFile) context.getRegs().clone(), cloneFlags & 0xff);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        if (!context.getKernel().map(newContext, new Predicate<Integer>() {
            public boolean apply(Integer candidateThreadId) {
                return true;
            }
        })) {
            throw new RuntimeException();
        }

        context.getKernel().getContexts().add(newContext);

        newContext.getRegs().setGpr(ArchitecturalRegisterFile.REG_SP, newsp);
        newContext.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        newContext.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, newContext.getPid());
    }

    private void munmap_impl(Context context) {
        int start = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int length = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);

        context.getProcess().getMemory().unmap(start, length);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    private void mmap_impl(Context context) {
        int start = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int length = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);

        int fd = context.getProcess().getMemory().readWord(context.getRegs().getGpr(ArchitecturalRegisterFile.REG_SP) + 16);

        if (fd != -1) {
            throw new IllegalArgumentException("system call mmap: system call is only supported with fd=-1");
        }

        if (start == 0) {
            start = context.getProcess().getHeapTop();
        }
        start = context.getProcess().getMemory().map(start, length);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, start);
    }

    private void getrlimit_impl(Context context) {
        int prlimit = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);

        if (context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0) != 3) {
            throw new RuntimeException();
        }

        context.getProcess().getMemory().writeWord(prlimit, stackLimit);
        context.getProcess().getMemory().writeWord(prlimit + 4, 0xffffffff);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    private void setrlimit_impl(Context context) {
        if (context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0) != 3) {
            throw new RuntimeException();
        }

        stackLimit = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    private void getppid_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, context.getPpid());
        error = checkSystemCallError(context);
    }

    private void ioctl_impl(Context context) {
        byte[] buf = new byte[128];

        if (context.getRegs().getGpr(6) != 0) {
            buf = context.getProcess().getMemory().readBlock(context.getRegs().getGpr(6), 128);
        }
        int fd = context.getProcess().translateFileDescriptor(context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0));
        if (fd < 3) {
            context.getRegs().setGpr(2, NativeSystemCalls.LIBC.ioctl(fd, context.getRegs().getGpr(5), buf)); //TODO: retrive errno value from libc call

            error = checkSystemCallError(context);
            if (context.getRegs().getGpr(6) != 0) {
                context.getProcess().getMemory().writeBlock(context.getRegs().getGpr(6), 128, buf);
            }
        } else {
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
        }
    }

    private void getegid_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, context.getEgid());
        error = checkSystemCallError(context);
    }

    private void geteuid_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, context.getEuid());
        error = checkSystemCallError(context);
    }

    private void getgid_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, context.getGid());
        error = checkSystemCallError(context);
    }

    private void brk_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    private void pipe_impl(Context context) {
        int[] fileDescriptors = new int[2];
        context.getKernel().createPipe(fileDescriptors);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, fileDescriptors[0]);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V1, fileDescriptors[1]);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);

        //            System.out.printf("\tpipe created with fd={%d, %d}\n", fd[0], fd[1]);
    }

    private void kill_impl(Context context) {
        int pid = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int sig = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);
        if (pid < 1) {
            throw new RuntimeException();
        }

        Context destContext = context.getKernel().getContextFromPid(pid);
        if (destContext == null) {
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, ESRCH);
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, -1);
            return;
        }

        destContext.getSignalMasks().getPending().set(sig);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);

        //            System.out.printf("\t%s: sending signal %d to %s (pid=%d)\n", context.getThread().getName(), sig, destContext.getThread().getName(), pid);
    }

    private void getuid_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, context.getUid());
        error = checkSystemCallError(context);
    }

    private void getpid_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, context.getPid());
        error = checkSystemCallError(context);
    }

    private void waitpid_impl(Context context) {
        int pid = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int pstatus = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);

        if (pid < 1) {
            throw new RuntimeException();
        }

        if (context.getKernel().getContextFromPid(pid) == null) {
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, ECHILD);
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, -1);
            return;
        }

        WaitEvent e = new WaitEvent(context, pid);
        context.getKernel().scheduleSystemEvent(e);
        context.suspend();
        if (pstatus != 0) {
            throw new RuntimeException();
        }
    }

    private void close_impl(Context context) {
        int fd = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);

        if (fd == 0 || fd == 1 || fd == 2) {
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
            return;
        }

        int ret = NativeSystemCalls.LIBC.close(fd);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, ret);
        error = checkSystemCallError(context);
    }

    private void open_impl(Context context) {
        int addr = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int tgtFlags = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);
        int mode = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2);

        int hostFlags = 0;
        for (OpenFlagMapping mapping : this.openFlagMappings) {
            if ((tgtFlags & mapping.getTargetFlag()) != 0) {
                tgtFlags &= ~mapping.getTargetFlag();
                hostFlags |= mapping.getHostFlag();
            }
        }

        if (tgtFlags != 0) {
            Logger.fatalf(Logger.SYSTEM_CALL, "system call open: cannot decode flags 0x%08x", context.getCycleAccurateEventQueue().getCurrentCycle(), tgtFlags);
        }

        String path = context.getProcess().getMemory().readString(addr, MAX_BUFFER_SIZE);

        int ret = NativeSystemCalls.LIBC.open(path, hostFlags, mode);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, ret);
        error = checkSystemCallError(context);
    }

    private void write_impl(Context context) {
        int fd = context.getProcess().translateFileDescriptor(context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0));
        int bufAddr = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);
        int count = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2);

        byte[] buf = context.getProcess().getMemory().readBlock(bufAddr, count);

        int ret;

        CircularByteBuffer buffer = context.getKernel().getWriteBuffer(fd);
        if (buffer != null) {
            buffer.write(buf, 0, count);
            ret = count;
        } else {
            ret = NativeSystemCalls.LIBC.write(fd, buf, count);
        }

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, ret);
        error = checkSystemCallError(context);

        //            System.out.printf("\tsystem call write: %d bytes written to fd=%d\n", ret, fd);
    }

    private void read_impl(Context context) {
        int readMaxSize = 1 << 25;

        int fd = context.getProcess().translateFileDescriptor(context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0));
        int bufAddr = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);
        int count = Math.min(readMaxSize, context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2));

        int ret;
        byte[] buf;

        CircularByteBuffer buffer = context.getKernel().getReadBuffer(fd);
        if (buffer != null) {
            if (buffer.isEmpty()) {
                ReadEvent e = new ReadEvent(context);
                e.getWaitFdCriterion().setBuffer(buffer);
                e.getWaitFdCriterion().setAddress(bufAddr);
                e.getWaitFdCriterion().setSize(count);
                context.getKernel().scheduleSystemEvent(e);
                context.suspend();
                return;
            } else {
                buf = new byte[count];
                ret = buffer.read(buf, 0, count);
            }
        } else {
            buf = new byte[count];
            ret = NativeSystemCalls.LIBC.read(fd, buf, count);
        }

        if (ret >= readMaxSize) {
            throw new RuntimeException();
        }

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, ret);
        this.error = checkSystemCallError(context);

        context.getProcess().getMemory().writeBlock(bufAddr, ret, buf);

        //            System.out.printf("\tsystem call read: %d bytes read from fd=%d\n", ret, fd);
    }

    private void exit_impl(Context context) {
        context.finish();
    }

    private class SysctrlArgs {
        private int name;
        private int nlen;
        private int oldval;
        private int oldlenp;
        private int newval;
        private int newlen;
    }
}
