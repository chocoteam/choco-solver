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

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

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
				if (tuple[i] != values[i]) valid = false;
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
		tuples.add(tuple);
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
