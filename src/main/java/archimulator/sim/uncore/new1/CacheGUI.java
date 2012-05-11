package archimulator.sim.uncore.new1;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

@SuppressWarnings("serial")
public class CacheGUI extends JFrame {

    int blocks;
    int blockSize;
    int coherence_protocol;

    //Important GUI components
    JLabel[][][] cacheContents; //[cacheNUmber][block][word]
    JLabel[][] tags; //[cacheNumber][block]
    JLabel[] characteristics; // 0 = blocks, 1 = blockSize
    JLabel[][] stats = new JLabel[4][5]; //0 = Operations counter, 1 = Hits, 2 = Misses, 3 = Hit rate, 4 = invalidations
    JSlider delaySlider;
    JCheckBox pause;

    Color tmpColour;
    Color invalidColor = new Color(243, 246, 255);    // Light red
    Color sharedColor = new Color(194, 255, 77);    // Light green
    Color modifiedColor = new Color(255, 217, 28);    // Light yellow
    Color exclusiveColor = new Color(57, 108, 255);// Light blue

    //Misc
    String seperator = "-";
    int indexColourReset = 0;
    int wayColourReset = 0;
    int wordColourReset = 0;

    //Setting up the JFrame and creating/adding all the components
    public CacheGUI(int blocks, int blockSize, long delay, int protocol) {
        //Frame title
        super("Direct mapped cache");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Frame size characteristics
        int frameWidth = 1100;
        int frameHeight = 800;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((int) screenSize.getWidth() - frameWidth, 0, frameWidth, frameHeight);

        //Window icon
        setIconImage(Toolkit.getDefaultToolkit().getImage("../icons/inf.png"));

        //Initialising instance variables
        this.blocks = blocks;
        this.blockSize = blockSize;
        this.coherence_protocol = protocol;
        this.cacheContents = new JLabel[4][blocks][blockSize];
        this.tags = new JLabel[4][blocks];

        //Top level container to store everything
        Container topLevelContainer = new Container();
        topLevelContainer.setLayout(new BoxLayout(topLevelContainer, BoxLayout.Y_AXIS));

        //Container to store information about the characteristics of the cache
        Container characteristicsContainer = createCharacteristics();

        //Adding all the cache table containers together
        Container[] cacheGridContainer = new Container[4];
        JScrollPane[] scrollPane = new JScrollPane[4];

        for (int i = 0; i < cacheGridContainer.length; i++) {
            //All of the containers which make up the items of the cache
            Container indexContainer = createBlockLabels(); //Store the labels for the indexes
            Container cacheContainer = createCacheTable(i); //Create a container to store the table of the contents of the cache
            Container tagsContainer = createTagLabels(i); //Create a container to store the tags

            cacheGridContainer[i] = new Container();
            cacheGridContainer[i].setLayout(new BoxLayout(cacheGridContainer[i], BoxLayout.X_AXIS));
            cacheGridContainer[i].add(indexContainer);
            cacheGridContainer[i].add(cacheContainer);
            cacheGridContainer[i].add(tagsContainer);

            //Adding the cache table to a scrollable Pane
            scrollPane[i] = new JScrollPane(cacheGridContainer[i]);
            scrollPane[i].setBorder(new TitledBorder("Cache table " + i));
        }

        //Container to hold all Scrollpanes containing each cache
        Container scrollPaneContainer = new Container();
        scrollPaneContainer.setLayout(new GridLayout(2, 2));
        for (JScrollPane aScrollPane : scrollPane) {
            scrollPaneContainer.add(aScrollPane);
        }

        //Creating a Slider to control the time delay between operations
        Container timingControls = createTimingControls(delay);

        //Create a container for cache stats
        Container statsContainer = new Container();
        statsContainer.setLayout(new GridLayout(2, 2));
        statsContainer.setMaximumSize(new Dimension(getWidth(), 50));

        Container[] statsContainers = new Container[4];//containers for seperate cache stats containers
        for (int i = 0; i < statsContainers.length; i++) {
            statsContainers[i] = createStatsContainer(i);
            statsContainer.add(statsContainers[i]);
        }


        //Packing everything into a top level container
        topLevelContainer.add(characteristicsContainer);
        topLevelContainer.add(scrollPaneContainer);
        topLevelContainer.add(timingControls);
        topLevelContainer.add(statsContainer);
        add(topLevelContainer);

        //show frame
        setVisible(true);
    }


