/*----------------------------------------------------------------------
  File    : ACOPanel.java
  Contents: ant colony optimization viewer panel
  Author  : Christian Borgelt
  History : 2005.11.19 file created
            2013.04.22 adapted to type argument of Comparable
---------------------------------------------------------------------*/
package archimulator.util.ai.aco.v2;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

/*--------------------------------------------------------------------*/
class Edge implements Comparable<Edge> {
/*--------------------------------------------------------------------*/

    /* --- instance variables --- */
    protected int i, j;           /* indices of connected vertices */
    protected int c;              /* color index */

  /*------------------------------------------------------------------*/

    public Edge() {
    }

    public Edge(int i, int j, int c) {
        this.i = i;
        this.j = j;
        this.c = c;
    }

  /*------------------------------------------------------------------*/

    public int compareTo(Edge obj) {                             /* --- compare two edges */
        if (this.c < obj.c) return -1;
        if (this.c > obj.c) return +1;
        return 0;                   /* return sign of color difference */
    }  /* compareTo() */

}  /* class Edge() */


/*--------------------------------------------------------------------*/
public class ACOPanel extends JPanel {
/*--------------------------------------------------------------------*/

    private static final long serialVersionUID = 0x00010000L;

    /* --- instance variables --- */
    private TSP tsp;        /* traveling salesman problem */
    private AntColony antc;       /* ant colony for TSP */
    private double xoff, yoff; /* translation parameters */
    private double scale;      /* scaling factor */
    private int[] xs, ys;     /* screen coordinates */
    private Edge[] edges;      /* (sorted) list of edges */
    private Color[] cols;       /* colors for trail */
    private Stroke thick;      /* stroke for trail drawing */
    private Stroke thin;       /* stroke for tour drawing */

  /*------------------------------------------------------------------*/

    public ACOPanel() {                             /* --- create an ACO panel */
        this.tsp = null;          /* there is no TSP */
        this.antc = null;          /* and no ant colony yet */
        this.xoff = this.yoff = 0; /* initialize offset */
        this.scale = 64.0;          /* and scale */
        this.cols = new Color[256];/* initialize the colors */
        for (int i = 256; --i >= 0; )
            this.cols[255 - i] = new Color(i / 255.0F, i / 255.0F, i / 255.0F);
        this.thick = new BasicStroke(7.0F);
        this.thin = new BasicStroke(2.0F);
    }  /* ACOPanel() */

  /*------------------------------------------------------------------*/

    public ACOPanel(TSP tsp) {
        this();
        this.setTSP(tsp);
    }

  /*------------------------------------------------------------------*/

    public TSP getTSP() {
        return this.tsp;
    }

    public AntColony getAnts() {
        return this.antc;
    }

  /*------------------------------------------------------------------*/

    public void setTSP(TSP tsp) {                             /* --- set new traveling salesman p. */
        this.antc = null;          /* delete the ant colony */
        this.edges = null;          /* and the edge vector */
        this.tsp = tsp;           /* and store the new TSP */
        if (this.tsp == null)       /* if no tsp, set default size */
            this.setPreferredSize(new Dimension(656, 656));
        else {                      /* if a new TSP was set */
            this.xoff = tsp.getX();
            this.yoff = tsp.getY();   /* set default offset */
            this.setScale(64.0);      /* and default scale */
            int n = this.tsp.size();  /* create the edge vector */
            this.edges = new Edge[n = (n * (n - 1)) >> 1];
            while (--n >= 0) this.edges[n] = new Edge();
        }                           /* create the edges */
        this.revalidate();          /* adapt the enclosing scroll pane */
        this.repaint();             /* and redraw the TSP */
    }  /* setTSP() */

  /*------------------------------------------------------------------*/

    public double getScale() {
        return this.scale;
    }

  /*------------------------------------------------------------------*/

    public void setScale(double scale) {                             /* --- rescale the display */
        int i, n;             /* loop variables, number of vertices */
        Dimension d;                /* preferred size of panel */
        int w, h;             /* size of background rectangle */

        this.scale = scale;         /* set new scaling factor */
        d = new Dimension();        /* compute new preferred size */
        d.width = (int) (this.tsp.getWidth() * scale + 16.5);
        d.height = (int) (this.tsp.getHeight() * scale + 16.5);
        this.setPreferredSize(d);   /* set new preferred size */
        w = 8;
        h = d.height - 8;     /* get the coordinates of the origin */
        n = this.tsp.size();        /* get the number of points */
        this.xs = new int[n];       /* and create buffer vectors */
        this.ys = new int[n];       /* for the transformed points */
        for (i = n; --i >= 0; ) {   /* traverse the points */
            this.xs[i] = (int) (w + scale * (this.tsp.getX(i) - this.xoff) + 0.5);
            this.ys[i] = (int) (h - scale * (this.tsp.getY(i) - this.yoff) + 0.5);
        }                           /* compute screen coordinates */
    }  /* setScale() */

