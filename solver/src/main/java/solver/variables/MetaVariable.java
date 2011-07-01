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
package solver.variables;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.variables.domain.delta.NoDelta;

public class MetaVariable<V extends Variable> extends AbstractVariable implements Variable<NoDelta> {

	protected V[] components;
	protected int dim;
	
	public MetaVariable(String name, Solver sol, V[] vars){
		super(name, sol);
		components = vars;
		dim = vars.length;
	}

	@Override
	public boolean instantiated() {
		for(int i=0;i<dim;i++){
			if (!components[i].instantiated()){
				return false;
			}
		}return true;
	}


	@Override
	public Explanation explain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NoDelta getDelta() {
		return NoDelta.singleton;
	}
	
	public String toString() {
        String s = this.name +"\n";
        for(int i=0; i<dim; i++){
        	s+=components[i].toString();
        }
        return s;
    }

	public V[] getComponents() {
		return components;
	}

	@Override
	public void contradiction(ICause cause, String message) throws ContradictionException {
		engine.fails(cause, this, message);
	}
}
