package archimulator.sim.uncore.new1;

public class MESICache implements Cache {

    //Cache configuration
    int cacheNumber;
    int cacheSize; //in words
    int blocks;
    int blockSize; //in words

    int operations = 0;
    int hits = 0;
    int hitRate = 0;
    int invalidations = 0;


    //The cache
    long[][] cache;
    int[] tags;
    boolean[] used;
    int[] status; //Status' 0 = Invalid, 1 = shared, 2 = modified, 3 = exclusive

    MESICache[] otherCaches = new MESICache[3];

    CacheGUI gui = null;
    boolean showGui;

    public MESICache(int cacheNumber, int blocks, int blockSize, CacheGUI cacheGui, boolean showGui) {

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

        boolean found = false;
        boolean hit = false;

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
        // Check if this block is in the cache
        // If we find the tag and it is valid
        // READ HIT
        if (tag == tags[block]) {
            // Then we can load it from the cache and increase the hit count
            if (verbose) {
                System.out.printf("Found tag %d in block %d!\n\n", tag, block);
            }
            // If we are in states M, E or S we stay there
            hit = true;
            hits++;
            found = true;
        }

        if (status[block] == 0) { // if cache line is in invalid state
            //Check if the line exists in any other cache
            for (int i = 0; i < otherCaches.length; i++) {
                if (otherCaches[i].getTags()[block] == tag /*&& otherCaches[i]*/) {
                    //We have found the tage in another cache, load line into cache and goto shared state
                    //Loading the corresponding block from memory to the cache
                    for (int j = 0; j < cache[block].length; j++) {
                        cache[block][j] = memoryBlock * blockSize + j;
                        tags[block] = tag;
                        used[block] = true;
                    }
                    status[block] = 1;
                    found = true;
                    break;
                }
            }

            if (!found) {
                //No other cache has this block. We can now load it into cache and go to exclusive state
                //Loading the corresponding block from memory to the cache
                for (int j = 0; j < cache[block].length; j++) {
                    cache[block][j] = memoryBlock * blockSize + j;
                    tags[block] = tag;
                    used[block] = true;
                }
                status[block] = 3;
                found = true;
            }
            //Update the gui
            if (showGui) {
                gui.updateBlock(cacheNumber, block, cache[block], tag);
                gui.setStatus(cacheNumber, block, status[block]);
            }
        }


        // READ MISS
        if (!found) {
            //Loading the corresponding block from memory to the cache
            for (int i = 0; i < cache[block].length; i++) {
                cache[block][i] = memoryBlock * blockSize + i;
                tags[block] = tag;
                used[block] = true;
            }

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
        for (int i = 0; i < otherCaches.length; i++) {
            otherCaches[i].remoteLoad(address, hit, verbose);
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
        //If we find the tag and it's not invalid
        //WRITE HIT
        if (tag == tags[block]) {
            // Then we can load it from the cache and increase the hit count
            if (verbose) {
                System.out.println("Write hit!!");
                System.out.printf("Found tag %d in block %d!\n", tag, block);
            }
            hits++;
            found = true;
            hit = true;
            //If block is in Shared state, changed to modified
            //Dont really care if exlcusive as it is write through and
            //since it is exclusive no other cache will be affected by new value.
            if (status[block] == 1) {
                status[block] = 2;
                if (showGui) {
                    gui.setStatus(cacheNumber, block, status[block]);
                }
                if (verbose) {
                    System.out.printf("Setting status of cache %d block %d to modified\n", cacheNumber, block);
                }
            }
        }

        //If the block is invalid state, load it from memory and change to modified state
        if (status[block] == 0) {
            //Loading the corresponding block from memory to the cache
            for (int i = 0; i < cache[block].length; i++) {
                cache[block][i] = memoryBlock * blockSize + i;
                tags[block] = tag;
                used[block] = true;
            }
            //Update the gui
            if (showGui) {
                gui.updateBlock(cacheNumber, block, cache[block], tag);
                gui.setStatus(cacheNumber, block, status[block]);
            }

            found = true;
        }

        //Not in cache
        if (!found) {
            if (verbose)
                System.out.println("Write miss!!");

            //Loading the corresponding block from memory to the cache
            for (int i = 0; i < cache[block].length; i++) {
                cache[block][i] = memoryBlock * blockSize + i;
                tags[block] = tag;
                used[block] = true;
            }

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
        for (int i = 0; i < otherCaches.length; i++) {
            otherCaches[i].remoteStore(address, hit, verbose);
        }
    }

    public void remoteLoad(long address, boolean hit, boolean verbose) {

        long memoryBlock = address / blockSize; //The block in main memory where we will find the word
        int block = (int) (memoryBlock % blocks); //The block in cache where we will put the memoryBlock
        int tag = (int) memoryBlock / blocks; //The tag of the memoryBlock

        if (tag == tags[block]) {
            if (used[block])
                status[block] = 1;
        }

        if (showGui) {
            //gui.updateBlock(cacheNumber, block, cache[block], tag);
            gui.setStatus(cacheNumber, block, status[block]);
            gui.updateStats(cacheNumber, operations, hits, invalidations);
        }
    }

    public void remoteStore(long address, boolean hit, boolean verbose) {

        long memoryBlock = address / blockSize; //The block in main memory where we will find the word
        int block = (int) (memoryBlock % blocks); //The block in cache where we will put the memoryBlock
        int tag = (int) memoryBlock / blocks; //The tag of the memoryBlock

        if (tag == tags[block] && !hit) {
            if (used[block]) {
                status[block] = 0;
                invalidations++;
            }
        }


        if (showGui) {
            //gui.updateBlock(cacheNumber, block, cache[block], tag);
            gui.setStatus(cacheNumber, block, status[block]);
            gui.updateStats(cacheNumber, operations, hits, invalidations);
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

    //Keep a reference to all other caches
    public void setOtherCaches(Cache[] caches) {
        int filled = 0;
        for (int i = 0; i < caches.length; i++) {
            if (i != cacheNumber) {
                otherCaches[filled++] = (MESICache) caches[i];
            }
        }
    }

    public int[] getTags() {
        return tags;
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

    //Some simple testing
    public static void main(String[] args) {
        int bl = 16;
        int s = 16;
//        CacheGUI gui = new CacheGUI(bl, s, 250, 0);
        CacheGUI gui = new CacheGUI(bl, s, 250, 1);

//        MSICache cache = new MSICache(1, bl, s, gui, true);
        MESICache cache = new MESICache(1, bl, s, gui, true);

        cache.printCaches();
    }

}
