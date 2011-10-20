/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.explanations;

import choco.kernel.memory.IStateBitSet;
import com.sun.istack.internal.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.ISearchMonitor;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.decision.Decision;
import solver.variables.IntVar;
import solver.variables.Variable;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26 oct. 2010
 * Time: 12:34:02
 * <p/>
 * A class to manage explanations. The default behavior is to do nothing !
 */
public class ExplanationEngine extends VoidSearchMonitor implements Serializable, ISearchMonitor, IExplanationMonitor {
    static Logger LOGGER = LoggerFactory.getLogger("explainer");
    ExplanationMonitorList emList;
    Solver solver;

    public void removeValue(IntVar var, int val, @NotNull ICause cause) {   }

    public void updateLowerBound(IntVar intVar, int old, int value, @NotNull ICause cause) {   }

    public void updateUpperBound(IntVar intVar, int old, int value, @NotNull ICause cause) {    }

    public void instantiateTo(IntVar var, int val, @NotNull ICause cause) {    }

    public IStateBitSet getRemovedValues(IntVar v) {
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
     */
    public Explanation retrieve(IntVar var, int val) {
        return null;
    }


    /**
     * Builds an ExplanationEngine
     *
     * @param slv associated solver's environment
     */
    public ExplanationEngine(Solver slv) {
        this.solver = slv;
        slv.getSearchLoop().plugSearchMonitor(this);
        emList = new ExplanationMonitorList();
    }


    /**
     * provides a VariableAssignment associated to a pair variable-value
     *
     * @param var an integer variable
     * @param val an integer value
     */

    public VariableAssignment getVariableAssignment(IntVar var, int val) {
        return null;
    }

    /**
     * provides a ValueRefutation associated to a pair variable-value
     *
     * @param var an integer variable
     * @param val an integer value
     */

    public VariableRefutation getVariableRefutation(IntVar var, int val, Decision dec) {
        return null;
    }


    /**
     * adds a ExplanationMonitor to the explainer in order to catch explanation-related events
     * @param mon the monitor to be added
     */
    public void addExplanationMonitor(IExplanationMonitor mon) {
        emList.add(mon);
    }


    public int getWorldNumber(Variable va, int val) {
        return 0;
    }


    @Override
    public void onRemoveValue(IntVar var, int val, ICause cause, Explanation explanation) {}

    @Override
    public void onUpdateLowerBound(IntVar intVar, int old, int value, ICause cause, Explanation explanation) {}

    @Override
    public void onUpdateUpperBound(IntVar intVar, int old, int value, ICause cause, Explanation explanation) {}

    @Override
    public void onInstantiateTo(IntVar var, int val, ICause cause, Explanation explanation) {}

    @Override
    public void onContradiction(ContradictionException cex, Explanation explanation, int upTo, Decision decision) {}
}
