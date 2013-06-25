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
package solver.explanations.strategies;

import gnu.trove.list.array.TIntArrayList;
import solver.ICause;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.*;
import solver.explanations.antidom.AntiDomain;
import solver.objective.ObjectiveManager;
import solver.search.loop.lns.neighbors.INeighbor;
import solver.search.loop.monitors.IMonitorInitPropagation;
import solver.search.loop.monitors.IMonitorInitialize;
import solver.search.loop.monitors.IMonitorRestart;
import solver.search.loop.monitors.IMonitorUpBranch;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.RootDecision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.variables.IntVar;
import util.iterators.DisposableValueIterator;

import java.util.*;

/**
 * @author Charles Prud'homme
 * @since 01/10/12
 */
public class ExplainedNeighborhood implements INeighbor, IMonitorInitPropagation, IMonitorUpBranch, IMonitorInitialize, IMonitorRestart {


    private static final int A = 0; // A: activated
    private static final int R = 1; // R: refuted
    private static final int O = 2; // O: involving the objective variable

    protected final Solver solver;
    protected ExplanationEngine mExplanationEngine;
    private ObjectiveManager om;
    private IntVar objective;
    private int LB, UB;
    private Decision last;

    // cluster mode on

    // decision path that leads to a solution
    private ArrayList<Decision> path;


    // list of decisions related to the explanation of the objective variable
    private final ArrayList<Decision> valueDecisions;
    // list of index of clusters
    private final TIntArrayList clusters;
    // current cluster treated
    private int cluster;
    // index of the decision currently applied, and the nb of decision already applied
    private int curIdx, nbDecApplied;
    // list of decisions related to the exception, if any
    private final ArrayList<Decision> exceptionDecisions;
    //
    private int idExDec;

    // various status of the decisions
    private BitSet[] decisions;
    // for restrict algo, do we fail applying the fragment
    private boolean applyFgmt;

    private final int n;
    private final IntVar[] vars;
    private final int[] bestSolution;

    // TEMPORARY DATA STRUCTURES
    private final ArrayList<Deduction> tmpDeductions;
    private final Set<Deduction> tmpValueDedutions;


    private final Random random;
    private boolean inRndMode;
    private double epsilon = 1.;
    private int nbFixedVars;

    int RR = 0, DD = 0;

    private final Policy policy;

    public static enum Policy {
        BACKTRACK, CONFLICT, RANDOM
    }

    public ExplainedNeighborhood(Solver solver, Policy policy, long seed) {
        this.solver = solver;
        this.policy = policy;
        this.random = new Random(seed);

        n = solver.getNbVars();
        vars = new IntVar[n];
        for (int k = 0; k < n; k++) {
            vars[k] = (IntVar) solver.getVar(k);
        }
        bestSolution = new int[n];
        nbFixedVars = n / 2;

        ExplanationFactory.LAZY.plugin(solver, false);
        this.mExplanationEngine = solver.getExplainer();

        path = new ArrayList<Decision>(16);
        valueDecisions = new ArrayList<Decision>(16);
        clusters = new TIntArrayList(16);
        decisions = new BitSet[3];
        for (int i = 0; i < 3; i++) {
            decisions[i] = new BitSet(16);
        }
        exceptionDecisions = new ArrayList<Decision>(16);

        // TEMPORARY DATA STRUCTURES
        tmpDeductions = new ArrayList<Deduction>(16);
        tmpValueDedutions = new HashSet<Deduction>(16);
        inRndMode = false;

        solver.getSearchLoop().plugSearchMonitor(this);
    }

    @Override
    public void beforeInitialize() {
    }

    @Override
    public void afterInitialize() {
        AbstractStrategy cstrat = solver.getSearchLoop().getStrategy();
        solver.set(new StrategiesSequencer(
                new Strategy(), cstrat
        ));
    }

    @Override
    public void beforeInitialPropagation() {
        // nothing to do
    }

    @Override
    public void afterInitialPropagation() {
        om = mExplanationEngine.getSolver().getSearchLoop().getObjectivemanager();
        objective = om.getObjective();
        LB = objective.getLB();
        UB = objective.getUB();
    }


