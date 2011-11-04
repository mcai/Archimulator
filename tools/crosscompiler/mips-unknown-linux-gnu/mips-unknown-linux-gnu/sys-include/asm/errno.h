/*
 * This file is subject to the terms and conditions of the GNU General Public
 * License.  See the file "COPYING" in the main directory of this archive
 * for more details.
 *
 * Copyright (C) 1995, 1999, 2001, 2002 by Ralf Baechle
 */
#ifndef _ASM_ERRNO_H
#define _ASM_ERRNO_H

#ifndef _LINUX_ERRNO_H
 #include <linux/errno.h>
#endif


#undef	EDEADLK
#undef	ENAMETOOLONG
#undef	ENOLCK
#undef	ENOSYS
#undef	ENOTEMPTY
#undef	ELOOP
#undef	EWOULDBLOCK
#undef	ENOMSG
#undef	EIDRM
#undef	ECHRNG
#undef	EL2NSYNC
#undef	EL3HLT
#undef	EL3RST
#undef	ELNRNG
#undef	EUNATCH
#undef	ENOCSI
#undef	EL2HLT
#undef	EBADE
#undef	EBADR
#undef	EXFULL
#undef	ENOANO
#undef	EBADRQC
#undef	EBADSLT
#undef	EDEADLOCK
#undef	EBFONT
#undef	ENOSTR
#undef	ENODATA
#undef	ETIME
#undef	ENOSR
#undef	ENONET
#undef	ENOPKG
#undef	EREMOTE
#undef	ENOLINK
#undef	EADV
#undef	ESRMNT
#undef	ECOMM
#undef	EPROTO
#undef	EMULTIHOP
#undef	EDOTDOT
#undef	EBADMSG
#undef	EOVERFLOW
#undef	ENOTUNIQ
#undef	EBADFD
#undef	EREMCHG
#undef	ELIBACC
#undef	ELIBBAD
#undef	ELIBSCN
#undef	ELIBMAX
#undef	ELIBEXEC
#undef	EILSEQ
#undef	ERESTART
#undef	ESTRPIPE
#undef	EUSERS
#undef	ENOTSOCK
#undef	EDESTADDRREQ
#undef	EMSGSIZE
#undef	EPROTOTYPE
#undef	ENOPROTOOPT
#undef	EPROTONOSUPPORT
#undef	ESOCKTNOSUPPORT
#undef	EOPNOTSUPP
#undef	EPFNOSUPPORT
#undef	EAFNOSUPPORT
#undef	EADDRINUSE
#undef	EADDRNOTAVAIL
#undef	ENETDOWN
#undef	ENETUNREACH
#undef	ENETRESET
#undef	ECONNABORTED
#undef	ECONNRESET
#undef	ENOBUFS
#undef	EISCONN
#undef	ENOTCONN
#undef	ESHUTDOWN
#undef	ETOOMANYREFS
#undef	ETIMEDOUT
#undef	ECONNREFUSED
#undef	EHOSTDOWN
#undef	EHOSTUNREACH
#undef	EALREADY
#undef	EINPROGRESS
#undef	ESTALE
#undef	EUCLEAN
#undef	ENOTNAM
#undef	ENAVAIL
#undef	EISNAM
#undef	EREMOTEIO
#undef	EDQUOT
#undef	ENOMEDIUM
#undef	EMEDIUMTYPE
#undef	ECANCELED
#undef	ENOKEY
#undef	EKEYEXPIRED
#undef	EKEYREVOKED
#undef	EKEYREJECTED
#undef	EOWNERDEAD
#undef	ENOTRECOVERABLE

/*
 * These error numbers are intended to be MIPS ABI compatible
 */


