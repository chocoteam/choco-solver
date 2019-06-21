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

import org.chocosolver.solver.constraints.nary.cumulative.literature.CumulativeFilter;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.Comparator;
import java.util.LinkedList;


/**
 * Disjunctive constraint filtering algorithms described in the thesis :
 * Vilim, P.: Global constraints in scheduling. Ph.D. thesis, Charles University in Prague (2007). http://vilim.eu/petr/disertace.pdf
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class PropDisjunctive extends CumulativeFilter {
    private ThetaLambdaTree thetaLambdaTree;
    private LinkedList<Integer> Q;

    public PropDisjunctive(Task[] tasks, boolean overloadCheck, boolean notFirst, boolean edgeFinding) {
        super(tasks);
        this.overloadCheck = overloadCheck;
        this.notFirst = notFirst;
        this.edgeFinding = edgeFinding;

        IntVar[] heights = tasks[0].getStart().getModel().intVarArray(tasks.length, 1, 1);
        IntVar capacity = tasks[0].getStart().getModel().intVar(1);
        thetaLambdaTree = new ThetaLambdaTree(tasks, heights, capacity);
        Q = new LinkedList<Integer>();
    }

    @Override
    public void overloadCheck() throws ContradictionException {
        thetaLambdaTree.initializeTree(false);
        indexes.sort(Comparator.comparingInt(i -> tasks[i].getEnd().getUB()));
        for(int i : indexes) {
            thetaLambdaTree.addToTheta(i);
            if(thetaLambdaTree.root.env > tasks[i].getEnd().getUB()) {
                aCause.fails();
            }
        }
    }

    private void fillQ() {
        Q.clear();
        indexes.sort(Comparator.comparingInt(j -> tasks[j].getStart().getUB()));
        for(int i : indexes) {
            Q.addLast(i);
        }
    }

    private int getEnvWithouti(int i) {
        thetaLambdaTree.removeFromTheta(i);
        int ect = thetaLambdaTree.root.env;
        thetaLambdaTree.addToTheta(i);
        return ect;
    }

    @Override
    public boolean notFirst() throws ContradictionException { // it is in fact notLast()
        boolean hasFiltered = false;

        thetaLambdaTree.initializeTree(false);
        fillQ();
        indexes.sort(Comparator.comparingInt(i -> tasks[i].getEnd().getUB()));
        int j = Q.getFirst();
        for(int i : indexes) {
            while(!Q.isEmpty() && tasks[i].getEnd().getUB()>tasks[Q.getFirst()].getStart().getUB()) {
                j = Q.removeFirst();
                thetaLambdaTree.addToTheta(j);
            }
            if(getEnvWithouti(i)>tasks[i].getStart().getUB()) {
                hasFiltered |= tasks[i].getEnd().updateUpperBound(tasks[j].getStart().getUB(), aCause);
            }
        }

        return hasFiltered;
    }

    @Override
    public boolean edgeFinding() throws ContradictionException {
        boolean hasFiltered = false;

        thetaLambdaTree.initializeTree(true);
        Q.clear();
        indexes.sort(Comparator.comparingInt(j -> -tasks[j].getEnd().getUB()));
        for(int i : indexes) {
            Q.addLast(i);
        }
        int j = Q.getFirst();
        while(Q.size() > 1) {
            if(thetaLambdaTree.root.env > tasks[j].getEnd().getUB()) { // Overload Check
                aCause.fails();
            }
            thetaLambdaTree.addToLambdaAndRemoveFromTheta(j);
            Q.removeFirst();
            j = Q.getFirst();
            while(thetaLambdaTree.root.envLambda > tasks[j].getEnd().getUB()) {
                int i = thetaLambdaTree.root.responsibleEnvLambda.taskIdx;
                hasFiltered |= tasks[i].getStart().updateLowerBound(thetaLambdaTree.root.env, aCause);
                thetaLambdaTree.removeFromLambda(i);
            }
        }

        return hasFiltered;
    }
}
