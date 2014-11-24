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


package org.chocosolver.solver.constraints.set;

import gnu.trove.map.hash.THashMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * 	Not Member propagator filtering Set->Int
 *  @author Jean-Guillaume Fages
 */
public class PropNotMemberSetInt extends Propagator<SetVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

    IntVar iv;
    SetVar sv;
    ISetDeltaMonitor sdm;
    IntProcedure elemRem;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNotMemberSetInt(IntVar intVar, SetVar setVar){
		super(new SetVar[]{setVar}, PropagatorPriority.UNARY, true);
		this.iv = intVar;
		this.sv = setVar;
		this.sdm = sv.monitorDelta(aCause);
		this.elemRem = new IntProcedure() {
			@Override
			public void execute(int i) throws ContradictionException {
				iv.removeValue(i, aCause);
			}
		};
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vidx){
		return SetEventType.ADD_TO_KER.getMask();
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int v=sv.getKernelFirst();v!=SetVar.END;v=sv.getKernelNext()){
			iv.removeValue(v,aCause);
		}
		if(sv.isInstantiated()) setPassive();
		sdm.unfreeze();
	}

	@Override
	public void propagate(int idxVarInProp, int mask) throws ContradictionException {
		sdm.freeze();
		sdm.forEach(elemRem, SetEventType.ADD_TO_KER);
		sdm.unfreeze();
		if(sv.isInstantiated()) setPassive();
	}

    @Override
    public ESat isEntailed() {
        if (iv.isInstantiated()) {
            int v = iv.getValue();
            if (sv.envelopeContains(v)) {
                if (sv.kernelContains(v)) {
                    return ESat.FALSE;
                } else {
                    return ESat.UNDEFINED;
                }
            } else {
                return ESat.TRUE;
            }
        } else {
            for (int v = iv.getLB(); v <= iv.getUB(); v = iv.nextValue(v)) {
                if (!sv.kernelContains(v)) {
                    return ESat.UNDEFINED;
                }
            }
        }
        return ESat.FALSE;
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            sv.duplicate(solver, identitymap);
            SetVar S = (SetVar) identitymap.get(sv);

            iv.duplicate(solver, identitymap);
            IntVar I = (IntVar) identitymap.get(iv);

            identitymap.put(this, new PropNotMemberSetInt(I, S));
        }

    }
}
