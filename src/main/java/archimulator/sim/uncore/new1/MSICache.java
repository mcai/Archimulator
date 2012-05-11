package archimulator.sim.uncore.new1;

public class MSICache implements Cache {

    //Cache configuration
    int cacheNumber;
    int cacheSize; //in words
    int blocks;
    int blockSize; //in words

    int operations = 0;
    int hits = 0;
    int invalidations = 0;


    //The cache
    long[][] cache;
    int[] tags;
    boolean[] used;
    int[] status; //Status' 0 = Invalid, 1 = shared, 2 modified

    MSICache[] otherCaches = new MSICache[3];

    CacheGUI gui = null;
    boolean showGui;

    public MSICache(int cacheNumber, int blocks, int blockSize, CacheGUI cacheGui, boolean showGui) {

        this.cacheNumber = cacheNumber;
        this.cacheSize = blocks * blockSize;
        this.blocks = blocks;
        this.blockSize = blockSize;
        if (showGui) this.gui = cacheGui;
        this.showGui = showGui;

        cache = new long[blocks][blockSize];
        tags = new int[blocks];
        used = new boolean[blocks];
        status = new int[blocks];

        for (int i = 0; i < used.length; i++) {
            used[i] = false;
            status[i] = 0;

            if (showGui) gui.setStatus(cacheNumber, i, status[i]);
        }

    }

    public void load(long address, boolean verbose) {

        boolean hit = false;
        boolean found = false;

        operations++;
        long memoryBlock = address / blockSize; //The block in main memory where we will find the word
        int block = (int) (memoryBlock % blocks); //The block in cache where we will put the memoryBlock
        int tag = (int) memoryBlock / blocks; //The tag of the memoryBlock

        if (verbose) {
            System.out.println("Operation: " + operations);
            System.out.printf("Processor %d load of address %d\n", cacheNumber, address);
            System.out.printf("Looking for word %d with tag %d in block %d\n", address, tag, block);
        }

        if (!used[block]) { //If this line is empty
            //Loading the corresponding block from memory to the cache
            for (int i = 0; i < cache[block].length; i++) {
                cache[block][i] = memoryBlock * blockSize + i;
                tags[block] = tag;
                used[block] = true;
            }

            //set status of this cache block to be shared (can only have read miss in invalid state)
            status[block] = 1;

            //Update the gui
            if (showGui) {
                gui.updateBlock(cacheNumber, block, cache[block], tag);
                gui.setStatus(cacheNumber, block, status[block]);
            }
            if (verbose) {
                System.out.println("READ MISS!");
                System.out.printf("Loading block %d into cache\n\n", block);
            }
            found = true;
        }
        //Check if this block is in the cache
        // If we find the tag
        // READ HIT
        if (tag == tags[block] && !found) {
            if (!(status[block] == 0)) { //check MSI state is not invalid
                // Then we can load it from the cache and increase the hit count
                if (verbose) {
                    System.out.printf("Found tag %d in block %d!\n\n", tag, block);
                }
                // Do not need to change state on read hit
                hits++;
                hit = true;
            } else {
                //Load in updated values for cache line (ie not a hit), change state to shared
                status[block] = 1;
                if (showGui) {
                    gui.updateBlock(cacheNumber, block, cache[block], tag);
                    gui.setStatus(cacheNumber, block, status[block]);
                }
                if (verbose) {
                    System.out.println("Found invalid cache line");
                    System.out.printf("Loading updated block %d into cache\n\n", block);
                }
            }
            found = true;

        }

        // READ MISS
        if (!found) {
            //Loading the corresponding block from memory to the cache
            for (int i = 0; i < cache[block].length; i++) {
                cache[block][i] = memoryBlock * blockSize + i;
                tags[block] = tag;
                used[block] = true;
            }

            //set status of this cache block to be shared (can only have read miss in invalid state)
            status[block] = 1;

            //Update the gui
            if (showGui) {
                gui.updateBlock(cacheNumber, block, cache[block], tag);
                gui.setStatus(cacheNumber, block, status[block]);
            }
            if (verbose) {
                System.out.println("READ MISS!");
                System.out.printf("Loading block %d into cache\n\n", block);
            }
        }

        //Update statsCounter in gui
        if (showGui) {
            gui.updateStats(cacheNumber, operations, hits, invalidations);
        }

        //Notify other caches of this operation
        for (MSICache otherCache : otherCaches) {
            otherCache.remoteLoad(address, hit, verbose);
        }

    }

