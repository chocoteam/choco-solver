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
package org.chocosolver.solver.variables.impl;

import org.chocosolver.memory.IStateDouble;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.RealEventType;

/**
 * An implementation of RealVar, variable for continuous constraints (solved using IBEX).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/07/12
 */
public class RealVarImpl extends AbstractVariable implements RealVar {

    IStateDouble LB, UB;
    double precision;

    public RealVarImpl(String name, double lb, double ub, double precision, Model model) {
        super(name, model);
        this.LB = model.getEnvironment().makeFloat(lb);
        this.UB = model.getEnvironment().makeFloat(ub);
        this.precision = precision;
    }

    @Override
    public double getPrecision() {
        return precision;
    }

    @Override
    public double getLB() {
        return LB.get();
    }

    @Override
    public double getUB() {
        return UB.get();
    }

    @Override
    public boolean updateLowerBound(double value, ICause cause) throws ContradictionException {
        assert cause != null;
//        TODO ICause antipromo = cause;
        double old = this.getLB();
        if (old < value) {
            if (this.getUB() < value) {
//                TODO solver.getExplainer().updateLowerBound(this, old, value, antipromo);
                this.contradiction(cause, MSG_LOW);
            } else {
                LB.set(value);
                this.notifyPropagators(RealEventType.INCLOW, cause);

//                TODO solver.getExplainer().updateLowerBound(this, old, value, antipromo);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(double value, ICause cause) throws ContradictionException {
        assert cause != null;
//        TODO ICause antipromo = cause;
        double old = this.getUB();
        if (old > value) {
            if (this.getLB() > value) {
//                TODO solver.getExplainer().updateUpperBound(this, old, value, antipromo);
                this.contradiction(cause, MSG_UPP);
            } else {
                UB.set(value);
                this.notifyPropagators(RealEventType.DECUPP, cause);
//                TODO solver.getExplainer().updateUpperBound(this, old, value, antipromo);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateBounds(double lowerbound, double upperbound, ICause cause) throws ContradictionException {
        assert cause != null;
//        TODO ICause antipromo = cause;
        double oldlb = this.getLB();
        double oldub = this.getUB();
        if (oldlb < lowerbound || oldub > upperbound) {
            if (oldub < lowerbound || oldlb > upperbound) {
//                TODO solver.getExplainer()...
                this.contradiction(cause, MSG_BOUND);
            } else {
				RealEventType e = RealEventType.VOID;
                if (oldlb < lowerbound) {
                    LB.set(lowerbound);
                    e = RealEventType.INCLOW;
                }
                if (oldub > upperbound) {
                    UB.set(upperbound);
                    e = (e == RealEventType.INCLOW) ? RealEventType.BOUND : RealEventType.DECUPP;
                }
                this.notifyPropagators(e, cause);
//                TODO solver.getExplainer()...
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isInstantiated() {
        double lb = LB.get();
        double ub = UB.get();
        return ub - lb < precision || nextValue(lb) >= ub;
    }

    private double nextValue(double x) {
        if (x < 0) {
            return Double.longBitsToDouble(Double.doubleToLongBits(x) - 1);
        } else if (x == 0) {
            return Double.longBitsToDouble(1);
        } else if (x < Double.POSITIVE_INFINITY) {
            return Double.longBitsToDouble(Double.doubleToLongBits(x) + 1);
        } else {
            return x; // nextValue(infty) = infty
        }
    }

    @Override
    public NoDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public void createDelta() {
        throw new SolverException("Unable to create delta for RealVar!");
    }

    @Override
    public void notifyMonitors(IEventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }

    @Override
    public void contradiction(ICause cause, String message) throws ContradictionException {
        assert cause != null;
        model.getSolver().getEngine().fails(cause, this, message);
    }

    @Override
    public int getTypeAndKind() {
        return VAR | REAL;
    }

    @Override
    public String toString() {
        return String.format("%s = [%.16f,%.16f]", name, getLB(), getUB());
    }
}
