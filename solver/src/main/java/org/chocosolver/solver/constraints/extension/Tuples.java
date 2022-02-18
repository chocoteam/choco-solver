/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension;

import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A unique interface to declare tuples for table constraints.
 * Handles both feasible tuples and forbidden tuples.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/04/2014
 */
public class Tuples {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private final boolean feasible;
	protected final List<int[]> tuples;
	private int arity;
	private int[] ranges;
	private boolean allowStar;
	private int star;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

    /**
     * Create a list of tuples which represents all allowed tuples if feasible=true
     * or a set of forbidden tuples if feasible=false.
	 * Lately, one can allow the presence of universal values, calling {@link #setUniversalValue(int)},
	 * meaning that some variables can take any values from their domain.
     *
     * @param values list of tuples
     * @param feasible indicates whether the tuples are allowed or forbidden
     */
    public Tuples(int[][] values, boolean feasible) {
        this.feasible = feasible;
        tuples = new ArrayList<>();
        for(int[] t : values){
            add(t);
        }
    }

	/**
	 * Create a list of tuples which represents all allowed tuples if feasible=true
	 * or a set of forbidden tuples if feasible=false
	 *
	 * @param feasible indicates whether the tuples are allowed or forbidden
	 */
	public Tuples(boolean feasible) {
		this.feasible = feasible;
		tuples = new ArrayList<>();
	}

	/**
	 * Create a list of tuples which represents all allowed tuples, i.e. other tuples are forbidden
	 */
	public Tuples() {
		this(true);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	/**
	 * One can allow the presence of universal values,
	 * meaning that some variables can take any values from their domain.
	 * @param star the universal value that can appear in any tuple.
	 */
	public void setUniversalValue(int star){
		this.star = star;
		this.allowStar = true;
	}

	/**
	 * @return <i>true</i> if the presence of universal values is allowed.
	 */
	public boolean allowUniversalValue(){
		return this.allowStar;
	}

	/**
	 * @return the value of the symbol which denotes that
	 * some variables can take any values from their domain.
	 */
	public int getStarValue(){
		assert allowUniversalValue();
		return this.star;
	}

	/**
	 * Checks entailment of a table constraint over vars with this Tuples object
	 * @param vars set of integer variables to test
	 * @return an ESat object indicating the entailement of the table over vars and this
	 */
	public ESat check(IntVar... vars) {
		if(nbTuples() == 0){
			return isFeasible()? ESat.FALSE: ESat.TRUE;
		}
		if(vars.length != arity){
			throw new SolverException("The given variable array does not match the arity: " + arity);
		}
		int[] values = new int[vars.length];
		for (int i=0;i<vars.length;i++) {
			if (vars[i].isInstantiated()) {
				values[i] = vars[i].getValue();
			}else{
				return ESat.UNDEFINED;
			}
		}
		for (int ti = 0; ti < nbTuples(); ti++) {
			int[] tuple = tuples.get(ti);
			boolean valid = true;
			for (int i = 0; i < values.length && valid; i++) {
				valid = tuple[i] == values[i] || (allowStar && tuple[i] == star);
			}
			if (valid) {
				return isFeasible()? ESat.TRUE: ESat.FALSE;
			}
		}
		return isFeasible()? ESat.FALSE: ESat.TRUE;
	}

	/**
	 * Add a new tuple to the set of tuples
	 *
	 * @param tuple a tuple.
	 * @throws org.chocosolver.solver.exception.SolverException if the size of the tuple added does not correspond to a the previous ones (if any).
	 */
	public void add(int... tuple) {
		if (tuples.size() == 0) {
			arity = tuple.length;
			ranges = new int[2 * arity];
			Arrays.fill(ranges, 0, arity, Integer.MAX_VALUE);
			Arrays.fill(ranges, arity, 2 * arity, Integer.MIN_VALUE);
		} else if (arity != tuple.length) {
			throw new SolverException("The given tuple does not match the arity: " + arity);
		}
		tuples.add(tuple.clone());
		for (int i = 0; i < arity; i++) {
			ranges[i] = Math.min(ranges[i], tuple[i]);
			ranges[i + arity] = Math.max(ranges[i + arity], tuple[i]);
		}
	}

	/**
	 * Add a tuple set
	 *
	 * @param tuples tuple set
	 */
	public void add(int[]... tuples) {
		for (int[] t : tuples) {
			add(t);
		}
	}

	/**
	 * Return true if these are allowed tuples, false otherwise
	 *
	 * @return a boolean
	 */
	public boolean isFeasible() {
		return feasible;
	}

	/**
	 * Return the minimum value for the idx^th column among all tuples
	 *
	 * @param idx idx of the column
	 * @return the minimum value
	 */
	public int min(int idx) {
		return ranges[idx];
	}

	/**
	 * Return the maximum value for the idx^th column among all tuples
	 *
	 * @param idx index of the column
	 * @return the maximum value
	 */
	public int max(int idx) {
		return ranges[idx + arity];
	}

	/**
	 * Return the number of tuples stored
	 *
	 * @return number of tuples stored
	 */
	public int nbTuples() {
		return tuples.size();
	}

	/**
	 * Return the idx^th tuple
	 */
	public int[] get(int idx) {
		return tuples.get(idx);
	}

	@Override
	public String toString() {
		StringBuilder st = new StringBuilder(isFeasible() ? "Allowed" : "Fordidden").append(" tuples: {");
		for (int i = 0; i < tuples.size(); i++) {
			st.append(Arrays.toString(tuples.get(i)));
		}

		st.append("}");
		return st.toString();

	}

	/**
	 * @return an array of tuples, each tuple is an int array
	 */
	public int[][] toMatrix() {
		int i = 0;
		int[][] matrix = new int[tuples.size()][];
		for (int[] tuple : tuples) {
			matrix[i++] = tuple.clone();
		}
		return matrix;
	}

	public void sort() {
		tuples.sort(new TupleComparator());
	}

	private static class TupleComparator implements Comparator<int[]> {

		@Override
		public int compare(int[] o1, int[] o2) {
			int i = 0;
			int l = o1.length;
			while (i < l && o1[i] == o2[i]) {
				i++;
			}
			return (i == l ? 0 : o1[i] - o2[i]);
		}
	}
}
