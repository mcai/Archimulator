package archimulator.sim.uncore.new1;

import java.io.*;

public class Simulator {
    //Program usage
    static String usage = "java Simulator [options] -b blocks -s block_size file\n" +
            "Options:\n" +
            "\t -p\t Coherence Protocol: 0 = MSI (Default), 1 = MESI\n" +
            "\t -d\t Delay, in milliseconds, between each tracefile instruction\n" +
            "\t -g\t Show gui, defaults to false if not present";

    //Cache configuration
    static int cacheSize = 0; //in words
    static int blocks = 0;
    static int blockSize = 0; //in words
    static int coherence_protocol = 0; // 0 = MSI, 1 = MESI

    //Output control
    static boolean verbose = false;
    static boolean ignoreVerbose = false;
    static long delay = 500;

    static Cache[] caches = new Cache[4];
    static CacheGUI gui = null;
    static boolean showGui = false;

    static int linesParsed = 0;

    //Tracefile
    static File traceFile;
    static FileReader traceFileInput;
    static BufferedReader inputBuffer;

    public static void main(String[] args) throws IOException {

        initialize(args);

        //Processing each line of the trace file
        while (inputBuffer.ready()) {
            processLine(inputBuffer.readLine());
            linesParsed++;
        }

        inputBuffer.close();
        traceFileInput.close();

    }

    @SuppressWarnings("static-access")
    public Simulator(int blocks, int blockSize, int protocol, File traceFile) {

        showGui = true;
        verbose = false;
        ignoreVerbose = true;

        this.blocks = blocks;
        this.blockSize = blockSize;
        this.cacheSize = blocks * blockSize;
        this.coherence_protocol = protocol;
        this.traceFile = traceFile;

    }

