/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.binPacking;

import java.util.ArrayList;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * Propagator for a Bin Packing constraint
 * Propagates item/bin allocations to bin loads
 * Reacts to item/bin allocation variables only
 *
 * The implementation is based on the following paper :  Paul Shaw. A constraint for bin packing. In Mark Wallace, editor, CP, volume 3258 of Lecture Notes in Computer Science, pages 648–662. Springer, 2004
 *
 * @author Arthur Godet : arth.godet@gmail.com
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

	/**
	 * Instantiates a new propagator of the bin packing constraint.
	 *
	 * @param items the items
	 * @param loads the loads
	 * @param weights the weights
	 * @param offset the offset
	 */
	public PropBinPacking(IntVar[] items, IntVar[] loads, int[] weights, int offset) {
		super(merge(items, loads));
		this.items = items;
		this.loads = loads;
		this.weights = weights;
		this.totalWeight = 0;
		for(int w : weights) {
			totalWeight += w;
		}
		this.offset = offset;
	}

	/**
	 * Sums the weights of the items in the set. The set contains the index of the items.
	 *
	 * @param set a set of the items
	 * @param weights the weights
	 * @return the sum of the weights of the items contained in the set
	 */
	private static int sum(ArrayList<Integer> set, int[] weights) {
		int res = 0;
		for(int i : set) {
			res += weights[i];
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
	public static int[] noSum(ArrayList<Integer> set, int[] weights, int alpha, int beta) {
		if(alpha <= 0 || sum(set,weights) <= beta) {
			return new int[]{};
		}
		int sumA = 0, sumB = 0, sumC = 0;
		int k = 0, k2 = 0; // k largest items and k2 smallest items

		while(k2 < set.size() && sumC + weights[set.get(set.size()-1-k2)] < alpha) {
			sumC += weights[set.get(set.size()-1-k2)];
			k2++;
		}
		if(k2 == set.size()) {
			k2--;
		}
		sumB = weights[set.get(set.size()-1-k2)];
		while(k2 >=0 && sumA<alpha && sumB <= beta) {
			k++;
			sumA += weights[set.get(k-1)];
			if(sumA < alpha) {
				k2--;
				sumB += weights[set.get(set.size()-1-k2)];
				sumC -= weights[set.get(set.size()-1-k2)];
				while(sumA+sumC >= alpha) {
					k2--;
					sumC -= weights[set.get(set.size()-1-k2)];
					sumB += (weights[set.get(set.size()-1-k2)]-weights[set.get(set.size()-1-k2-k-1)]);
				}
			}
		}
		if(sumA < alpha) {
			return new int[]{sumA+sumC,sumB};
		}
		else {
			return new int[]{};
		}
	}

	/**
	 * Merges two arrays of IntVar variables.
	 *
	 * @param a the first array
	 * @param b the second array
	 * @return the merged array
	 */
	private static IntVar[] merge(IntVar[] a, IntVar[] b) {
		IntVar[] t = new IntVar[a.length+b.length];
		for(int i = 0; i<t.length; i++) {
			if(i<a.length) {
				t[i] = a[i];
			}
			else {
				t[i] = b[i-a.length];
			}
		}
		return t;
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
	public static void order(ArrayList<Integer> list, int[] weights, int idx) {
		assert 0<=idx && idx<weights.length : "idx should be betwwen 0 (include) and weights.length (exclude)";
		int i = 0;
		while(i<list.size() && weights[list.get(i)]>weights[idx]) {
			i++;
		}
		list.add(i, idx);
	}
	
	/**
	 * Load and Size Coherence rule.
	 *
	 * @return true, if a modification has been made on the items or loads variables
	 * @throws ContradictionException the contradiction exception
	 */
	private boolean loadAndSizeCoherence() throws ContradictionException { // 2.5 Load and Size Coherence
		boolean modif = false;
		int min = 0, max = 0;
		for(int j = 0; j<loads.length; j++) {
			min += loads[j].getLB();
			max += loads[j].getUB();
		}
		for(int j = 0; j<loads.length; j++) {
			boolean localModif = false;
			int lb = this.totalWeight-(max-loads[j].getUB()), ub = this.totalWeight-(min-loads[j].getLB());
			if(lb>loads[j].getLB()) {
				localModif = true;
				min = min - loads[j].getLB() + lb;
				loads[j].updateLowerBound(lb, this);
				modif = true;
			}
			if(ub<loads[j].getUB()) {
				localModif = true;
				max = max - loads[j].getUB() + ub;
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
	 * @param pj the pj
	 * @param sumPj the sum pj
	 * @return true, if a modification has been made on the items or loads variables
	 * @throws ContradictionException the contradiction exception
	 */
	private boolean loadMaintenance(int[] pj, int[] sumPj) throws ContradictionException { // 2.5 Load Maintenance
		boolean modif = false;
		for(int j = 0; j<loads.length; j++) {
			if(loads[j].getLB()<pj[j] || loads[j].getUB()>sumPj[j]) {
				modif = true;
			}
			loads[j].updateLowerBound(pj[j], this);
			loads[j].updateUpperBound(sumPj[j], this);
		}
		return modif;
	}
	
	/**
	 * Single Item Elimination and Commitment rule.
	 *
	 * @param C the c
	 * @param P the p
	 * @param R the r
	 * @param pj the pj
	 * @param sumPj the sum pj
	 * @return true, if a modification has been made on the items or loads variables
	 * @throws ContradictionException the contradiction exception
	 */
	private boolean singleItemEliminationAndCommitment(ArrayList<ArrayList<Integer>> C, ArrayList<ArrayList<Integer>> P, ArrayList<ArrayList<Integer>> R, int[] pj, int[] sumPj) throws ContradictionException{ // 2.5 Single Item Elimination and Commitment
		boolean modif = false;
		for(int j = 0; j<loads.length; j++) {
			for(int k = 0; k<C.get(j).size(); k++) {
				boolean localModif = false;
				int i = C.get(j).get(k);
				if(pj[j]+weights[i]>loads[j].getUB()) {
					localModif = true;
					items[i].removeValue(j, this);
					C.get(j).remove(k);
					P.get(j).remove(new Integer(i));
					sumPj[j] -= weights[i];
					modif = true;
				}
				else if(sumPj[j]-weights[i]<loads[j].getLB()) {
					localModif = true;
					items[i].instantiateTo(j, this);
					C.get(j).remove(k);
					R.get(j).add(i);
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
	 * @param C the c
	 * @param pj the pj
	 * @throws ContradictionException the contradiction exception
	 */
	private void pruningRule(ArrayList<ArrayList<Integer>> C, int[] pj) throws ContradictionException { // 3.2 Pruning Rule
		for(int j = 0; j<loads.length; j++) {
			if(noSum(C.get(j), weights, loads[j].getLB()-pj[j], loads[j].getUB()-pj[j]).length==2) {
				throw new ContradictionException();
			}
		}
	}
	
	/**
	 * Tightening Bounds on Bin Load rule.
	 *
	 * @param C the c
	 * @param pj the pj
	 * @return true, if a modification has been made on the items or loads variables
	 * @throws ContradictionException the contradiction exception
	 */
	private boolean tighteningBoundsOnBinLoad(ArrayList<ArrayList<Integer>> C, int[] pj) throws ContradictionException { // 3.3 Tightening Bounds on Bin Load
		boolean modif = false;
		for(int j = 0; j<loads.length; j++) {
			int[] ns = noSum(C.get(j), weights, loads[j].getLB()-pj[j], loads[j].getLB()-pj[j]);
			if(ns.length==2) {
				loads[j].updateLowerBound(pj[j]+ns[1], this);
				modif = true;
			}
			ns = noSum(C.get(j), weights, loads[j].getUB()-pj[j], loads[j].getUB()-pj[j]);
			if(ns.length==2) {
				loads[j].updateUpperBound(pj[j]+ns[0], this);
				modif = true;
			}
		}
		return modif;
	}
	
	/**
	 * Elimination and Commitment of Items rule.
	 *
	 * @param C the c
	 * @param P the p
	 * @param R the r
	 * @param pj the pj
	 * @param sumPj the sum pj
	 * @return true, if a modification has been made on the items or loads variables
	 * @throws ContradictionException the contradiction exception
	 */
	private boolean eliminationAndCommitmentOfItems(ArrayList<ArrayList<Integer>> C, ArrayList<ArrayList<Integer>> P, ArrayList<ArrayList<Integer>> R, int[] pj, int[] sumPj) throws ContradictionException { // 3.4 Elimination and Commitment of Items
		boolean modif = false;
		for(int j = 0; j<loads.length; j++) {
			for(int k = 0; k<C.get(j).size(); k++) {
				boolean localModif = false;
				int i = C.get(j).remove(k);
				if(noSum(C.get(j), weights, loads[j].getLB()-pj[j]-weights[i], loads[j].getUB()-pj[j]-weights[i]).length==2) {
					localModif = true;
					items[i].removeValue(j, this);
					P.get(j).remove(new Integer(i));
					sumPj[j] -= weights[i];
					modif = true;
				}
				else if(noSum(C.get(j), weights, loads[j].getLB()-pj[j], loads[j].getUB()-pj[j]).length==2) {
					localModif = true;
					items[i].instantiateTo(j, this);
					R.get(j).add(i);
					pj[j] += weights[i];
					modif = true;
				}
				if(localModif) {
					k = -1;
				}
				else {
					C.get(j).add(k, new Integer(i));
				}
			}
		}
		return modif;
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
		
		// Building Pj and Rj and Cj for each j
		ArrayList<ArrayList<Integer>> P = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> R = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> C = new ArrayList<ArrayList<Integer>>();
		for(int j = 0; j<loads.length; j++) {
			P.add(new ArrayList<Integer>());
			R.add(new ArrayList<Integer>());
			C.add(new ArrayList<Integer>());
		}
		for(int i = 0; i<items.length; i++) {
			if(items[i].isInstantiated()) {
				int value = items[i].getValue();
				R.get(value-this.offset).add(i);
				order(P.get(value-this.offset), weights, i);
			}
			else {
				for(int value = items[i].getLB(); value <= items[i].getUB(); value = items[i].nextValue(value)) {
					order(P.get(value-this.offset), weights, i);
					order(C.get(value-this.offset), weights, i);
				}
			}
		}
		int[] pj = new int[R.size()]; // represents the sum pj of the weights of the items in Rj
		int[] sumPj = new int[P.size()]; // represents the sum of the weights of the items in Pj
		for(int j = 0; j<loads.length; j++) {
			pj[j] = sum(R.get(j), weights);
			sumPj[j] = sum(P.get(j), weights);
		}
		
		boolean modif = false;
		do {
			modif = false;
			modif |= this.loadMaintenance(pj, sumPj); // 2.5 Load Maintenance
			modif |= this.loadAndSizeCoherence(); // 2.5 Load and Size Coherence
			modif |= this.singleItemEliminationAndCommitment(C, P, R, pj, sumPj); // 2.5 Single Item Elimination and Commitment
			this.pruningRule(C, pj); // 3.2 Pruning Rule
			modif |= this.tighteningBoundsOnBinLoad(C, pj); // 3.3 Tightening Bounds on Bin Load
			modif |= this.eliminationAndCommitmentOfItems(C, P, R, pj, sumPj); // 3.4 Elimination and Commitment of Items
		} while(modif);
		
	}

}
