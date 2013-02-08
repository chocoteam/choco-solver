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

package solver.constraints.propagators.reified;

import common.ESat;
import common.util.tools.ArrayUtils;
import memory.IStateInt;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.Variable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Implication propagator
 *
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 02/2013
 */
public class PropImplied extends Propagator<Variable> {

    private BoolVar bVar;
	private Constraint impliedCons;
    private final Propagator[] consProps;
	private final IStateInt lastActive;

	private static Variable[] extractVariable(BoolVar bVar, Constraint c) {
        Variable[] varsC = c.getVariables();
        for (int i = 0; i < varsC.length; i++) {
			if(varsC[i]==bVar){
				return varsC;
			}
        }
        return ArrayUtils.append(new Variable[]{bVar},varsC);
    }

    public PropImplied(BoolVar bool, Constraint cons) {
        super(extractVariable(bool,cons), PropagatorPriority.LINEAR, false);
		Propagator[] consP = cons.propagators.clone();
        this.bVar = bool;
		this.impliedCons = cons;
        this.consProps = consP;
        Field state = null;
        try {
            state = Propagator.class.getDeclaredField("state");
            state.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        if (state == null) {
            throw new SolverException("");
        }
        for (int i = 0; i < consProps.length; i++) {
            // disconnect propagator from variable
            consProps[i].unlink();
            try {
                state.setShort(consProps[i], ACTIVE);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            consProps[i].overrideCause(this);
        }
		lastActive = environment.makeInt(consProps.length);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        filter();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        forcePropagate(EventType.FULL_PROPAGATION);
    }

    public final void filter() throws ContradictionException {
        if (bVar.instantiated()) {
			if(bVar.getBooleanValue()==ESat.TRUE){
				filterFromBool();
			}
        }
		filterFromConstraint();
    }

    public final void filterFromBool() throws ContradictionException {
		int _last = lastActive.get();
        Propagator prop;
        for (int p = 0; p < _last; p++) {
            prop = consProps[p];
            ESat entailed = prop.isEntailed();
            switch (entailed) {
                case FALSE:
                    contradiction(null, "");
                    break;
                case TRUE:
                    //set passive: swap
                {
                    Propagator _prop = consProps[--_last];
                    consProps[_last] = prop;
                    consProps[p--] = _prop;
                    lastActive.add(-1);
                }
                break;
                case UNDEFINED:
                    prop.propagate(EventType.FULL_PROPAGATION.mask);
                    if (prop.isPassive()) { //if the propagation has an impact on entailment
                        Propagator _prop = consProps[--_last];
                        consProps[_last] = prop;
                        consProps[p--] = _prop;
                        lastActive.add(-1);
                    }
                    break;

            }
        }
    }

    public void filterFromConstraint() throws ContradictionException {
        ESat sat = targetEntailed();
		if(sat==ESat.FALSE){
			bVar.setToFalse(aCause);
		}
    }

    private ESat targetEntailed() {
		int _last = lastActive.get();
        int sat = 0;
        for (int i = 0; i < _last; i++) {
            ESat entail = consProps[i].isEntailed();
            if (entail.equals(ESat.FALSE)) {
                return entail;
            } else if (entail.equals(ESat.TRUE)) {
                sat++;
            }
        }
        if (sat == _last) {
            return ESat.TRUE;
        } else {
            return ESat.UNDEFINED;
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
		// we do not known which kind of variables are involved in the target constraint
        return EventType.ALL_FINE_EVENTS.mask;
    }

    @Override
    public ESat isEntailed() {
        if (bVar.instantiated()) {
            if (bVar.getValue() == 1) {
                return targetEntailed();
            } else {
                return ESat.TRUE;
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return bVar.toString() + "=>" + impliedCons.toString();
    }
}
