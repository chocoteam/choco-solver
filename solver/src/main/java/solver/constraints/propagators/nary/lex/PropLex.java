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
package solver.constraints.propagators.nary.lex;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;

/**
 * Enforce a lexicographic ordering on two vectors of integer
 * variables x <_lex y with x = <x_0, ..., x_n>, and y = <y_0, ..., y_n>.
 * ref : Global Constraints for Lexicographic Orderings (Frisch and al)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 10/08/11
 */
public class PropLex extends Propagator<IntVar> {

    public final int n;            // size of both vectors
    public final IStateInt alpha;  // size of both vectors
    public final IStateInt beta;
    public final IStateBool entailed;
    public final IntVar[] x;
    public final IntVar[] y;
    public final boolean strict;


    public PropLex(IntVar[] X, IntVar[] Y, boolean strict, Solver solver, Constraint<IntVar, Propagator<IntVar>> constraint) {
        super(ArrayUtils.append(X, Y), solver, constraint, PropagatorPriority.LINEAR, false);
        this.x = X.clone();
        this.y = Y.clone();

        this.strict = strict;
        this.n = X.length;
        alpha = environment.makeInt(0);
        beta = environment.makeInt(0);
        entailed = environment.makeBool(false);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    @Override
    public void propagate() throws ContradictionException {
        filter(alpha.get());
    }

    @Override
    public void propagateOnRequest(IRequest<IntVar> intVarIRequest, int vIdx, int mask) throws ContradictionException {
        if (vIdx < n) {
            filter(vIdx);
        } else {
            filter(vIdx - n);
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            for (int i = 0; i < x.length; i++) {
                int xi = vars[i].getValue();
                int yi = vars[i + n].getValue();
                if (xi < yi) {
                    return ESat.TRUE;
                } else if (xi > yi) {
                    return ESat.FALSE;
                }//else xi == yi
            }
            if (strict) {
                return ESat.FALSE;
            } else {
                return ESat.eval(vars[n - 1].getValue() == vars[n - 1 + n].getValue());
            }
        }
        return ESat.UNDEFINED;
    }


    /////////////////////
    public boolean groundEq(IntVar x1, IntVar y1) {
        return x1.instantiated() && y1.instantiated() && x1.getValue() == y1.getValue();
    }

    public boolean leq(IntVar x1, IntVar y1) {
        return x1.getUB() <= y1.getLB();
    }

    public boolean less(IntVar x1, IntVar y1) {
        return x1.getUB() < y1.getLB();
    }

    public boolean greater(IntVar x1, IntVar y1) {
        return x1.getLB() > y1.getUB();
    }

    public boolean checkLex(int i) {
        if (!strict) {
            if (i == n - 1) {
                return leq(x[i], y[i]);
            } else {
                return less(x[i], y[i]);
            }
        } else {
            return less(x[i], y[i]);
        }
    }

    public void ACleq(int i) throws ContradictionException {
        x[i].updateUpperBound(y[i].getUB(), this);
        y[i].updateLowerBound(x[i].getLB(), this);
    }

    public void ACless(int i) throws ContradictionException {
        x[i].updateUpperBound(y[i].getUB() - 1, this);
        y[i].updateLowerBound(x[i].getLB() + 1, this);
    }

    public void updateAlpha(int i) throws ContradictionException {
        if (i == beta.get()) {
            this.contradiction(null, "");
        }
        if (i == n) {
            entailed.set(true);
        } else {
            if (!groundEq(x[i], y[i])) {
                alpha.set(i);
                filter(i);
            } else {
                updateAlpha(i + 1);
            }
        }
    }

    public void updateBeta(int i) throws ContradictionException {
        if ((i + 1) == alpha.get()) {
            this.contradiction(null, "");
        }
        if (x[i].getLB() < y[i].getUB()) {
            beta.set(i + 1);
            if (x[i].getUB() >= y[i].getLB()) {
                filter(i);
            }
        } else if (x[i].getLB() == y[i].getUB()) {
            updateBeta(i - 1);
        }
    }

    public void initialize() throws ContradictionException {
        entailed.set(false);
        int i = 0;
        while (i < n && groundEq(x[i], y[i])) {
            i++;
        }
        if (i == n) {
            if (!strict) {
                entailed.set(true);
            } else {
                this.contradiction(null, "");
            }
        } else {
            alpha.set(i);
            if (checkLex(i)) {
                entailed.set(true);
            }
            beta.set(-1);
            while (i != n && x[i].getLB() <= y[i].getUB()) {
                if (x[i].getLB() == y[i].getUB()) {
                    if (beta.get() == -1) {
                        beta.set(i);
                    }
                } else {
                    beta.set(-1);
                }
                i++;
            }
            if (i == n) {
                if (!strict) {
                    beta.set(Integer.MAX_VALUE);
                } else {
                    beta.set(n);
                }
            } else if (beta.get() == -1) {
                beta.set(i);
            }
            if (alpha.get() >= beta.get()) {
                this.contradiction(null, "");
            }
            filter(alpha.get());
        }
    }

    public void filter(int i) throws ContradictionException {
        if (i < beta.get() && !entailed.get()) {                   //Part A
            if (i == alpha.get() && (i + 1 == beta.get())) {        //Part B
                ACless(i);
                if (checkLex(i)) {
                    entailed.set(true);
                }
            } else if (i == alpha.get() && (i + 1 < beta.get())) {  //Part C
                ACleq(i);
                if (checkLex(i)) {
                    entailed.set(true);
                } else if (groundEq(x[i], y[i])) {
                    updateAlpha(i + 1);
                }
            } else if (alpha.get() < i && i < beta.get()) {         //Part D
                if (((i == beta.get() - 1) && x[i].getLB() == y[i].getUB()) || greater(x[i], y[i])) {
                    updateBeta(i - 1);
                }
            }
        }
    }


}
