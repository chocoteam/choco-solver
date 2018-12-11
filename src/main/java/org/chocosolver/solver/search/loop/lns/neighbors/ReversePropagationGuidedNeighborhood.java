/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    protected void update() throws ContradictionException {
        while (logSum > fgmtSize && fragment.cardinality() > 0) {
            // 1. pick a variable
            int id = selectVariable();

            // 2. freeze it to its solution value
            if (variables[id].contains(values[id])) {  // to deal with objective variable and related

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
                            int closeness = (int) ((dsize[i] - ds) / (dsize[i] * 1.) * 100);
                            //                            System.out.printf("%d -> %d :%d\n", dsize[i], ds, closeness);
                            if (closeness > 0) {
                                all[i] = closeness; // add it to candidate list
                            }
                        }
                    }
                }
                mModel.getEnvironment().worldPop();
                candidates = IntStream.range(0, n)
                        .filter(i -> fragment.get(i) && all[i] > 0)
                        .boxed()
                        .sorted(Comparator.comparingInt(i -> all[i]))
                        .limit(listSize)
                        .collect(Collectors.toList());
                logSum = 0;
                for (int i = fragment.nextSetBit(0); i > -1 && i < n; i = fragment.nextSetBit(i + 1)) {
                    logSum += Math.log(variables[i].getDomainSize());
                }
            } else {
                fragment.clear(id);
            }

        }
        for (int i = fragment.nextSetBit(0); i > -1 && i < n; i = fragment.nextSetBit(i + 1)) {
            if (variables[i].contains(values[i])) {
                freeze(i);
            }
        }
//        mModel.getSolver().propagate();
//        logSum = Arrays.stream(variables).mapToDouble(v -> Math.log(v.getDomainSize())).sum();
    }
}
