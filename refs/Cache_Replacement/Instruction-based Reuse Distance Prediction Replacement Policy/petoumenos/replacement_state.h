#ifndef REPL_STATE_H
#define REPL_STATE_H

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

#include <cstdlib>
#include <cassert>
#include "utils.h"
#include "crc_cache_defs.h"

// MAX and SAFE values for the IbRDPredictor confidence counters
#define MAX_CONFIDENCE 3
#define SAFE_CONFIDENCE 0

// Max value and quantization granularity for the prediction
#define MAX_VALUE_PREDICTION 15
#define QUANTUM_PREDICTION 8192

// Max value and quantization granulariry for the timestamp
#define MAX_VALUE_TIMESTAMP 7
#define QUANTUM_TIMESTAMP 16384

// Sampling Period and max reuse distance that the sampler must be able to hold
#define SAMPLER_PERIOD 4096
#define SAMPLER_MAX_RD ( ( MAX_VALUE_PREDICTION + 1 ) * QUANTUM_PREDICTION )

// Number of bits that we keep for the PC and the address
// We enforce these numbers of bits by calling TransformAddress and
// TransformPC from the entry-points of our code so that the only values we 
// use throughout our code are limited to these numbers of bits
#define BITS_PC 20
#define BITS_ADDR 26

// Sets and associativity of the IbRDPredictor storage
#define IBRDP_SETS 16
#define IBRDP_WAYS 16

// Selective Caching controls the use of cache bypassing
#define SELECTIVE_CACHING

//---------------------------------------------------------------------------///
//---------------------------------------------------------------------------///
//---            INSTRUCTION BASED REUSE DISTANCE PREDICTOR               ---///
//---------------------------------------------------------------------------///
//---------------------------------------------------------------------------///
typedef struct
{
    UINT32 valid;               // Valid: 1 bit
    UINT32 tag;                 // Tag: 20 bits PC - 4 bits for set indexing
    UINT32 prediction;          // Prediction: 4 bits (limited by MAX_VALUE_PREDICTION)
    UINT32 confidence;          // Confidence: 2 bits (limited by MAX_CONFIDENCE)
    UINT32 StackPosition;       // StackPosition: 4 bits (log2(IBRDP_WAYS))
} IBRDP_Entry;                  // Total = 27 bits

class IBRDPredictor
{
  private:
    IBRDP_Entry **predictor;    // Predictor Storage
    UINT32 numsets;             // Number of sets, constant and  == IBRDP_SETS
    UINT32 assoc;               // Associativity, constant and == IBRDP_WAYS
    UINT32 set_mask;            // mask for keeping the set indexing bits of pc
                                //    always == numsets - 1;
    UINT32 set_shift;           // # of bits that we shift pc, to get tag
                                //    always == log2(numsets)

  public:
    IBRDPredictor( UINT32 _sets, UINT32 _ways );
    // Lookup returns a reuse distance prediction for the given pc
    UINT32 Lookup( UINT32 pc );
    // Update, changes the confidence counters for the given pc,
    // and if they are zero and observation does not match the prediction
    // it changes the prediction
    void Update( UINT32 pc, UINT32 observation );

  private:
    // Helper function: 
    // FindEntry finds the entry which corresponds to the pc
    // GetEntry returns the LRU entry in the set and re-initializes it
    UINT32 FindEntry( UINT32 pc );
    UINT32 GetEntry( UINT32 pc );
};

//---------------------------------------------------------------------------///
//---------------------------------------------------------------------------///
//---                     REUSE DISTANCE SAMPLER                          ---///
//---------------------------------------------------------------------------///
//---------------------------------------------------------------------------///

typedef struct
{
    UINT32 valid;           // Valid: 1 bit
    UINT32 pc;              // PC of the sampled access: 20 bits
    UINT32 address;         // Address of the sampled access: 26 bits
    UINT32 FifoPosition;    // Position in the FIFO Queue: log2(sampler_size) bits
                            //     = 5 bits
} RDSamplerEntry;           // Total: 52 bits

class RDSampler 
{
  private:
    RDSamplerEntry *sampler;    // Sampler Storage
    IBRDPredictor *predictor;   // Reference to the IbRDPredictor

    UINT32 size;                // Sampler size == SAMPLER_MAX_RD / SAMPLER_PERIOD
    UINT32 period;              // Sampling Period == SAMPLER_PERIOD
    UINT32 sampling_counter;    // Counts from period-1 to zero.
                                // We take a new sample when it reaches zero

