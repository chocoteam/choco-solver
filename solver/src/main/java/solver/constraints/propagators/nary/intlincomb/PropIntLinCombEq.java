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

package solver.constraints.propagators.nary.intlincomb;

import choco.kernel.ESat;
import choco.kernel.memory.IEnvironment;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 23 avr. 2010br/>
 * Since : Galak 0.1<br/>
 */
public final class PropIntLinCombEq extends AbstractPropIntLinComb {

    public PropIntLinCombEq(final int[] coeffs, final int nbPosVars, final int cste, final IntVar[] vars,
                            final Constraint constraint, final IEnvironment env) {
        super(coeffs, nbPosVars, cste, vars, constraint, env);
    }

    /**
	 * Checks if the constraint is entailed.
	 * @return Boolean.TRUE if the constraint is satisfied, Boolean.FALSE if it
	 * is violated, and null if the filtering algorithm cannot infer yet.
	 */
	/*public Boolean isEntailed() {
        int a = coeffPolicy.computeLowerBound();
        int b = coeffPolicy.computeUpperBound();
        if (b < 0 || a > 0) {
            return Boolean.FALSE;
        } else if (a == 0 && b == 0) {
            return Boolean.TRUE;
        } else {
            return null;
        }
	}*/

    @Override
    public int getPropagationConditions() {
        return EventType.INSTANTIATE.mask + EventType.INCLOW.mask + EventType.DECUPP.mask;
    }

    /**
	 * Checks a new lower bound.
	 * @return true if filtering has been infered
	 * @throws ContradictionException if a domain empties or a contradiction is
	 * infered
     */
    public boolean filterOnImprovedLowerBound()
	throws ContradictionException {
        // the constraint check is needed only for
        // equality constraints (otherwise passive constraint)
        return propagateNewLowerBound(coeffPolicy.computeLowerBound());
	}

	/**
	 * Checks a new upper bound.
	 * @return true if filtering has been infered
	 * @throws ContradictionException if a domain empties or a contradiction is
	 * infered  */
    public boolean filterOnImprovedUpperBound()
	throws ContradictionException {
        return propagateNewUpperBound(coeffPolicy.computeUpperBound());
	}

    /**
	 * Tests if the constraint is consistent
	 * with respect to the current state of domains.
	 * @return true iff the constraint is bound consistent
	 * (weaker than arc consistent)
	 */
	public boolean isConsistent() {
        return (hasConsistentLowerBound() && hasConsistentUpperBound());
	}

    @Override
    protected String getOperator() {
        return " = ";
    }

    @Override
    protected ESat check(int value) {
        return ESat.eval(value == 0);
    }

    @Override
    protected void checkEntailment() {
        //nothing can be done over EQ and Integer linear combination
    }
}
