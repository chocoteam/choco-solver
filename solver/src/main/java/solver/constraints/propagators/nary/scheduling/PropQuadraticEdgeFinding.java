/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.nary.scheduling;

/**
 * Created with IntelliJ IDEA.
 * User: adeclerc
 * Date: 25/07/12
 * Time: 18:03
 * To change this template use File | Settings | File Templates.
 */

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.scheduling.Cumulative;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;


/**
 * This class implements the Edge-Finding algorithm described in
 * Kameugne - Fotso - Scott - NgoKateu
 * "A quadratic Edge-Finding Filtering algorithm for Cumulative Resource Constraints - CP'11
 */

public class PropQuadraticEdgeFinding extends Propagator<IntVar> {
    private Cumulative cu;

    private Solver s;
    int nbTasks;
    private int[] heirelease, durrelease, releasedates, heidue, durdue, duedates;
    private int[] indexrelease, indexdue, endrelease, releaseend;

    public PropQuadraticEdgeFinding(IntVar[] vars, Solver solver, Constraint<IntVar, Propagator<IntVar>> cstr) {
        super(vars, solver, cstr, PropagatorPriority.QUADRATIC, true);
        this.s = solver;
        this.nbTasks = ((Cumulative) (constraint)).nbTasks();
        this.cu = (Cumulative) constraint;
    }

    public boolean kameugneStartEF() throws ContradictionException {
        boolean modified = false;
        initializeEdgeFindingKameugneStart();
        int[] LBp = new int[nbTasks];
        int[] Dupd = new int[nbTasks];
        int[] SLupd = new int[nbTasks];
        int[] E = new int[nbTasks];
        int energy, maxEnergy, rRho, rTho;
        int rest, restP, minSL;
        for (int i = 0; i < nbTasks; i++) {
            LBp[i] = releasedates[i];
            Dupd[i] = Integer.MIN_VALUE;
            SLupd[i] = Integer.MIN_VALUE;
        }
        for (int u = 0; u < nbTasks; u++) {
            energy = 0;
            maxEnergy = 0;
            //Original algorithm :
            rRho = Integer.MIN_VALUE;

            for (int i = nbTasks - 1; i >= 0; i--) {

                if (endrelease[i] <= duedates[u]) {
                    energy += heirelease[i] * durrelease[i];
                    if (energy / (duedates[u] - releasedates[i]) > maxEnergy / (duedates[u] - rRho)) {
                        maxEnergy = energy;
                        rRho = releasedates[i];
                    }
                } else {
                    rest = maxEnergy - (cu.limit() - heirelease[i]) * (duedates[u] - rRho);
                    if (rest > 0) {
                        Dupd[i] = (int) Math.max(Dupd[i], rRho + Math.ceil(rest / heirelease[i]));
                    }
                }
                E[i] = energy;
            }
            minSL = Integer.MAX_VALUE;
            rTho = duedates[u];
            for (int i = 0; i < nbTasks; i++) {

                if (cu.limit() * (duedates[u] - releasedates[i]) - E[i] < minSL) {
                    rTho = releasedates[i];
                    minSL = (int) cu.limit() * (duedates[u] - rTho) - E[i];
                }
                if (endrelease[i] > duedates[u]) {
                    restP = heirelease[i] * (duedates[u] - rTho) - minSL;
                    if ((rTho <= duedates[u]) && (restP > 0)) {
                        SLupd[i] = (int) Math.max(SLupd[i], rTho + Math.ceil(restP / heirelease[i]));
                    }
                    if ((releasedates[i] + durrelease[i] >= duedates[u]) || (minSL - (durrelease[i] * heirelease[i]) < 0)) {
                        LBp[i] = Math.max(LBp[i], Math.max(Dupd[i], SLupd[i]));
                    }
                }
            }
            //System.out.println("-----------------------");
        }
        for (int i = 0; i < nbTasks; i++) {
            modified |= vars[indexrelease[i]].updateLowerBound(LBp[i], aCause);
            modified |= vars[2 * nbTasks + indexrelease[i]].updateLowerBound(LBp[i] + durrelease[i], aCause);
        }
        return modified;
    }

    public void initializeEdgeFindingKameugneStart() {
        releasedates = new int[nbTasks];
        durrelease = new int[nbTasks];
        heirelease = new int[nbTasks];
        indexrelease = new int[nbTasks];
        duedates = new int[nbTasks];
        durdue = new int[nbTasks];
        heidue = new int[nbTasks];
        indexdue = new int[nbTasks];
        endrelease = new int[nbTasks];
        releaseend = new int[nbTasks];
        for (int i = 0; i < nbTasks; i++) {
            releasedates[i] = vars[i].getLB();
            durrelease[i] = vars[i + nbTasks].getLB();
            heirelease[i] = vars[i + 3 * nbTasks].getLB();
            indexrelease[i] = i;
            endrelease[i] = vars[i + 2 * nbTasks].getUB();

            duedates[i] = vars[i + 2 * nbTasks].getUB();
            durdue[i] = vars[i + nbTasks].getLB();
            heidue[i] = vars[i + 3 * nbTasks].getLB();
            indexdue[i] = i;
            releaseend[i] = vars[i].getLB();
        }
        sort(new int[][]{releasedates, durrelease, heirelease, indexrelease, endrelease});
        sort(new int[][]{duedates, durdue, heidue, indexdue, releaseend});
    }

