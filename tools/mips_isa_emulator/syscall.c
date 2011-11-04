#include <sys/types.h>
#include <sys/stat.h>
#include <sys/utsname.h>
#include <sys/uio.h>
#include <sys/mman.h>
#include <sys/resource.h>
#include <sys/times.h>
#include <sys/ioctl.h>
#include <sys/sysctl.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <errno.h>
#include <stdio.h>
#include <unistd.h>
#include <time.h>
#include <signal.h>
#include <assert.h>
#include <sched.h>
#include <termios.h>

#include "buffer.h"
#include "mhandle.h"
#include "private.h"


#define NR_Linux			4000
#define DEF_SYSCALLS                               \
	DEF_SYSCALL(syscall,                 0   ) \
	DEF_SYSCALL(exit,                    1   ) \
	DEF_SYSCALL(fork,                    2   ) \
	DEF_SYSCALL(read,                    3   ) \
	DEF_SYSCALL(write,                   4   ) \
	DEF_SYSCALL(open,                    5   ) \
	DEF_SYSCALL(close,                   6   ) \
	DEF_SYSCALL(waitpid,                 7   ) \
	DEF_SYSCALL(creat,                   8   ) \
	DEF_SYSCALL(link,                    9   ) \
	DEF_SYSCALL(unlink,                  10  ) \
	DEF_SYSCALL(execve,                  11  ) \
	DEF_SYSCALL(chdir,                   12  ) \
	DEF_SYSCALL(time,                    13  ) \
	DEF_SYSCALL(mknod,                   14  ) \
	DEF_SYSCALL(chmod,                   15  ) \
	DEF_SYSCALL(lchown,                  16  ) \
	DEF_SYSCALL(break,                   17  ) \
	DEF_SYSCALL(unused18,                18  ) \
	DEF_SYSCALL(lseek,                   19  ) \
	DEF_SYSCALL(getpid,                  20  ) \
	DEF_SYSCALL(mount,                   21  ) \
	DEF_SYSCALL(umount,                  22  ) \
	DEF_SYSCALL(setuid,                  23  ) \
	DEF_SYSCALL(getuid,                  24  ) \
	DEF_SYSCALL(stime,                   25  ) \
	DEF_SYSCALL(ptrace,                  26  ) \
	DEF_SYSCALL(alarm,                   27  ) \
	DEF_SYSCALL(unused28,                28  ) \
	DEF_SYSCALL(pause,                   29  ) \
	DEF_SYSCALL(utime,                   30  ) \
	DEF_SYSCALL(stty,                    31  ) \
	DEF_SYSCALL(gtty,                    32  ) \
	DEF_SYSCALL(access,                  33  ) \
	DEF_SYSCALL(nice,                    34  ) \
	DEF_SYSCALL(ftime,                   35  ) \
	DEF_SYSCALL(sync,                    36  ) \
	DEF_SYSCALL(kill,                    37  ) \
	DEF_SYSCALL(rename,                  38  ) \
	DEF_SYSCALL(mkdir,                   39  ) \
	DEF_SYSCALL(rmdir,                   40  ) \
	DEF_SYSCALL(dup,                     41  ) \
	DEF_SYSCALL(pipe,                    42  ) \
	DEF_SYSCALL(times,                   43  ) \
	DEF_SYSCALL(prof,                    44  ) \
	DEF_SYSCALL(brk,                     45  ) \
	DEF_SYSCALL(setgid,                  46  ) \
	DEF_SYSCALL(getgid,                  47  ) \
	DEF_SYSCALL(signal,                  48  ) \
	DEF_SYSCALL(geteuid,                 49  ) \
	DEF_SYSCALL(getegid,                 50  ) \
	DEF_SYSCALL(acct,                    51  ) \
	DEF_SYSCALL(umount2,                 52  ) \
	DEF_SYSCALL(lock,                    53  ) \
	DEF_SYSCALL(ioctl,                   54  ) \
	DEF_SYSCALL(fcntl,                   55  ) \
	DEF_SYSCALL(mpx,                     56  ) \
	DEF_SYSCALL(setpgid,                 57  ) \
	DEF_SYSCALL(ulimit,                  58  ) \
	DEF_SYSCALL(unused59,                59  ) \
	DEF_SYSCALL(umask,                   60  ) \
	DEF_SYSCALL(chroot,                  61  ) \
	DEF_SYSCALL(ustat,                   62  ) \
	DEF_SYSCALL(dup2,                    63  ) \
	DEF_SYSCALL(getppid,                 64  ) \
	DEF_SYSCALL(getpgrp,                 65  ) \
	DEF_SYSCALL(setsid,                  66  ) \
	DEF_SYSCALL(sigaction,               67  ) \
	DEF_SYSCALL(sgetmask,                68  ) \
	DEF_SYSCALL(ssetmask,                69  ) \
	DEF_SYSCALL(setreuid,                70  ) \
	DEF_SYSCALL(setregid,                71  ) \
	DEF_SYSCALL(sigsuspend,              72  ) \
	DEF_SYSCALL(sigpending,              73  ) \
	DEF_SYSCALL(sethostname,             74  ) \
	DEF_SYSCALL(setrlimit,               75  ) \
	DEF_SYSCALL(getrlimit,               76  ) \
	DEF_SYSCALL(getrusage,               77  ) \
	DEF_SYSCALL(gettimeofday,            78  ) \
	DEF_SYSCALL(settimeofday,            79  ) \
	DEF_SYSCALL(getgroups,               80  ) \
	DEF_SYSCALL(setgroups,               81  ) \
	DEF_SYSCALL(reserved82,              82  ) \
	DEF_SYSCALL(symlink,                 83  ) \
	DEF_SYSCALL(unused84,                84  ) \
	DEF_SYSCALL(readlink,                85  ) \
	DEF_SYSCALL(uselib,                  86  ) \
	DEF_SYSCALL(swapon,                  87  ) \
	DEF_SYSCALL(reboot,                  88  ) \
	DEF_SYSCALL(readdir,                 89  ) \
	DEF_SYSCALL(mmap,                    90  ) \
	DEF_SYSCALL(munmap,                  91  ) \
	DEF_SYSCALL(truncate,                92  ) \
	DEF_SYSCALL(ftruncate,               93  ) \
	DEF_SYSCALL(fchmod,                  94  ) \
	DEF_SYSCALL(fchown,                  95  ) \
	DEF_SYSCALL(getpriority,             96  ) \
	DEF_SYSCALL(setpriority,             97  ) \
	DEF_SYSCALL(profil,                  98  ) \
	DEF_SYSCALL(statfs,                  99  ) \
	DEF_SYSCALL(fstatfs,                 100 ) \
	DEF_SYSCALL(ioperm,                  101 ) \
	DEF_SYSCALL(socketcall,              102 ) \
	DEF_SYSCALL(syslog,                  103 ) \
	DEF_SYSCALL(setitimer,               104 ) \
	DEF_SYSCALL(getitimer,               105 ) \
	DEF_SYSCALL(stat,                    106 ) \
	DEF_SYSCALL(lstat,                   107 ) \
	DEF_SYSCALL(fstat,                   108 ) \
	DEF_SYSCALL(unused109,               109 ) \
	DEF_SYSCALL(iopl,                    110 ) \
	DEF_SYSCALL(vhangup,                 111 ) \
	DEF_SYSCALL(idle,                    112 ) \
	DEF_SYSCALL(vm86,                    113 ) \
	DEF_SYSCALL(wait4,                   114 ) \
	DEF_SYSCALL(swapoff,                 115 ) \
	DEF_SYSCALL(sysinfo,                 116 ) \
	DEF_SYSCALL(ipc,                     117 ) \
	DEF_SYSCALL(fsync,                   118 ) \
	DEF_SYSCALL(sigreturn,               119 ) \
	DEF_SYSCALL(clone,                   120 ) \
	DEF_SYSCALL(setdomainname,           121 ) \
	DEF_SYSCALL(uname,                   122 ) \
	DEF_SYSCALL(modify_ldt,              123 ) \
	DEF_SYSCALL(adjtimex,                124 ) \
	DEF_SYSCALL(mprotect,                125 ) \
	DEF_SYSCALL(sigprocmask,             126 ) \
	DEF_SYSCALL(create_module,           127 ) \
	DEF_SYSCALL(init_module,             128 ) \
	DEF_SYSCALL(delete_module,           129 ) \
	DEF_SYSCALL(get_kernel_syms,         130 ) \
	DEF_SYSCALL(quotactl,                131 ) \
	DEF_SYSCALL(getpgid,                 132 ) \
	DEF_SYSCALL(fchdir,                  133 ) \
	DEF_SYSCALL(bdflush,                 134 ) \
	DEF_SYSCALL(sysfs,                   135 ) \
	DEF_SYSCALL(personality,             136 ) \
	DEF_SYSCALL(afs_syscall,             137 ) \
	DEF_SYSCALL(setfsuid,                138 ) \
	DEF_SYSCALL(setfsgid,                139 ) \
	DEF_SYSCALL(_llseek,                 140 ) \
	DEF_SYSCALL(getdents,                141 ) \
	DEF_SYSCALL(_newselect,              142 ) \
	DEF_SYSCALL(flock,                   143 ) \
	DEF_SYSCALL(msync,                   144 ) \
	DEF_SYSCALL(readv,                   145 ) \
	DEF_SYSCALL(writev,                  146 ) \
	DEF_SYSCALL(cacheflush,              147 ) \
	DEF_SYSCALL(cachectl,                148 ) \
	DEF_SYSCALL(sysmips,                 149 ) \
	DEF_SYSCALL(unused150,               150 ) \
	DEF_SYSCALL(getsid,                  151 ) \
	DEF_SYSCALL(fdatasync,               152 ) \
	DEF_SYSCALL(_sysctl,                 153 ) \
	DEF_SYSCALL(mlock,                   154 ) \
	DEF_SYSCALL(munlock,                 155 ) \
	DEF_SYSCALL(mlockall,                156 ) \
	DEF_SYSCALL(munlockall,              157 ) \
	DEF_SYSCALL(sched_setparam,          158 ) \
	DEF_SYSCALL(sched_getparam,          159 ) \
	DEF_SYSCALL(sched_setscheduler,      160 ) \
	DEF_SYSCALL(sched_getscheduler,      161 ) \
	DEF_SYSCALL(sched_yield,             162 ) \
	DEF_SYSCALL(sched_get_priority_max,  163 ) \
	DEF_SYSCALL(sched_get_priority_min,  164 ) \
	DEF_SYSCALL(sched_rr_get_interval,   165 ) \
	DEF_SYSCALL(nanosleep,               166 ) \
	DEF_SYSCALL(mremap,                  167 ) \
	DEF_SYSCALL(accept,                  168 ) \
	DEF_SYSCALL(bind,                    169 ) \
	DEF_SYSCALL(connect,                 170 ) \
	DEF_SYSCALL(getpeername,             171 ) \
	DEF_SYSCALL(getsockname,             172 ) \
	DEF_SYSCALL(getsockopt,              173 ) \
	DEF_SYSCALL(listen,                  174 ) \
	DEF_SYSCALL(recv,                    175 ) \
	DEF_SYSCALL(recvfrom,                176 ) \
	DEF_SYSCALL(recvmsg,                 177 ) \
	DEF_SYSCALL(send,                    178 ) \
	DEF_SYSCALL(sendmsg,                 179 ) \
	DEF_SYSCALL(sendto,                  180 ) \
	DEF_SYSCALL(setsockopt,              181 ) \
	DEF_SYSCALL(shutdown,                182 ) \
	DEF_SYSCALL(socket,                  183 ) \
	DEF_SYSCALL(socketpair,              184 ) \
	DEF_SYSCALL(setresuid,               185 ) \
	DEF_SYSCALL(getresuid,               186 ) \
	DEF_SYSCALL(query_module,            187 ) \
	DEF_SYSCALL(poll,                    188 ) \
	DEF_SYSCALL(nfsservctl,              189 ) \
	DEF_SYSCALL(setresgid,               190 ) \
	DEF_SYSCALL(getresgid,               191 ) \
	DEF_SYSCALL(prctl,                   192 ) \
	DEF_SYSCALL(rt_sigreturn,            193 ) \
	DEF_SYSCALL(rt_sigaction,            194 ) \
	DEF_SYSCALL(rt_sigprocmask,          195 ) \
	DEF_SYSCALL(rt_sigpending,           196 ) \
	DEF_SYSCALL(rt_sigtimedwait,         197 ) \
	DEF_SYSCALL(rt_sigqueueinfo,         198 ) \
	DEF_SYSCALL(rt_sigsuspend,           199 ) \
	DEF_SYSCALL(pread64,                 200 ) \
	DEF_SYSCALL(pwrite64,                201 ) \
	DEF_SYSCALL(chown,                   202 ) \
	DEF_SYSCALL(getcwd,                  203 ) \
	DEF_SYSCALL(capget,                  204 ) \
	DEF_SYSCALL(capset,                  205 ) \
	DEF_SYSCALL(sigaltstack,             206 ) \
	DEF_SYSCALL(sendfile,                207 ) \
	DEF_SYSCALL(getpmsg,                 208 ) \
	DEF_SYSCALL(putpmsg,                 209 ) \
	DEF_SYSCALL(mmap2,                   210 ) \
	DEF_SYSCALL(truncate64,              211 ) \
	DEF_SYSCALL(ftruncate64,             212 ) \
	DEF_SYSCALL(stat64,                  213 ) \
	DEF_SYSCALL(lstat64,                 214 ) \
	DEF_SYSCALL(fstat64,                 215 ) \
	DEF_SYSCALL(pivot_root,              216 ) \
	DEF_SYSCALL(mincore,                 217 ) \
	DEF_SYSCALL(madvise,                 218 ) \
	DEF_SYSCALL(getdents64,              219 ) \
	DEF_SYSCALL(fcntl64,                 220 ) \
	DEF_SYSCALL(reserved221,             221 ) \
	DEF_SYSCALL(gettid,                  222 ) \
	DEF_SYSCALL(readahead,               223 ) \
	DEF_SYSCALL(setxattr,                224 ) \
	DEF_SYSCALL(lsetxattr,               225 ) \
	DEF_SYSCALL(fsetxattr,               226 ) \
	DEF_SYSCALL(getxattr,                227 ) \
	DEF_SYSCALL(lgetxattr,               228 ) \
	DEF_SYSCALL(fgetxattr,               229 ) \
	DEF_SYSCALL(listxattr,               230 ) \
	DEF_SYSCALL(llistxattr,              231 ) \
	DEF_SYSCALL(flistxattr,              232 ) \
	DEF_SYSCALL(removexattr,             233 ) \
	DEF_SYSCALL(lremovexattr,            234 ) \
	DEF_SYSCALL(fremovexattr,            235 ) \
	DEF_SYSCALL(tkill,                   236 ) \
	DEF_SYSCALL(sendfile64,              237 ) \
	DEF_SYSCALL(futex,                   238 ) \
	DEF_SYSCALL(sched_setaffinity,       239 ) \
	DEF_SYSCALL(sched_getaffinity,       240 ) \
	DEF_SYSCALL(io_setup,                241 ) \
	DEF_SYSCALL(io_destroy,              242 ) \
	DEF_SYSCALL(io_getevents,            243 ) \
	DEF_SYSCALL(io_submit,               244 ) \
	DEF_SYSCALL(io_cancel,               245 ) \
	DEF_SYSCALL(exit_group,              246 ) \
	DEF_SYSCALL(lookup_dcookie,          247 ) \
	DEF_SYSCALL(epoll_create,            248 ) \
	DEF_SYSCALL(epoll_ctl,               249 ) \
	DEF_SYSCALL(epoll_wait,              250 ) \
	DEF_SYSCALL(remap_file_pages,        251 ) \
	DEF_SYSCALL(set_tid_address,         252 ) \
	DEF_SYSCALL(restart_syscall,         253 ) \
	DEF_SYSCALL(fadvise64,               254 ) \
	DEF_SYSCALL(statfs64,                255 ) \
	DEF_SYSCALL(fstatfs64,               256 ) \
	DEF_SYSCALL(timer_create,            257 ) \
	DEF_SYSCALL(timer_settime,           258 ) \
	DEF_SYSCALL(timer_gettime,           259 ) \
	DEF_SYSCALL(timer_getoverrun,        260 ) \
	DEF_SYSCALL(timer_delete,            261 ) \
	DEF_SYSCALL(clock_settime,           262 ) \
	DEF_SYSCALL(clock_gettime,           263 ) \
	DEF_SYSCALL(clock_getres,            264 ) \
	DEF_SYSCALL(clock_nanosleep,         265 ) \
	DEF_SYSCALL(tgkill,                  266 ) \
	DEF_SYSCALL(utimes,                  267 )




