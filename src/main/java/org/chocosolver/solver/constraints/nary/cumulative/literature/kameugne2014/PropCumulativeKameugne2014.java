/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.kameugne2014;

import org.chocosolver.solver.constraints.nary.cumulative.literature.CumulativeFilter;
import org.chocosolver.solver.constraints.nary.cumulative.literature.vilim2009.ThetaLambdaTree;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.Comparator;

/**
 * Cumulative constraint filtering algorithms described in the following paper :
 * Kameugne, R., Fotso, L.P.: A cumulative not-first/not-last filtering algorithm in O(n2 log(n)). Indian Journal of Pure and Applied Mathematics 44(1), pages 95-115 (2013). https://doi.org/10.1007/s13226-013-0005-z
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class PropCumulativeKameugne2014 extends CumulativeFilter {
    private ThetaLambdaTree tree;

    public PropCumulativeKameugne2014(Task[] tasks, IntVar[] heights, IntVar capacity, boolean notFirst) {
        super(tasks, heights, capacity);
        this.notFirst = notFirst;

        tree = new ThetaLambdaTree(tasks, heights, capacity);
    }

    @Override
    public boolean notFirst() throws ContradictionException {
        boolean hasFiltered = false;
        indexes.sort(Comparator.comparingInt(i -> tasks[i].getEnd().getUB()));
        for(int i : indexes) {
            if(!tasks[i].getStart().isInstantiated()) {
                int minEct = Integer.MAX_VALUE;
                tree.setC(heights[i].getLB());
                tree.initializeTree(false);
                for(int j : indexes) {
                    if(i!=j && tasks[i].getStart().getLB()<tasks[j].getEnd().getLB()) {
                        tree.addToTheta(j);
                        minEct = Math.min(minEct, tasks[j].getEnd().getLB());
                        int lctj = tasks[j].getEnd().getUB();
                        if(tree.envC()>capacity.getUB()*lctj-heights[i].getLB()*Math.min(tasks[i].getEnd().getLB(), lctj)) {
                            hasFiltered |= tasks[i].getStart().updateLowerBound(minEct, aCause);
                            break;
                        }
                    }
                }
            }
        }
        return hasFiltered;
    }

}