    public float runTest(int testType) {
        if (showGui) gui = new CacheGUI(blocks, blockSize, delay, coherence_protocol);

        float result = 0;

        try {
            //Setting up input trace file
            traceFileInput = new FileReader(traceFile);
            inputBuffer = new BufferedReader(traceFileInput);

            //Create cache objects
            for (int i = 0; i < caches.length; i++) {
                if (coherence_protocol == 0) {
                    caches[i] = new MSICache(i, blocks, blockSize, gui, showGui);
                } else {
                    caches[i] = new MESICache(i, blocks, blockSize, gui, showGui);
                }
            }
            //Give each cache a reference to the other caches
            for (int i = 0; i < caches.length; i++) {
                caches[i].setOtherCaches(caches);
            }

            //Processing each line of the trace file
            while (inputBuffer.ready()) {
                processLine(inputBuffer.readLine());
            }

            if (testType == 0) { //Get average hitrate of all caches
                int operations = 0;
                int hits = 0;
                for (int i = 0; i < caches.length; i++) {
                    operations += caches[i].getOperations();
                    hits += caches[i].getHits();
                }
                result = ((float) hits) / operations;

            } else { //Get total invalidations
                for (int i = 0; i < caches.length; i++) {
                    result += caches[i].getInvalidations();
                }
            }

            inputBuffer.close();
            traceFileInput.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void processLine(String line) {

        int processor = 0;

        if (line.charAt(0) == 'v' && !ignoreVerbose) { // Toggle verbose
            toggleVerbose();
            System.out.printf("Switching line-by-line explaination %s\n", verbose ? "ON" : "OFF");
        } else if (line.charAt(0) == 'p' && !ignoreVerbose) { // Print contents
            System.out.println("\nPrinting cache contents:");
            for (int i = 0; i < caches.length; i++) {
                System.out.println("Cache " + i);
                caches[i].printCaches();
            }
        } else if (line.charAt(0) == 'h' && !ignoreVerbose) { // Print hit rate

            for (int i = 0; i < caches.length; i++) {
                System.out.printf("Hit ratio of cache %d: %.4f\n", i, caches[i].getHitRate());
            }
        } else if (line.charAt(0) == 'P') { // Memory operation

            try { //Get processor number
                processor = Integer.parseInt(Character.toString(line.charAt(1)));
            } catch (NumberFormatException nFE) {
                System.out.println("\nSimulator ERROR - No address found after laod instruction");
                System.out.printf("In line %d of tracefile:\n%s\n", linesParsed, line);
            }

            try {
                long address = Long.parseLong(line.substring(5, line.length()));
                String operation = Character.toString(line.charAt(3));

                if (operation.equals("l")) {
                    caches[processor].load(address, verbose);
                } else {
                    caches[processor].store(address, verbose);
                }
            } catch (NumberFormatException nFE) {
                System.out.println("\nSimulator ERROR - No address found after laod instruction");
                System.out.printf("In line %d of tracefile:\n%s\n", linesParsed, line);
            }
        } else if (!Character.isLetter(line.charAt(0)) && (line.toLowerCase().charAt(0) != '#')) {
            System.out.printf("\nSimulator ERROR - Unexpected chatacter '%s'\nin line %d of trace file:\n", line.toLowerCase().charAt(0), linesParsed);
            System.out.println("\t" + line);
            return;
        } else if (line.toLowerCase().charAt(0) == '#') {
            return;
        }


        try {
            if (showGui) {
                while (isPaused()) {
                    Thread.sleep(100);
                }
                Thread.sleep(gui.getDelay());
            } else if (verbose) {
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void initialize(String[] args) {
        //Check for the minimum number of arguments from user
        if (args.length < 3) {
            System.out.println("Error - Invalid number of arguments");
            System.out.println("\nUsage: " + usage);
            System.exit(1);
        }

        //Print program usage if user requests help
        if (args[0].equals("--help") || args[0].equals("-h")) {
            System.out.println("Usage: " + usage);
            System.exit(0);
        }

        try {
            //Setting up input trace file
            traceFile = new File(args[args.length - 1]);
            traceFileInput = new FileReader(traceFile);
            inputBuffer = new BufferedReader(traceFileInput);

            //Setting up control variables
            System.out.println("Initialising cache:");
            try {
                for (int i = 0; i < args.length - 1; i++) {
                    if (args[i].equals("-d")) {
                        delay = Long.parseLong(args[i + 1]);
                    } else if (args[i].equals("-g")) {
                        showGui = true;
                        System.out.println("YES");
                    } else if (args[i].equals("-b")) {
                        blocks = Integer.parseInt(args[i + 1]);
                    } else if (args[i].equals("-s")) {
                        blockSize = Integer.parseInt(args[i + 1]);
                    } else if (args[i].equals("-p")) {
                        coherence_protocol = Integer.parseInt(args[i + 1]);
                    }
                }
            } catch (NumberFormatException nFE) {
                System.out.println("Error, invalid parameters\n\nUsage:" + usage);
            }

            //Setting up cache configuration

            //Validating input variables
            if (blocks == 0 || blockSize == 0) {
                System.out.println("Error creating cache");
                System.out.println("\nUsage: " + usage);
                System.exit(1);
            }

            cacheSize = blocks * blockSize;
            if (showGui) gui = new CacheGUI(blocks, blockSize, delay, coherence_protocol);

            //Displaying cache configuration
            System.out.printf("Cache Size: \t%d bytes\nBlocks: \t%d\nBlock Size: \t%d bytes\n\n", cacheSize, blocks, blockSize);

            for (int i = 0; i < caches.length; i++) {
                if (coherence_protocol == 0) {
                    caches[i] = new MSICache(i, blocks, blockSize, gui, showGui);
                } else {
                    caches[i] = new MESICache(i, blocks, blockSize, gui, showGui);
                }
            }
            //Give each cache a reference to the other caches
            for (int i = 0; i < caches.length; i++) {
                caches[i].setOtherCaches(caches);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Could not open file: " + args[args.length - 1]);
            System.out.println("Usage: " + usage);
        }
    }

    public static void toggleVerbose() {
        if (verbose) {
            verbose = false;
        } else {
            verbose = true;
        }
    }

    public static boolean isPaused() {
        return gui.getPaused();
    }
}
