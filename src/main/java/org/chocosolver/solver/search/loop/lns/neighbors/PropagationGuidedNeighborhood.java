/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
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
public class PropagationGuidedNeighborhood implements INeighbor {


    /**
     * Number of variables
     */
    protected final int n;
    /**
     * Array of variables to consider in a fragment
     */
    protected final IntVar[] vars;
    /**
     * Last solution found, wrt {@link #vars}
     */
    protected final int[] bestSolution;
    /**
     * Domain size of each variable in {@link #vars}
     */
    protected int[] dsize;
    /**
     * For randomness
     */
    protected Random rd;
    /**
     * Intial size of the fragment
     */
    protected int fgmtSize = 100;
    /**
     * Number of variables modified through propagation to consider while computing the neighbor
     */
    protected int listSize;
    /**
     * Restriction parameter
     */
    protected double epsilon = 1.;
    /**
     * Logarithmic cardinality of domains
     */
    protected double logSum = 0.;
    /**
     * Store the modified variables
     */
    protected int[] all;
    /**
     * Store the variable elligible for propagation
     */
    protected List<Integer> candidates;

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
        this.mModel = vars[0].getModel();
        this.n = vars.length;
        this.vars = vars.clone();
        this.rd = new Random(seed);
        this.bestSolution = new int[n];
        this.fgmtSize = fgmtSize;
        this.listSize = listSize;
        this.all = new int[n];
        this.candidates = new ArrayList<>();
        this.fragment = new BitSet(n);
    }

    @Override
    public boolean isSearchComplete() {
        return false;
    }

    @Override
    public void recordSolution() {
        for (int i = 0; i < vars.length; i++) {
            bestSolution[i] = vars[i].getValue();
        }
    }

    @Override
    public void loadFromSolution(Solution solution) {
        for (int i = 0; i < vars.length; i++) {
            bestSolution[i] = solution.getIntVal(vars[i]);
        }
    }

    @Override
    public void fixSomeVariables(DecisionPath decisionPath) {
        logSum = 0.;
        for (int i = 0; i < n; i++) {
            int ds = vars[i].getDomainSize();
            logSum += Math.log(ds);
        }
        fgmtSize = (int) (30 * (1 + epsilon));
        fragment.set(0, n); // all variables are frozen
        mModel.getEnvironment().worldPush();
        try {
            update(decisionPath);
        } catch (ContradictionException cex) {
            mModel.getSolver().getEngine().flush();
        }
        mModel.getEnvironment().worldPop();
        epsilon = (.95 * epsilon) + (.05 * (logSum / fgmtSize));
    }

    /**
     * Create the fragment
     *
     * @param decisionPath the decision path to feed
     * @throws ContradictionException if the fragment is trivially infeasible
     */
    protected void update(DecisionPath decisionPath) throws ContradictionException {
        while (logSum > fgmtSize && fragment.cardinality() > 0) {
            // 1. pick a variable
            int id = selectVariable();
            // 2. fix it to its solution value and propagate
            if (vars[id].contains(bestSolution[id])) {  // to deal with objective variable and related
                impose(id, decisionPath);
                mModel.getSolver().propagate();
                fragment.clear(id);
                logSum = 0;
                // 3. compute domain reductions & update logSum
                for (int i = 0; i < n; i++) {
                    int ds = vars[i].getDomainSize();
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
                logSum -= Math.log(vars[id].getDomainSize());
            }
        }
    }

    /**
     * Impose a decision to be part of the fragment
     *
     * @param id           variable id in {@link #vars}
     * @param decisionPath the current decision path
     * @throws ContradictionException if the application of the decision fails
     */
    protected void impose(int id, DecisionPath decisionPath) throws ContradictionException {
        IntDecision decision = decisionPath.makeIntDecision(vars[id], DecisionOperatorFactory.makeIntEq(), bestSolution[id]);
        decision.setRefutable(false);
        decisionPath.pushDecision(decision);
        vars[id].instantiateTo(bestSolution[id], Cause.Null);
    }


    /**
     * @return a variable id in {@link #vars} to be part of the fragment
     */
    protected int selectVariable() {
        int id;
        if (candidates.isEmpty()) {
            int cc = rd.nextInt(fragment.cardinality());
            for (id = fragment.nextSetBit(0); id >= 0 && cc > 0; id = fragment.nextSetBit(id + 1)) {
                cc--;
            }
        } else {
            id = candidates.remove(0).intValue();
        }
        return id;
    }

    @Override
    public void restrictLess() {
        epsilon += .1 * (logSum / fgmtSize);
    }

    @Override
    public void init() {
        // todo plug search monitor
        this.dsize = new int[n];
        for (int i = 0; i < n; i++) {
            dsize[i] = vars[i].getDomainSize();
        }
    }
}
