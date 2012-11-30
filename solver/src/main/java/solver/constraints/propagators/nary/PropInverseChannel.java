/*
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

package solver.constraints.propagators.nary;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.UnaryIntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;

/**
 * X[i] = j+Ox <=> Y[j] = i+Oy
 *
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 * @since 30 sept. 2010
 */
@SuppressWarnings({"UnnecessaryLocalVariable"})
public class PropInverseChannel extends Propagator<IntVar> {

    protected int minX, minY;
    protected int n;
    protected IntVar[] X, Y;
    protected RemProc rem_proc;
    protected IIntDeltaMonitor[] idms;
	protected boolean allEnumerated;

    @SuppressWarnings({"unchecked"})
    public PropInverseChannel(IntVar[] X, IntVar[] Y, int minX, int minY, Solver solver, IntConstraint constraint) {
        super(ArrayUtils.append(X, Y), solver, constraint, PropagatorPriority.CUBIC, false);
		allEnumerated = true;
        for (int i = 0; i < this.vars.length; i++) {
			if(!vars[i].hasEnumeratedDomain()){
				allEnumerated = false;
			}
        }
        this.X = X;
        this.Y = Y;
        n = Y.length;
		this.minX = minX;
		this.minY = minY;
		if(allEnumerated){
			rem_proc = new RemProc();
			this.idms = new IIntDeltaMonitor[this.vars.length];
			for (int i = 0; i < n; i++) {
				idms[i] = this.vars[i].monitorDelta(this);
			}
		}else{
			
		}
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vars[vIdx].hasEnumeratedDomain()) {
            return EventType.INT_ALL_MASK();
        } else {
            return EventType.INSTANTIATE.mask + EventType.BOUND.mask;
        }
    }

	@Override
	public void propagate(int evtmask) throws ContradictionException {
//		setBounds();
//		if(bothEnumerated){
//		for(int i=0;i<n;i++){
//			X[i].updateUpperBound(n+minX,aCause);
//			Y[i].updateUpperBound(n+minY,aCause);
//		}
		for(int i=0;i<n;i++){
			X[i].updateLowerBound(minX,aCause);
			X[i].updateUpperBound(n-1+minX,aCause);
			Y[i].updateLowerBound(minY,aCause);
			Y[i].updateUpperBound(n-1+minY,aCause);
		}
		if(allEnumerated){
			for(int i=0;i<n;i++){
				enumeratedFilteringOfX(i);
				enumeratedFilteringOfY(i);
			}
			for(int i=0;i<vars.length;i++){
				idms[i].unfreeze();
			}
		}else{
			for(int i=0;i<n;i++){
				boundedFilteringOfX(i);
				boundedFilteringOfY(i);
			}
		}
//		for(int i=0;i<n;i++){
//			enumeratedFilteringOfX(i);
//			enumeratedFilteringOfY(i);
//		}
//		for(int i=0;i<vars.length;i++){
//			idms[i].unfreeze();
//		}
	}

	@Override
	public void propagate(int varIdx, int mask) throws ContradictionException {
		if(allEnumerated){
			idms[varIdx].freeze();
			idms[varIdx].forEach(rem_proc.set(varIdx), EventType.REMOVE);
			idms[varIdx].unfreeze();
		}else{
			//bounds
			if(varIdx<n){
				boundedFilteringOfX(varIdx);
			}else{
				boundedFilteringOfY(varIdx - n);
			}
		}
	}

