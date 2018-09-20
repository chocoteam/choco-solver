/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.binpacking;

import java.util.Arrays;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import gnu.trove.list.array.TIntArrayList;

/**
 * Propagator for a Bin Packing constraint
 * Propagates item/bin allocations to bin loads
 * Reacts to item/bin allocation variables only
 *
 * The implementation is based on the following paper :  Paul Shaw. A constraint for bin packing. In Mark Wallace, editor, CP, volume 3258 of Lecture Notes in Computer Science, pages 648–662. Springer, 2004
 *
 * @author Arthur Godet : arth.godet@gmail.com
 * @since 18/09/18
 */
public class PropBinPacking extends Propagator<IntVar> {
	
	/** The offset. */
	private int offset;
	
	/** The items variables. */
	private IntVar[] items;
	
	/** The loads variables. */
	private IntVar[] loads;
	
	/** The weights of the items. */
	private int[] weights;
	
	/** The total weight : it is the sum of all the weights. */
	private int totalWeight;
	
	/** The sum of the lower bounds of loads variables. */
	private int minLoad;
	
	/** The sum of the upper bounds of loads variables. */
	private int maxLoad;
	
	/** First value computed by the noSum algorithm. */
	private int a;
	
	/** Second value computed by the noSum algorithm. */
	private int b;
	
	/** P sets, representing the possible values that can be packed in each bin. */
	private TIntArrayList[] P;
	
	/** R sets, representing the items already packed in each bin. */
	private TIntArrayList[] R;
	
	/** C sets, representing P\R. */
	private TIntArrayList[] C;
	
	/** Array used to compute the weights corresponding to R sets. */
	private int[] pj;
	
	/** Array used to compute the weights corresponding to the P sets. */
	private int[] sumPj;
	
	/**
	 * Instantiates a new propagator of the bin packing constraint.
	 *
	 * @param items the items
	 * @param loads the loads
	 * @param weights the weights
	 * @param offset the offset
	 */
	public PropBinPacking(IntVar[] items, IntVar[] loads, int[] weights, int offset) {
		super(ArrayUtils.append(items, loads));
		this.items = items;
		this.loads = loads;
		this.weights = weights;
		this.totalWeight = 0;
		for(int w : weights) {
			totalWeight += w;
		}
		this.minLoad = 0;
		this.maxLoad = 0;
		this.offset = offset;
		this.C = new TIntArrayList[loads.length];
		this.P = new TIntArrayList[loads.length];
		this.R = new TIntArrayList[loads.length];
		this.pj = new int[loads.length];
		this.sumPj = new int[loads.length];
		for(int j = 0; j<loads.length; j++) {
			C[j] = new TIntArrayList();
			P[j] = new TIntArrayList();
			R[j] = new TIntArrayList();
		}
	}
	
	/**
	 * Returns the first value computed by the last call to the noSum function.
	 * Used only for tests purposes.
	 *
	 * @return the first value computed by the last call to the noSum function
	 */
	public int getA() {
		return this.a;
	}
	
	/**
	 * Returns the second value computed by the last call to the noSum function.
	 * Used only for tests purposes.
	 *
	 * @return the second value computed by the last call to the noSum function
	 */
	public int getB() {
		return this.b;
	}

	/**
	 * Sums the weights of the items in the set. The set contains the index of the items.
	 *
	 * @param set a set of the items
	 * @param weights the weights
	 * @return the sum of the weights of the items contained in the set
	 */
	private static int sum(TIntArrayList set, int[] weights) {
		int res = 0;
		int size = set.size();
		for(int i = 0; i<size; i++) {
			res += weights[set.get(i)];
		}
		return res;
	}

