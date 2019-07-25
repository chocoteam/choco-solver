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

import gnu.trove.list.linked.TIntLinkedList;
import org.chocosolver.solver.constraints.nary.cumulative.literature.DisjunctiveFilter;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;


/**
 * Disjunctive constraint filtering algorithms described in the thesis :
 * Vilim, P.: Global constraints in scheduling. Ph.D. thesis, Charles University in Prague (2007). http://vilim.eu/petr/disertace.pdf
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
public class PropDisjunctiveVilim2009 extends DisjunctiveFilter {
    private ThetaLambdaTree thetaLambdaTree;
    private TIntLinkedList Q;

    public PropDisjunctiveVilim2009(Task[] tasks, boolean overloadCheck, boolean notFirst, boolean edgeFinding) {
        super(tasks);
        this.overloadCheck = overloadCheck;
        this.notFirst = notFirst;
        this.edgeFinding = edgeFinding;

        IntVar[] heights = tasks[0].getStart().getModel().intVarArray(tasks.length, 1, 1);
        IntVar capacity = tasks[0].getStart().getModel().intVar(1);
        thetaLambdaTree = new ThetaLambdaTree(tasks, heights, capacity);
        Q = new TIntLinkedList();
    }

    @Override
    public void overloadCheck() throws ContradictionException {
        thetaLambdaTree.initializeTree(false);
        arraySort.sort(indexes, indexes.length, (i1, i2) -> Integer.compare(tasks[i1].getEnd().getUB(), tasks[i2].getEnd().getUB()));
        for(int i : indexes) {
            thetaLambdaTree.addToTheta(i);
            if(thetaLambdaTree.root.env > tasks[i].getEnd().getUB()) {
                aCause.fails();
            }
        }
    }

    private void fillQ() {
        Q.clear();
        arraySort.sort(indexes, indexes.length, (i1, i2) -> Integer.compare(tasks[i1].getStart().getUB(), tasks[i2].getStart().getUB()));
        for(int i : indexes) {
            Q.add(i);
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
        arraySort.sort(indexes, indexes.length, (i1, i2) -> Integer.compare(tasks[i1].getEnd().getUB(), tasks[i2].getEnd().getUB()));
        int j = Q.get(0);
        for(int i : indexes) {
            while(!Q.isEmpty() && tasks[i].getEnd().getUB()>tasks[Q.get(0)].getStart().getUB()) {
                j = Q.removeAt(0);
                thetaLambdaTree.addToTheta(j);
            }
            if(i!=j && getEnvWithouti(i)>tasks[i].getStart().getUB()) {
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
        arraySort.sort(indexes, indexes.length, (i1, i2) -> Integer.compare(-tasks[i1].getEnd().getUB(), -tasks[i2].getEnd().getUB()));
        for(int i : indexes) {
            Q.add(i);
        }
        int j = Q.get(0);
        while(Q.size() > 1) {
            if(thetaLambdaTree.root.env > tasks[j].getEnd().getUB()) { // Overload Check
                aCause.fails();
            }
            thetaLambdaTree.addToLambdaAndRemoveFromTheta(j);
            Q.removeAt(0);
            j = Q.get(0);
            while(thetaLambdaTree.root.envLambda > tasks[j].getEnd().getUB()) {
                int i = thetaLambdaTree.root.responsibleEnvLambda.taskIdx;
                hasFiltered |= tasks[i].getStart().updateLowerBound(thetaLambdaTree.root.env, aCause);
                thetaLambdaTree.removeFromLambda(i);
            }
        }

        return hasFiltered;
    }
}
