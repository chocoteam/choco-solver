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
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Maintain a link between a set variable and the union of values taken by an array of
 * integer variables
 *
 * Not idempotent (use two of them)
 *
 * @author Jean-Guillaume Fages
 */
public class PropSetIntValuesUnion extends Propagator<Variable> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected IntVar[] X;
	protected SetVar values;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropSetIntValuesUnion(IntVar[] X, SetVar values){
		super(ArrayUtils.append(X,new Variable[]{values}));
		this.X = X;
		this.values = values;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int v=values.getEnvelopeFirst();v!=SetVar.END;v=values.getEnvelopeNext()){
			int support = -1;
			for(int i=0;i<X.length;i++){
				if(X[i].contains(v)){
					if(support==-1){
						support = i;
					}else{
						support = -2;
						break;
					}
				}
			}
			if(support == -1){
				values.removeFromEnvelope(v, this);
			}else if(support!=-2 && values.kernelContains(v)){
				X[support].instantiateTo(v, this);
			}
		}
		for(int i=0;i<X.length;i++){
			if(X[i].isInstantiated()){
				values.addToKernel(X[i].getValue(), this);
			}else {
				for (int v = X[i].getLB(); v <= X[i].getUB(); v = X[i].nextValue(v)) {
					if (!values.envelopeContains(v)) {
						X[i].removeValue(v, this);
					}
				}
			}
		}
	}

	@Override
	public ESat isEntailed() {
		for(int v=values.getKernelFirst();v!=SetVar.END;v=values.getKernelNext()){
			int support = -1;
			for(int i=0;i<X.length;i++){
				if(X[i].contains(v)){
					if(support==-1){
						support = i;
					}else{
						support = -2;
						break;
					}
				}
			}
			if(support == -1){
				return ESat.FALSE;
			}
		}
		for(IntVar x:X){
			if(x.isInstantiated() && !values.envelopeContains(x.getValue())){
				return ESat.FALSE;
			}
		}
		if(isCompletelyInstantiated()){
			return ESat.TRUE;
		}return ESat.UNDEFINED;
	}
}
