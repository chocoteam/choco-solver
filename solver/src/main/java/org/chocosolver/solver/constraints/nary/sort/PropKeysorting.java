/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sort;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.sort.IntComparator;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Based on Technical Report from Mats Carlsson: "Propagating THE KEYSORTING Constraint" - Sept. 15, 2014.
 *
 * @author Charles Prud'homme
 * @since 02 feb. 2015
 */

public final class PropKeysorting extends Propagator<IntVar> {

    private int n; // size of X, and obviously Y
    private int m;
    private int k; // number of keys

    private IntVar[][] X, Y; // ref to X and Y, instead of vars
    private int[][] XLB, XUB, YLB, YUB;
    private int[] CHUNK, SORTMIN, SORTMAX, XMATE, YMATE, NODE, ROOT, RIGHTMOST, MAXX, SCC, SORTY, ARRAY, CUR;
    private boolean prune;

    protected final ArraySort sorter;
    private final IntComparator sortmincomp1 = (i, j) -> {
        int z = 0;
        while (z <= k && XLB[i][z] == XLB[j][z]) {
            z++;
        }
        return z <= k ? XLB[i][z] - XLB[j][z] : 0;
    };

    private final IntComparator sortmincomp2 = (i, j) -> {
        if (SCC[XMATE[i]] != SCC[XMATE[j]]) {
            return SCC[XMATE[i]] - SCC[XMATE[j]];
        } else {
            return 0;
        }
    };

    private final IntComparator sortmaxcomp1 = (i, j) -> {
        int z = 0;
        while (z <= k && XUB[i][z] == XUB[j][z]) {
            z++;
        }
        return z <= k ? XUB[i][z] - XUB[j][z] : 0;
    };

    private final IntComparator sortmaxcomp2 = (i, j) -> {
        if (SCC[XMATE[i]] != SCC[XMATE[j]]) {
            return SCC[XMATE[i]] - SCC[XMATE[j]];
        } else {
            return 0;
        }
    };

    private final IntComparator sortycomp = (i, j) -> {
        if (SCC[i] != SCC[j]) {
            return SCC[i] - SCC[j];
        } else {
            return i - j;
        }
    };


