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
package solver.constraints.extension;

import solver.exception.SolverException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A unique interface to declare tuples for table constraints.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/04/2014
 */
public class Tuples {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

    protected final boolean feasible;
    protected final List<int[]> tuples;
    protected int arity;
    protected int[] ranges;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

    public Tuples(boolean feasible) {
        this.feasible = feasible;
        tuples = new ArrayList<>();
    }

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

    /**
     * Add a new tuple to the set of tuples
     *
     * @param tuple a tuple.
     * @throws solver.exception.SolverException if the size of the tuple added does not correspond to a the previous ones (if any).
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
     *
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
	public int[][] toMatrix(){
		int i=0;
		int[][] matrix = new int[tuples.size()][];
		for(int[] tuple:tuples){
			matrix[i++] = tuple.clone();
		}
		return matrix;
	}
}
