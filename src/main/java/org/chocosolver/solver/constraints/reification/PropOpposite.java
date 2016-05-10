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
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

/**
 * Constraint representing the negation of a given constraint
 * does not filter but fails if the given constraint is satisfied
 * Can be used within any constraint
 * <p/>
 * Should not be called by the user
 *
 * @author Jean-Guillaume Fages
 * @since 15/05/2013
 */
public class PropOpposite extends Propagator<Variable> {

    // constraint to negate
    Constraint original;

    public PropOpposite(Constraint original, Variable[] vars) {
        super(vars, PropagatorPriority.LINEAR, false);
        this.original = original;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ESat op = original.isSatisfied();
        if (op == ESat.TRUE) {
            fails();
        }
        if (op == ESat.FALSE) {
            setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        ESat op = original.isSatisfied();
        if (op == ESat.TRUE) {
            return ESat.FALSE;
        }
        if (op == ESat.FALSE) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
