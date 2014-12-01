/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chocosolver.solver.constraints.nary.sum;

import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * <br/>
 * Bounds Consistency Techniques for Long Linear Constraint" </br>
 * W. Harvey and J. Schimpf
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public class Scalar extends Constraint {

	/**
	 * Scalar product A.X Op B
	 * e.g. a1.x1 + a2.x2 = b
	 *
	 * @param X		Array of integer variables
	 * @param A		Array of integer coefficients
	 * @param B		Integer result of the scalar product
	 */
	public Scalar(IntVar[] X, int[] A, int B) {
		super("Scalar",makeProp(X, A, B));
	}

	/**
	 * Scalar product A.X Op B.Y
	 * e.g. a1.x1 + a2.x2 = b.y
	 *
	 * @param X		Array of integer variables
	 * @param A		Array of integer coefficients
	 * @param Y		Integer variable
	 * @param B		Integer coefficient
	 */
	public Scalar(IntVar[] X, int[] A, IntVar Y, int B) {
		this(ArrayUtils.append(X,new IntVar[]{Y}),ArrayUtils.append(A,new int[]{-B}),0);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// GENERIC /////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static Propagator<IntVar> makeProp(IntVar[] vars, int[] coeffs, int result) {
		// aggregate multiple variable occurrences
		TObjectIntHashMap<IntVar> map = new TObjectIntHashMap<>();
		for (int i = 0; i < vars.length; i++) {
			map.adjustOrPutValue(vars[i], coeffs[i], coeffs[i]);
			if (map.get(vars[i]) == 0) {
				map.remove(vars[i]);
			}
		}
		// to fix determinism in the construction, we iterate over the original array of variables
		int b = 0, e = map.size();
		IntVar[] tmpV = new IntVar[e];
		int[] tmpC = new int[e];
		for (int i = 0; i < vars.length; i++) {
			IntVar key = vars[i];
			int coeff = map.get(key);
			if (coeff > 0) {
				tmpV[b] = key;
				tmpC[b++] = coeff;
			} else if (coeff < 0) {
				tmpV[--e] = key;
				tmpC[e] = coeff;
			}
			map.adjustValue(key, -coeff); // to avoid multiple occurrence of the variable
		}
		return new PropScalarEq(tmpV, tmpC, b, result);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int[] getScalarBounds(IntVar[] vars, int[] coefs) {
		int[] ext = new int[2];
		for (int i = 0; i < vars.length; i++) {
			int min = Math.min(0, vars[i].getLB() * coefs[i]);
			min = Math.min(min, vars[i].getUB() * coefs[i]);
			int max = Math.max(0, vars[i].getLB() * coefs[i]);
			max = Math.max(max, vars[i].getUB() * coefs[i]);
			ext[0] += min;
			ext[1] += max;
		}
		return ext;
	}
}