    private int selectVariable() {
        int id;
        int cc = random.nextInt(bestSolution.length);
        for (id = 0; id >= 0 && cc > 0; id++) {
            cc--;
        }
        return id;
    }

    @Override
    public void beforeRestart() {
    }

    @Override
    public void afterRestart() {
        last = null;
        nbDecApplied = curIdx = 0;

        if (applyFgmt && !inRndMode) {
            RR++;
            restrictLess();
        } else {
            DD++;
            switch (policy) {
                case BACKTRACK:
                    if (decisions[A].cardinality() > 0 && !inRndMode) {
                        restrictLess();
                    } else {
                        epsilon = inRndMode ? epsilon * 1.02 : 1.;
                        inRndMode = true;
                        random();
                    }
                    break;
                case CONFLICT:
//                    conflict();
                    if (decisions[A].cardinality() > 0 && !inRndMode) {
                        conflict();
                    } else {
                        epsilon = inRndMode ? epsilon * 1.02 : 1.;
                        inRndMode = true;
                        random();
                    }
                    break;
                case RANDOM:
                    epsilon = inRndMode ? epsilon * 1.02 : 1.;
                    inRndMode = true;
                    random();
                    break;
            }
        }
    }

    private void conflict() {
        if (path.size() > 0 && exceptionDecisions.isEmpty()) {
            explainCut();
            int id = path.indexOf(exceptionDecisions.get(idExDec++));
            decisions[A].clear(id);
        } else if (idExDec < exceptionDecisions.size()) {
            int id = path.indexOf(exceptionDecisions.get(idExDec - 1));
            decisions[A].set(id);
            id = path.indexOf(exceptionDecisions.get(idExDec++));
            decisions[A].clear(id);
        } else {
            restrictLess();
        }
    }

    private void random() {
        if (path.size() > 0) {
            FastDecision d0 = (FastDecision) path.get(0);
            int cste = (int) ((2 * n) / (3 * epsilon)) - 1;
            path.clear();
            for (int i = 0; i < cste; i++) {
                int id = selectVariable();
                if (vars[id].contains(bestSolution[id])) {  // to deal with objective variable and related
                    FastDecision d = (FastDecision) d0.duplicate();
                    d.set(vars[id], bestSolution[id], DecisionOperator.int_eq);
                    path.add(d);
                }
            }
            nbFixedVars = path.size();
            decisions[A].clear();
            decisions[A].set(0, path.size());
            decisions[O].clear();
            decisions[R].clear();
        }
    }


    @Override
    public boolean isSearchComplete() {
        return decisions[A].cardinality() == 0 || (policy == Policy.BACKTRACK && nbFixedVars == 0);
    }

    @Override
    public void recordSolution() {
//        System.out.printf("%d - %d\n", RR, DD);
        for (int i = 0; i < vars.length; i++) {
            bestSolution[i] = vars[i].getValue();
        }

        // 1. clear data structures
        tmpDeductions.clear();
        valueDecisions.clear();
        exceptionDecisions.clear();
        clusters.clear();
        path.clear();
        decisions[A].clear();
        decisions[O].clear();
        decisions[R].clear();


        // 2. get anti domain of the objective variable
        readAntiDomain();

        // 3. re-build the clusters if required
        buildCluster();

        // 4. store the decisions related to the objective variable
        for (int i = 0; i < tmpDeductions.size(); i++) {
            valueDecisions.add(((BranchingDecision) tmpDeductions.get(i)).getDecision());
        }

        // 5. compute the first fragment to apply
        computeFragment();

        // 6. prepare the next fragment heuristic
        cluster = 1;
        epsilon = 1.;
        // for the restart:
        applyFgmt = true;
        inRndMode = false;
    }


    @Override
    public void fixSomeVariables(ICause cause) throws ContradictionException {
        // nothing to do, every thing is done with the delegate strategy
    }

    @Override
    public void restrictLess() {
        chooseNext();
        nbDecApplied = curIdx = 0;
    }

    @Override
    public void beforeUpBranch() {
    }

