/**
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
package solver.constraints.nary.min_max;

import memory.IStateInt;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * <br/>
 *
 * @Jean-Guillaume Fages
 * @since 15/12/2013
 */
public class PropBoolMin extends Propagator<BoolVar> {

	final int n;
	final IStateInt x1,x2;

	public PropBoolMin(BoolVar[] variables, BoolVar maxVar) {
		super(ArrayUtils.append(variables,new BoolVar[]{maxVar}), PropagatorPriority.UNARY, true);
		n = variables.length;
		x1 = solver.getEnvironment().makeInt(-1);
		x2 = solver.getEnvironment().makeInt(-1);
		assert n>0;
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		x1.set(-1);
		x2.set(-1);
		for(int i=0; i<n; i++){
			if(!vars[i].isInstantiated()){
				if(x1.get()==-1) {
					x1.set(i);
				}else if(x2.get()==-1){
					x2.set(i);
				}
			}else if(vars[i].getValue()==0){
				if(vars[n].instantiateTo(0, aCause)){
					setPassive();
					return;
				}
			}
		}
		filter();
	}

	public void propagate(int idxVarInProp, int mask) throws ContradictionException {
		if(idxVarInProp == n){
			filter();
		} else{
			if(vars[idxVarInProp].isInstantiatedTo(0)){
				if(vars[n].instantiateTo(0, aCause)){
					setPassive();
					return;
				}
			}else if(idxVarInProp == x1.get() || idxVarInProp == x2.get()){
				if(idxVarInProp == x1.get()) {
					x1.set(x2.get());
				}
				x2.set(-1);
				for(int i=0; i<n; i++){
					if(i != x1.get() && !vars[i].isInstantiated()){
						x2.set(i);
						break;
					}
				}
				filter();
			}
		}
	}

	public void filter() throws ContradictionException {
		if(x1.get()==-1){
			if(vars[n].instantiateTo(1,aCause)) {
				setPassive();
				return;
			}
		}
		if(x2.get()==-1 && vars[n].isInstantiatedTo(0)){
			if(vars[x1.get()].instantiateTo(0, aCause)) {
				setPassive();
				return;
			}
		}
		if(vars[n].isInstantiatedTo(1)){
			for(int i=0; i<n; i++){
				vars[i].instantiateTo(1,aCause);
			}
		}
	}

	@Override
	public ESat isEntailed() {
		int lb = vars[n].getLB();
		for(int i=0; i<n; i++){
			if(vars[i].getUB()<lb){
				return ESat.FALSE;
			}
		}
		for(int i=0; i<n; i++){
			if(vars[i].getLB()<lb){
				return ESat.UNDEFINED;
			}
		}
		if(vars[n].isInstantiated()){
			for(int i=0; i<n; i++){
				if(vars[i].isInstantiatedTo(lb)){
					return ESat.TRUE;
				}
			}
		}
		return ESat.UNDEFINED;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("PropBoolMin ");
		sb.append(vars[n]).append(" = min({");
		sb.append(vars[0]);
		for (int i = 1; i < n; i++) {
			sb.append(", ");
			sb.append(vars[i]);
		}
		sb.append("})");
		return sb.toString();

	}
}
