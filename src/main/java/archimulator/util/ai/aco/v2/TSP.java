/*----------------------------------------------------------------------
  File    : TSP.java
  Contents: a traveling salesman problem
  Author  : Christian Borgelt
  History : 19.11.2005 file created
----------------------------------------------------------------------*/
package archimulator.util.ai.aco.v2;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Random;

/*--------------------------------------------------------------------*/
public class TSP {
/*--------------------------------------------------------------------*/

    /* --- constants --- */
    private static final int BLKSIZE = 16;

    /* --- instance variables --- */
    protected int size;    /* current number of vertices */
    protected double[] xs, ys;  /* coordinates of vertices */
    protected double[][] dists;   /* distances between vertices */
    protected boolean sym;     /* flag for symmetric distances */
    protected boolean euclid;  /* flag for euclidean distances */
    protected int[] tour;    /* best (known) tour */
    private double bbx, bby;/* bounding box of vertices */
    private double bbw, bbh;/* (position and width and height) */
    private boolean valid;   /* flag for valid bounding box */

  /*------------------------------------------------------------------*/

    public TSP() {
        this(BLKSIZE);
    }

    public TSP(int size) {                             /* --- create a traveling salesman p. */
        this.size = 0;            /* initialize the variables */
        this.xs = new double[size];
        this.ys = new double[size];
        this.dists = null;         /* do not create distance matrix yet */
        this.euclid = true;         /* default: Euclidean distances */
        this.sym = true;         /* default: symmetric distances */
        this.tour = null;         /* there is no known (best) tour */
        this.valid = false;        /* the bounding box is not valid */
    }  /* TSP() */

  /*------------------------------------------------------------------*/

    public TSP(int size, Random rand) {                             /* --- create a traveling salesman p. */
        this(size);                 /* initialize the variables, */
        this.randomize(rand);       /* create random vertices, and */
        this.makeDists(true);       /* calculate the distances */
    }  /* TSP() */

  /*------------------------------------------------------------------*/

    private void resize(int size) {                             /* --- resize coord. vectors */
        int k;                   /* current/new vector size */
        double v[];                 /* buffer for reallocation */

        k = this.size;              /* get the number of nodes to copy */
        if (size < 0) size = k + ((k < BLKSIZE) ? BLKSIZE : (k >> 1));
        if (size < k) k = this.size = size;
        System.arraycopy(this.xs, 0, v = new double[size], 0, k);
        this.xs = v;                /* enlarge the x-coordinates vector */
        System.arraycopy(this.ys, 0, v = new double[size], 0, k);
        this.ys = v;                /* enlarge the y-coordinates vector */
    }  /* resize() */

  /*------------------------------------------------------------------*/

    public int add(double x, double y) {                             /* --- add a vertex */
        if (this.size >= this.xs.length)
            this.resize(-1);          /* resize the coord. vectors if nec. */
        this.valid = false;         /* bounding box is no longer valid */
        this.xs[this.size] = x;     /* set the coordinates */
        this.ys[this.size] = y;     /* of the new vertex */
        return this.size++;         /* and return its index */
    }  /* add() */

  /*------------------------------------------------------------------*/

    public void randomize(Random rand) {                             /* --- create random vertices */
        for (int i = this.size = this.xs.length; --i >= 0; ) {
            this.xs[i] = rand.nextDouble();
            this.ys[i] = rand.nextDouble();
        }                           /* set random coordinates */
        this.valid = false;         /* bounding box is no longer valid */
    }  /* randomize() */

  /*------------------------------------------------------------------*/

    public int size() {
        return this.size;
    }

    public double getX(int i) {
        return this.xs[i];
    }

    public double getY(int i) {
        return this.ys[i];
    }

    public void setPos(int i, double x, double y) {
        this.xs[i] = x;
        this.ys[i] = y;
    }

  /*------------------------------------------------------------------*/

    public void transform(double scale, double xoff, double yoff) {                             /* --- transform vertex coordinates */
        for (int i = this.size; --i >= 0; ) {
            this.xs[i] = this.xs[i] * scale + xoff;
            this.ys[i] = this.ys[i] * scale + yoff;
        }                           /* traverse and transform vertices */
        this.valid = false;         /* bounding box is no longer valid */
    }  /* transform() */

  /*------------------------------------------------------------------*/

