/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
 */

package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

/**
 * Sums elements given by a set variable
 *
 * @author Jean-Guillaume Fages
 */
public class PropSumOfElements extends Propagator<Variable> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private IntVar sum;
	private SetVar set;
	private int offSet;
	private int[] weights;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Sums elements given by a set variable:
	 *
	 * if(weights !=null){
	 *     SUM(weights[i-offset] | i in setVar) = sum
	 *     (also ensures indexes is a subset of [offset, offset+weights.length-1])
	 * }else{
	 *     SUM(i | i in setVar) = sum
	 * }
	 *
	 * @param setVar a set variable
	 * @param weights array of int (can be null to sum the indexes)
	 * @param offset offset to access array cells (0 in Java, 1 in MiniZinc)
	 * @param sum integer variable representing the sum over the set
	 */
	public PropSumOfElements(SetVar setVar, int[] weights, int offset, IntVar sum) {
		super(new Variable[]{setVar, sum}, PropagatorPriority.BINARY, false);
		this.sum = (IntVar) vars[1];
		this.set = (SetVar) vars[0];
		this.weights = weights;
		this.offSet = offset;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		if (vIdx == 0) return SetEventType.all();
		else return IntEventType.boundAndInst();
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int lbSum = 0;
		int ubPosSum = 0;
		int ubNegSum = 0;
		ISetIterator iter = set.getUB().iterator();
		while (iter.hasNext()){
			int j = iter.nextInt();
			if(outOfScope(j)){
				set.remove(j,this);
			}else {
				if (set.getLB().contains(j)) {
					lbSum += get(j);
				} else if (get(j) >= 0) {
					ubPosSum += get(j);
				} else {
					ubNegSum += get(j);
				}
			}
		}
		int min = lbSum+ubNegSum;
		int max = lbSum+ubPosSum;
		sum.updateBounds(min, max, this);
		boolean again = false;
		// filter set
		int lb = sum.getLB();
		int ub = sum.getUB();
		iter = set.getUB().iterator();
		while (iter.hasNext()){
			int j = iter.nextInt();
			if (!set.getLB().contains(j)) {
				if(min + get(j) > ub || max + get(j) < lb){
					if (set.remove(j, this)) {
						again = true;
					}
				}
				if(max - get(j) < lb || min - get(j) > ub) {
					if (set.force(j, this)) {
						again = true;
					}
				}
			}
		}
		if (again) {
			propagate(0, 0);
		}
	}

	private boolean outOfScope(int j){
		return weights!=null && (j<offSet || j>=offSet+weights.length);
	}

	private int get(int j) {
		return (weights == null) ? j : weights[j - offSet];
	}

	@Override
	public ESat isEntailed() {
		int lbSum = 0;
		int ubPosSum = 0;
		int ubNegSum = 0;
		ISetIterator iter = set.getUB().iterator();
		while (iter.hasNext()){
			int j = iter.nextInt();
			if(set.getLB().contains(j)){
				if(outOfScope(j)){
					return ESat.FALSE;
				}
				lbSum += get(j);
			}else if(!outOfScope(j)) {
				if (get(j) >= 0) {
					ubPosSum += get(j);
				} else {
					ubNegSum += get(j);
				}
			}
		}
		int min = lbSum+ubNegSum;
		int max = lbSum+ubPosSum;
		if(sum.getLB()>max || sum.getUB()<min){
			return ESat.FALSE;
		}
		if (isCompletelyInstantiated()) {
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}
}
