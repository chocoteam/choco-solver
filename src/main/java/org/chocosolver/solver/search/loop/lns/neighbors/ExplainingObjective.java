/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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
import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.explanations.store.IEventStore;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.restart.GeometricalRestartStrategy;
import org.chocosolver.solver.search.restart.IRestartStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;

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
public class ExplainingObjective extends ExplainingCut{

    private ObjectiveManager<IntVar, Integer> om;
    private IntVar objective;
    private int LB, UB;

    // list of index of clusters
    private final TIntArrayList clusters;
    private final IRestartStrategy geo4cluster;
    // current cluster treated
    private int cluster;
    // various status of the decisions

    // TEMPORARY DATA STRUCTURES
    private final TIntArrayList tmpDeductions;

    private final TIntSet tmpValueDeductions;


    public ExplainingObjective(Model aModel, int level, long seed) {
        super(aModel, level, seed);
        clusters = new TIntArrayList(16);
        // TEMPORARY DATA STRUCTURES
        tmpDeductions = new TIntArrayList();
        tmpValueDeductions = new TIntHashSet(16);
        geo4cluster = new GeometricalRestartStrategy(1, 1.2);
        //TODO: check plug monitor
    }

    @Override
    public void recordSolution() {
        // 1. clear data structures
        tmpDeductions.clear();
        tmpValueDeductions.clear();
        clusters.clear();
        path.free();

        // 2. get anti domain of the objective variable
        readRemovedValues();

        // 3. re-build the clusters if required
        buildCluster();

        clonePath();

        related.clear();
        for (int i = 0; i < tmpDeductions.size(); i++) {
            related.set(tmpDeductions.get(i));
        }
        unrelated.clear();
        unrelated.or(related);
        forceCft = true;
    }

    @Override
    protected void _fixVar() {
        if (cluster < clusters.size()) {
            int k = cluster++;
            if (k < clusters.size()) {
                for (int i = clusters.get(k - 1); i < clusters.get(k); i++) {
                    int idx = tmpDeductions.get(i);
                    notFrozen.clear(idx);
                }
            }
        } else {
            super._fixVar();
        }
    }


    @Override
    public void init() {
        Solver r = mModel.getSolver();
        om = r.getObjectiveManager();
        objective = om.getObjective();
        LB = objective.getLB();
        UB = objective.getUB();
        if (mExplanationEngine == null) {
            if (r.getExplainer() == null) {
                r.set(new ExplanationEngine(mModel, false, false));
            }
            this.mExplanationEngine = r.getExplainer();
        }
    }

    /*@Override
    public void beforeUpBranch() {
    }

    @Override
    public void afterUpBranch() {
        // we need to catch up that case when the sub tree is closed and this imposes a fragment
        if (last != null && mModel.getResolver().getLastDecision()*//*.getId()*//* == last*//*.getId()*//*) {
            mModel.getSearchLoop().restart();
        }
    }*/

    ///////////////////////


    @Override
    protected void explain() {
        forceCft = false;
        nbFixedVariables = related.cardinality();
        nbCall = 0;
        increaseLimit();

        cluster = 1;
        notFrozen.clear();
        notFrozen.or(related);
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
            // explain why obj cannot take a smaller value: from far to near
            while (far >= near) {
                explainValueE(far);
                far--;
            }
        } else {
            far = LB;
            near = objective.getValue() - 1;
            // explain why obj cannot take a smaller value: from far to near
            while (far <= near) {
                explainValueE(far);
                far++;
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
        Explanation explanation = mExplanationEngine.makeExplanation(false);
        RuleStore rs = mExplanationEngine.getRuleStore();
        rs.init(explanation);
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
        explanation.recycle();
    }


    /**
     * Iterate over removed values to explain the objective variable state
     */
    private void readRemovedValuesB() {
        clusters.add(0);
        // 2'. compute bounds to avoid explaining the whole domain
        boolean ismax = om.getPolicy() == ResolutionPolicy.MAXIMIZE;
        Explanation explanation = mExplanationEngine.makeExplanation(false);
        RuleStore rs = mExplanationEngine.getRuleStore();
        IEventStore es = mExplanationEngine.getEventStore();
        rs.init(explanation);
        int i = 0;
        int far, near;
        if (ismax) {
            far = UB;
            near = objective.getValue() + 1;
            rs.addUpperBoundRule(objective);
            while (i < es.getSize()) {
                if (rs.match(i, es)) {
                    int val = es.getFirstValue(i);
                    int old = es.getSecondValue(i);
                    // need to check event: 2nd pos is old lb in case of instantiation
                    if(es.getEventType(i) == IntEventType.INSTANTIATE){
                        old = es.getThirdValue(i);
                    }
                    if (far >= near && far > val && far <= old) {
                        explainValueB(far, es, i);
                        far = val;
                        rs.init(explanation);
                    }
                    if (far < near) {
                        explanation.recycle();
                        return;
                    }
                }
                i++;
            }

        } else {
            far = LB;
            near = objective.getValue() - 1;
            rs.addLowerBoundRule(objective);
            while (i < es.getSize()) {
                if (rs.match(i, es)) {
                    int val = es.getFirstValue(i);
                    int old = es.getSecondValue(i);
                    // no need to check event; 2nd pos is always old lb
                    if (far <= near && far < val && far >= old) {
                        explainValueB(far, es, i);
                        far = val;
                        rs.init(explanation);
                    }
                    if (far > near) {
                        explanation.recycle();
                        return;
                    }
                }
                i++;
            }
        }
        explanation.recycle();
    }


    private void explainValueB(int value, IEventStore es, int i) {

        // mimic explanation computation
        Explanation explanation = mExplanationEngine.makeExplanation(false);
        RuleStore rs = mExplanationEngine.getRuleStore();
        rs.init(explanation);
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

        tmpValueDeductions.removeAll(tmpDeductions);
        //        assert tmpDeductions.size() == 0 || correct : "E(" + value + ") not INCLUDED in previous ones";
        if (tmpDeductions.addAll(tmpValueDeductions)) {
            clusters.add(tmpDeductions.size());
        }
        explanation.recycle();
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
}
