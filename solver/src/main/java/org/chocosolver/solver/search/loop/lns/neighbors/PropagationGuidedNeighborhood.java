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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
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
import org.chocosolver.util.tools.MathUtils;

/**
 * A Propagation Guided LNS
 * <p>
 * Based on "Propagation Guided Large Neighborhood Search", Perron et al. CP2004.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/04/13
 */
public class PropagationGuidedNeighborhood extends IntNeighbor {


    /**
     * Number of variables
     */
    protected final int n;
    /**
     * Domain size of each variable in {@link #variables}
     */
    protected int[] curDoms;
    /**
     * Domain size of each variable in {@link #variables} before propagation
     */
    protected int[] befDoms;
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
     * Current size of the fragment
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
     * @param desiredSize desired size of the fragment
     * @param listSize number of modified variable to store while propagating
     * @param seed     for randomness
     */
    public PropagationGuidedNeighborhood(IntVar[] vars, double desiredSize, int listSize, long seed) {
        super(vars);
        this.mModel = vars[0].getModel();
        this.n = vars.length;
        this.rd = new Random(seed);
        this.desiredSize = desiredSize;
        this.listSize = listSize;
        this.all = new int[n];
        this.candidates = new ArrayList<>();
        this.fragment = new BitSet(n);
    }

    @Override
    public void fixSomeVariables() throws ContradictionException {
        logSum = Arrays.stream(variables).mapToDouble(v -> MathUtils.log2(v.getDomainSize())).sum();
        System.arraycopy(curDoms, 0, befDoms, 0, curDoms.length);
        fragment.set(0, n); // all variables are frozen
        update();
    }

    /**
     * Create the fragment
     *
     * @throws ContradictionException if the fragment is trivially infeasible
     */
    protected void update() throws ContradictionException {
        while (logSum > size && fragment.cardinality() > 0) {
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
                        } else if (curDoms[i] - ds > 0) {
                            all[i] = befDoms[i] - ds; // add it to candidate list
                            befDoms[i] = ds;
                        }
                    }
                }
                // 4. update variable list
                candidates = IntStream.range(0, n)
                        .filter(i -> fragment.get(i) && all[i] > 0)
                        .boxed()
                        .sorted(Comparator.comparingInt(i -> -all[(int)i]))
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
    public void loadFromSolution(Solution solution) {
        super.loadFromSolution(solution);
        size = desiredSize;
    }

    @Override
    public void recordSolution() {
        super.recordSolution();
        size = desiredSize;
    }

    @Override
    public void restrictLess() {
        size *= 1.01;
    }

    @Override
    public void init() {
        this.curDoms = new int[n];
        this.befDoms = new int[n];
        for (int i = 0; i < n; i++) {
            curDoms[i] = variables[i].getDomainSize();
        }
    }
}