	/**
	 * NoSum algorithm described in the paper cited in the header. Items' index in the set are sorted by decreasing weight.
	 *
	 * @param set a set of items
	 * @param weights the weights
	 * @param alpha the alpha bound
	 * @param beta the beta bound
	 * @return the int[]
	 */
	public boolean noSum(TIntArrayList set, int[] weights, int alpha, int beta) {
		if(alpha <= 0 || sum(set,weights) <= beta) {
			return false;
		}
		int sumA = 0;
		int sumB = 0;
		int sumC = 0;
		int k = 0; // k largest items
		int k2 = 0; // k2 smallest items
		
		int size = set.size();

		while(k2 < size && sumC + weights[set.get(size-1-k2)] < alpha) {
			sumC += weights[set.get(size-1-k2)];
			k2++;
		}
		if(k2 == size) {
			k2--;
		}
		sumB = weights[set.get(size-1-k2)];
		while(k2 >=0 && sumA<alpha && sumB <= beta) {
			k++;
			sumA += weights[set.get(k-1)];
			if(sumA < alpha) {
				k2--;
				sumB += weights[set.get(size-1-k2)];
				sumC -= weights[set.get(size-1-k2)];
				while(sumA+sumC >= alpha) {
					k2--;
					sumC -= weights[set.get(size-1-k2)];
					sumB += (weights[set.get(size-1-k2)]-weights[set.get(size-1-k2-k-1)]);
				}
			}
		}
		if(sumA < alpha) {
			a = sumA+sumC;
			b = sumB;
			return true;
		}
		else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.chocosolver.solver.constraints.Propagator#isEntailed()
	 */
	@Override
	public ESat isEntailed() {
		if(this.isCompletelyInstantiated()) {
			int[] bins = new int[loads.length];
			for(int i = 0; i<items.length; i++) {
				bins[items[i].getValue()-this.offset] += weights[i];
			}
			for(int j = 0; j<loads.length; j++) {
				if(bins[j]!=loads[j].getValue()) {
					return ESat.FALSE;
				}
			}
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}

	/**
	 * Inserts the item idx in the list such that the list's items are sorted by decreasing weight.
	 * idx should be between 0 (include) and weights.length (exclude).
	 *
	 * @param list the list
	 * @param weights the weights
	 * @param idx the index of the item to add
	 */
	public static void order(TIntArrayList list, int[] weights, int idx) {
		assert 0<=idx && idx<weights.length : "idx should be betwwen 0 (include) and weights.length (exclude)";
		int i = 0;
		while(i<list.size() && weights[list.get(i)]>weights[idx]) {
			i++;
		}
		list.insert(i, idx);
	}
	
	/**
	 * Load and Size Coherence rule.
	 *
	 * @return true, if a modification has been made on the items or loads variables
	 * @throws ContradictionException the contradiction exception
	 */
	private boolean loadAndSizeCoherence() throws ContradictionException { // 2.5 Load and Size Coherence
		boolean modif = false;
		for(int j = 0; j<loads.length; j++) {
			boolean localModif = false;
			int lb = this.totalWeight-(maxLoad-loads[j].getUB());
			int ub = this.totalWeight-(minLoad-loads[j].getLB());
			if(lb>loads[j].getLB()) {
				localModif = true;
				minLoad = minLoad - loads[j].getLB() + lb;
				loads[j].updateLowerBound(lb, this);
				modif = true;
			}
			if(ub<loads[j].getUB()) {
				localModif = true;
				maxLoad = maxLoad - loads[j].getUB() + ub;
				loads[j].updateUpperBound(ub, this);
				modif = true;
			}
			if(localModif) {
				j = -1;
			}
		}
		return modif;
	}
	
	/**
	 * Load Maintenance rule.
	 *
	 * @return true, if a modification has been made on the items or loads variables
	 * @throws ContradictionException the contradiction exception
	 */
	private boolean loadMaintenance() throws ContradictionException { // 2.5 Load Maintenance
		boolean modif = false;
		for(int j = 0; j<loads.length; j++) {
			if(loads[j].getLB()<pj[j] || loads[j].getUB()>sumPj[j]) {
				modif = true;
				int lb = loads[j].getLB();
				int ub = loads[j].getUB();
				loads[j].updateBounds(pj[j], sumPj[j], this);
				minLoad += loads[j].getLB()-lb;
				maxLoad += loads[j].getUB()-ub;
			}
		}
		return modif;
	}
	
	/**
	 * Single Item Elimination and Commitment rule.
	 *
	 * @return true, if a modification has been made on the items or loads variables
	 * @throws ContradictionException the contradiction exception
	 */
	private boolean singleItemEliminationAndCommitment() throws ContradictionException{ // 2.5 Single Item Elimination and Commitment
		boolean modif = false;
		for(int j = 0; j<loads.length; j++) {
			for(int k = 0; k<C[j].size(); k++) {
				boolean localModif = false;
				int i = C[j].get(k);
				if(pj[j]+weights[i]>loads[j].getUB()) {
					localModif = true;
					items[i].removeValue(j+offset, this);
					C[j].removeAt(k);
					P[j].remove(i);
					sumPj[j] -= weights[i];
					modif = true;
				}
				else if(sumPj[j]-weights[i]<loads[j].getLB()) {
					localModif = true;
					items[i].instantiateTo(j+offset, this);
					C[j].removeAt(k);
					R[j].add(i);
					pj[j] += weights[i];
					modif = true;
				}
				if(localModif) {
					k = -1;
				}
			}
		}
		return modif;
	}
	
	/**
	 * Pruning rule.
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	private void pruningRule() throws ContradictionException { // 3.2 Pruning Rule
		for(int j = 0; j<loads.length; j++) {
			if(noSum(C[j], weights, loads[j].getLB()-pj[j], loads[j].getUB()-pj[j])) {
				throw new ContradictionException();
			}
		}
	}
	
	/**
	 * Tightening Bounds on Bin Load rule.
	 *
	 * @return true, if a modification has been made on the items or loads variables
	 * @throws ContradictionException the contradiction exception
	 */
	private boolean tighteningBoundsOnBinLoad() throws ContradictionException { // 3.3 Tightening Bounds on Bin Load
		boolean modif = false;
		for(int j = 0; j<loads.length; j++) {
			int lb = loads[j].getLB();
			int ub = loads[j].getUB();
			if(noSum(C[j], weights, lb-pj[j], lb-pj[j])) {
				loads[j].updateLowerBound(pj[j]+b, this);
				minLoad += loads[j].getLB()-lb;
				modif = true;
			}
			if(noSum(C[j], weights, ub-pj[j], ub-pj[j])) {
				loads[j].updateUpperBound(pj[j]+a, this);
				maxLoad += loads[j].getUB()-ub;
				modif = true;
			}
		}
		return modif;
	}
	
	/**
	 * Elimination and Commitment of Items rule.
	 *
	 * @return true, if a modification has been made on the items or loads variables
	 * @throws ContradictionException the contradiction exception
	 */
	private boolean eliminationAndCommitmentOfItems() throws ContradictionException { // 3.4 Elimination and Commitment of Items
		boolean modif = false;
		for(int j = 0; j<loads.length; j++) {
			boolean localModif = false;
			if(C[j].size()>=2) {
				for(int k = 0; k<C[j].size(); k++) {
					int i = C[j].removeAt(k);
					if(noSum(C[j], weights, loads[j].getLB()-pj[j]-weights[i], loads[j].getUB()-pj[j]-weights[i])) {
						localModif = true;
						items[i].removeValue(j+offset, this);
						P[j].remove(i);
						sumPj[j] -= weights[i];
						modif = true;
						k--;
					}
					else if(noSum(C[j], weights, loads[j].getLB()-pj[j], loads[j].getUB()-pj[j])) {
						localModif = true;
						items[i].instantiateTo(j+offset, this);
						R[j].add(i);
						pj[j] += weights[i];
						modif = true;
						k--;
					}
					else {
						C[j].insert(k, new Integer(i));
					}
				}
			}
			if(localModif) {
				j--;
			}
		}
		return modif;
	}
	
	/**
	 * Initializes variables used by the filtering algorithm.
	 */
	private void init() {
		this.minLoad = 0;
		this.maxLoad = 0;
		Arrays.fill(pj, 0);
		Arrays.fill(sumPj, 0);
		for(int j = 0; j<loads.length; j++) {
			P[j].clear();
			R[j].clear();
			C[j].clear();
			this.minLoad += loads[j].getLB();
			this.maxLoad += loads[j].getUB();
		}
		
		for(int i = 0; i<items.length; i++) {
			if(items[i].isInstantiated()) {
				int value = items[i].getValue();
				R[value-this.offset].add(i);
				pj[value-this.offset] += weights[i];
				order(P[value-this.offset], weights, i);
				sumPj[value-offset] += weights[i];
			}
			else {
				for(int value = items[i].getLB(); value <= items[i].getUB(); value = items[i].nextValue(value)) {
					order(P[value-this.offset], weights, i);
					order(C[value-this.offset], weights, i);
					sumPj[value-offset] += weights[i];
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.chocosolver.solver.constraints.Propagator#propagate(int)
	 */
	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if(this.getModel().getSolver().getDecisionCount()==0) { // 2.5 Pack all
			for(int i = 0; i<items.length; i++) {
				items[i].updateLowerBound(this.offset, this);
				items[i].updateUpperBound(loads.length-(1-this.offset), this);
			}
		}
		
		this.init();
		
		boolean modif = false;
		do {
			modif = false;
			modif |= this.loadMaintenance(); // 2.5 Load Maintenance
			modif |= this.loadAndSizeCoherence(); // 2.5 Load and Size Coherence
			modif |= this.singleItemEliminationAndCommitment(); // 2.5 Single Item Elimination and Commitment
			this.pruningRule(); // 3.2 Pruning Rule
			modif |= this.tighteningBoundsOnBinLoad(); // 3.3 Tightening Bounds on Bin Load
			modif |= this.eliminationAndCommitmentOfItems(); // 3.4 Elimination and Commitment of Items
		} while(modif);
		
	}
	
	@Override
	public int getPropagationConditions(int vIdx) {
		if(vIdx>=items.length) {
			return IntEventType.boundAndInst();
		}
		return IntEventType.all();
	}
	
}
