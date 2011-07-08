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
import solver.Solver;
import solver.constraints.gary.GraphProperty;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

/**Relation of distance (fixed) between two integer variables 
 * Used for VRP (time windows)
 * Xi + MATRIX_i_j = Xj
 * 
 * @author Jean-Guillaume Fages
 *
 */
public class Dist_Int extends GraphRelation<IntVar> {
	
	private int[][] distanceMatrix;

	protected Dist_Int(IntVar[] vars, int[][] matrix) {
		super(vars);
		this.distanceMatrix = matrix;
		if(matrix.length!=matrix[0].length){
			throw new UnsupportedOperationException("the distance matrix should be squarred");
		}
	}

	@Override
	public ESat isEntail(int var1, int var2) {
		if(var1 == var2){
			if(distanceMatrix[var1][var2]==0){
				return ESat.TRUE;
			}else{
				return ESat.FALSE;
			}
		}
		IntVar x = vars[var1];
		IntVar y = vars[var2];
		if(x.getLB()+distanceMatrix[var1][var2]>y.getUB() || x.getUB()+distanceMatrix[var1][var2]<y.getLB()){
			return ESat.FALSE;
		}
		if(x.instantiated() && y.instantiated()){
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}
	
	@Override
	public void applyTrue(int var1, int var2, Solver solver, Propagator prop) throws ContradictionException {
		if(var1 != var2){
			IntVar x = vars[var1];
			IntVar y = vars[var2];
			x.updateLowerBound(y.getLB()-distanceMatrix[var1][var2], prop);
			x.updateUpperBound(y.getUB()-distanceMatrix[var1][var2], prop);
			y.updateLowerBound(x.getLB()+distanceMatrix[var1][var2], prop);
			y.updateUpperBound(x.getUB()+distanceMatrix[var1][var2], prop);
		}
	}
	
	@Override
	public void applyFalse(int var1, int var2, Solver solver, Propagator prop) throws ContradictionException {
		if(var1 != var2){
			IntVar x = vars[var1];
			IntVar y = vars[var2];
			if (x.instantiated()) {
	            y.removeValue(x.getValue()+distanceMatrix[var1][var2], prop);
	        } else if (y.instantiated()) {
	        	x.removeValue(y.getValue()-distanceMatrix[var1][var2], prop);
	        }
		}else if(distanceMatrix[var1][var2]==0){
//			vars[var1].contradiction(prop, "x != x"); 
		}
	}
	
	@Override
	public boolean isDirected() {
		return true;
	}
	
	@Override
	public GraphProperty[] getGraphProperties() {
		return new GraphProperty[]{GraphProperty.REFLEXIVITY};
	}
}
