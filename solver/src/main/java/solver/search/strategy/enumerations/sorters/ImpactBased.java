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
package solver.search.strategy.enumerations.sorters;

import choco.kernel.common.util.PoolManager;
import choco.kernel.common.util.iterators.DisposableValueIterator;
import choco.kernel.memory.IStateDouble;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import solver.Cause;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.search.loop.monitors.ISearchMonitor;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.Variable;

import java.util.Random;

/**
 * Implementation of the search described in:
 * "Impact-Based Search Strategies for Constraint Programming",
 * Philippe Refalo, CP2004.
 * <br/>
 * <p/>
 * Impacts of variables are first computed on initialisation, (only 3 values are tested for bounded variables);
 * Ties are broken randomly.
 *
 * @author Charles Prud'homme
 * @since 21/09/12
 */
public class ImpactBased extends AbstractStrategy<IntVar> implements ISearchMonitor, ICause {

    protected final int aging; // aging parameter
    protected double[][] Ilabel; // impact per labeling
    protected int[] offsets; // initial lower bound of each variable
    protected int split; // domains are divided into at most 2^s subdomains
    protected IStateDouble searchSpaceSize;

    protected int currentVar, currentVal;

    TIntList bests = new TIntArrayList();

    java.util.Random random; //  a random object to break ties

    protected int nodeImpact;

    PoolManager<FastDecision> decisionPool;

    protected Solver solver;

    protected boolean asgntFailed; // does the assignment leads to a failure

    protected boolean learnsAndFails; // does the learning pahse leads to a failure
    protected IntVar lAfVar; // the index of one of the variables involved into a failure during the learning phase

    IntVar trick;

    /**
     * Create an Impact-based search strategy with Node Impact strategy.
     * <p/>
     * <b>"Impact-Based Search Strategies for Constraint Programming",
     * Philippe Refalo, CP2004.</b>
     *
     * @param ivariables variables of the problem (should be integers)
     * @param alpha      aging paramter
     * @param split      split paramater for dubdomains computation
     * @param nodeImpact force update of impacts every <code>nodeImpact</code> nodes. Set value to 0 to avoid using it.
     * @param seed       a seed for random
     * @param initOnly   only apply the initialisation phase, do not update impacte therafter
     */
    public ImpactBased(IntVar[] ivariables, int alpha, int split, int nodeImpact, long seed, boolean initOnly) { //TODO: node impacts
        super(ivariables);
        this.solver = ivariables[0].getSolver();
        this.aging = alpha;
        this.split = (int) Math.pow(2, split);
        this.searchSpaceSize = solver.getEnvironment().makeFloat();
        random = new Random(seed);
        decisionPool = new PoolManager<FastDecision>();
        this.nodeImpact = nodeImpact;
        if (!initOnly) solver.getSearchLoop().plugSearchMonitor(this);
    }

