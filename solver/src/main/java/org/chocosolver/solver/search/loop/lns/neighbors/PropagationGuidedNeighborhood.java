/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.MathUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A Propagation Guided Large Neighborhood Search (LNS) neighbor.
 * <p>
 * Based on "Propagation Guided Large Neighborhood Search", Perron et al. CP2004.
 * <p>
 * This implementation selects variables to be part of the fragment (to be frozen) based on
 * the impact of constraint propagation. The algorithm maintains a fragment of variables
 * that are frozen to their current values. It iteratively selects variables to add to the
 * fragment, prioritizing those that cause the most domain reduction when frozen.
 * <p>
 * Variables that cause significant domain reduction in other variables through propagation
 * are considered most influential and are prioritized for inclusion in the fragment.
 * This creates a dynamic neighborhood that adapts based on constraint propagation effects.
 * <p>
 * This strategy is particularly effective when the constraint propagation provides
 * strong guidance on which variables are most influential in reducing the search space.
 * For a reverse approach, see {@link ReversePropagationGuidedNeighborhood}.
 *
 * @author Charles Prud'homme
 * @since 08/04/13
 */
public class PropagationGuidedNeighborhood extends IntNeighbor {


    /**
     * Number of variables in the neighborhood
     */
    protected final int n;
    /**
     * Current domain size of each variable in {@link #variables}
     */
    protected int[] curDoms;
    /**
     * Domain size of each variable in {@link #variables} before the current propagation step.
     * Used to compute the domain reduction caused by freezing a variable.
     */
    protected int[] befDoms;
    /**
     * Stores the domain reduction (in absolute values) for each variable,
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
     * Current size of the fragment.
     * This is dynamically adjusted by {@link #restrictLess()} to increase the neighborhood size
     * over time (size *= 1.01 each call).
     */
    double size;
    /**
     * Maximum number of candidate variables to store and consider.
     * Only the top <code>listSize</code> variables with the highest domain reduction impact
     * are kept as candidates for the next selection.
     */
    int listSize;
    /**
     * Current logarithmic sum of domain sizes of variables in the fragment.
     * Used to track progress toward the desired fragment size.
     * The loop in {@link #update()} continues while logSum > size.
     */
    double logSum = 0.;
    /**
     * List of candidate variable indices eligible for selection.
     * Contains variables from the fragment that caused domain reduction when frozen,
     * sorted by their impact (highest first) and limited to {@link #listSize} entries.
     */
    List<Integer> candidates;
    /**
     * BitSet indicating which variables are currently in the fragment (to be frozen).
     * A bit set to 1 means the variable is in the fragment and will be frozen.
     */
    protected BitSet fragment;
    /**
     * Reference to the model containing the variables and constraints
     */
    protected Model mModel;

    /**
     * Constructs a Propagation Guided LNS neighbor.
     * <p>
     * This neighbor selects variables to be part of the fragment (to be frozen) based on
     * the impact of constraint propagation. Variables that cause the most domain reduction
     * when frozen are prioritized.
     *
     * @param vars         the integer variables to consider for the neighborhood
     * @param desiredSize  the desired size of the fragment (logarithmic sum of domain sizes).
     *                     Note: this is a double value representing a target sum, not a count of variables.
     * @param listSize     the number of modified variables to store and consider while propagating.
     *                     Variables are ranked by their impact (domain reduction caused) and only the
     *                     top <code>listSize</code> are kept as candidates for the next selection.
     * @param seed         the seed for the random number generator used when no candidates are available
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

    /**
     * Creates a fragment by freezing variables based on propagation guidance.
     * Initially computes the logarithmic sum of all variable domain sizes and copies
     * current domain sizes to {@link #befDoms}. All variables start in the fragment.
     * Then calls {@link #update()} to iteratively select and freeze variables.
     *
     * @throws ContradictionException if the fragment is trivially infeasible
     */
    @Override
    public void fixSomeVariables() throws ContradictionException {
        logSum = Arrays.stream(variables).mapToDouble(v -> MathUtils.log2(v.getDomainSize())).sum();
        System.arraycopy(curDoms, 0, befDoms, 0, curDoms.length);
        fragment.set(0, n); // all variables are frozen
        update();
    }

    /**
     * Creates the fragment by iteratively selecting and freezing variables.
     * For each selected variable, it freezes the variable to its solution value,
     * propagates constraints, and measures the impact on other variables' domains.
     * Variables that cause significant domain reduction in others are prioritized for
     * inclusion in the fragment.
     * <p>
     * The method stops when either:
     * <ul>
     *   <li>The logarithmic sum of domain sizes falls below the target size</li>
     *   <li>No more variables are left in the fragment</li>
     * </ul>
     *
     * @throws ContradictionException if propagating the freezing of a variable leads to a contradiction
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
                        .sorted(Comparator.comparingInt(i -> -all[i]))
                        .limit(listSize)
                        .collect(Collectors.toList());
            } else {
                fragment.clear(id);
                logSum -= Math.log(variables[id].getDomainSize());
            }
        }
    }

    /**
     * Selects the next variable to process from the fragment.
     * If there are candidate variables (those that caused significant domain reduction when
     * frozen), it prioritizes them (selecting from the head of the list).
     * Otherwise, it selects a variable randomly from the remaining variables in the fragment.
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
     * Loads the neighborhood state from a solution.
     * Resets the current size to the desired size.
     *
     * @param solution the solution to load from
     */
    @Override
    public void loadFromSolution(Solution solution) {
        super.loadFromSolution(solution);
        size = desiredSize;
    }

    /**
     * Records the current solution.
     * Resets the current size to the desired size after recording.
     */
    @Override
    public void recordSolution() {
        super.recordSolution();
        size = desiredSize;
    }

    /**
     * Restricts the neighborhood less by increasing the fragment size.
     * Multiplies the current size by 1.01, allowing the neighborhood to grow
     * over time and explore larger fragments.
     */
    @Override
    public void restrictLess() {
        size *= 1.01;
    }

    /**
     * Initializes the neighborhood by recording the initial domain sizes of all variables.
     * This is called once at the beginning of the search to establish baseline domain sizes
     * in {@link #curDoms} and {@link #befDoms} that are used to measure the impact of
     * freezing variables during the neighborhood exploration.
     */
    @Override
    public void init() {
        this.curDoms = new int[n];
        this.befDoms = new int[n];
        for (int i = 0; i < n; i++) {
            curDoms[i] = variables[i].getDomainSize();
        }
    }
}
