/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.sort;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
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
    private int[] SORTMIN, SORTMAX, XMATE, YMATE, NODE, ROOT, RIGHTMOST, MAXX, SCC, SORTY;
    private TIntList[] CHUNK;
    private int NSCC;

    /**
     * Creates a new <code>PropSort</code> instance.
     *
     * @param x the first array of integer variables
     * @param y the second array of integer variables
     */
    public PropKeysorting(IntVar[][] x, IntVar[][] y, int k) {
        super(ArrayUtils.append(ArrayUtils.flatten(x), ArrayUtils.flatten(y)), PropagatorPriority.LINEAR, false);
        this.n = x.length;
        this.m = x[0].length;
        this.k = k;
        this.X = x;
        this.Y = y;
        this.XLB = new int[n][k];
        this.XUB = new int[n][k];
        this.YLB = new int[n][k];
        this.YUB = new int[n][k];
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
        this.CHUNK = new TIntList[n];
        for (int i = 0; i < n; i++) {
            CHUNK[i] = new TIntArrayList();
        }
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            //TODO
        }
    }


    @Override
    public ESat isEntailed() {
        // TODO
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
        boolean prune = false;
        do {
            if (
                    !init()
                            || !normalizeY()
                            || !normalizeX()
                            || !matchUp()
                            || !matchDown()
                            || !findSCC()
                            || !narrow()
                            || !(prune = prune())
                    )
                this.contradiction(null, "");
        } while (prune);
    }

    /**
     * Initialize lexicographic bounds
     */
    private boolean init() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k; j++) {
                XLB[i][j] = X[i][j].getLB();
                XUB[i][j] = X[i][j].getUB();
                YLB[i][j] = Y[i][j].getLB();
                YUB[i][j] = Y[i][j].getUB();
            }
        }
        return true;
    }

    /**
     * Normalize endpoints of Y intervals
     */
    private boolean normalizeY() {
        int[] cur = YLB[0];
        for (int i = 1; i < n - 1; i++) {
            cur[k] = cur[k + 1];
            if (lexFixMin(YLB[i], YUB[i], cur)) {
                return false;
            }
            cur = YLB[i];

        }
        cur = YLB[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            cur[k] = cur[k - 1];
            if (lexFixMax(YLB[i], YUB[i], cur)) {
                return false;
            }
            cur = YUB[i];
        }
        return true;
    }

    /**
     * Normalize endpoints of X intervals
     */
    private boolean normalizeX() throws ContradictionException {
        //TODO: INITIALIZE SORTMIN
        int x, y = 0;
        for (int i = 0; i < n; i++) {
            x = SORTMIN[i];
            while (y < n && lexLt(YUB[y], XLB[x])) {
                y++;
            }
            if (y == n) {
                return false;
            }
            if (lexLt(XLB[x], YLB[y])) {
                if (!lexFixMin(XLB[x], XUB[x], YLB[y])) {
                    return false;
                }
            }
        }
        //TODO: INIT SORTMAX
        y = n - 1;
        for (int i = n - 1; i > +0; i--) {
            x = SORTMAX[i];
            while (y >= 0 && lexGt(YLB[y], XUB[x])) {
                y--;
            }
            if (y < 0) {
                return false;
            }
            if (lexLt(XUB[x], YUB[y])) {
                if (!lexFixMax(XLB[x], XUB[x], YUB[y])) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Matching, up phase. Simulate a priority queue iterating y from 0 to n-1 ...
     */
    private boolean matchUp() {
        int e = 0, i = 0;
        int y = 0, x = SORTMIN[0];
        for (y = 0; y < n; y++) {
            while (i < n && lexGe(YUB[y], XLB[x])) {
                CHUNK[x].add(y);
                i++;
                if (i < n) {
                    x = SORTMIN[i];
                }
            }
            if (i == e) {
                return false;
            } else {
                makeDSet(y);
                e++;
            }
        }
        if (i != e) {
            return false;
        }
        for (e = 0; e < n; e++) {
            x = SORTMAX[e];
            y = min(findDSet(CHUNK[x]));
            if (lexLt(XUB[x], YLB[y])) {
                return false;
            }
            YMATE[y] = x;
            XMATE[x] = y;
            unionDSet(findDSet(y), findDSet(y + 1));
        }
        for (y = 0; y < n; y++) {
            x = YMATE[y];
            if (!lexFixMax(YLB[y], YUB[y], XUB[x])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Matching, up phase. Simulate a priority queue iterating y from 0 to n-1 ...
     */
    private boolean matchDown() {
        int e = 0, i = 0;
        int y, x = SORTMAX[n - 1];
        for (y = n - 1; y >= 0; y--) {
            while (i < n && lexGe(YUB[x], YLB[y])) {
                CHUNK[x].add(y);
                i++;
                if (i < n) {
                    x = SORTMAX[n - i - 1];
                }
            }
            if (i == e) {
                return false;
            } else {
                makeDSet(y);
                e++;
            }
        }
        if (i != e) {
            return false;
        }
        for (e = 0; e < n; e++) {
            x = SORTMIN[e];
            y = min(findDSet(CHUNK[x]));
            if (lexLt(YUB[y], XLB[x])) {
                return false;
            }
            YMATE[y] = x;
            XMATE[x] = y;
            unionDSet(findDSet(y), findDSet(y - 1));
        }
        for (y = 0; y < n; y++) {
            x = YMATE[y];
            if (!lexFixMin(YLB[y], YUB[y], XLB[x])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Find all SCCs
     *
     * @return
     */
    private boolean findSCC() {
        int p, r, x, y = 0, s = 0, t = 0, u = 0;
        while (y - t < n) {
            if (u == 0) {
                x = YMATE[y];
                NODE[0] = ROOT[0] = RIGHTMOST[0] = y;
                MAXX[0] = x;
                t = u = 1;
                y++;
            } else if (y < n && lexGe(XUB[MAXX[u - 1]], YLB[y])) {
                p = x = YMATE[y];
                NODE[t] = ROOT[u] = y;
                t++;
                while (u > 0 && lexGe(YUB[RIGHTMOST[u - 1]], XLB[x])) {
                    u--;
                    if (lexLt(XUB[p], XUB[MAXX[u]])) {
                        p = MAXX[u];
                    }
                }
                RIGHTMOST[u] = y;
                MAXX[u] = p;
                u++;
                y++;
            } else {
                u--;
                r = ROOT[t];
                do {
                    t--;
                    y = NODE[t];
                    SCC[y] = s;
                } while (y <= r);
                s++;
            }
        }
        NSCC = s;
        return true;
    }

    /**
     * Narrow amm X[i] lex intervals
     */
    private boolean narrow() {
        // TODO: SORTY, SORTMIN, SORTMAX
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
                if (lexGe(YUB[y], XLB[x])) {
                    if (!lexFixMin(XLB[x], XUB[x], YLB[y])) {
                        return false;
                    }
                    j++;
                } else {
                    i++;
                }
            }
        }
        j = n;
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
                if (lexGe(XUB[x], YLB[y])) {
                    if (!lexFixMax(XLB[x], XUB[x], YUB[y])) {
                        return false;
                    }
                    j--;
                } else {
                    i--;
                }
            }
        }
        return true;
    }

    /**
     * Make all tuple fields bounds-consistent
     */
    private boolean prune() {
        for (int i = 0; i < n; i++) {
            if (
                    !DvarLexFixMin(X[i], XLB[i])
                            || !DvarLexFixMax(X[i], XUB[i])
                            || !DvarLexFixMin(Y[i], YLB[i])
                            || !DvarLexFixMax(Y[i], YUB[i])
                    ) {
                return false;
            }
        }
        for (int d = 0; d < m; d++) {
            int j = 0, x;
            for (int i = 0; i < n; i++) {
                int y = SORTY[i];
                int s = SCC[y];
                int xlb = Integer.MAX_VALUE, ylb = Integer.MAX_VALUE;
                int xub = Integer.MIN_VALUE, yub = Integer.MIN_VALUE;
                while (i < n && SCC[SORTY[i]] == s) {
                    y = SORTY[i];
                    x = YMATE[y];
                    i++;
                    xlb = Math.min(X[x][d].getLB(), xlb);
                    ylb = Math.min(Y[y][d].getLB(), ylb);
                    xub = Math.max(X[x][d].getUB(), xub);
                    yub = Math.max(Y[y][d].getUB(), yub);
                }
                while (j < i) {
                    y = SORTY[j];
                    x = YMATE[y];
                    j++;
                    if (!DvarFixInterval(X[x][d], ylb, yub) ||
                            !DvarFixInterval(Y[y][d], xlb, xub)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Find the lex smalles L' such that L <=_lex L' <=_lex U and T <=_lex L', or fail.
     * AlT is true while L = T might be true. AtU is true while U = T might be true.
     */
    private boolean lexFixMin(int[] L, int[] U, int[] T) {
        boolean AtL = true, AtU = true;
        for (int i = 0; i < k; i++) {
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
        for (int i = 0; i < k; i++) {
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
    private boolean DvarLexFixMin(IntVar[] V, int[] T) {
        for(int i  = 0; i < k ; i++){
            int q = i;
            if(V[i].getLB() > T[i]){
                return true;
            }else if (V[i].getUB() > T[i]){
                if(!DvarLexFixMin(V[q], T[q]));
            }
        }
        return true;
    }

    private boolean DvarFixInterval(IntVar V, int L, int U) {
        return true;
    }

    private boolean DvarLexFixMax(IntVar[] V, int[] T) {
        return true;
    }

    private boolean lexLt(int[] A1, int[] A2) {
        return true;
    }

    private boolean lexLe(int[] A1, int[] A2) {
        return true;
    }


    private boolean lexGt(int[] A1, int[] A2) {
        return lexLt(A2,A1);
    }

    private boolean lexGe(int[] A1, int[] A2) {
        return lexLe(A2,A1);
    }

    private void makeDSet(int y) {

    }

}


