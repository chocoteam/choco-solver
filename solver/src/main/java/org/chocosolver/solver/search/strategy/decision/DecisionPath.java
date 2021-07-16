/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * To handle set of decisions.
 * <p>
 * Decisions are added to this set of decisions with a call to {@link #pushDecision(Decision)},
 * Decisions are then applied in a call to {@link #buildNext()} and {@link #apply()},
 * and removed in a call to {@link #synchronize()}.
 * </br>
 * Only one decision can be added at the same level.
 * The last declared will erased the previous ones.
 * Only one decision is applied/removed at a time.
 * </br>
 * First decision is <b>always</b> {@link RootDecision#ROOT}, so, {@link #size()} returns at least 1.
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 11/03/2016.
 */
public class DecisionPath extends DecisionMaker implements Serializable {

    /**
     * Current decision path.
     */
    private List<Decision> decisions;

    IStateInt last;

    /**
     * Create a decision path
     * @param environment    backtracking environment
     */
    public DecisionPath(IEnvironment environment) {
        this.decisions = new ArrayList<>();
        this.decisions.add(RootDecision.ROOT);
        this.last = environment.makeInt(1);
    }

    /**
     * Prepare the last decision pushed since the last call to this method to be applied.
     */
    public void buildNext() {
        int p = last.get();
        if(p == decisions.size()-1) {
            decisions.get(p).buildNext();
        }
    }

    /**
     * Apply decision pushed since the last call to this method.
     * This call should always be preceded by a call to {@link #buildNext()}.
     *
     * @throws ContradictionException if one decision application fails
     */
    public void apply() throws ContradictionException {
        int p = last.get();
        if(p == decisions.size()-1) {
            decisions.get(p).apply();
            last.add(1);
        }
    }

    /**
     * Add a decision at the decision path.
     *
     * @param decision the decision to add
     */
    public void pushDecision(Decision decision) {
        int p = last.get();
        decision.setPosition(p);
        if(decisions.size() == p){
            decisions.add(decision);
        }else if(decisions.size() == p + 1) {
            decisions.set(p, decision);
        }else throw new SolverException("Cannot add decision to decision path");
    }

    /**
     * Synchronizes the decision path after a backtrack.
     * Removes and frees all decisions with level greater or equal to the current level.
     * Recall that the very first decision, {@link RootDecision#ROOT}, can not be removed from this.
     */
    public void synchronize() {
        synchronize(true);
    }

    /**
     * Synchronizes the decision path after a backtrack.
     * Removes all decisions with level greater or equal to the current level.
     * Recall that the very first decision, {@link RootDecision#ROOT}, can not be removed from this.
     * @param free set to <i>true</i> to synchronize <b>and</b> free out-dated decisions
     */
    public void synchronize(boolean free) {
        if (decisions.size() > 1) { // never remove ROOT decision.
            int t = last.get();
            for (int f = decisions.size() - 1; f >= t; f--) {
                Decision d = decisions.remove(f);
                if(free)d.free();
            }
        }
    }

    /**
     * Retrieves, but not removes, the last decision of the decision path.
     * Recall that the very first decision of this decision path is {@link RootDecision#ROOT}.
     *
     * @return the last decision of the decision path.
     */
    public Decision getLastDecision() {
        int size = decisions.size();
        return decisions.get(size - 1);

    }

    /**
     * Return the number of decision in this decision path.
     * Recall that this decisions path contains at least one decision: {@link RootDecision#ROOT}.
     *
     * @return the size of the decision path
     */
    public int size() {
        return decisions.size();
    }

    /**
     * Return the decision in position <i>i</i> in this decision path, or null if no decision exists at that position.
     *
     * @param i index of the decision to return
     * @return the decision in position <i>i</i> in this decision path
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public Decision getDecision(int i) {
        if (i < 0 || i >= decisions.size()) {
            throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + decisions.size());
        }
        return decisions.get(i);
    }

    /**
     * Add all decisions of this decision path into a list of decision
     *
     * @param aList               list to populate
     * @param includeRootDecision set to <tt>true<tt/> to include the very first fake decision, ROOT, in the list.
     */
    public void transferInto(Collection<Decision> aList, boolean includeRootDecision) {
        for (int i = includeRootDecision ? 0 : 1; i < decisions.size(); i++) {
            aList.add(decisions.get(i));
        }
    }

    /**
     * @return a pretty print of the downmost decision(s)
     */
    public String lastDecisionToString() {
        StringBuilder st = new StringBuilder();
        int lst = last.get();
        if (lst < decisions.size()) {
            Decision decision = decisions.get(lst);
            st.append(String.format("[%d/%d] %s",
                    decision.getArity() - decision.triesLeft() + 1, decision.getArity(), decision.toString())
            );
        } else {
            st.append(String.format("[1/1] d_0: %s", decisions.get(0).toString()));
        }
        return st.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("Path[%s]: ", decisions.size()));
        sb.append(decisions.get(0));
        for(int i = 1; i < decisions.size(); i++){
            sb.append(", ").append(decisions.get(i));
        }
        return sb.toString();
    }
}
