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
package solver.constraints.propagators.extension.binary;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public abstract class PropBinCSP extends Propagator<IntVar> {

    protected BinRelation relation;

    protected PropBinCSP(IntVar x, IntVar y, BinRelation relation,
                         Solver solver, Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(ArrayUtils.toArray(x, y), solver, intVarPropagatorConstraint, PropagatorPriority.BINARY, false);
    }

    public final BinRelation getRelation() {
        return relation;
    }

    @Override
    public ESat isEntailed() {
        int nbCons = 0;

        int ub0 = vars[0].getUB();
        for (int val0 = vars[0].getLB(); val0 <= ub0; val0 = vars[0].nextValue(val0)) {
            int nbS = 0;
            int ub1 = vars[1].getUB();
            for (int val1 = vars[1].getLB(); val1 <= ub1; val1 = vars[1].nextValue(val1)) {
                if (relation.isConsistent(val0, val1)) {
                    nbS++;
                }
            }
            if (nbS > 0 && nbS < vars[1].getDomainSize()) {
                return ESat.UNDEFINED;
            }
            nbCons += nbS;

        }
        if (nbCons == 0) {
            return ESat.FALSE;
        } else if (nbCons == vars[0].getDomainSize() * vars[1].getDomainSize()) {
            return ESat.TRUE;
        }
        return null;
    }
}
