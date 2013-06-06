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
package solver.constraints.gary.channeling.relations;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;

public class Eq_Int extends GraphRelation<IntVar> {


    protected Eq_Int(IntVar[] vars) {
        super(vars);
    }

    @Override
    public ESat isEntail(int var1, int var2) {
        if (var1 == var2) {
            return ESat.TRUE;
        }
        IntVar x = vars[var1];
        IntVar y = vars[var2];
        if (x.instantiated() && y.instantiated() && x.getValue() == y.getValue()) {
            return ESat.TRUE;
        }
        if (x.getLB() > y.getUB() || x.getUB() < y.getLB()) {
            return ESat.FALSE;
        }
        if (!x.hasEnumeratedDomain()) {
            return ESat.UNDEFINED;
        }
        int up = x.getUB();
        int up2 = y.getUB();
        for (int i = x.getLB(); i <= up; i = x.nextValue(i)) {
            for (int j = y.getLB(); j <= up2; j = y.nextValue(j)) {
                if (i == j) {
                    return ESat.UNDEFINED;
                }
            }
        }
        return ESat.FALSE;
    }

    @Override
    public void applyTrue(int var1, int var2, Solver solver, ICause cause) throws ContradictionException {
        if (var1 != var2) {
            IntVar x = vars[var1];
            IntVar y = vars[var2];
            x.updateLowerBound(y.getLB(), cause);
            y.updateLowerBound(x.getLB(), cause);
            x.updateUpperBound(y.getUB(), cause);
            y.updateUpperBound(x.getUB(), cause);
            // ensure that, in case of enumerated domains,  holes are also propagated
            if (y.hasEnumeratedDomain() && x.hasEnumeratedDomain()) {
                int ub = x.getUB();
                for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
                    if (!(y.contains(val))) {
                        x.removeValue(val, cause);
                    }
                }
                ub = y.getUB();
                for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                    if (!(x.contains(val))) {
                        y.removeValue(val, cause);
                    }
                }
            }
        }
    }

    @Override
    public void applyFalse(int var1, int var2, Solver solver, ICause cause) throws ContradictionException {
        if (var1 != var2) {
            IntVar x = vars[var1];
            IntVar y = vars[var2];
            if (x.instantiated()) {
                y.removeValue(x.getValue(), cause);
            } else if (y.instantiated()) {
                x.removeValue(y.getValue(), cause);
            }
        } else {
            vars[var1].contradiction(cause, EventType.REMOVE, "x != x");
        }
    }

    @Override
    public boolean isDirected() {
        return false;
    }
//	@Override
//	public GraphProperty[] getGraphProperties() {
//		return new GraphProperty[]{GraphProperty.REFLEXIVITY, GraphProperty.TRANSITIVITY, GraphProperty.SYMMETRY};
//	}
}
