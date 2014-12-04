/*----------------------------------------------------------------------
  File    : AntColony.java
  Contents: an ant colony of an ant colony optimization algorithm
            for the traveling salesman problem
  Author  : Christian Borgelt
  History : 19.11.2005 file created
            03.12.2005 trail and inverse distance exponents exchanged
----------------------------------------------------------------------*/
package archimulator.util.ai.aco.v2;

import java.util.Random;

/*--------------------------------------------------------------------*/
public class AntColony {
/*--------------------------------------------------------------------*/

    /* --- instance variables --- */
    protected TSP tsp;     /* traveling salesman problem */
    protected double[][] dists;   /* distances between vertices */
    protected double[][] nears;   /* nearness factors: d_ij^{-\alpha} */
    protected double[][] trail;   /* pheromone trail on edges */
    protected double[][] delta;   /* change of pheromone trail on edges */
    protected double[][] quals;   /* qualities of the edges */
    protected boolean[] visited; /* flags for visited vertices */
    protected int[] tour;    /* tour of current ant */
    protected double len;     /* and its length */
    protected int[] brun;    /* best tour in current run */
    protected double brlen;   /* and its length */
    protected int[] best;    /* best tour found so far */
    protected double bestlen; /* and its length */
    protected double avglen;  /* average tour length */
    private double max;     /* maximal trail value */
    private double avg;     /* average trail value */

    protected int antcnt;  /* number of ants in each run */
    protected int epoch;   /* current number of epochs */
    protected double exploit; /* prob. for exploiting the best edge */
    protected double alpha;   /* exponent for trail values */
    protected double beta;    /* exponent for distances */
    protected double evap;    /* pheromone evaporation factor */
    protected double layexp;  /* pheromone laying exponent */
    protected double elite;   /* best tour enhancement factor */

    private Random rand;    /* random number generator */
    private int[] dsts;    /* buffer for destinations */
    private double[] sums;    /* buffer for probabilities */

  /*------------------------------------------------------------------*/

    public AntColony(TSP tsp, int antcnt, Random rand) {                             /* --- create an ant colony */
        this.tsp = tsp;         /* note traveling salesman problem */
        this.dists = tsp.dists;   /* and its distance matrix */
        int size = tsp.size();  /* create the trail matrices */
        this.nears = new double[size][size];
        this.trail = new double[size][size];
        this.delta = new double[size][size];
        this.quals = new double[size][size];
        this.visited = new boolean[size];
        this.tour = new int[size];
        this.len = Double.MAX_VALUE;
        this.brun = new int[size];
        this.brlen = Double.MAX_VALUE;
        this.best = new int[size];
        this.bestlen = Double.MAX_VALUE;
        this.dsts = new int[size];
        this.sums = new double[size];
        this.rand = rand;        /* store the random number generator */
        this.antcnt = antcnt;      /* note the number of ants and */
        this.exploit = 0.0;         /* prob. for exploiting best edge */
        this.alpha = 1.0;         /* weighting exponents for */
        this.beta = 1.0;         /* distance versus trail */
        this.evap = 0.1;         /* pheromone evaporation factor */
        this.epoch = 0;           /* initialize the epoch counter */
    }  /* AntColony() */

  /*------------------------------------------------------------------*/

    public AntColony(TSP tsp, int antcnt) {
        this(tsp, antcnt, new Random());
    }

  /*------------------------------------------------------------------*/

    public TSP getTSP() {
        return this.tsp;
    }

  /*------------------------------------------------------------------*/

