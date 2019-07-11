/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.gingras2016;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.nary.cumulative.literature.CumulativeFilter;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.LinkedList;

/**
 * Cumulative constraint filtering algorithms described in the following paper :
 * Gingras, V., Quimper, C.-G.: Generalizing the edge-finder rule for the cumulative constraint. In: Proceedings of the 25th International Joint Conference on Artificial Intelligence (IJCAI 2016), pp. 3103–3109 (2016). https://www.ijcai.org/Proceedings/16/Papers/440.pdf
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class PropCumulativeGingras2016 extends CumulativeFilter {
    private Profile profile;
    private TIntArrayList theta;

    // used for overloadCheck(), detection(), detectPrecedences(...), adjustment(...) and computeBound(...) methods
    private TIntArrayList omega;
    private TIntArrayList lambda;
    private TIntArrayList lambdaH;
    private TIntArrayList lcts;
    private TIntArrayList lctj;
    LinkedList<Prec> prec;

    public PropCumulativeGingras2016(Task[] tasks, IntVar[] heights, IntVar capacity, boolean overloadCheck, boolean edgeFinding) {
        super(tasks, heights, capacity);
        this.overloadCheck = overloadCheck;
        this.edgeFinding = edgeFinding;

        profile = new Profile(tasks, heights, capacity);
        theta = new TIntArrayList(tasks.length);
        omega = new TIntArrayList();
        theta = new TIntArrayList();
        lambda = new TIntArrayList();
        lambdaH = new TIntArrayList();
        lcts = new TIntArrayList(tasks.length);
        lctj = new TIntArrayList(tasks.length);
        prec = new LinkedList<>();
    }

    @Override
    public void overloadCheck() throws ContradictionException {
        profile.initialize();
        theta.clear();
        arraySort.sort(indexes, indexes.length, (i1, i2) -> Integer.compare(tasks[i1].getEnd().getUB(), tasks[i2].getEnd().getUB()));
        for(int i : indexes) {
            theta.add(i);
            int[] ectOv = profile.scheduleTasks(theta, capacity.getUB()); // [ect, ov]
            if(ectOv[0]>tasks[i].getEnd().getUB() || ectOv[1]>0) {
                throw new ContradictionException();
            }
        }
    }

    @Override
    public boolean edgeFinding() throws ContradictionException {
        profile.initialize();
        detection();
        return adjustment();
    }

    //***********************************************************************************
    // USEFUL METHODS AND ALGORITHMS
    //***********************************************************************************

    class Prec {
        final int i;
        final TIntArrayList theta;

        Prec(int i, TIntArrayList theta) {
            this.i = i;
            this.theta = theta;
        }

        @Override
        public String toString() {
            return theta.toString()+" <. "+i;
        }
    }

    private void fillLctj(int t) {
        lctj.clear();
        for(int j = 0; j<tasks.length; j++) {
            if(tasks[j].getEnd().getUB() == t) {
                lctj.add(j);
            }
        }
    }

    private void fillLambdaH(int h) {
        lambdaH.clear();
        for(int i = 0; i<lambda.size(); i++) {
            if(heights[lambda.getQuick(i)].getLB() == h) {
                lambdaH.add(lambda.getQuick(i));
            }
        }
    }

    private void fillPrec(TIntArrayList omega, int ectTheta, int lctTheta) {
        for(int im = 0; im<omega.size(); im++) {
            int j = omega.getQuick(im);
            if(Math.max(tasks[j].getEnd().getLB(), ectTheta)>lctTheta) {
                prec.add(new Prec(j, new TIntArrayList(theta)));
            }
        }
    }

    private int computeLct(TIntArrayList list) {
        int lct = -Integer.MAX_VALUE;
        for(int k = 0; k<list.size(); k++) {
            lct = Math.max(lct, tasks[list.getQuick(k)].getEnd().getUB());
        }
        return lct;
    }

    private int computeEct(TIntArrayList list) {
        int lct = -Integer.MAX_VALUE;
        for(int k = 0; k<list.size(); k++) {
            lct = Math.max(lct, tasks[list.getQuick(k)].getEnd().getLB());
        }
        return lct;
    }

    private int initDetect() {
        prec.clear();
        theta.clear();
        lambda.clear();
        int minLct = Integer.MAX_VALUE;
        lcts.clear();
        for(int i = 0; i<tasks.length; i++) {
            theta.add(i);
            int lct = tasks[i].getEnd().getUB();
            minLct = Math.min(minLct, lct);
            lcts.add(lct);
        }
        lcts.sort();
        return minLct;
    }

    void detection() throws ContradictionException {
        int minLct = initDetect();
        int size = lcts.size();
        for(int k = 0; k<size; k++) {
            int t = lcts.getQuick(size-1-k);
            if(t != minLct) {
                fillLctj(t);
                theta.removeAll(lctj);
                lambda.addAll(lctj);
                int[] ectOv = profile.scheduleTasks(theta, capacity.getUB());
                if(ectOv[1]>0) {
                    throw new ContradictionException();
                }
                int lctTheta = computeLct(theta);
                int ectTheta = computeEct(theta);
                for(int iL = 0; iL<lambda.size(); iL++) {
                    int h = heights[lambda.getQuick(iL)].getLB();
                    fillLambdaH(h);
                    omega = profile.detectPrecedences(omega, theta, lambdaH, h, lctTheta);
                    fillPrec(omega, ectTheta, lctTheta);
                    lambda.removeAll(omega);
                }
            }
        }
    }

    private boolean adjustment() throws ContradictionException {
        boolean hasFiltered = false;
        for(Prec p : prec) {
            int[] ectOv = profile.scheduleTasks(p.theta, capacity.getUB()-heights[p.i].getLB());
            hasFiltered |= tasks[p.i].getStart().updateLowerBound(profile.computeBound(p.i, p.theta, ectOv[1]), aCause);
        }
        return hasFiltered;
    }
}
