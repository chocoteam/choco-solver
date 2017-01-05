/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.explanations;

import gnu.trove.set.hash.THashSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.search.strategy.decision.Decision;
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
 * @author Charles Prud'homme, Narendra Jussien
 */
public class Explanation {

    /**
     * Set to <tt>true</tt> to save causes (ie propagator) into this explanation
     */
    private final boolean saveCauses;

    /**
     * The rules inferring this explanation.
     * This is null when the explanation is complete, valuated otherwise.
     */
    private Rules rules;

    /**
     * Possibly empty (see {@link #saveCauses}) set of causes related to this explanation
     */
    private final THashSet<ICause> causes;

    /**
     * Set of decisions related to this explanation.
     * BitSet is selected because of efficient operations provided.
     */
    private final BitSet decisions;

    /**
     * Index of the last analyzed event in the event store.
     * For partial explanation only.
     */
    private int evtstrIdx;

    /**
     * A pool manager to avoid requesting too much the GC.
     */
    private final PoolManager<Explanation> explanationPool;

    /**
     * Create an explanation of a state (most of the time, a failure but not restricted to that).
     * @param explanationPool explanations pool manager, to limit GC
     * @param saveCauses set to <tt>true</tt> to store causes in this explanation
     */
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
    public void addDecision(Decision decision) {
        decisions.set(decision.getPosition());
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
        decisions.clear(decision.getPosition());
    }


    /**
     * @return a unmodifiable copy of the set of decisions
     */
    public BitSet getDecisions() {
        return decisions;
    }

    /**
     * @return a unmodifiable copy of the set of causes
     */
    @SuppressWarnings("unused")
    public Set<ICause> getCauses() {
        return Collections.unmodifiableSet(causes);
    }

    /**
     * @return <tt>true</tt> if the explanation is complete
     */
    public boolean isComplete() {
        // since rules is valuated only when the explanation is not complete, this test is correct
        return rules.isEmpty();
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

    /**
     * Recycle this explanation when it is not used anymore.
     */
    public void recycle() {
        evtstrIdx = 0;
        causes.clear();
        decisions.clear();
        rules.clear();
        explanationPool.returnE(this);
    }
}
