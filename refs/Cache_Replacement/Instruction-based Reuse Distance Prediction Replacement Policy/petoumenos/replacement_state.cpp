#include "replacement_state.h"

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
//                                                                            //
// Modified by:                                                               //
// Pavlos Petoumenos <ppetoumenos@ece.upatras.gr>                             //
// Georgios Keramidas <keramidas@ece.upatras.gr>                              //
// Stefanos Kaxiras <kaxiras@ece.upatras.gr>                                  //
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
    accessesCounterLow  = 0;
    accessesCounterHigh = 1;
    set_shift = CRC_FloorLog2( numsets );
    predictor = new IBRDPredictor( IBRDP_SETS, IBRDP_WAYS );
    rdsampler = new RDSampler( SAMPLER_PERIOD, SAMPLER_MAX_RD, predictor );
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
        return Get_IBRDP_Victim( setIndex, PC, paddr, accessType );
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
        UpdateIBRDP( setIndex, updateWayID, currLine, PC, accessType, cacheHit );
    }
    
    
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
//////// HELPER FUNCTIONS FOR REPLACEMENT UPDATE AND VICTIM SELECTION //////////
//                                                                            //
////////////////////////////////////////////////////////////////////////////////


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

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// This function finds the IBRDP victim in the cache set                      //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
INT32 CACHE_REPLACEMENT_STATE::Get_IBRDP_Victim( UINT32 setIndex, Addr_t PC, Addr_t paddr, UINT32 accessType )
{
    UINT32 way;
                                // All local variables hold unquantized values
    UINT32 now;                 // Current time
    UINT32 timestamp;           // The line's time of last use
    UINT32 prediction;          // The line's reuse distance prediction
    UINT32 time_left;           // The line's predicted time left until reuse
    UINT32 time_idle;           // The line's elapsed time since last use

    INT32 victim_way  = 0;      // The line which was used farthest in the past
                                // or will be used farthest in the future
    UINT32 victim_time = 0;     // The idle or left time for the victim_way

    UINT32 new_prediction = 0 ; // new_prediction is set to zero, unless 
                                // Selective Caching is activated. That forces 
                                // all conditions which control cache bypassing
                                // to be always false

#if defined( SELECTIVE_CACHING ) 
    new_prediction = predictor->Lookup( TransformPC( PC ) );
#endif

    // If the predicted quantized reuse distance for the new line has the 
    // maximum value, it almost certainly doesn't fit in the cache
    if( new_prediction == MAX_VALUE_PREDICTION )
    {
        victim_way = -1;
    }
    else
    {
        // We search the set to find the line which will be used farthest in 
        // the future / was used farthest in the past
        for( way = 0; way < assoc; way++ )
        {
            // ---> Un-Quantize all the needed variables <---

            // 'timestamp' refers to a point in the past, so it should be less 
            // than 'accessesCounterHigh'. If this is not the case, it means 
            // that the accesses counter has overflowed since the last access,
            // so we have to add to accessesCounterHigh 'MAX_VALUE_TIMESTAMP+1'
            if( repl[setIndex][way].timestamp > accessesCounterHigh )
                now = UnQuantizeTimestamp( accessesCounterHigh + MAX_VALUE_TIMESTAMP + 1 );
            else
                now = UnQuantizeTimestamp( accessesCounterHigh );

            timestamp  = UnQuantizeTimestamp( repl[setIndex][way].timestamp );
            prediction = UnQuantizePrediction( repl[setIndex][way].prediction );

            // ---> Look at the future <---

            // Calculate Time Left until next access
            if( timestamp + prediction > now )
                time_left = timestamp + prediction - now;
            else
                time_left = 0;
            
            // If the line is going to be used farther in the future than the
            // previously selected victim, then we replace the selected victim
            if( time_left > victim_time )
            {
                victim_time = time_left;
                victim_way  = way;
            }

            // ---> Look at the past <---

            // Calculate time passed since last access
            time_idle = now - timestamp;

            // If the line was used farther in the past than the previously
            // selected victim, then we replace the selected victim
            if( time_idle > victim_time )
            {
                victim_time = time_idle;
                victim_way  = way;
            }
        }
        // If the reuse-distance prediction for the new line is greater than 
        // the victim_time, then the new line is less likely to fit in the
        // cache than the selected victim, so we choose to bypass the cache
        if( UnQuantizePrediction( new_prediction ) > victim_time )
            victim_way = -1;
    }

    // If we bypass the cache, then the ReplPolicy won't be updated, so we 
    // have to update some things from here
    if (victim_way == -1)
        UpdateOnEveryAccess( TransformAddress( paddr >> 6 ), TransformPC( PC ), accessType );

    return victim_way;

}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// This function implements the IBRDP update routine                          //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
void CACHE_REPLACEMENT_STATE::UpdateIBRDP( UINT32 setIndex, INT32 updateWayID, 
    const LINE_STATE *currLine, Addr_t PC, UINT32 accessType, bool cacheHit )
{
    UINT32 prediction = 0;
    UINT32 myPC = TransformPC( PC );
    UINT32 myAddress = TransformAddress( ( currLine->tag << set_shift ) + setIndex );

    // Update the accesses counter and the sampler
    UpdateOnEveryAccess( myAddress, myPC, accessType );

    // Get the prediction information for the accessed line
    if(( accessType == ACCESS_LOAD ) || ( accessType == ACCESS_STORE ))
        prediction = predictor->Lookup( myPC );

    // Fill the accessed line with the replacement policy information
    // For Loads and Stores we update both fields
    // For Ifetches we update with real info only the timestamp field
    //    the prediction field is set to zero (==no prediction)
    // For Writebacks, we give dummy values to both fields
    //    so that the line will be almost certainly replaced upon
    //    the next miss
    if( accessType != ACCESS_WRITEBACK )
    {
        repl[setIndex][updateWayID].timestamp  = accessesCounterHigh;
        repl[setIndex][updateWayID].prediction = prediction;
    }
    else 
    {
        repl[setIndex][updateWayID].timestamp  = 0;
        repl[setIndex][updateWayID].prediction = MAX_VALUE_PREDICTION;
    }
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// This function updates the IBRDP elements that must be updated              //
// for every access: the accessesCounter and the the Sampler                  //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
void CACHE_REPLACEMENT_STATE::UpdateOnEveryAccess( UINT32 address, UINT32 PC, UINT32 accessType )
{
    if(( accessType == ACCESS_LOAD ) || ( accessType == ACCESS_STORE ))
    {
        accessesCounterLow++;
        if( accessesCounterLow == QUANTUM_TIMESTAMP )
        {
            accessesCounterLow = 0;
            accessesCounterHigh++;
            if( accessesCounterHigh > MAX_VALUE_TIMESTAMP )
                accessesCounterHigh = 0;
        }
        rdsampler->Update( address, PC, accessType );
    }
}


//---------------------------------------------------------------------------///
//---------------------------------------------------------------------------///
//                         REUSE DISTANCE SAMPLER                            ///
//---------------------------------------------------------------------------///
//---------------------------------------------------------------------------///

////////////////////////////////////////////////////////////////////////////////
// _max_rd is always 1 larger than the longest reuse distance not truncated   //
// due to the limited width of the prediction, that is equal to:              //
// (MAX_VALUE_PREDICTION + 1) * QUANTUM_PREDICTION                            //
// Based on that the RDSampler allocates enough entries so that it holds      //
// each sample for a time equal to _max_rd cache accesses                     //
////////////////////////////////////////////////////////////////////////////////
RDSampler::RDSampler( UINT32 _period, UINT32 _max_rd, IBRDPredictor *_predictor )
{
    UINT32 i;

    period = _period;
    size   = _max_rd / _period;
    predictor = _predictor;

    sampling_counter = 0;

    sampler = new RDSamplerEntry[ size ];
    assert(sampler != NULL);

    // Initialize entries
    for( i = 0; i < size; i++ )
    {
        sampler[i].valid = 0;
        sampler[i].FifoPosition = i;
    }
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// This function updates the Sampler. It searches for a previously taken      //
// sample for the currently accessed address and if it finds one it updates   //
// the predictor. Also it checks whether we should take a new sample.         //
// When we take a sample, if the oldest (soon to be evicted) entry is still   //
// valid, its reuse distance is longer than the MAX_VALUE_PREDICTION so we    //
// update the predictor with this maximum value, even though we don't know    //
// its exact non-quantized reuse distance.                                    //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
void RDSampler::Update( UINT32 address, UINT32 pc, UINT32 accessType )
{
    UINT32 observation;
    UINT32 position;
    UINT32 index;
    UINT32 j;

    //
    // ---> Match <---
    //
    
    // Search the sampler for a previous sample of this address
    // Stop when we've checked all entries or when we've found a previous sample
    for( index = 0; index < size; index++ )
        if( ( sampler[index].valid ) && ( sampler[index].address == address ) )
            break;

    // If we found a sample, invalidate the entry, determine the observed
    // reuse distance and update the predictor
    // Optimization: penalize stores by artificially increasing their positions
    if( index < size )
    {
        sampler[index].valid = 0;

        if( accessType == ACCESS_STORE )
            position = sampler[index].FifoPosition + 8;
        else
            position = sampler[index].FifoPosition;

        observation = QuantizePrediction( position * period );
        predictor->Update( sampler[index].pc, observation );
    }

    //
    // ---> Sample <---
    //

    // It's time for a new sample?
    if( sampling_counter == 0 )
    {
        // Get the oldest entry
        for( index = 0; index < size; index++ )
            if( sampler[index].FifoPosition == ( size - 1 ) )
                break;

        // If the oldest entry is still valid, update the 
        // predictor with the maximum prediction value
        if( sampler[index].valid == 1 )
            predictor->Update( sampler[index].pc, MAX_VALUE_PREDICTION );

        // Update the FIFO Queue
        for( j = 0; j < size; j++ )
            sampler[j].FifoPosition++;

        // Fill the new entry
        sampler[index].valid = 1;
        sampler[index].FifoPosition = 0;
        sampler[index].pc = pc;
        sampler[index].address = address;

        sampling_counter = period - 1;
    }
    else
    {
        sampling_counter--;
    }
}

//---------------------------------------------------------------------------///
//---------------------------------------------------------------------------///
//               INSTRUCTION BASED REUSE DISTANCE PREDICTOR                  ///
//---------------------------------------------------------------------------///
//---------------------------------------------------------------------------///
IBRDPredictor::IBRDPredictor( UINT32 _numsets, UINT32 _assoc )
{
    UINT32 set, way;

    numsets   = _numsets;
    assoc     = _assoc;
    set_mask  = numsets - 1;
    set_shift = CRC_FloorLog2( numsets );
    
    predictor = new IBRDP_Entry* [ numsets ];
    assert(predictor != NULL);

    for( set = 0; set < numsets; set++ )
    {
        predictor[set] = new IBRDP_Entry [ assoc ];
        assert(predictor[set] != NULL);
        for( way = 0; way < assoc; way++ )
        {
            predictor[set][way].valid = 0;
            predictor[set][way].tag = 0;
            predictor[set][way].prediction = 0;
            predictor[set][way].confidence = 0;
            predictor[set][way].StackPosition = way;
        }
    }
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// Lookup searches for an IBRDPredictor entry for the given PC.               //
// If it finds one, it returns the prediction stored in the entry.            //
// If not, it returns -1.                                                     //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
UINT32 IBRDPredictor::Lookup( UINT32 pc )
{
    UINT32 set, way;
    UINT32 prediction = 0;

    set = pc & set_mask;
    way = FindEntry( pc );
    
    if( way != assoc )
        if( predictor[set][way].confidence >= SAFE_CONFIDENCE )
            prediction = predictor[set][way].prediction;
    
    return prediction;
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// Update finds the entry associated with the given pc, or allocates a new one//
// and then it updates its prediction: If the observation is equal to the     //
// prediction, it increases the confidence in our prediction. If the          //
// observation is different than the prediction, it decreases the confidence. //
// If the confidence is already zero, then we replace the prediction          //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
void IBRDPredictor::Update( UINT32 pc, UINT32 observation )
{
    UINT32 set, way;

    set = pc & set_mask;
    way = FindEntry( pc );

    // If no entry was found, get a new one, and initialize it
    if( way == assoc )
    {
        way = GetEntry( pc );
        predictor[set][way].prediction = observation;
        predictor[set][way].confidence = 0;
    }
    // else update the entry
    else
    {
        if( predictor[set][way].prediction == observation )
        {
            if ( predictor[set][way].confidence < MAX_CONFIDENCE )
                predictor[set][way].confidence++;
        }
        else
        {
            if( predictor[set][way].confidence == 0 )
                predictor[set][way].prediction = observation;
            else
                predictor[set][way].confidence--;
        }
    }
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// FindEntry searches the predictor to find an entry associated with the      //
// given PC. Afterwards it updates the LRU StackPositions of the entries.     //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
UINT32 IBRDPredictor::FindEntry( UINT32 pc )
{ 
    UINT32 myway, way;
    UINT32 set   = pc & set_mask;
    UINT32 tag   = pc >> set_shift;

    // Search the set, to find a matching entry
    for( way = 0; way < assoc; way++ )
        if( predictor[set][way].tag == tag )
            break;

    myway = way;

    // If we found an entry, update the LRU Stack Positions
    if( myway != assoc )
    {
        for( way = 0; way < assoc; way++)
            if( predictor[set][way].StackPosition < predictor[set][myway].StackPosition)
                predictor[set][way].StackPosition++;

        predictor[set][myway].StackPosition = 0;
    }

    return myway;
}

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// GetEntry is called when we want to allocate a new entry. It searches for   //
// the LRU Element in the list, and re-initializes it.                        //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
UINT32 IBRDPredictor::GetEntry( UINT32 pc )
{
    UINT32 way;
    UINT32 myway = assoc;
    UINT32 set   = pc & set_mask;
    UINT32 tag   = pc >> set_shift;

    // Search the set to find the LRU entry
    // At the same time, update the LRU Stack Positions
    for( way = 0; way < assoc ; way++ )
    {
        if( predictor[set][way].StackPosition == ( assoc - 1 ) )
            myway = way;
        else
            predictor[set][way].StackPosition++;
    }
    assert( myway != assoc );

    // Initialize the new entry    
    predictor[set][myway].valid = 1;
    predictor[set][myway].tag = tag;
    predictor[set][myway].StackPosition = 0;

    return myway;
}

