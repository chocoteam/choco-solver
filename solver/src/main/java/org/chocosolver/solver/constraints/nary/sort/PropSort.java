/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sort;

import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.PriorityQueue;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * <code>SortingConstraint</code> is a constraint that ensures
 * that a vector is the sorted version of a second one. The filtering
 * algorithm is the version of Kurt Mehlhorn and Sven Thiel, from
 * CP'00 (<i>Faster algorithms for Bound-Consistency of the Sortedness
 * and the Alldifferent Constraint</i>).
 *
 * @author Sylvain Bouveret (initial code)
 * @author Charles Prud'homme (migration to choco3, debugging)
 * @since 17 apr. 2014
 */

public final class PropSort extends Propagator<IntVar> {

    private final int n; // size of X, and obviously Y

    private final PriorityQueue pQueue; // a priority queue
    private final IntVar[] x;
    private final IntVar[] y; // ref to X and Y, instead of vars
    private final int[] f;
    private final int[] fPrime;
    private final int[][] xyGraph;
    private final int[] dfsNodes;
    private final int[] sccNumbers;
    private int currentSccNumber;
    private final int[] tmpArray;
    private final int[][] sccSequences;
    private final TIntStack s1;
    private final Stack2 s2;
    private final int[] recupStack = new int[3];
    private final int[] recupStack2 = new int[3];

