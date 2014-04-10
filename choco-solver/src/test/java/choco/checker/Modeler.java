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

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import memory.IEnvironment;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.Task;
import solver.variables.VariableFactory;
import util.tools.ArrayUtils;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
public interface Modeler {

    Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters);

    String name();

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
            Constraint ctr = IntConstraintFactory.arithm(vars[0], "=", vars[1]);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);

            return s;
        }

        @Override
        public String name() {
            return "modelEqAC";
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

            Constraint ctr = IntConstraintFactory.inverse_channeling(X, Y, 0, 0);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(allvars);
            s.post(ctrs);
            s.set(strategy);

            return s;
        }

        @Override
        public String name() {
            return "modelInverseChannelingAC";
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

            Constraint ctr = IntConstraintFactory.inverse_channeling(X, Y, 0, 0);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(allvars);
            s.post(ctrs);
            s.set(strategy);

            return s;
        }

        @Override
        public String name() {
            return "modelInverseChannelingBounds";
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
            Constraint ctr = IntConstraintFactory.arithm(vars[0], "!=", vars[1]);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);

            return s;
        }

        @Override
        public String name() {
            return "modelNeqAC";
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
            Constraint ctr = IntConstraintFactory.alldifferent(vars, "AC");
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelAllDiffAC";
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
            Constraint ctr = IntConstraintFactory.alldifferent(vars, "BC");
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelAllDiffBC";
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
            Constraint ctr = IntConstraintFactory.alldifferent(vars, "AC");
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelAllDiffGraph";
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
            Constraint ctr = IntConstraintFactory.alldifferent(vars, "AC");
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelAllDiffGraphBc";
        }
    };

    Modeler modelGCC = new Modeler() {
        @Override
        public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
            Solver s = new Solver("GCC_" + n);
            IEnvironment env = s.getEnvironment();

            boolean closed = (Boolean) parameters;
            IntVar[] vars = new IntVar[n / 2];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            int[] values = new int[n / 2];
            IntVar[] cards = new IntVar[n / 2];
            for (int i = 0; i < cards.length; i++) {
                values[i] = i;
                cards[i] = VariableFactory.enumerated("c_" + i, domains[i + n / 2], s);
                if (map != null) map.put(domains[i + n / 2], cards[i]);
            }
            Constraint ctr = IntConstraintFactory.global_cardinality(vars, values, cards, closed);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelGCC";
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
            Constraint ctr = IntConstraintFactory.times(vars[0], vars[1], vars[2]);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelTimes";
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
            Constraint ctr = IntConstraintFactory.absolute(vars[0], vars[1]);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelAbsolute";
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
            String ro = "=";
            switch (params[0]) {
                case 1:
                    ro = "<=";
                    break;
                case 2:
                    ro = ">=";
                    break;
            }
            IntVar tmp = VariableFactory.bounded("occ", 0, vars.length, s);
            Constraint link = IntConstraintFactory.arithm(tmp, ro, occVar);
            Constraint ctr = IntConstraintFactory.count(params[1], vars, tmp);
            Constraint[] ctrs = new Constraint[]{ctr, link};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelCountBC";
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
            String ro = "=";
            switch (params[0]) {
                case 1:
                    ro = "<=";
                    break;
                case 2:
                    ro = ">=";
                    break;
            }
            IntVar tmp = VariableFactory.bounded("occ", 0, vars.length, s);
            Constraint link = IntConstraintFactory.arithm(tmp, ro, occVar);
            Constraint ctr = IntConstraintFactory.count(params[1], vars, tmp);
            Constraint[] ctrs = new Constraint[]{ctr, link};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelCountAC";
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
            Constraint ctr = (Boolean) parameters?ICF.lex_less(X,Y):ICF.lex_less_eq(X,Y);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(ArrayUtils.append(X, Y));
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelLexAC";
        }
    };

    Modeler modelLexChainAC = new Modeler() {
        @Override
        public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
            Solver s = new Solver("LexChain");

            IntVar[] X = new IntVar[n / 3];
            for (int i = 0; i < n / 3; i++) {
                X[i] = VariableFactory.enumerated("X_" + i, domains[i], s);
                if (map != null) map.put(domains[i], X[i]);
            }
            IntVar[] Y = new IntVar[n / 3];
            for (int i = n / 3; i < 2 * n / 3; i++) {
                Y[i - n / 3] = VariableFactory.enumerated("Y_" + i, domains[i], s);
                if (map != null) map.put(domains[i], Y[i - n / 3]);
            }
            IntVar[] Z = new IntVar[n / 3];
            for (int i = 2 * n / 3; i < n; i++) {
                Z[i - 2 * n / 3] = VariableFactory.enumerated("Z_" + i, domains[i], s);
                if (map != null) map.put(domains[i], Z[i - 2 * n / 3]);
            }
            Constraint ctr = (Boolean) parameters?ICF.lex_chain_less(X,Y,Z):ICF.lex_chain_less_eq(X,Y,Z);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(ArrayUtils.append(X, Y, Z));
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelLexChainAC";
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
            Constraint ctr = IntConstraintFactory.element(vars[0], new int[]{-2, 0, 1, -1, 0, 4}, vars[1], 0, "detect");
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelNthBC";
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
            Constraint ctr = IntConstraintFactory.among(occVar, vars, params);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelAmongBC";
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
            Constraint ctr = IntConstraintFactory.among(occVar, vars, params);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelAmongAC";
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
            Constraint ctr = IntConstraintFactory.nvalues(decvars, vars[n - 1], (String[]) parameters);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelNValues";
        }
    };

    Modeler modelGCC_alldiff = new Modeler() {
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

            Constraint ctr = IntConstraintFactory.global_cardinality(vars, values, cards, false);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelGCC_alldiff";
        }
    };

    Modeler modelTree = new Modeler() {
        @Override
        public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
            Solver s = new Solver("tree_" + n);
            IEnvironment env = s.getEnvironment();

            IntVar[] vars = new IntVar[n];
            IntVar[] succs = new IntVar[n - 1];
            for (int i = 0; i < n; i++) {
                vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
                if (i < n - 1) {
                    succs[i] = vars[i];
                }
                if (map != null) map.put(domains[i], vars[i]);
            }
            IntVar nbRoots = vars[n - 1];
            Constraint ctr = IntConstraintFactory.tree(succs, nbRoots, 0);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelTree";
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
			Constraint ctr = IntConstraintFactory.circuit(vars, 0);
			Constraint[] ctrs = new Constraint[]{ctr};
			AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}

		@Override
		public String name() {
			return "modelCircuit";
		}
	};

	Modeler modelPath = new Modeler() {
		@Override
		public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
			Solver s = new Solver("path_" + n);
			IntVar[] vars = new IntVar[n-2];
			for (int i = 0; i < n-2; i++) {
				vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
				if (map != null) map.put(domains[i], vars[i]);
			}
			IntVar from = VariableFactory.enumerated("v_" + (n-2), domains[n-2], s);
			if (map != null) map.put(domains[n-2], from);
			IntVar to = VariableFactory.enumerated("v_" + (n-1), domains[n-1], s);
			if (map != null) map.put(domains[n-1], to);
			Constraint[] ctrs = IntConstraintFactory.path(vars, from, to, 0);
			AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
			s.post(ctrs);
			s.set(strategy);
			return s;
		}

		@Override
		public String name() {
			return "modelPath";
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
            Constraint ctr = IntConstraintFactory.subcircuit(vars, 0, VariableFactory.bounded("length", 0, vars.length - 1, s));
            Constraint[] ctrs = new Constraint[]{ctr};
            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelSubcircuit";
        }
    };

    Modeler modelDiffn = new Modeler() {
        @Override
        public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
            Solver s = new Solver("diffn_" + n);
            IEnvironment env = s.getEnvironment();
            IntVar[] vars = new IntVar[n];
            if (n % 4 != 0) {
                throw new UnsupportedOperationException();
            }
            int k = n / 4;
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
                y[i] = vars[i + k];
                dx[i] = vars[i + 2 * k];
                dy[i] = vars[i + 3 * k];
            }
            Constraint[] ctrs = IntConstraintFactory.diffn(x, y, dx, dy,true);
            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelDiffn";
        }
    };

    Modeler modelCumulative = new Modeler() {
        @Override
        public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
            Solver solver = new Solver("Cumulative_" + n);
            IEnvironment env = solver.getEnvironment();
            IntVar[] vars = new IntVar[n];
            if (n % 4 != 1) {
                throw new UnsupportedOperationException();
            }
            int k = n / 4;
            IntVar[] h = new IntVar[k];
            Task[] tasks = new Task[k];
            for (int i = 0; i < n; i++) {
                vars[i] = VariableFactory.enumerated("v_" + i, domains[i], solver);
                if (map != null) map.put(domains[i], vars[i]);
            }
            for (int i = 0; i < k; i++) {
                tasks[i] = VariableFactory.task(vars[i], vars[i + k], vars[i + 2 * k]);
                h[i] = vars[i + 3 * k];
            }
            IntVar capa = vars[vars.length - 1];
            Constraint ctr = IntConstraintFactory.cumulative(tasks, h, capa, true);
            Constraint[] ctrs = new Constraint[]{ctr};
            AbstractStrategy strategy = IntStrategyFactory.lexico_LB(vars);
            solver.post(ctrs);
            solver.set(strategy);
            return solver;
        }

        @Override
        public String name() {
            return "modelCumulative";
        }
    };
}