#define DEF_SYSCALL(NAME, CODE) #NAME,
static char *syscall_name[] = { DEF_SYSCALLS ""};
#undef DEF_SYSCALL

#define DEF_SYSCALL(NAME, CODE) SIM_SYS_##NAME = (CODE + NR_Linux),
enum {
	DEF_SYSCALLS
	SIM_SYS_MAX
};
#undef DEF_SYSCALL




/* flags de 'open' */
#define SIM_O_RDONLY		0
#define SIM_O_WRONLY		1
#define SIM_O_RDWR		2
#define SIM_O_CREAT		0x100
#define SIM_O_EXCL		0x400
#define SIM_O_NOCTTY		0x800
#define SIM_O_TRUNC		0x200
#define SIM_O_APPEND		8
#define SIM_O_NONBLOCK		0x80
#define SIM_O_SYNC		0x10

/* tabla de traducción de flags */
struct {
	int	sim_flag;
	int	local_flag;
} sim_flag_table[] = {
	{ 0x0000,	O_RDONLY },
	{ 0x0001,	O_WRONLY },
	{ 0x0002,	O_RDWR },
	{ 0x0008,	O_APPEND },
	{ 0x0010,	O_SYNC },
	{ 0x0080,	O_NONBLOCK },
	{ 0x0100,	O_CREAT },
	{ 0x0200,	O_TRUNC },
	{ 0x0400,	O_EXCL },
	{ 0x0800,	O_NOCTTY },
	{ 0x2000,	0 },
};
#define SIM_NFLAGS	(sizeof(sim_flag_table) / sizeof(sim_flag_table[0]))


