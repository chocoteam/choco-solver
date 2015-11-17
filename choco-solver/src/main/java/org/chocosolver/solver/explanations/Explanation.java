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
package org.chocosolver.solver.explanations;

import gnu.trove.list.TIntList;
import gnu.trove.set.hash.THashSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.cnf.PropNogoods;
import org.chocosolver.solver.constraints.nary.cnf.SatSolver;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.PoolManager;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

/**
 * An explanation is simply a set of causes and decisions explaining a <i>situation</i>, for instance a conflict.
 * It is related to the explanation engine (replacement of Explanation)
 * Created by cprudhom on 09/12/14.
 * Project: choco.
 */
public class Explanation {

    private final boolean saveCauses;
    private Rules rules;
    private final THashSet<ICause> causes;
    private final BitSet decisions;
    private int evtstrIdx;  // event store index of the last analysis
    private final PoolManager<Explanation> explanationPool;

    Explanation(PoolManager<Explanation> explanationPool, boolean saveCauses) {
        this.causes = new THashSet<>();
        this.decisions = new BitSet();
        this.saveCauses = saveCauses;
        this.explanationPool = explanationPool;
        this.rules = new Rules(16, 16);
    }

    /**
     * Add a cause, which explains, partially, the situation
     *
     * @param cause a cause
     * @return true if this was an unknown cause
     */
    public boolean addCause(ICause cause) {
        return saveCauses && causes.add(cause);
    }

    /**
     * Add a decision, which explains, partially, the situation
     *
     * @param decision a decision
     */
    public void addDecicion(Decision decision) {
        decisions.set(decision.getWorldIndex());
    }


    /**
     * Return the number of causes explaining the situation
     *
     * @return an int
     */
    public int nbCauses() {
        return causes.size();
    }

    /**
     * Return the number of decisions explaining the situation
     *
     * @return an int
     */
    public int nbDecisions() {
        return decisions.cardinality();
    }

    /**
     * Merge all causes and decisions from <code>explanation</code> in this.
     *
     * @param explanation a given explanation
     */
    public void addCausesAndDecisions(Explanation explanation) {
        if (explanation.nbCauses() > 0) {
            this.causes.addAll(explanation.causes);
        }
        if (explanation.nbDecisions() > 0) {
            this.decisions.or(explanation.decisions);
        }
    }

    /**
     * Merge 'someRules' into this rules
     *
     * @param someRules the rules to add
     */
    public void addRules(Rules someRules) {
        rules.or(someRules);
    }

    /**
     * Remove one cause from the set of causes explaining the situation
     *
     * @param cause a cause to remove
     * @return true if the explanation changed
     */
    public boolean remove(ICause cause) {
        return causes.remove(cause);
    }

    /**
     * Remove one decision from the set of decisions explaining the situation
     *
     * @param decision a decision to remove
     */
    public void remove(Decision decision) {
        decisions.clear(decision.getWorldIndex());
    }


    /**
     * Return a unmodifiable copy of the set of decisions
     */
    public BitSet getDecisions() {
        return decisions;
    }

    /**
     * Return a unmodifiable copy of the set of causes
     */
    public Set<ICause> getCauses() {
        return Collections.unmodifiableSet(causes);
    }

    /**
     * Indicates whether or not the explanation is complete
     */
    public boolean isComplete() {
        return rules == null;
    }

    /**
     * Get the event store idx at which the last analysis ends
     *
     * @return an event store index
     */
    public int getEvtstrIdx() {
        return evtstrIdx;
    }

    /**
     * Set the event store idx, where the last analysis ends
     *
     * @param evtstrIdx an event store index
     */
    public void setEvtstrIdx(int evtstrIdx) {
        this.evtstrIdx = evtstrIdx;
    }

    /**
     * Return the rules, may be null
     *
     * @return the rules or null
     */
    public Rules getRules() {
        return rules;
    }

    /**
     * Duplicate the current explanation
     *
     * @return a new explanation
     */
    public Explanation duplicate() {
        Explanation explanation = explanationPool.getE();
        if (explanation == null) {
            explanation = new Explanation(explanationPool, saveCauses);
        }
        explanation.addCausesAndDecisions(this);
        explanation.addRules(this.rules);
        return explanation;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder("Explanation ");
        if (saveCauses) {
            st.append(Arrays.toString(causes.toArray()));
        }
        st.append(decisions);
        if (rules != null) {
            st.append(rules.toString());
        }
        return st.toString();
    }

    @SuppressWarnings("unchecked")
    public void postNogood(PropNogoods ngstore, TIntList ps) {
        if (rules == null) {
            Solver mSolver = ngstore.getSolver();
            Decision<IntVar> decision = mSolver.getSearchLoop().getLastDecision();
            ps.clear();
            while (decision != RootDecision.ROOT) {
                if (decisions.get(decision.getWorldIndex())) {
                    assert decision.hasNext();
                    ps.add(SatSolver.negated(ngstore.Literal(decision.getDecisionVariables(), (Integer) decision.getDecisionValue())));
                }
                decision = decision.getPrevious();
            }
            ngstore.addLearnt(ps.toArray());
        }
    }

    public void recycle() {
        evtstrIdx = 0;
        causes.clear();
        decisions.clear();
        rules.clear();
        explanationPool.returnE(this);
    }
}
