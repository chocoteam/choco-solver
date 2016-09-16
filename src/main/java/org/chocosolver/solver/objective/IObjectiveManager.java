/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.objective;

import java.util.function.Function;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;
/**
 * interface to monitor the bounds of the objective variable.
 * 
 * @author Jean-Guillaume Fages, Charles Prud'homme, Arnaud Malapert
 * 
 * @param <V> type of objective variable
 */
public interface IObjectiveManager<V extends Variable> extends IBoundsManager, ICause {

    /**
     * @return the objective variable
     */
    V getObjective();

    /**
     * Informs the manager that a new solution has been found
     */
    void updateBestSolution(Number n);
    
    /**
     * Informs the manager that a new solution has been found
     */
    void updateBestSolution();
    
    /**
     * Set a user-defined cut computer to avoid "worse" solutions 
     */
    void setCutComputer(Function<Number, Number> cutComputer);

    /**
     * Define a strict cut computer where in the next solution to find should be strictly greater (resp. lesser) than
     * the best solution found so far when maximizing (resp. minimizing) a problem.
     */
    void setStrictDynamicCut();
    
    /**
     * Define a <i>walking</i> cut computer where in the next solution to find should be greater than (resp. less than)
     * or equal to the best solution found so far when maximizing (resp. minimizing) a problem.
     */
    void setWalkingDynamicCut();
    
    /**
     * Prevent the model from computing worse quality solutions
     *
     * @throws org.chocosolver.solver.exception.ContradictionException if posting this cut fails
     */
    void postDynamicCut() throws ContradictionException;
}