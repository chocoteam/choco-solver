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
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.Variable;

import java.lang.reflect.Field;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19 nov. 2010
 */
public class PropReified extends Propagator<Variable> {

    public static final String MSG_ENTAILED = "Entailed false";

    protected BoolVar bVar;

    //    protected Constraint constraint, oppositeConstraint;
    protected final Propagator[] left, right;

    protected final IStateInt lastActiveR, lastActiveL;

    private static PropagatorPriority extractPriority(Propagator[] cons, Propagator[] oppCons) {
        int pc = 0;
        int poc = 0;
        for (int i = 0; i < cons.length; i++) {
            pc = Math.max(pc, cons[i].getPriority().priority);
        }
        for (int i = 0; i < oppCons.length; i++) {
            poc = Math.max(poc, oppCons[i].getPriority().priority);
        }
        return PropagatorPriority.get(Math.max(pc, poc));

    }

    public PropReified(Variable[] vars,
                       Propagator[] cons,
                       Propagator[] oppCons,
                       Solver solver,
                       Constraint<Variable, Propagator<Variable>> owner) {
        super(vars, solver, owner, extractPriority(cons, oppCons), false);
        this.bVar = (BoolVar) vars[0];
        left = cons;
        right = oppCons;
        Field state = null;

        try {
            state = Propagator.class.getDeclaredField("state");
            state.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        if(state == null){
            throw new SolverException("");
        }
        for (int i = 0; i < left.length; i++) {
            // disconnect propagator from variable
            for (int j = 0; j < left[i].getNbVars(); j++) {
                left[i].getVar(j).unlink(left[i]);

            }
            try {
                state.setShort(left[i], ACTIVE);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < right.length; i++) {
            // disconnect propagator from variable
            for (int j = 0; j < right[i].getNbVars(); j++) {
                right[i].getVar(j).unlink(right[i]);
            }
            try {
                state.setShort(right[i], ACTIVE);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        lastActiveL = environment.makeInt(left.length);
        lastActiveR = environment.makeInt(right.length);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int varIdx, int mask) throws ContradictionException {
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
                filter(left, lastActiveL);
                break;
            case FALSE:
                filter(right, lastActiveR);
                break;
        }
    }

    private void filter(Propagator[] propagators, IStateInt last) throws ContradictionException {
        int _last = last.get();
        Propagator prop;
        for (int p = 0; p < _last; p++) {
            prop = propagators[p];
            ESat entailed = prop.isEntailed();
            switch (entailed) {
                case FALSE:
                    contradiction(null, MSG_ENTAILED);
                    break;
                case TRUE:
                    //set passive: swap
                {
                    Propagator _prop = propagators[--_last];
                    propagators[_last] = prop;
                    propagators[p--] = _prop;
                    last.add(-1);
                }
                break;
                case UNDEFINED:
                    prop.propagate(EventType.FULL_PROPAGATION.mask);
                    if (prop.isPassive()) { //if the propagation has an impact on entailment
                        Propagator _prop = propagators[--_last];
                        propagators[_last] = prop;
                        propagators[p--] = _prop;
                        last.add(-1);
                    }
                    break;

            }
        }
    }

    public void filterFromConstraint() throws ContradictionException {
        ESat sat = entailed(left, lastActiveL);
        switch (sat) {
            case TRUE:
                bVar.setToTrue(this, false);
                this.setPassive();
                break;
            case FALSE:
                sat = entailed(right, lastActiveR);
                switch (sat) {
                    case TRUE:
                        bVar.setToFalse(this, false);
                        this.setPassive();
                        break;
                    case FALSE:
                        this.contradiction(bVar, "reified, inconsistency");
                        break;
                }
                break;
        }
    }

    private ESat entailed(Propagator[] propagators, IStateInt last) {
        int _last = last.get();
        int sat = 0;
        for (int i = 0; i < _last; i++) {
            ESat entail = propagators[i].isEntailed();
            if (entail.equals(ESat.FALSE)) {
                return entail;
            } else if (entail.equals(ESat.TRUE)) {
                sat++;
            }
        }
        if (sat == _last) {
            return ESat.TRUE;
        }
        // No need to check if FALSE, must have been returned before
        else {
            return ESat.UNDEFINED;
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
                return entailed(left, lastActiveL);
            } else {
                return entailed(right, lastActiveR);
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return constraint.toString();
    }
}
