/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
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

	private final IntVar sum;
	private final SetVar set;
	private final int offSet;
	private final int[] weights;

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
		// filter set
		int lb = sum.getLB();
		int ub = sum.getUB();
		if (lb == min && ub == max) {
			return;
		}
		iter = set.getUB().iterator();
		while (iter.hasNext()){
			int j = iter.nextInt();
			if (!set.getLB().contains(j)) {
				if (min + get(j) > ub) {
					if (set.remove(j, this)) {
						// weights[j] is positive
						ubPosSum -= get(j);
					}
				} else {
					if (max + get(j) < lb) {
						if (set.remove(j, this)) {
							// weights[j] is negative
							ubNegSum -= get(j);
						}
					} else {
						if (max - get(j) < lb || min - get(j) > ub) {
							if (set.force(j, this)) {
								// weight[j] is positive
								lbSum += get(j);
							}
						}
					}
				}
			}
		}
		min = lbSum + ubNegSum;
		max = lbSum + ubPosSum;
		sum.updateBounds(min, max, this);
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
