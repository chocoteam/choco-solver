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
package org.chocosolver.solver.constraints.nary.alldifferent.conditions;

import org.chocosolver.solver.constraints.nary.alldifferent.PropAllDiffInst;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * Propagator for ConditionnalAllDifferent that only reacts on instantiation
 *
 * @author Jean-Guillaume Fages
 */
public class PropCondAllDiffInst extends PropAllDiffInst {

	protected Condition condition;
	private boolean mode;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * ConditionnalAllDifferent constraint for integer variables
     * enables to control the cardinality of the matching
	 *
	 * IF mode
	 * 	for all X in vars, condition(X) => X != Y, for all Y in vars
	 * ELSE
	 * 	for all X,Y in vars, condition(X) AND condition(Y) => X != Y
     *
     * @param variables array of integer variables
	 * @param c a condition to define the subset of variables subject to the AllDiff cstr
	 * @param mode defines how to apply filtering
     */
    public PropCondAllDiffInst(IntVar[] variables, Condition c, boolean mode) {
        super(variables);
		this.condition = c;
		this.mode = mode;
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

	protected void fixpoint() throws ContradictionException {
		while (toCheck.size() > 0) {
			int vidx = toCheck.pop();
			if(condition.holdOnVar(vars[vidx])){
				int val = vars[vidx].getValue();
				for (int i = 0; i < n; i++) {
					if (i != vidx && (mode || condition.holdOnVar(vars[i]))) {
						if (vars[i].removeValue(val, this)) {
							if (vars[i].isInstantiated()) {
								toCheck.push(i);
							}
						}
					}
				}
			}
		}
	}

    @Override
    public ESat isEntailed() {
		int nbInst = 0;
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].isInstantiated()) {
				nbInst++;
				if(condition.holdOnVar(vars[i])){
					for (int j = i + 1; j < vars.length; j++) {
						if(condition.holdOnVar(vars[j]))
							if (vars[j].isInstantiated() && vars[i].getValue() == vars[j].getValue()) {
								return ESat.FALSE;
							}
					}
				}
			}
		}
		if (nbInst == vars.length) {
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
    }
}
