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
import solver.constraints.nary.nValue.amnv.differences.AutoDiffDetection;
import solver.constraints.nary.nValue.amnv.differences.D;
import solver.constraints.nary.nValue.amnv.graph.G;
import solver.constraints.nary.nValue.amnv.graph.Gci;
import solver.constraints.nary.nValue.amnv.graph.Gi;
import solver.constraints.nary.nValue.amnv.mis.F;
import solver.constraints.nary.nValue.amnv.mis.MD;
import solver.constraints.nary.nValue.amnv.mis.MDRk;
import solver.constraints.nary.nValue.amnv.mis.Rk;
import solver.constraints.nary.nValue.amnv.rules.*;
import solver.variables.IntVar;
import util.tools.ArrayUtils;

/**
 * NValues constraint
 * The number of distinct values in the set of variables and within a set of given values is equal to nValues
 *
 * @author Jean-Guillaume Fages
 */
public class NValues extends Constraint {

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
			switch (t){
				case "at_most_BC":
					boolean enumDom = false;
					for (IntVar v : vars) {
						if (v.hasEnumeratedDomain()) {
							enumDom = true;
							break;
						}
					}
					if (enumDom){ // added twice to perform fixpoint
						props = ArrayUtils.append(props,new Propagator[]{
								new PropAtMostNValues_BC(vars, nValues),
								new PropAtMostNValues_BC(vars, nValues)
						});
					}else{
						props = ArrayUtils.append(props,new Propagator[]{new PropAtMostNValues_BC(vars, nValues)});
					}
					break;
				case "at_least_AC":
					props = ArrayUtils.append(props,new Propagator[]{new PropAtLeastNValues_AC(vars, nValues)});
					break;
				default:
					if(!t.contains("AMNV")){
						t="AMNV<Gci|MDRk|R13>";
					}
					String[] cols = t.replace('<','|').replace('>', '|').split("\\|");
					D diff = new AutoDiffDetection(vars);
					G graph;
					switch (cols[1]){
						case "Gi":	graph = new Gi(vars);break;
						case "Gci":	graph = new Gci(vars,diff);break;
						default:throw new UnsupportedOperationException("unknown graph type "+cols[1]+". \nUse Gi or Gci");
					}
					F heur;
					switch (cols[2]){
						case "MD":	heur = new MD(graph);break;
						case "Rk":	heur = new Rk(graph,30);break;
						case "MDRk":heur = new MDRk(graph,30);break;
						default:throw new UnsupportedOperationException("unknown independent set heuristic type "+cols[2]+". \n" +
								"Use MD, Rk or MDRk");
					}
					R[] rules;
					switch (cols[3]){
						case "R1":	rules = new R[]{new R1()};break;
						case "R12":	rules = new R[]{new R1(),new R2()};break;
						case "R13":	rules = new R[]{new R1(),new R3()};break;
						case "R14":	rules = new R[]{new R1(),new R4()};break;
						case "R124":rules = new R[]{new R1(),new R2(),new R4()};break;
						case "R134":rules = new R[]{new R1(),new R3(),new R4()};break;
						default:throw new UnsupportedOperationException("unknown or unimplemented filtering rule configuration "+
								cols[3]+".\nUse R1, R12, R13, R14, R124 or R134");
					}
					props = ArrayUtils.append(props,new Propagator[]{new PropAMNV(vars,nValues,graph,heur,rules)});
					break;
			}
		}
		return props;
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
