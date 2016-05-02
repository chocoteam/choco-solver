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
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.exception.ContradictionException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * To handle set of decisions.
 * <p>
 * Decisions are added to this set of decisions with a call to {@link #pushDecision(Decision)},
 * Decisions are then applied in a call to {@link #apply()}, and removed in a call to {@link #synchronize()}.
 * </br>
 * Note that, if more than one decision is added before calling {@link #apply()}, these decisions belong to the same level.
 * They will be applied at the same time in a call to {@link #apply()}, and remove at the same time in a call to {@link #synchronize()}.
 * Otherwise, only one decision is applied/removed at a time.
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
    /**
     * Store the sizes of {@link #decisions} during search, to evaluate which decisions are part of the same level.
     */
    protected IStateInt mLevel;
    /**
     * Indices of level in {@link #decisions}
     */
    protected int[] levels;


    /**
     * Create a decision path
     * @param environment    backtracking environment
     */
    public DecisionPath(IEnvironment environment) {
        this.decisions = new ArrayList<>();
        this.mLevel = environment.makeInt(0);
        this.decisions.add(RootDecision.ROOT);
        this.levels = new int[10];
        this.levels[0] = 1;
    }

    /**
     * Apply decisions pushed since the last call to this method.
     * If more than one decision are applied in this call,
     * they are automatically force to not be refuted, and are considered to belong to the same level.
     *
     * @throws ContradictionException if one decision application fails
     */
    public void apply() throws ContradictionException {
        // 1. look for the first decision in decisions with that rank
        int l = mLevel.get();
        int f = levels[l];
        int t = decisions.size();
        boolean samelevel = (f < t - 1);
        Decision decision;
        for (int i = f; i < t; i++) {
            decision = decisions.get(i);
            if (samelevel) {
                decision.setRefutable(false);
            }
            decision.buildNext();
            decision.apply();
        }
        if (t - f > 0) {
            mLevel.add(1);
            ensureCapacity(l + 1);
            levels[l + 1] = decisions.size();
        }
    }

    private void ensureCapacity(int ncapa) {
        if (levels.length <= ncapa) {
            int[] tmp = levels;
            levels = new int[ncapa * 3 / 2 + 1];
            System.arraycopy(tmp, 0, levels, 0, tmp.length);
        }
    }

    /**
     * Add a decision at the decision path.
     *
     * @param decision the decision to add
     */
    public void pushDecision(Decision decision) {
        decision.setPosition(decisions.size());
        decisions.add(decision);
    }

    /**
     * Synchronizes the decision path after a backtrack.
     * Removes all decisions with level greater or equal to the current level.
     * Recall that the very first decision, {@link RootDecision#ROOT}, can not be removed from this.
     */
    public void synchronize() {
        if (decisions.size() > 1) { // never remove ROOT decision.
            int t = levels[mLevel.get()];
            for (int f = decisions.size() - 1; f >= t; f--) {
                decisions.remove(f).free();
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
        int l = mLevel.get();
        int f = levels[l];
        int t = decisions.size();
        Decision decision;
        if (f < t - 1) {
            st.append("[1/1] ");
            decision = decisions.get(f);
            st.append(decision.toString());
            for (int i = f + 1; i < t; i++) {
                decision = decisions.get(i);
                st.append(" /\\ ").append(decision.toString());
            }
        } else if(f < t){
            decision = decisions.get(f);
            st.append(String.format("[%d/%d] %s",
                    decision.getArity() - decision.triesLeft() + 1, decision.getArity(), decision.toString())
            );
        }
        return st.toString();

    }

}
