/*----------------------------------------------------------------------
  File    : ACODemo.java
  Contents: ant colony optimization demonstration program
  Author  : Christian Borgelt
  History : 2005.11.18 file created
            2005.12.03 trail and inverse distance exponents exchanged
            2014.10.23 changed from LGPL license to MIT license
----------------------------------------------------------------------*/
package archimulator.util.ai.aco.v2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

/*--------------------------------------------------------------------*/
class LogoPanel extends JPanel {
/*--------------------------------------------------------------------*/

    private static final long serialVersionUID = 0x00010000L;

    private byte[] logodata = {   /* data for logo (GIF format) */
            71, 73, 70, 56, 57, 97, 100, 0, 101, 0, -25, 0,
            0, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3,
            3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7,
            7, 8, 8, 8, 9, 9, 9, 10, 10, 10, 11, 11,
            11, 12, 12, 12, 13, 13, 13, 14, 14, 14, 15, 15,
            15, 16, 16, 16, 17, 17, 17, 18, 18, 18, 19, 19,
            19, 20, 20, 20, 21, 21, 21, 22, 22, 22, 23, 23,
            23, 24, 24, 24, 25, 25, 25, 26, 26, 26, 27, 27,
            27, 28, 28, 28, 29, 29, 29, 30, 30, 30, 31, 31,
            31, 32, 32, 32, 33, 33, 33, 34, 34, 34, 35, 35,
            35, 36, 36, 36, 37, 37, 37, 38, 38, 38, 39, 39,
            39, 40, 40, 40, 41, 41, 41, 42, 42, 42, 43, 43,
            43, 44, 44, 44, 45, 45, 45, 46, 46, 46, 47, 47,
            47, 48, 48, 48, 49, 49, 49, 50, 50, 50, 51, 51,
            51, 52, 52, 52, 53, 53, 53, 54, 54, 54, 55, 55,
            55, 56, 56, 56, 57, 57, 57, 58, 58, 58, 59, 59,
            59, 60, 60, 60, 61, 61, 61, 62, 62, 62, 63, 63,
            63, 64, 64, 64, 65, 65, 65, 66, 66, 66, 67, 67,
            67, 68, 68, 68, 69, 69, 69, 70, 70, 70, 71, 71,
            71, 72, 72, 72, 73, 73, 73, 74, 74, 74, 75, 75,
            75, 76, 76, 76, 77, 77, 77, 78, 78, 78, 79, 79,
            79, 80, 80, 80, 81, 81, 81, 82, 82, 82, 83, 83,
            83, 84, 84, 84, 85, 85, 85, 86, 86, 86, 87, 87,
            87, 88, 88, 88, 89, 89, 89, 90, 90, 90, 91, 91,
            91, 92, 92, 92, 93, 93, 93, 94, 94, 94, 95, 95,
            95, 96, 96, 96, 97, 97, 97, 98, 98, 98, 99, 99,
            99, 100, 100, 100, 101, 101, 101, 102, 102, 102, 103, 103,
            103, 104, 104, 104, 105, 105, 105, 106, 106, 106, 107, 107,
            107, 108, 108, 108, 109, 109, 109, 110, 110, 110, 111, 111,
            111, 112, 112, 112, 113, 113, 113, 114, 114, 114, 115, 115,
            115, 116, 116, 116, 117, 117, 117, 118, 118, 118, 119, 119,
            119, 120, 120, 120, 121, 121, 121, 122, 122, 122, 123, 123,
            123, 124, 124, 124, 125, 125, 125, 126, 126, 126, 127, 127,
            127, -128, -128, -128, -127, -127, -127, -126, -126, -126, -125, -125,
            -125, -124, -124, -124, -123, -123, -123, -122, -122, -122, -121, -121,
            -121, -120, -120, -120, -119, -119, -119, -118, -118, -118, -117, -117,
            -117, -116, -116, -116, -115, -115, -115, -114, -114, -114, -113, -113,
            -113, -112, -112, -112, -111, -111, -111, -110, -110, -110, -109, -109,
            -109, -108, -108, -108, -107, -107, -107, -106, -106, -106, -105, -105,
            -105, -104, -104, -104, -103, -103, -103, -102, -102, -102, -101, -101,
            -101, -100, -100, -100, -99, -99, -99, -98, -98, -98, -97, -97,
            -97, -96, -96, -96, -95, -95, -95, -94, -94, -94, -93, -93,
            -93, -92, -92, -92, -91, -91, -91, -90, -90, -90, -89, -89,
            -89, -88, -88, -88, -87, -87, -87, -86, -86, -86, -85, -85,
            -85, -84, -84, -84, -83, -83, -83, -82, -82, -82, -81, -81,
            -81, -80, -80, -80, -79, -79, -79, -78, -78, -78, -77, -77,
            -77, -76, -76, -76, -75, -75, -75, -74, -74, -74, -73, -73,
            -73, -72, -72, -72, -71, -71, -71, -70, -70, -70, -69, -69,
            -69, -68, -68, -68, -67, -67, -67, -66, -66, -66, -65, -65,
            -65, -64, -64, -64, -63, -63, -63, -62, -62, -62, -61, -61,
            -61, -60, -60, -60, -59, -59, -59, -58, -58, -58, -57, -57,
            -57, -56, -56, -56, -55, -55, -55, -54, -54, -54, -53, -53,
            -53, -52, -52, -52, -51, -51, -51, -50, -50, -50, -49, -49,
            -49, -48, -48, -48, -47, -47, -47, -46, -46, -46, -45, -45,
            -45, -44, -44, -44, -43, -43, -43, -42, -42, -42, -41, -41,
            -41, -40, -40, -40, -39, -39, -39, -38, -38, -38, -37, -37,
            -37, -36, -36, -36, -35, -35, -35, -34, -34, -34, -33, -33,
            -33, -32, -32, -32, -31, -31, -31, -30, -30, -30, -29, -29,
            -29, -28, -28, -28, -27, -27, -27, -26, -26, -26, -25, -25,
            -25, -24, -24, -24, -23, -23, -23, -22, -22, -22, -21, -21,
            -21, -20, -20, -20, -19, -19, -19, -18, -18, -18, -17, -17,
            -17, -16, -16, -16, -15, -15, -15, -14, -14, -14, -13, -13,
            -13, -12, -12, -12, -11, -11, -11, -10, -10, -10, -9, -9,
            -9, -8, -8, -8, -7, -7, -7, -6, -6, -6, -5, -5,
            -5, -4, -4, -4, -3, -3, -3, -2, -2, -2, -1, -1,
            -1, 33, -7, 4, 1, 10, 0, -1, 0, 44, 0, 0,
            0, 0, 100, 0, 101, 0, 0, 8, -2, 0, -1, 9,
            28, 72, -80, -96, -63, -125, 8, 15, 2, 88, -56, -80,
            33, -61, -124, 16, 35, 74, -100, 72, -111, 98, -61, -119,
            14, 43, 106, -36, -56, 49, -31, -61, -114, 2, 51, -126,
            28, 73, 114, -32, 71, -123, 33, 77, -86, 68, -72, -80,
            -92, 75, -115, 45, 81, -90, -4, 23, 19, -64, 74, -101,
            10, 99, -66, -36, 73, 80, 39, -53, -101, 56, 113, -46,
            -100, 41, -76, -96, 79, -98, 36, -113, 30, 29, 74, 116,
            104, -48, -101, 33, -117, 70, 69, -102, -44, -96, -44, -89,
            77, 105, 98, 117, -86, 82, -22, 84, -86, 48, -123, -118,
            -43, 106, 85, -25, 88, -90, 100, -65, -86, -99, 9, 54,
            -94, -46, -98, 47, -117, -38, 28, 123, -43, 107, -37, -107,
            89, 83, 46, 5, -23, 115, 43, -36, -69, 70, -83, 106,
            61, -117, 84, -20, 73, -71, -128, -43, -102, -11, 123, 119,
            -21, -36, -65, 109, 17, 7, 78, 44, -109, 107, 79, -69,
            37, -81, 10, -90, 44, -8, 49, 94, -76, 113, 55, -125,
            -26, -116, -46, -16, 100, -105, -110, -103, 98, 38, -115, -105,
            -16, -24, -114, 117, 77, -82, 14, 123, -111, -17, -23, -39,
            24, 127, -14, 117, -56, 91, 36, 76, -93, -82, 57, -102,
            -2, 108, -79, -73, 113, -34, -74, -47, -30, -10, 8, -107,
            45, -58, -29, -48, 125, 87, -60, -68, 92, -76, 106, -31,
            -47, -77, 35, -49, -67, -71, 58, 113, -78, 123, 89, -2,
            70, 31, -84, -3, -28, 116, -84, -34, -107, 127, 119, -101,
            61, 42, 114, -24, -65, 65, 123, 63, -53, 24, -94, 118,
            -32, 25, -113, 79, -73, -68, 94, -76, 103, -38, -58, 61,
            55, 87, -128, 97, -27, -59, -36, 95, -23, -111, 87, 91,
            124, 4, -18, -57, -42, 108, 118, -3, 39, -111, 126, 27,
            -43, -44, -101, -125, -51, -23, 38, 31, -128, -46, -99, 39,
            -37, -126, 21, 6, 7, 89, 90, 33, 54, -72, 27, -120,
            -59, 25, 56, 98, 127, 30, -63, -105, -44, 118, 22, 65,
            22, -95, 115, 9, 42, -120, -30, -119, 29, -62, -26, -97,
            -120, -10, 81, -8, -30, -123, -103, 89, 55, 88, -119, 64,
            -2, 8, 99, 85, 43, -98, 104, 35, 106, 62, -114, -108,
            90, 125, -20, 61, -108, 35, 108, 77, -98, 71, 24, 122,
            -81, -119, -105, -97, 121, 70, 30, -7, -36, -125, -51, 73,
            -88, -27, -106, 38, 18, -23, 37, -122, -22, 109, -24, -42,
            -112, -18, 85, 121, 89, 75, 74, -71, -7, -91, -127, 80,
            86, 86, -34, 106, -7, -31, 87, 100, -120, 93, 1, 87,
            -36, 120, 111, 34, 88, -103, -115, 83, -74, -88, 89, -115,
            119, 14, 104, -95, 92, 120, -66, 89, -90, 112, 89, -10,
            -104, -24, 71, 92, -74, 73, -36, 125, -69, 97, -105, 40,
            -101, 17, -118, 89, 87, 123, -110, 50, 106, 102, 121, 75,
            -106, 117, -29, -121, -128, -82, 105, 33, -86, 120, -34, -23,
            -36, -85, -2, 105, 77, 73, -22, -124, -79, 122, 5, -95,
            -85, -18, -11, 55, 96, -92, -77, 26, 42, 106, -110, -90,
            -126, 90, -42, -124, -99, 98, -54, 39, -117, -114, -70, -104,
            -109, -109, -67, 22, -40, 38, 110, -97, -98, -70, -42, -88,
            -54, -114, 41, 91, 92, 103, 2, -5, -89, -79, -125, 6,
            85, 99, 78, -39, -62, -54, 33, -73, 35, 122, -21, -24,
            -74, -31, 70, 106, 37, -82, -127, -95, -25, 90, 117, -87,
            94, 38, 100, -108, -20, -6, 57, -100, -102, -95, -54, 105,
            -21, -72, -62, -46, -22, -41, 114, -28, 126, 5, 39, -75,
            -43, -86, -118, 42, -70, 123, -54, -5, 45, -95, 5, -77,
            -38, 39, -114, -113, 90, 10, 41, -100, 17, 127, 70, 23,
            -77, 5, 3, -71, -16, -77, -31, -107, 27, -90, -76, -116,
            86, 27, 112, -69, 12, 87, 42, -45, -107, -13, 10, -36,
            -98, -73, -108, -90, -53, 106, -59, -61, -74, 118, 26, -72,
            -9, -99, 41, -27, 94, 44, -113, 44, -18, -69, -12, 78,
            -86, -24, -51, 3, 119, -90, 95, -72, -69, 122, -84, 110,
            -78, -109, -106, 102, -14, -49, -17, 73, -85, 90, 77, 51,
            35, 92, -90, -108, -90, 42, 77, 33, -124, 89, 105, -90,
            105, -68, 126, -82, -101, 48, 119, -81, 125, -37, -20, -76,
            -86, 38, -35, -75, -118, -81, -102, -84, -25, -93, 29, 119,
            23, -32, -41, 78, 105, 61, 51, -113, 72, 39, -36, -74,
            -68, 37, 75, -36, 93, -2, -84, 7, -90, 70, 115, -58,
            -16, 26, 38, -89, -98, 104, 103, 120, -12, -40, 119, -109,
            88, -78, -45, 112, -43, 25, -90, -92, -117, 47, 122, 93,
            -52, 68, 113, -67, 22, -35, 8, 2, -100, -13, -123, 23,
            29, -39, -41, -26, 45, -17, 29, -75, -122, -13, 10, 62,
            52, -59, -28, -79, -39, 26, -116, 75, 5, 55, -33, -96,
            -63, -6, -36, 97, -100, -116, -29, -101, 30, 125, 61, -53,
            110, 30, -105, 44, -9, -99, -78, 125, -41, 18, 43, 120,
            -28, -66, -31, -100, -77, -99, 120, -57, 23, 54, -27, -107,
            99, -86, -74, 124, -44, -55, -84, 35, -115, -30, 89, -68,
            -14, -35, 29, -33, 60, 58, -102, -114, 39, 95, 119, -19,
            -34, -97, -116, 44, -40, -118, 3, -49, 28, -8, -80, -109,
            -8, -33, -58, 115, 7, -70, 125, -29, 47, 10, 10, -26,
            78, -110, -67, 123, -21, -47, 96, -5, -115, 63, -92, 70,
            -117, -21, 126, -112, -100, -46, 22, 0, -87, -25, -65, -58,
            -79, -17, 59, -6, 43, 12, -18, -56, -42, -75, -60, -11,
            -56, 123, 7, -116, -47, -27, -40, 19, 62, -27, -91, -81,
            49, 23, -84, -96, 3, 127, 87, 62, -50, -56, -115, 121,
            28, -84, 30, 8, 89, -93, -66, 3, -75, -56, -126, -63,
            35, -31, -80, 58, 53, 39, 98, -99, 77, -123, 34, -84,
            -32, 9, 41, -104, 66, 24, -102, -81, -122, 45, -44, -48,
            6, 109, -8, -85, 99, 45, 107, -121, 44, 54, 84, -50,
            -13, 30, 24, -84, 32, 86, 72, 49, 67, 28, 97, 4,
            85, 40, -90, -17, -59, 11, -120, 70, -60, 97, 102, 48,
            23, -59, 27, -14, 4, 117, 85, -52, 98, -82, -76, -56,
            -59, 37, 82, 36, 32, 0, 59};

