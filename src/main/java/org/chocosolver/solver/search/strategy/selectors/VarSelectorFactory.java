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
package org.chocosolver.solver.search.strategy.selectors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

/**
 * Strategies over real variables
 * Just there to simplify strategies creation.
 * <br/>
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 */
public class VarSelectorFactory {

    // ************************************************************************************
    // GENERIC SELECTORS
    // ************************************************************************************

    /**
     * Selects the first free (non-instantiated) variable
     * @return a variable selector choosing always the first non-instantiated variable
     */
    public static <V extends Variable> VariableSelector<V> inputOrderVar(Model model) {
        return new InputOrder<>(model);
    }

    /**
     * Selects sequentially the next non-instantiated variable to branch on.
     * Iterates over variables one by one and looping back to the first variable if necessary,
     * until all variable are instantiated.
     * Does not require the previous variable to be instantiated to move on the next variable.
     * example : selects vars[0], then vars[1] even if vars[0] is still uninstantiated
     * @return a variable selector iterating over variables one by one
     */
    public static <V extends Variable> VariableSelector<V> roundRobinVar() {
        return new Cyclic<>();
    }

    /**
     * Selects randomly a non-instantiated variable
     * @param seed randomness seed
     * @return a variable selector choosing variables randomly
     */
    public static <V extends Variable> VariableSelector<V> randomVar(long seed){
        return new Random<>(seed);
    }

    /**
     * Selects randomly a non-instantiated variable
     * @return a variable selector choosing variables randomly
     */
    public static <V extends Variable> VariableSelector<V> randomVar(){
        return randomVar(0);
    }

    /**
     * Selects the non instantiated variable involved in the largest number of constraints
     * @return a variable selector choosing the variable involved in the largest number of constraints
     */
    public static <V extends Variable> VariableSelector<V> maxCstrsVar(){
        return new Occurrence<>();
    }

    /**
     * Selects the non instantiated variable with the smallest domain
     * @return a variable selector choosing the variable with the smallest domain
     */
    public static <V extends Variable> VariableSelector<V> minDomVar(){
        return new GeneralizedMinDomVarSelector();
    }

    /**
     * Selects the non instantiated variable with the largest domain
     * @return a variable selector choosing the variable with the largest domain
     */
    public static <V extends Variable> VariableSelector<V> maxDomVar(){
        return new GeneralizedMinDomVarSelector(false);
    }

    // ************************************************************************************
    // INTVAR SELECTORS
    // ************************************************************************************

    /**
     * Selects the non instantiated variable with the smallest domain
     * Only for integer variables
     * @return a variable selector choosing the variable with the smallest domain
     */
    public static VariableSelector<IntVar> minDomIntVar(Model model){
        return new FirstFail(model);
    }

    /**
     * Selects the non instantiated variable with the largest domain
     * Only for integer variables
     * @return a variable selector choosing the variable with the largest domain
     */
    public static VariableSelector<IntVar> maxDomIntVar(){
        return new AntiFirstFail();
    }

    /**
     * Selects the non instantiated variable with the smallest lower bound
     * Only for integer variables
     * @return a variable selector choosing the variable with the smallest lower bound
     */
    public static VariableSelector<IntVar> minLBVar(){
        return new Smallest();
    }

    /**
     * Selects the non instantiated variable with the largest upper bound
     * Only for integer variables
     * @return a variable selector choosing the variable with the upper bound
     */
    public static VariableSelector<IntVar> maxUBVar(){
        return new Largest();
    }
}