    /**
     * Tri rapide it�ratif
     */
    public void sort(int[][] tab) {
        int[] range = new int[tab[0].length + 1]; // if (range[i]<0) then skip[i] =
        // |range[i]|
        range[0] = tab[0].length - 1;
        int i, j, sortedCount = 0;
        while (sortedCount < tab[0].length) {
            for (i = 0; i < tab[0].length; i++)
                if (range[i] >= i) {
                    j = range[i];
                    if (j - i < 7) {
                        // selectionsort the elements from a[i] to a[j]
                        // inclusive
                        // and set all their ranges to -((j+1)-k)
                        for (int m = i; m <= j; m++) {
                            for (int n = m; n > i
                                    && tab[0][n - 1] > tab[0][n]; n--)
                                swap(n, n - 1, tab);
                            range[m] = -((j + 1) - m);
                            sortedCount++;
                        }
                        i = j;
                    } else {
                        for (; i <= j; i++) {
                            int p = partition(i, j, tab);
                            sortedCount++;
                            if (p > i)
                                range[i] = p - 1;
                            if (p < j)
                                range[p + 1] = j;
                            range[i = p] = -1; // sorted
                        }
                    }
                } else {
                    // skip[i] += skip[i + skip[i]];
                    while ((j = range[i - range[i]]) < 0)
                        range[i] += j;
                    i += -range[i] - 1;
                }
        }
    }

    public int partition(int left, int right, int[][] tab) {
        // DK: added check if (left == right):
        if (left == right)
            return left;
        int i = left - 1;
        int j = right;
        while (true) {
            while (tab[0][++i] < tab[0][right])
                // find item on left to swap
                ; // a[right] acts as sentinel
            while (tab[0][right] < tab[0][--j])
                // find item on right to swap
                if (j == left)
                    break; // don't go out-of-bounds
            if (i >= j)
                break; // check if pointers cross
            swap(i, j, tab); // swap two elements into place
        }
        swap(i, right, tab); // swap with partition element
        return i;
    }

    private void swap(int i, int j, int[][] tab) {
        int tabTempo = tab[0][i];
        int durTempo = tab[1][i];
        int heiTempo = tab[2][i];
        int indexTempo = tab[3][i];
        int otherdateTempo = tab[4][i];

        tab[0][i] = tab[0][j];
        tab[1][i] = tab[1][j];
        tab[2][i] = tab[2][j];
        tab[3][i] = tab[3][j];
        tab[4][i] = tab[4][j];

        tab[0][j] = tabTempo;
        tab[1][j] = durTempo;
        tab[2][j] = heiTempo;
        tab[3][j] = indexTempo;
        tab[4][j] = otherdateTempo;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.BOUND.mask + EventType.INSTANTIATE.mask;
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        this.kameugneStartEF();
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        this.forcePropagate(EventType.FULL_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        Cumulative cu = ((Cumulative) constraint);
        int minStart = Integer.MAX_VALUE;
        int maxEnd = Integer.MIN_VALUE;
        // compute min start and max end
        for (int is = 0, id = cu.nbTasks(), ie = 2 * cu.nbTasks(), ih = 3 * cu.nbTasks(); is < cu.nbTasks(); is++, id++, ie++, ih++) { // is = start index, id = duration index, ie = end index
            if (!vars[is].instantiated() || !vars[id].instantiated() || !vars[ie].instantiated() || !vars[ih].instantiated())
                return ESat.UNDEFINED;
            if (vars[is].getValue() < minStart) minStart = vars[is].getValue();
            if (vars[ie].getValue() > maxEnd) maxEnd = vars[ie].getValue();
        }
        int sumHeight;
        // scan the time axis and check the height
        for (int i = minStart; i <= maxEnd; i++) {
            sumHeight = 0;
            //System.out.println(minStart+"   "+maxEnd);
            for (int is = 0, ie = 2 * cu.nbTasks(), ih = 3 * cu.nbTasks(); is < cu.nbTasks(); is++, ie++, ih++) {
                if (i >= vars[is].getValue() && i < vars[ie].getValue()) sumHeight += vars[ih].getValue();
            }
            if (sumHeight > cu.limit()) {
                //System.out.println("sumHeight = "+sumHeight+" at "+i);
                return ESat.FALSE;
            }
        }
        return ESat.TRUE;
    }
}
