/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A Propagation Guided LNS
 * <p>
 * Based on "Propagation Guided Large Neighborhood Search", Perron et al. CP2004.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/04/13
 */
public class PropagationGuidedNeighborhood extends Neighbor {


    /**
     * Number of variables
     */
    protected final int n;
    /**
     * Domain size of each variable in {@link #variables}
     */
    protected int[] dsize;
    /**
     * For randomness
     */
    protected Random rd;
    /**
     * Intial size of the fragment
     */
    int fgmtSize = 100;
    /**
     * Number of variables modified through propagation to consider while computing the neighbor
     */
    int listSize;
    /**
     * Restriction parameter
     */
    private double epsilon = 1.;
    /**
     * Logarithmic cardinality of domains
     */
    double logSum = 0.;
    /**
     * Store the modified variables
     */
    protected int[] all;
    /**
     * Store the variable elligible for propagation
     */
    List<Integer> candidates;

    /**
     * Indicate which variables are selected in a fragment
     */
    protected BitSet fragment;
    /**
     * Reference to the model
     */
    protected Model mModel;

    /**
     * Create a propagation-guided neighbor for LNS
     *
     * @param vars     set of variables to consider
     * @param fgmtSize initial size of the fragment
     * @param listSize number of modified variable to store while propagating
     * @param seed     for randomness
     */
    public PropagationGuidedNeighborhood(IntVar[] vars, int fgmtSize, int listSize, long seed) {
        super(vars);
        this.mModel = vars[0].getModel();
        this.n = vars.length;
        this.rd = new Random(seed);
        this.fgmtSize = fgmtSize;
        this.listSize = listSize;
        this.all = new int[n];
        this.candidates = new ArrayList<>();
        this.fragment = new BitSet(n);
    }

    @Override
    public void fixSomeVariables() throws ContradictionException {
        logSum = Arrays.stream(variables).mapToDouble(v -> Math.log(v.getDomainSize())).sum();
        for (int i = 0; i < n; i++) {
            int ds = variables[i].getDomainSize();
            logSum += Math.log(ds);
        }
        fgmtSize = (int) (30 * (1 + epsilon));
        fragment.set(0, n); // all variables are frozen
        try {
            update();
        }finally {
            epsilon = (.95 * epsilon) + (.05 * (logSum / fgmtSize));
        }
    }

    /**
     * Create the fragment
     *
     * @throws ContradictionException if the fragment is trivially infeasible
     */
    protected void update() throws ContradictionException {
        while (logSum > fgmtSize && fragment.cardinality() > 0) {
            // 1. pick a variable
            int id = selectVariable();
            // 2. freeze it to its solution value and propagate
            if (variables[id].contains(values[id])) {  // to deal with objective variable and related
                freeze(id);
                mModel.getSolver().propagate();
                fragment.clear(id);
                logSum = 0.;
                // 3. compute domain reductions & update logSum
                for (int i = 0; i < n; i++) {
                    int ds = variables[i].getDomainSize();
                    logSum += Math.log(ds);
                    if (fragment.get(i)) { // if not frozen until now
                        if (ds == 1) {       // if fixed by side effect
                            fragment.clear(i); // set it has fixed
                        } else if (dsize[i] - ds > 0) {
                            all[i] = dsize[i] - ds; // add it to candidate list
                        }
                    }
                }
                // 4. update variable list
                candidates = IntStream.range(0, n)
                        .filter(i -> fragment.get(i) && all[i] > 0)
                        .boxed()
                        .sorted(Comparator.comparingInt(i -> all[i]))
                        .limit(listSize)
                        .collect(Collectors.toList());
            } else {
                fragment.clear(id);
                logSum -= Math.log(variables[id].getDomainSize());
            }
        }
    }

    /**
     * @return a variable id in {@link #variables} to be part of the fragment
     */
    int selectVariable() {
        int id;
        if (candidates.isEmpty()) {
            int cc = rd.nextInt(fragment.cardinality());
            for (id = fragment.nextSetBit(0); id >= 0 && cc > 0; id = fragment.nextSetBit(id + 1)) {
                cc--;
            }
        } else {
            id = candidates.remove(0);
        }
        return id;
    }

    @Override
    public void restrictLess() {
        epsilon += .1 * (logSum / fgmtSize);
    }

    @Override
    public void init() {
        this.dsize = new int[n];
        for (int i = 0; i < n; i++) {
            dsize[i] = variables[i].getDomainSize();
        }
    }
}
