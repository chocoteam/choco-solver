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

package choco.checker;

import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.binary.Absolute;
import solver.constraints.binary.Element;
import solver.constraints.nary.*;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.nary.lex.Lex;
import solver.constraints.ternary.Times;
import solver.exception.ContradictionException;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.selectors.values.InDomainMin;
import solver.search.strategy.selectors.variables.InputOrder;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.Assignment;
import solver.variables.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
public interface Modeler {

	Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters);


	Modeler modelEqAC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("EqAC_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			try {
				for (int i = 0; i < vars.length; i++) {
					vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
					if (map != null) map.put(domains[i], vars[i]);
				}
			} catch (ArrayIndexOutOfBoundsException ce) {
				System.out.printf("");
			}
			Constraint ctr = ConstraintFactory.eq(vars[0], vars[1], s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);

			return s;
		}
	};

	Modeler modelInverseChannelingAC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("InverseChannelingAC_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] X = new IntVar[n / 2];
			IntVar[] Y = new IntVar[n / 2];
			for (int i = 0; i < n / 2; i++) {
				X[i] = VariableFactory.enumerated("X_" + i, domains[i], s);
				if (map != null) map.put(domains[i], X[i]);
				Y[i] = VariableFactory.enumerated("Y_" + i, domains[i + (n / 2)], s);
				if (map != null) map.put(domains[i + (n / 2)], Y[i]);
			}
			IntVar[] allvars = ArrayUtils.append(X, Y);

			Constraint ctr = new InverseChanneling(X, Y, s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(allvars, env);
			s.post(ctrs);
			s.set(strategy);

			return s;
		}
	};

	Modeler modelInverseChannelingBounds = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("InverseChannelingBC_" + n);
			IEnvironment env = s.getEnvironment();
			for (int i = 0; i < domains.length; i++) {
				int m = domains[i][0];
				int M = domains[i][domains[i].length - 1];
				domains[i] = new int[M - m + 1];
				for (int j = 0; j < M - m + 1; j++) {
					domains[i][j] = j + m;
				}
			}

			IntVar[] X = new IntVar[n / 2];
			IntVar[] Y = new IntVar[n / 2];
			int off = n / 2;
			for (int i = 0; i < n / 2; i++) {
				X[i] = VariableFactory.bounded("X_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
				if (map != null) map.put(domains[i], X[i]);
				Y[i] = VariableFactory.bounded("Y_" + i, domains[i + off][0], domains[i + off][domains[i + off].length - 1], s);
				if (map != null) map.put(domains[i + (n / 2)], Y[i]);
			}
			IntVar[] allvars = ArrayUtils.append(X, Y);

			Constraint ctr = new InverseChanneling(X, Y, s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(allvars, env);
			s.post(ctrs);
			s.set(strategy);

			return s;
		}
	};

	Modeler modelNeqAC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("NeqAC_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			for (int i = 0; i < vars.length; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			Constraint ctr = ConstraintFactory.neq(vars[0], vars[1], s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);

			return s;
		}
	};

	Modeler modelAllDiffAC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("AllDiffAC_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			for (int i = 0; i < vars.length; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			Constraint ctr = new AllDifferent(vars, s, AllDifferent.Type.AC);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelAllDiffBC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("AllDiffBC_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			for (int i = 0; i < vars.length; i++) {
				vars[i] = VariableFactory.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			Constraint ctr = new AllDifferent(vars, s, AllDifferent.Type.BC);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelAllDiffGraph = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("AllDiffGRAPH_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			for (int i = 0; i < vars.length; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			Constraint ctr = new AllDifferent(vars, s, AllDifferent.Type.AC);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelAllDiffGraphBc = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("AllDiffGRAPH_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			for (int i = 0; i < vars.length; i++) {
				vars[i] = VariableFactory.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			Constraint ctr = new AllDifferent(vars, s, AllDifferent.Type.AC);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelTimes = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("Times_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			for (int i = 0; i < vars.length; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			Constraint ctr = new Times(vars[0], vars[1], vars[2], s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelAbsolute = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("Absolute_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			for (int i = 0; i < vars.length; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			Constraint ctr = new Absolute(vars[0], vars[1], s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelCountBC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("Count");
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n - 1];
			for (int i = 0; i < vars.length; i++) {
				vars[i] = VariableFactory.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			IntVar occVar = VariableFactory.bounded("ovar", domains[n - 1][0], domains[n - 1][domains[n - 1].length - 1], s);
			if (map != null) map.put(domains[n - 1], occVar);
			int[] params = (int[]) parameters;
			Count.Relop ro = null;
			switch (params[0]) {
				case 0:
					ro = Count.Relop.EQ;
					break;
				case 1:
					ro = Count.Relop.LEQ;
					break;
				case 2:
					ro = Count.Relop.GEQ;
					break;
			}
			Constraint ctr = new Count(params[1], vars, ro, occVar, s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelCountAC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("Count");
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n - 1];
			for (int i = 0; i < vars.length; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			IntVar occVar = VariableFactory.enumerated("ovar", domains[n - 1], s);
			if (map != null) map.put(domains[n - 1], occVar);
			int[] params = (int[]) parameters;
			Count.Relop ro = null;
			switch (params[0]) {
				case 0:
					ro = Count.Relop.EQ;
					break;
				case 1:
					ro = Count.Relop.LEQ;
					break;
				case 2:
					ro = Count.Relop.GEQ;
					break;
			}
			Constraint ctr = new Count(params[1], vars, ro, occVar, s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelLexAC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("Lex");
			IEnvironment env = s.getEnvironment();

			IntVar[] X = new IntVar[n / 2];
			for (int i = 0; i < n / 2; i++) {
				X[i] = VariableFactory.enumerated("X_" + i, domains[i], s);
				if (map != null) map.put(domains[i], X[i]);
			}
			IntVar[] Y = new IntVar[n / 2];
			for (int i = n / 2; i < n; i++) {
				Y[i - n / 2] = VariableFactory.enumerated("Y_" + i, domains[i], s);
				if (map != null) map.put(domains[i], Y[i - n / 2]);
			}
			Constraint ctr = new Lex(X, Y, (Boolean) parameters, s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(ArrayUtils.append(X, Y), env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelNthBC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("Element_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			for (int i = 0; i < vars.length; i++) {
				vars[i] = VariableFactory.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			Constraint ctr = new Element(vars[0], new int[]{-2, 0, 1, -1, 0, 4}, vars[1], 0, s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelAmongBC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("Among");
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n - 1];
			for (int i = 0; i < vars.length; i++) {
				vars[i] = VariableFactory.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			IntVar occVar = VariableFactory.enumerated("ovar", domains[n - 1][0], domains[n - 1][domains[n - 1].length - 1], s);
			if (map != null) map.put(domains[n - 1], occVar);
			int[] params = (int[]) parameters;
			Constraint ctr = new Among(params, vars, occVar, s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelAmongAC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("Among");
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n - 1];
			for (int i = 0; i < vars.length; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			IntVar occVar = VariableFactory.enumerated("ovar", domains[n - 1], s);
			if (map != null) map.put(domains[n - 1], occVar);
			int[] params = (int[]) parameters;
			Constraint ctr = new Among(params, vars, occVar, s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = new Assignment(vars, new InputOrder(vars, s.getEnvironment()),
					new InDomainMin());//StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelNValues = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("modelNValues_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			IntVar[] decvars = new IntVar[n - 1];
			for (int i = 0; i < n; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
				if (i < n - 1) {
					decvars[i] = vars[i];
				}
			}
			Constraint ctr = new NValues(decvars, vars[n - 1], s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelNValues_AtMostBC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("modelNValues_AtMostBC_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			IntVar[] decvars = new IntVar[n - 1];
			for (int i = 0; i < n; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
				if (i < n - 1) {
					decvars[i] = vars[i];
				}
			}
			Constraint ctr = new NValues(decvars, vars[n - 1], s, NValues.Type.AtMost_BC);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelNValues_AtLeastAC = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("modelNValues_AtLeastAC_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			IntVar[] decvars = new IntVar[n - 1];
			for (int i = 0; i < n; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
				if (i < n - 1) {
					decvars[i] = vars[i];
				}
			}
			Constraint ctr = new NValues(decvars, vars[n - 1], s, NValues.Type.AtLeast_AC);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelNValues_AtMostGreedy = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("modelNValues_AtMostGreedy_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			IntVar[] decvars = new IntVar[n - 1];
			for (int i = 0; i < n; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
				if (i < n - 1) {
					decvars[i] = vars[i];
				}
			}
			Constraint ctr = new NValues(decvars, vars[n - 1], s, NValues.Type.AtMost_GreedyGraph);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelNValues_simple = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("modelNValues_simple_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			IntVar[] decvars = new IntVar[n - 1];
			for (int i = 0; i < n; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
				if (i < n - 1) {
					decvars[i] = vars[i];
				}
			}
			Constraint ctr = new NValues(decvars, vars[n - 1], s, new NValues.Type[]{});
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelGCC_alldiff_Cards = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("modelGCC_Cards_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			TIntArrayList vals = new TIntArrayList();
			for (int i = 0; i < n; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				for (int j : domains[i]) {
					if (!vals.contains(j)) {
						vals.add(j);
					}
				}
				if (map != null) map.put(domains[i], vars[i]);
			}
			int[] values = vals.toArray();
			IntVar[] cards = VariableFactory.boolArray("cards", values.length, s);

			Constraint ctr = new GCC_AC(vars, values, cards, true, s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelGCC_alldiff_Fast = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("modelGCC_Fast_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			TIntArrayList vals = new TIntArrayList();
			for (int i = 0; i < n; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				for (int j : domains[i]) {
					if (!vals.contains(j)) {
						vals.add(j);
					}
				}
				if (map != null) map.put(domains[i], vars[i]);
			}
			int[] values = vals.toArray();
			IntVar[] cards = VariableFactory.boolArray("cards", values.length, s);

			Constraint ctr = new GCC_AC(vars, values, cards, false, s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelGCC_alldiff_LowUp = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("modelGCC_LowUp_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			TIntArrayList vals = new TIntArrayList();
			for (int i = 0; i < n; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				for (int j : domains[i]) {
					if (!vals.contains(j)) {
						vals.add(j);
					}
				}
				if (map != null) map.put(domains[i], vars[i]);
			}
			int[] values = vals.toArray();
			int[] low = new int[values.length];
			int[] up = new int[values.length];
			for (int i = 0; i < values.length; i++) {
				up[i] = 1;
			}
			Constraint ctr = new GCC_AC(vars, values, low, up, s);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelTree = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("tree_" + n);
			IEnvironment env = s.getEnvironment();

			IntVar[] vars = new IntVar[n];
			IntVar[] succs = new IntVar[n-1];
			for (int i = 0; i < n; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if(i<n-1){
					succs[i] = vars[i];
				}
				if (map != null) map.put(domains[i], vars[i]);
			}
			IntVar nbRoots = vars[n-1];
			Constraint ctr = ConstraintFactory.tree(succs,nbRoots,0,s,(Boolean)parameters);
			Constraint[] ctrs = new Constraint[]{ctr};

			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelCircuit = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("circuit_" + n);
			IEnvironment env = s.getEnvironment();
			IntVar[] vars = new IntVar[n];
			for (int i = 0; i < n; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			Constraint ctr = ConstraintFactory.circuit(vars,0,s);
			Constraint[] ctrs = new Constraint[]{ctr};
			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelSubcircuit = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("subcircuit_" + n);
			IEnvironment env = s.getEnvironment();
			IntVar[] vars = new IntVar[n];
			for (int i = 0; i < n; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			Constraint ctr = ConstraintFactory.subcircuit(vars, 0, s);
			Constraint[] ctrs = new Constraint[]{ctr};
			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelDiffn = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("diffn_" + n);
			IEnvironment env = s.getEnvironment();
			IntVar[] vars = new IntVar[n];
			if(n%4!=0){
				throw new UnsupportedOperationException();
			}
			int k = n/4;
			IntVar[] x = new IntVar[k];
			IntVar[] y = new IntVar[k];
			IntVar[] dx = new IntVar[k];
			IntVar[] dy = new IntVar[k];
			for (int i = 0; i < n; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			for (int i = 0; i < k; i++) {
				x[i] = vars[i];
				y[i] = vars[i+k];
				dx[i] = vars[i+2*k];
				dy[i] = vars[i+3*k];
			}
			Constraint ctr = ConstraintFactory.diffn(x, y, dx, dy, s);
			Constraint[] ctrs = new Constraint[]{ctr};
			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}
	};

	Modeler modelCumul = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver solver = new Solver("cumulative_" + n);
			IEnvironment env = solver.getEnvironment();
			IntVar[] vars = new IntVar[n];
			if(n%4!=1){
				throw new UnsupportedOperationException();
			}
			int k = n/4;
			IntVar[] s = new IntVar[k];
			IntVar[] d = new IntVar[k];
			IntVar[] e = new IntVar[k];
			IntVar[] h = new IntVar[k];
			for (int i = 0; i < n; i++) {
//				vars[i] = VariableFactory.bounded("v_" + i, domains[i][0],domains[i][domains[i].length-1], solver);
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], solver);
				if (map != null) map.put(domains[i], vars[i]);
			}
			for (int i = 0; i < k; i++) {
				s[i] = vars[i];
				d[i] = vars[i+k];
				e[i] = vars[i+2*k];
				h[i] = vars[i+3*k];
				final IntVar start = s[i];
				final IntVar end = e[i];
				final IntVar duration = d[i];
				IVariableMonitor update = new IVariableMonitor() {
					@Override
					public void onUpdate(Variable var, EventType evt, ICause cause) throws ContradictionException {
						// start
						start.updateLowerBound(end.getLB() - duration.getUB(), cause);
						start.updateUpperBound(end.getUB() - duration.getLB(), cause);
						// end
						end.updateLowerBound(start.getLB() + duration.getLB(), cause);
						end.updateUpperBound(start.getUB() + duration.getUB(), cause);
						// duration
						duration.updateLowerBound(end.getLB() - start.getUB(), cause);
						duration.updateUpperBound(end.getUB() - start.getLB(), cause);
					}
				};
				start.addMonitor(update);
				duration.addMonitor(update);
				end.addMonitor(update);
			}
			IntVar capa = vars[n-1];
			Constraint ctr = ConstraintFactory.cumulative(s,d,e,h,capa, solver);
			Constraint[] ctrs = new Constraint[]{ctr};
			AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(vars, env);
			solver.post(ctrs);
			solver.set(strategy);
			return solver;
		}
	};
}
