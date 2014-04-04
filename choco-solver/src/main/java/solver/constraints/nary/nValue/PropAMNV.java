/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package solver.constraints.nary.nValue;

import solver.constraints.nary.nValue.amnv.graph.G;
import solver.constraints.nary.nValue.amnv.mis.F;
import solver.constraints.nary.nValue.amnv.rules.R;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * Propagator for the atMostNValues constraint
 * The number of distinct values in the set of variables vars is at most equal to nValues
 *
 * @since 01/01/2014
 * @author Jean-Guillaume Fages
 */
public class PropAMNV extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected G graph;
	protected F heur;
	protected R[] rules;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Creates a propagator for the atMostNValues constraint
	 * The number of distinct values in X is at most equal to N
	 */
	public PropAMNV(IntVar[] X, IntVar N, G graph, F heur, R[] rules){
		super(ArrayUtils.append(X, new IntVar[]{N}), PropagatorPriority.CUBIC, true);
		this.graph = graph;
		this.heur  = heur;
		this.rules = rules;
		graph.build();
	}

	//***********************************************************************************
	// ALGORITHMS
	//***********************************************************************************

	@Override
	protected int getPropagationConditions(int i) {
		return EventType.INT_ALL_MASK();
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if((evtmask & EventType.FULL_PROPAGATION.mask)!=0)
		graph.update();
		heur.prepare();
		do{
			heur.computeMIS();
			for(R rule:rules){
				rule.filter(vars,graph,heur,aCause);
			}
		}while(heur.hasNextMIS());
	}

	@Override
	public void propagate(int idxVarInProp, int mask) throws ContradictionException {
		if(idxVarInProp<vars.length-1){
			graph.update(idxVarInProp);
		}
		forcePropagate(EventType.CUSTOM_PROPAGATION);
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public ESat isEntailed() {
		// this is only a redundant propagator (solution checking uses the default NValue propagator)
		return ESat.TRUE;
	}
}
