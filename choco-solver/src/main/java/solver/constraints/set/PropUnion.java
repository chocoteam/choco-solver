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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package solver.constraints.set;

import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.SetVar;
import solver.variables.delta.ISetDeltaMonitor;
import solver.variables.delta.monitor.SetDeltaMonitor;
import util.ESat;
import util.procedure.IntProcedure;
import util.tools.ArrayUtils;

public class PropUnion extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int k;
    private ISetDeltaMonitor[] sdm;
    private IntProcedure unionForced, unionRemoved, setForced, setRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * The union of sets is equal to union
     *
     * @param sets
     * @param union
     */
    public PropUnion(SetVar[] sets, SetVar union) {
        super(ArrayUtils.append(sets, new SetVar[]{union}), PropagatorPriority.LINEAR, true);
        k = sets.length;
        sdm = new ISetDeltaMonitor[k + 1];
        for (int i = 0; i <= k; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        // PROCEDURES
        unionForced = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
				int mate = -1;
				for(int i=0;i<k && mate!=-2;i++){
					if(vars[i].envelopeContains(element)){
						if(mate == -1){
							mate = i;
						}else{
							mate = -2;
						}
					}
				}
				if(mate == -1){
					contradiction(vars[k],"");
				}else if(mate != -2){
					vars[mate].addToKernel(element,aCause);
				}
            }
        };
        unionRemoved = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                for (int i = 0; i < k; i++) {
                    vars[i].removeFromEnvelope(element, aCause);
                }
            }
        };
        setForced = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
                vars[k].addToKernel(element, aCause);
            }
        };
        setRemoved = new IntProcedure() {
            @Override
            public void execute(int element) throws ContradictionException {
				if(vars[k].envelopeContains(element)){
					int mate = -1;
					for(int i=0;i<k && mate!=-2;i++){
						if(vars[i].envelopeContains(element)){
							if(mate == -1){
								mate = i;
							}else{
								mate = -2;
							}
						}
					}
					if(mate == -1){
						vars[k].removeFromEnvelope(element,aCause);
					}else if(mate != -2 && vars[k].kernelContains(element)){
						vars[mate].addToKernel(element,aCause);
					}
				}
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
			SetVar union = vars[k];
            for (int i = 0; i < k; i++) {
                for (int j=vars[i].getKernelFirst(); j!=SetVar.END; j=vars[i].getKernelNext())
                    union.addToKernel(j, aCause);
                for (int j=vars[i].getEnvelopeFirst(); j!=SetVar.END; j=vars[i].getEnvelopeNext())
                    if (!union.envelopeContains(j))
                        vars[i].removeFromEnvelope(j, aCause);
            }
            for (int j=union.getEnvelopeFirst(); j!=SetVar.END; j=union.getEnvelopeNext()) {
                if (union.kernelContains(j)) {
					int mate = -1;
					for(int i=0;i<k && mate!=-2;i++){
						if(vars[i].envelopeContains(j)){
							if(mate == -1){
								mate = i;
							}else{
								mate = -2;
							}
						}
					}
					if(mate == -1){
						contradiction(vars[k],"");
					}else if(mate != -2){
						vars[mate].addToKernel(j,aCause);
					}
                } else {
                    int mate = -1;
                    for (int i = 0; i < k; i++) {
                        if (vars[i].envelopeContains(j)) {
                            mate = i;
                            break;
                        }
                    }
                    if (mate == -1) union.removeFromEnvelope(j, aCause);
                }
            }
			// ------------------
			if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0)
				for (int i = 0; i <= k; i++)
					sdm[i].unfreeze();
		}
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        sdm[idxVarInProp].freeze();
        if (idxVarInProp < k) {
            sdm[idxVarInProp].forEach(setForced, EventType.ADD_TO_KER);
            sdm[idxVarInProp].forEach(setRemoved, EventType.REMOVE_FROM_ENVELOPE);
        } else {
            sdm[idxVarInProp].forEach(unionForced, EventType.ADD_TO_KER);
            sdm[idxVarInProp].forEach(unionRemoved, EventType.REMOVE_FROM_ENVELOPE);
        }
        sdm[idxVarInProp].unfreeze();
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < k; i++) {
            for (int j=vars[i].getKernelFirst(); j!=SetVar.END; j=vars[i].getKernelNext())
                if (!vars[k].envelopeContains(j))
                    return ESat.FALSE;
        }
        for (int j=vars[k].getKernelFirst(); j!=SetVar.END; j=vars[k].getKernelNext()) {
            int mate = -1;
            for (int i = 0; i < k; i++)
                if (vars[i].envelopeContains(j)) {
                    mate = i;
                    break;
                }
            if (mate == -1) return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) return ESat.TRUE;
        return ESat.UNDEFINED;
    }
}
