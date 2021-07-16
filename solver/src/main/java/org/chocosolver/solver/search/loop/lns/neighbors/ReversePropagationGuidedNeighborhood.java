/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.MathUtils;

/**
 * A Propagation Guided LNS
 * <p/>
 * Based on "Propagation Guided Large Neighborhood Search", Perron et al. CP2004.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/04/13
 */
public class ReversePropagationGuidedNeighborhood extends IntNeighbor{

    /**
     * Number of variables
     */
    protected final int n;
    /**
     * Domain size of each variable in {@link #variables}
     */
    protected int[] domSiz;
    /**
     * Store the modified variables
     */
    protected int[] all;
    /**
     * For randomness
     */
    protected Random rd;
    /**
     * Intial size of the fragment
     */
    final double desiredSize;
    /**
     * Goal size of the fragment
     */
    double size;
    /**
     * Number of variables modified through propagation to consider while computing the neighbor
     */
    int listSize;
    /**
     * Logarithmic cardinality of domains
     */
    double logSum = 0.;
    /**
     * Restriction parameter
     */
    private double epsilon = 1.;
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
     * Create a reverse adaptive neighbor for LNS based on PGLNS, which selects variables to not be part of a fragment
     * @param vars variables to consider
     * @param desiredSize desired size of the fragment
     * @param listSize number of modified variable to store while propagating
     * @param seed for randomness
     */
    public ReversePropagationGuidedNeighborhood(IntVar[] vars, int desiredSize, int listSize, long seed) {
        super(vars);
        this.mModel = vars[0].getModel();
        this.n = vars.length;
        this.rd = new Random(seed);
        this.desiredSize = desiredSize;
        this.listSize = listSize;
        this.all = new int[n];
        this.domSiz = new int[n];
        this.candidates = new ArrayList<>();
        this.fragment = new BitSet(n);
    }

    @Override
    public void fixSomeVariables() throws ContradictionException {
        logSum = 0;
        size = desiredSize * epsilon;
        fragment.set(0, n); // all variables are frozen
        try {
            update();
            epsilon = (.95 * epsilon) + (.05 * (logSum / size));
        }catch (ContradictionException ce){
            epsilon = (.95 * epsilon) + (.05 / size);
            throw ce;
        }
    }

    protected void update() throws ContradictionException {
        while (logSum < size && fragment.cardinality() > 0) {
            // 1. pick a variable
            int id = selectVariable();

            // 2. freeze it to its solution value
            if (variables[id].contains(values[id])) {  // to deal with objective variable and related
                logSum += MathUtils.log2(variables[id].getDomainSize());

                mModel.getEnvironment().worldPush();
                variables[id].instantiateTo(values[id], Cause.Null);
                mModel.getSolver().propagate();
                fragment.clear(id);

                for (int i = 0; i < n; i++) {
                    int ds = variables[i].getDomainSize();
                    if (fragment.get(i)) { // if not frozen until now
                        if (ds == 1) { // if fixed by side effect
                            fragment.clear(i); // set it has fixed
                        } else {
                            all[i] = (int) ((domSiz[i] - ds) / (domSiz[i] * 1.) * 100);
                            // we do not deal with previous reductions
                        }
                    }
                }
                mModel.getEnvironment().worldPop();
                candidates = IntStream.range(0, n)
                        .filter(i -> fragment.get(i) && all[i] > 0)
                        .boxed()
                        .sorted(Comparator.comparingInt(i -> -all[i]))
                        .limit(listSize)
                        .collect(Collectors.toList());
            } else {
                fragment.clear(id);
            }

        }
        // Then freeze variables not selected
        for (int i = fragment.nextSetBit(0); i > -1 && i < n; i = fragment.nextSetBit(i + 1)) {
            if (variables[i].contains(values[i])) {
                freeze(i);
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
    public void init() {
        this.domSiz = new int[n];
        for (int i = 0; i < n; i++) {
            domSiz[i] = variables[i].getDomainSize();
        }
    }

}
