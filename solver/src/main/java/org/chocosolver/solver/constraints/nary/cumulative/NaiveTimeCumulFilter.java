/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * Time-based filtering (compute the profile over every point in time)
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 */
public class NaiveTimeCumulFilter extends CumulFilter {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int[] time = new int[31];

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public NaiveTimeCumulFilter(int nbMaxTasks) {
        super(nbMaxTasks);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************


    @Override
    public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks, Propagator<IntVar> aCause) throws ContradictionException {
        int min = Integer.MAX_VALUE / 2;
        int max = Integer.MIN_VALUE / 2;
        ISetIterator tIter = tasks.iterator();
        while (tIter.hasNext()) {
            int i = tIter.nextInt();
            if (s[i].getUB() < s[i].getLB() + d[i].getValue()) {
                min = Math.min(min, s[i].getUB());
                max = Math.max(max, s[i].getLB() + d[i].getValue());
            }
        }
        if (min < max) {
            if (max - min > time.length) {
                time = new int[max - min];
            } else {
                Arrays.fill(time, 0, max - min, 0);
            }
            int capaMax = capa.getValue();
            // fill mandatory parts and filter capacity
            int elb, hlb;

            tIter = tasks.iterator();
            while (tIter.hasNext()) {
                int i = tIter.nextInt();
                elb = s[i].getLB() + d[i].getValue();
                hlb = h[i].getValue();
                for (int t = s[i].getUB(); t < elb; t++) {
                    time[t - min] += hlb;
                }
            }
            for (int i : tasks) {
                if (d[i].getValue() > 0 && h[i].getValue() > 0) {
                    if (s[i].getLB() + d[i].getValue() > min) {
                        filterInf(s[i], d[i].getValue(), h[i].getValue(), min, max, time, capaMax, aCause);
                    }
                    if (s[i].getUB() < max) {
                        filterSup(s[i], d[i].getValue(), h[i].getValue(), min, max, time, capaMax, aCause);
                    }
                }
            }
            OptionalInt tmax = IntStream.range(0, max - min)
                    .filter(k -> time[k] > capaMax).findAny();
            if (tmax.isPresent()) {
                int t = tmax.getAsInt() + min;
                // should fail
                int[] _tasks = IntStream.range(0, s.length)
                        .filter(i ->
                                d[i].getValue() > 0
                                        && h[i].getValue() > 0
                                        && s[i].getUB() <= t
                                        && t < s[i].getLB() + d[i].getValue())
                        .toArray();
                if (_tasks.length > 0) {
                    s[_tasks[0]].updateUpperBound(t - d[_tasks[0]].getValue(), aCause);
                } else {
                    throw new IllegalStateException();
                }
            }
        }
    }

    private void filterInf(IntVar start, int dlb, int hlb, int min, int max, int[] time, int capaMax, Propagator<IntVar> aCause) throws ContradictionException {
        int nbOk = 0;
        int sub = start.getUB();
        for (int t = start.getLB(); t < sub; t++) {
            if (t < min || t >= max || hlb + time[t - min] <= capaMax) {
                nbOk++;
                if (nbOk == dlb) {
                    return;
                }
            } else {
                nbOk = 0;
                start.updateLowerBound(t + 1, aCause);
            }
        }
    }

    private void filterSup(IntVar start, int dlb, int hlb, int min, int max, int[] time, int capaMax, Propagator<IntVar> aCause) throws ContradictionException {
        int nbOk = 0;
        int elb = start.getLB() + dlb;
        for (int t = start.getUB() + dlb; t > elb; t--) {
            if (t - 1 < min || t - 1 >= max || hlb + time[t - min - 1] <= capaMax) {
                nbOk++;
                if (nbOk == dlb) {
                    return;
                }
            } else {
                nbOk = 0;
                start.updateUpperBound(t - 1 - dlb, aCause);
            }
        }
    }
}