    public void store(long address, boolean verbose) {

        boolean hit = false;
        boolean found = false;

        operations++;
        long memoryBlock = address / blockSize; //The block in main memory where we will find the word
        int block = (int) (memoryBlock % blocks); //The block in cache where we will put the memoryBlock
        int tag = (int) memoryBlock / blocks; //The tag of the memoryBlock

        if (verbose) {
            System.out.println("Operation: " + operations);
            System.out.printf("Processor %d store of address %d\n", cacheNumber, address);
            System.out.printf("Looking for word %d with tag %d in block %d\n", address, tag, block);
        }

        if (!used[block]) { //If this line is empty
            //Loading the corresponding block from memory to the cache
            for (int i = 0; i < cache[block].length; i++) {
                cache[block][i] = memoryBlock * blockSize + i;
                tags[block] = tag;
                used[block] = true;
            }

            //set status of this cache block to be modified
            status[block] = 2;

            //Update the gui
            if (showGui) {
                gui.updateBlock(cacheNumber, block, cache[block], tag);
                gui.setStatus(cacheNumber, block, status[block]);
            }
            if (verbose) {
                System.out.println("READ MISS!");
                System.out.printf("Loading block %d into cache\n\n", block);
            }
            found = true;
        }
        //Check if this block is in the cache
        //If we find the tag
        //WRITE HIT
        if (tag == tags[block]) {
            //If in modified state
            if (status[block] != 2) {
                // Then we can load it from the cache and increase the hit count
                if (verbose) {
                    System.out.println("Write hit!!");
                    System.out.printf("Found tag %d in block %d!\n", tag, block);
                }
                hits++;
                hit = true;
            }
            //If block is in Shared state, changed to modified
            if (status[block] == 1) {
                status[block] = 2;
                if (showGui) {
                    gui.setStatus(cacheNumber, block, status[block]);
                }
                if (verbose) {
                    System.out.printf("Setting status of cache %d block %d to modified\n", cacheNumber, block);
                }
            }
            found = true;
        }

        //WRITE MISS
        if (!found) {
            if (verbose)
                System.out.println("Write miss!!");

            //Loading the corresponding block from memory to the cache
            for (int i = 0; i < cache[block].length; i++) {
                cache[block][i] = memoryBlock * blockSize + i;
                tags[block] = tag;
                used[block] = true;
            }

            //set status of this cache block to shared
            status[block] = 2;

            //Update the gui
            if (showGui) {
                gui.updateBlock(cacheNumber, block, cache[block], tag);
                gui.setStatus(cacheNumber, block, status[block]);
            }
        }

        //Update statsCounter in gui
        if (showGui) {
            gui.updateStats(cacheNumber, operations, hits, invalidations);
        }

        //Notify other caches of this operation
        for (MSICache otherCache : otherCaches) {
            otherCache.remoteStore(address, hit, verbose);
        }
    }

    public void remoteLoad(long address, boolean hit, boolean verbose) {

        long memoryBlock = address / blockSize; //The block in main memory where we will find the word
        int block = (int) (memoryBlock % blocks); //The block in cache where we will put the memoryBlock
        int tag = (int) memoryBlock / blocks; //The tag of the memoryBlock

        //Do we have the remote address in this cache?
        if ((tag == tags[block]) && used[block]) {

            //was it a read hit? dont care
            //Was it a read miss?
            if (!hit) {
                //If it is a remote read miss and we are in modified state, go to shared state
                if (status[block] == 2) {
                    status[block] = 1;
                    if (showGui) {
                        gui.setStatus(cacheNumber, block, status[block]);
                        gui.updateStats(cacheNumber, operations, hits, invalidations);
                    }
                }
                //If it is a remote read miss and we are in shared state, stay in shared state
            }

        }


    }

    public void remoteStore(long address, boolean hit, boolean verbose) {

        long memoryBlock = address / blockSize; //The block in main memory where we will find the word
        int block = (int) (memoryBlock % blocks); //The block in cache where we will put the memoryBlock
        int tag = (int) memoryBlock / blocks; //The tag of the memoryBlock

        //Do we have the remote address in this cache?
        if ((tag == tags[block]) && used[block]) {

            //Was it a remote write miss?
            if (!hit) {
                //If it was a remote write miss and we are in modified state change to Invalid
                if (status[block] == 2) {
                    status[block] = 0;
                    invalidations++;
                    if (verbose)
                        System.out.printf("\nCACHE %d INVALIDATION!!!\n\n", cacheNumber);
                    if (showGui) {
                        gui.setStatus(cacheNumber, block, status[block]);
                        gui.updateStats(cacheNumber, operations, hits, invalidations);
                    }
                }
                //If it was a remote write miss and we are in shared state change to Invalid
                if (status[block] == 1) {
                    status[block] = 0;
                    invalidations++;
                    if (verbose)
                        System.out.printf("\nCACHE %d INVALIDATION!!!\n\n", cacheNumber);
                    if (showGui) {
                        gui.setStatus(cacheNumber, block, status[block]);
                        gui.updateStats(cacheNumber, operations, hits, invalidations);
                    }
                }
            }
        }
    }

    public void printCaches() {
        for (int block = 0; block < cache.length; block++) {
            System.out.printf("Block %3d:\n", block);
            for (int word = 0; word < cache[block].length; word++) {
                System.out.print(cache[block][word] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void setOtherCaches(Cache[] caches) {
        int filled = 0;
        for (int i = 0; i < caches.length; i++) {
            if (i != cacheNumber) {
                otherCaches[filled++] = (MSICache) caches[i];
            }
        }
    }

    public int getOperations() {
        return operations;
    }

    public int getHits() {
        return hits;
    }

    public float getHitRate() {
        return (float) hits / operations;
    }

    public int getInvalidations() {
        return invalidations;
    }

    public static void main(String[] args) {
        int bl = 16;
        int s = 16;
        CacheGUI gui = new CacheGUI(bl, s, 250, 0);

        MSICache cache = new MSICache(1, bl, s, gui, true);

        cache.printCaches();
    }

}