    private void bbox() {                             /* --- compute bounding box */
        int i;                   /* loop variable */
        double x, y;                /* coordinates of a vertex */
        double xmax, ymax;          /* maximal x- and y-coordinates */

        this.bbx = Double.MAX_VALUE;
        xmax = -Double.MAX_VALUE;
        this.bby = Double.MAX_VALUE;
        ymax = -Double.MAX_VALUE;
        for (i = this.xs.length; --i >= 0; ) {
            x = this.xs[i];          /* traverse the vertices */
            y = this.ys[i];          /* of the problem */
            if (x < this.bbx) this.bbx = x;
            if (x > xmax) xmax = x;
            if (y < this.bby) this.bby = y;
            if (y > ymax) ymax = y;
        }                           /* find minimum and maximum coords. */
        this.bbw = xmax - this.bbx;  /* compute the width and height */
        this.bbh = ymax - this.bby;  /* of the bounding box */
        this.valid = true;          /* the bounding box is now valid */
    }  /* bbox() */

  /*------------------------------------------------------------------*/

    public double getX() {
        if (!this.valid) this.bbox();
        return this.bbx;
    }

    public double getY() {
        if (!this.valid) this.bbox();
        return this.bby;
    }

    public double getWidth() {
        if (!this.valid) this.bbox();
        return this.bbw;
    }

    public double getHeight() {
        if (!this.valid) this.bbox();
        return this.bbh;
    }

  /*------------------------------------------------------------------*/

    public void makeDists(boolean calc) {                             /* --- calculate distance matrix */
        int i, k;                /* loop variables */
        double dx, dy;              /* coordinate-wise distances */
        double v[];                 /* buffer for reallocation */

        if (this.size < this.xs.length)
            this.resize(this.size);   /* shrink the coord. vectors if poss. */
        this.dists = new double[this.size][this.size];
        if (!calc) return;          /* create the distance matrix */
        for (i = this.size; --i >= 0; ) {
            this.dists[i][i] = 0;     /* set diagonal elements to zero */
            for (k = i; --k >= 0; ) { /* traverse the off-diagonal elements */
                dx = this.xs[i] - this.xs[k];
                dy = this.ys[i] - this.ys[k];
                this.dists[i][k] = this.dists[k][i] = Math.sqrt(dx * dx + dy * dy);
            }                         /* compute pairwise vertex distances */
        }                           /* (Euclidian distances, symmetric) */
        this.euclid = this.sym = true;
    }  /* makeDists() */

  /*------------------------------------------------------------------*/

    public boolean isSymmetric() {
        return this.sym;
    }

    public double getDist(int i, int j) {
        return this.dists[i][j];
    }

    public void setDist(int i, int j, double dist) {
        this.dists[i][j] = this.dists[j][i] = dist;
        this.euclid = false;
    }

    public void setDistAsym(int i, int j, double dist) {
        this.dists[i][j] = dist;
        this.sym = false;
        this.euclid = false;
    }

  /*------------------------------------------------------------------*/

    public int[] getTour() {
        return this.tour;
    }

    public void setTour(int[] tour) {                             /* --- set a (best) tour */
        if (this.tour == null)      /* create a tour if necessary */
            this.tour = new int[this.size];
        System.arraycopy(tour, 0, this.tour, 0, this.size);
    }  /* setTour() */            /* copy the given tour */

  /*------------------------------------------------------------------*/

    public String toString() {                             /* --- create a string description */
        int i, k;          /* loop variables */
        StringBuffer s;             /* created string description */

        s = new StringBuffer("TSP = {\n");
        s.append("  vertices = {"); /* there are always vertices */
        s.append("\n    (" + this.xs[0] + ", " + this.ys[0] + ")");
        for (i = 1; i < this.size; i++)
            s.append(",\n    (" + this.xs[i] + ", " + this.ys[i] + ")");
        s.append("\n  };\n");       /* list the vertices */
        if (!this.euclid) {         /* if the distances are not Euclidean */
            s.append("  distances = {");
            for (i = 0; i < this.size; i++) {
                if (i > 0) s.append(",");
                s.append("\n    { " + this.dists[i][0]);
                for (k = 1; k < this.size; k++)
                    s.append(", " + this.dists[i][k]);
                s.append(" }");         /* list the pairwise distances */
            }                         /* (which are possibly asymmetric, */
            s.append("\n  };\n");     /* so that the full distance matrix */
        }                           /* may be needed) */
        if (this.tour != null) {    /* if a (best) tour is known */
            s.append("  tour = {\n    " + this.tour[0]);
            for (i = 1; i < this.size; i++)
                s.append(", " + this.tour[i]);
            s.append("\n  };\n");     /* list the vertices of the tour */
        }
        s.append("};\n");           /* terminate the description */
        return s.toString();        /* return the created string */
    }  /* toString() */

  /*------------------------------------------------------------------*/

