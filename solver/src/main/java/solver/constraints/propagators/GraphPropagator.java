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

package solver.constraints.propagators;

import choco.kernel.memory.IEnvironment;
import solver.constraints.Constraint;
import solver.requests.GraphRequest;
import solver.requests.IRequest;
import solver.requests.PropRequest;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;

public abstract class GraphPropagator<V extends Variable> extends Propagator<V>{

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	protected GraphPropagator(V[] vars, IEnvironment environment,
			Constraint<V, Propagator<V>> constraint,
			PropagatorPriority priority, boolean reactOnPromotion) {
		super(vars, environment, constraint, priority, reactOnPromotion);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@SuppressWarnings({"unchecked"})
	@Override
	protected void linkToVariables() {
		requests = new IRequest[vars.length];
		for (int i = 0; i < vars.length; i++) {
			vars[i].addPropagator(this, i);
			if (vars[i] instanceof GraphVar) {
				requests[i] = new GraphRequest(this, (GraphVar) vars[i], i);
			}else{
				requests[i] = new PropRequest<V, Propagator<V>>(this, vars[i], i);
			}
			vars[i].addRequest(requests[i]);
		}
	}
}
