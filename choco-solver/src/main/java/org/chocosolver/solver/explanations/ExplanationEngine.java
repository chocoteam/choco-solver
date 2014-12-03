/*
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

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.antidom.AntiDomain;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26 oct. 2010
 * Time: 12:34:02
 * <p/>
 * A class to manage explanations. The default behavior is to do nothing !
 */
public class ExplanationEngine implements Serializable {
    static Logger LOGGER = LoggerFactory.getLogger(ExplanationEngine.class);
    Solver solver;

    /**
     * Builds an ExplanationEngine
     *
     * @param slv associated solver's environment
     */
    public ExplanationEngine(Solver slv) {
        this.solver = slv;
    }

    public boolean isActive() {
        return false;
    }


    /**
     * Explain the activation of a propagator involved in a reified constraint
     *
     * @param var        the reified variable
     * @param propagator the propagator to awake.
     */
    public void activePropagator(BoolVar var, Propagator propagator) {
    }

    /**
     * Explain the removal of the <code>val</code> from <code>var</code>, due to <code>cause</code>.
     *
     * @param var   an integer variable
     * @param val   a value
     * @param cause a cause
     */
    public void removeValue(IntVar var, int val, ICause cause) {
    }

    /**
     * Explain the removal of [<code>old</code>,<code>value</code>[ from <code>var</code>, due to <code>cause</code>.
     * <p/>
     * Prerequisite: <code>value</code> should belong to <code>var</code>
     *
     * @param intVar an integer variable
     * @param old    the previous lower bound
     * @param value  the current lower bound
     * @param cause  the cause
     */
    public void updateLowerBound(IntVar intVar, int old, int value, ICause cause) {
    }

    /**
     * Explain the removal of ]<code>value</code>,<code>old</code>] from <code>var</code>, due to <code>cause</code>.
     * <p/>
     * Prerequisite: <code>value</code> should belong to <code>var</code>
     *
     * @param var   an integer variable
     * @param old   the previous upper bound
     * @param value the current upper bound
     * @param cause the cause
     */
    public void updateUpperBound(IntVar var, int old, int value, ICause cause) {
    }

    /**
     * Explain the assignment to <code>val</code> of <code>var</code> due to <code>cause</code>.
     *
     * @param var   an integer variable
     * @param val   the assignment value
     * @param cause the cause
     * @param oldLB the previous LB
     * @param oldUB the previous UB
     */
    public void instantiateTo(IntVar var, int val, ICause cause, int oldLB, int oldUB) {
    }

    public AntiDomain getRemovedValues(IntVar v) {
        return null;
    }

    /**
     * Provides an explanation for the removal of value <code>val</code> from variable
     * <code>var</code> ; the implementation is recording policy dependent
     * for a flattened policy, the database is checked (automatically flattening explanations)
     * for a non flattened policy, only the value removal is returned
     *
     * @param var an integer variable
     * @param val an integer value
     * @return a deduction
     */
    public Deduction explain(IntVar var, int val) {
        return null;
    }

    /**
     * Provides an explanation for the deduction <code>deduction</code> ; the implementation is recording policy dependent
     * for a flattened policy, the database is checked (automatically flattening explanations)
     * for a non flattened policy, the deduction is returned unmodified
     *
     * @param deduction a Deduction
     * @return a deduction
     */
    public Deduction explain(Deduction deduction) {
        return null;
    }

    /**
     * Provides a FLATTENED explanation for the removal of value <code>val</code> from variable
     * <code>var</code>
     *
     * @param var an integer variable
     * @param val an integer value
     * @return an explanation
     */
    public Explanation flatten(IntVar var, int val) {
        return null;
    }

    public Explanation flatten(Explanation expl) {
        return null;
    }

    public Explanation flatten(Deduction deduction) {
        return null;
    }

    /**
     * Provides the recorded explanation in database for the removal of value <code>val</code>
     * from variable <code>var</code>
     * The result will depend upon the recording policy of the engine
     *
     * @param var an integer variable
     * @param val an integer value
     * @return an explanation
     */
    public Explanation retrieve(IntVar var, int val) {
        return null;
    }

    /**
     * provides a BranchingDecision associated to a decision
     *
     * @param decision an integer variable
     * @param isLeft   is left branch decision
     * @return the associated right BranchingDecision
     */
    public BranchingDecision getDecision(Decision decision, boolean isLeft) {
        return null;
    }

    public PropagatorActivation getPropagatorActivation(Propagator propagator) {
        return null;
    }

    /**
     * Store the <code>explanation</code> of the <code>deduction</code>
     *
     * @param deduction   deduction to explain
     * @param explanation explanation of the deduction
     */
    public void store(Deduction deduction, Explanation explanation) {

    }

    /**
     * Remove the <code>decision</code> from the set of left decisions over <code>var</code>
     *
     * @param decision a left decision over <code>var</code>
     * @param var      a variable
     */
    public void removeLeftDecisionFrom(Decision decision, Variable var) {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void onRemoveValue(IntVar var, int val, ICause cause, Explanation explanation) {
        LOGGER.debug("::EXPL:: REMVAL " + val + " FROM " + var + " APPLYING " + cause + " BECAUSE OF " + flatten(explanation));
    }

    public void onActivatePropagator(Propagator propagator, Explanation explanation) {
        LOGGER.debug("::EXPL:: ACTIV. " + propagator + " BECAUSE OF " + flatten(explanation));
    }

    public void onContradiction(ContradictionException cex, Explanation explanation) {
        if (cex.v != null) {
            LOGGER.debug("::EXPL:: CONTRADICTION on " + cex.v + " BECAUSE " + explanation);
        } else if (cex.c != null) {
            LOGGER.debug("::EXPL:: CONTRADICTION on " + cex.c + " BECAUSE " + explanation);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public Solver getSolver() {
        return solver;
    }
}
