/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

package solver.constraints.nary.nValue;

import gnu.trove.list.array.TIntArrayList;
import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.variables.IntVar;
import util.tools.ArrayUtils;

/**
 * NValues constraint
 * The number of distinct values in the set of variables and within a set of given values is equal to nValues
 *
 * @author Jean-Guillaume Fages
 */
public class NValues extends Constraint {

	public enum Type {
		at_most_BC {
			@Override
			public Propagator[] addProp(IntVar[] vars, IntVar nValues) {
				boolean enumDom = false;
				for (IntVar v : vars) {
					if (v.hasEnumeratedDomain()) {
						enumDom = true;
						break;
					}
				}
				if (enumDom){ // added twice to perform fixpoint
					return new Propagator[]{
							new PropAtMostNValues_BC(vars, nValues),
							new PropAtMostNValues_BC(vars, nValues)
					};
				}else{
					return new Propagator[]{new PropAtMostNValues_BC(vars, nValues)};
				}
			}
		},
		at_most_greedy {
			@Override
			public Propagator[] addProp(IntVar[] vars, IntVar nValues) {
				return new Propagator[]{
						new AMNV_Gci_MD_R13(vars,nValues,Differences.NONE),
						new AMNV_Gci_R_R13(vars,nValues,Differences.NONE,30)
				};
			}
		},
		at_least_AC {
			@Override
			public Propagator[] addProp(IntVar[] vars, IntVar nValues) {
				return new Propagator[]{new PropAtLeastNValues_AC(vars, nValues)};
			}
		};

		public abstract Propagator[] addProp(IntVar[] vars, IntVar nValues);
	}

	/**
	 * NValues constraint
	 * The number of distinct values in vars is exactly nValues
	 *
	 * @param vars
	 * @param nValues
	 * @param types   additional filtering algorithms to consider
	 */
	public NValues(IntVar[] vars, IntVar nValues, String... types) {
		super("NValue",createProps(vars,nValues,types));
	}

	public static Propagator[] createProps(IntVar[] vars, IntVar nValues, String... types) {
		Propagator[] props = new Propagator[]{new PropNValues_Light(vars, getDomainUnion(vars), nValues)};
		for (String t : types) {
			props = ArrayUtils.append(props,Type.valueOf(t).addProp(vars, nValues));
		}
		return props;
	}

	/**
	 * NValues constraint
	 * The number of distinct values in vars is exactly nValues
	 * Considers a set of difference constraints "diff" to achieve
	 * a stronger filtering (AMNV(Gci,RMD,R13) of Fages and Lapegue, CP'13)
	 *
	 * @param vars
	 * @param nValues
	 * @param diff
	 */
	public NValues(IntVar[] vars, IntVar nValues, Differences diff) {
		super("NValue",new PropNValues_Light(vars, getDomainUnion(vars), nValues),
				new AMNV_Gci_MD_R13(vars,nValues,diff),
				new AMNV_Gci_R_R13(vars,nValues,diff,30)
		);
	}

	private static TIntArrayList getDomainUnion(IntVar[] vars) {
		TIntArrayList values = new TIntArrayList();
		for (IntVar v : vars) {
			int ub = v.getUB();
			for (int i = v.getLB(); i <= ub; i = v.nextValue(i)) {
				if (!values.contains(i)) {
					values.add(i);
				}
			}
		}
		return values;
	}
}
