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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.nary.nogood.NogoodStoreForRestarts;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.explanations.*;
import solver.explanations.antidom.AntiDomain;
import solver.objective.ObjectiveManager;
import solver.search.loop.lns.neighbors.INeighbor;
import solver.search.loop.monitors.IMonitorInitPropagation;
import solver.search.loop.monitors.IMonitorInitialize;
import solver.search.loop.monitors.IMonitorRestart;
import solver.search.loop.monitors.IMonitorUpBranch;
import solver.search.restart.GeometricalRestartStrategy;
import solver.search.restart.IRestartStrategy;
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
 * A new version of the Explained-based neighborhood for LNS.
 * <p/>
 * Short description of the algorithm:
 * a. on a solution:
 *
 * @author Charles Prud'homme
 * @since 01/10/12
 */
public class ExplainedNeighborhood2 implements INeighbor, IMonitorInitPropagation, IMonitorUpBranch, IMonitorInitialize, IMonitorRestart {

    private static Logger LOGGER = LoggerFactory.getLogger("solver");


    private static final int ACTIVATED = 0; // ACTIVATED: activated
    private static final int REFUTED = 1; // REFUTED: refuted
    private static final int OBJECTIVE = 2; // OBJECTIVE: involving the objective variable
    private static final int CUT = 3; // CUT: decisions associated to the cut

    protected final Solver solver;
    protected ExplanationEngine mExplanationEngine;
    private ObjectiveManager om;
    private IntVar objective;
    private int LB, UB;
    private final int n;
    private final IntVar[] vars;
    private Decision last;

    // decision path that leads to a solution
    private ArrayList<Decision> path;

    // list of decisions related to the explanation of the objective variable
    private final ArrayList<Decision> valueDecisions;
    // list of index of clusters
    private final TIntArrayList clusters;
    private final IRestartStrategy geo4cluster, geo4rnd;
    // current cluster treated
    private int cluster;
    // index of the decision currently applied, and the nb of decision already applied
    private int curIdx, nbDecApplied;
    // list of decisions related to the exception, if any
//    private final ArrayList<Decision> exceptionDecisions;

    // various status of the decisions
    private BitSet[] decisions;
    // for restrict algo, do we fail applying the fragment
    private boolean applyFgmt;
    // do we need to force conflict?
    private boolean forceCft;

    // TEMPORARY DATA STRUCTURES
    private final ArrayList<Deduction> tmpDeductions;
    private final Set<Deduction> tmpValueDedutions;


    // FOR RANDOM
    private final double rfactor;
    private final Random random;
    private boolean inRndMode;
    private int nbCall, limit;
    private FastDecision duplicator;
    private double nbFixedVars;
    private final int[] bestSolution;
    private BitSet notFrozen;

    // FOR NOGOOD
    private NogoodStoreForRestarts ngs;
    private boolean recordNG = false;