    public TSP(Scanner s) throws IOException {                             /* --- read a traveling salesman p. */
        this();                     /* initialize the variables */
        int i, k;                /* loop variables */
        double x, y;                /* buffers for coordinates */

    /* --- read vertices --- */
        s.getID("TSP");             /* check for keyword 'TSP' */
        s.getChar('=');             /* check for a '=' */
        s.getChar('{');             /* check for a '{' */
        s.getID("vertices");        /* check for keyword 'vertices' */
        s.getChar('=');             /* check for a '=' */
        s.getChar('{');             /* check for a '{' */
        if (s.nextToken() == '}')   /* if the list is empty, abort */
            throw new IOException("no vertices in TSP");
        s.pushBack();               /* push back already read token */
        do {                        /* vertex read loop */
            s.getChar('(');           /* check for a '(' */
            s.getNumber();            /* get the x-coordinate */
            x = Double.parseDouble(s.value);
            s.getChar(',');           /* check for a ',' */
            s.getNumber();            /* get the y-coordinate */
            y = Double.parseDouble(s.value);
            s.getChar(')');           /* check for a ')' */
            if (this.size >= this.xs.length)
                this.resize(-1);        /* resize the coord. vectors if nec. */
            this.xs[this.size] = x; /* store the coordinates */
            this.ys[this.size++] = y; /* of the new vertex */
        } while (s.nextToken() == ',');
        s.pushBack();               /* push back last token */
        s.getChar('}');             /* check for a '}' */
        s.getChar(';');             /* check for a ';' */
        if (this.size < this.xs.length)
            this.resize(this.size);   /* shrink coord. vectors if possible */

    /* --- read distances --- */
        if ((s.nextToken() != Scanner.T_ID)
                || !s.value.equals("distances")) {
            this.makeDists(true);     /* calculate distances if necessary */
            s.pushBack();
        }           /* (symmetric, Euclidean distances) */ else {                      /* if distances are given */
            s.getChar('=');           /* check for a '=' */
            s.getChar('{');           /* check for a '{' */
            this.dists = new double[this.size][this.size];
            for (i = 0; i < this.size; i++) {
                if (i > 0) s.getChar(',');
                s.getChar('{');         /* check for a '{' */
                for (k = 0; k < this.size; k++) {
                    if (k > 0) s.getChar(',');
                    s.getNumber();        /* check for a number */
                    this.dists[i][k] = Double.parseDouble(s.value);
                }                       /* store the distance */
                s.getChar('}');         /* check for a '}' */
            }
            this.euclid = false;      /* distances were read */
            s.getChar('}');           /* check for a '}' */
            s.getChar(';');           /* check for a ';' */
            this.sym = true;          /* default: symmetric distances */
            for (i = this.size; (--i >= 0) && this.sym; ) {
                for (k = i; --k >= 0; )
                    if (this.dists[i][k] != this.dists[k][i]) {
                        this.sym = false;
                        break;
                    }
            }                         /* check for symmetric distances */
        }                           /* and set flag accordingly */

    /* --- read tour --- */
        if ((s.nextToken() != Scanner.T_ID)
                || !s.value.equals("tour"))
            s.pushBack();             /* check whether a tour is known */
        else {                      /* if there is a tour */
            s.getChar('=');           /* check for a '=' */
            s.getChar('{');           /* check for a '{' */
            for (i = 0; i < this.size; i++) {
                if (i > 0) s.getChar(',');
                s.getNumber();          /* check for a number */
                this.tour[i] = Integer.parseInt(s.value);
            }                         /* store the vertex index */
            s.getChar('}');           /* check for a '}' */
            s.getChar(';');           /* check for a ';' */
        }

        s.getChar('}');             /* check for a '}' */
        s.getChar(';');             /* check for a ';' */
    }  /* TSP() */

  /*------------------------------------------------------------------*/

    public TSP(String desc) throws IOException {
        this(new Scanner(desc));
    }

    public TSP(InputStream in) throws IOException {
        this(new Scanner(in));
    }

    public TSP(Reader reader) throws IOException {
        this(new Scanner(reader));
    }

  /*------------------------------------------------------------------*/

    public static void main(String args[]) {                             /* --- main function for testing */
        int size;                /* number of vertices */
        double scale = 1.0;         /* scaling factor */
        long seed;                /* seed value for random numbers */
        TSP tsp;                 /* created random TSP */

        if (args.length <= 0) {     /* if no file name is given */
            System.err.println("usage: TSP <vertcnt> [<scale>] [<seed>]");
            return;                   /* print a usage message */
        }                           /* and abort the program */
        seed = System.currentTimeMillis();
        size = Integer.parseInt(args[0]);
        if (args.length > 1) scale = Double.parseDouble(args[1]);
        if (args.length > 2) seed = Integer.parseInt(args[2]);
        tsp = new TSP(size, new Random(seed));
        tsp.transform(scale, 0, 0);   /* create a TSP and scale it */
        System.out.print(tsp);      /* print the created TSP */
    }  /* main() */

}  /* class TSP */
