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

package solver.constraints.propagators.nary;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.UnaryIntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.Delta;
import solver.variables.delta.IntDelta;

/**
 * (X[i] = j' + Ox && j = j' + Ox) <=> (Y[j] = i' + Oy[j]  && i = i' + Oy[j])
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30 sept. 2010
 */
@SuppressWarnings({"UnnecessaryLocalVariable"})
public class PropInverseChannelingJGTEST extends Propagator<IntVar> {

	IntVar[] X, Y;
	int n;

	protected final RemProc rem_proc;

	@SuppressWarnings({"unchecked"})
	public PropInverseChannelingJGTEST(IntVar[] X, IntVar[] Y, Solver solver, IntConstraint constraint) {
		super(ArrayUtils.append(X, Y), solver, constraint, PropagatorPriority.CUBIC, true);
		this.X = X;
		this.Y = Y;
		n=X.length;
		rem_proc = new RemProc(this);
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INT_ALL_MASK();
	}

	IntVar[] other(IntVar[] set){
		if(set==X){
			return Y;
		}
		return X;
	}

	void awakeOnInst(IntVar[] set, int var, int val) throws ContradictionException {
		if(var>=n || val>=n){
			throw new UnsupportedOperationException();
		}
		other(set)[val].instantiateTo(var,this,false);
//		if(other(set)[val].instantiateTo(var,this,false) && other(set)[val].instantiated()){
//			awakeOnInst(other(set),val,other(set)[val].getValue());
//		}
	}

	void awakeOnRem(IntVar[] set, int var, int val) throws ContradictionException {
		if(var>=n || val>=n){
			throw new UnsupportedOperationException();
		}
		other(set)[val].removeValue(var,this,false);
//		if(other(set)[val].removeValue(var,this,false) && other(set)[val].instantiated()){
//			awakeOnInst(other(set),val,other(set)[val].getValue());
//		}
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		IntVar v;
		for(int i=0;i<n;i++){
			v = X[i];
			if(v.instantiated()){
				awakeOnInst(X,i,v.getValue());
			}else{
				for(int j=v.getLB();j<=v.getUB();j=v.nextValue(j)){
					if(!Y[j].contains(i)){
						v.removeValue(j,this,false);
						if(v.instantiated()){
							awakeOnInst(X,i,v.getValue());
						}
					}
				}
			}
			v = Y[i];
			if(v.instantiated()){
				awakeOnInst(Y,i,v.getValue());
			}else{
				for(int j=v.getLB();j<=v.getUB();j=v.nextValue(j)){
					if(!X[j].contains(i)){
						v.removeValue(j,this,false);
						if(v.instantiated()){
							awakeOnInst(Y,i,v.getValue());
						}
					}
				}
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int varIdx, int mask) throws ContradictionException {
		if (EventType.isInstantiate(mask)) {
			if(varIdx<n){
				awakeOnInst(X,varIdx,X[varIdx].getValue());
			}else{
				awakeOnInst(Y,varIdx-n,Y[varIdx-n].getValue());
			}
		} else {
			eventRecorder.getDeltaMonitor(vars[varIdx]).forEach(rem_proc.set(varIdx), EventType.REMOVE);
		}
	}

	@Override
	public ESat isEntailed() {
		if (isCompletelyInstantiated()) {
			for(int i=0;i<n;i++){
				if(Y[X[i].getValue()].getValue()!=i){
					return ESat.FALSE;
				}
			}
			return ESat.TRUE;
		} else {
			return ESat.UNDEFINED;
		}
	}


	private class RemProc implements UnaryIntProcedure<Integer> {

		private final PropInverseChannelingJGTEST p;
		private int idxVar;

		public RemProc(PropInverseChannelingJGTEST p) {
			this.p = p;
		}

		@Override
		public UnaryIntProcedure set(Integer idxVar) {
			this.idxVar = idxVar;
			return this;
		}

		@Override
		public void execute(int i) throws ContradictionException {
			if(idxVar<n){
//				System.out.println("REM "+idxVar+"->"+i);
				p.awakeOnRem(X,idxVar,i);
			}else{
//				System.out.println("REM "+(idxVar-n)+"<-"+i);
				p.awakeOnRem(Y,idxVar-n,i);
			}
		}
	}
}