#define	ENOMSG		35	/* No message of desired type */
#define	EIDRM		36	/* Identifier removed */
#define	ECHRNG		37	/* Channel number out of range */
#define	EL2NSYNC	38	/* Level 2 not synchronized */
#define	EL3HLT		39	/* Level 3 halted */
#define	EL3RST		40	/* Level 3 reset */
#define	ELNRNG		41	/* Link number out of range */
#define	EUNATCH		42	/* Protocol driver not attached */
#define	ENOCSI		43	/* No CSI structure available */
#define	EL2HLT		44	/* Level 2 halted */
#define	EDEADLK		45	/* Resource deadlock would occur */
#define	ENOLCK		46	/* No record locks available */
#define	EBADE		50	/* Invalid exchange */
#define	EBADR		51	/* Invalid request descriptor */
#define	EXFULL		52	/* Exchange full */
#define	ENOANO		53	/* No anode */
#define	EBADRQC		54	/* Invalid request code */
#define	EBADSLT		55	/* Invalid slot */
#define	EDEADLOCK	56	/* File locking deadlock error */
#define	EBFONT		59	/* Bad font file format */
#define	ENOSTR		60	/* Device not a stream */
#define	ENODATA		61	/* No data available */
#define	ETIME		62	/* Timer expired */
#define	ENOSR		63	/* Out of streams resources */
#define	ENONET		64	/* Machine is not on the network */
#define	ENOPKG		65	/* Package not installed */
#define	EREMOTE		66	/* Object is remote */
#define	ENOLINK		67	/* Link has been severed */
#define	EADV		68	/* Advertise error */
#define	ESRMNT		69	/* Srmount error */
#define	ECOMM		70	/* Communication error on send */
#define	EPROTO		71	/* Protocol error */
#define	EDOTDOT		73	/* RFS specific error */
#define	EMULTIHOP	74	/* Multihop attempted */
#define	EBADMSG		77	/* Not a data message */
#define	ENAMETOOLONG	78	/* File name too long */
#define	EOVERFLOW	79	/* Value too large for defined data type */
#define	ENOTUNIQ	80	/* Name not unique on network */
#define	EBADFD		81	/* File descriptor in bad state */
#define	EREMCHG		82	/* Remote address changed */
#define	ELIBACC		83	/* Can not access a needed shared library */
#define	ELIBBAD		84	/* Accessing a corrupted shared library */
#define	ELIBSCN		85	/* .lib section in a.out corrupted */
#define	ELIBMAX		86	/* Attempting to link in too many shared libraries */
#define	ELIBEXEC	87	/* Cannot exec a shared library directly */
#define	EILSEQ		88	/* Illegal byte sequence */
#define	ENOSYS		89	/* Function not implemented */
#define	ELOOP		90	/* Too many symbolic links encountered */
#define	ERESTART	91	/* Interrupted system call should be restarted */
#define	ESTRPIPE	92	/* Streams pipe error */
#define	ENOTEMPTY	93	/* Directory not empty */
#define	EUSERS		94	/* Too many users */
#define	ENOTSOCK	95	/* Socket operation on non-socket */
#define	EDESTADDRREQ	96	/* Destination address required */
#define	EMSGSIZE	97	/* Message too long */
#define	EPROTOTYPE	98	/* Protocol wrong type for socket */
#define	ENOPROTOOPT	99	/* Protocol not available */
#define	EPROTONOSUPPORT	120	/* Protocol not supported */
#define	ESOCKTNOSUPPORT	121	/* Socket type not supported */
#define	EOPNOTSUPP	122	/* Operation not supported on transport endpoint */
#define	EPFNOSUPPORT	123	/* Protocol family not supported */
#define	EAFNOSUPPORT	124	/* Address family not supported by protocol */
#define	EADDRINUSE	125	/* Address already in use */
#define	EADDRNOTAVAIL	126	/* Cannot assign requested address */
#define	ENETDOWN	127	/* Network is down */
#define	ENETUNREACH	128	/* Network is unreachable */
#define	ENETRESET	129	/* Network dropped connection because of reset */
#define	ECONNABORTED	130	/* Software caused connection abort */
#define	ECONNRESET	131	/* Connection reset by peer */
#define	ENOBUFS		132	/* No buffer space available */
#define	EISCONN		133	/* Transport endpoint is already connected */
#define	ENOTCONN	134	/* Transport endpoint is not connected */
#define	EUCLEAN		135	/* Structure needs cleaning */
#define	ENOTNAM		137	/* Not a XENIX named type file */
#define	ENAVAIL		138	/* No XENIX semaphores available */
#define	EISNAM		139	/* Is a named type file */
#define	EREMOTEIO	140	/* Remote I/O error */
#define EINIT		141	/* Reserved */
#define EREMDEV		142	/* Error 142 */
#define	ESHUTDOWN	143	/* Cannot send after transport endpoint shutdown */
#define	ETOOMANYREFS	144	/* Too many references: cannot splice */
#define	ETIMEDOUT	145	/* Connection timed out */
#define	ECONNREFUSED	146	/* Connection refused */
#define	EHOSTDOWN	147	/* Host is down */
#define	EHOSTUNREACH	148	/* No route to host */
#define	EWOULDBLOCK	EAGAIN	/* Operation would block */
#define	EALREADY	149	/* Operation already in progress */
#define	EINPROGRESS	150	/* Operation now in progress */
#define	ESTALE		151	/* Stale NFS file handle */
#define ECANCELED	158	/* AIO operation canceled */

/*
 * These error are Linux extensions.
 */
#define ENOMEDIUM	159	/* No medium found */
#define EMEDIUMTYPE	160	/* Wrong medium type */
#define	ENOKEY		161	/* Required key not available */
#define	EKEYEXPIRED	162	/* Key has expired */
#define	EKEYREVOKED	163	/* Key has been revoked */
#define	EKEYREJECTED	164	/* Key was rejected by service */

/* for robust mutexes */
#define    EOWNERDEAD  165 /* Owner died */
#define    ENOTRECOVERABLE 166 /* State not recoverable */

#define EDQUOT		1133	/* Quota exceeded */


#endif /* _ASM_ERRNO_H */