  /*------------------------------------------------------------------*/

    public void initAnts(int antcnt, double phero, Random rand) {                             /* --- initialize an ant colony */
        if (this.tsp == null) return;
        this.antc = new AntColony(this.tsp, antcnt, rand);
        this.antc.init(phero);      /* create and init. an ant colony */
        this.repaint();             /* and redraw the TSP */
    }  /* initAnts() */

  /*------------------------------------------------------------------*/

    public void setParams(double exploit, double alpha, double beta,
                          double trail, double elite, double evap) {                             /* --- set parameters */
        if (this.antc == null) return;
        this.antc.setExploit(exploit);
        this.antc.setAlpha(alpha);
        this.antc.setBeta(beta);
        this.antc.setTrail(trail);
        this.antc.setElite(elite);
        this.antc.setEvap(evap);
    }  /* setParams() */

  /*------------------------------------------------------------------*/

    public void runAnts() {                             /* --- run the ants */
        if (this.antc == null) return;
        this.antc.runAllAnts();     /* run all ants once */
        this.repaint();             /* and redraw the TSP */
    }  /* runAnts() */

  /*------------------------------------------------------------------*/

    public void paint(Graphics g) {                             /* --- (re)paint the whole panel */
        int i, j, k, n;       /* loop variables, number of vertices */
        Dimension d;                /* (preferred) size of panel */
        int w, h;             /* size of background rectangle */
        int x, y, ox, oy;     /* coordinates of points */
        double trl, avg, max;    /* (average/maximal) trail on edge */
        double scl;              /* scaling factor */
        int[] tour;             /* best tour found so far */
        Edge e;                /* to traverse the edges */

        d = this.getSize();         /* get the (preferred) panel size */
        w = d.width;
        h = d.height;  /* (whichever is larger) */
        d = this.getPreferredSize();
        if (d.width > w) w = d.width;
        if (d.height > h) h = d.height;
        g.setColor(Color.white);    /* set the background color */
        g.fillRect(0, 0, w, h);     /* draw the background */
        if (this.tsp == null)       /* if there is no TSP, */
            return;                   /* abort the function */
        n = this.tsp.size();        /* get the number of vertices and */
        w = 8;
        h = d.height - 8;     /* the coordinates of the origin */

    /* --- draw the trail --- */
        if (this.antc != null) {    /* if there is an ant colony */
            avg = this.antc.getTrailAvg();
            max = this.antc.getTrailMax();
            if (max < 2 * avg) max = 2 * avg;
            max = 255.0 / max;          /* compute color scaling factor */
            for (k = 0, i = n; --i >= 0; ) {
                for (j = i; --j >= 0; ) {
                    e = this.edges[k++];/* traverse the edges */
                    trl = this.antc.getTrail(e.i = i, e.j = j);
                    e.c = (int) (max * trl); /* compute the color index */
                    if (e.c > 255) e.c = 255;
                }                       /* bound the color index */
            }                         /* (for programming safety) */
            Arrays.sort(this.edges, 0, k);
            ((Graphics2D) g).setStroke(this.thick);
            for (i = 0; i < k; i++) { /* traverse the edges */
                e = this.edges[i];      /* get edge and set color */
                g.setColor(this.cols[e.c]);
                g.drawLine(this.xs[e.i], this.ys[e.i], this.xs[e.j], this.ys[e.j]);
            }                         /* draw a line between the vertices */
            ((Graphics2D) g).setStroke(this.thin);
            g.setColor(Color.red);    /* draw the best tour in red */
            tour = this.antc.getBestTour();
            i = tour[0];           /* get the tour and its start */
            for (k = n; --k >= 0; ) { /* traverse the edges of the tour */
                j = i;
                i = tour[k];     /* get the next vertex index */
                g.drawLine(this.xs[i], this.ys[i], this.xs[j], this.ys[j]);
            }                         /* draw the next edge of the tour */
        }                           /* (first edge closes tour) */

    /* --- draw the vertices --- */
        for (i = n; --i >= 0; ) {   /* traverse the vertices */
            x = this.xs[i];
            y = this.ys[i];
            g.setColor(Color.black);  /* black outline */
            g.fillOval(x - 4, y - 4, 9, 9);
            g.setColor(Color.red);    /* red interior */
            g.fillOval(x - 3, y - 3, 7, 7);
        }                           /* draw a circle */
    }  /* paint() */

  /*------------------------------------------------------------------*/

    public BufferedImage makeImage() {                             /* --- create an image of contents */
        BufferedImage img;          /* created image */
        Dimension d;            /* size of panel */

        d = this.getPreferredSize();
        img = new BufferedImage(d.width, d.height,
                BufferedImage.TYPE_3BYTE_BGR);
        this.paint(img.getGraphics());
        return img;                 /* draw window contents to image */
    }  /* BufferedImage() */      /* and return the image */

}  /* class ACOPanel */
