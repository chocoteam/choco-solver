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
package org.chocosolver.solver.search.strategy.selectors;

import org.chocosolver.solver.variables.Variable;

/**
 * A variable evaluator. One provide a way to evaluate a variable (domain size, smallest values, ...).
 * It should return a value which can be minimized.
 * For instance, to select the integer variable with the smallest value in its domain, return ivar.getLB().
 * To select the variable with the largest value in its domain, return -ivar.getUB().
 * <p/>
 * Such evaluator can be called and combined with others to define a variable selector which enables tie breaking.
 * Indeed, many uninstantied variables may return the same value for the evaluation.
 * In that case, the next evaluator should break ties, otherwise the first computed variable would be returned.
 * <p/>
 * Be aware that using a single variable evaluator in {@code solver.search.strategy.selectors.VariableSelectorWithTies} may result
 * in a slower execution due to the generalisation it requires.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/03/2014
 */
public interface VariableEvaluator<V extends Variable> {

    /**
     * Evaluates the heuristic that is <b>minimized</b> in order to find the best variable
     *
     * @param variable array of variable
     * @return the result of the evaluation, to minimize
     */
    public abstract double evaluate(V variable);
}