    //Create a container to hold the cahracteristics of the cache
    private Container createCharacteristics() {

        Container container = new Container();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));

        if (coherence_protocol == 1) {
            characteristics = new JLabel[8];
        } else {
            characteristics = new JLabel[7];
        }

        characteristics[0] = new JLabel("Number of blocks: " + blocks + " | ");
        characteristics[1] = new JLabel("Block size: " + blockSize + " | ");
        characteristics[2] = new JLabel(String.format("Coherence Protocol: %s", (coherence_protocol == 0) ? "MSI" : "MESI"));
        characteristics[3] = new JLabel("       ");
        characteristics[4] = new JLabel("  Shared  ");
        characteristics[4].setOpaque(true);
        characteristics[4].setBackground(sharedColor);
        characteristics[5] = new JLabel("  Modified  ");
        characteristics[5].setBackground(modifiedColor);
        characteristics[5].setOpaque(true);
        characteristics[6] = new JLabel("  Invalid  ");
        characteristics[6].setBackground(invalidColor);
        characteristics[6].setOpaque(true);
        if (coherence_protocol == 1) {
            characteristics[7] = new JLabel("  Exclusive  ");
            characteristics[7].setBackground(exclusiveColor);
            characteristics[7].setOpaque(true);
        }

        JPanel jPanel = new JPanel();
        jPanel.setBorder(new TitledBorder("Cache characteristics"));
        jPanel.add(characteristics[0]);
        jPanel.add(characteristics[1]);
        jPanel.add(characteristics[2]);
        jPanel.add(characteristics[3]);
        jPanel.add(characteristics[4]);
        jPanel.add(characteristics[5]);
        jPanel.add(characteristics[6]);
        if (coherence_protocol == 1)
            jPanel.add(characteristics[7]);

        container.setMaximumSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width, 100));
        container.add(jPanel);

        return container;
    }

    //Create a container to hold the labels for the cache indexes
    private Container createBlockLabels() {

        Container mainContainer = new Container();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

        //adding the header
        JLabel blockLabel = new JLabel("Block");
        Font curFont = blockLabel.getFont();
        blockLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 8));
        mainContainer.add(blockLabel);

        //Setting up the container
        Container indexContainer = new Container();
        indexContainer.setLayout(new GridLayout(blocks, 1));

        for (int i = 0; i < blocks; i++) {
            JLabel label = new JLabel(String.valueOf(i));
            label.setHorizontalTextPosition(JLabel.CENTER);
            Font f = new Font("Monospaced", Font.BOLD, 10);
            label.setFont(f);
            indexContainer.add(label);
        }

        mainContainer.add(indexContainer);

        return mainContainer;
    }

    private Container createCacheTable(int cache) {

        Container mainContainer = new Container();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

        //adding the headers
        Container headerContainer = new Container();
        headerContainer.setLayout(new GridLayout(1, blockSize));
        headerContainer.setMaximumSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width, 50));

        for (int i = 0; i < blockSize; i++) {
            JLabel wordLabel = new JLabel("W" + i);
            Font curFont = wordLabel.getFont();
            wordLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 8));
            headerContainer.add(wordLabel);
        }

        //Adding the cache contents
        Container cacheContainer = new Container();
        cacheContainer.setLayout(new GridLayout(blocks, blockSize));

        for (int i = 0; i < blocks; i++) {
            for (int k = 0; k < blockSize; k++) {
                JLabel blah = new JLabel("0");
                Font curFont = blah.getFont();
                blah.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 8));
                cacheContents[cache][i][k] = blah;
                cacheContainer.add(cacheContents[cache][i][k]);
            }
        }

        mainContainer.add(headerContainer);
        mainContainer.add(cacheContainer);

        return mainContainer;
    }

    //Create a container to hold the labels for the cache indexes
    private Container createTagLabels(int cache) {

        Container mainContainer = new Container();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

        //adding the header
        JLabel tagLabel = new JLabel("TAG");
        Font curFont = tagLabel.getFont();
        tagLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 8));
        tagLabel.setHorizontalTextPosition(JLabel.LEFT);
        mainContainer.add(tagLabel);

        //Setting up the container
        Container tagsContainer = new Container();
        tagsContainer.setLayout(new GridLayout(blocks, 1));

        for (int i = 0; i < blocks; i++) {
            JLabel tagsLabel = new JLabel("Empty");
            tagsLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 8));
            tags[cache][i] = tagsLabel;
            tags[cache][i].setHorizontalTextPosition(SwingConstants.RIGHT);
            tagsContainer.add(tags[cache][i]);
        }

        mainContainer.add(tagsContainer);

        return mainContainer;
    }

    private Container createTimingControls(long delay) {

        Container container = new Container();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));

        delaySlider = new JSlider(JSlider.HORIZONTAL, 0, 3000, (int) delay);
        delaySlider.setMajorTickSpacing(250);
        delaySlider.setMinorTickSpacing(50);
        delaySlider.setPaintTicks(true);
        delaySlider.setPaintLabels(true);
        delaySlider.setOpaque(false);

        pause = new JCheckBox("Pause", false);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.setBorder(new TitledBorder("Timing controls (milliseconds)"));

        jPanel.add(delaySlider);
        jPanel.add(pause);

        container.add(jPanel);

        return container;
    }

    private Container createStatsContainer(int cache) {

        //0 = Operations counter, 1 = Hits, 2 = Misses, 3 = Hit rate
        Container container = new Container();
        container.setLayout(new GridLayout(1, 8));
        container.setMaximumSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width, 50));

        stats[cache] = new JLabel[5];

        JPanel statsPanel = new JPanel();
        statsPanel.setBorder(new TitledBorder(String.format("Cache %d stats", cache)));

        Font f = new Font("Monospaced", Font.PLAIN, 9);
        Font f2 = new Font("Monospaced", Font.BOLD, 9);

        JLabel operationsLabel = new JLabel("Total operations: ");
        operationsLabel.setFont(f2);
        statsPanel.add(operationsLabel);
        stats[cache][0] = new JLabel("0");
        stats[cache][0].setFont(f);
        statsPanel.add(stats[cache][0]);

        JLabel hitsLabel = new JLabel(" | Hits: ");
        hitsLabel.setFont(f2);
        statsPanel.add(hitsLabel);
        stats[cache][1] = new JLabel("0");
        stats[cache][1].setFont(f);
        statsPanel.add(stats[cache][1]);

        JLabel missesLabel = new JLabel(" | Misses: ");
        missesLabel.setFont(f2);
        statsPanel.add(missesLabel);
        stats[cache][2] = new JLabel("0");
        stats[cache][2].setFont(f);
        statsPanel.add(stats[cache][2]);

        JLabel hitrateLabel = new JLabel(" | Hit Rate: ");
        hitrateLabel.setFont(f2);
        statsPanel.add(hitrateLabel);
        stats[cache][3] = new JLabel("0");
        stats[cache][3].setFont(f);
        statsPanel.add(stats[cache][3]);

        JLabel invalidationsLabel = new JLabel(" | Invalidations: ");
        invalidationsLabel.setFont(f2);
        statsPanel.add(invalidationsLabel);
        stats[cache][4] = new JLabel("0");
        stats[cache][4].setFont(f);
        statsPanel.add(stats[cache][4]);

        container.add(statsPanel);

        return container;
    }

    public void startAccessWord(int cacheToUpdate, int blockToUpdate, int wordToUpdate) {

        tmpColour = cacheContents[cacheToUpdate][blockToUpdate][wordToUpdate].getBackground();
        //Making the accessed word BLUE
        cacheContents[cacheToUpdate][blockToUpdate][wordToUpdate].setOpaque(true);
        cacheContents[cacheToUpdate][blockToUpdate][wordToUpdate].setBackground(Color.BLUE);
    }

    public void endAccessWord(int cacheToUpdate, int blockToUpdate, int wordToUpdate) {

        //Making the accessed word tmpColour
        cacheContents[cacheToUpdate][blockToUpdate][wordToUpdate].setOpaque(true);
        cacheContents[cacheToUpdate][blockToUpdate][wordToUpdate].setBackground(tmpColour);
    }

    public void setStatus(int cacheToUpdate, int blockToUpdate, int status) {
        //Status' 0 = Invalid, 1 = shared, 2 modified#
        if (status == 0) {
            //Making the accessed block Invalid
            for (int i = 0; i < cacheContents[cacheToUpdate][blockToUpdate].length; i++) {
                cacheContents[cacheToUpdate][blockToUpdate][i].setOpaque(true);
                cacheContents[cacheToUpdate][blockToUpdate][i].setBackground(invalidColor);
            }
        } else if (status == 1) {
            //Making the accessed block Shared
            for (int i = 0; i < cacheContents[cacheToUpdate][blockToUpdate].length; i++) {
                cacheContents[cacheToUpdate][blockToUpdate][i].setOpaque(true);
                cacheContents[cacheToUpdate][blockToUpdate][i].setBackground(sharedColor);
            }
        } else if (status == 2) {
            //Making the accessed block modified
            for (int i = 0; i < cacheContents[cacheToUpdate][blockToUpdate].length; i++) {
                cacheContents[cacheToUpdate][blockToUpdate][i].setOpaque(true);
                cacheContents[cacheToUpdate][blockToUpdate][i].setBackground(modifiedColor);
            }
        } else if (status == 3) {
            //Making the accessed block exclusive
            for (int i = 0; i < cacheContents[cacheToUpdate][blockToUpdate].length; i++) {
                cacheContents[cacheToUpdate][blockToUpdate][i].setOpaque(true);
                cacheContents[cacheToUpdate][blockToUpdate][i].setBackground(exclusiveColor);
            }
        } else {
            System.out.println("Error setting cache status");
        }
    }

    public void updateStats(int cache, int operations, int hits, int invalidations) {
        //0 = Operations counter, 1 = Hits, 2 = Misses, 3 = Hit rate, 4 = Invalidations

        stats[cache][0].setText(String.valueOf(operations));
        stats[cache][1].setText(String.valueOf(hits));
        stats[cache][2].setText(String.valueOf(operations - hits));
        stats[cache][3].setText(String.format("%.4f", (float) hits / operations));
        stats[cache][4].setText(String.valueOf(invalidations));

    }

    public void updateBlock(int process, int blockToUpdate, long[] newBlock, int tag) {
        for (int i = 0; i < cacheContents[process][blockToUpdate].length; i++) {
            cacheContents[process][blockToUpdate][i].setText(String.valueOf(newBlock[i]));
        }
        tags[process][blockToUpdate].setText(String.valueOf(tag));

    }

    public long getDelay() {
        return delaySlider.getValue();
    }

    public boolean getPaused() {
        return pause.isSelected();
    }
}