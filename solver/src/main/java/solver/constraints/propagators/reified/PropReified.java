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

package solver.constraints.propagators.reified;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.Variable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19 nov. 2010
 */
public class PropReified extends Propagator<Variable> {

    protected BoolVar bVar;

    protected Constraint constraint, oppositeConstraint;

    public PropReified(Variable[] vars,
                       Constraint cons,
                       Constraint oppCons,
                       Solver solver,
                       Constraint<Variable, Propagator<Variable>> owner,
                       PropagatorPriority priority, boolean reactOnPromotion) {
        super(vars, solver, owner, priority, reactOnPromotion);
        this.bVar = (BoolVar) vars[0];
        this.constraint = cons;
        this.oppositeConstraint = oppCons;

        try {
            Method unlink = Propagator.class.getDeclaredMethod("unlinkVariables");
            unlink.setAccessible(true);
            Propagator[] props = constraint.propagators;
            for (int p = 0; p < props.length; p++) {
                props[p].setActive();
                unlink.invoke(props[p]);
            }
            props = oppositeConstraint.propagators;
            for (int p = 0; p < props.length; p++) {
                props[p].setActive();
                unlink.invoke(props[p]);
            }
            unlink.setAccessible(false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void propagate() throws ContradictionException {
        filter();
    }

    @Override
    public void propagateOnRequest(IRequest<Variable> variableIFineRequest, int varIdx, int mask) throws ContradictionException {
//        throw new UnsupportedOperationException();
        filter();
    }

    public final void filter() throws ContradictionException {
        if (bVar.instantiated()) {
            filterFromBool();
        } else {
            filterFromConstraint();
        }
    }

    public final void filterFromBool() throws ContradictionException {

        switch (bVar.getBooleanValue()) {
            case TRUE:
                constraint.filter();
                break;
            case FALSE:
                oppositeConstraint.filter();
                break;
        }
    }

    public void filterFromConstraint() throws ContradictionException {
        ESat sat = constraint.isEntailed();
        switch (sat) {
            case TRUE:
                bVar.setToTrue(this);
                this.setPassive();
                break;
            case FALSE:
                sat = oppositeConstraint.isEntailed();
                switch (sat) {
                    case TRUE:
                        bVar.setToFalse(this);
                        this.setPassive();
                        break;
                    case FALSE:
                        this.contradiction(bVar, "reified, inconsistency");
                        break;
                }
                break;
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INT_ALL_MASK();
    }

    @Override
    public ESat isEntailed() {
        if (vars[0].instantiated()) {
            BoolVar b = (BoolVar) vars[0];
            if (b.getValue() == 1) {
                return constraint.isEntailed();
            } else {
                return oppositeConstraint.isEntailed();
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return constraint.toString();
    }
}
