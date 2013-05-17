/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.search.loop.monitors;

import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.util.BitSet;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A Propagation Guided LNS
 * <p/>
 * Based on "Propagation Guided Large Neighborhood Search", Perron et al. CP2004.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/04/13
 */
public class PGLNS extends Abstract_LNS_SearchMonitor implements IMonitorInitPropagation {

    private final int n;
    private final IntVar[] vars;
    private final int[] bestSolution;
    private final int[] dsize;
    private Random rd;
    private int cste = 100;
    private double epsilon = 1.;
    private int nbFixedVariables = 0;

    private SortedMap<Integer, Integer> all;
    private SortedMap<Integer, Integer> candidate;
    BitSet fragment;  // index of variable to set unfrozen

    private int fairness;

    public PGLNS(Solver solver, IntVar[] vars, long seed, int listSize, int cste) {
        super(solver, true);

        this.n = vars.length;
        this.vars = vars.clone();

        this.rd = new Random(seed);
        this.bestSolution = new int[n];
        this.dsize = new int[n];
        this.cste = cste;

        this.all = new TreeMap<Integer, Integer>();
        this.candidate = new TreeMap<Integer, Integer>();
        this.fragment = new BitSet(n);
        nbFixedVariables = n / 2;
        fairness = -1;
    }

    @Override
    protected boolean isSearchComplete() {
        return nbFixedVariables == 0;
    }

    @Override
    protected void recordSolution() {
        for (int i = 0; i < vars.length; i++) {
            bestSolution[i] = vars[i].getValue();
        }
        nbFixedVariables = n / 2;
    }

    @Override
    protected void fixSomeVariables() throws ContradictionException {
        int which = fairness++ % 3;
        switch (which) {
            case 0:
                pglns();
                break;
            case 1:
                rpglns();
                break;
            case 2:
                random();
                break;
        }

    }

    private void pglns() throws ContradictionException {
        double logSum = 0;
        for (int i = 0; i < n; i++) {
            int ds = vars[i].getDomainSize();
            logSum += Math.log(ds);
        }
        cste = (int) (30 * (1 + epsilon));//(int) ((logSum * epsilon) / 4.);
        fragment.set(0, n); // all variables are frozen
        while (logSum > cste && fragment.cardinality() > 0) {
            all.clear();
            // 1. pick a variable
            int id = selectVariable();

            // 2. fix it to its solution value
            if (vars[id].contains(bestSolution[id])) {  // to deal with objective variable and related
                vars[id].instantiateTo(bestSolution[id], this);
                solver.propagate();
                fragment.clear(id);

                logSum = 0;
                for (int i = 0; i < n; i++) {
                    int ds = vars[i].getDomainSize();
                    logSum += Math.log(ds);
                    if (fragment.get(i)) { // if not frozen until now
                        if (ds == 1) { // if fixed by side effect
                            fragment.clear(i); // set it has fixed
                        } else if (dsize[i] - ds > 0) {
                            all.put(i, Integer.MAX_VALUE - (dsize[i] - ds)); // add it to candidate list
                        }
                    }
                }
                candidate.clear();
                int k = 0;
                while (!all.isEmpty() && candidate.size() < 10) {
                    int first = all.firstKey();
                    all.remove(first);
                    if (fragment.get(first)) {
                        candidate.put(first, k++);
                    }
                }
            } else {
                fragment.clear(id);
                logSum -= Math.log(vars[id].getDomainSize());
            }
        }
        epsilon = (.95 * epsilon) + (.05 * (logSum / cste));
    }

    private void rpglns() throws ContradictionException {
        double logSum = 0.;
        for (int i = 0; i < n; i++) {
            int ds = vars[i].getDomainSize();
            logSum += Math.log(ds);
        }
        cste = (int) (30 * (1 + epsilon));//(int) ((logSum * epsilon) / 3.);
        fragment.set(0, n); // all variables are fixed by default
        while (logSum > cste && fragment.cardinality() > 0) {
            all.clear();
            // 1. pick a variable
            int id = selectVariable();

            // 2. fix it to its solution value
            if (vars[id].contains(bestSolution[id])) {  // to deal with objective variable and related

                solver.getEnvironment().worldPush();
                vars[id].instantiateTo(bestSolution[id], this);
                solver.propagate();
                fragment.clear(id);

                for (int i = 0; i < n; i++) {
                    int ds = vars[i].getDomainSize();
                    if (fragment.get(i)) { // if not frozen until now
                        if (ds == 1) { // if fixed by side effect
                            fragment.clear(i); // set it has fixed
                        } else {
                            int closeness = (int) ((dsize[i] - ds) / (dsize[i] * 1.) * 100);
//                            System.out.printf("%d -> %d :%d\n", dsize[i], ds, closeness);
                            if (closeness > 0) {
                                all.put(i, Integer.MAX_VALUE - closeness); // add it to candidate list
                            }
                        }
                    }
                }
                solver.getEnvironment().worldPop();
                candidate.clear();
                int k = 1;
                while (!all.isEmpty() && candidate.size() < 10) {
                    int first = all.firstKey();
                    all.remove(first);
                    if (fragment.get(first)) {
                        candidate.put(first, k++);
                    }
                }
                logSum = 0;
                for (int i = fragment.nextSetBit(0); i > -1 && i < n; i = fragment.nextSetBit(i + 1)) {
                    logSum += Math.log(vars[i].getDomainSize());
                }
            } else {
                fragment.clear(id);
            }

        }
        for (int i = fragment.nextSetBit(0); i > -1 && i < n; i = fragment.nextSetBit(i + 1)) {
            if (vars[i].contains(bestSolution[i])) {
                vars[i].instantiateTo(bestSolution[i], this);
            }
        }
        solver.propagate();

        logSum = 0;
        for (int i = 0; i < n; i++) {
            logSum += Math.log(vars[i].getDomainSize());
        }
        epsilon = (.95 * epsilon) + (.05 * (logSum / cste));
    }

    private void random() throws ContradictionException {
        fragment.set(0, n); // all variables are frozen
        for (int i = 0; i < nbFixedVariables; i++) {
            int id = selectVariable();
            if (vars[id].contains(bestSolution[id])) {  // to deal with objective variable and related
                vars[id].instantiateTo(bestSolution[id], this);
                fragment.clear(id);
            } else {
//                i--;
                fragment.clear(id);
            }
//            System.out.println(fragment.cardinality());
        }
    }

    private int selectVariable() {
        int id;
        if (candidate.isEmpty()) {
            int cc = rd.nextInt(fragment.cardinality());
            for (id = fragment.nextSetBit(0); id >= 0 && cc > 0; id = fragment.nextSetBit(id + 1)) {
                cc--;
            }
        } else {
            id = candidate.firstKey();
            candidate.remove(id);
        }
        return id;
    }

    @Override
    protected void restrictLess() {
        if (fairness % 3 == 2) {
            nbFixedVariables = 0;
        }
    }

    @Override
    public void beforeInitialPropagation() {
    }

    @Override
    public void afterInitialPropagation() {
        for (int i = 0; i < n; i++) {
            dsize[i] = vars[i].getDomainSize();
        }
    }
}
