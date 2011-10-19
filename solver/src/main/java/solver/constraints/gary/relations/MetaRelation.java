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
import solver.exception.ContradictionException;
import solver.variables.MetaVariable;

public abstract class MetaRelation extends GraphRelation<MetaVariable> {
	

	protected int dim;
	protected GraphRelation[] unidimRelation;
	
	protected MetaRelation(MetaVariable[] vars) {
		super(vars);
		dim = vars[0].getComponents().length;
		unidimRelation = new GraphRelation[dim];
	}

	@Override
	public ESat isEntail(int var1, int var2) {
		ESat entail = ESat.TRUE;
		for(int i=0; i<dim; i++){
			entail = and(entail, unidimRelation[i].isEntail(var1, var2));
			if(entail == ESat.FALSE){
				return entail;
			}
		}
		return entail;
	}
	
	@Override
	public void applyTrue(int var1, int var2, Solver solver, ICause cause, boolean informCause) throws ContradictionException {
		for(int i=0; i<dim; i++){
			unidimRelation[i].applyTrue(var1, var2, solver, cause, informCause);
		}
	}
	
	@Override
	public void applyFalse(int var1, int var2, Solver solver, ICause cause, boolean informCause) throws ContradictionException {
		for(int i=0; i<dim; i++){
			if (unidimRelation[i].isDirected() || !isDirected()){
				unidimRelation[i].applyFalse(var1, var2, solver, cause, informCause);
			}
		}
	}
	
	@Override
	public void applySymmetricFalse(int var1, int var2, Solver solver, ICause cause, boolean informCause) throws ContradictionException {
		for(int i=0; i<dim; i++){
			unidimRelation[i].applyFalse(var1, var2, solver, cause, informCause);
		}
	}
	
	@Override
	public boolean isDirected() {
		for(int i=0; i<dim; i++){
			if(unidimRelation[i].isDirected()){
				return true;
			}
		}
		return false;
	}
}
