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

package solver.constraints;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.SolverException;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public abstract class IntConstraint<I extends IntVar> extends Constraint<I, Propagator<I>> {

    public IntConstraint(I[] vars, Solver solver, PropagatorPriority storeThreshold) {
        super(vars, solver, storeThreshold);
    }

    public ESat isSatisfied() {
        int[] tuple = new int[vars.length];
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].instantiated()) {
                tuple[i] = vars[i].getValue();
            } else {
                return ESat.UNDEFINED;
            }
        }
        return isSatisfied(tuple);
    }


    /**
     * Test if the <code>tuple</code> satisfies this <code>Constraint</code> object.
     * The <i>i^th</i> cell corresponds to the value attributed to the <i>i^th</i> <code>Variable</code> object
     *
     * @param tuple array of values
     * @return <code>ESat.TRUE</code> if this <code>Constraint</code> object is satisfied regarding <code>tuple</code>, <code>ESat.FALSE</code> otherwise.
     */
    public abstract ESat isSatisfied(int[] tuple);

    @Override
    public HeuristicVal getIterator(String name, I var) {
        if (name.equals(VAL_DEFAULT)) {
            return HeuristicValFactory.fastenumVal(var);
        }
        throw new SolverException("Unknown comparator name :" + name);
    }
}
