#include "replacement_state.h"
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

/*
** This file implements the cache replacement state. Users can enhance the code
** below to develop their cache replacement ideas.
**
*/
////////////////////////////////////////////////////////////////////////////////
// The replacement state constructor:                                         //
// Inputs: number of sets, associativity, and replacement policy to use       //
// Outputs: None                                                              //
//                                                                            //
// DO NOT CHANGE THE CONSTRUCTOR PROTOTYPE                                    //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
CACHE_REPLACEMENT_STATE::CACHE_REPLACEMENT_STATE( UINT32 _sets, UINT32 _assoc, UINT32 _pol )
{

    numsets    = _sets;
    assoc      = _assoc;
    replPolicy = _pol;

    mytimer    = 0;

    InitReplacementState();
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// This function initializes the replacement policy hardware by creating      //
// storage for the replacement state on a per-line/per-cache basis.           //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
void CACHE_REPLACEMENT_STATE::InitReplacementState()
{
    // Create the state for sets, then create the state for the ways
    repl  = new LINE_REPLACEMENT_STATE* [ numsets ];

    // ensure that we were able to create replacement state
    assert(repl);

    // Create the state for the sets
    for(UINT32 setIndex=0; setIndex<numsets; setIndex++) 
    {
        repl[ setIndex ]  = new LINE_REPLACEMENT_STATE[ assoc ];

        for(UINT32 way=0; way<assoc; way++) 
        {
            // initialize stack position (for true LRU)
            repl[ setIndex ][ way ].LRUstackposition = way;
        }
    }

    // Contestants:  ADD INITIALIZATION FOR YOUR HARDWARE HERE
    lastStat=0; // Pointer to the last newly created stat, just used so that we
                // are less likely to erase recently freed stat entries 
                // associated with new PC/tid/policy combinations 

    curPolicy=0;  // Everone LRU by default at first
    for(UINT32 i=0; i<4; i++){ pSel[i]=511; }  // Set the pSel counters to be mildly LRU

    // Create the PC tracking stats table, # of entries depends on the cache
    // size (1024 sets = 1MB, 4096 sets = 4MB)
    if(numsets==1024){ 
      pcstats.resize(1024);  // 1024 tracking entries for 1MB case
    }else{
      pcstats.resize(2048);  // 2048 tracking entries for 4MB case
    }

    for(UINT32 setIndex=0; setIndex<numsets; setIndex++) 
    {
        for(UINT32 way=0; way<assoc; way++) 
        {
            // By default every cache line (in the tracker sets) doesnt have a
            // statID and is set to false streaming by default.
            repl[ setIndex ][ way ].PCStatID  = 0;
            repl[ setIndex ][ way ].streaming = false;
        }
    }
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// This function is called by the cache on every cache miss. The input        //
// arguments are the thread id, set index, pointers to ways in current set    //
// and the associativity.  We are also providing the PC, physical address,    //
// and accesstype should you wish to use them at victim selection time.       //
// The return value is the physical way index for the line being replaced.    //
// Return -1 if you wish to bypass LLC.                                       //
//                                                                            //
// vicSet is the current set. You can access the contents of the set by       //
// indexing using the wayID which ranges from 0 to assoc-1 e.g. vicSet[0]     //
// is the first way and vicSet[4] is the 4th physical way of the cache.       //
// Elements of LINE_STATE are defined in crc_cache_defs.h                     //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
INT32 CACHE_REPLACEMENT_STATE::GetVictimInSet( UINT32 tid, UINT32 setIndex, const LINE_STATE *vicSet, UINT32 assoc,
                                               Addr_t PC, Addr_t paddr, UINT32 accessType )
{
    // If no invalid lines, then replace based on replacement policy
    if( replPolicy == CRC_REPL_LRU ) 
    {
        return Get_LRU_Victim( setIndex );
    }
    else if( replPolicy == CRC_REPL_RANDOM )
    {
        return Get_Random_Victim( setIndex );
    }
    else if( replPolicy == CRC_REPL_CONTESTANT )
    {
        // Contestants:  ADD YOUR VICTIM SELECTION FUNCTION HERE
        return Get_Contestant_Victim( setIndex, tid, PC);
    }else if(replPolicy == CRC_DIP){
        return Get_LRU_Victim(setIndex);
    }

    // We should never get here
    assert(0);

    return -1; // Returning -1 bypasses the LLC
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// This function is called by the cache after every cache hit/miss            //
// The arguments are: the set index, the physical way of the cache,           //
// the pointer to the physical line (should contestants need access           //
// to information of the line filled or hit upon), the thread id              //
// of the request, the PC of the request, the accesstype, and finall          //
// whether the line was a cachehit or not (cacheHit=true implies hit)         //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
void CACHE_REPLACEMENT_STATE::UpdateReplacementState( 
    UINT32 setIndex, INT32 updateWayID, const LINE_STATE *currLine, 
    UINT32 tid, Addr_t PC, UINT32 accessType, bool cacheHit )
{
    // What replacement policy?
    if( replPolicy == CRC_REPL_LRU ) 
    {
        UpdateLRU( setIndex, updateWayID );
    }
    else if( replPolicy == CRC_REPL_RANDOM )
    {
        // Random replacement requires no replacement state update
    }
    else if( replPolicy == CRC_REPL_CONTESTANT )
    {
        // Contestants:  ADD YOUR UPDATE REPLACEMENT STATE FUNCTION HERE
        // Feel free to use any of the input parameters to make
        // updates to your replacement policy
        UpdateContestant( setIndex, updateWayID, tid, PC, cacheHit );
    }else if(replPolicy == CRC_DIP){
        UpdateDIP(setIndex, updateWayID, cacheHit);
    }   
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
//////// HELPER FUNCTIONS FOR REPLACEMENT UPDATE AND VICTIM SELECTION //////////
//                                                                            //
////////////////////////////////////////////////////////////////////////////////


////////////////////////////////////////////////////////////////////////////////
//  Function: Get_Contestant_Victim                                           //
//  Description: This function selects the victim for a given cache access.   //
//               The victim with either be the LRU victim or -1 to signify    //
//               that the line should not be cached within the L3.  There are //
//               3 different regions of the cache which could be accessed.    //
//                 1) The tracking region which defaults to LRU (w/filtering) //
//                 2) The tracking region which defaults to BBIP (w/scavange) //
//                 3) The "normal" region of the cache which will treat the   //
//                    access according to the current best policy, applying   //
//                    PC specific information in making its replacement       //
//                    decision.                                               //
////////////////////////////////////////////////////////////////////////////////
INT32 CACHE_REPLACEMENT_STATE::Get_Contestant_Victim( UINT32 setIndex, UINT32 tid, Addr_t PC){
  // Pick out the LRU victim for this given set (this could be overridden later
  // to bypass by setting it to -1)
  INT32 vict = Get_LRU_Victim(setIndex);
  
  // Determine which region (tracker sets or not) the access is to using set
  // index bits.  Bits used in the selection depends on the cache's configured
  // size.  Hardcoded now for the 4MB or 1MB contest sizes.
  if( (setIndex&0x1f) == ((setIndex>>(numsets==1024 ? 5 : 7))&0x1f) ){ // LRU+filt 
    // Access is to the tracking set which defaults to LRU for a specific
    // thread but uses PC-based information to filter out streaming requests.

    // Determine which thread defaults to LRU for this set (all other threads
    // will follow their default policy);
    UINT32 sampleThread = numsets==1024 ? 0 : (setIndex>>5)&0x3;

    // Cache miss to the LRU tracking set for this thread, increment
    // corresponding pSel counter.  High pSel = BBIP default policy.  Low pSel
    // = LRU policy.
    pSel[sampleThread] = pSel[sampleThread]==1023 ? 1023 : pSel[sampleThread]+1;
    // Set the overall default policy for the cache for this thread.  This only
    // has an effect if the just set value of pSel has just changed its MSB.
    curPolicy = (pSel[sampleThread] >= 512) ? (curPolicy | (0x1<<sampleThread)) : (curPolicy & (~(0x1<<sampleThread)));
   
    // Go through all of our tracking statistics and see if we have an entry
    // corresponding to the tid and PC being loaded under the current cache
    // replacement policy (note we forcibly zero out the bit for the current
    // thread so that it corresponds to the LRU default policy).
    for(UINT32 stat=1; stat<pcstats.size(); stat++){
      if(pcstats[stat].policy == (curPolicy&(~(0x1<<sampleThread))) && tid == pcstats[stat].tid && PC == pcstats[stat].PC){
        // Found a PC entry telling us to bypass?  If so, do so almost all of 
        // the time (small amount of time we dont bypass will allow us to keep
        // training).
        if(pcstats[stat].streaming>31 && rand()%32!=0) vict=-1;
        break;
      }
    }

    // If are going to cache the line we just missed on, and the line
    // previously in the cache has tracking information associated with it,
    // update the tracking info.  If its labeled as streaming, this means it
    // was never reused while in the L3.  Increment saturating counter
    // associated with the PCstats and decrement the numCached entry.
    if(repl[setIndex][vict].PCStatID != 0 && vict!=-1){
      if(repl[setIndex][vict].streaming) pcstats[repl[setIndex][vict].PCStatID].streaming 
        = pcstats[repl[setIndex][vict].PCStatID].streaming == 63 ? 63 : pcstats[repl[setIndex][vict].PCStatID].streaming+1; 
      pcstats[repl[setIndex][vict].PCStatID].numCached--;
    }

  }else if( (setIndex&0x1f) == (((setIndex>>(numsets==1024 ? 5 : 7))+4)&0x1f)){ // Bypass+scavage
    // Access is to the tracking set which defaults to bypass for a specific
    // thread but uses PC-based information to possibly override the default
    // decision to bypass.
    
    // Determine which thread defaults to bypass for this set (all other
    // threads will follow their default policy)
    UINT32 sampleThread = numsets==1024 ? 0 : (setIndex>>5)&0x3;

    // Cache miss to the bypass tracking set for this thread, decrement
    // corresponding pSel counter.  High pSel = BBIP default policy.  Low pSel
    // = LRU policy.
    pSel[sampleThread] = pSel[sampleThread]==0 ? 0 : pSel[sampleThread]-1;
    // Set the overall default policy for the cache for this thread.  This only
    // has an effect if the just set value of pSel has just changed its MSB.
    curPolicy = (pSel[sampleThread] >= 512) ? (curPolicy | (0x1<<sampleThread)) : (curPolicy & (~(0x1<<sampleThread)));
    
    // See if we have a tracker entry corresponding to the current tid, PC, and
    // policy that says NOT to bypass this line.
    bool overrideBypass=false;
    for(UINT32 stat=1; stat<pcstats.size(); stat++){
      if(pcstats[stat].policy == (curPolicy|(0x1<<sampleThread)) && tid == pcstats[stat].tid && PC == pcstats[stat].PC){
        if(pcstats[stat].streaming<32) overrideBypass=true;
        break;
      }
    }

    // If we don't find an override, bypass this line most of the time (not
    // always so we can keep training)
    if(!overrideBypass) vict = (rand()%32==0) ? vict : -1;
    
    // If are going to cache the line we just missed on, and the line
    // previously in the cache has tracking information associated with it,
    // update the tracking info.  If its labeled as streaming, this means it
    // was never reused while in the L3.  Increment saturating counter
    // associated with the PCstats and decrement the numCached entry.
    if(repl[setIndex][vict].PCStatID != 0 && vict!=-1){
      if(repl[setIndex][vict].streaming) pcstats[repl[setIndex][vict].PCStatID].streaming 
        = pcstats[repl[setIndex][vict].PCStatID].streaming == 63 ? 63 : pcstats[repl[setIndex][vict].PCStatID].streaming+1; 
      pcstats[repl[setIndex][vict].PCStatID].numCached--;
    }
  }else{
    // This access is to a set which follows the per-thread policy decided by
    // the tracker sets

    if(!(curPolicy&(0x1<<tid))){ // LRU+filter
      // Current policy for this thread is LRU + Streaming access filtering.
      
      // See if we have a PC tracking stat that labels this access as
      // streaming, if so, bypass it.
      for(UINT32 stat=1; stat<pcstats.size(); stat++){
        if(curPolicy == pcstats[stat].policy && tid == pcstats[stat].tid && PC == pcstats[stat].PC){
          if(pcstats[stat].streaming > 31) vict=-1;
          break;
        }
      }
    }else{ // Bypass+scavage
      // Current policy for this thread is BBIP + Scavange.
      
      // See if we have an override which will cause us NOT to bypass this line
      bool overrideBypass = false;
      for(UINT32 stat=1; stat<pcstats.size(); stat++){
        if(curPolicy == pcstats[stat].policy && tid == pcstats[stat].tid && PC == pcstats[stat].PC){
          if(pcstats[stat].streaming<32) overrideBypass=true;
          break;
        }
      }

      // If we dont find an override, bypass this line the majority of the time
      if(!overrideBypass) vict = (rand()%32==0) ? vict : -1;
    }
  }
  return vict;
}



////////////////////////////////////////////////////////////////////////////////
//  Function: UpdateContestant                                                //
//  Description: This function updates/creates the tracking stats for         // 
//               accesses which are hits (if access is to a tracker set).     //
//               Always updates LRU bits reguardless of which region of the   //
//               cache the access is in.                                      //
////////////////////////////////////////////////////////////////////////////////
void CACHE_REPLACEMENT_STATE::UpdateContestant( UINT32 setIndex, INT32 updateWayID, UINT32 tid, Addr_t PC, bool cacheHit ){
  
  // Determine which region (tracker sets or not) the access is to using set
  // index bits.  Bits used in the selection depends on the cache's configured
  // size.  Hardcoded now for the 4MB or 1MB contest sizes.
  if( (setIndex&0x1f) == ((setIndex>>(numsets==1024 ? 5 : 7))&0x1f)  ){  // LRU+filt
    // Access is to the tracking set which defaults to LRU for a specific
    // thread but uses PC-based information to filter out streaming requests.
    
    // Determine which thread defaults to LRU for this set (all other threads
    // will follow their default policy);
    UINT32 sampleThread = numsets==1024 ? 0 : (setIndex>>5)&0x3;
    // Determine the current policy for this set (zeroing out the corresponding
    // bit for the thread associated with this tracker set)
    UINT32 threadPolicy = curPolicy & (~(0x1<<sampleThread));

    if(!cacheHit){ // Cache Miss in tracker set
      // Create new tracking entry with defaults 
      repl[setIndex][updateWayID].PCStatID = 0;
      // Set streaming to true so it can get unset on an access.
      repl[setIndex][updateWayID].streaming = true;
     
      // See if we have a PC stat associated with this tid,pc, and policy
      // already
      for(UINT32 stat=1; stat<pcstats.size(); stat++){
        if(threadPolicy==pcstats[stat].policy && tid == pcstats[stat].tid && PC == pcstats[stat].PC){
          repl[setIndex][updateWayID].PCStatID = stat;
          pcstats[stat].numCached++;
          break;
        } // if tid/pc match
      } // for stats

      if(repl[setIndex][updateWayID].PCStatID == 0){
        // We didn't find an existing PC stat entry, create one if we can.
        for(UINT32 stat=(lastStat+1)%pcstats.size(); stat!=lastStat; stat=(stat+1)%pcstats.size()){
          if(pcstats[stat].numCached == 0 && stat!=0){
            pcstats[stat].policy = threadPolicy;
            pcstats[stat].tid = tid;
            pcstats[stat].PC = PC;
            pcstats[stat].streaming = 31;  // Set to be slightly non-streamin by default
            pcstats[stat].numCached = 1;
            repl[setIndex][updateWayID].PCStatID = stat;
            break;
          } // if numCached==0
        } // for stats
      } // if pcstatid==0

      // This was a cache hit or a miss where we decided NOT to bypass the
      // entry, so update the LRU bits accordingly.
      UpdateLRU(setIndex, updateWayID);
    }else{ // Cache Hit in tracker set
      // This was a cache hit or a miss where we decided NOT to bypass the
      // entry, so update the LRU bits accordingly.
      UpdateLRU(setIndex, updateWayID);
      // Mark cache line as no longer streaming (so we dont increment statid
      // upon eviction)
      repl[setIndex][updateWayID].streaming = false;
      // Decrement streaming counter to give credit to this access (multiple
      // accesses decrement the streaming counter multiple times)
      pcstats[repl[setIndex][updateWayID].PCStatID].streaming 
        = pcstats[repl[setIndex][updateWayID].PCStatID].streaming == 0 ? 0 : pcstats[repl[setIndex][updateWayID].PCStatID].streaming-1;
      //if(repl[setIndex][updateWayID].PCStatID != 0) pcstats[repl[setIndex][updateWayID].PCStatID].streaming = false;
    } // else cache hit in tracker
  }else if( (setIndex&0x1f) == (((setIndex>>(numsets==1024 ? 5 : 7))+4)&0x1f) ){ // Bypass+scavage
    // Access is to the tracking set which defaults to bypass for a specific
    // thread but uses PC-based information to possibly override the default
    // decision to bypass.
    
    // Determine which thread defaults to bypass for this tracker set (all other threads
    // will follow their default policy);
    UINT32 sampleThread = numsets==1024 ? 0 : (setIndex>>5)&0x3;
    // Determine the current policy for this set (setting the corresponding
    // bit for the thread associated with this tracker set)
    UINT32 threadPolicy = curPolicy | (0x1<<sampleThread);

    if(!cacheHit){ // Cache Miss in tracker set
      // Create new tracking entry with defaults
      repl[setIndex][updateWayID].PCStatID = 0;
      // Set streaming to true so it can get unset on an access.
      repl[setIndex][updateWayID].streaming = true;
     
      // See if we have an stat information associated with this PC,tid,and
      // policy combination
      for(UINT32 stat=1; stat<pcstats.size(); stat++){
        if(threadPolicy==pcstats[stat].policy && tid == pcstats[stat].tid && PC == pcstats[stat].PC){
          repl[setIndex][updateWayID].PCStatID = stat;
          pcstats[stat].numCached++;
          break;
        } // if tid/pc match
      } // for stats

      if(repl[setIndex][updateWayID].PCStatID == 0){
        // Didnt find a statistics associated with this PC, see if there's one
        // free so that we can create it
        for(UINT32 stat=(lastStat+1)%pcstats.size(); stat!=lastStat; stat=(stat+1)%pcstats.size()){
          if(pcstats[stat].numCached == 0 && stat!=0){
            pcstats[stat].policy = threadPolicy;
            pcstats[stat].tid = tid;
            pcstats[stat].PC = PC;
            pcstats[stat].streaming = 32; // Set to be slightly streaming by default
            pcstats[stat].numCached = 1;
            repl[setIndex][updateWayID].PCStatID = stat;
            break;
          } // if numCached==0
        } // for stats
      } // if pcstatid==0

      // This was a cache hit or a miss where we decided NOT to bypass the
      // entry, so update the LRU bits accordingly.
      UpdateLRU(setIndex, updateWayID);
    }else{ // Cache Hit in tracker set
      // This was a cache hit or a miss where we decided NOT to bypass the
      // entry, so update the LRU bits accordingly.
      UpdateLRU(setIndex, updateWayID);
      repl[setIndex][updateWayID].streaming = false;
      // Decrement streaming counter to give credit to this access (multiple
      // accesses decrement the streaming counter multiple times)
      pcstats[repl[setIndex][updateWayID].PCStatID].streaming 
        = pcstats[repl[setIndex][updateWayID].PCStatID].streaming == 0 ? 0 : pcstats[repl[setIndex][updateWayID].PCStatID].streaming-1;
      //if(repl[setIndex][updateWayID].PCStatID != 0) pcstats[repl[setIndex][updateWayID].PCStatID].streaming = false;
    } // else cache hit in tracker
  }else{ // Cache Hit or Miss in non-tracker set
    // No stats to update, most of the logic for the non-tracking sets is done
    // in the victim selection depending on the current policy.  Here we can
    // just update the LRU bits as normal.
    UpdateLRU(setIndex, updateWayID);
  }
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// This function finds the LRU victim in the cache set by returning the       //
// cache block at the bottom of the LRU stack. Top of LRU stack is '0'        //
// while bottom of LRU stack is 'assoc-1'                                     //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
INT32 CACHE_REPLACEMENT_STATE::Get_LRU_Victim( UINT32 setIndex )
{
    // Get pointer to replacement state of current set
    LINE_REPLACEMENT_STATE *replSet = repl[ setIndex ];

    INT32   lruWay   = 0;

    // Search for victim whose stack position is assoc-1
    for(UINT32 way=0; way<assoc; way++) 
    {
        if( replSet[way].LRUstackposition == (assoc-1) ) 
        {
            lruWay = way;
            break;
        }
    }

    // return lru way
    return lruWay;
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// This function finds a random victim in the cache set                       //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
INT32 CACHE_REPLACEMENT_STATE::Get_Random_Victim( UINT32 setIndex )
{
    INT32 way = (rand() % assoc);
    
    return way;
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// This function implements the LRU update routine for the traditional        //
// LRU replacement policy. The arguments to the function are the physical     //
// way and set index.                                                         //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
void CACHE_REPLACEMENT_STATE::UpdateLRU( UINT32 setIndex, INT32 updateWayID )
{
    // Determine current LRU stack position
    UINT32 currLRUstackposition = repl[ setIndex ][ updateWayID ].LRUstackposition;

    // Update the stack position of all lines before the current line
    // Update implies incremeting their stack positions by one
    for(UINT32 way=0; way<assoc; way++) 
    {
        if( repl[setIndex][way].LRUstackposition < currLRUstackposition ) 
        {
            repl[setIndex][way].LRUstackposition++;
        }
    }

    // Set the LRU stack position of new line to be zero
    repl[ setIndex ][ updateWayID ].LRUstackposition = 0;
}



////////////////////////////////////////////////////////////////////////////////
//  Function: UpdateDIP                                                       //
//  Description: This function implements the update policy for DIP           // 
////////////////////////////////////////////////////////////////////////////////
void CACHE_REPLACEMENT_STATE::UpdateDIP( UINT32 setIndex, INT32 updateWayID, bool cacheHit){
  if(!cacheHit){
    if(((setIndex>>5)&0x1f) == (setIndex&0x1f)){
      // LRU dedicated set
      pSel[0] = (pSel[0]==0x3ff) ? 0x3ff : pSel[0]+1;
      UpdateLRU(setIndex, updateWayID);
    }else if(((~(setIndex>>5))&0x1f) == (setIndex&0x1f)){
      // BIP dedicated set
      pSel[0] = (pSel[0]==0) ? 0 : pSel[0]-1;
      UpdateBIP(setIndex, updateWayID);
    }else{
      // Follower set
      if(pSel[0]&0x200) UpdateBIP(setIndex, updateWayID);
      else UpdateLRU(setIndex, updateWayID);
    }
  }else{
    // Standard LRU approach
    UpdateLRU(setIndex, updateWayID);
  }
}

////////////////////////////////////////////////////////////////////////////////
//  Function: UpdateBIP                                                       //
//  Description: This function implements the update policy for BIP where     //
//               only 1/32 of the time will it update the lru bits, causing   // 
//               the newly inserted line to assume the MRU position.          //
////////////////////////////////////////////////////////////////////////////////
void CACHE_REPLACEMENT_STATE::UpdateBIP( UINT32 setIndex, INT32 updateWayID){
  if(rand()%32==0){
    UpdateLRU(setIndex, updateWayID);
  }
  // No other action needs to be taken for BIP, this *is* the LRU line
  // (assoc-1) so we dont need to update the LRU stack
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// The function prints the statistics for the cache                           //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////

ostream & CACHE_REPLACEMENT_STATE::PrintStats(ostream &out)
{

    out<<"=========================================================="<<endl;
    out<<"=========== Replacement Policy Statistics ================"<<endl;
    out<<"=========================================================="<<endl;

    // CONTESTANTS:  Insert your statistics printing here
    return out;
    
}

