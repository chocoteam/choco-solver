/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.ternary;

import common.ESat;
import solver.constraints.Operator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * A constraint to state |x0 - x1| operator x2
 * where operator can be =, <=, >= and x1, x2, x3 are variables
 * Warning: only achieves BoundConsistency for the moment !
 *
 * @author Hadrien Cambazard, Charles Prud'homme
 * @since 06/04/12
 */
public final class PropDistanceXYZ extends Propagator<IntVar> {

    protected Operator operator;

    /**
     * Enforces |x0 - x1| op x2
     * where op can be =, <, >
     *
     * @param vars variable
     * @param op   the operator to be chosen among {0,1,2} standing for (eq,lt,gt)
     */
    public PropDistanceXYZ(IntVar[] vars, Operator op) {
        super(vars, PropagatorPriority.TERNARY, false);
        this.operator = op;
    }


    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filterFixPoint();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        filterFixPoint();
    }


    //*************************************************************//
    //********************** main filter **************************//
    //*************************************************************//

    public void filterFixPoint() throws ContradictionException {
        boolean change = true;
        while (change) {
            if (operator == Operator.EQ) {
                change = filterFromXYtoLBZ();
                change |= filterFromXYtoUBZ();
                change |= filterEQFromXZToY();
                change |= filterEQFromYZToX();
            } else if (operator == Operator.LT) {
                change = filterFromXYtoLBZ();
                change |= filterLTFromXZtoY();
                change |= filterLTFromYZtoX();
            } else if (operator == Operator.GT) {
                change = filterFromXYtoUBZ();
                change |= filterGTFromXZtoY();
                change |= filterGTFromYZtoX();
            }
        }
    }


    //*************************************************************//
    //********************** Bounds on Z **************************//
    //*************************************************************//

    //update lower bound of vars[2] if we have vars[0] != vars[1]
    //
    public boolean filterFromXYtoLBZ() throws ContradictionException {
        int t = vars[1].getLB() - vars[0].getUB();
        if (t > 0) { // x < y
            return vars[2].updateLowerBound(t, aCause);
        }
        t = vars[0].getLB() - vars[1].getUB();
        if (t > 0) { // x > y
            return vars[2].updateLowerBound(t, aCause);
        }
        return false;
    }

    //update upper bound of vars[2] as max(|vars[1].sup - vars[0].inf|, |vars[1].inf - vars[0].sup|)
    public boolean filterFromXYtoUBZ() throws ContradictionException {
        int a = Math.abs(vars[1].getUB() - vars[0].getLB());
        int b = Math.abs(vars[0].getUB() - vars[1].getLB());
        return vars[2].updateUpperBound((a > b) ? a : b, aCause);
    }

    //*************************************************************//
    //********************** EQ on XY *****************************//
    //*************************************************************//

    public boolean filterEQFromYZToX() throws ContradictionException {
        int l1 = vars[1].getLB();
        int u1 = vars[1].getUB();
        int l2 = vars[2].getLB();
        int u2 = vars[2].getUB();
        int lb = l1 - u2;
        int ub = u1 + u2;
        int lbv0 = u1 - l2 + 1;
        int ubv0 = l1 + l2 - 1;
        return vars[0].updateLowerBound(lb, aCause) | vars[0].updateUpperBound(ub, aCause) | vars[0].removeInterval(lbv0, ubv0, aCause);
    }

    public boolean filterEQFromXZToY() throws ContradictionException {
        int l0 = vars[0].getLB();
        int u0 = vars[0].getUB();
        int l2 = vars[2].getLB();
        int u2 = vars[2].getUB();
        int lb = l0 - u2;
        int ub = u0 + u2;
        int lbv1 = u0 - l2 + 1;
        int ubv1 = l0 + l2 - 1;
        return vars[1].updateLowerBound(lb, aCause) |
                vars[1].updateUpperBound(ub, aCause) |
                vars[1].removeInterval(lbv1, ubv1, aCause);
    }

    //*************************************************************//
    //********************** LT on XY *****************************//
    //*************************************************************//

    // LEQ: update x from the domain of z and y
    public boolean filterLTFromYZtoX() throws ContradictionException {
        int u2 = vars[2].getUB();
        int lb = vars[1].getLB() - u2 + 1;
        int ub = vars[1].getUB() + u2 - 1;
        return vars[0].updateLowerBound(lb, aCause) | vars[0].updateUpperBound(ub, aCause);
    }

    // LEQ: update x from the domain of z and y
    public boolean filterLTFromXZtoY() throws ContradictionException {
        int u2 = vars[2].getUB();
        int lb = vars[0].getLB() - u2 + 1;
        int ub = vars[0].getUB() + u2 - 1;
        return vars[1].updateLowerBound(lb, aCause) | vars[1].updateUpperBound(ub, aCause);
    }

    //*************************************************************//
    //********************** GT on XY *****************************//
    //*************************************************************//

    // GEQ: remove interval for x from the domain of z and y
    public boolean filterGTFromYZtoX() throws ContradictionException {
//        DisposableIntIterator it = vars[0].getDomain().getIterator();
//        boolean b = false;
//        while(it.hasNext()) {
//            int val = it.next();
//            if (Math.max(Math.abs(val- vars[1].getLB()),Math.abs(val - vars[1].getUB())) <= vars[2].getLB()) {
//                b |= vars[0].removeVal(val,cIdx0);
//            }
//        }
        int l2 = vars[2].getLB();
        int lbv0 = vars[1].getUB() - l2;
        int ubv0 = vars[1].getLB() + l2;
        // remove interval [lbv0, ubv0] from domain of vars[0]
        return vars[0].removeInterval(lbv0, ubv0, aCause);
    }

    // GEQ: remove interval for y from the domain of x and y
    public boolean filterGTFromXZtoY() throws ContradictionException {
//         DisposableIntIterator it = vars[1].getDomain().getIterator();
//         boolean b = false;
//         while(it.hasNext()) {
//             int val = it.next();
//             if (Math.max(Math.abs(vars[0].getLB() - val),Math.abs(vars[0].getUB() - val)) <= vars[2].getLB()) {
//                 b |= vars[1].removeVal(val,cIdx1);
//             }
//         }
//         return b;
        int l2 = vars[2].getLB();
        int lbv1 = vars[0].getUB() - l2;
        int ubv1 = vars[0].getLB() + l2;
        // remove interval [lbv0, ubv0] from domain of vars[0]
        return vars[1].removeInterval(lbv1, ubv1, aCause);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (operator == Operator.EQ) {
                return ESat.eval(Math.abs(vars[0].getValue() - vars[1].getValue()) == vars[2].getValue());
            } else if (operator == Operator.LT) {
                return ESat.eval(Math.abs(vars[0].getValue() - vars[1].getValue()) < vars[2].getValue());
            } else if (operator == Operator.GT) {
                return ESat.eval(Math.abs(vars[0].getValue() - vars[1].getValue()) > vars[2].getValue());
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        String op;
        if (operator == Operator.EQ) {
            op = "=";
        } else if (operator == Operator.GT) {
            op = ">";
        } else if (operator == Operator.LT) {
            op = "<";
        } else {
            throw new SolverException("unknown operator");
        }
        return "|" + vars[0] + " - " + vars[1] + "| " + op + " " + vars[2];
    }
}
