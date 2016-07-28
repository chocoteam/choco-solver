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

package org.chocosolver.solver.search.strategy;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

/**
 * Small factory enabling an easier instantiation of the variable selectors.
 */
public class VariableSelect {

    // ************************************************************************************
    // INTVAR VARIABLES SELECTORS
    // ************************************************************************************

    /**
     * Chooses the variable with the smallest domain (instantiated variables are ignored).
     * Used to fail early and avoid to explore a large part of the search space
     *  <br/>
     * <p>
     *     e.g. if three {@link IntVar} are defined :
     *     <ul>
     *         <li>a of domain {1, 5, 8, 9}</li>
     *         <li>b of domain {5, 8, 9}</li>
     *         <li>c of domain {8, 9, 10, 15}</li>
     *     </ul>
     *     it chooses b
     * </p>
     *
     * @param model choco {@link Model}
     * @return first fail variable selector
     */
    public static VariableSelector<IntVar> firstFail(Model model) {
        return new FirstFail(model);
    }

    /**
     * Chooses the variable with the largest domain (instantiated variables are ignored).
     * <br/>
     * <p>
     *     e.g. if three {@link IntVar} are defined :
     *     <ul>
     *         <li>a of domain {1, 5, 8, 9}</li>
     *         <li>b of domain {5, 8, 9}</li>
     *         <li>c of domain {8, 9, 10, 15}</li>
     *     </ul>
     *     it chooses c
     * </p>
     * @param model choco {@link Model}
     * @return anti first fail variable selector
     */
    public static VariableSelector<IntVar> antiFirstFail(Model model) {
        return new AntiFirstFail(model);
    }

    /**
     * Chooses the variable with the smallest value in its domain (instantiated variables are ignored).
     * <br/>
     * <p>
     *     e.g. if three {@link IntVar} are defined :
     *     <ul>
     *         <li>a of domain {1, 5, 8, 9}</li>
     *         <li>b of domain {5, 8, 9}</li>
     *         <li>c of domain {8, 9, 10, 15}</li>
     *     </ul>
     *     it chooses a
     * </p>
     * @return smallest value variable selector
     */
    public static VariableSelector<IntVar> smallestValue() {
        return new Smallest();
    }

    /**
     * Chooses the variable with the largest value in its domain (instantiated variables are ignored).
     * <br/>
     * <p>
     *     e.g. if three {@link IntVar} are defined :
     *     <ul>
     *         <li>a of domain {1, 5, 8, 9}</li>
     *         <li>b of domain {5, 8, 9}</li>
     *         <li>c of domain {8, 9, 10, 15}</li>
     *     </ul>
     *     it chooses c
     * </p>
     * @return largest value variable selector
     */
    public static VariableSelector<IntVar> largestValue() {
        return new Largest();
    }


    /**
     * Chooses the variable with the largest difference between the two smallest values in its domain
     * (instantiated variables are ignored)
     * <br/>
     * <p>
     *     e.g. if three {@link IntVar} are defined :
     *     <ul>
     *         <li>a of domain {1, 5, 8, 9}</li>
     *         <li>b of domain {5, 8, 9}</li>
     *         <li>c of domain {8, 9, 10, 15}</li>
     *     </ul>
     *     it chooses a
     * </p>
     * @return max regret variable selector
     */
    public static VariableSelector<IntVar> maxRegret() {
        return new MaxRegret();
    }


    // ************************************************************************************
    // SETVAR VARIABLES SELECTORS
    // ************************************************************************************

    /**
     * Chooses the variables minimising envelopeSize-kernelSize (quite similar to {@link VariableSelect#firstFail(Model)})
     * (instantiated variables are ignored)
     * @return min delta variable selector
     */
    public static VariableSelector<SetVar> minDelta() {
        return new MinDelta();
    }


    /**
     * Chooses the variables maximising envelopeSize-kernelSize (quite similar to {@link VariableSelect#antiFirstFail(Model)})
     * (instantiated variables are ignored)
     * @return max delta variable selector
     */
    public static VariableSelector<SetVar> maxDelta() {
        return new MaxDelta();
    }

    // ************************************************************************************
    // GENERIC VARIABLES SELECTORS
    // ************************************************************************************

    /**
     * Chooses iteratively variables in a cyclic manner.
     * It retrieves the variables by lexicographic order, and goes back the beginning once the last variable chosen
     * <br/>
     * <p>
     *     e.g. if three {@link IntVar} are defined :
     *     <ul>
     *         <li>a of domain {1, 5, 8, 9}</li>
     *         <li>b of domain {5, 8, 9}</li>
     *         <li>c of domain {8, 9, 10, 15}</li>
     *     </ul>
     *     and no change is done on the domains, it chooses successively a, b, c, a, b, c, ...
     * </p>
     * @return cyclic variable selector
     */
    public static <T extends Variable> VariableSelector<T> cyclic() {
        return new Cyclic<>();
    }


    /**
     * Chooses the variable with the smallest domain (instantiated variables are ignored).
     * Used to fail early and avoid to explore a large part of the search space
     *  <br/>
     * This var selector works for {@link IntVar}, {@link org.chocosolver.solver.variables.RealVar} and
     * {@link org.chocosolver.solver.variables.SetVar}. It is heavier than {@link VariableSelect#firstFail(Model)}
     * (more checks), but handles more variables types.
     *
     * @return generic first fail variable selector
     */
    public static VariableSelector firstFailGeneric() {
        return new GeneralizedMinDomVarSelector();
    }


    /**
     * Chooses variables in order they appears (instantiated variables are ignored).
     * @param model choco {@link Model}
     * @return input order variable selector
     */
    public static VariableSelector inputOrder(Model model) {
        return new InputOrder(model);
    }


    /**
     * Chooses the variable with the largest number of attached propagators (instantiated variables are ignored).
     * @return occurences variable selector
     */
    public static VariableSelector occurences() {
        return new Occurrence();
    }


    /**
     * Chooses randomly a non-instantiated variables
     * @param seed random generator seed
     * @return random variable selector
     */
    public static VariableSelector random(long seed) {
        return new Random(seed);
    }



}
