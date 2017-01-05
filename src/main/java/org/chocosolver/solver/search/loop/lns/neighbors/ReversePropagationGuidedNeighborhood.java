/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.variables.IntVar;

/**
 * A Propagation Guided LNS
 * <p/>
 * Based on "Propagation Guided Large Neighborhood Search", Perron et al. CP2004.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/04/13
 */
public class ReversePropagationGuidedNeighborhood extends PropagationGuidedNeighborhood {

    /**
     * Create a neighbor for LNS based on PGLNS, which selects variables to not be part of a fragment
     * @param vars variables to consider
     * @param fgmtSize initial size of the fragment
     * @param listSize number of modified variable to store while propagating
     * @param seed for randomness
     */
    public ReversePropagationGuidedNeighborhood(IntVar[] vars, int fgmtSize, int listSize, long seed) {
        super(vars, fgmtSize, listSize, seed);
    }

    @Override
    protected void update(DecisionPath decisionPath) throws ContradictionException {
        while (logSum > fgmtSize && fragment.cardinality() > 0) {
            all.clear();
            // 1. pick a variable
            int id = selectVariable();

            // 2. fix it to its solution value
            if (vars[id].contains(bestSolution[id])) {  // to deal with objective variable and related

                mModel.getEnvironment().worldPush();
                vars[id].instantiateTo(bestSolution[id], Cause.Null);
                mModel.getSolver().propagate();
                fragment.clear(id);

                for (int i = 0; i < n; i++) {
                    int ds = vars[i].getDomainSize();
                    if (fragment.get(i)) { // if not frozen until now
                        if (ds == 1) { // if fixed by side effect
                            fragment.clear(i); // set it has fixed
                        } else {
                            int closeness = (int) ((dsize[i] - ds) / (dsize[i] * 1.) * 100);
                            //                            System.out.printf("%d -> %d :%d\n", dsize[i], ds, closeness);
                            if (closeness > 0) {
                                all.put(i, Integer.MAX_VALUE - closeness); // add it to candidate list
                            }
                        }
                    }
                }
                mModel.getEnvironment().worldPop();
                candidate.clear();
                int k = 1;
                while (!all.isEmpty() && candidate.size() < listSize) {
                    int first = all.firstKey();
                    all.remove(first);
                    if (fragment.get(first)) {
                        candidate.put(first, k++);
                    }
                }
                logSum = 0;
                for (int i = fragment.nextSetBit(0); i > -1 && i < n; i = fragment.nextSetBit(i + 1)) {
                    logSum += Math.log(vars[i].getDomainSize());
                }
            } else {
                fragment.clear(id);
            }

        }
        for (int i = fragment.nextSetBit(0); i > -1 && i < n; i = fragment.nextSetBit(i + 1)) {
            if (vars[i].contains(bestSolution[i])) {
                impose(i, decisionPath);
            }
        }
        mModel.getSolver().propagate();

        logSum = 0;
        for (int i = 0; i < n; i++) {
            logSum += Math.log(vars[i].getDomainSize());
        }
    }
}