    public void setExploit(double exploit) {
        this.exploit = exploit;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public void setEvap(double evap) {
        this.evap = evap;
    }

    public void setTrail(double trail) {
        this.elite = elite;
    }

    public void setElite(double elite) {
        this.elite = elite;
    }

  /*------------------------------------------------------------------*/

    public double getDist(int i, int j) {
        return this.dists[i][j];
    }

    public double getTrail(int i, int j) {
        return this.trail[i][j];
    }

    public double getTrailAvg() {
        return this.avg;
    }

    public double getTrailMax() {
        return this.max;
    }

    public int[] getBestTour() {
        return this.best;
    }

    public double getBestLen() {
        return this.bestlen;
    }

    public int getEpoch() {
        return this.epoch;
    }

  /*------------------------------------------------------------------*/

    public void init() {
        this.init(-1);
    }

    public void init(double val) {                             /* --- initialize nearness and trail */
        int i, j;                /* loop variables */
        double sum = 0;             /* sum of edge lengths */

        for (i = this.tour.length; --i >= 0; )
            for (j = this.tour.length; --j >= 0; )
                sum += this.dists[i][j];/* compute the average tour length */
        this.avglen = sum / this.tour.length;
        if (val <= 0) val = 1;      /* check and adapt initial value */
        for (i = this.tour.length; --i >= 0; ) {
            for (j = this.tour.length; --j >= 0; ) {
                this.nears[i][j] = Math.pow(this.dists[i][j], -this.beta);
                this.trail[i][j] = val; /* compute nearness from distance */
            }                         /* and set all trail elements */
        }                           /* to the same value */
        this.max = this.avg = val;  /* set maximal/average trail value */
        this.bestlen = Double.MAX_VALUE;
        this.epoch = 0;            /* init. length of best tour */
    }  /* init() */               /* and the epoch counter */

  /*------------------------------------------------------------------*/

    private static int find(double vec[], int n, double val) {                             /* --- find edge based on random val. */
        int i, k;                   /* left and middle element */

        for (--n, i = 0; i < n; ) { /* do a binary search */
            k = (i + n) >> 1;          /* in the given vector */
            if (vec[k] < val) i = k + 1;
            else n = k;  /* i and n are the boundaries */
        }                           /* of the section still to search */
        return i;                   /* return index i for which it is */
    }  /* find() */               /* vec[i-1] < val <= vec[i] */

  /*------------------------------------------------------------------*/

    private void placePhero(int[] tour, double amount) {                             /* --- place pheromone on ant's tour */
        int i;                  /* loop variable */
        int src, dst;           /* source and destination of an edge */
        boolean sym;                /* whether TSP is symmetric */

        src = this.tour[0];         /* get the start of the tour */
        for (i = tour.length; --i >= 0; ) {
            dst = src;
            src = tour[i]; /* traverse the vertices on the tour */
            this.delta[src][dst] += amount;
        }                           /* place pheromone on the edge */
    }  /* placeTrail() */

  /*------------------------------------------------------------------*/

    public double runAnt() {                             /* --- run one ant of the colony */
        int i, j, n;             /* loop variables, counter */
        int src, dst = 0;        /* source and dest. of next edge */
        double emax;                /* maximal value of an edge */
        double sum;                 /* sum of edge values */
        double chg;                 /* change of trail */

    /* --- initialize variables --- */
        for (i = this.visited.length; --i >= 0; )
            this.visited[i] = false;  /* clear the visited flags */
        this.len = 0;               /* and the tour length */

    /* --- run the ant --- */
        src = this.rand.nextInt(this.tour.length);
        this.tour[0] = src;    /* randomly select the vertex */
        this.visited[src] = true;   /* the ant starts from */
        for (i = 0; ++i < this.tour.length; ) {
            if ((this.exploit > 0)    /* if to exploit best known edge */
                    && (rand.nextDouble() < this.exploit)) {
                emax = -1.0;            /* init. the best edge value */
                for (j = this.tour.length; --j >= 0; ) {
                    if (!this.visited[j]  /* traverse edges to unvisited verts. */
                            && (this.quals[src][j] > emax)) {
                        emax = this.quals[src][j];
                        dst = j;
                    }
                }
            }                     /* find the best edge to follow */ else {                    /* if to choose edge randomly */
                sum = 0;                /* init. the quality sum */
                for (j = n = 0; j < this.tour.length; j++) {
                    if (this.visited[j]) continue;
                    sum += this.quals[src][j];
                    this.sums[n] = sum; /* collect and sum the qualities */
                    this.dsts[n++] = j;   /* of the different edges */
                }                       /* to unvisited destinations */
                j = find(this.sums, n, sum * rand.nextDouble());
                dst = this.dsts[j];     /* choose destination randomly */
            }                         /* based on the edge qualities */
            this.visited[dst] = true; /* mark the destination as visited */
            this.len += this.dists[src][dst];
            this.tour[i] = src = dst; /* sum the edge lengths and */
        }                           /* add the vertex to the tour */
        this.len += this.dists[src][this.tour[0]];

    /* --- place pheromone --- */
        chg = this.avglen / this.len; /* compute amount of pheromone */
        if (this.layexp != 1) chg = Math.pow(chg, this.layexp);
        this.placePhero(this.tour, chg);

        return this.len;            /* return the length of the tour */
    }  /* runAnt() */

  /*------------------------------------------------------------------*/

    public double runAllAnts() {                             /* --- run all ants of the colony */
        int i, j;                /* loop variables */
        double t, min;              /* new/minimal trail value */
        double stick;               /* stick factor for pheromone */

    /* --- initialize the edge qualities --- */
        for (i = this.delta.length; --i >= 0; ) {
            for (j = this.delta.length; --j >= 0; ) {
                this.delta[i][j] = 0;   /* init. the trail change matrix */
                this.quals[i][j] = this.nears[i][j]
                        * ((this.alpha == 1.0) ? this.trail[i][j]
                        : Math.pow(this.trail[i][j], this.alpha));
            }                         /* compute the current qualities */
        }                           /* of the different edges */

    /* --- run the ants --- */
        this.brlen = Double.MAX_VALUE;
        for (i = this.antcnt; --i >= 0; ) {
            this.runAnt();            /* run an ant on the trail */
            if (this.len >= this.brlen) continue;
            System.arraycopy(this.tour, 0, this.brun, 0, this.brun.length);
            this.brlen = this.len;    /* if the new tour is better than */
        }                           /* the currently best, replace it */
        if (this.brlen < this.bestlen) {
            System.arraycopy(this.brun, 0, this.best, 0, this.best.length);
            this.bestlen = this.brlen;/* if the best run tour is better */
        }                           /* than the best, replace it */
        if (this.elite > 0) {       /* strengthen best tour */
            t = this.avglen / this.bestlen;
            if (this.layexp != 1) t = Math.pow(t, this.layexp);
            this.placePhero(this.best, this.elite * this.antcnt * t);
        }                           /* place pheromone on best tour */

    /* --- update trail matrix --- */
        min = this.avg / this.tour.length;
        this.max = this.avg = 0;    /* reinit. the max./avg. trail value */
        stick = 1 - this.evap;    /* and compute stick factor */
        if (this.tsp.isSymmetric()) {/* if symmetric distances */
            for (i = this.trail.length; --i >= 0; ) {
                for (j = i; --j >= 0; ) {
                    t = stick * this.trail[i][j]
                            + this.evap * (this.delta[i][j] + this.delta[j][i]);
                    if (t < min) t = min; /* compute the new trail value */
                    this.trail[i][j] =    /* from both edge directions */
                            this.trail[j][i] = t; /* and store it symmetrically */
                    if (t > this.max) this.max = t;
                    this.avg += t;        /* update the trail matrix, */
                }                       /* find highest trail value, */
            }                         /* and sum the trail values */
            this.avg /= 0.5 * this.tour.length * this.tour.length;
        } else {                      /* if asymmetric distances */
            for (i = this.trail.length; --i >= 0; ) {
                for (j = this.trail.length; --j >= 0; ) {
                    t = stick * this.trail[i][j]
                            + this.evap * this.delta[i][j];
                    if (t < min) t = min; /* compute the new trail value */
                    this.trail[i][j] = t; /* and store it asymmetrically */
                    if (t > this.max) this.max = t;
                    this.avg += t;        /* update the trail matrix, */
                }                       /* find highest trail value, */
            }                         /* and sum the trail values */
            if (this.tour.length > 1) /* compute average trail value */
                this.avg /= this.tour.length * (this.tour.length - 1);
        }

        this.epoch++;               /* count the run */
        return this.bestlen;        /* return length of best tour */
    }  /* runAllAnts() */

}  /* class AntColony */