    /**
     * Creates a new <code>PropSort</code> instance.
     *
     * @param x the first array of integer variables
     * @param y the second array of integer variables
     */
    public PropKeysorting(IntVar[][] x, IntVar[][] y, IntVar[] p, int key) {
        super(ArrayUtils.append(ArrayUtils.flatten(x), ArrayUtils.flatten(y), p), PropagatorPriority.LINEAR, false);
        this.n = x.length;
        this.m = x[0].length;
        this.k = key;
        this.X = new IntVar[n][m + 1];
        this.Y = new IntVar[n][m + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(x[i], 0, X[i], 0, k);
            System.arraycopy(y[i], 0, Y[i], 0, k);
            this.X[i][k] = model.intVar(i + 1);
            this.Y[i][k] = p[i];
            System.arraycopy(x[i], k, X[i], k + 1, m - k);
            System.arraycopy(y[i], k, Y[i], k + 1, m - k);
        }

        this.XLB = new int[n][k + 1];
        this.XUB = new int[n][k + 1];
        this.YLB = new int[n][k + 1];
        this.YUB = new int[n][k + 1];
        this.SORTMIN = new int[n];
        this.SORTMAX = new int[n];
        this.XMATE = new int[n];
        this.YMATE = new int[n];
        this.NODE = new int[n];
        this.ROOT = new int[n];
        this.RIGHTMOST = new int[n];
        this.MAXX = new int[n];
        this.SCC = new int[n];
        this.SORTY = new int[n];
        this.CHUNK = new int[n];
        this.ARRAY = new int[n];
        this.CUR = new int[k + 1];

        sorter = new ArraySort(n, false, true);
    }


    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            IntVar[] ys;
            ys = Y[0];
            for (int i = 1; i < n; i++) {
                // ensure Y[i-1] <= lex Y[i]
                for (int j = 0; j < k + 1; j++) {
                    if (ys[j].getValue() != Y[i][j].getValue()) {
                        if (ys[j].getValue() < Y[i][j].getValue()) {
                            break;
                        } else {
                            return ESat.FALSE;
                        }
                    }
                }
                // then make sure Y[i][j] = X[P[i]-1][j]
                int p = ys[k].getValue() - 1;
                for (int j = 0; j <= m; j++) {
                    if (X[p][j].getValue() != ys[j].getValue()) {
                        return ESat.FALSE;
                    }
                }
                ys = Y[i];
            }
            // deal with the last one
            int p = ys[k].getValue() - 1;
            for (int j = 0; j <= m; j++) {
                if (X[p][j].getValue() != ys[j].getValue()) {
                    return ESat.FALSE;
                }
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Main program
     */
    private void filter() throws ContradictionException {
        do {
            prune = false;
            init();
            normalizeY();
            normalizeX();
            matchUp();
            matchDown();
            findSCC();
            narrow();
            prune();
        } while (prune);
    }

    /**
     * Initialize lexicographic bounds
     */
    private boolean init() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j <= k; j++) {
                XLB[i][j] = X[i][j].getLB();
                XUB[i][j] = X[i][j].getUB();
                YLB[i][j] = Y[i][j].getLB();
                YUB[i][j] = Y[i][j].getUB();
                SORTMIN[i] = SORTMAX[i] = SORTY[i] = i;
            }
        }
        return true;
    }


    /**
     * Normalize endpoints of Y intervals
     */
    private void normalizeY() throws ContradictionException {
        // TODO: remove arraycopy ?
        System.arraycopy(YLB[0], 0, CUR, 0, k + 1);
        for (int i = 1; i < n; i++) {
            CUR[k]++;
            if (!lexFixMin(YLB[i], YUB[i], CUR)) {
                this.fails();
            }
            System.arraycopy(YLB[i], 0, CUR, 0, k + 1);
        }
        System.arraycopy(YUB[n - 1], 0, CUR, 0, k + 1);
        for (int i = n - 2; i >= 0; i--) {
            CUR[k]--;
            if (!lexFixMax(YLB[i], YUB[i], CUR)) {
                this.fails();
            }
            System.arraycopy(YUB[i], 0, CUR, 0, k + 1);
        }
    }

    /**
     * Normalize endpoints of X intervals
     */
    private void normalizeX() throws ContradictionException {
        sorter.sort(SORTMIN, n, sortmincomp1);
        int x, y = 0;
        for (int i = 0; i < n; i++) {
            x = SORTMIN[i];
            while (y < n && lexLt(YUB[y], XLB[x], k + 1)) {
                y++;
            }
            if (y == n) {
                this.fails();
            }
            if (lexLt(XLB[x], YLB[y], k + 1)) {
                if (!lexFixMin(XLB[x], XUB[x], YLB[y])) {
                    this.fails();
                }
            }
        }
        sorter.sort(SORTMAX, n, sortmaxcomp1);
        y = n - 1;
        for (int i = n - 1; i > +0; i--) {
            x = SORTMAX[i];
            while (y >= 0 && lexLt(XUB[x], YLB[y], k + 1)) {
                y--;
            }
            if (y < 0) {
                this.fails();
            }
            if (lexLt(YUB[y], XUB[x], k + 1)) {
                if (!lexFixMax(XLB[x], XUB[x], YUB[y])) {
                    this.fails();
                }
            }
        }
    }


    /**
     * Matching, up phase. Simulate a priority queue iterating y from 0 to n-1 ...
     */
    private void matchUp() throws ContradictionException {
        int e = 0, i = 0;
        int y, x = SORTMIN[i];
        for (y = 0; y < n; y++) {
            while (i < n && !lexLt(YUB[y], XLB[x], k + 1)) {
                CHUNK[x] = y;
                i++;
                if (i < n) {
                    x = SORTMIN[i];
                }
            }
            if (i == e) {
                this.fails();
            } else {
                ARRAY[y] = y;
                e++;
            }
        }
        if (i != e) {
            this.fails();
        }
        for (e = 0; e < n; e++) {
            x = SORTMAX[e];
            y = CHUNK[x];
            while (y < ARRAY[y]) {
                ARRAY[y] = ARRAY[ARRAY[y]]; //path compression
                y = ARRAY[y];
            }

            if (lexLt(XUB[x], YLB[y], k + 1)) {
                this.fails();
            }
            YMATE[y] = x;
            XMATE[x] = y;
            ARRAY[y] = y + 1;
        }
        for (y = 0; y < n; y++) {
            x = YMATE[y];
            if (!lexFixMax(YLB[y], YUB[y], XUB[x])) {
                this.fails();
            }
        }
    }

    /**
     * Matching, down phase. Simulate a priority queue iterating y from n-1 to 0 ...
     */
    private void matchDown() throws ContradictionException {
        int e = 0, i = 0;
        int y, x = SORTMAX[n - 1];
        for (y = n - 1; y >= 0; y--) {
            while (i < n && !lexLt(XUB[x], YLB[y], k + 1)) {
                CHUNK[x] = y;
                i++;
                if (i < n) {
                    x = SORTMAX[n - i - 1];
                }
            }
            if (i == e) {
                this.fails();
            } else {
                ARRAY[y] = y;
                e++;
            }
        }
        if (i != e) {
            this.fails();
        }
        for (e = n - 1; e >= 0; e--) {
            x = SORTMIN[e];
            y = CHUNK[x];
            while (y > ARRAY[y]) {
                ARRAY[y] = ARRAY[ARRAY[y]]; //path compression
                y = ARRAY[y];
            }
            if (lexLt(YUB[y], XLB[x], k + 1)) {
                this.fails();
            }
            YMATE[y] = x;
            XMATE[x] = y;
            ARRAY[y] = y - 1;
        }
        for (y = 0; y < n; y++) {
            x = YMATE[y];
            if (!lexFixMin(YLB[y], YUB[y], XLB[x])) {
                this.fails();
            }
        }
    }

    /**
     * Find all SCCs
     */
    private void findSCC() {
        int p, q, r, x, y = 0, s = 0, t = 0, u = 0;
        while (y - t < n) {
            if (u == 0) {
                x = YMATE[y];
                NODE[0] = ROOT[0] = RIGHTMOST[0] = y;
                MAXX[0] = x;
                t = u = 1;
                y++;
            } else if (y < n && !lexLt(XUB[MAXX[u - 1]], YLB[y], k + 1)) {
                p = x = YMATE[y];
                NODE[t] = ROOT[u] = y;
                t++;
                while (u > 0 && !lexLt(YUB[RIGHTMOST[u - 1]], XLB[x], k + 1)) {
                    u--;
                    if (lexLt(XUB[p], XUB[MAXX[u]], k + 1)) {
                        p = MAXX[u];
                    }
                }
                RIGHTMOST[u] = y;
                MAXX[u] = p;
                u++;
                y++;
            } else {
                r = ROOT[--u];
                do {
                    q = NODE[--t];
                    SCC[q] = s;
                } while (q > r);
                s++;
            }
        }
    }

    /**
     * Narrow amm X[i] lex intervals
     */
    private void narrow() throws ContradictionException {
        sorter.sort(SORTY, n, sortycomp);
        sorter.sort(SORTMIN, n, sortmincomp2);
        sorter.sort(SORTMAX, n, sortmaxcomp2);
        int s, i, x, y, j = 0, d = 0;
        while (d < n) {
            i = d;
            y = SORTY[i];
            s = SCC[y];
            d = i + 1;
            while (d < n && SCC[SORTY[d]] == s) {
                d++;
            }
            while (j < d) {
                x = SORTMIN[j];
                y = SORTY[i];
                if (!lexLt(YUB[y], XLB[x], k + 1)) {
                    if (!lexFixMin(XLB[x], XUB[x], YLB[y])) {
                        this.fails();
                    }
                    j++;
                } else {
                    i++;
                }
            }
        }
        j = n - 1;
        d = n - 1;
        while (d >= 0) {
            i = d;
            y = SORTY[i];
            s = SCC[y];
            d = i - 1;
            while (d >= 0 && SCC[SORTY[d]] == s) {
                d--;
            }
            while (j > d) {
                x = SORTMAX[j];
                y = SORTY[i];
                if (!lexLt(XUB[x], YLB[y], k + 1)) {
                    if (!lexFixMax(XLB[x], XUB[x], YUB[y])) {
                        this.fails();
                    }
                    j--;
                } else {
                    i--;
                }
            }
        }
    }

    /**
     * Make all tuple fields bounds-consistent
     */
    private void prune() throws ContradictionException {
        for (int i = 0; i < n; i++) {
            DvarLexFixMin(X[i], XLB[i]);
            DvarLexFixMax(X[i], XUB[i]);
            DvarLexFixMin(Y[i], YLB[i]);
            DvarLexFixMax(Y[i], YUB[i]);
        }
        for (int d = 0; d <= m; d++) {
            int j = 0, x;
            for (int i = 0; i < n; ) {
                int y = SORTY[i];
                int s = SCC[y];
                int xlb = Integer.MAX_VALUE, ylb = Integer.MAX_VALUE;
                int xub = Integer.MIN_VALUE, yub = Integer.MIN_VALUE;
                while (i < n && SCC[SORTY[i]] == s) {
                    y = SORTY[i++];
                    x = YMATE[y];
                    xlb = Math.min(X[x][d].getLB(), xlb);
                    ylb = Math.min(Y[y][d].getLB(), ylb);
                    xub = Math.max(X[x][d].getUB(), xub);
                    yub = Math.max(Y[y][d].getUB(), yub);
                }
                while (j < i) {
                    y = SORTY[j++];
                    x = YMATE[y];

                    prune |= X[x][d].updateLowerBound(ylb, this);
                    prune |= X[x][d].updateUpperBound(yub, this);
                    prune |= Y[y][d].updateLowerBound(xlb, this);
                    prune |= Y[y][d].updateUpperBound(xub, this);
                }
            }
        }
    }

    /**
     * Find the lex smallest L' such that L <=_lex L' <=_lex U and T <=_lex L', or fail.
     * AlT is true while L = T might be true. AtU is true while U = T might be true.
     */
    private boolean lexFixMin(int[] L, int[] U, int[] T) {
        boolean AtL = true, AtU = true;
        for (int i = 0; i <= k; i++) {
            if (U[i] != T[i]) {
                if (AtU && U[i] < T[i]) {
                    return false;
                } else {
                    AtU = false;
                }
            }
            if (L[i] != T[i]) {
                if (AtL && L[i] > T[i]) {
                    return true;
                } else {
                    AtL = false;
                }
                L[i] = T[i];
            }
        }
        return true;
    }

    /**
     * Find the lex greated U' s.t. L <=_lex U' <=_lex U and U' <=_lex T, or fail.
     * AtL is true while L = T might be true.
     * AtU is true while U = T might be true.
     */
    private boolean lexFixMax(int[] L, int[] U, int[] T) {
        boolean AtL = true, AtU = true;
        for (int i = 0; i <= k; i++) {
            if (L[i] != T[i]) {
                if (AtL && L[i] > T[i]) {
                    return false;
                } else {
                    AtL = false;
                }
            }
            if (U[i] != T[i]) {
                if (AtU && U[i] < T[i]) {
                    return true;
                } else {
                    AtU = false;
                }
                U[i] = T[i];
            }
        }
        return true;
    }

    /**
     * Maintain T <=_lex V, where T is an integer tuple and V a dvar tuple.
     */
    private void DvarLexFixMin(IntVar[] V, int[] T) throws ContradictionException {
        int i, q = 0;
        for (i = 0; i <= k; i++) {
            q = i;
            if (V[i].getLB() > T[i]) {
                return;
            } else if (V[i].getUB() > T[i]) {
                prune |= V[q].updateLowerBound(T[q], this);
                break;
            } else {
                prune |= V[q].instantiateTo(T[q], this);
            }
        }
        for (i++; i <= k; i++) {
            if (V[i].getUB() < T[i]) {
                prune |= V[q].updateLowerBound(T[q] + 1, this);
                return;
            } else if (V[i].getUB() > T[i]) {
                return;
            }
        }
    }

    /**
     * Maintain V <=_lex T, where T is an integer tuple and V a dvar tuple
     */
    private void DvarLexFixMax(IntVar[] V, int[] T) throws ContradictionException {
        int i, q = 0;
        for (i = 0; i <= k; i++) {
            q = i;
            if (V[i].getUB() < T[i]) {
                return;
            } else if (V[i].getLB() < T[i]) {
                prune |= V[q].updateUpperBound(T[q], this);
                break;
            } else {
                prune |= V[q].instantiateTo(T[q], this);
            }
        }
        for (i++; i <= k; i++) {
            if (V[i].getLB() > T[i]) {
                prune |= V[q].updateUpperBound(T[q] - 1, this);
            } else if (V[i].getLB() < T[i]) {
                return;
            }
        }
    }


    /**
     * A1[0..N] is <_lex thant A2[0..N] if:
     * - A1[0] < A2[0] or
     * - A1[0] == A2[0] AND A1[1..n] <_lex A2[1..N]
     */
    private boolean lexLt(int[] A1, int[] A2, int n) {
        int i;
        for (i = 0; i < n; i++) {
            if (A1[i] != A2[i]) {
                return A1[i] < A2[i];
            }
        }
        return false;
    }

}


