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

package solver.constraints.nary;

import choco.kernel.ESat;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.probabilistic.propagators.nary.PropProbaAllDiffBC;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.binary.PropNotEqualX_YC;
import solver.constraints.propagators.gary.PropIntVarsGraphChanneling;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraph;
import solver.constraints.propagators.gary.constraintSpecific.PropGraphAllDiffBC;
import solver.constraints.propagators.gary.undirected.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.undirected.PropAtMostNNeighbors;
import solver.constraints.propagators.nary.PropAllDiffAC;
import solver.constraints.propagators.nary.PropAllDiffBC;
import solver.constraints.propagators.nary.PropAllDiff_AC_JG;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphType;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * Standard alldiff constraint with generalized AC
 * integer valued variables are used only for the left vertex set
 * no explicit variables are used for the right vertex set
 * the right vertex set is the interval (minValue .. maxValue)
 */
public class AllDifferent extends IntConstraint<IntVar> {

	public static enum Type {
		AC, PROBABILISTIC, BC, CLIQUE, GRAPH
	}

	public AllDifferent(IntVar[] vars, Solver solver) {
		this(vars, solver, Type.BC);
	}

	public AllDifferent(IntVar[] vars, Solver solver, Type type) {
		super(vars, solver);
		switch (type) {
			case CLIQUE: {
				int s = vars.length;
				int k = 0;
				Propagator[] props = new Propagator[(s * s - s) / 2];
				for (int i = 0; i < s - 1; i++) {
					for (int j = i + 1; j < s; j++) {
						props[k++] = new PropNotEqualX_YC(new IntVar[]{vars[i], vars[j]}, 0, solver, this);
					}
				}
				setPropagators(props);
			}
			break;
			case PROBABILISTIC:
				setPropagators(new PropProbaAllDiffBC(this.vars, solver, this)); //new PropAllDiffBC(this.vars, solver, this));
				/*{
								int s = vars.length;
								int k = 0;
								Propagator[] props = new Propagator[(s * s - s) / 2];
								for (int i = 0; i < s - 1; i++) {
									for (int j = i + 1; j < s; j++) {
										props[k++] = new PropNotEqualX_YC(new IntVar[]{vars[i], vars[j]}, 0, solver, this);
									}
								}
								addPropagators(props);
							}//*/
				break;
			case GRAPH:
				buildGraphAllDifferent(this.vars, solver);
				break;
			case AC:
//				setPropagators(new PropAllDiffAC(this.vars, this, solver));
				// TODO check 
				setPropagators(new PropAllDiff_AC_JG(this.vars, this, solver));

				break;
			case BC:
			default:
				setPropagators(new PropAllDiffBC(this.vars, solver, this));
				break;
		}
	}

	/**
	 * AllDifferent constraint using an explicit graph variables-values
	 * Uses Regin algorithm GAC in O(m.rac(n)) worst case time
	 * <p/>
	 * BEWARE pretty heavy : to avo•d when the amount of bks is high and when
	 * there are much more values than variables
	 *
	 * @param vars   should be enumerated variables otherwise a BC choice would be much more relevant
	 * @param solver
	 */
	private void buildGraphAllDifferent(IntVar[] vars, Solver solver) {
		TIntArrayList valuesList = new TIntArrayList();
		boolean bcMode = false;
		int val, ub;
		for (int v = 0; v < vars.length; v++) {
			if (!vars[v].hasEnumeratedDomain()) {
				bcMode = true;
			}
			ub = vars[v].getUB();
			for (val = vars[v].getLB(); val <= ub; val = vars[v].nextValue(val)) {
				if (!valuesList.contains(val)) {
					valuesList.add(val);
				}
			}
		}
		int n = vars.length + valuesList.size();
		int[] values = new int[n];
		TIntIntHashMap valuesHash = new TIntIntHashMap();
		for (int i = 0; i < vars.length; i++) {
			values[i] = i;
			valuesHash.put(i, i);
		}
		for (int i = vars.length; i < n; i++) {
			values[i] = valuesList.get(i - vars.length);
			valuesHash.put(values[i], i);
		}
		UndirectedGraphVar graph = new UndirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
		for (int v = 0; v < vars.length; v++) {
			ub = vars[v].getUB();
			for (val = vars[v].getLB(); val <= ub; val = vars[v].nextValue(val)) {
				graph.getEnvelopGraph().addEdge(v, valuesHash.get(val));
			}
		}
		if (bcMode) {
			setPropagators(
					new PropAllDiffGraph(graph, vars.length, solver, this),
					new PropAtMostNNeighbors(graph, solver, this, 1),
					new PropAtLeastNNeighbors(graph, solver, this, 1),
					new PropIntVarsGraphChanneling(vars, graph, solver, this, values, valuesHash),
					new PropGraphAllDiffBC(vars, graph, solver, this, valuesHash)
			);
		} else {
			setPropagators(
					new PropAllDiffGraph(graph, vars.length, solver, this),
					new PropAtMostNNeighbors(graph, solver, this, 1),
					new PropAtLeastNNeighbors(graph, solver, this, 1),
					new PropIntVarsGraphChanneling(vars, graph, solver, this, values, valuesHash)
			);
		}
	}

	/**
	 * Checks if the constraint is satisfied when all variables are instantiated.
	 *
	 * @param tuple an complete instantiation
	 * @return true iff a solution
	 */
	@Override
	public ESat isSatisfied(int[] tuple) {
		for (int i = 0; i < vars.length; i++) {
			for (int j = 0; j < i; j++) {
				if (tuple[i] == tuple[j]) {
					return ESat.FALSE;
				}
			}
		}
		return ESat.TRUE;
	}

	@Override
	public ESat isSatisfied() {
		for (IntVar v : vars) {
			if (v.instantiated()) {
				int vv = v.getValue();
				for (IntVar w : vars) {
					if (w != v) {
						if (w.instantiated()) {
							if (vv == w.getValue()) {
								return ESat.FALSE;
							}
						} else {
							return ESat.UNDEFINED;
						}
					}
				}
			} else {
				return ESat.UNDEFINED;
			}
		}
		return ESat.TRUE;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		sb.append("AllDifferent({");
		for (int i = 0; i < vars.length; i++) {
			if (i > 0) sb.append(", ");
			Variable var = vars[i];
			sb.append(var);
		}
		sb.append("})");
		return sb.toString();
	}
}
