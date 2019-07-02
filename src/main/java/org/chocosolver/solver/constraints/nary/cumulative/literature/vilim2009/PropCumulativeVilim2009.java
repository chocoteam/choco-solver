/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.vilim2009;

import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.constraints.nary.cumulative.literature.CumulativeFilter;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Cumulative constraint filtering algorithms described in the following paper :
 * Vilim, P.: Edge finding filtering algorithm for discrete cumulative resources in O(k n log(n)). In: Proceedings of the 15th International Conference on Principles and Practice of Constraint Programming (CP 2009), pp. 802-816 (2009). https://doi.org/10.1007/978-3-642-04244-7_62
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class PropCumulativeVilim2009 extends CumulativeFilter {
    private ThetaLambdaTree thetaLambdaTree;

    private int[] prec;
    private int[][] updateJC;

    private TIntArrayList Cs;
    private TIntIntHashMap mapHeightsToCsIdx;

    public PropCumulativeVilim2009(Task[] tasks, IntVar[] heights, IntVar capacity, boolean edgeFinding) {
        super(tasks, heights, capacity);
        this.edgeFinding = edgeFinding;

        thetaLambdaTree = new ThetaLambdaTree(tasks, heights, capacity);
        prec = new int[tasks.length];

        Cs = new TIntArrayList(heights.length);
        updateJC = new int[tasks.length][];
    }

    @Override
    public boolean edgeFinding() throws ContradictionException {
        boolean hasFiltered = false;
        buildMapCs();
        detection();
        computeAllUpdateJC();
        for(int i = 0; i<prec.length; i++) {
            if(tasks[i].getStart().isInstantiated()) {
                continue;
            }
            hasFiltered |= tasks[i].getStart().updateLowerBound(prec[i]-tasks[i].getDuration().getLB(), aCause);
            int j = 0;
            while(j<tasks.length && prec[i] != tasks[j].getEnd().getUB()) {
                j++;
            }
            if(j != tasks.length) {
                hasFiltered |= tasks[i].getStart().updateLowerBound(updateJC[j][mapHeightsToCsIdx.get(i)], aCause);
            }
        }
        return hasFiltered;
    }

    private void buildMapCs() {
        Cs.clear();
        for(int k = 0; k<heights.length; k++) {
            int c = heights[k].getLB();
            if(!Cs.contains(c)) {
                Cs.add(c);
            }
        }
        mapHeightsToCsIdx = new TIntIntHashMap(heights.length);
        for(int i = 0; i<heights.length; i++) {
            for(int j = 0; j< Cs.size(); j++) {
                if(heights[i].getLB() == Cs.getQuick(j)) {
                    mapHeightsToCsIdx.put(i, j);
                }
            }
        }

        for(int j = 0; j<tasks.length; j++) {
            updateJC[j] = new int[Cs.size()];
        }
    }

    private void detection() throws ContradictionException {
        for(int i = 0; i<prec.length; i++) {
            prec[i] = tasks[i].getEnd().getLB();
        }
        thetaLambdaTree.initializeTree(true);
        Arrays.sort(indexes, Comparator.comparingInt(j -> -tasks[j].getEnd().getUB()));
        for(int j : indexes) {
            if(thetaLambdaTree.root.env > capacity.getUB()*tasks[j].getEnd().getUB()) {
                aCause.fails();
            }
            while(thetaLambdaTree.envThetaLambda()>capacity.getUB()*tasks[j].getEnd().getUB()) {
                int i = thetaLambdaTree.getResponsible();
                prec[i] = Math.max(prec[i], tasks[j].getEnd().getUB());
                thetaLambdaTree.removeFromLambda(i);
            }
            thetaLambdaTree.addToLambdaAndRemoveFromTheta(j);
        }
    }

    private void computeAllUpdateJC() {
        Arrays.sort(indexes, Comparator.comparingInt(j -> tasks[j].getEnd().getUB()));
        for(int k = 0; k<Cs.size(); k++) {
            int c = Cs.getQuick(k);
            thetaLambdaTree.setC(c);
            thetaLambdaTree.initializeTree(false);
            int upd = ThetaLambdaTree.MINF;
            for(int j : indexes) {
                thetaLambdaTree.addToTheta(j);
                int envJC = thetaLambdaTree.computeEnvJC(j, c);
                int diff = ThetaLambdaTree.MINF;
                if(envJC != ThetaLambdaTree.MINF) {
                    diff = (int) (Math.ceil(1.0*(envJC-(capacity.getUB()-c)*tasks[j].getEnd().getUB())/c));
                }
                upd = Math.max(upd, diff);
                updateJC[j][k] = upd;
            }
        }
    }
}
