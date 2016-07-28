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
import org.chocosolver.solver.variables.Variable;

public class VariableSelect {

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
    public static VariableSelector<IntVar> antiFirstFailInt(Model model) {
        return new AntiFirstFail(model);
    }

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

}
