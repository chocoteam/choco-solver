/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.MathUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A Reverse Propagation Guided Large Neighborhood Search (LNS) neighbor.
 * <p>
 * This implementation works in reverse compared to {@link PropagationGuidedNeighborhood}:
 * instead of selecting variables to be part of the fragment (to be frozen), it selects
 * variables to NOT be part of the fragment (to be relaxed). The approach is based on
 * "Propagation Guided Large Neighborhood Search", Perron et al. CP2004.
 * <p>
 * The algorithm maintains a fragment of variables that are frozen to their current values.
 * It iteratively selects variables to remove from the fragment (unfreeze) based on the
 * impact of propagation. Variables that cause the most domain reduction when frozen are
 * prioritized for removal, creating a dynamic neighborhood that adapts based on constraint
 * propagation effects.
 * <p>
 * This strategy can be particularly effective when the constraint propagation provides
 * strong guidance on which variables are most influential in reducing the search space.
 *
 * @author Charles Prud'homme
 * @since 08/04/13
 */
public class ReversePropagationGuidedNeighborhood extends IntNeighbor {

    /**
     * Number of variables in the neighborhood
     */
    protected final int n;
    /**
     * Initial domain size of each variable in {@link #variables},
     * recorded during initialization
     */
    protected int[] domSiz;
    /**
     * Stores the domain reduction percentage for each variable,
     * used to rank variables by their impact on propagation
     */
    protected int[] all;
    /**
     * Random number generator for random variable selection
     */
    protected Random rd;
    /**
     * Desired size of the fragment (target logarithmic sum of domain sizes)
     */
    final double desiredSize;
    /**
     * Current target size of the fragment (adjusted by epsilon)
     */
    double size;
    /**
     * Maximum number of candidate variables to store and consider.
     * Only the top <code>listSize</code> variables with the highest domain reduction impact
     * are kept as candidates for the next selection.
     */
    int listSize;
    /**
     * Current logarithmic sum of domain sizes of frozen variables.
     * Used to track progress toward the desired fragment size.
     */
    double logSum = 0.;
    /**
     * Adaptive restriction parameter that adjusts the fragment size dynamically.
     * It is updated after each call to {@link #fixSomeVariables()} based on the
     * actual log-sum achieved, allowing the algorithm to adapt to the problem structure.
     * A value greater than 1.0 increases the fragment size, while a value less than 1.0 decreases it.
     */
    private double epsilon = 1.;
    /**
     * List of candidate variable indices eligible for selection.
     * Contains variables from the fragment that caused domain reduction when frozen,
     * sorted by their impact (highest first) and limited to {@link #listSize} entries.
     */
    List<Integer> candidates;
    /**
     * BitSet indicating which variables are currently in the fragment (frozen).
     * A bit set to 1 means the variable is frozen to its solution value.
     */
    protected BitSet fragment;
    /**
     * Reference to the model containing the variables and constraints
     */
    protected Model mModel;

    /**
     * Constructs a Reverse Propagation Guided LNS neighbor.
     * <p>
     * This neighbor selects variables to NOT be part of the fragment (i.e., to relax/freeze).
     * The selection is guided by the impact of constraint propagation: variables that cause
     * the most domain reduction when frozen are prioritized.
     *
     * @param vars         the integer variables to consider for the neighborhood
     * @param desiredSize  the desired size of the fragment (number of variables to freeze)
     * @param listSize     the number of modified variables to store and consider while propagating.
     *                     Variables are ranked by their impact (domain reduction caused) and only the
     *                     top <code>listSize</code> are kept as candidates for the next selection.
     * @param seed         the seed for the random number generator used when no candidates are available
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

    /**
     * Creates a fragment by freezing variables based on reverse propagation guidance.
     * Initially, all variables are considered frozen (part of the fragment).
     * The method iteratively removes variables from the fragment until the desired size
     * is reached or a contradiction is detected.
     * <p>
     * The epsilon parameter is adaptively adjusted based on the actual log-sum of domain
     * sizes encountered during the process, allowing the neighborhood size to adapt over time.
     *
     * @throws ContradictionException if fixing variables leads to a contradiction
     */
    @Override
    public void fixSomeVariables() throws ContradictionException {
        logSum = 0;
        size = desiredSize * epsilon;
        fragment.set(0, n); // all variables are frozen
        try {
            update();
            epsilon = (.95 * epsilon) + (.05 * (logSum / size));
        } catch (ContradictionException ce) {
            epsilon = (.95 * epsilon) + (.05 / size);
            throw ce;
        }
    }

    /**
     * Updates the fragment by iteratively selecting and removing variables.
     * For each selected variable, it temporarily freezes the variable to its solution value,
     * propagates constraints, and measures the impact on other variables' domains.
     * Variables that cause significant domain reduction in others are prioritized for removal
     * from the fragment in subsequent iterations.
     * <p>
     * The method stops when either:
     * <ul>
     *   <li>The logarithmic sum of domain sizes reaches or exceeds the target size</li>
     *   <li>No more variables are left in the fragment</li>
     * </ul>
     *
     * @throws ContradictionException if propagating the freezing of a variable leads to a contradiction
     */
    protected void update() throws ContradictionException {
        while (logSum < size && fragment.cardinality() > 0) {
            // 1. pick a variable
            int id = selectVariable();

            // 2. freeze it to its solution value
            if (variables[id].contains(values[id])) {  // to deal with objective variable and related
                logSum += MathUtils.log2(variables[id].getDomainSize());

                mModel.getSolver().pushTrail();
                variables[id].instantiateTo(values[id], Cause.Null);
                try {
                    mModel.getSolver().propagate();
                } catch (ContradictionException ignored) {
                }
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
                mModel.getSolver().cancelTrail();
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
     * Selects the next variable to process from the fragment.
     * If there are candidate variables (those that caused significant domain reduction when
     * frozen), it prioritizes them. Otherwise, it selects a variable randomly from the
     * remaining variables in the fragment.
     *
     * @return the index of the selected variable in {@link #variables}
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

    /**
     * Initializes the neighborhood by recording the initial domain sizes of all variables.
     * This is called once at the beginning of the search to establish baseline domain sizes
     * that are used to measure the impact of freezing variables during the neighborhood exploration.
     */
    @Override
    public void init() {
        this.domSiz = new int[n];
        for (int i = 0; i < n; i++) {
            domSiz[i] = variables[i].getDomainSize();
        }
    }

}
