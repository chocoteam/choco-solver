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
package org.chocosolver.solver;


import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;



/**
 * This interface describes services of smallest element which can act on variables.
 * As an example, propagator is a cause because it filters values from variable domain.
 * So do decision, objective manager, etc.
 * It has an impact on domain variables and so it can fails.
 * <p>
 *     Important: when the {@link ICause#why(org.chocosolver.solver.explanations.RuleStore, org.chocosolver.solver.variables.IntVar, org.chocosolver.solver.variables.events.IEventType, int)} method
 *     needs to evaluate the incoming event, one may be aware that in some cases (for instance, BoolVar), the original event can promoted.
 *     Hence, if a cause can only explain bound modifications, it should also either consider the INSTANTIATION or the strengthen mask.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 oct. 2010
 */
public interface ICause  {


    /**
     * Add new rules to the rule store
     *
     * @param ruleStore the rule store
     * @param var       the modified variable
     * @param evt       the undergoing event
     * @param value     the value (for REMOVE only)
     * @return true if at least one rule has been added to the rule store
     */
    default boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        throw new SolverException("Undefined why(...) method for " + this);
    }
}
