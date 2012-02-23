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

import archimulator.model.base.Logger;
import archimulator.sim.isa.ArchitecturalRegisterFile;
import archimulator.sim.os.event.*;
import archimulator.sim.os.signal.SignalMask;
import archimulator.util.action.Predicate;
import archimulator.util.io.buffer.CircularByteBuffer;
import org.jruby.ext.posix.FileStat;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class BasicSyscallEmulation extends SyscallEmulation {
    private static final int MAX_BUFFER_SIZE = 1024;

    private final List<OpenFlagMapping> openFlagMappings;

    private static int stackLimit = 0x800000;

    public BasicSyscallEmulation(Kernel kernel) {
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
    }

    @Override
    protected void exit_group_impl(Context context) {
        context.finish();
    }
    
    public static FileDescriptor getFD(int fd) {
        try {
            return new FileInputStream("/proc/self/fd/" + fd).getFD();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void fstat64_impl(Context context) { //TODO: to be implemented in Archimulator.
        int fd = context.getProcess().translateFileDescriptor(context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0));
        int bufAddr = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);

        FileStat fstat = PosixUtil.current().fstat(fd);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);

        error = checkSyscallError(context);

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

    @Override
    protected void rt_sigsuspend_impl(Context context) {
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

    @Override
    protected void rt_sigprocmask_impl(Context context) {
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

    @Override
    protected void rt_sigaction_impl(Context context) {
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

    @Override
    protected void poll_impl(Context context) {
        int pufds = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int nfds = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);
        int timeout = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2);
        if (nfds < 1) {
            throw new IllegalArgumentException("syscall poll: nfds < 1");
        }

        for (int i = 0; i < nfds; i++, pufds += 8) {
            int fd = context.getProcess().getMemory().readWord(pufds);
            short events = context.getProcess().getMemory().readHalfWord(pufds + 4);
            if (events != 1) {
                throw new IllegalArgumentException("syscall poll: ufds.events != POLLIN");
            }

            PollEvent e = new PollEvent(context);
            e.getTimeCriterion().setWhen(NativeSyscalls.clock(context.getKernel().getCurrentCycle()) + timeout * NativeSyscalls.CLOCKS_PER_SEC / 1000);
            e.getWaitFdCriterion().setBuffer(context.getKernel().getReadBuffer(fd));
            if (e.getWaitFdCriterion().getBuffer() == null) {
                throw new IllegalArgumentException("syscall poll: fd does not belong to a pipe read buffer");
            }
            e.getWaitFdCriterion().setPufds(pufds);
            context.getKernel().scheduleSystemEvent(e);
        }
        context.suspend();
    }

    @Override
    protected void nanosleep_impl(Context context) {
        int preq = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int sec = context.getProcess().getMemory().readWord(preq);
        int nsec = context.getProcess().getMemory().readWord(preq + 4);

        int total = (int) (sec * NativeSyscalls.CLOCKS_PER_SEC + nsec / 1e9 * NativeSyscalls.CLOCKS_PER_SEC);

        ResumeEvent e = new ResumeEvent(context);
        e.getTimeCriterion().setWhen(NativeSyscalls.clock(context.getKernel().getCurrentCycle()) + total);
        context.getKernel().scheduleSystemEvent(e);
        context.suspend();
    }

    @Override
    protected void mremap_impl(Context context) {
        int oldAddr = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int oldSize = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);
        int newSize = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2);

        int start = context.getProcess().getMemory().remap(oldAddr, oldSize, newSize);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, start);

        error = checkSyscallError(context);
    }

    @Override
    protected void _sysctl_impl(Context context) {
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
            throw new RuntimeException("syscall sysctl still not implemented for name[0] != 0");
        }
    }

    @Override
    protected void _llseek_impl(Context context) {
        int fd = context.getProcess().translateFileDescriptor(context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0));
        int offset = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);
        int whence = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A2);

        int ret = NativeSyscalls.LIBC.lseek(fd, offset, whence);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, ret);

        error = checkSyscallError(context);
    }

    @Override
    protected void mprotect_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    @Override
    protected void uname_impl(Context context) {
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

    @Override
    protected void clone_impl(Context context) {
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

    @Override
    protected void munmap_impl(Context context) {
        int start = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int length = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);

        context.getProcess().getMemory().unmap(start, length);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    @Override
    protected void mmap_impl(Context context) {
        int start = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);
        int length = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);

        int fd = context.getProcess().getMemory().readWord(context.getRegs().getGpr(ArchitecturalRegisterFile.REG_SP) + 16);

        if (fd != -1) {
            throw new IllegalArgumentException("syscall mmap: system call is only supported with fd=-1");
        }

        if (start == 0) {
            start = context.getProcess().getHeapTop();
        }
        start = context.getProcess().getMemory().map(start, length);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, start);
    }

    @Override
    protected void getrlimit_impl(Context context) {
        int prlimit = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);

        if (context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0) != 3) {
            throw new RuntimeException();
        }

        context.getProcess().getMemory().writeWord(prlimit, stackLimit);
        context.getProcess().getMemory().writeWord(prlimit + 4, 0xffffffff);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    @Override
    protected void setrlimit_impl(Context context) {
        if (context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0) != 3) {
            throw new RuntimeException();
        }

        stackLimit = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A1);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    @Override
    protected void getppid_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, context.getPpid());
        error = checkSyscallError(context);
    }

    @Override
    protected void ioctl_impl(Context context) {
        byte[] buf = new byte[128];

        if (context.getRegs().getGpr(6) != 0) {
            buf = context.getProcess().getMemory().readBlock(context.getRegs().getGpr(6), 128);
        }
        int fd = context.getProcess().translateFileDescriptor(context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0));
        if (fd < 3) {
            context.getRegs().setGpr(2, NativeSyscalls.LIBC.ioctl(fd, context.getRegs().getGpr(5), buf)); //TODO: retrive errno value from libc call

            error = checkSyscallError(context);
            if (context.getRegs().getGpr(6) != 0) {
                context.getProcess().getMemory().writeBlock(context.getRegs().getGpr(6), 128, buf);
            }
        } else {
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
        }
    }

    @Override
    protected void getegid_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, context.getEgid());
        error = checkSyscallError(context);
    }

    @Override
    protected void geteuid_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, context.getEuid());
        error = checkSyscallError(context);
    }

    @Override
    protected void getgid_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, context.getGid());
        error = checkSyscallError(context);
    }

    @Override
    protected void brk_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, 0);
    }

    @Override
    protected void pipe_impl(Context context) {
        int[] fileDescriptors = new int[2];
        context.getKernel().createPipe(fileDescriptors);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, fileDescriptors[0]);
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V1, fileDescriptors[1]);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);

        //            System.out.printf("\tpipe created with fd={%d, %d}\n", fd[0], fd[1]);
    }

    @Override
    protected void kill_impl(Context context) {
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

    @Override
    protected void getuid_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, context.getUid());
        error = checkSyscallError(context);
    }

    @Override
    protected void getpid_impl(Context context) {
        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, context.getPid());
        error = checkSyscallError(context);
    }

    @Override
    protected void waitpid_impl(Context context) {
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

    @Override
    protected void close_impl(Context context) {
        int fd = context.getRegs().getGpr(ArchitecturalRegisterFile.REG_A0);

        if (fd == 0 || fd == 1 || fd == 2) {
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A3, 0);
            return;
        }

        int ret = NativeSyscalls.LIBC.close(fd);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, ret);
        error = checkSyscallError(context);
    }

    @Override
    protected void open_impl(Context context) {
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
            context.getLogger().fatalf(Logger.SYSCALL, "Syscall (open): cannot decode flags 0x%08x", tgtFlags);
        }

        String path = context.getProcess().getMemory().readString(addr, MAX_BUFFER_SIZE);

        int ret = NativeSyscalls.LIBC.open(path, hostFlags, mode);

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, ret);
        error = checkSyscallError(context);
    }

    @Override
    protected void write_impl(Context context) {
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
            ret = NativeSyscalls.LIBC.write(fd, buf, count);
        }

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, ret);
        error = checkSyscallError(context);

        //            System.out.printf("\tsyscall write: %d bytes written to fd=%d\n", ret, fd);
    }

    @Override
    protected void read_impl(Context context) {
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
            ret = NativeSyscalls.LIBC.read(fd, buf, count);
        }

        if (ret >= readMaxSize) {
            throw new RuntimeException();
        }

        context.getRegs().setGpr(ArchitecturalRegisterFile.REG_V0, ret);
        this.error = checkSyscallError(context);

        context.getProcess().getMemory().writeBlock(bufAddr, ret, buf);

        //            System.out.printf("\tsyscall read: %d bytes read from fd=%d\n", ret, fd);
    }

    @Override
    protected void exit_impl(Context context) {
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
