/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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


package solver.constraints.set;

import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.events.IntEventType;
import util.ESat;

/**
 * 	Not Member propagator filtering Int->Set
 *  @author Jean-Guillaume Fages
 */
public class PropNotMemberIntSet extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	IntVar iv;
	SetVar sv;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNotMemberIntSet(IntVar iv, SetVar sv){
		super(new IntVar[]{iv}, PropagatorPriority.UNARY, true);
		this.iv = iv;
		this.sv = sv;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vidx){
		return IntEventType.instantiation();
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if(iv.isInstantiated()){
			sv.removeFromEnvelope(iv.getValue(),aCause);
		}
	}

	@Override
	public void propagate(int vidx, int evtmask) throws ContradictionException {
		assert iv.isInstantiated();
		sv.removeFromEnvelope(iv.getValue(),aCause);
	}

	@Override
	public ESat isEntailed() {
		if(iv.isInstantiated()){
			int v = iv.getValue();
			if(sv.envelopeContains(v)){
				if(sv.kernelContains(v)){
					return ESat.FALSE;
				}else{
					return ESat.UNDEFINED;
				}
			}else{
				return ESat.TRUE;
			}
		}else{
			for(int v=iv.getLB();v<=iv.getUB();v=iv.nextValue(v)){
				if(!sv.kernelContains(v)){
					return ESat.UNDEFINED;
				}
			}
		}
		return ESat.FALSE;
	}
}