    public ExplainedNeighborhood2(Solver solver, IntVar[] dvars, long seed, NogoodStoreForRestarts ngs, double rfactor) {
        this.solver = solver;
        this.random = new Random(seed);
        this.ngs = ngs;
        this.rfactor = rfactor;

        if (dvars == null) {
            n = solver.getNbVars();
            vars = new IntVar[solver.getNbVars()];
            for (int k = 0; k < n; k++) {
                vars[k] = (IntVar) solver.getVar(k);
            }
        } else {
            n = dvars.length;
            vars = dvars.clone();
        }
        bestSolution = new int[n];
        nbFixedVars = 2. * n / 3. + 1;

        ExplanationFactory.LAZY.plugin(solver, true);
        this.mExplanationEngine = solver.getExplainer();

        path = new ArrayList<Decision>(16);
        valueDecisions = new ArrayList<Decision>(16);
        clusters = new TIntArrayList(16);
        decisions = new BitSet[5];
        for (int i = 0; i < 4; i++) {
            decisions[i] = new BitSet(16);
        }

        // TEMPORARY DATA STRUCTURES
        tmpDeductions = new ArrayList<Deduction>(16);
        tmpValueDedutions = new HashSet<Deduction>(16);

        inRndMode = false;

        solver.getSearchLoop().plugSearchMonitor(this);
        notFrozen = new BitSet(n);
        geo4cluster = new GeometricalRestartStrategy(1, 1.2);
        geo4rnd = new GeometricalRestartStrategy(n / 2, 1.01);
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

    @Override
    public void beforeRestart() {
        if (recordNG && ngs != null) {
            ngs.beforeRestart();
        }
        recordNG = false;
    }

    @Override
    public void afterRestart() {
        if (ngs != null) {
            ngs.afterRestart();
        }
        last = null;
        nbDecApplied = curIdx = 0;
        if (inRndMode) {
            if (applyFgmt) {
                nbFixedVars /= rfactor;
            }
            random();
            return;
        }
        // 1. if a new solution has been found:
        if (path.size() > 0) {
            // force the failure and explain it
            if (forceCft) {
                explainCut();
            }
            relaxNeighborhood();
        }
    }

    @Override
    public boolean isSearchComplete() {
        return decisions[ACTIVATED].cardinality() == 0 || (inRndMode && nbFixedVars < 1);
    }

    @Override
    public void recordSolution() {
        for (int i = 0; i < vars.length; i++) {
            bestSolution[i] = vars[i].getValue();
        }
        if (duplicator == null) {
            duplicator = (FastDecision) solver.getSearchLoop().decision.duplicate();
        }
        // 1. clear data structures
        tmpDeductions.clear();
        valueDecisions.clear();
        clusters.clear();
        path.clear();
        decisions[ACTIVATED].clear();
        decisions[OBJECTIVE].clear();
        decisions[REFUTED].clear();
        decisions[CUT].clear();


        // 2. get anti domain of the objective variable
        readAntiDomain();

        // 3. re-build the clusters if required
        buildCluster();

        // 4. store the decisions related to the objective variable
        for (int i = 0; i < tmpDeductions.size(); i++) {
            valueDecisions.add(((BranchingDecision) tmpDeductions.get(i)).getDecision());
        }

        // 5. compute the first fragment to apply
        clonePath();

        // 6. prepare the next fragment heuristic
        cluster = 1;
        // for the restart:
        applyFgmt = true;
        // force conflict
        forceCft = true;
        inRndMode = false;
        nbFixedVars = 2. * n / 3. + 1;
        nbCall = 0;
        limit = geo4rnd.getNextCutoff(nbCall);

    }


    @Override
    public void fixSomeVariables(ICause cause) throws ContradictionException {
        // nothing to do, every thing is done with the delegate strategy
    }

    @Override
    public void restrictLess() {
        nbFixedVars /= rfactor;
        relaxNeighborhood();
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
            solver.getSearchLoop().restart();
            // HACK for nogood recording
            solver.getSearchLoop().decision.buildNext();
            recordNG = !inRndMode;
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
            if (far == objective.getValue()) {
                return;
            }
        } else {
            far = LB;
            near = objective.getValue() - 1;
            if (far == objective.getValue()) {
                return;
            }
        }


        int value;
        if (ismax) {
            // explain why obj cannot take a smaller value: from far to near
            if (it.hasNext()) {
                do {
                    value = it.next();
                } while (it.hasNext() && (value < near || value > far)); // skip {LBs} and {UBs before init propag}
                do {
                    explainValue(value);
                } while (it.hasNext() && (value = it.next()) >= near);
            }
        } else {
            // explain why obj cannot take a smaller value: from far to near
            if (it.hasNext()) {
                do {
                    value = it.next();
                } while (it.hasNext() && value < far);// skip {LBs before init propag}
                do {
                    explainValue(value);
                } while (it.hasNext() && (value = it.next()) <= near);
            }
        }
        it.dispose();
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
     * Build the cluster, for restrict less operation
     */
    private void buildCluster() {
        if (clusters.size() > 1) {
            int one = clusters.get(1);
            clusters.clear();
            clusters.add(0);
            clusters.add(one);
            for (int j = 0, i = one + 1; i < tmpDeductions.size(); j++, i += geo4cluster.getNextCutoff(j)) {
                clusters.add(i);
            }
            if (clusters.get(clusters.size() - 1) != tmpDeductions.size() - 1) {
                clusters.add(tmpDeductions.size() - 1);
            }
//            for (int i = one + 1; i < tmpDeductions.size(); i++) {
//                clusters.add(i);
//            }
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
    private void clonePath() {
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


            for (int k = 0; k < 3; k++) { // avoid CUT, because it is empty

                boolean bi = decisions[k].get(i);
                boolean bj = decisions[k].get(j);
                decisions[k].set(i, bj);
                decisions[k].set(j, bi);

            }
        }

        assert path.size() - 1 == decisions[ACTIVATED].previousSetBit(path.size() * 2);
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
            decisions[REFUTED].set(pos);
        }
        if (dec.getDecisionVariable().getId() == objective.getId()) {
            decisions[OBJECTIVE].set(pos);
        }
        decisions[ACTIVATED].set(pos);
        int idx = valueDecisions.indexOf(dec);
        if (idx > -1) valueDecisions.set(idx, clone);
    }

////

    /**
     * Force the failure, apply decisions to the last solution + cut => failure!
     */
    private void explainCut() {
        // Goal: force the failure to get the set of decisions related to the cut
        forceCft = false;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("explain Cut");
        }
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
                d.setPrevious(previous);
                d.buildNext();
                d.apply();
                solver.propagate();
                previous = d;
            }
            //solver.propagate();
            assert false : "SHOULD FAIL!";
        } catch (ContradictionException cex) {
            if ((cex.v != null) || (cex.c != null)) { // contradiction on domain wipe out
                tmpDeductions.clear();
                tmpValueDedutions.clear();
                decisions[CUT].clear(); // useless but... you know...

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

                if (tmpDeductions.isEmpty()) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("2 cases: (a) optimality proven or (b) bug in explanation");
                    }
                    throw new SolverException("2 cases: (a) optimality proven or (b) bug in explanation");
                }

                for (int i = 0; i < tmpDeductions.size(); i++) {
                    decisions[CUT].set(path.indexOf(((BranchingDecision) tmpDeductions.get(i)).getDecision()));
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
    }


    /**
     * Compute the next decisions to relax
     */
    private void relaxNeighborhood() {
        if (cluster < clusters.size()) {
            int k = cluster++;
            for (int i = clusters.get(k - 1); i < clusters.get(k); i++) {
                Decision dec = valueDecisions.get(i);
                int idx = path.indexOf(dec);
                decisions[ACTIVATED].clear(idx);
                decisions[CUT].clear(idx);
            }
        } else if (!decisions[CUT].isEmpty()) {
            // then hit the conflict decisions...
//            int idx = decisions[CUT].previousSetBit(path.size());
            int idx = decisions[CUT].nextSetBit(0);
            if (idx > -1) {
                decisions[ACTIVATED].clear(idx);
                decisions[CUT].clear(idx);
            }
        } else {
//          switch to rnd mode
            inRndMode = true;
            random();
            return;
        }
        // then deactivate refuted decisions
        int firstClear = decisions[ACTIVATED].nextClearBit(0);
        for (int i = decisions[REFUTED].nextSetBit(0); i > -1 && i < firstClear && firstClear > -1; i = decisions[REFUTED].nextSetBit(i + 1)) {
            if (decisions[OBJECTIVE].get(i)) {
                decisions[ACTIVATED].set(i);
            }
        }
        for (int j = decisions[REFUTED].nextSetBit(firstClear); j > -1; j = decisions[REFUTED].nextSetBit(j + 1)) {
            decisions[ACTIVATED].clear(j);
        }

        assert path.size() - 1 >= decisions[ACTIVATED].previousSetBit(path.size() * 2);
    }

    private void random() {
        if (duplicator != null) {
            nbCall++;
            if (nbCall > limit) {
                limit = nbCall + geo4rnd.getNextCutoff(nbCall);
                nbFixedVars /= rfactor;
            }
            notFrozen.set(0, n);
            path.clear();
            for (int i = 0; i < nbFixedVars - 1 && notFrozen.cardinality() > 0; i++) {
                int id = selectVariable();
                if (vars[id].contains(bestSolution[id])) {  // to deal with objective variable and related
                    FastDecision d = (FastDecision) duplicator.duplicate();
                    d.set(vars[id], bestSolution[id], DecisionOperator.int_eq);
                    path.add(d);
                    notFrozen.clear(id);
                }
            }
            decisions[ACTIVATED].clear();
            decisions[ACTIVATED].set(0, path.size());
            decisions[OBJECTIVE].clear();
            decisions[REFUTED].clear();
            decisions[CUT].clear();
        }
    }

    private int selectVariable() {
        int id;
        int cc = random.nextInt(notFrozen.cardinality());
        for (id = notFrozen.nextSetBit(0); id >= 0 && cc > 0; id = notFrozen.nextSetBit(id + 1)) {
            cc--;
        }
        return id;
    }

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
            if (nbDecApplied < decisions[ACTIVATED].cardinality()) {
                curIdx = decisions[ACTIVATED].nextSetBit(curIdx);
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
}