  public:
    RDSampler( UINT32 _period, UINT32 _max_rd, IBRDPredictor *_predictor );
    // Update is the main function of the sampler
    // 1) It performs an associative search on the sampler for the given
    //    address and if there is a hit it update the predictor
    // 2) It takes a new sample every period accesses and it enqueues it 
    //    in the sampler. If the dequeued entry was still valid, we update 
    //    the predictor using the entries pc and the MAX_VALUE_PREDICTION
    void Update( UINT32 address, UINT32 pc, UINT32 accessType );
};

 
//---------------------------------------------------------------------------///
//---------------------------------------------------------------------------///
//---                    CACHE REPLACEMENT STATE                          ---///
//---------------------------------------------------------------------------///
//---------------------------------------------------------------------------///

// Replacement Policies Supported
typedef enum 
{
    CRC_REPL_LRU        = 0,
    CRC_REPL_RANDOM     = 1,
    CRC_REPL_CONTESTANT = 2
} ReplacemntPolicy;

// Replacement State Per Cache Line
typedef struct
{
    UINT32  LRUstackposition;

    // CONTESTANTS: Add extra state per cache line here
    UINT32 timestamp;       // 3 bits Quantized timestamp
    UINT32 prediction;      // 4 bits Quantized reuse distance prediction

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
    // 1) We use the 17 bit accessesCounter instead of the 'timer' variable
    //    because we wish to count the accesses caused only by loads and stores
    // 2) We break the accessesCounter into a lower and a higher part, just 
    //    to make our lives easier: Since only the 3 higher order bits of
    //    the accessesCounter are used directly by our policy, we keep them 
    //    separated by the lower 14 bits. One could very well merge the two 
    //    parts in one variable and just write some extra code to isolate 
    //    the three higher order bits.
    UINT32 accessesCounterLow;  // Lower 14 bits of acccessesCounter
    UINT32 accessesCounterHigh; // Higher 3 bits of accessesCounter
    UINT32 set_shift;           // constant == log2(numsets)
    IBRDPredictor *predictor;   // Reference to the IbRDPredictor
    RDSampler *rdsampler;       // Reference to the RDSampler

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

    //---> Our Functions <---
    void UpdateOnEveryAccess( UINT32 address, UINT32 PC, UINT32 accessType );
    INT32 Get_IBRDP_Victim( UINT32 setIndex, Addr_t PC, Addr_t paddr, 
            UINT32 accessType );
    void UpdateIBRDP( UINT32 setIndex, INT32 updateWayID, const LINE_STATE *currLine, 
            Addr_t PC, UINT32 accessType, bool cacheHit );
    //---> Our Functions <---
};

// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------
// ---------------                HELPER FUNCTIONS               ---------------
// -----------------------------------------------------------------------------
// -----------------------------------------------------------------------------

// returns bits 21:2 of pc
// We ignore bits 0 and 1, even though the x86 architecture instructions are
// not alligned on word boundaries, because we believe that the possibility of
// two different memory instrunction starting in the same memory word is too 
// low to worth one or two extra bits.
static inline UINT32 TransformPC( Addr_t pc )
{
    Addr_t new_pc;
    new_pc = pc >> 2;
    new_pc = new_pc & ( ( 1 << BITS_PC ) - 1 );
    return (UINT32)new_pc;
}

// returns bits 25:0 of address (bits 31:6 of the real address,
// the argument address has already been stripped of the byte offset bits)
static inline UINT32 TransformAddress( Addr_t address )
{
    Addr_t new_address;
    new_address = address & ( ( 1 << BITS_ADDR ) - 1 );
    return (UINT32)new_address;
}

//
// The rest of the helper functions are self-explanatory, I think
//

static inline UINT32 QuantizeTimestamp( UINT32 timestamp )
{
    return (timestamp / QUANTUM_TIMESTAMP) & MAX_VALUE_TIMESTAMP;
}

static inline UINT32 UnQuantizeTimestamp( UINT32 timestamp )
{
    return timestamp * QUANTUM_TIMESTAMP;
}

static inline UINT32 QuantizePrediction( UINT32 prediction )
{
    prediction = prediction / QUANTUM_PREDICTION;

    if (prediction < MAX_VALUE_PREDICTION)
        return prediction;
    else
        return MAX_VALUE_PREDICTION;
}

static inline UINT32 UnQuantizePrediction( UINT32 prediction )
{
    return prediction * QUANTUM_PREDICTION;
}


#endif