/* stat, fstat, lstat */
static void struct_stat_write(struct mem_t *mem, word addr, struct stat *buf)
{
	struct sim_stat {
		word sim_dev;
		word sim_pad1[3];
		word sim_ino;
		word sim_mode;
		word sim_nlink;
		word sim_uid;
		word sim_gid;
		word sim_rdev;
		word sim_pad2[2];
		word sim_size;
		word sim_pad3;
		word sim_atime;
		word sim_atime_nsec;
		word sim_mtime;
		word sim_mtime_nsec;
		word sim_ctime;
		word sim_ctime_nsec;
		word sim_blksize;
		word sim_blocks;
		word sim_pad4[14];
	} sim_buf;

	bzero(&sim_buf, sizeof(sim_buf));
	sim_buf.sim_dev = SWAPW(buf->st_dev);
	sim_buf.sim_ino = SWAPW(buf->st_ino);
	sim_buf.sim_mode = SWAPW(buf->st_mode);
	sim_buf.sim_nlink = SWAPW(buf->st_nlink);
	sim_buf.sim_uid = SWAPW(buf->st_uid);
	sim_buf.sim_gid = SWAPW(buf->st_gid);
	sim_buf.sim_rdev = SWAPW(buf->st_rdev);
	sim_buf.sim_size = SWAPW(buf->st_size);
	sim_buf.sim_atime = SWAPW((dword) buf->st_atime >> 32);
	sim_buf.sim_atime_nsec = SWAPW(buf->st_atime);
	sim_buf.sim_mtime = SWAPW((dword) buf->st_mtime >> 32);
	sim_buf.sim_mtime_nsec = SWAPW(buf->st_mtime);
	sim_buf.sim_ctime = SWAPW((dword) buf->st_ctime >> 32);
	sim_buf.sim_ctime_nsec = SWAPW(buf->st_ctime);
	sim_buf.sim_blksize = SWAPW(buf->st_blksize);
	sim_buf.sim_blocks = SWAPW(buf->st_blocks);
	WRITE_BLK(addr, sizeof(sim_buf), &sim_buf);
}


/* getrlimit & getrusage */

#define SIM_RLIMIT_CPU		0
#define SIM_RLIMIT_FSIZE	1
#define SIM_RLIMIT_DATA		2
#define SIM_RLIMIT_STACK	3
#define SIM_RLIMIT_CORE		4

struct sim_rlimit {
	word rlim_cur;
	word rlim_max;
};

static struct sim_rlimit sim_rlim_stack = { 0x800000, 0xffffffff };
static struct sim_rlimit sim_rlim_data = { 0xffffffff, 0xffffffff };

string_map_t rlimit_map = {
	5, {
		{ "RLIMIT_CPU", 0 },
		{ "RLIMIT_FSIZE", 1 },
		{ "RLIMIT_DATA", 2 },
		{ "RLIMIT_STACK", 3 },
		{ "RLIMIT_CORE", 4 }
	}
};

string_map_t rusage_map = {
	3, {
		{ "RUSAGE_SELF", 0 },
		{ "RUSAGE_CHILDREN", -1 },
		{ "RUSAGE_BOTH", -2 }
	}
};

static void sim_rlimit_write(struct mem_t *mem, word addr, struct sim_rlimit *sim_rlim)
{
	WRITE_WORD(addr, sim_rlim->rlim_cur);
	WRITE_WORD(addr + 4, sim_rlim->rlim_max);
}

static void sim_rlimit_read(struct mem_t *mem, word addr, struct sim_rlimit *sim_rlim)
{
	READ_WORD(addr, &sim_rlim->rlim_cur);
	READ_WORD(addr + 4, &sim_rlim->rlim_max);
}


/* errno */
string_map_t errno_map = {
	34, {
		{ "EPERM",	1 },
		{ "ENOENT",	2 },
		{ "ESRCH",	3 },
		{ "EINTR",	4 },
		{ "EIO",	5 },
		{ "ENXIO",	6 },
		{ "E2BIG",	7 },
		{ "ENOEXEC",	8 },
		{ "EBADF",	9 },
		{ "ECHILD",	10 },
		{ "EAGAIN",	11 },
		{ "ENOMEM",	12 },
		{ "EACCES",	13 },
		{ "EFAULT",	14 },
		{ "ENOTBLK",	15 },
		{ "EBUSY",	16 },
		{ "EEXIST",	17 },
		{ "EXDEV",	18 },
		{ "ENODEV",	19 },
		{ "ENOTDIR",	20 },
		{ "EISDIR",	21 },
		{ "EINVAL",	22 },
		{ "ENFILE",	23 },
		{ "EMFILE",	24 },
		{ "ENOTTY",	25 },
		{ "ETXTBSY",	26 },
		{ "EFBIG",	27 },
		{ "ENOSPC",	28 },
		{ "ESPIPE",	29 },
		{ "EROFS",	30 },
		{ "EMLINK",	31 },
		{ "EPIPE",	32 },
		{ "EDOM",	33 },
		{ "ERANGE",	34 }
	}
};


/* mmap flags */
string_map_t mmapflags_map = {
	4, {
		{ "MAP_SHARED",		0x01 },
		{ "MAP_PRIVATE",	0x02 },
		{ "MAP_FIXED",		0x10 },
		{ "MAP_ANONYMOUS",	0x800 }
	}
};


/* wait options */
string_map_t waitoptions_map = {
	2, {
		{ "WNOHANG",		0x01 },
		{ "WUNTRACED",		0x02 }
	}
};


/* clone */
string_map_t clone_map = {
	11, {
		{ "CLONE_VM",		0x00100 },
		{ "CLONE_FS",		0x00200 },
		{ "CLONE_FILES",	0x00400 },
		{ "CLONE_SIGHAND",	0x00800 },
		{ "CLONE_PTRACE",	0x02000 },
		{ "CLONE_VFORK",	0x04000 },
		{ "CLONE_PARENT",	0x08000 },
		{ "CLONE_KECTX",	0x10000 },
		{ "CLONE_NEWNS",	0x20000 },
		{ "CLONE_SYSVSEM",	0x40000 },
		{ "CLONE_SETTLS",	0x80000 }
	}
};

void dump_stack(struct mem_t *mem, struct regs_t *regs)
{
	int i;
	word addr, w, fp, sp;
	fp = GPR(REGS_FP);
	sp = GPR(REGS_SP);
	debug("stack contents (sp=0x%x, fp=0x%x):",
		sp, fp);
	for (i = 20; i >= 0; i--) {
		addr = sp + i * 4;
		READ_WORD(addr, &w);
		debug("\t%02x(sp) = 0x%x%s", i * 4, w,
			addr == fp ? " <--- fp" : "");
	}
}


/* poll */
string_map_t poll_map = {
	6, {
		{ "POLLIN", 0x0001 },
		{ "POLLPRI", 0x0002 },
		{ "POLLOUT", 0x0004 },
		{ "POLLERR", 0x0008 },
		{ "POLLHUP", 0x0010 },
		{ "POLLNVAL", 0x0020 }
	}
};