//	private void setBounds() throws ContradictionException {
//		// X = |Y|
//		int max = X.getUB();
//		int min = X.getLB();
//		Y.updateUpperBound(max,aCause);
//		Y.updateLowerBound(-max,aCause);
//		Y.removeInterval(1-min,min-1,aCause);
//		/////////////////////////////////////////////////
//		int prevLB = X.getLB();
//		int prevUB = X.getUB();
//		min = Y.getLB();
//		max = Y.getUB();
//		if(max<=0){
//			X.updateLowerBound(-max,aCause);
//			X.updateUpperBound(-min,aCause);
//		}else if (min>=0){
//			X.updateLowerBound(min,aCause);
//			X.updateUpperBound(max,aCause);
//		}else {
//			if(Y.hasEnumeratedDomain()){
//				int mP = Y.nextValue(-1);
//				int mN = -Y.previousValue(1);
//				X.updateLowerBound(Math.min(mP,mN),aCause);
//			}
//			X.updateUpperBound(Math.max(-min,max),aCause);
//		}
//		if(prevLB!=X.getLB() || prevUB!=X.getUB())setBounds();
//	}

	private void enumeratedFilteringOfX(int var) throws ContradictionException {
		// X[i] = j+Ox <=> Y[j] = i+Oy
		int min = X[var].getLB();
		int	max = X[var].getUB();
		for(int v=min;v<=max;v=X[var].nextValue(v)){
			if(!Y[v-minX].contains(var+minY)){
				X[var].removeValue(v,aCause);
			}
		}
	}

	private void enumeratedFilteringOfY(int var) throws ContradictionException {
		// X[i] = j+Ox <=> Y[j] = i+Oy
		int min = Y[var].getLB();
		int	max = Y[var].getUB();
		for(int v=min;v<=max ;v=Y[var].nextValue(v)){
			if(!X[v-minY].contains(var+minX)){
				Y[var].removeValue(v,aCause);
			}
		}
	}

	private void boundedFilteringOfX(int var) throws ContradictionException {
		// X[i] = j+Ox <=> Y[j] = i+Oy
		int min = X[var].getLB();
		int	max = X[var].getUB();
		for(int v=min;v<=max;v=X[var].nextValue(v)){
			if(!Y[v-minX].contains(var+minY)){
				X[var].removeValue(v,aCause);
			}else{
				break;
			}
		}
		for(int v=max;v>=min;v=X[var].previousValue(v)){
			if(!Y[v-minX].contains(var+minY)){
				X[var].removeValue(v,aCause);
			}else{
				break;
			}
		}
	}

	private void boundedFilteringOfY(int var) throws ContradictionException {
		// X[i] = j+Ox <=> Y[j] = i+Oy
		int min = Y[var].getLB();
		int	max = Y[var].getUB();
		for(int v=min;v<=max ;v=Y[var].nextValue(v)){
			if(!X[v-minY].contains(var+minX)){
				Y[var].removeValue(v,aCause);
			}else{
				break;
			}
		}
		for(int v=max;v>=min;v=Y[var].previousValue(v)){
			if(!X[v-minY].contains(var+minX)){
				Y[var].removeValue(v,aCause);
			}else{
				break;
			}
		}
	}

	private class RemProc implements UnaryIntProcedure<Integer> {
		private int var;
		@Override
		public UnaryIntProcedure set(Integer idxVar) {
			this.var = idxVar;
			return this;
		}
		@Override
		public void execute(int val) throws ContradictionException {
			if(var<n){
				Y[val-minX].removeValue(var+minY,aCause);
			}else {
				X[val-minY].removeValue(var-n+minX,aCause);
			}
		}
	}

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            for (int i = 0; i < n; i++) {
                if (X[i].instantiated()) {
                    int j = X[i].getValue() - minX;
                    if (j < 0 || j > n) {
                        return ESat.FALSE;
                    }
                    if (Y[j].instantiated()) {
                        if (Y[j].getValue() != (i + minY)) {
                            return ESat.FALSE;
                        }
                    } else {
                        return ESat.UNDEFINED;
                    }
                } else {
                    return ESat.UNDEFINED;
                }
            }
            return ESat.TRUE;
        } else {
            return ESat.UNDEFINED;
        }
    }

    @Override
    public String toString() {
        return "Inverse({" + X[0] + "...}{" + Y[0] + "...})";
    }
}
