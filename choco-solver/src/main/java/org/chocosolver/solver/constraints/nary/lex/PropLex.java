/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.lex;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

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
    public boolean entailed;
    public final IntVar[] x;
    public final IntVar[] y;
    public final boolean strict;


    public PropLex(IntVar[] X, IntVar[] Y, boolean strict) {
        super(ArrayUtils.append(X, Y), PropagatorPriority.LINEAR, true);
        this.x = Arrays.copyOfRange(vars, 0, X.length);
        this.y = Arrays.copyOfRange(vars, X.length, vars.length);

        this.strict = strict;
        this.n = X.length;
        IEnvironment environment = solver.getEnvironment();
        alpha = environment.makeInt(0);
        beta = environment.makeInt(0);
        entailed = false;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            initialize();
        } else {
            gacLexLeq(alpha.get());
        }
    }

    @Override
    public void propagate(int vIdx, int mask) throws ContradictionException {
        entailed = false;
        if (vIdx < n) {
            gacLexLeq(vIdx);
        } else {
            gacLexLeq(vIdx - n);
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
        return x1.isInstantiated() && y1.isInstantiated() && x1.getValue() == y1.getValue();
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
            fails();
        }
        if (i == n) {
            if (strict) {
                fails();
            } else {
                entailed = true;
                setPassive();
                return;
            }
        }
        if (!groundEq(x[i], y[i])) {
            alpha.set(i);
            gacLexLeq(i);
        } else {
            updateAlpha(i + 1);
        }
    }

    public void updateBeta(int i) throws ContradictionException {
        if ((i + 1) == alpha.get()) {
            fails();
        }
        if (x[i].getLB() < y[i].getUB()) {
            beta.set(i + 1);
            if (x[i].getUB() >= y[i].getLB()) {
                gacLexLeq(i);
            }
        } else if (x[i].getLB() == y[i].getUB()) {
            updateBeta(i - 1);
        }
    }

    /**
     * Build internal structure of the propagator, if necessary
     *
     * @throws org.chocosolver.solver.exception.ContradictionException if initialisation encounters a contradiction
     */
    protected void initialize() throws ContradictionException {
        entailed = false;
        int i = 0;
        int a, b;
        while (i < n && groundEq(x[i], y[i])) {
            i++;
        }
        if (i == n) {
            if (!strict) {
                entailed = true;
                setPassive();
            } else {
                fails();
            }
        } else {
            a = i;
            if (checkLex(i)) {
                setPassive();
                return;
            }
            b = -1;
            while (i != n && x[i].getLB() <= y[i].getUB()) {
                if (x[i].getLB() == y[i].getUB()) {
                    if (b == -1) {
                        b = i;
                    }
                } else {
                    b = -1;
                }
                i++;
            }

            if (!strict && i == n) {
                b = Integer.MAX_VALUE;
            }
            if (b == -1) {
                b = i;
            }
            if (a >= b) {
                fails();
            }
            alpha.set(a);
            beta.set(b);
            gacLexLeq(a);
        }
    }

    public void gacLexLeq(int i) throws ContradictionException {
        int a = alpha.get();
        int b = beta.get();
        //Part A
        if (i >= b || entailed) {
            return;
        }
        //Part B
        if (i == a && (i + 1) == b) {
            ACless(i);
            if (checkLex(i)) {
                entailed = true;
                setPassive();
                return;
            }
        }
        //Part C
        if (i == a && (i + 1) < b) {
            ACleq(i);
            if (checkLex(i)) {
                entailed = true;
                setPassive();
                return;
            }
            if (groundEq(x[i], y[i])) {
                updateAlpha(i + 1);
            }
        }
        //Part D
        if (a < i && i < b) {
            if ((i == (b - 1) && x[i].getLB() == y[i].getUB()) || greater(x[i], y[i])) {
                updateBeta(i - 1);
            }
        }
    }


    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("LEX <");
        int i = 0;
        for (; i < Math.min(this.x.length - 1, 2); i++) {
            sb.append(this.x[i]).append(", ");
        }
        if (i == 2 && this.x.length - 1 > 2) sb.append("..., ");
        sb.append(this.x[x.length - 1]);
        sb.append(">, <");
        i = 0;
        for (; i < Math.min(this.y.length - 1, 2); i++) {
            sb.append(this.y[i]).append(", ");
        }
        if (i == 2 && this.y.length - 1 > 2) sb.append("..., ");
        sb.append(this.y[y.length - 1]);
        sb.append(">");

        return sb.toString();
    }

}
