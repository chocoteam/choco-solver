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

package solver.constraints.set;

import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.Variable;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * Maintain a link between a set variable and the union of values taken by an array of
 * integer variables
 *
 * Filters from set to ints
 *
 * @author Jean-Guillaume Fages
 */
public class PropSetIntValuesUnionB extends Propagator<SetVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected IntVar[] X;
	protected SetVar values;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropSetIntValuesUnionB(IntVar[] X, SetVar values){
		super(values);
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
			if(support!=-2 && values.kernelContains(v)){
				X[support].instantiateTo(v,aCause);
			}
		}
		for(int i=0;i<X.length;i++){
			for(int v=X[i].getLB();v<=X[i].getUB();v=X[i].nextValue(v)){
				if(!values.envelopeContains(v)){
					X[i].removeValue(v,aCause);
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
			return ESat.UNDEFINED;
		}return ESat.TRUE;
	}
}
