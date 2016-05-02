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
package org.chocosolver.solver.constraints.set;

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

	private IntVar iv;
	private SetVar sv;

	private ISetDeltaMonitor sdm;
	private IntProcedure elemRem;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

    public PropNotMemberSetInt(IntVar intVar, SetVar setVar) {
        super(new SetVar[]{setVar}, PropagatorPriority.UNARY, true);
        this.iv = intVar;
        this.sv = setVar;
        this.sdm = sv.monitorDelta(this);
        this.elemRem = i -> iv.removeValue(i, this);
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
		for(int v:sv.getLB()){
			iv.removeValue(v, this);
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
            if (sv.getUB().contain(v)) {
                if (sv.getLB().contain(v)) {
                    return ESat.FALSE;
                } else {
                    return ESat.UNDEFINED;
                }
            } else {
                return ESat.TRUE;
            }
        } else {
            for (int v = iv.getLB(); v <= iv.getUB(); v = iv.nextValue(v)) {
                if (!sv.getLB().contain(v)) {
                    return ESat.UNDEFINED;
                }
            }
        }
        return ESat.FALSE;
    }

}
