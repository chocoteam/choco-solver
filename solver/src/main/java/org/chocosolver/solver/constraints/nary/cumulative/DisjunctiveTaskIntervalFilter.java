/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.sort.IntComparator;

/**
 * @author Jean-Guillaume FAGES
 */
public class DisjunctiveTaskIntervalFilter extends CumulFilter {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************
    private final ArraySort<?> sort;
    private final StartComparator comp;
    private final int[] tsks;
    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public DisjunctiveTaskIntervalFilter(int nbMaxTasks) {
        super(nbMaxTasks);
        sort = new ArraySort<>(nbMaxTasks, false, true);
        comp = new StartComparator();
        tsks = new int[nbMaxTasks];
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks, Propagator<IntVar> aCause) throws ContradictionException {
        // filtering algorithm for disjunctive constraint
        capa.updateUpperBound(1, aCause);
        // remove tasks that do not consume any resource
        int tskSize = 0;
        ISetIterator tIter = tasks.iterator();
        while (tIter.hasNext()) {
            int t = tIter.nextInt();
            if (d[t].getLB() > 0 && h[t].getLB() > 0) {
                tsks[tskSize++] = t;
            }
        }
        comp.setS(s);
        sort.sort(tsks, tskSize, comp);
        // run energetic reasoning
        for (int x = 0; x < tskSize; x++) {
            int task1 = tsks[x];
            for (int y = 0; y < tskSize; y++) {
                if (x != y) {
                    int task2 = tsks[y];
                    int t1 = s[task1].getLB();
                    int t2 = e[task2].getUB();
                    if (e[task1].getLB() > s[task2].getUB()) {
                        s[task1].updateLowerBound(e[task2].getLB(), aCause);
                        e[task2].updateUpperBound(s[task1].getUB(), aCause);
                    } else if (t1 < t2 && (t1 < e[task2].getLB() || t2 > s[task1].getUB())) {
                        int W = 0;
                        for (int task3 : tsks) {
                            if (task3 != task1 && task3 != task2) {
                                if (s[task3].getLB() >= t2) {
                                    break;
                                }
                                int pB = d[task3].getLB() * h[task3].getLB();
                                int pbt1 = Math.max(0, pB - Math.max(0, t1 - s[task3].getLB()));
                                int pbt2 = Math.max(0, pB - Math.max(0, e[task3].getUB() - t2));
                                int pbt = Math.min(pbt1, pbt2);
                                W += Math.min(t2 - t1, pbt);
                            }
                        }
                        if (W + d[task1].getLB() + d[task2].getLB() > t2 - t1) {
                            s[task1].updateLowerBound(e[task2].getLB(), aCause);
                            e[task2].updateUpperBound(s[task1].getUB(), aCause);
                        }
                    }
                }
            }
        }
    }

    private static class StartComparator implements IntComparator {
        IntVar[] s;

        void setS(IntVar[] s) {
            this.s = s;
        }

        @Override
        public int compare(int i1, int i2) {
            return s[i1].getLB() - s[i2].getLB();
        }
    }

}