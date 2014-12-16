/**
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package org.chocosolver.solver.explanations.arlil;

import gnu.trove.set.hash.THashSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.search.strategy.decision.Decision;

import java.util.Collections;
import java.util.Set;

/**
 * A reason is simply a set of causes and decisions explaining a <i>situation</i>, for instance a conflict.
 * It is related to ARLIL explanation engine, in substitution of {@link org.chocosolver.solver.explanations.Explanation}.
 * Created by cprudhom on 09/12/14.
 * Project: choco.
 */
public class Reason {

    private final THashSet<ICause> causes;
    private final THashSet<Decision> decisions;

    public Reason() {
        causes = new THashSet<>();
        decisions = new THashSet<>();
    }

    /**
     * Add a cause, which explains, partially, the situation
     *
     * @param cause a cause
     * @return true if this was an unknown cause
     */
    public boolean addCause(ICause cause) {
        return causes.add(cause);
    }

    /**
     * Add a decision, which explains, partially, the situation
     *
     * @param decision a decision
     * @return true if this was an unknown decision
     */
    public boolean addDecicion(Decision decision) {
        return decisions.add(decision);
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
        return decisions.size();
    }

    public static long count = 0;

    /**
     * Merge all causes and decisions from <code>reason</code> in this.
     *
     * @param reason a given reason
     * @return true if the reason changed
     */
    public boolean addAll(Reason reason) {
        boolean b1 = reason.nbCauses() > 0 && this.causes.addAll(reason.causes);
        boolean b2 = reason.nbDecisions() > 0 && this.decisions.addAll(reason.decisions);
        return b1 | b2;
    }

    /**
     * Remove one cause from the set of causes explaining the situation
     *
     * @param cause a cause to remove
     * @return true if the reason changed
     */
    public boolean remove(ICause cause) {
        return causes.remove(cause);
    }

    /**
     * Remove one decision from the set of decisions explaining the situation
     *
     * @param decision a decision to remove
     * @return true if the reason changed
     */
    public boolean remove(Decision decision) {
        return decisions.remove(decision);
    }


    /**
     * Return a unmodifiable copy of the set of decisions
     */
    public Set<Decision> getDecisions() {
        return Collections.unmodifiableSet(decisions);
    }

    /**
     * Return a unmodifiable copy of the set of causes
     */
    public Set<ICause> getCauses() {
        return Collections.unmodifiableSet(causes);
    }

    /**
     * Duplicate the current reason
     *
     * @return a new reason
     */
    public Reason duplicate() {
        Reason reason = new Reason();
        reason.addAll(this);
        return reason;
    }

    /**
     * Clear the Reason, to enable reusing it.
     */
    public void clear(){
        causes.clear();
        decisions.clear();
    }
}
