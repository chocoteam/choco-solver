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
package solver.constraints.gary.relations;

import choco.kernel.ESat;
import solver.ICause;
import solver.Solver;
import solver.constraints.gary.GraphProperty;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.util.BitSet;

public class Member extends GraphRelation<IntVar> {

	BitSet values;
	int firstVal;
	int lastVal;

	public Member(IntVar[] vars, int[] values){
		super(vars);
		this.values = new BitSet();
		firstVal = values[0];
		lastVal = values[0];
		for(int v:values){
			firstVal = Math.min(firstVal, v);
			lastVal = Math.max(lastVal, v);
		}
		for(int v:values){
			this.values.set(v-firstVal);
		}
	}

	@Override
	public ESat isEntail(int var1, int var2) {
		if(var1 != var2){
			return ESat.FALSE;
		}
		IntVar x = vars[var1];
		if(x.instantiated()){
			if(values.get(x.getValue()-firstVal)){
				return ESat.TRUE;
			}else{
				return ESat.FALSE;
			}
		}
		if(x.getLB()>lastVal || x.getUB()<firstVal){
			return ESat.FALSE;
		}
		if(!x.hasEnumeratedDomain()){
			return ESat.UNDEFINED;
		}
		int up = x.getUB();
		for(int i=x.getLB();i<=up;i=x.nextValue(i)){
			if(values.get(i-firstVal)){
				return ESat.UNDEFINED;
			}
		}
		return ESat.FALSE;
	}

	@Override
	public void applyTrue(int var1, int var2, Solver solver, ICause cause, boolean informCause) throws ContradictionException {
		if(var1 != var2){
			throw new UnsupportedOperationException("unappropriate question only loops are concerned by such a relation");
		}
		IntVar x = vars[var1];
		if(x.getLB()<firstVal){
			x.removeInterval(x.getLB(), firstVal-1, cause, informCause);
		}
		if(x.getUB()>lastVal){
			x.removeInterval(lastVal+1, x.getUB(), cause, informCause);
		}
		if(x.hasEnumeratedDomain()){
			int up = x.getUB();
			for(int v=x.getLB(); v<=up; v = x.nextValue(v)){
				if(!values.get(v-firstVal)){
					x.removeValue(v, cause, informCause);
				}
			}
		}
	}

	@Override
	public void applyFalse(int var1, int var2, Solver solver, ICause cause, boolean informCause) throws ContradictionException {
		if(var1 != var2){
			throw new UnsupportedOperationException("unappropriate question only loops are concerned by such a relation");
		}
		IntVar x = vars[var1];
		if(x.hasEnumeratedDomain()){
			int up = Math.min(lastVal, x.getUB());
			int lb = Math.max(firstVal,x.getLB());
			for(int v=lb; v<=up; v = x.nextValue(v)){
				if(values.get(v-firstVal)){
					x.removeValue(v, cause, informCause);
				}
			}
		}else{
			x.removeInterval(firstVal, lastVal, cause, informCause);
		}
	}
	
	@Override
	public boolean isDirected() {
		return false;
	}
	
	@Override
	public GraphProperty[] getGraphProperties() {
		return new GraphProperty[]{};
	}
}
