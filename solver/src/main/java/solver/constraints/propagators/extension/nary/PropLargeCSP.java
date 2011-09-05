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
package solver.constraints.propagators.extension.nary;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public class PropLargeCSP extends Propagator<IntVar> {

    protected final LargeRelation relation;

    protected final int[] currentTuple;

    public PropLargeCSP(IntVar[] vars, LargeRelation relation, Solver solver, Constraint<IntVar, Propagator<IntVar>> intVarPropagatorConstraint) {
        super(vars, solver, intVarPropagatorConstraint, PropagatorPriority.QUADRATIC, false);
        this.relation = relation;
        this.currentTuple = new int[vars.length];
    }


    public final LargeRelation getRelation() {
        return relation;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.REMOVE.mask;
    }

    @Override
    public void propagate() throws ContradictionException {
        filter();
    }

    @Override
    public void propagateOnRequest(IRequest<IntVar> intVarIRequest, int idxVarInProp, int mask) throws ContradictionException {
        forcePropagate();
    }

    @Override
    public ESat isEntailed() {
//        if (isCompletelyInstantiated()) {
//            int[] tuple = new int[vars.length];
//            for (int i = 0; i < vars.length; i++) {
//                tuple[i] = vars[i].getValue();
//            }
//            return ESat.eval(relation.isConsistent(tuple));
//        }
//        return ESat.UNDEFINED;
        return ESat.TRUE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CSPLarge({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(vars[i] + ", ");
        }
        sb.append("})");
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void filter() throws ContradictionException {
        boolean stop = false;
        int nbUnassigned = 0;
        int index = -1, i = 0;
        while (!stop && i < vars.length) {
            if (!vars[i].instantiated()) {
                nbUnassigned++;
                index = i;
            } else {
                currentTuple[i] = vars[i].getValue();
            }
            if (nbUnassigned > 1) {
                stop = true;
            }
            i++;
        }
        if (!stop) {
            if (nbUnassigned == 1) {
                int left = Integer.MIN_VALUE;
                int right = left;

                int ub = vars[index].getUB();
                for (int val = vars[index].getLB(); val <= ub; val = vars[index].nextValue(val)) {
                    currentTuple[index] = val;
                    if (!relation.isConsistent(currentTuple)) {
                        if (val == right + 1) {
                            right = val;
                        } else {
                            vars[index].removeInterval(left, right, this);
                            left = right = val;
                        }
                    }
                }
                vars[index].removeInterval(left, right, this);
            } else {
                if (!relation.isConsistent(currentTuple)) {
                    this.contradiction(null, "not consistent");
                }
            }
        }
    }
}