/* pthread_request */
enum reqkind_t {
	REQ_CREATE, REQ_FREE, REQ_PROCESS_EXIT, REQ_MAIN_KECTX_EXIT,
	REQ_POST, REQ_DEBUG, REQ_KICK, REQ_FOR_EACH_KECTX
};

string_map_t reqkind_map = {
	8, {
		{ "REQ_CREATE", REQ_CREATE },
		{ "REQ_FREE", REQ_FREE },
		{ "REQ_PROCESS_EXIT", REQ_PROCESS_EXIT },
		{ "REQ_MAIN_KECTX_EXIT", REQ_MAIN_KECTX_EXIT },
		{ "REQ_POST", REQ_POST },
		{ "REQ_DEBUG", REQ_DEBUG },
		{ "REQ_KICK", REQ_KICK },
		{ "REQ_FOR_EACH_KECTX", REQ_FOR_EACH_KECTX }
	}
};

void dump_req(void *buf)
{
	word kind;
	kind = SWAPW(* (word *) (buf + 4));
	debug("\tpthread request: req_kind = %s",
		map_value(&reqkind_map, kind));
}


/* waitpid */
string_map_t waitpid_map = {
	2, {
		{ "WNOHANG", WNOHANG },
		{ "WUNTRACED", WUNTRACED }
	}
};


/* system information */
struct {
	char sysname[65];
	char nodename[65];
	char release[65];
	char version[65];
	char machine[65];
	char domainname[65];
} utsname = {
	"Linux",
	"sim",
	"2.6",
	"Tue Apr 5 12:21:57 UTC 2005",
	"mips"
};


/* return name of a system call code */
char *get_syscall_name(int syscode)
	
{
	if (syscode < 0 || syscode >= SIM_SYS_MAX)
		return "";
	else
		return syscall_name[syscode - NR_Linux];
}


int check_syscall_error(struct regs_t *regs)
{
	int error;
	if (SGPR(REGS_V0) != -1) {
		SET_GPR(REGS_A3, 0);
		error = FALSE;
	} else {
		SET_GPR(REGS_V0, errno);
		SET_GPR(REGS_A3, 1);
		error = TRUE;
	}
	return error;
}


/* system call;
 * return value: values of type SYSCALL_XXX */
