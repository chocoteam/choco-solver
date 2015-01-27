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
package org.chocosolver.solver.explanations;

import gnu.trove.set.hash.THashSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.search.strategy.decision.Decision;

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
    private final THashSet<ICause> causes;
    private final BitSet decisions;

    public Explanation(boolean saveCauses) {
        this.causes = new THashSet<>();
        this.decisions = new BitSet();
        this.saveCauses = saveCauses;
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
    public void addAll(Explanation explanation) {
        if (explanation.nbCauses() > 0) {
            this.causes.addAll(explanation.causes);
        }
        if (explanation.nbDecisions() > 0) {
            this.decisions.or(explanation.decisions);
        }
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
     * Duplicate the current explanation
     *
     * @return a new explanation
     */
    public Explanation duplicate() {
        Explanation explanation = new Explanation(this.saveCauses);
        explanation.addAll(this);
        return explanation;
    }

    /**
     * Clear the explanation, to enable reusing it.
     */
    public void clear() {
        causes.clear();
        decisions.clear();
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder("Explanation");
        if(saveCauses){
            st.append(Arrays.toString(causes.toArray()));
        }
        return st.toString();
    }
}