    @Override
    public Decision getDecision() {
        // 1. first select the variable with the largest impact
        bests.clear();
        double bestImpact = -Double.MAX_VALUE;
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].instantiated()) {
                double imp = computeImpact(i);
                if (imp > bestImpact) {
                    bests.clear();
                    bests.add(i);
                    bestImpact = imp;
                } else if (imp == bestImpact) {
                    bests.add(i);
                }
            }
        }
        if (bests.size() > 0) {
            // 2. select the variable
            IntVar best = trick;
            if (!Configuration.STORE_LAST_DECISION_VAR || (trick == null || trick.instantiated())) {
                currentVar = bests.get(random.nextInt(bests.size()));
                best = vars[currentVar];
                trick = best;
            }

            // 3. then iterate over values
            bests.clear();
            bestImpact = 1.0;
            if (best.hasEnumeratedDomain()) {
                DisposableValueIterator it = best.getValueIterator(true);
                int o = offsets[currentVar];
                while (it.hasNext()) {
                    int val = it.next();
                    double impact = Ilabel[currentVar][val - o];
                    if (impact < bestImpact) {
                        bests.clear();
                        bests.add(val);
                        bestImpact = impact;
                    } else if (impact == bestImpact) {
                        bests.add(val);
                    }
                }
                it.dispose();

                currentVal = bests.get(random.nextInt(bests.size()));
            } else {
                int lb = best.getLB();
                int ub = best.getUB();
                currentVal = random.nextBoolean() ? lb : ub;
            }

            FastDecision currrent = decisionPool.getE();
            if (currrent == null) {
                currrent = new FastDecision(decisionPool);
            }
            currrent.set(best, currentVal, DecisionOperator.int_eq);
            //            System.out.printf("D: %d, %d: %s\n", currentVar, currentVal, best);
            return currrent;
        } else {
            return null;
        }
    }

    @Override
    public void init() throws ContradictionException {
        // TIME LIMIT??
        // 0. Data structure construction
        Ilabel = new double[vars.length][];
        offsets = new int[vars.length];
        // 1. Estimation of assignment and variable impacts
        double before = searchSpaceSize();
        searchSpaceSize.set(before);
        learnsAndFails = false;
        for (int i = 0; i < vars.length; i++) {
            IntVar v = vars[i];
            int dsz = v.getDomainSize();
            if (!v.instantiated()) { // if the variable is not instantiated
                int offset = v.getLB();
                Ilabel[i] = new double[v.hasEnumeratedDomain() ? dsz : 1];
                offsets[i] = offset;

                if (v.hasEnumeratedDomain()) {
                    if (v.getDomainSize() < split) { // try each value
                        DisposableValueIterator it = v.getValueIterator(true);
                        while (it.hasNext()) {
                            int a = it.next();
                            double im = computeImpact(v, a, before);
                            Ilabel[i][a - offset] = im;
                        }
                        it.dispose();
                    } else { // estimate per subdomains
                        int step = 0;
                        int size = dsz / split;
                        DisposableValueIterator it = v.getValueIterator(true);
                        while (it.hasNext()) {
                            int a = it.next();
                            double im;
                            if (step % size == 0) {
                                im = computeImpact(v, a, before);
                            } else {
                                im = Ilabel[i][a - 1 - offset];
                            }
                            Ilabel[i][a - offset] = im;
                            step++;
                        }
                        it.dispose();
                    }
                } else {
                    // A. choose 3 values in the domain to have an estimation of the impact
                    double i1 = computeImpact(v, v.getLB(), before);
                    double i2 = computeImpact(v, v.getUB(), before);
                    double i3 = computeImpact(v, (v.getLB() + v.getUB()) / 2, before);
                    Ilabel[i][0] = (i1 + i2 + i3) / 3d;
                }
            }
        }
        if (learnsAndFails) {
            // If the initialisation detects a failure, then the problem has no solution!
            learnsAndFails = false;
            throw solver.getEngine().getContradictionException().set(this, lAfVar, "Impact::init:: detect failures");
        }
    }


    @Override
    public void afterDownLeftBranch() {
        if (asgntFailed) {
            updateImpact(1.0d, currentVar, currentVal);
            asgntFailed = false;
        } else {
            double sssz = searchSpaceSize();
            updateImpact(sssz / searchSpaceSize.get(), currentVar, currentVal);
            searchSpaceSize.set(sssz);
        }
        reevaluateImpact();
    }

    @Override
    public void afterDownRightBranch() {
        reevaluateImpact();
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        asgntFailed = true;
    }

    /**
     * Compute the impact of a <b>variable</b>
     *
     * @param idx index of the variable
     * @return the impact of the variable idx
     */
    protected double computeImpact(int idx) {
        IntVar var = vars[idx];
        if (var.hasEnumeratedDomain()) {
            int of = offsets[idx];
            DisposableValueIterator it = var.getValueIterator(true);
            double impact = 0.0;
            while (it.hasNext()) {
                int val = it.next();
                impact += Ilabel[idx][val - of];
            }
            it.dispose();
            return impact - var.getDomainSize();
        } else {
            return Ilabel[idx][0] - var.getDomainSize();
        }
    }

    /**
     * Compute the impact of an <b>assignment</b>
     *
     * @param v      the variable
     * @param a      the value
     * @param before search space size before the assignment
     * @return the impact I(v = a)
     */
    private double computeImpact(IntVar v, int a, double before) {
        solver.getEnvironment().worldPush();
        double after;
        try {
            v.instantiateTo(a, Cause.Null);
            solver.getEngine().propagate();
            after = searchSpaceSize();
            solver.getEnvironment().worldPop();
            return 1.0d - (after / before);
        } catch (ContradictionException e) {
            solver.getEngine().flush();
            solver.getEnvironment().worldPop();
            // if the value leads to fail, then the value can be removed from the domain
            try {
                v.removeValue(a, Cause.Null);
                solver.getEngine().propagate();
            } catch (ContradictionException ex) {
                learnsAndFails = true;
                lAfVar = v;
                solver.getEngine().flush();
            }
            return 1.0d;
        }
    }

    /**
     * Update the impact of an assignment I(v=a)
     *
     * @param nImpact new impact
     * @param varIdx  index of the variable
     * @param valIdx  index of the value
     */
    protected void updateImpact(double nImpact, int varIdx, int valIdx) {
        valIdx = Ilabel[varIdx].length > 1 ? valIdx - offsets[varIdx] : 0;
        double impact = Ilabel[varIdx][valIdx] * (aging - 1);
        impact += nImpact;
        impact /= aging;
        Ilabel[varIdx][valIdx] = impact;
    }

    /**
     * Compute the search space size
     *
     * @return search space size
     */
    protected double searchSpaceSize() {
        double size = 1;
        for (int i = 0; i < vars.length; i++) {
            size *= vars[i].getDomainSize();
            assert size > 0 : "Search space is not correct!";
        }
        return size;
    }

    protected void reevaluateImpact() {
        if (nodeImpact > 0 && solver.getMeasures().getNodeCount() % nodeImpact == 0) {
            double before = searchSpaceSize.get();
            learnsAndFails = false;
            for (int i = 0; i < vars.length; i++) {
                IntVar v = vars[i];
                int dsz = v.getDomainSize();
                if (!v.instantiated()) { // if the variable is not instantiated
                    int offset = v.getLB();
                    if (v.hasEnumeratedDomain()) {
                        if (v.getDomainSize() < split) { // try each value
                            DisposableValueIterator it = v.getValueIterator(true);
                            while (it.hasNext()) {
                                int a = it.next();
                                double im = computeImpact(v, a, before);
                                updateImpact(im, i, a);
                            }
                            it.dispose();
                        } else { // estimate per subdomains
                            int step = 0;
                            int size = dsz / split;
                            DisposableValueIterator it = v.getValueIterator(true);
                            while (it.hasNext()) {
                                int a = it.next();
                                double im;
                                if (step % size == 0) {
                                    im = computeImpact(v, a, before);
                                } else {
                                    im = Ilabel[i][a - 1 - offset];
                                }
                                updateImpact(im, i, a);
                                step++;
                            }
                            it.dispose();
                        }
                    } else {
                        // A. choose 3 values in the domain to have an estimation of the impact
                        double i1 = computeImpact(v, v.getLB(), before);
                        double i2 = computeImpact(v, v.getUB(), before);
                        double i3 = computeImpact(v, (v.getLB() + v.getUB()) / 2, before);
                        updateImpact((i1 + i2 + i3) / 3d, i, 0);
                    }
                }
            }
            if (learnsAndFails) {
                learnsAndFails = false;
                solver.getSearchLoop().moveTo(solver.getSearchLoop().stateAfterFail);
                solver.getSearchLoop().smList.onContradiction(
                        solver.getEngine().getContradictionException().set(this, lAfVar, "Impact::reevaluate:: detect failures")
                );
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void beforeInitialize() {
    }

    @Override
    public void afterInitialize() {
    }

    @Override
    public void beforeInitialPropagation() {
    }

    @Override
    public void afterInitialPropagation() {
    }

    @Override
    public void beforeOpenNode() {
    }

    @Override
    public void afterOpenNode() {
    }

    @Override
    public void onSolution() {
    }

    @Override
    public void beforeDownLeftBranch() {
    }

    @Override
    public void beforeDownRightBranch() {
    }

    @Override
    public void beforeUpBranch() {
    }

    @Override
    public void afterUpBranch() {
    }

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
    }

    @Override
    public void afterInterrupt() {
    }

    @Override
    public void beforeClose() {
    }

    @Override
    public void afterClose() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public Constraint getConstraint() {
        return null;
    }

    @Override
    public Explanation explain(Deduction d) {
        Explanation expl = Explanation.build();
        // the current deduction is due to the current domain of the involved variables
        for (Variable v : this.vars) {
            expl.add(v.explain(VariableState.DOM));
        }
        return expl;
    }

    @Override
    public boolean reactOnPromotion() {
        return false;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return 0;
    }
}
