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

import memory.IStateInt;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.variables.delta.ISetDeltaMonitor;
import util.ESat;
import util.procedure.IntProcedure;

/**
 * Propagator for Member constraint: iv is in set
 *
 * @author Jean-Guillaume Fages
 */
public class PropIntMemberSet extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar iv;
    private SetVar set;
    private ISetDeltaMonitor sdm;
    private IntProcedure elemRem;
	private IStateInt watchLit1, watchLit2;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for Member constraint
     * val(intVar) is in setVar
     *
     * @param setVar
     * @param intVar
     */
    public PropIntMemberSet(SetVar setVar, IntVar intVar) {
        super(new Variable[]{setVar, intVar}, PropagatorPriority.BINARY, true);
        this.iv = (IntVar) vars[1];
        this.set = (SetVar) vars[0];
        this.sdm = set.monitorDelta(this);
		watchLit1 = environment.makeInt(iv.getLB()-1);
		watchLit2 = environment.makeInt(iv.getLB()-1);
        elemRem = new IntProcedure() {
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
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVE_FROM_ENVELOPE.mask + EventType.INT_ALL_MASK();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (iv.isInstantiated()) {
            set.addToKernel(iv.getValue(), aCause);
            setPassive();
            return;
        }
		watchLitFilter();
        int maxVal = set.getEnvelopeFirst();
        int minVal = maxVal;
        for (int j = maxVal; j!=SetVar.END; j=set.getEnvelopeNext()) {
            if (maxVal < j) {
                maxVal = j;
            }
            if (minVal > j) {
                minVal = j;
            }
        }
        iv.updateUpperBound(maxVal, aCause);
        iv.updateLowerBound(minVal, aCause);
        minVal = iv.getLB();
        maxVal = iv.getUB();
        for (int i = minVal; i <= maxVal; i = iv.nextValue(i)) {
            if (!set.envelopeContains(i)) {
                iv.removeValue(i, aCause);
            }
        }
        if (iv.isInstantiated()) {
            set.addToKernel(iv.getValue(), aCause);
            setPassive();
        }
        sdm.unfreeze();
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        if (i == 1) {
			if(iv.isInstantiated()){
				set.addToKernel(iv.getValue(), aCause);
				setPassive();
			}else{
				watchLitFilter();
			}
        } else {
            sdm.freeze();
            sdm.forEach(elemRem, EventType.REMOVE_FROM_ENVELOPE);
            sdm.unfreeze();
            if (iv.isInstantiated()) {
                set.addToKernel(iv.getValue(), aCause);
                setPassive();
            }else{
				watchLitFilter();
			}
        }
    }

	private void watchLitFilter() throws ContradictionException {
		int def = iv.getLB()-1;
		int w1 = iv.contains(watchLit1.get())? watchLit1.get():def;
		int w2 = iv.contains(watchLit2.get())? watchLit2.get():def;
		if(w1 == def || w2 == def){
			for (int j = set.getEnvelopeFirst(); j!=SetVar.END; j=set.getEnvelopeNext()) {
				if(iv.contains(j)){
					if(w1 == def){
						w1 = j;
						watchLit1.set(j);
					}else if(w2 == def){
						w2 = j;
						watchLit2.set(j);
					}else{
						return;
					}
				}
			}
			if(w1 != def && w2 == def){
				set.addToKernel(w1,aCause);
				iv.instantiateTo(w1,aCause);
			}else if (w1 == def) {
				contradiction(iv,"");
			}
		}
	}

	@Override
    public ESat isEntailed() {
        if (iv.isInstantiated()) {
            if (!set.envelopeContains(iv.getValue())) {
                return ESat.FALSE;
            } else {
                if (set.isInstantiated()) {
                    return ESat.TRUE;
                } else {
                    return ESat.UNDEFINED;
                }
            }
        } else {
            int minVal = iv.getLB();
            int maxVal = iv.getUB();
            boolean all = true;
            for (int i = minVal; i <= maxVal; i = iv.nextValue(i)) {
                if (!set.kernelContains(i)) {
                    all = false;
                    break;
                }
            }
            if (all) {
                return ESat.TRUE;
            }
            for (int i = minVal; i <= maxVal; i = iv.nextValue(i)) {
                if (set.envelopeContains(i)) {
                    return ESat.UNDEFINED;
                }
            }
            return ESat.FALSE;
        }
    }
}