    private ImageIcon logo = new ImageIcon(logodata);

  /*------------------------------------------------------------------*/

    public Dimension getPreferredSize() {
        return new Dimension(logo.getIconWidth(), logo.getIconHeight());
    }

    public void paint(Graphics g) {
        g.drawImage(logo.getImage(), 0, 0, this);
    }

}  /* class LogoPanel */


/*--------------------------------------------------------------------*/
public class ACODemo extends JFrame
        implements MouseListener, MouseMotionListener, Runnable {
/*--------------------------------------------------------------------*/

    private static final long serialVersionUID = 0x00010005L;
    public static final String VERSION = "1.5 (2014.10.23)";

    private static final Font font = new Font("Dialog", Font.BOLD, 12);
    private static final Font small = new Font("Dialog", Font.PLAIN, 10);

    private boolean isprog = false; /* whether run as a program */
    private JScrollPane scroll = null;  /* scroll pane viewport */
    private ACOPanel panel = null;  /* ACO viewer panel */
    private JTextField stat = null;  /* status bar for messages */
    private JDialog randtsp = null;  /* TSP generation dialog box */
    private JDialog antcol = null;  /* ant colony dialog box */
    private JDialog runopt = null;  /* run opt. dialog box */
    private JDialog params = null;  /* parameter dialog box */
    private JDialog about = null;  /* "About..." dialog box */
    private JFileChooser chooser = null;  /* a file chooser */
    private File curr = null;  /* current TSP file */
    private TSP tsp = null;  /* current TSP */
    private Timer timer = null;  /* timer for repeated update */
    private int cnt = -1;    /* counter for epochs */
    private int mode = 0;     /* mouse operation mode */
    private int mx, my;          /* mouse coordinates */
    private double scale, factor;   /* buffer for scaling factor */

  /*------------------------------------------------------------------*/

    private JFileChooser createChooser() {                             /* --- create a file chooser */
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileHidingEnabled(true);
        fc.setAcceptAllFileFilterUsed(true);
        fc.setMultiSelectionEnabled(false);
        fc.setFileView(null);       /* create a standard file chooser */
        return fc;                  /* without customized filters */
    }  /* createChooser() */

  /*------------------------------------------------------------------*/

    public void loadTSP(File file) {                             /* --- load a traveling salesman p. */
        if (file == null) {         /* if no file name is given */
            if (chooser == null) chooser = createChooser();
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            int r = chooser.showDialog(this, null);
            if (r != JFileChooser.APPROVE_OPTION) return;
            file = chooser.getSelectedFile();
        }                           /* get the selected file */
        try {                       /* load the traveling salesman prob. */
            this.tsp = new TSP(new FileReader(curr));
            this.panel.setTSP(this.tsp); /* store the loaded TSP */
            this.stat.setText("traveling salesman problem loaded ("
                    + file.getName() + ").");
        } catch (IOException e) {
            String msg = e.getMessage();
            this.stat.setText(msg);
            System.err.println(msg);
            JOptionPane.showMessageDialog(this, msg,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }                           /* set the status text */
        this.curr = file;           /* note the new file name */
    }  /* loadTSP() */

  /*------------------------------------------------------------------*/

    public void saveTSP(File file) {                             /* --- save a traveling salesman p. */
        if (file == null) {         /* if no file name is given */
            if (chooser == null) chooser = createChooser();
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            int r = chooser.showDialog(this, null);
            if (r != JFileChooser.APPROVE_OPTION) return;
            file = chooser.getSelectedFile();
        }                           /* get the selected file */
        try {                       /* save the current TSP */
            FileWriter writer = new FileWriter(file);
            writer.write(this.tsp.toString());
            writer.close();
        } catch (IOException e) {
            String msg = e.getMessage();
            this.stat.setText(msg);
            System.err.println(msg);
            JOptionPane.showMessageDialog(this, msg,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }                           /* set the status text */
        this.curr = file;           /* note the new file name */
    }  /* saveTSP() */

  /*------------------------------------------------------------------*/

    private void genTSP(int vertcnt, long seed) {                             /* --- generate random pointgon */
        if (this.cnt >= 0) return;  /* check for running update */
        Random rand = (seed > 0) ? new Random(seed) : new Random();
        this.tsp = new TSP(vertcnt, rand);
        this.tsp.transform(10.0, 0, 0);
        this.panel.setTSP(ACODemo.this.tsp);
        this.repaint();             /* repaint the window contents */
        this.stat.setText("random traveling salesman problem generated.");
        this.curr = null;           /* invalidate the file name */
    }  /* genTSP() */

  /*------------------------------------------------------------------*/

    private JDialog createRandTSP() {                             /* --- create "Generate..." dialog */
        final JDialog dlg = new JDialog(this,
                "Generate Random TSP...");
        GridBagLayout g = new GridBagLayout();
        GridBagConstraints lc = new GridBagConstraints();
        GridBagConstraints rc = new GridBagConstraints();
        JPanel grid = new JPanel(g);
        JPanel bbar;
        JLabel lbl;
        JTextArea help;
        JButton btn;

        grid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        lc.fill =              /* fill fields in both directions */
                rc.fill = GridBagConstraints.BOTH;
        rc.weightx = 1.0;         /* resize only the input fields, */
        lc.weightx = 0.0;         /* but not the labels */
        lc.weighty = 0.0;         /* resize lines equally */
        rc.weighty = 0.0;         /* in vertical direction */
        lc.ipadx = 10;          /* gap between labels and inputs */
        lc.ipady = 10;          /* make all lines of the same height */
        rc.gridwidth = GridBagConstraints.REMAINDER;

        lbl = new JLabel("Number of vertices:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JSpinner vertcnt = new JSpinner(
                new SpinnerNumberModel(30, 1, 999999, 1));
        g.setConstraints(vertcnt, rc);
        grid.add(vertcnt);

        lbl = new JLabel("Seed for random numbers:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JSpinner seed = new JSpinner(
                new SpinnerNumberModel(0, 0, 999999, 1));
        g.setConstraints(seed, rc);
        grid.add(seed);

        help = new JTextArea(
                "If the seed for the pseudo-random number generator\n"
                        + "is set to zero, the system time will be used instead.");
        help.setFont(small);
        help.setEditable(false);
        help.setBackground(this.getBackground());
        g.setConstraints(help, rc);
        grid.add(help);

        bbar = new JPanel(new GridLayout(1, 2, 5, 5));
        bbar.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 3));
        btn = new JButton("Ok");
        bbar.add(btn);
        btn.addActionListener(e -> {
            dlg.setVisible(false);
            ACODemo.this.genTSP((Integer) vertcnt.getValue(),
                    ((Integer) seed.getValue()).longValue());
        });
        btn = new JButton("Apply");
        bbar.add(btn);
        btn.addActionListener(e -> ACODemo.this.genTSP((Integer) vertcnt.getValue(),
                ((Integer) seed.getValue()).longValue()));
        btn = new JButton("Close");
        bbar.add(btn);
        btn.addActionListener(e -> dlg.setVisible(false));

        dlg.getContentPane().add(grid, BorderLayout.CENTER);
        dlg.getContentPane().add(bbar, BorderLayout.SOUTH);
        dlg.setLocationRelativeTo(this);
        dlg.setLocation(664, 0);
        dlg.pack();
        return dlg;
    }  /* createRandTSP() */

  /*------------------------------------------------------------------*/

    public void saveImage(File file) {                             /* --- save image to a file */
        if (file == null) {         /* if no file name is given */
            if (this.chooser == null) this.chooser = this.createChooser();
            this.chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            int r = this.chooser.showDialog(this, null);
            if (r != JFileChooser.APPROVE_OPTION) return;
            file = this.chooser.getSelectedFile();
        }                           /* let the user choose a file name */
        try {                       /* open an output stream */
            FileOutputStream stream = new FileOutputStream(file);
            ImageIO.write(this.panel.makeImage(), "png", stream);
            stream.close();
        }         /* save the decision tree image */ catch (IOException e) {     /* catch and report i/o errors */
            String msg = e.getMessage();
            stat.setText(msg);
            System.err.println(msg);
            JOptionPane.showMessageDialog(this, msg,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }                           /* set the status text */
    }  /* saveImage() */

  /*------------------------------------------------------------------*/

    private JDialog createAnts() {                             /* --- create ant colony dialog */
        final JDialog dlg = new JDialog(this, "Create Ant Colony...");
        GridBagLayout g = new GridBagLayout();
        GridBagConstraints lc = new GridBagConstraints();
        GridBagConstraints rc = new GridBagConstraints();
        JPanel grid = new JPanel(g);
        JPanel bbar;
        JLabel lbl;
        JTextArea help;
        JButton btn;

        grid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        lc.fill =              /* fill fields in both directions */
                rc.fill = GridBagConstraints.BOTH;
        rc.weightx = 1.0;         /* resize only the input fields, */
        lc.weightx = 0.0;         /* but not the labels */
        lc.weighty = 0.0;         /* resize lines equally */
        rc.weighty = 0.0;         /* in vertical direction */
        lc.ipadx = 10;          /* gap between labels and inputs */
        lc.ipady = 10;          /* make all lines of the same height */
        rc.gridwidth = GridBagConstraints.REMAINDER;

        lbl = new JLabel("Number of ants:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JSpinner antcnt = new JSpinner(
                new SpinnerNumberModel(30, 1, 999999, 1));
        g.setConstraints(antcnt, rc);
        grid.add(antcnt);

        lbl = new JLabel("Seed for random numbers:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JSpinner seed = new JSpinner(
                new SpinnerNumberModel(0, 0, 999999, 1));
        g.setConstraints(seed, rc);
        grid.add(seed);

        help = new JTextArea(
                "If the seed for the pseudo-random number generator\n"
                        + "is set to zero, the system time will be used instead.");
        help.setFont(small);
        help.setEditable(false);
        help.setBackground(this.getBackground());
        g.setConstraints(help, rc);
        grid.add(help);

        lbl = new JLabel("Initial pheromone:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JTextField phinit = new JTextField("0");
        phinit.setFont(font);
        g.setConstraints(phinit, rc);
        grid.add(phinit);

        lbl = new JLabel("Exploitation probability:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JTextField exploit = new JTextField("0.2");
        exploit.setFont(font);
        g.setConstraints(exploit, rc);
        grid.add(exploit);

        lbl = new JLabel("Pheromone trail weight:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JTextField alpha = new JTextField("1");
        alpha.setFont(font);
        g.setConstraints(alpha, rc);
        grid.add(alpha);

        lbl = new JLabel("Inverse distance weight:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JTextField beta = new JTextField("1");
        beta.setFont(font);
        g.setConstraints(beta, rc);
        grid.add(beta);

        lbl = new JLabel("Evaporation fraction:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JTextField evap = new JTextField("0.1");
        evap.setFont(font);
        g.setConstraints(evap, rc);
        grid.add(evap);

        lbl = new JLabel("Trail laying exponent:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JTextField layexp = new JTextField("1");
        layexp.setFont(font);
        g.setConstraints(layexp, rc);
        grid.add(layexp);

        lbl = new JLabel("Elite enhancement:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JTextField elite = new JTextField("0.1");
        elite.setFont(font);
        g.setConstraints(elite, rc);
        grid.add(elite);

        bbar = new JPanel(new GridLayout(1, 2, 5, 5));
        bbar.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 3));
        btn = new JButton("Ok");
        bbar.add(btn);
        btn.addActionListener(e -> {
            dlg.setVisible(false);
            int s = (Integer) seed.getValue();
            ACODemo.this.panel.initAnts(
                    (Integer) antcnt.getValue(),
                    Double.parseDouble(phinit.getText()),
                    (s != 0) ? new Random(s) : new Random());
            ACODemo.this.panel.setParams(
                    Double.parseDouble(exploit.getText()),
                    Double.parseDouble(alpha.getText()),
                    Double.parseDouble(beta.getText()),
                    Double.parseDouble(layexp.getText()),
                    Double.parseDouble(elite.getText()),
                    Double.parseDouble(evap.getText()));
        });
        btn = new JButton("Apply");
        bbar.add(btn);
        btn.addActionListener(e -> {
            int s = (Integer) seed.getValue();
            ACODemo.this.panel.initAnts(
                    (Integer) antcnt.getValue(),
                    Double.parseDouble(phinit.getText()),
                    (s != 0) ? new Random(s) : new Random());
            ACODemo.this.panel.setParams(
                    Double.parseDouble(exploit.getText()),
                    Double.parseDouble(alpha.getText()),
                    Double.parseDouble(beta.getText()),
                    Double.parseDouble(layexp.getText()),
                    Double.parseDouble(elite.getText()),
                    Double.parseDouble(evap.getText()));
        });
        btn = new JButton("Close");
        bbar.add(btn);
        btn.addActionListener(e -> dlg.setVisible(false));

        dlg.getContentPane().add(grid, BorderLayout.CENTER);
        dlg.getContentPane().add(bbar, BorderLayout.SOUTH);
        dlg.setLocationRelativeTo(this);
        dlg.setLocation(664, 145);
        dlg.pack();
        return dlg;
    }  /* createAnts() */

  /*------------------------------------------------------------------*/

    private void runAnts(int epochs, int delay) {                             /* --- run the ants */
        if (this.cnt >= 0) {        /* check for running update */
            this.timer.stop();
            this.cnt = -1;
            return;
        }
        AntColony ants = this.panel.getAnts();
        if (ants == null) return;   /* get the ant colony */
        if (delay <= 0) {           /* if to update at the end */
            while (--epochs >= 0)     /* while more epochs to compute, */
                this.panel.runAnts();   /* run the ants again */
            this.panel.repaint();     /* redraw the window contents */
            this.stat.setText("epoch: " + ants.getEpoch()
                    + ", best tour: " + ants.getBestLen());
            return;                   /* show a status message, */
        }                           /* then abort the function */
        this.cnt = epochs;        /* note the epochs */
        this.timer = new Timer(delay, e -> {
            if (--ACODemo.this.cnt < 0) {
                ACODemo.this.timer.stop();
                return;
            }
            ACODemo.this.panel.runAnts(); /* run the ants and */
            ACODemo.this.panel.repaint(); /* redraw the window contents */
            AntColony ants1 = ACODemo.this.panel.getAnts();
            ACODemo.this.stat.setText("epoch: " + ants1.getEpoch()
                    + ", best tour: " + ants1.getBestLen());
        });                    /* update the status text */
        this.timer.start();         /* start the status update timer */
    }  /* runAnts() */

  /*------------------------------------------------------------------*/

    private JDialog createRunOpt() {                             /* --- create run dialog */
        final JDialog dlg = new JDialog(this, "Run Optimization...");
        GridBagLayout g = new GridBagLayout();
        GridBagConstraints lc = new GridBagConstraints();
        GridBagConstraints rc = new GridBagConstraints();
        JPanel grid = new JPanel(g);
        JPanel bbar;
        JLabel lbl;
        JTextArea help;
        JButton btn;

        grid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        lc.fill =              /* fill fields in both directions */
                rc.fill = GridBagConstraints.BOTH;
        rc.weightx = 1.0;         /* resize only the input fields, */
        lc.weightx = 0.0;         /* but not the labels */
        lc.weighty = 0.0;         /* resize lines equally */
        rc.weighty = 0.0;         /* in vertical direction */
        lc.ipadx = 10;          /* gap between labels and inputs */
        lc.ipady = 10;          /* make all lines of the same height */
        rc.gridwidth = GridBagConstraints.REMAINDER;

        lbl = new JLabel("Number of epochs:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JSpinner epochs = new JSpinner(
                new SpinnerNumberModel(5000, 1, 999999, 1));
        g.setConstraints(epochs, rc);
        grid.add(epochs);

        lbl = new JLabel("Delay between epochs:");
        g.setConstraints(lbl, lc);
        grid.add(lbl);
        final JSpinner delay = new JSpinner(
                new SpinnerNumberModel(200, 0, 999999, 10));
        g.setConstraints(delay, rc);
        grid.add(delay);

        bbar = new JPanel(new GridLayout(1, 2, 5, 5));
        bbar.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 3));
        btn = new JButton("Ok");
        bbar.add(btn);
        btn.addActionListener(e -> {
            dlg.setVisible(false);
            ACODemo.this.runAnts((Integer) epochs.getValue(),
                    (Integer) delay.getValue());
        });
        btn = new JButton("Apply");
        bbar.add(btn);
        btn.addActionListener(e -> ACODemo.this.runAnts((Integer) epochs.getValue(),
                (Integer) delay.getValue()));
        btn = new JButton("Close");
        bbar.add(btn);
        btn.addActionListener(e -> dlg.setVisible(false));

        dlg.getContentPane().add(grid, BorderLayout.CENTER);
        dlg.getContentPane().add(bbar, BorderLayout.SOUTH);
        dlg.setLocationRelativeTo(this);
        dlg.setLocation(664, 465);
        dlg.pack();
        return dlg;
    }  /* createRunOpt() */

  /*------------------------------------------------------------------*/

    private JDialog createAbout() {                             /* --- create "About..." dialog box */
        final JDialog dlg = new JDialog(this, "About ACODemo...", true);
        Container pane = dlg.getContentPane();
        LogoPanel logo = new LogoPanel();
        JButton btn = new JButton("Ok");
        JPanel rest = new JPanel(new BorderLayout(2, 2));
        JTextArea text = new JTextArea
                ("ACODemo\n"
                        + "An Ant Colony Optimization Demo\n"
                        + "Version " + ACODemo.VERSION + "\n\n"
                        + "written by Christian Borgelt\n"
                        + "Otto-von-Guericke-University of Magdeburg\n"
                        + "Universitatsplatz 2, D-39106 Magdeburg\n"
                        + "e-mail: borgelt@iws.cs.uni-magdeburg.de");
        text.setBackground(this.getBackground());
        text.setFont(new Font("Dialog", Font.BOLD, 12));
        text.setEditable(false);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dlg.setVisible(false);
            }
        });
        rest.add(logo, BorderLayout.NORTH);
        rest.add(btn, BorderLayout.SOUTH);
        pane.setLayout(new FlowLayout());
        pane.add(text);
        pane.add(rest);
        dlg.setLocationRelativeTo(this);
        dlg.pack();
        dlg.setResizable(false);
        return dlg;
    }  /* createAbout() */

  /*------------------------------------------------------------------*/

    public void mousePressed(MouseEvent me) {                             /* --- handle mouse clicks */
        int m;                      /* event modifiers */

        if (this.tsp == null) return;  /* check for a TSP */
        this.mode = 0;              /* clear the operation mode */
        this.mx = me.getX();        /* note the coordinates of the point */
        this.my = me.getY();        /* at which the mouse was pressed */
        m = me.getModifiers();      /* get the modifiers (buttons) */
        switch (m) {                /* depending on the mouse button */
            case InputEvent.BUTTON1_MASK:
                this.mode = 1;
                break;   /* panning mode */
            case InputEvent.BUTTON2_MASK:  /* scaling mode (low  factor) */
            case InputEvent.BUTTON3_MASK:  /* scaling mode (high factor) */
                this.mode = (m == InputEvent.BUTTON2_MASK) ? 2 : 3;
                this.scale = this.panel.getScale();
                this.my -= this.scroll.getViewport().getViewPosition().y;
                break;                  /* adapt position to viewport */
        }                           /* (set the mouse operation mode) */
    }  /* mousePressed() */

  /*------------------------------------------------------------------*/

    public void mouseDragged(MouseEvent e) {                             /* --- handle mouse movement */
        JViewport view;             /* viewport of scrollpane */
        Point refp;             /* coordinates of upper left corner */
        Dimension size;             /* extension of viewing area */
        int xmax, ymax, d;    /* maximum view position, movement */
        double scl;              /* new scaling factor */

        if ((this.tsp == null)         /* check for a TSP and */
                || (this.mode <= 0)) return;  /* for a valid operation mode */
        view = this.scroll.getViewport();
        refp = view.getViewPosition(); /* get reference point */
        if (this.mode == 1) {       /* if to do panning */
            refp.x += this.mx - e.getX();
            refp.y += this.my - e.getY();
            size = this.panel.getPreferredSize();
            xmax = size.width;
            ymax = size.height;
            size = view.getExtentSize();  /* get maximum reference point */
            xmax -= size.width;
            ymax -= size.height;
            if (refp.x > xmax) {
                this.mx -= refp.x - xmax;
                refp.x = xmax;
            }
            if (refp.x < 0) {
                this.mx -= refp.x;
                refp.x = 0;
            }
            if (refp.y > ymax) {
                this.my -= refp.y - ymax;
                refp.y = ymax;
            }
            if (refp.y < 0) {
                this.my -= refp.y;
                refp.y = 0;
            }
            view.setViewPosition(refp);
        } else {                      /* if to do scaling */
            d = (e.getY() - refp.y) - this.my;
            scl = Math.pow((this.mode > 2) ? 1.004 : 1.02, d);
            this.panel.setScale(this.scale * scl);
        }                           /* set new scaling factor */
        this.panel.revalidate();    /* adapt the enclosing scroll pane */
        this.panel.repaint();       /* and redraw the TSP */
    }  /* mouseDragged() */

  /*------------------------------------------------------------------*/

    public void mouseReleased(MouseEvent e) {
        this.mode = 0;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

  /*------------------------------------------------------------------*/

    public void run() {                             /* --- create GUI of ACO viewer */
        JMenuBar mbar;             /* menu bar */
        JMenu menu;             /* to create menu titles */
        JMenuItem item;             /* to create menu items */

        this.getContentPane().setLayout(new BorderLayout());

    /* --- create and set the menu bar --- */
        mbar = new JMenuBar();
        this.getContentPane().add(mbar, BorderLayout.NORTH);

        menu = mbar.add(new JMenu("File"));
        menu.setMnemonic('f');
        item = menu.add(new JMenuItem("Load TSP..."));
        item.setMnemonic('l');
        item.addActionListener(e -> ACODemo.this.loadTSP(null));
        item = menu.add(new JMenuItem("Reload TSP"));
        item.setMnemonic('r');
        item.addActionListener(e -> ACODemo.this.loadTSP(ACODemo.this.curr));
        item = menu.add(new JMenuItem("Save TSP"));
        item.setMnemonic('s');
        item.addActionListener(e -> ACODemo.this.saveTSP(ACODemo.this.curr));
        item = menu.add(new JMenuItem("Save TSP as..."));
        item.setMnemonic('a');
        item.addActionListener(e -> ACODemo.this.saveTSP(null));
        item = menu.add(new JMenuItem("Save PNG Image..."));
        item.setMnemonic('i');
        item.addActionListener(e -> ACODemo.this.saveImage(null));
        menu.addSeparator();
        item = menu.add(new JMenuItem("Quit"));
        item.setMnemonic('q');
        if (this.isprog) {          /* if stand-alone program */
            item.addActionListener(e -> System.exit(0));
        }     /* terminate the program */ else {                      /* if only visualization module */
            item.addActionListener(e -> {
                if (ACODemo.this.about != null)
                    ACODemo.this.about.setVisible(false);
                if (ACODemo.this.randtsp != null)
                    ACODemo.this.randtsp.setVisible(false);
                if (ACODemo.this.antcol != null)
                    ACODemo.this.antcol.setVisible(false);
                if (ACODemo.this.runopt != null)
                    ACODemo.this.runopt.setVisible(false);
                if (ACODemo.this.params != null)
                    ACODemo.this.params.setVisible(false);
                ACODemo.this.setVisible(false);
            });                  /* only close the window */
        }                           /* and the dialog boxes */

        menu = mbar.add(new JMenu("Actions"));
        menu.setMnemonic('a');
        item = menu.add(new JMenuItem("Generate Random TSP..."));
        item.setMnemonic('g');
        item.addActionListener(e -> {
            if (ACODemo.this.randtsp == null)
                ACODemo.this.randtsp = createRandTSP();
            ACODemo.this.randtsp.setVisible(true);
        });
        item = menu.add(new JMenuItem("Create Ant Colony..."));
        item.setMnemonic('c');
        item.addActionListener(e -> {
            if (ACODemo.this.antcol == null)
                ACODemo.this.antcol = createAnts();
            ACODemo.this.antcol.setVisible(true);
        });
        item = menu.add(new JMenuItem("Run Optimization..."));
        item.setMnemonic('o');
        item.addActionListener(e -> {
            if (ACODemo.this.runopt == null)
                ACODemo.this.runopt = createRunOpt();
            ACODemo.this.runopt.setVisible(true);
        });
        item = menu.add(new JMenuItem("Stop Optimization"));
        item.setMnemonic('s');
        item.addActionListener(e -> {
            if (ACODemo.this.timer == null) return;
            ACODemo.this.timer.stop();
            ACODemo.this.cnt = -1;
        });
        menu.addSeparator();
        item = menu.add(new JMenuItem("Redraw"));
        item.setMnemonic('r');
        item.addActionListener(e -> ACODemo.this.panel.repaint());

        menu = mbar.add(new JMenu("Help"));
        menu.setMnemonic('h');
        item = menu.add(new JMenuItem("About..."));
        item.setMnemonic('a');
        item.addActionListener(e -> {
            if (ACODemo.this.about == null)
                ACODemo.this.about = ACODemo.this.createAbout();
            ACODemo.this.about.setVisible(true);
        });

    /* --- create and set the main panel --- */
        this.panel = new ACOPanel();
        this.panel.setLayout(new BorderLayout());
        this.panel.setPreferredSize(new Dimension(656, 656));
        this.panel.addMouseListener(this);
        this.panel.addMouseMotionListener(this);
        this.scroll = new JScrollPane(this.panel);
        this.getContentPane().add(this.scroll, BorderLayout.CENTER);

    /* --- create and set a status bar --- */
        this.stat = new JTextField("");
        this.stat.setEditable(false);
        this.getContentPane().add(this.stat, BorderLayout.SOUTH);

    /* --- show the frame window --- */
        this.setTitle("ACODemo");
        this.setDefaultCloseOperation(this.isprog
                ? WindowConstants.EXIT_ON_CLOSE : WindowConstants.HIDE_ON_CLOSE);
        this.setLocation(0, 0);
        this.pack();
        if (this.isprog) this.setVisible(true);
        this.stat.setText("ACODemo is up and running.");
    }  /* run() */

  /* Following the recommendations in the Java tutorial, the user   */
  /* interface is created in the "run" method, which is invoked     */
  /* from the event queue, in order to avoid problems with threads. */

  /*------------------------------------------------------------------*/

    public ACODemo(boolean isProg) {
        this.isprog = isProg;
        try {
            EventQueue.invokeAndWait(this);
        } catch (Exception e) {
        }
    }

    public ACODemo() {
        this.isprog = false;
        try {
            EventQueue.invokeAndWait(this);
        } catch (Exception e) {
        }
    }

    public ACODemo(String title) {
        this(false);
        this.setTitle(title);
    }

    public ACODemo(File file) {
        this(false);
        this.loadTSP(file);
    }

    public ACODemo(String title, File file) {
        this(title);
        this.loadTSP(file);
    }

  /*------------------------------------------------------------------*/

    public static void main(String args[]) {                             /* --- main function */
        ACODemo v = new ACODemo(true);    /* create an ACO demo viewer */
        if (args.length > 0) v.loadTSP(new File(args[0]));
    }  /* main() */               /* load traveling salesman problem */

}  /* class ACODemo */