    /**
     * Creates a new <code>PropSort</code> instance.
     *
     * @param x the first array of integer variables
     * @param y the second array of integer variables
     */
    public PropSort(IntVar[] x, IntVar[] y) {
        super(ArrayUtils.append(x, y), PropagatorPriority.LINEAR, false);
        if (x.length != y.length || x.length == 0) {
            throw new IllegalArgumentException("PropSort Error: the two vectors "
                    + "must be of the same (non zero) size");
        }
        this.n = x.length;
        this.x = x;
        this.y = y;
        this.f = new int[this.n];
        this.fPrime = new int[this.n];
        this.xyGraph = new int[this.n][this.n];
        this.sccSequences = new int[this.n][this.n];
        this.dfsNodes = new int[this.n];
        this.sccNumbers = new int[this.n];
        this.tmpArray = new int[this.n];
        this.pQueue = new PriorityQueue(this.n);
        this.s1 = new TIntArrayStack(this.n);
        this.s2 = new Stack2(this.n);
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int[] _x = new int[this.n];
            for (int i = 0; i < n; i++) {
                _x[i] = x[i].getValue();
            }
            java.util.Arrays.sort(_x);

            int i;
            i = 0;
            while (i < n && _x[i] == y[i].getValue()) {
                i++;
            }
            return ESat.eval(i == n);
        }
        return ESat.UNDEFINED;
    }

    private void filter() throws ContradictionException {
        int i, jprime, tmp, j, k;

        for (i = 0; i < this.n; i++) {
            Arrays.fill(this.xyGraph[i], -1);
            Arrays.fill(this.sccSequences[i], -1);
        }


        /////////////////////////////////////////////////////////////
        // Normalizing the vectors...
        /////////////////////////////////////////////////////////////
        normalize(y);

        /////////////////////////////////////////////////////////////
        // Computing the perfect maching f... (optimized !)
        /////////////////////////////////////////////////////////////
        this.pQueue.clear();
        for (i = 0; i < this.n; i++) {
            if (intersect(0, i)) {
                this.pQueue.addElement(i, x[i].getUB());
            }
        }

        this.f[0] = this.computeF(0);

        for (j = 1; j < this.n; j++) {
            for (i = 0; i < this.n; i++) {
                if (x[i].getLB() > y[j - 1].getUB() && x[i].getLB() <= y[j].getUB()) {
                    this.pQueue.addElement(i, x[i].getUB());
                }
            }
            this.f[j] = this.computeF(j);
        }

        /////////////////////////////////////////////////////////////
        // Narrowing the upper bounds of y...
        /////////////////////////////////////////////////////////////

        for (i = 0; i < this.n; i++) {
            y[i].updateUpperBound(x[this.f[i]].getUB(), this);
        }

        /////////////////////////////////////////////////////////////
        // Computing the perfect maching f'... (optimized !)
        /////////////////////////////////////////////////////////////

        this.pQueue.clear();

        for (i = 0; i < this.n; i++) {
            if (intersect(n - 1, i)) {
                this.pQueue.addElement(i, -x[i].getLB());
            }
        }

        this.fPrime[this.n - 1] = this.computeFPrime(this.n - 1);

        for (j = this.n - 2; j >= 0; j--) {
            for (i = 0; i < this.n; i++) {
                if (x[i].getUB() < y[j + 1].getLB() && x[i].getUB() >= y[j].getLB()) {
                    this.pQueue.addElement(i, -x[i].getLB());
                }
            }
            this.fPrime[j] = this.computeFPrime(j);
        }

        /////////////////////////////////////////////////////////////
        // Narrowing the lower bounds of y...
        /////////////////////////////////////////////////////////////

        for (i = 0; i < this.n; i++) {
            y[i].updateLowerBound(x[this.fPrime[i]].getLB(), this);
        }

        /////////////////////////////////////////////////////////////
        // Computing the strong connected components (optimized)...
        /////////////////////////////////////////////////////////////

        for (j = 0; j < this.n; j++) { // for each y
            tmp = 0;
            jprime = this.f[j]; // jprime is the number of x associated with y_j
            for (i = 0; i < this.n; i++) { // for each other y
                if (j != i && intersect(i, jprime)) {
                    this.xyGraph[j][tmp] = i;
                    tmp++;
                }
            }
        }

        this.dfs();

        /////////////////////////////////////////////////////////////
        // Narrowing the lower bounds of x... (to be optimized)
        /////////////////////////////////////////////////////////////

        Arrays.fill(this.tmpArray, 0);
        for (i = 0; i < this.n; i++) {
            this.sccSequences[this.sccNumbers[i]][tmpArray[this.sccNumbers[i]]] = i;
            tmpArray[this.sccNumbers[i]]++;
        }

        for (i = 0; i < this.n && this.sccSequences[i][0] != -1; i++) { // for each strongly connected component...
            for (j = 0; j < this.n && this.sccSequences[i][j] != -1; j++) { // for each x of the component
                jprime = this.f[this.sccSequences[i][j]];
                k = 0;
                while (k < this.n && this.sccSequences[i][k] != -1 && x[jprime].getLB() > y[this.sccSequences[i][k]].getUB()) {
                    k++;
                }
                // scan the sequence of the ys of the connected component, until one becomes greater than or equal to x
                assert (this.sccSequences[i][k] != -1);
                x[jprime].updateLowerBound(y[this.sccSequences[i][k]].getLB(), this);
            }
        }

        /////////////////////////////////////////////////////////////
        // Narrowing the upper bounds of x... (to be optimized)
        /////////////////////////////////////////////////////////////

        Arrays.fill(this.tmpArray, 0);
        for (i = this.n - 1; i >= 0; i--) {
            this.sccSequences[this.sccNumbers[i]][tmpArray[this.sccNumbers[i]]] = i;
            tmpArray[this.sccNumbers[i]]++;
        }

        for (i = 0; i < this.n && this.sccSequences[i][0] != -1; i++) { // for each strongly connected component...
            for (j = 0; j < this.n && this.sccSequences[i][j] != -1; j++) { // for each x of the component
                jprime = this.f[this.sccSequences[i][j]];
                k = 0;
                while (k < this.n && this.sccSequences[i][k] != -1 && x[jprime].getUB() < y[this.sccSequences[i][k]].getLB()) {
                    k++;
                }
                // scan the sequence of the ys of the connected component, until one becomes lower than or equal to x
                assert (this.sccSequences[i][k] != -1);
                x[jprime].updateUpperBound(y[this.sccSequences[i][k]].getUB(), this);
            }
        }
    }

    private void normalize(IntVar[] y) throws ContradictionException {
        for (int i = 1; i < this.n; i++) {
            y[i].updateLowerBound(y[i - 1].getLB(), this);
        }

        for (int i = this.n - 2; i >= 0; i--) {
            y[i].updateUpperBound(y[i + 1].getUB(), this);
        }
    }


    private boolean intersect(int y, int x) {
        return (this.x[x].getLB() >= this.y[y].getLB() && this.x[x].getLB() <= this.y[y].getUB())
                || (this.x[x].getUB() >= this.y[y].getLB() && this.x[x].getUB() <= this.y[y].getUB())
                || (this.y[y].getLB() >= this.x[x].getLB() && this.y[y].getLB() <= this.x[x].getUB())
                || (this.y[y].getUB() >= this.x[x].getLB() && this.y[y].getUB() <= this.x[x].getUB());


    }

    private int computeF(int j) throws ContradictionException {
        if (this.pQueue.isEmpty()) {
            this.fails();
        }
        int i = this.pQueue.pop();
        if (x[i].getUB() < y[j].getLB()) {
            this.fails();
        }

        return i;
    }

    private int computeFPrime(int j) throws ContradictionException {
        if (this.pQueue.isEmpty()) {
            this.fails();
        }
        int i = this.pQueue.pop();
        if (x[i].getLB() > y[j].getUB()) {
            this.fails();
        }

        return i;
    }


    private void dfs() {
        Arrays.fill(this.dfsNodes, 0);
        this.s1.clear();
        this.s2.clear();
        this.currentSccNumber = 0;

        int i;
        for (i = 0; i < this.n; i++) {
            if (this.dfsNodes[i] == 0) {
                this.dfsVisit(i);
            }
        }
        while (s1.size() > 0 && !s2.isEmpty()) {
            s2.peek(recupStack);
            do {
                i = this.s1.pop();
                this.sccNumbers[i] = currentSccNumber;
            } while (s1.size() > 0 && i != recupStack[0]);
            currentSccNumber++;
            s2.pop();
        }
    }

    private void dfsVisit(int node) {
        int i;
        this.dfsNodes[node] = 1;
        if (this.s2.isEmpty()) {
            this.s1.push(node);
            this.s2.push(node, node, x[f[node]].getUB());
            i = 0;
            while (xyGraph[node][i] != -1) {
                if (dfsNodes[xyGraph[node][i]] == 0) {
                    this.dfsVisit(xyGraph[node][i]);
                }
                i++;
            }
        } else {
            while (this.s2.peek(this.recupStack) && this.recupStack[2] < y[node].getLB()) {// the topmost component cannot reach "node".
                while ((i = this.s1.pop()) != this.recupStack[0]) {
                    this.sccNumbers[i] = currentSccNumber;
                }
                this.sccNumbers[i] = currentSccNumber;
                this.s2.pop();
                currentSccNumber++;
            }
            this.s1.push(node);
            this.recupStack[0] = node;
            this.recupStack[1] = node;
            this.recupStack[2] = this.x[this.f[node]].getUB();
            this.mergeStack(node);
            i = 0;
            while (xyGraph[node][i] != -1) {
                if (dfsNodes[xyGraph[node][i]] == 0) {
                    this.dfsVisit(xyGraph[node][i]);
                }
                i++;
            }
        }

        this.dfsNodes[node] = 2;
    }

    private boolean mergeStack(int node) {
        this.s2.peek(this.recupStack2);
        boolean flag = false;
        while (!this.s2.isEmpty() && y[this.recupStack2[1]].getUB() >= x[this.f[node]].getLB()) {
            flag = true;
            this.recupStack[0] = this.recupStack2[0];
            this.recupStack[1] = node;
            this.recupStack[2] = this.recupStack[2] > this.recupStack2[2] ? this.recupStack[2] : this.recupStack2[2];
            this.s2.pop();
            this.s2.peek(this.recupStack2);
        }
        this.s2.push(this.recupStack[0], this.recupStack[1], this.recupStack[2]);
        return flag;
    }


    //////////////////////
    //////////////////////
    //////////////////////


    private static class Stack2  {
        private final int[] roots;
        private final int[] rightMosts;
        private final int[] maxXs;
        private final int n;
        private int nbElts = 0;

        public Stack2(int _n) {
            this.n = _n;
            this.roots = new int[_n];
            this.rightMosts = new int[_n];
            this.maxXs = new int[_n];
        }

        public boolean push(int root, int rightMost, int maxX) {
            if (this.nbElts == this.n) {
                return false;
            }
            this.roots[this.nbElts] = root;
            this.rightMosts[this.nbElts] = rightMost;
            this.maxXs[this.nbElts] = maxX;
            this.nbElts++;
            return true;
        }

        public boolean pop() {
            if (this.isEmpty()) {
                return false;
            }
            this.nbElts--;
            return true;
        }

        public boolean pop(int[] x) {
            if (this.isEmpty()) {
                return false;
            }
            this.nbElts--;
            x[0] = this.roots[this.nbElts];
            x[1] = this.rightMosts[this.nbElts];
            x[2] = this.maxXs[this.nbElts];
            return true;
        }

        public boolean peek(int[] x) {
            if (this.isEmpty()) {
                return false;
            }
            x[0] = this.roots[this.nbElts - 1];
            x[1] = this.rightMosts[this.nbElts - 1];
            x[2] = this.maxXs[this.nbElts - 1];
            return true;
        }

        public boolean isEmpty() {
            return (this.nbElts == 0);
        }

        public void clear() {
            this.nbElts = 0;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < this.nbElts; i++) {
                s.append(" <").append(this.roots[i]).append(", ")
                        .append(this.rightMosts[i]).append(", ").append(this.maxXs[i]).append(">");
            }
            return s.toString();
        }
    }

}