    @Override
    public void afterUpBranch() {
        if (applyFgmt) {
            solver.getSearchLoop().restart();
        } else if (last != null && solver.getSearchLoop().decision.getId() == last.getId()) {
            // if we close the subtree
            if (policy != Policy.BACKTRACK) {
                solver.getSearchLoop().restart();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Iterate over removed values to explain the objective variable state
     */
    private void readAntiDomain() {
        AntiDomain adObj = mExplanationEngine.getRemovedValues(objective);
        DisposableValueIterator it = adObj.getValueIterator();
        clusters.add(0);
        // 2'. compute bounds to avoid explaining the whole domain
        boolean ismax = om.getPolicy() == ResolutionPolicy.MAXIMIZE;
        int far, near;
        if (ismax) {
            far = UB;
            near = objective.getValue() + 1;
        } else {
            far = LB;
            near = objective.getValue() - 1;
        }


        int value;
        if (ismax) {
            while (it.hasNext() && it.next() < near) {
            }
            while (it.hasNext() && (value = it.next()) < far) {
                explainValue(value);
            }
        } else {
            while (it.hasNext() && (value = it.next()) <= near) {
                explainValue(value);
            }
        }
        it.dispose();

    }


    /**
     * Build the cluster, for restrict less operation
     */
    private void buildCluster() {
        if (clusters.size() > 1) {
            int one = clusters.get(1);
            clusters.clear();
            clusters.add(0);
            clusters.add(one);
            for (int i = one + 1; i < tmpDeductions.size(); i++) {
                clusters.add(i);
            }
        }
    }


    /**
     * Explain the removal of value from the objective variable
     *
     * @param value value to explain
     */
    private void explainValue(int value) {
        tmpValueDedutions.clear();

        Explanation explanation = new Explanation();
        objective.explain(VariableState.REM, value, explanation);
        explanation = mExplanationEngine.flatten(explanation);
        extractDecision(explanation, tmpValueDedutions);

        assert tmpValueDedutions.size() > 0 : "E(" + value + ") is EMPTY";
        boolean correct = tmpValueDedutions.removeAll(tmpDeductions);
//        assert tmpDeductions.size() == 0 || correct : "E(" + value + ") not INCLUDED in previous ones";
        if (tmpDeductions.addAll(tmpValueDedutions)) {
            clusters.add(tmpDeductions.size());
        }
    }

    /**
     * Extract decision from the explanation
     *
     * @param explanation the explanation
     * @param decisions   a set of decisions
     */
    private void extractDecision(Explanation explanation, Set<Deduction> decisions) {
        decisions.clear();
        if (explanation.nbDeductions() > 0) {
            for (int d = 0; d < explanation.nbDeductions(); d++) {
                Deduction dec = explanation.getDeduction(d);
                if (dec.getmType() == Deduction.Type.DecLeft) {
                    decisions.add(dec);
                }
            }
        }
    }

    /**
     * Compute the initial fragment, ie set of decisions to keep.
     */
    private void computeFragment() {
        Decision dec = solver.getSearchLoop().decision;
        while ((dec != RootDecision.ROOT)) {
            addToPath(dec);
            dec = dec.getPrevious();
        }
        int size = path.size();
        for (int i = 0, mid = size >> 1, j = size - 1; i < mid; i++, j--) {
            Decision di = path.get(i);
            Decision dj = path.get(j);

            path.set(i, dj);
            path.set(j, di);


            for (int k = 0; k < 3; k++) {

                boolean bi = decisions[k].get(i);
                boolean bj = decisions[k].get(j);
                decisions[k].set(i, bj);
                decisions[k].set(j, bi);

            }
        }

        assert path.size() - 1 == decisions[A].previousSetBit(path.size() * 2);
    }


    /**
     * Add a copy of the current decision to path
     *
     * @param dec a decision of the current decision path
     */
    private void addToPath(Decision dec) {
        Decision clone = dec.duplicate();

        path.add(clone);
        int pos = path.size() - 1;
        boolean forceNext = !dec.hasNext();
        if (forceNext) {
            clone.buildNext(); // hack
            decisions[R].set(pos);
        }
        if (dec.getDecisionVariable().getId() != objective.getId()) {
            decisions[O].set(pos);
        }
        decisions[A].set(pos);
        int idx = valueDecisions.indexOf(dec);
        if (idx > -1) valueDecisions.set(idx, clone);
    }

////

    private class Strategy extends AbstractStrategy<IntVar> {

        protected Strategy() {
            super(new IntVar[0]);
        }

        @Override
        public void init() throws ContradictionException {
        }

        @Override
        public Decision<IntVar> getDecision() {
            Decision d = null;
            if (nbDecApplied < decisions[A].cardinality()) {
                curIdx = decisions[A].nextSetBit(curIdx);
                if (curIdx > -1) {
                    d = path.get(curIdx++);
                    nbDecApplied++;
                    d = d.duplicate();
                    d.setWorldIndex(solver.getEnvironment().getWorldIndex() - 1);
                    last = d;
                }
            }
            applyFgmt = d != null;
            return d;
        }

    }


    /**
     * Compute the next decisions to relax
     */
    private void chooseNext() {
        if (cluster < clusters.size()) {
            int k = cluster++;
            for (int i = clusters.get(k - 1); i < clusters.get(k); i++) {
                Decision dec = valueDecisions.get(i);
                int idx = path.indexOf(dec);
                decisions[A].clear(idx);
            }
        } else {
            int idx = decisions[A].previousSetBit(path.size());
            if (idx > -1) {
                decisions[A].clear(idx);
            }
        }
        // then desactivate refuted decisions
        int firstClear = decisions[A].nextClearBit(0);
        for (int i = decisions[R].nextSetBit(0); i > -1 && i < firstClear && firstClear > -1; i = decisions[R].nextSetBit(i + 1)) {
            if (!decisions[O].get(i)) {
                decisions[A].set(i);
            }
        }
        for (int j = decisions[R].nextSetBit(firstClear); j > -1; j = decisions[R].nextSetBit(j + 1)) {
            decisions[A].clear(j);
        }

        assert path.size() - 1 >= decisions[A].previousSetBit(path.size() * 2);
    }

    /**
     * Force the failure, apply decisions to the last solution + cut => failure!
     */
    private void explainCut() {
        // Goal: force the failure to get the set of decisions related to the cut

        // 1. make a backup
        solver.getEnvironment().worldPush();
        Decision d;
        try {

            Decision previous = solver.getSearchLoop().decision;
            assert previous == RootDecision.ROOT;
            // 2. apply the decisions
            mExplanationEngine.getSolver().getSearchLoop().getObjectivemanager().postDynamicCut();
            for (int i = 0; i < path.size(); i++) {
                d = path.get(i);
                nbDecApplied++;
                d.buildNext();
                d.apply();
                d.setPrevious(previous);
                solver.propagate();
                previous = d;
            }
            //solver.propagate();
            assert false : "SHOULD FAIL!";
        } catch (ContradictionException cex) {
            if ((cex.v != null) || (cex.c != null)) { // contradiction on domain wipe out
                tmpDeductions.clear();
                tmpValueDedutions.clear();
                exceptionDecisions.clear(); // useless but... you know...

                // 3. explain the failure
                Explanation expl = new Explanation();
                if (cex.v != null) {
                    cex.v.explain(VariableState.DOM, expl);
                } else {
                    cex.c.explain(null, expl);
                }
                Explanation complete = mExplanationEngine.flatten(expl);
                extractDecision(complete, tmpValueDedutions);
                tmpDeductions.addAll(tmpValueDedutions);

                assert tmpDeductions.size() > 0 : "woo... if this is empty, that's strange...";

                for (int i = 0; i < tmpDeductions.size(); i++) {
                    exceptionDecisions.add(((BranchingDecision) tmpDeductions.get(i)).getDecision());
                }

                // 4. need to replace the duplicate decision with the correct one
                for (int i = 0; i < path.size(); i++) {
                    Decision dec = path.get(i);
                    boolean forceNext = !dec.hasNext();
                    dec.rewind();
                    if (forceNext) dec.buildNext();
                    dec.setPrevious(null); // useless .. but ... you know
                }

            } else {
                throw new UnsupportedOperationException(this.getClass().getName() + ".onContradiction incoherent state");
            }
        }
        solver.getEnvironment().worldPop();

        solver.getEngine().flush();

        idExDec = 0;
    }
}