void sim_syscall(struct kernel_t *ke, int ctx)
{
	struct	mem_t *mem = ke_mem(ke, ctx);
	struct	regs_t *regs = ke_regs(ke, ctx);
	int	syscode = GPR(REGS_V0);
	char	tempstr[MAX_STRING_SIZE];
	int	error = 0;

	/* execute syscall */
	switch (syscode) {


		
		/* 0 */
		case SIM_SYS_syscall:
		{
			word arg5, arg6;
			READ_WORD(GPR(REGS_SP) + 16, &arg5);
			READ_WORD(GPR(REGS_SP) + 20, &arg6);
			printf(
				"arg1=%d\n"
				"arg2=%d\n"
				"arg3=%d\n"
				"arg4=%d\n"
				"arg5=%d\n"
				"arg6=%d\n",
				GPR(REGS_A0),
				GPR(REGS_A1),
				GPR(REGS_A2),
				GPR(REGS_A3),
				arg5, arg6);
			exit(1);
			break;
		}
		
		
		
		/* 1 */
		case SIM_SYS_exit:
		{
			ke_finish_ctx(ke, ctx);
			break;
		}

	
		
		/* 3 */
		case SIM_SYS_read:
		{
#define READ_MAX_SIZE (1<<25)
			char *buf;
			word size;
			sword fd, ssize;
			struct buffer_t *buffer;

			fd = ld_translate_fd(ke, ctx, SGPR(REGS_A0));
			size = MIN(READ_MAX_SIZE, GPR(REGS_A2));

			/* read from pipe/fd */
			buffer = pipedb_read_buffer(ke, fd);
			if (buffer) {
				if (!buffer_count(buffer)) {
				
					/* suspend ctx */
					struct sysevent_t *e;
					e = sysevent_create(ctx, SYSEVENT_READ);
					e->for_waitfd.active = 1;
					e->for_waitfd.buffer = buffer;
					e->for_waitfd.addr = GPR(REGS_A1);
					e->for_waitfd.size = size;
					syseventq_sched(ke, e);
					ke_suspend_ctx(ke, ctx);
					break;
					
				} else {
					buf = calloc(1, size);
					ssize = buffer_read(buffer, buf, size);
				}
			} else {
				buf = calloc(1, size);
				ssize = read(fd, buf, size);
			}
			
			if (ssize >= READ_MAX_SIZE)
				fatal("syscall read (0x%x bytes): READ_MAX_SIZE to small, increase it!",
					GPR(REGS_A2));
			SET_GPR(REGS_V0, ssize);
			error = check_syscall_error(regs);

			/* copy result in memory */
			WRITE_BLK(GPR(REGS_A1), ssize, buf);
			debug("syscall read: %d bytes read from fd=%d: '%s'",
					ssize, GPR(REGS_A0), buf);
			free(buf);
			break;
#undef READ_MAX_SIZE
		}



		/* 4 */
		case SIM_SYS_write:
		{
			char *buf;
			word size, fd, pbuf;
			sword res;
			struct buffer_t *buffer;

			fd = ld_translate_fd(ke, ctx, SGPR(REGS_A0));
			pbuf = GPR(REGS_A1);
			size = GPR(REGS_A2);
			buf = calloc(1, size);
			READ_BLK(pbuf, size, buf);
			
			if (debugging()) {
				char str[80];
				strdump(str, buf, MIN(size + 1, 80));
				debug("\tbuffer='%s'", str);
				if (size == 0x94)
					dump_req(buf);
			}
			
			/* write in a pipe? */
			buffer = pipedb_write_buffer(ke, fd);
			if (buffer) {
				res = buffer_write(buffer, buf, size);
			} else {
				res = write(fd, buf, size);
			}
			
			SET_GPR(REGS_V0, res);
			error = check_syscall_error(regs);
			free(buf);
			break;
		}
	
	
	
		/* 5 */
		case SIM_SYS_open:
		{
			char filename[MAX_STRING_SIZE];
			int sim_flags = GPR(5), local_flags = 0;
			word i;
		
			/* traducir flags */
			for (i = 0; i < SIM_NFLAGS; i++) {
				if (sim_flags & sim_flag_table[i].sim_flag) {
					sim_flags &= ~sim_flag_table[i].sim_flag;
					local_flags |= sim_flag_table[i].local_flag;
				}
			}
			if (sim_flags)
				fatal("syscall open: cannot decode flags: 0x%08x", sim_flags);

			/* convert file name if it is a relative path */
			READ_STR(GPR(4), MAX_STRING_SIZE, filename);
			ld_convert_filename(ke, ctx, filename);
			debug("\tfilename=%s", filename);
			
			/* open the file */
			SET_GPR(2, open(filename, local_flags, GPR(6)));
			error = check_syscall_error(regs);
			break;
		}
	
	
	
		/* 6 */
		case SIM_SYS_close:
		{
	
			/* do not close stdin, stdout or stderr */
			if (GPR(4) == 0 || GPR(4) == 1 || GPR(4) == 2) {
				SET_GPR(7, 0);
				break;
			}

			SET_GPR(2, close((int) GPR(4)));
			error = check_syscall_error(regs);
			break;
		}
		
		
		
		/* 7 */
		case SIM_SYS_waitpid:
		{
			int pid = SGPR(REGS_A0);
			word pstatus = GPR(REGS_A1);
			struct sysevent_t *e;
			
			/* not supported values */
			if (pid < -1 || !pid)
				fatal("waitpid not supported with pid < 1 (pid=%d)", pid);
			
			/* check if pid exists if waiting to a specific one */
			if (ke_pid_ctx(ke, pid) < 0 && !ke_is_zombie(ke, pid)) {
				SET_GPR(REGS_V0, -1);
				SET_GPR(REGS_A3, ECHILD);
				break;
			}
			
			/* wait event */
			e = sysevent_create(ctx, SYSEVENT_WAIT);
			e->for_waitpid.active = 1;
			e->for_waitpid.pid = pid;
			e->for_signal.active = 1;
			syseventq_sched(ke, e);
			ke_suspend_ctx(ke, ctx);
			if (pstatus)
				fatal("waitpid with status != NULL");
			break;
		}



		/* 10 */
		case SIM_SYS_unlink:
		{
			char buf[0x100];
			READ_STR(GPR(4), sizeof(buf), buf);
			SET_GPR(2, unlink(buf));
			error = check_syscall_error(regs);
			break;
		}
	
	
	
		/* 13 */
		case SIM_SYS_time:
		{
			time_t t;
			SET_GPR(2, time(&t));
			if (GPR(4))
				WRITE_WORD(GPR(4), t);
			error = check_syscall_error(regs);
			break;
		}



		/* 19 */
		case SIM_SYS_lseek:
		{
			int fd = ld_translate_fd(ke, ctx, SGPR(REGS_A0));
			sword offset = SGPR(REGS_A1);
			word whence = GPR(REGS_A2);
			sdword result = lseek(fd, offset, whence);
			SET_GPR(REGS_V0, result);
			error = check_syscall_error(regs);
			break;
		}
	
	
	
		/* 20 */
		case SIM_SYS_getpid:
		{
			SET_GPR(REGS_V0, ke_get_pid(ke, ctx));
			error = check_syscall_error(regs);
			break;
		}
	
		
		
		/* 24 */
		case SIM_SYS_getuid:
		{
		
			SET_GPR(2, getuid());
			error = check_syscall_error(regs);
			break;
		}
		
		
		
		/* 33 */
		case SIM_SYS_access:
		{
			char name[0x100];
			int mode;
			READ_STR(GPR(4), sizeof(name), name);
			mode = GPR(5);
			SET_GPR(2, access(name, mode));
			error = check_syscall_error(regs);
		}
		
		
		
		/* 37 */
		case SIM_SYS_kill:
		{
			int pid, sig, destctx;
			pid = SGPR(REGS_A0);
			sig = SGPR(REGS_A1);
			if (pid < 1)
				fatal("kill not supported with pid < 1 (pid=%d)", pid);
			
			destctx = ke_pid_ctx(ke, pid);
			if (destctx == -1) {
				debug("\tkill: invalid pid");
				SET_GPR(REGS_A3, ESRCH);
				SET_GPR(REGS_V0, -1);
				break;
			}
			
			debug("\tth%d: sending signal %d to th%d (pid=%d)",
				ctx, sig, destctx, pid);
			sim_sigset_add(&ke->signaldb->context[destctx].pending, sig);
			SET_GPR(REGS_A3, 0);
			SET_GPR(REGS_V0, 0);
			break;
		}
		
		
		
		/* 38 */
		case SIM_SYS_rename:
		{
			char old[0x100], new[0x100];
			READ_STR(GPR(4), sizeof(old), old);
			READ_STR(GPR(5), sizeof(new), new);
			SET_GPR(REGS_V0, rename(old, new));
			error = check_syscall_error(regs);
			break;
		}



		/* 41 */
		case SIM_SYS_mkdir:
		{
			char path[MAX_STRING_SIZE];
			int result;
			word ppath, mode;
			ppath = GPR(REGS_A0);
			mode = GPR(REGS_A1);

			READ_STR(ppath, MAX_STRING_SIZE, path);
			ld_convert_filename(ke, ctx, path);
			result = mkdir(path, mode);

			SET_GPR(REGS_V0, result);
			error = check_syscall_error(regs);
			break;
		}
	
	
	
		/* 41 */
		/*case SIM_SYS_dup:
		{
			int fd;
			fd = ld_translate_fd(ld, SGPR(REGS_A0));
			SET_GPR(REGS_V0, dup(fd));
			error = check_syscall_error(regs);
			break;
		}*/
		
		
		
		/* 42 */
		case SIM_SYS_pipe:
		{
			int fd[2];
			pipedb_pipe(ke, fd);
			SET_GPR(REGS_V0, fd[0]);
			SET_GPR(REGS_V1, fd[1]);
			SET_GPR(REGS_A3, 0);
			debug("\tpipe created with fd={%d, %d}", fd[0], fd[1]);
			break;
		}



		/* 43 */
		case SIM_SYS_times:
		{
			struct tms tms;
			SET_GPR(2, times(&tms));
			SET_GPR(7, 0);
			WRITE_WORD(GPR(4) + 0, tms.tms_utime);
			WRITE_WORD(GPR(4) + 4, tms.tms_stime);
			WRITE_WORD(GPR(4) + 8, tms.tms_cutime);
			WRITE_WORD(GPR(4) + 12, tms.tms_cstime);
			break;
		}
		
		
		
		/* 45 */
		case SIM_SYS_brk:
		{
			/* FIXME */
			//word addr = GPR(REGS_A0);
			//SET_GPR(REGS_V0, addr);
			SET_GPR(REGS_V0, 0);
			SET_GPR(REGS_A3, 0);
			break;
		}

		
		
		
		/* 47 */
		case SIM_SYS_getgid:
		{
		
			SET_GPR(2, getgid());
			error = check_syscall_error(regs);
			break;
		}
	
		
		
		/* 49 */
		case SIM_SYS_geteuid:
		{
		
			SET_GPR(2, geteuid());
			error = check_syscall_error(regs);
			break;
		}
		
		
		
		/* 50 */
		case SIM_SYS_getegid:
		{
		
			SET_GPR(2, getegid());
			error = check_syscall_error(regs);
			break;
		}
		
		
		
		/* 54 */
		case SIM_SYS_ioctl:
		{
                        /* Parameters */
                        int fd, result = 0;
                        word req, pbuf;
                        fd = ld_translate_fd(ke, ctx, SGPR(REGS_A0));
                        req = GPR(REGS_A1);
                        pbuf = GPR(REGS_A2);

                        /* Action */
                        switch (req) {
				case 0x5401: /* TCGETA */
                                case 0x540d: /* TCGETS */
				{
                                        struct sim_termios {
                                                word c_iflag;
                                                word c_oflag;
                                                word c_cflag;
                                                word c_lflag;
                                                byte c_line;
                                                byte c_cc[32];
                                        } sbuf;
                                        struct termios buf;
                                        int i;

                                        READ_BLK(pbuf, sizeof(sbuf), &sbuf);
                                        buf.c_iflag = SWAPW(sbuf.c_iflag);
                                        buf.c_oflag = SWAPW(sbuf.c_oflag);
                                        buf.c_cflag = SWAPW(sbuf.c_cflag);
                                        buf.c_lflag = SWAPW(sbuf.c_lflag);
                                        buf.c_line = sbuf.c_line;
                                        for (i = 0; i < 32; i++)
                                                buf.c_cc[i] = sbuf.c_cc[i];

                                        result = ioctl(fd, TCGETS, &buf);
                                        sbuf.c_iflag = SWAPW(buf.c_iflag);
                                        sbuf.c_oflag = SWAPW(buf.c_oflag);
                                        sbuf.c_cflag = SWAPW(buf.c_cflag);
                                        sbuf.c_lflag = SWAPW(buf.c_lflag);
                                        sbuf.c_line = buf.c_line;
                                        for (i = 0; i < 32; i++)
                                                sbuf.c_cc[i] = buf.c_cc[i];

                                        debug("\tsyscall ioctl: req=TCGETS, result=%d, c_iflag=%u, c_oflag=%u, c_cflag=%u, c_lflag=%u\n",
                                                result, buf.c_iflag, buf.c_oflag, buf.c_cflag, buf.c_lflag);
                                        break;
                                }

                                default:
                                        fatal("unknown ioctl request: 0x%x", req);
                        }
                        SET_GPR(REGS_V0, result);
                        error = check_syscall_error(regs);
                        break;
		}



		/* 64 */
		case SIM_SYS_getppid:
		{
			SET_GPR(REGS_V0, ke_get_ppid(ke, ctx));
			error = check_syscall_error(regs);
			break;
		}
		
		
		
		/* 75 */
		case SIM_SYS_setrlimit:
		{
			word resource = GPR(REGS_A0);
			word prlimit = GPR(REGS_A1);
			switch (resource) {
				case SIM_RLIMIT_STACK:
					sim_rlimit_read(mem, prlimit, &sim_rlim_stack);
					break;
				case SIM_RLIMIT_DATA:
					sim_rlimit_read(mem, prlimit, &sim_rlim_data);
					break;
				default:
					fatal("syscall setrlimit: resource=%d", resource);
			}
			SET_GPR(REGS_V0, 0);
			SET_GPR(REGS_A3, 0);
			break;
		}
	
	
	
		/* 76 */
		case SIM_SYS_getrlimit:
		{
			word resource = GPR(REGS_A0);
			word prlimit = GPR(REGS_A1);
			switch (resource) {
				case SIM_RLIMIT_STACK:
					sim_rlimit_write(mem, prlimit, &sim_rlim_stack);
					break;
				case SIM_RLIMIT_DATA:
					sim_rlimit_write(mem, prlimit, &sim_rlim_data);
					break;
				default:
					fatal("syscall getrlimit: resource=%d", resource);
			}
			SET_GPR(REGS_V0, 0);
			SET_GPR(REGS_A3, 0);
			break;
		}
	
	
	
		/* 77 */
		
	      /*  u.ru_utime    0    8
	          u.ru_stime    8    8
	         u.ru_maxrss   16    4
	          u.ru_ixrss   20    4
	          u.ru_idrss   24    4
	          u.ru_isrss   28    4
	         u.ru_minflt   32    4
	         u.ru_majflt   36    4
	          u.ru_nswap   40    4
		u.ru_inblock   44    4
	        u.ru_oublock   48    4
	         u.ru_msgsnd   52    4
	         u.ru_msgrcv   56    4
	       u.ru_nsignals   60    4
	          u.ru_nvcsw   64    4
	         u.ru_nivcsw   68    4 */
		
		case SIM_SYS_getrusage:
		{
			sword who = GPR(REGS_A0);
			word pusage = GPR(REGS_A1);
			struct rusage ru;
			
			if (who)
				warning("rusage used with who != RUSAGE_SELF");
			getrusage(RUSAGE_SELF, &ru);
			WRITE_WORD(pusage, ru.ru_utime.tv_sec);
			WRITE_WORD(pusage + 4, ru.ru_utime.tv_usec);
			WRITE_WORD(pusage + 8, ru.ru_stime.tv_sec);
			WRITE_WORD(pusage + 12, ru.ru_stime.tv_usec);
			WRITE_WORD(pusage + 16, ru.ru_maxrss);
			WRITE_WORD(pusage + 20, ru.ru_ixrss);
			WRITE_WORD(pusage + 24, ru.ru_idrss);
			WRITE_WORD(pusage + 28, ru.ru_isrss);
			WRITE_WORD(pusage + 32, ru.ru_minflt);
			WRITE_WORD(pusage + 36, ru.ru_majflt);
			WRITE_WORD(pusage + 40, ru.ru_nswap);
			WRITE_WORD(pusage + 44, ru.ru_inblock);
			WRITE_WORD(pusage + 48, ru.ru_oublock);
			WRITE_WORD(pusage + 52, ru.ru_msgsnd);
			WRITE_WORD(pusage + 56, ru.ru_msgrcv);
			WRITE_WORD(pusage + 60, ru.ru_nsignals);
			WRITE_WORD(pusage + 64, ru.ru_nvcsw);
			WRITE_WORD(pusage + 68, ru.ru_nivcsw);
			
			SET_GPR(REGS_V0, 0);
			SET_GPR(REGS_A3, 0);
			break;
		}
	
	
	
		/* 85 */
		case SIM_SYS_readlink:
		{
			char path[0x100], buf[0x100];
			READ_STR(GPR(4), sizeof(path), path);
			SET_GPR(2, readlink(path, buf, sizeof(buf)));
			error = check_syscall_error(regs);
			if (!error)
				WRITE_STR(GPR(5), buf);
			break;
		}
	
	
	
		/* 90 */
		case SIM_SYS_mmap:
		{
			word start, length, prot, flags, fd, offset;
			char sflags[100];
		
			/* parameters */
			start = GPR(REGS_A0);
			length = GPR(REGS_A1);
			prot = GPR(REGS_A2);
			flags = GPR(REGS_A3);
			READ_WORD(GPR(REGS_SP) + 16, &fd);
			READ_WORD(GPR(REGS_SP) + 20, &offset);
			map_flags(&mmapflags_map, flags, sflags);
			debug("syscall mmap: start=0x%x, length=0x%x, prot=0x%x, flags=%s, "
				"fd=%d, offset=0x%x",
				start, length, prot, sflags, fd, offset);
			if ((sword) fd != -1)
				fatal("syscall mmap: system call is only supported with fd=-1");
		
			if (!start)
				start = KECTX.ld->heap_top;
			start = mem_map(mem, start, length, MEM_PROT_WRITE | MEM_PROT_READ);
			
			SET_GPR(REGS_V0, start);
			SET_GPR(7, 0);
		
			break;
		}
		
		
		
		/* 91 */
		case SIM_SYS_munmap:
		{
			word start, length;
			start = GPR(REGS_A0);
			length = GPR(REGS_A1);
			mem_unmap(mem, start, length);
			SET_GPR(REGS_V0, 0);
			SET_GPR(REGS_A3, 0);
			break;
		}



		/* 106 */
		case SIM_SYS_stat:
		{
			struct stat buf;
			int result;
			word ppath, pbuf;
			char path[MAX_STRING_SIZE];

			ppath = GPR(REGS_A0);
			pbuf = GPR(REGS_A1);
			READ_STR(ppath, MAX_STRING_SIZE, path);
			ld_convert_filename(ke, ctx, path);
			result = stat(path, &buf);

			SET_GPR(REGS_V0, result);
			if (result != -1)
				struct_stat_write(mem, pbuf, &buf);
			error = check_syscall_error(regs);
			break;
		}


		/* 108 */
		case SIM_SYS_fstat:
		{
			struct stat buf;
			int fd, result;
			word pbuf;

			fd = ld_translate_fd(ke, ctx, SGPR(REGS_A0));
			pbuf = GPR(REGS_A1);
			result = fstat(fd, &buf);

			SET_GPR(REGS_V0, result);
			if (result != -1)
				struct_stat_write(mem, pbuf, &buf);
			error = check_syscall_error(regs);
			break;
		}
		
		
		
		/* 114 */
		case SIM_SYS_wait4:
		{
			char soptions[100];
			sword pid, status, options;
			word prusage;
			struct sysevent_t *e;
			
			pid = GPR(REGS_A0);
			status = GPR(REGS_A1);
			options = GPR(REGS_A2);
			prusage = GPR(REGS_A3);
			
			map_flags(&waitoptions_map, options, soptions);
			debug("syscall wait4: pid=%d status=%d options=%s prusage=0x%x",
				pid, status, soptions, prusage);
			
			/* not supported values */
			if (pid < -1 || !pid)
				fatal("wait4 pid value not supported: %d", pid);
			
			/* no child */
			if (!ke_child_count(ke, ctx)) {
				SET_GPR(REGS_V0, -1);
				SET_GPR(REGS_A3, ECHILD);
				break;
			}
			
			/* check if pid exists if waiting to a specific one */
			if (ke_pid_ctx(ke, pid) < 0 && pid >= 0) {
				SET_GPR(REGS_V0, -1);
				SET_GPR(REGS_A3, ECHILD);
				break;
			}
			
			/* enqueue event */
			e = sysevent_create(ctx, SYSEVENT_WAIT);
			e->for_waitpid.active = 1;
			e->for_waitpid.pid = pid;
			syseventq_sched(ke, e);
			ke_suspend_ctx(ke, ctx);
			
			break;
		}



		/* 153 */
		case SIM_SYS__sysctl:
		{
			struct {
				word name;
				word nlen;
				word oldval;
				word oldlenp;
				word newval;
				word newlen;
			} args;
			word name[10];
			READ_BLK(GPR(4), sizeof(args), &args);
			READ_BLK((word) args.name, 10 * sizeof(word), &name);
			/*printf("SYSCALL SYSCTL\n"
				"address=0x%llx\n"
				"name=0x%x\n"
				"name[0]=%u\n"
				"nlen=%u\n"
				"oldval=%u\n"
				"oldlenp=0x%x\n"
				"newval=0x%x\n"
				"newlen=%u\n",
				GPR(4), args.name, name[0], args.nlen, args.oldval,
				args.oldlenp, args.newval, args.newlen);*/
			SET_GPR(7, 0);
			if (name[0] == 0)
				break;
			fatal("syscall sysctl still not implemented for name[0] != 0");
			break;
		}
		
		
		/* 213 */
		case SIM_SYS_stat64:
		{
			struct sim_stat {
				dword	sim_st_dev;
				dword	sim_st_ino;
				dword	sim_st_nlink;
				word	sim_st_mode;
				word	sim_st_uid;
				word	sim_st_gid;
				word	unused0;
				dword	sim_st_rdev;
				dword	sim_st_size;
				dword	sim_st_blksize;
				dword	sim_st_blocks;
				dword	sim_st_atime;
				dword	unused1;
				dword	sim_st_mtime;
				dword	unused2;
				dword	sim_st_ctime;
			};
			
			struct stat buf;
			struct sim_stat sim_buf;
			char filename[MAX_STRING_SIZE];
		
			/* read & convert filename */
			READ_STR(GPR(4), MAX_STRING_SIZE, filename);
			ld_convert_filename(ke, ctx, filename);
			debug("\tfilename=%s", filename);
			
			/* execute syscall */
			SET_GPR(2, stat(filename, &buf));
			error = check_syscall_error(regs);
			if (!error) {		
				sim_buf.sim_st_dev	= SWAPW(buf.st_dev);
				sim_buf.sim_st_ino	= SWAPW(buf.st_ino);
				sim_buf.sim_st_mode	= SWAPW(buf.st_mode);
				sim_buf.sim_st_nlink	= SWAPW(buf.st_nlink);
				sim_buf.sim_st_uid	= SWAPW(buf.st_uid);
				sim_buf.sim_st_gid	= SWAPW(buf.st_gid);
				sim_buf.sim_st_rdev	= SWAPW(buf.st_rdev);
				sim_buf.sim_st_size	= SWAPW(buf.st_size);
				sim_buf.sim_st_atime	= SWAPW(buf.st_atime);
				sim_buf.sim_st_mtime	= SWAPW(buf.st_mtime);
				sim_buf.sim_st_ctime	= SWAPW(buf.st_ctime);
				sim_buf.sim_st_blksize	= SWAPW(buf.st_blksize);
				sim_buf.sim_st_blocks	= SWAPW(buf.st_blocks);
				WRITE_BLK(GPR(5), sizeof(sim_buf), &sim_buf);
			}
			break;
		}
		
		
		
		/* 215 */
		/*
		st_dev		0       4
		st_ino  	16      4
		st_mode 	20      4
		st_nlink        24      4
		st_uid  	28      4
		st_gid  	32      4
		st_rdev 	36      4
		st_size 	48      4
		st_atime        56      4
		st_mtime        64      4
		st_ctime        72      4
		st_blksize      80      4
		st_blocks       84      4
		*/
		case SIM_SYS_fstat64:
		{
			int fd;
			struct sim_stat {
				word	sim_st_dev;
				word	pad0[3];
				dword	sim_st_ino;
				word	sim_st_mode;
				word	sim_st_nlink;
				word	sim_st_uid;
				word	sim_st_gid;
				word	sim_st_rdev;
				word	pad1[3];
				dword	sim_st_size;
				word	sim_st_atime;
				word	pad2;
				word	sim_st_mtime;
				word	pad3;
				word	sim_st_ctime;
				word	pad4;
				word	sim_st_blksize;
				word	pad5;
				dword	sim_st_blocks;
			};
	
			struct stat buf;
			struct sim_stat sim_buf;

			fd = ld_translate_fd(ke, ctx, SGPR(REGS_A0));
			SET_GPR(2, fstat(fd, &buf));
			error = check_syscall_error(regs);

			if (!error) {
				/*if (fd == 1) {
					buf.st_dev = 0xb;
					buf.st_ino = 0x2;
					buf.st_mode = 0x2190;
					buf.st_rdev = 0x8800;
					buf.st_blksize = 0x2190;
					fprintf(stderr, "*****************\n");
					fprintf(stderr, "st_dev=0x%x\n", (word) buf.st_dev);
					fprintf(stderr, "st_ino=0x%llx\n", (long long) buf.st_ino);
					fprintf(stderr, "st_mode=0x%x\n", buf.st_mode);
					fprintf(stderr, "st_rdev=0x%x\n", (word) buf.st_rdev);
					fprintf(stderr, "st_blksize=0x%x\n", buf.st_mode);
					fflush(stderr);
				}*/
				
				sim_buf.sim_st_dev	= SWAPW(buf.st_dev);
				sim_buf.sim_st_ino	= SWAPDW(buf.st_ino);
				sim_buf.sim_st_mode	= SWAPW(buf.st_mode);
				sim_buf.sim_st_nlink	= SWAPW(buf.st_nlink);
				sim_buf.sim_st_uid	= SWAPW(buf.st_uid);
				sim_buf.sim_st_gid	= SWAPW(buf.st_gid);
				sim_buf.sim_st_rdev	= SWAPW(buf.st_rdev);
				sim_buf.sim_st_size	= SWAPDW(buf.st_size);
				sim_buf.sim_st_atime	= SWAPW(buf.st_atime);
				sim_buf.sim_st_mtime	= SWAPW(buf.st_mtime);
				sim_buf.sim_st_ctime	= SWAPW(buf.st_ctime);
				sim_buf.sim_st_blksize	= SWAPW(buf.st_blksize);
				sim_buf.sim_st_blocks	= SWAPDW(buf.st_blocks);
			
				WRITE_BLK(GPR(5), sizeof(sim_buf), &sim_buf);
			}

			break;
		}
		
		
		
		/* 120 */
		case SIM_SYS_clone:
		{
			int new;
			char flags[100];
			word clone_flags, newsp;
			struct regs_t *newregs;

			/* arguments */
			clone_flags = GPR(REGS_A0);
			newsp = GPR(REGS_A1);
			map_flags(&clone_map, clone_flags & ~0xff, flags);
			debug("\tclone flags = %s + %d", flags, clone_flags & 0xff);
			new = ke_clone_ctx(ke, ctx, clone_flags & 0xff);
			
			newregs = ke_regs(ke, new);
			newregs->regs_R[REGS_SP] = newsp;
			newregs->regs_R[REGS_A3] = 0;
			newregs->regs_R[REGS_V0] = 0;

			SET_GPR(REGS_V0, ke_get_pid(ke, new));
			SET_GPR(REGS_A3, 0);
			break;
		}
	
	
	
		/* 122 */
		case SIM_SYS_uname:
		{
			WRITE_BLK(GPR(4), sizeof(utsname), &utsname);
			SET_GPR(2, 0);
			SET_GPR(7, 0);
			break;
		}
		
		
		
		/* 125 */
		case SIM_SYS_mprotect:
		{
			/* FIXME */
			SET_GPR(2, 0);
			SET_GPR(7, 0);
			break;
		}
		
		
		
		/* 140 */
		case SIM_SYS__llseek:
		{
			int fd, result;
			word lo, hi, presult, whence;
			fd = ld_translate_fd(ke, ctx, SGPR(REGS_A0));
			hi = GPR(REGS_A1);
			lo = GPR(REGS_A2);
			presult = GPR(REGS_A3);
			READ_WORD(GPR(REGS_SP) + 16, &whence);

			result = lseek(fd, lo, whence);
			SET_GPR(REGS_V0, result);
			WRITE_WORD(presult, 0);
			WRITE_WORD(presult + 4, result);
			error = check_syscall_error(regs);
			break;
		}

		
		
		/* 146 */
		case SIM_SYS_writev:
		{
			int	i, fd;
			byte	*buf;
			struct	iovec *iov;
		
			/* estructura 'iov' simulada */
			struct sim_iovec {
				word iov_base;
				word iov_len;
			} *sim_iov;
		
			/* reserva memoria para vectores E/S */
			iov = (struct iovec *) malloc(GPR(6) * sizeof(struct iovec));
			sim_iov = (struct sim_iovec *) malloc(GPR(6) * sizeof(struct sim_iovec));
			READ_BLK(GPR(5), GPR(6) * sizeof(struct sim_iovec), (byte *) sim_iov);
			
			/* copy target side I/O vector buffers to host memory */
			for (i = 0; i < GPR(6); i++) {
				sim_iov[i].iov_base = SWAPW(sim_iov[i].iov_base);
				sim_iov[i].iov_len = SWAPW(sim_iov[i].iov_len);
				if (iov[i].iov_base) {
					buf = (byte *) calloc(sim_iov[i].iov_len, 1);
					READ_BLK(sim_iov[i].iov_base, sim_iov[i].iov_len, buf);
					iov[i].iov_base = buf;
					iov[i].iov_len = sim_iov[i].iov_len;
				} else {
					iov[i].iov_base = NULL;
					iov[i].iov_len = 0;
				}
			}

			/* escritura vectorizada */
			fd = ld_translate_fd(ke, ctx, SGPR(REGS_A0));
			SET_GPR(2, writev(fd, iov, (size_t) GPR(6)));
			error = check_syscall_error(regs);

			/* liberar memoria */
			for (i = 0; i < GPR(6); i++)
				if (iov[i].iov_base)
					free(iov[i].iov_base);
			free(iov);
			free(sim_iov);
			break;
		}
		
		
		
		/* 166 */
		case SIM_SYS_nanosleep:
		{
			word preq, prem;
			word sec, nsec;
			clock_t total;
			struct sysevent_t *e;
			
			preq = GPR(REGS_A0);
			prem = GPR(REGS_A1);
			READ_WORD(preq, &sec);
			READ_WORD(preq + 4, &nsec);
			total = sec * CLOCKS_PER_SEC + nsec / 1e9 * CLOCKS_PER_SEC;
			
			/* schedule wakeup event */
			e = sysevent_create(ctx, SYSEVENT_RESUME);
			e->for_time.active = 1;
			e->for_time.when = clock() + total;
			syseventq_sched(ke, e);
			ke_suspend_ctx(ke, ctx);
			
			break;
		}



		/* 167 */
		case SIM_SYS_mremap:
		{
			/* Read arguments */
			word oldaddr, oldsize, newsize, start;
			oldaddr = GPR(REGS_A0);
			oldsize = GPR(REGS_A1);
			newsize = GPR(REGS_A2);

			/////////////////
			fprintf(stderr, "syscall mremap: oldaddr=0x%x, oldsize=0x%x, newsize=0x%x\n",
				oldaddr, oldsize, newsize);

			/* Action */
			start = mem_remap(mem, oldaddr, oldsize, newsize);
			
			SET_GPR(REGS_V0, start);
			error = check_syscall_error(regs);
			break;
		}
		
		
		
		/* 188 */
		case SIM_SYS_poll:
		{
			/*   ufds.fd    0    4
			 ufds.events    4    2
			ufds.revents    6    2 */

			struct sysevent_t *e;
			word pufds, nfds;
			sword timeout;
			int i;
			
			pufds = GPR(REGS_A0);
			nfds = GPR(REGS_A1);
			timeout = SGPR(REGS_A2);
			if (nfds < 1)
				fatal("syscall poll: nfds < 1");
			
			/* read ufds and schedule events */
			for (i = 0; i < nfds; i++, pufds += 8) {
				sword fd;
				shalf events, revents;
				READ_WORD(pufds, (word *) &fd);
				READ_HALF(pufds + 4, (half *) &events);
				READ_HALF(pufds + 6, (half *) &revents);
				if (events != 1)
					fatal("syscall poll: ufds.events != POLLIN");
				
				e = sysevent_create(ctx, SYSEVENT_POLL);
				e->for_time.active = 1;
				e->for_time.when = clock() + (double) timeout * CLOCKS_PER_SEC / 1000;
				e->for_waitfd.active = 1;
				e->for_waitfd.buffer = pipedb_read_buffer(ke, fd);
				if (!e->for_waitfd.buffer)
					fatal("syscall poll: fd does not belong to a pipe read buffer");
				e->for_waitfd.pufds = pufds;
				syseventq_sched(ke, e);
			}
			ke_suspend_ctx(ke, ctx);
			break;
		}



		/* 194 */
		case SIM_SYS_rt_sigaction:
		{
			word signum, pact, poact;
			
			signum = GPR(REGS_A0);
			pact = GPR(REGS_A1);
			poact = GPR(REGS_A2);
			if (poact)
				sim_sigaction_write(mem, poact, &ke->signaldb->sim_sigaction[signum - 1]);
			if (pact)
				sim_sigaction_read(mem, pact, &ke->signaldb->sim_sigaction[signum - 1]);
			
			SET_GPR(REGS_A3, 0);
			SET_GPR(REGS_V0, 0);
			break;
		}
	
	
	
		/* 195 */
		case SIM_SYS_rt_sigprocmask:
		{
			word how, pset, poset;
			struct sim_sigset_t set;
			int i;
			
			how = GPR(REGS_A0);	/* 1..3 */
			pset = GPR(REGS_A1);
			poset = GPR(REGS_A2);
			
			if (poset)
				sim_sigset_write(mem, poset, &ke->signaldb->context[ctx].blocked);
			
			if (pset) {
				sim_sigset_read(mem, pset, &set);
				if (debugging()) {
					printf("\tth%d.set=", ctx);
					sim_sigset_dump(&set, stdout);
					printf("\n");
				}
			
				if (how == 1) {
					for (i = 1; i <= MAXSIGNAL; i++)
						if (sim_sigset_member(&set, i))
							sim_sigset_add(&ke->signaldb->context[ctx].blocked, i);
				} else if (how == 2) {
					for (i = 1; i <= MAXSIGNAL; i++)
						if (sim_sigset_member(&set, i))
							sim_sigset_del(&ke->signaldb->context[ctx].blocked, i);
				} else if (how == 3) {
					ke->signaldb->context[ctx].blocked = set;
				}
			}
			
			if (debugging()) {
				printf("\tth%d.mask=", ctx);
				sim_sigset_dump(&ke->signaldb->context[ctx].blocked, stdout);
				printf("\n");
			}
			
			SET_GPR(7, 0);
			SET_GPR(2, 0);
			break;
		}
		
		
		
		/* 199 */
		case SIM_SYS_rt_sigsuspend:
		{
			word pmask = GPR(REGS_A0);
			struct sysevent_t *e;
			
			if (!pmask)
				fatal("sigsuspend with mask = NULL");
			
			/* replace blocked signals and suspend */
			ke->signaldb->context[ctx].backup = ke->signaldb->context[ctx].blocked;
			sim_sigset_read(mem, pmask, &ke->signaldb->context[ctx].blocked);
			ke_suspend_ctx(ke, ctx);
			
			/* debug */
			if (debugging()) {
				printf("\told mask = ");
				sim_sigset_dump(&ke->signaldb->context[ctx].backup, stdout);
				printf("\n\tsuspended mask = ");
				sim_sigset_dump(&ke->signaldb->context[ctx].blocked, stdout);
				printf("\n\tpending signals = ");
				sim_sigset_dump(&ke->signaldb->context[ctx].pending, stdout);
				printf("\n");
			}
			
			/* schedule wake up event */
			e = sysevent_create(ctx, SYSEVENT_SIGSUSPEND);
			e->for_signal.active = 1;
			syseventq_sched(ke, e);
			
			SET_GPR(7, 0);
			SET_GPR(2, 0);
			break;
		}



		/* 203 */
		case SIM_SYS_getcwd:
		{
			word pbuf, size;
			pbuf = GPR(REGS_A0);
			size = GPR(REGS_A1);

			if (strlen(KECTX.ld->cwd) >= size) {
				SET_GPR(REGS_V0, -1);
				SET_GPR(REGS_A3, ERANGE);
			} else {
				WRITE_STR(pbuf, KECTX.ld->cwd);
				SET_GPR(REGS_V0, 0);
			}
			error = check_syscall_error(regs);
			break;
		}



                /* 212 */
                case SIM_SYS_ftruncate64:
		{
			int fd;
			word hi, lo;
			fd = ld_translate_fd(ke, ctx, SGPR(REGS_A0));
			hi = GPR(REGS_A1);
			lo = GPR(REGS_A2);
			SET_GPR(REGS_V0, ftruncate(fd, lo));
			error = check_syscall_error(regs);
			break;
		}



		/* 220 */
		case SIM_SYS_fcntl64:
		{
			int fd;
			warning("fcntl - cmd=%d", (int) SGPR(REGS_A0));
			fd = ld_translate_fd(ke, ctx, SGPR(REGS_A0));
			SET_GPR(2, fcntl(fd, (int) GPR(5)));
			error = check_syscall_error(regs);
			break;
		}
		
		
		
		/* 246 */
		case SIM_SYS_exit_group:
		{
			ke_finish_ctx(ke, ctx);
			break;
		}

		
				
		default:
			fatal("invalid/not implemented system call '%s' (code %d) at 0x%x",
			      get_syscall_name(syscode), syscode, regs->regs_pc);
	}
	
	/* debug syscall */
	if (error) sprintf(tempstr, "errno=%s", map_value(&errno_map, GPR(2)));
	else sprintf(tempstr, "ret=0x%x", GPR(2));
	/////////
	/*printf("th%d 0x%x: syscall %s (a0=0x%x, a1=0x%x, a2=0x%x); %s\n",
		ctx, regs->regs_pc, get_syscall_name(syscode),
		GPR(4), GPR(5), GPR(6), tempstr), fflush(stdout);*/
	debug("th%d 0x%x: syscall %s (a0=0x%x, a1=0x%x, a2=0x%x); %s",
		ctx, regs->regs_pc, get_syscall_name(syscode),
		GPR(4), GPR(5), GPR(6), tempstr);
	
	/* process event queue and pending signals */
	syseventq_process(ke);
	signaldb_process(ke);
	
}
