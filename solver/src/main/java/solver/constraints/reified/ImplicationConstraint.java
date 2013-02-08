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

package solver.constraints.reified;

import common.ESat;
import common.util.tools.ArrayUtils;
import memory.structure.Operation;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.reified.PropImplied;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.Variable;

/**
 * Implication constraint: boolean b => constraint c
 * Also known as half reification
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 02/2013
 */
public class ImplicationConstraint extends Constraint<Variable, Propagator<Variable>> {

	// boolean variable of the reification
	private final BoolVar bool;
	// constraint to apply if bool = true
	private final Constraint targetCons;

	private static Variable[] extractVariable(BoolVar bVar, Constraint c) {
		Variable[] varsC = c.getVariables();
		for (int i = 0; i < varsC.length; i++) {
			if(varsC[i]==bVar){
				return varsC;
			}
		}
		return ArrayUtils.append(new Variable[]{bVar},varsC);
	}

	public ImplicationConstraint(BoolVar bVar, Constraint constraint) {
		super(extractVariable(bVar,constraint), bVar.getSolver());
		targetCons = constraint;
		bool = bVar;
		PropImplied reifProp = new PropImplied(bVar, this, targetCons);
		setPropagators(ArrayUtils.append(new Propagator[]{reifProp}, targetCons.propagators.clone()));
		for (int i = 1; i < propagators.length; i++) {
			propagators[i].setReifiedSilent();
			propagators[i].overrideCause(propagators[0]);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void activate() throws ContradictionException {
		assert bool.instantiated()&&bool.getBooleanValue()==ESat.TRUE;
		for (int p = 1; p < propagators.length; p++) {
			assert (propagators[p].isReifiedAndSilent());
			propagators[p].setReifiedTrue();
			propagators[p].propagate(EventType.FULL_PROPAGATION.strengthened_mask);
			solver.getEngine().onPropagatorExecution(propagators[p]);
		}
	}

	@Override
	public ESat isSatisfied() {
		if (bool.instantiated()) {
			if (bool.getValue() == 1) {
				return targetCons.isSatisfied();
			}
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}

	@Override
	public ESat isEntailed() {
		if (bool.instantiated()) {
			if (bool.getValue() == 1) {
				return targetCons.isEntailed();
			}
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}

	@Override
	public String toString() {
		return bool.toString() + "=>" + targetCons.toString();
	}
}
