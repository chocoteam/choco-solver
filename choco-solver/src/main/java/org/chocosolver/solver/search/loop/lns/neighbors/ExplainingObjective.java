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
package org.chocosolver.solver.search.loop.lns.neighbors;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.explanations.store.IEventStore;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.loop.monitors.IMonitorInitPropagation;
import org.chocosolver.solver.search.loop.monitors.IMonitorUpBranch;
import org.chocosolver.solver.search.restart.GeometricalRestartStrategy;
import org.chocosolver.solver.search.restart.IRestartStrategy;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.StatisticUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;

/**
 * a specific neighborhood for LNS based on the explanation of the objective variable.
 * <p>
 * This neightborhood is specific in the sense that it needs to compute explanation on a solution.
 * Furthermore, the fixSomeVariables method creates and applies decision, so that the explanation recorder can infer.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/07/13
 */
public class ExplainingObjective extends ANeighbor implements IMonitorInitPropagation, IMonitorUpBranch {

    protected ExplanationEngine mExplanationEngine;
    protected RuleStore mRuleStore;
    private ObjectiveManager<IntVar, Integer> om;
    private IntVar objective;
    private int LB, UB;
    protected final Random random;

    // decision path that leads to a solution
    private ArrayList<Decision> path;

    // list of decisions related to the explanation of the objective variable
    private final ArrayList<Decision> valueDecisions;
    // list of index of clusters
    private final TIntArrayList clusters;
    private final IRestartStrategy geo4cluster;
    // current cluster treated
    private int cluster;
    // various status of the decisions
    private BitSet related2dom; // a bitset indicating which decisions of the path are related to the cut
    private BitSet notFrozen;
    private BitSet unrelated;
    private BitSet refuted;

    private Decision last; // needed to catch up the case when a subtree is closed, and this imposes the fgmt

    // TEMPORARY DATA STRUCTURES
    private final TIntArrayList tmpDeductions;

    private final TIntSet tmpValueDeductions;

    // FOR RANDOM
    private double nbFixedVariables = 0d; // number of decision to fix in the set of decisions explaining the cut
    private int nbCall, limit;
    private final int level;


    public ExplainingObjective(Solver aSolver, int level, long seed) {
        super(aSolver);
        this.random = new Random(seed);
        this.level = level;

        this.mExplanationEngine = aSolver.getExplainer();
        assert mExplanationEngine != null;

        path = new ArrayList<>(16);
        valueDecisions = new ArrayList<>(16);
        clusters = new TIntArrayList(16);
        related2dom = new BitSet(16);
        notFrozen = new BitSet(16);
        unrelated = new BitSet(16);
        refuted = new BitSet(16);

        // TEMPORARY DATA STRUCTURES
        tmpDeductions = new TIntArrayList();
        tmpValueDeductions = new TIntHashSet(16);

        notFrozen = new BitSet(16);
        geo4cluster = new GeometricalRestartStrategy(1, 1.2);
        mSolver.plugMonitor(this);
    }


    @Override
    public void recordSolution() {
        // 1. clear data structures
        tmpDeductions.clear();
        valueDecisions.clear();
        clusters.clear();
        path.clear();
        related2dom.clear();
        unrelated.clear();

        // 2. get anti domain of the objective variable
        readRemovedValues();

        // 3. re-build the clusters if required
        buildCluster();

        for (int i = 0; i < tmpDeductions.size(); i++) {
            valueDecisions.add(null);
        }


        // 5. compute the first fragment to apply
        clonePath();

        // 6. prepare iteration over decisions of DOM intersection CUT
        cluster = 1;
        notFrozen.or(related2dom);
        nbFixedVariables = related2dom.cardinality();
        nbCall = 0;
        increaseLimit();
    }

