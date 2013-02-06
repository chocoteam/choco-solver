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

package solver.constraints.reified;

import common.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.reified.PropReified;
import solver.variables.BoolVar;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19 nov. 2010
 */
public class ReifiedConstraint extends Constraint<Variable, Propagator<Variable>> {

    final Constraint cons, oppcons;

    //TODO: to improve, works, but HashSet can be removed

    private static Variable[] extractVariable(BoolVar bVar, Constraint constraint, Constraint oppositeConstraint) {
        Variable[] vars1 = constraint.getVariables();
        Variable[] vars2 = oppositeConstraint.getVariables();

        List<Variable> union = new ArrayList<Variable>();
        union.add(bVar);
        for (int i = 0; i < vars1.length; i++) {
            if (!union.contains(vars1[i])) {
                union.add(vars1[i]);
            }
        }
        for (int i = 0; i < vars2.length; i++) {
            if (!union.contains(vars2[i])) {
                union.add(vars2[i]);
            }
        }
        return union.toArray(new Variable[union.size()]);
    }

    public ReifiedConstraint(BoolVar bVar, Constraint constraint, Constraint oppositeConstraint,
                             Solver solver) {
        super(extractVariable(bVar, constraint, oppositeConstraint), solver);
        cons = constraint;
        oppcons = oppositeConstraint;
        Propagator[] left = cons.propagators.clone();
        Propagator[] right = oppositeConstraint.propagators.clone();
        setPropagators(new PropReified(vars, left, right, solver, this));
//        addPropagators(left);
//        addPropagators(right);
    }

    @Override
    public ESat isSatisfied() {
        if (vars[0].instantiated()) {
            BoolVar b = (BoolVar) vars[0];
            if (b.getValue() == 1) {
                return cons.isSatisfied();
            } else {
                return oppcons.isSatisfied();
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return vars[0].toString() + "<=>" + cons.toString() + " (" + oppcons.toString() + ")";
    }

//    @Override
//    public HeuristicVal getIterator(String name, Variable var) {
//        throw new UnsupportedOperationException("ReifiedConstraint does not provide such a service");
//    }
}
