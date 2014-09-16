/**
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
package solver.constraints.nary.min_max;

import gnu.trove.map.hash.THashMap;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.events.IntEventType;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * <br/>
 *
 * @Jean-Guillaume Fages
 * @since 15/12/2013
 */
public class PropMin extends Propagator<IntVar> {

	final int n;

	public PropMin(IntVar[] variables, IntVar maxVar) {
		super(ArrayUtils.append(variables,new IntVar[]{maxVar}), PropagatorPriority.LINEAR, false);
		n = variables.length;
		assert n>0;
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return IntEventType.boundAndInst();
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int idx = -1;
		int ub = vars[n].getUB()+1;
		int lb = ub;
		// update min
		for(int i=0; i<n; i++){
			lb = Math.min(lb,vars[i].getLB());
			ub = Math.min(ub,vars[i].getUB());
		}
		vars[n].updateLowerBound(lb,aCause);
		vars[n].updateUpperBound(ub,aCause);
		lb = vars[n].getLB();
		// back-propagation
		for(int i=0; i<n; i++){
			if(vars[i].getLB()<=ub){
				idx=idx==-1?i:-2;
				vars[i].updateLowerBound(lb,aCause);
			}
		}
		if(idx>=0){
			if(vars[idx].updateUpperBound(ub,aCause) && lb==ub){ // entailed
				setPassive();
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
		StringBuilder sb = new StringBuilder("PropMin ");
		sb.append(vars[n]).append(" = min({");
		sb.append(vars[0]);
		for (int i = 1; i < n; i++) {
			sb.append(", ");
			sb.append(vars[i]);
		}
		sb.append("})");
		return sb.toString();

	}

    @Override
        public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
            if (!identitymap.containsKey(this)) {
                int size = this.vars.length - 1;
                IntVar[] aVars = new IntVar[size];
                for (int i = 0; i < size; i++) {
                    this.vars[i].duplicate(solver, identitymap);
                    aVars[i] = (IntVar) identitymap.get(this.vars[i]);
                }
                this.vars[size].duplicate(solver, identitymap);
                IntVar M = (IntVar) identitymap.get(this.vars[size]);
                identitymap.put(this, new PropMin(aVars, M));
            }
    }
}