    @Override
    public void fixSomeVariables(ICause cause) throws ContradictionException {
        // then fix variables
        // this part is specific: a fake decision path has to be created
        // 1. try by relaxing from the cluster
        if (cluster < clusters.size()) {
            int k = cluster++;
            if (k < clusters.size()) {
                for (int i = clusters.get(k - 1); i < clusters.get(k); i++) {
                    Decision dec = valueDecisions.get(i);
                    int idx = path.indexOf(dec);
                    notFrozen.clear(idx);
                }
            }
        } else {
            nbCall++;
            restrictLess();
            notFrozen.clear();
            notFrozen.or(related2dom);
            for (; !notFrozen.isEmpty() && notFrozen.cardinality() > nbFixedVariables; ) {
                int idx = selectVariable();
                notFrozen.clear(idx);
            }
        }
        assert mSolver.getSearchLoop().getLastDecision() == RootDecision.ROOT;
        // add the first refuted decisions
        int first = notFrozen.nextSetBit(0);
        for (int i = (first > -1 ? refuted.nextSetBit(first) : first); i > -1; i = refuted.nextSetBit(i + 1)) {
            notFrozen.clear(i);
        }
        // add unrelated decisions
        notFrozen.or(unrelated);
        // then build the fake decision path
        last = null;
//        LOGGER.debug("relax dom {}", notFrozen.cardinality());
        for (int id = notFrozen.nextSetBit(0); id >= 0 && id < path.size(); id = notFrozen.nextSetBit(id + 1)) {
            //            last = ExplanationToolbox.mimic(path.get(id)); // required because some unrelated decisions can be refuted
            if (path.get(id).hasNext()) {
                last = path.get(id).duplicate();
                if (refuted.get(id)) last.buildNext();
                imposeDecisionPath(mSolver, last);
            }
        }
    }

    @Override
    public void restrictLess() {
        if (nbCall > limit) {
            nbFixedVariables = random.nextDouble() * related2dom.cardinality();
            increaseLimit();
        }
        last = null;
    }

    @Override
    public boolean isSearchComplete() {
        return false;
    }

