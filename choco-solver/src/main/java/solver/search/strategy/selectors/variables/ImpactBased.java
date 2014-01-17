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
package solver.search.strategy.selectors.variables;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import memory.IStateDouble;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.search.loop.monitors.IMonitorContradiction;
import solver.search.loop.monitors.IMonitorDownBranch;
import solver.search.loop.monitors.IMonitorRestart;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.Variable;
import util.PoolManager;
import util.iterators.DisposableValueIterator;

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
public class ImpactBased extends AbstractStrategy<IntVar> implements IMonitorDownBranch, IMonitorRestart,
        IMonitorContradiction, ICause {

    protected final int aging; // aging parameter
    protected double[][] Ilabel; // impact per labeling
    protected int[] offsets; // initial lower bound of each variable
    protected int split; // domains are divided into at most 2^s subdomains
    protected IStateDouble searchSpaceSize;

    protected int currentVar = -1, currentVal = -1;

    TIntList bests = new TIntArrayList();

    java.util.Random random; //  a random object to break ties

    protected int nodeImpact;

    PoolManager<FastDecision> decisionPool;

    protected Solver solver;

    protected boolean asgntFailed; // does the assignment leads to a failure

    protected boolean learnsAndFails; // does the learning pahse leads to a failure
    protected IntVar lAfVar; // the index of one of the variables involved into a failure during the learning phase

    protected long timeLimit = Integer.MAX_VALUE; // a time limit for init()

    /**
     * Create an Impact-based search strategy with Node Impact strategy.
     * <p/>
     * <b>"Impact-Based Search Strategies for Constraint Programming",
     * Philippe Refalo, CP2004.</b>
     *
     * @param ivariables variables of the problem (should be integers)
     * @param alpha      aging parameter
     * @param split      split parameter for subdomains computation
     * @param nodeImpact force update of impacts every <code>nodeImpact</code> nodes. Set value to 0 to avoid using it.
     * @param seed       a seed for random
     * @param initOnly   only apply the initialisation phase, do not update impact thereafter
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
        if (!initOnly) solver.plugMonitor(this);
    }

    @Override
    public Decision<IntVar> computeDecision(IntVar variable) {
        if (variable == null || variable.instantiated()) {
            return null;
        }
        if (currentVar == -1 || vars[currentVar] != variable) {
            // retrieve indice of the variable in vars
			for(int i=0;i<vars.length;i++){
				if(vars[i]==variable){
					currentVar = i;
				}
			}
            assert vars[currentVar] == variable;
        }
        bests.clear();
        double bestImpact = 1.0;
        if (variable.hasEnumeratedDomain()) {
            DisposableValueIterator it = variable.getValueIterator(true);
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
            int lb = variable.getLB();
            int ub = variable.getUB();
            currentVal = random.nextBoolean() ? lb : ub;
        }

        FastDecision currrent = decisionPool.getE();
        if (currrent == null) {
            currrent = new FastDecision(decisionPool);
        }
        currrent.set(variable, currentVal, DecisionOperator.int_eq);
        //System.out.printf("D: %d, %d: %s\n", currentVar, currentVal, best);
        return currrent;
    }

    @Override
    public Decision<IntVar> getDecision() {
        IntVar best = null;
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
            currentVar = bests.get(random.nextInt(bests.size()));
            best = vars[currentVar];
        }
        return computeDecision(best);
    }

    public void setTimeLimit(long timeLimit) {
        if (timeLimit > -1) {
            this.timeLimit = timeLimit;
        }
    }

    @Override
    public void init() throws ContradictionException {
        long tl = System.currentTimeMillis() + this.timeLimit;
        // 0. Data structure construction
        Ilabel = new double[vars.length][];
        offsets = new int[vars.length];
        // 1. Estimation of assignment and variable impacts
        double before = searchSpaceSize();
        searchSpaceSize.set(before);
        learnsAndFails = false;
        loop:
        for (int i = 0; i < vars.length; i++) {
            IntVar v = vars[i];
            int offset = v.getLB();
            int UB = v.getUB();
            int dsz = UB - offset + 1;//v.getDomainSize();
            if (!v.instantiated()) { // if the variable is not instantiated
                Ilabel[i] = new double[v.hasEnumeratedDomain() ? dsz : 1];
                offsets[i] = offset;

                if (v.hasEnumeratedDomain()) {
                    if (v.getDomainSize() < split) { // try each value
                        DisposableValueIterator it = v.getValueIterator(true);
                        while (it.hasNext()) {
                            if (System.currentTimeMillis() > tl) {
                                break loop;
                            }
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
                            if (System.currentTimeMillis() > tl) {
                                break loop;
                            }
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
                    if (System.currentTimeMillis() > tl) {
                        break;
                    }
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
            solver.getEngine().fails(this, lAfVar, "Impact::init:: detect failures");
        } else if (System.currentTimeMillis() > tl) {
            LOGGER.debug("ImpactBased Search stops its init phase -- reach time limit!");
            for (int i = 0; i < vars.length; i++) {  // create arrays to avoid null pointer errors
                IntVar v = vars[i];
                int offset = v.getLB();
                int UB = v.getUB();
                int dsz = UB - offset + 1;//v.getDomainSize();
                if (!v.instantiated() && Ilabel[i] == null) {
                    Ilabel[i] = new double[v.hasEnumeratedDomain() ? dsz : 1];
                    offsets[i] = offset;
                }
            }
        }
    }


    @Override
    public void beforeDownLeftBranch() {
    }

    @Override
    public void afterDownLeftBranch() {
        if (currentVar > -1) { // if the decision was computed by another strategy
            if (asgntFailed) {
                updateImpact(1.0d, currentVar, currentVal);
            } else {
                double sssz = searchSpaceSize();
                updateImpact(sssz / searchSpaceSize.get(), currentVar, currentVal);
                searchSpaceSize.set(sssz);
            }
            currentVar = -1;
        }
        asgntFailed = false; // to handle cases where a contradiction was thrown, but the decision was computed outside
        reevaluateImpact();
    }

    @Override
    public void beforeDownRightBranch() {
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
            v.instantiateTo(a, this);
            solver.getEngine().propagate();
            after = searchSpaceSize();
            solver.getEnvironment().worldPop();
            return 1.0d - (after / before);
        } catch (ContradictionException e) {
            solver.getEngine().flush();
            solver.getEnvironment().worldPop();
            // if the value leads to fail, then the value can be removed from the domain
            try {
                v.removeValue(a, this);
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
        if (size == Double.POSITIVE_INFINITY) {
            size = Double.MAX_VALUE;
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
                //noinspection ThrowableResultOfMethodCallIgnored
                solver.getSearchLoop().smList.onContradiction(
                        solver.getEngine().getContradictionException().set(this, lAfVar, "Impact::reevaluate:: detect failures")
                );
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void explain(Deduction d, Explanation e) {
        // the current deduction is due to the current domain of the involved variables
        for (Variable v : this.vars) {
            v.explain(VariableState.DOM, e);
        }
    }

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
    }
}
