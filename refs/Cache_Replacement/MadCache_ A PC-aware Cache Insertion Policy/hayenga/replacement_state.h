#ifndef REPL_STATE_H
#define REPL_STATE_H
///////////////////////////////////////////////////////////////////////////////
//   __  __           _  ____           _                                    //
//  |  \/  | __ _  __| |/ ___|__ _  ___| |__   ___                           //
//  | |\/| |/ _` |/ _` | |   / _` |/ __| '_ \ / _ \                          //
//  | |  | | (_| | (_| | |__| (_| | (__| | | |  __/                          //
//  |_|  |_|\__,_|\__,_|\____\__,_|\___|_| |_|\___|                          //
//                                                                           //
// Authors: Mitch Hayenga and Andrew Nere                                    //
// Emails: hayenga@wisc.edu or nere@wisc.edu                                 //
// Description: These sources implement the MadCache replacement policy.     //
//              MadCache can be thought of as a refinement of TADIP-F which, //
//              in addition to having a default policy of primarily          //
//              bypassing or primarily LRU per thread, also utilizes PC      //
//              information to categorize L3 accesses as streaming or        //
//              nonstreaming.  This allows PC-specific overrides to the      //
//              general cache replacement algorithm.  See the JWAC-1 paper   //
//              for a fuller description of the general algorithm and the    //
//              motivations behind it.                                       //
///////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// This file is distributed as part of the Cache Replacement Championship     //
// workshop held in conjunction with ISCA'2010.                               //
//                                                                            //
//                                                                            //
// Everyone is granted permission to copy, modify, and/or re-distribute       //
// this software.                                                             //
//                                                                            //
// Please contact Aamer Jaleel <ajaleel@gmail.com> should you have any        //
// questions                                                                  //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

#include <cstdlib>
#include <cassert>
#include "utils.h"
#include "crc_cache_defs.h"

#include <vector>

// Replacement Policies Supported
typedef enum 
{
    CRC_REPL_LRU        = 0,
    CRC_REPL_RANDOM     = 1,
    CRC_REPL_CONTESTANT = 2,
    CRC_DIP=3
} ReplacemntPolicy;

// Replacement State Per Cache Line
typedef struct
{
    UINT32  LRUstackposition;

    // CONTESTANTS: Add extra state per cache line here
    UINT32 PCStatID; // Only used in tracker sets - Identifies tracking stat id if not 0
    bool streaming; // Only used in tracker sets - Identifies if line has been accessed since replacement
} LINE_REPLACEMENT_STATE;


// The implementation for the cache replacement policy
class CACHE_REPLACEMENT_STATE
{

  private:
    UINT32 numsets;
    UINT32 assoc;
    UINT32 replPolicy;
    
    LINE_REPLACEMENT_STATE   **repl;

    COUNTER mytimer;  // tracks # of references to the cache

    // CONTESTANTS:  Add extra state for cache here
    
    // Data structure for tracking PC info on L3 cache accesses.
    // Lookup is done based upon the tid, PC, and current policy for a given
    // thread.
    struct PCEntry{
      // Default Constructor - zeroed out and set to be slightly non-streaming
      PCEntry() : numCached(0), policy(0), tid(0), PC(0), streaming(31) {}

      UINT32 numCached;  // Number of elements currently held within the 
                         // tracker sets which were loaded by this
                         // PC/tid/policy pair

      UINT32 policy;     // Cache policy corresponding this tracking 
                         // information. Low-bits are set corresponding to the
                         // replacement policy of each thread. 1==LRU, 0=BYP
      
      UINT32 tid;        // Thread ID associated with this PC entry
      Addr_t PC;         // PC associated with these load/tracker stats
      UINT32 streaming;  // 6-bit saturating counter.  Value in lower half =
                         // nonstreaming.  Upper half = streaming.
    };
    vector<PCEntry> pcstats; // Table of tracked PC info, sized based upon 
                             // cache configuration
    UINT32 lastStat;         // Pointer to the last entry we created in the 
                             // tracking table, just used so that we don't
                             // replace the newest entries if they happen to
                             // free up fast.
    UINT32 curPolicy; // Current policy of the 4 threads - Most signifigant
                      // bits of the of the pSel counters
    UINT32 pSel[4];   // 10-bit saturating counters for each thread.  
                      // Just like in TADIP-F.  MSB determines policy.

  public:

    // The constructor CAN NOT be changed
    CACHE_REPLACEMENT_STATE( UINT32 _sets, UINT32 _assoc, UINT32 _pol );

    INT32  GetVictimInSet( UINT32 tid, UINT32 setIndex, const LINE_STATE *vicSet, UINT32 assoc, Addr_t PC, Addr_t paddr, UINT32 accessType );
    void   UpdateReplacementState( UINT32 setIndex, INT32 updateWayID );

    void   SetReplacementPolicy( UINT32 _pol ) { replPolicy = _pol; } 
    void   IncrementTimer() { mytimer++; } 

    void   UpdateReplacementState( UINT32 setIndex, INT32 updateWayID, const LINE_STATE *currLine, 
                                   UINT32 tid, Addr_t PC, UINT32 accessType, bool cacheHit );

    ostream&   PrintStats( ostream &out);

  private:
    
    void   InitReplacementState();
    INT32  Get_Random_Victim( UINT32 setIndex );

    INT32  Get_LRU_Victim( UINT32 setIndex );
    void   UpdateLRU( UINT32 setIndex, INT32 updateWayID );

    // Function to get the victim, depending on the PC of the access and which
    // set is accessed, will either return the LRU vict or -1 to bypass.
    INT32  Get_Contestant_Victim( UINT32 setIndex, UINT32 tid, Addr_t PC );

    // Function to update tracking stats, allocate new PC trackng info, etc.
    // Always updates LRU bits accordingly.
    void UpdateContestant( UINT32 setIndex, INT32 updateWayID, UINT32 tid, Addr_t PC, bool cacheHit );
    
    // Following two functions used for our implementation of DIP which is also
    // included within these files
    void UpdateDIP(UINT32 setIndex, INT32 updateWayID, bool cacheHit);
    void UpdateBIP(UINT32 setIndex, INT32 updateWayID);
};


#endif