    @Override
    public void beforeInitialPropagation() {
        // nothing to do
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterInitialPropagation() {
        om = mExplanationEngine.getSolver().getObjectiveManager();
        objective = om.getObjective();
        LB = objective.getLB();
        UB = objective.getUB();
        if (!objective.hasEnumeratedDomain()) {
            mRuleStore = new RuleStore(mSolver, false);
        }
    }

    @Override
    public void beforeUpBranch() {
    }

    @Override
    public void afterUpBranch() {
        // we need to catch up that case when the sub tree is closed and this imposes a fragment
        if (last != null && mSolver.getSearchLoop().getLastDecision().getId() == last.getId()) {
            mSolver.getSearchLoop().restart();
        }
    }

    ///////////////////////

    private void increaseLimit() {
        long ank = (long) (1.2 * StatisticUtils.binomialCoefficients(related2dom.cardinality(), (int) nbFixedVariables - 1));
        int step = (int) Math.min(ank, level);
        limit = nbCall + step;
    }

    private int selectVariable() {
        int id;
        int cc = random.nextInt(notFrozen.cardinality());
        for (id = notFrozen.nextSetBit(0); id >= 0 && cc > 0; id = notFrozen.nextSetBit(id + 1)) {
            cc--;
        }
        return id;
    }


    /**
     * Iterate over removed values to explain the objective variable state
     */
    private void readRemovedValues() {
        clusters.add(0);
        if (objective.hasEnumeratedDomain()) {
            readRemovedValuesE();
        } else {
            readRemovedValuesB();
        }
    }

    /**
     * Iterate over removed values to explain the objective variable state
     */
    private void readRemovedValuesE() {
        // 2'. compute bounds to avoid explaining the whole domain
        boolean ismax = om.getPolicy() == ResolutionPolicy.MAXIMIZE;
        int far, near;
        if (ismax) {
            far = UB;
            near = objective.getValue() + 1;
            if (far != objective.getValue()) {
                // explain why obj cannot take a smaller value: from far to near
                while (far >= near) {
                    explainValueE(far);
                    far--;
                }
            }
        } else {
            far = LB;
            near = objective.getValue() - 1;
            if (far != objective.getValue()) {
                // explain why obj cannot take a smaller value: from far to near
                while (far <= near) {
                    explainValueE(far);
                    far++;
                }
            }
        }
    }

    /**
     * Explain the removal of value from the objective variable
     *
     * @param value value to explain
     */
    private void explainValueE(int value) {

        // mimic explanation computation
        RuleStore rs = mExplanationEngine.getRuleStore();
        rs.clear();
        Explanation explanation = new Explanation(false);
        rs.addRemovalRule(objective, value);
        IEventStore es = mExplanationEngine.getEventStore();
        int i = es.getSize() - 1;

        while (i > -1) {
            if (rs.match(i, es)) {
                rs.update(i, es, explanation);
            }
            i--;
        }
        for (int b = explanation.getDecisions().nextSetBit(0); b >= 0; b = explanation.getDecisions().nextSetBit(b + 1)) {
            tmpValueDeductions.add(b);
        }

        assert tmpValueDeductions.size() > 0 : "E(" + value + ") is EMPTY";
        tmpValueDeductions.removeAll(tmpDeductions);
        //        assert tmpDeductions.size() == 0 || correct : "E(" + value + ") not INCLUDED in previous ones";
        if (tmpDeductions.addAll(tmpValueDeductions)) {
            clusters.add(tmpDeductions.size());
        }
    }


    /**
     * Iterate over removed values to explain the objective variable state
     */
    private void readRemovedValuesB() {
        clusters.add(0);
        // 2'. compute bounds to avoid explaining the whole domain
        boolean ismax = om.getPolicy() == ResolutionPolicy.MAXIMIZE;
        mRuleStore.clear();
        IEventStore es = mExplanationEngine.getEventStore();
        int i = 0;
        int far, near;
        if (ismax) {
            far = UB;
            near = objective.getValue() + 1;
            if (far != objective.getValue()) {
                mRuleStore.addUpperBoundRule(objective);
                while (i < es.getSize()) {
                    if (mRuleStore.match(i, es)) {
                        int val = es.getFirstValue(i);
                        int old = es.getSecondValue(i);
                        while (far <= near && far > val && far <= old) {
                            explainValueB(far--, es, i);
                        }
                        if (far == near) return;
                    }
                    i++;
                }
            }

        } else {
            far = LB;
            near = objective.getValue() - 1;
            if (far != objective.getValue()) {
                mRuleStore.addLowerBoundRule(objective);
                while (i < es.getSize()) {
                    if (mRuleStore.match(i, es)) {
                        int val = es.getFirstValue(i);
                        int old = es.getSecondValue(i);
                        while (far <= near && far < val && far >= old) {
                            explainValueB(far++, es, i);
                        }
                        if (far == near) return;
                    }
                    i++;
                }
            }
        }
    }


    private void explainValueB(int value, IEventStore es, int i) {

        // mimic explanation computation
        RuleStore rs = mExplanationEngine.getRuleStore();
        rs.clear();
        Explanation explanation = new Explanation(false);
        rs.addRemovalRule(objective, value);

        while (i > -1) {
            if (rs.match(i, es)) {
                rs.update(i, es, explanation);
            }
            i--;
        }
        for (int b = explanation.getDecisions().nextSetBit(0); b >= 0; b = explanation.getDecisions().nextSetBit(b + 1)) {
            tmpValueDeductions.add(b);
        }

        assert tmpValueDeductions.size() > 0 : "E(" + value + ") is EMPTY";
        tmpValueDeductions.removeAll(tmpDeductions);
        //        assert tmpDeductions.size() == 0 || correct : "E(" + value + ") not INCLUDED in previous ones";
        if (tmpDeductions.addAll(tmpValueDeductions)) {
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
        }
    }


    /**
     * Compute the initial fragment, ie set of decisions to keep.
     */
    private void clonePath() {
        Decision dec = mSolver.getSearchLoop().getLastDecision();
        while ((dec != RootDecision.ROOT)) {
            addToPath(dec);
            dec = dec.getPrevious();
        }
        Collections.reverse(path);
        int size = path.size();
        for (int i = 0, mid = size >> 1, j = size - 1; i < mid; i++, j--) {
            boolean bi = related2dom.get(i);
            related2dom.set(i, related2dom.get(j));
            related2dom.set(j, bi);

            bi = unrelated.get(i);
            unrelated.set(i, unrelated.get(j));
            unrelated.set(j, bi);

            bi = refuted.get(i);
            refuted.set(i, refuted.get(j));
            refuted.set(j, bi);
        }
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
        if (dec.hasNext()) {
            int idx = tmpDeductions.indexOf(dec.getWorldIndex());
            if (idx > -1) {
                valueDecisions.set(idx, clone);
                related2dom.set(pos);
            } else {
                unrelated.set(pos);
            }
        } else {
            refuted.set(pos);
        }
    }

    /**
     * Simulate a decision path, with backup
     *
     * @param aSolver  the concerned solver
     * @param decision the decision to apply
     * @throws ContradictionException
     */
    private static void imposeDecisionPath(Solver aSolver, Decision decision) throws ContradictionException {
        IEnvironment environment = aSolver.getEnvironment();
        ObjectiveManager objectiveManager = aSolver.getObjectiveManager();
        // 1. simulates open node
        Decision current = aSolver.getSearchLoop().getLastDecision();
        decision.setPrevious(current);
        aSolver.getSearchLoop().setLastDecision(decision);
        // 2. simulates down branch
        environment.worldPush();
        decision.setWorldIndex(environment.getWorldIndex());
        decision.buildNext();
        objectiveManager.apply(decision);
        objectiveManager.postDynamicCut();
//        aSolver.getEngine().propagate();
    }
}
