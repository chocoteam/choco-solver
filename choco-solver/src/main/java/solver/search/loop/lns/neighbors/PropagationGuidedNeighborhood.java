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
package solver.search.loop.lns.neighbors;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.IMonitorInitPropagation;
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
public class PropagationGuidedNeighborhood extends ANeighbor implements IMonitorInitPropagation {

    protected final int n;
    protected final IntVar[] vars;
    protected final int[] bestSolution;
    protected int[] dsize;
    protected Random rd;
    protected int fgmtSize = 100;
    protected int listSize;
    protected double epsilon = 1.;
    protected double logSum = 0.;

    protected SortedMap<Integer, Integer> all;
    protected SortedMap<Integer, Integer> candidate;
    BitSet fragment;  // index of variable to set unfrozen


    public PropagationGuidedNeighborhood(Solver solver, IntVar[] vars, long seed, int fgmtSize, int listSize) {
        super(solver);

        this.n = vars.length;
        this.vars = vars.clone();

        this.rd = new Random(seed);
        this.bestSolution = new int[n];
        this.fgmtSize = fgmtSize;
        this.listSize = listSize;

        this.all = new TreeMap<Integer, Integer>();
        this.candidate = new TreeMap<Integer, Integer>();
        this.fragment = new BitSet(n);
        solver.getSearchLoop().plugSearchMonitor(this);
    }

    @Override
    public boolean isSearchComplete() {
        return false;
    }

    @Override
    public void recordSolution() {
        for (int i = 0; i < vars.length; i++) {
            bestSolution[i] = vars[i].getValue();
        }
    }

    @Override
    public void fixSomeVariables(ICause cause) throws ContradictionException {
        logSum = 0.;
        for (int i = 0; i < n; i++) {
            int ds = vars[i].getDomainSize();
            logSum += Math.log(ds);
        }
        fgmtSize = (int) (30 * (1 + epsilon));
        fragment.set(0, n); // all variables are frozen
        update(cause);
        epsilon = (.95 * epsilon) + (.05 * (logSum / fgmtSize));
    }

    protected void update(ICause cause) throws ContradictionException {
        while (logSum > fgmtSize && fragment.cardinality() > 0) {
            all.clear();
            // 1. pick a variable
            int id = selectVariable();

            // 2. fix it to its solution value
            if (vars[id].contains(bestSolution[id])) {  // to deal with objective variable and related
                vars[id].instantiateTo(bestSolution[id], cause);
                mSolver.propagate();
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
                while (!all.isEmpty() && candidate.size() < listSize) {
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
    }

    protected int selectVariable() {
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
    public void restrictLess() {
//        System.out.printf("%.3f\n", epsilon);
    }

    @Override
    public void beforeInitialPropagation() {
    }

    @Override
    public void afterInitialPropagation() {
        // todo plug search monitor
        this.dsize = new int[n];
        for (int i = 0; i < n; i++) {
            dsize[i] = vars[i].getDomainSize();
        }
    }
}
