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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.nary.nValue.PropAtLeastNValues_AC;
import solver.constraints.nary.nValue.PropAtMostNValues_BC;
import solver.search.strategy.ISF;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.Task;
import solver.variables.VF;
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
            IntVar[] vars = new IntVar[n];
            try {
                for (int i = 0; i < vars.length; i++) {
                    vars[i] = VF.enumerated("v_" + i, domains[i], s);
                    if (map != null) map.put(domains[i], vars[i]);
                }
            } catch (ArrayIndexOutOfBoundsException ce) {
//                System.out.printf("");
            }
            s.post(ICF.arithm(vars[0], "=", vars[1]));
            s.set(ISF.random_value(vars));
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
            IntVar[] X = new IntVar[n / 2];
            IntVar[] Y = new IntVar[n / 2];
            for (int i = 0; i < n / 2; i++) {
                X[i] = VF.enumerated("X_" + i, domains[i], s);
                if (map != null) map.put(domains[i], X[i]);
                Y[i] = VF.enumerated("Y_" + i, domains[i + (n / 2)], s);
                if (map != null) map.put(domains[i + (n / 2)], Y[i]);
            }
            IntVar[] allvars = ArrayUtils.append(X, Y);
            s.post(ICF.inverse_channeling(X, Y, 0, 0));
            s.set(ISF.random_value(allvars));
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
                X[i] = VF.bounded("X_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
                if (map != null) map.put(domains[i], X[i]);
                Y[i] = VF.bounded("Y_" + i, domains[i + off][0], domains[i + off][domains[i + off].length - 1], s);
                if (map != null) map.put(domains[i + (n / 2)], Y[i]);
            }
            IntVar[] allvars = ArrayUtils.append(X, Y);
            s.post(ICF.inverse_channeling(X, Y, 0, 0));
            s.set(ISF.random_bound(allvars));
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
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            s.post(ICF.arithm(vars[0], "!=", vars[1]));
            s.set(ISF.random_value(vars));
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
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            s.post(ICF.alldifferent(vars, "AC"));
            s.set(ISF.random_value(vars));
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
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            s.post(ICF.alldifferent(vars, "BC"));
            s.set(ISF.random_bound(vars));
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
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            s.post(ICF.alldifferent(vars, "AC"));
            s.set(ISF.random_value(vars));
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
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            s.post(ICF.alldifferent(vars, "AC"));
            s.set(ISF.random_bound(vars));
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
            boolean closed = (Boolean) parameters;
            IntVar[] vars = new IntVar[n / 2];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            int[] values = new int[n / 2];
            IntVar[] cards = new IntVar[n / 2];
            for (int i = 0; i < cards.length; i++) {
                values[i] = i;
                cards[i] = VF.enumerated("c_" + i, domains[i + n / 2], s);
                if (map != null) map.put(domains[i + n / 2], cards[i]);
            }
            s.post(ICF.global_cardinality(vars, values, cards, closed));
            s.set(ISF.random_value(vars));
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
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            s.post(ICF.times(vars[0], vars[1], vars[2]));
            s.set(ISF.random_value(vars));
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
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            s.post(ICF.absolute(vars[0], vars[1]));
            s.set(ISF.random_bound(vars));
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
            IntVar[] vars = new IntVar[n - 1];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            IntVar occVar = VF.bounded("ovar", domains[n - 1][0], domains[n - 1][domains[n - 1].length - 1], s);
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
            IntVar tmp = VF.bounded("occ", 0, vars.length, s);
            s.post(
                    ICF.arithm(tmp, ro, occVar),
                    ICF.count(params[1], vars, tmp)
            );
            s.set(ISF.random_bound(vars));
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
            IntVar[] vars = new IntVar[n - 1];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            IntVar occVar = VF.enumerated("ovar", domains[n - 1], s);
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
            IntVar tmp = VF.bounded("occ", 0, vars.length, s);
            s.post(
                    ICF.count(params[1], vars, tmp),
                    ICF.arithm(tmp, ro, occVar)
            );
            s.set(ISF.random_bound(vars));
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
            IntVar[] X = new IntVar[n / 2];
            for (int i = 0; i < n / 2; i++) {
                X[i] = VF.enumerated("X_" + i, domains[i], s);
                if (map != null) map.put(domains[i], X[i]);
            }
            IntVar[] Y = new IntVar[n / 2];
            for (int i = n / 2; i < n; i++) {
                Y[i - n / 2] = VF.enumerated("Y_" + i, domains[i], s);
                if (map != null) map.put(domains[i], Y[i - n / 2]);
            }
            Constraint ctr = (Boolean) parameters ? ICF.lex_less(X, Y) : ICF.lex_less_eq(X, Y);
            s.post(ctr);
            s.set(ISF.random_value(ArrayUtils.append(X, Y)));
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
                X[i] = VF.enumerated("X_" + i, domains[i], s);
                if (map != null) map.put(domains[i], X[i]);
            }
            IntVar[] Y = new IntVar[n / 3];
            for (int i = n / 3; i < 2 * n / 3; i++) {
                Y[i - n / 3] = VF.enumerated("Y_" + i, domains[i], s);
                if (map != null) map.put(domains[i], Y[i - n / 3]);
            }
            IntVar[] Z = new IntVar[n / 3];
            for (int i = 2 * n / 3; i < n; i++) {
                Z[i - 2 * n / 3] = VF.enumerated("Z_" + i, domains[i], s);
                if (map != null) map.put(domains[i], Z[i - 2 * n / 3]);
            }
            Constraint ctr = (Boolean) parameters ? ICF.lex_chain_less(X, Y, Z) : ICF.lex_chain_less_eq(X, Y, Z);
            s.post(ctr);
            s.set(ISF.random_value(ArrayUtils.append(X, Y, Z)));
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
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            s.post(ICF.element(vars[0], new int[]{-2, 0, 1, -1, 0, 4}, vars[1], 0, "detect"));
            s.set(ISF.random_bound(vars));
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
            IntVar[] vars = new IntVar[n - 1];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            IntVar occVar = VF.enumerated("ovar", domains[n - 1][0], domains[n - 1][domains[n - 1].length - 1], s);
            if (map != null) map.put(domains[n - 1], occVar);
            int[] params = (int[]) parameters;
            s.post(ICF.among(occVar, vars, params));
            s.set(ISF.random_bound(vars));
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
            IntVar[] vars = new IntVar[n - 1];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            IntVar occVar = VF.enumerated("ovar", domains[n - 1], s);
            if (map != null) map.put(domains[n - 1], occVar);
            int[] params = (int[]) parameters;
            s.post(ICF.among(occVar, vars, params));
            s.set(ISF.random_value(vars));
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
            IntVar[] vars = new IntVar[n];
            IntVar[] decvars = new IntVar[n - 1];
            for (int i = 0; i < n; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
                if (i < n - 1) {
                    decvars[i] = vars[i];
                }
            }
			s.post(ICF.atleast_nvalues(decvars, vars[n - 1],false));
			s.post(ICF.atmost_nvalues(decvars, vars[n - 1],false));
			for(String st : (String[]) parameters){
				switch (st){
					case "at_least_AC": s.post(new Constraint("atLeastNVAC",new PropAtLeastNValues_AC(decvars,vars[n - 1])));break;
					case "at_most_BC" : s.post(new Constraint("atMostBC",new PropAtMostNValues_BC(decvars,vars[n - 1])));break;
					case "at_most_greedy": s.post(ICF.nvalues(decvars, vars[n - 1]));break;
					default : throw new UnsupportedOperationException();
				}
			}
            s.set(ISF.random_value(vars));
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
            IntVar[] vars = new IntVar[n];
            TIntArrayList vals = new TIntArrayList();
            for (int i = 0; i < n; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                for (int j : domains[i]) {
                    if (!vals.contains(j)) {
                        vals.add(j);
                    }
                }
                if (map != null) map.put(domains[i], vars[i]);
            }
            int[] values = vals.toArray();
            IntVar[] cards = VF.boolArray("cards", values.length, s);
            s.post(ICF.global_cardinality(vars, values, cards, false));
            s.set(ISF.random_value(vars));
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
            IntVar[] vars = new IntVar[n];
            IntVar[] succs = new IntVar[n - 1];
            for (int i = 0; i < n; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (i < n - 1) {
                    succs[i] = vars[i];
                }
                if (map != null) map.put(domains[i], vars[i]);
            }
            IntVar nbRoots = vars[n - 1];
            s.post(ICF.tree(succs, nbRoots, 0));
            s.set(ISF.random_value(vars));
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
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            s.post(ICF.circuit(vars, 0));
            s.set(ISF.random_value(vars));
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
            IntVar[] vars = new IntVar[n - 2];
            for (int i = 0; i < n - 2; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            IntVar from = VF.enumerated("v_" + (n - 2), domains[n - 2], s);
            if (map != null) map.put(domains[n - 2], from);
            IntVar to = VF.enumerated("v_" + (n - 1), domains[n - 1], s);
            if (map != null) map.put(domains[n - 1], to);
            s.post(ICF.path(vars, from, to, 0));
            s.set(ISF.random_value(ArrayUtils.append(vars, new IntVar[]{from, to})));
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
            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            s.post(ICF.subcircuit(vars, 0, VF.bounded("length", 0, vars.length - 1, s)));
            s.set(ISF.random_value(vars));
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
                vars[i] = VF.enumerated("v_" + i, domains[i], s);
                if (map != null) map.put(domains[i], vars[i]);
            }
            for (int i = 0; i < k; i++) {
                x[i] = vars[i];
                y[i] = vars[i + k];
                dx[i] = vars[i + 2 * k];
                dy[i] = vars[i + 3 * k];
            }
            s.post(ICF.diffn(x, y, dx, dy, true));
            s.set(ISF.random_bound(vars));
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
            IntVar[] vars = new IntVar[n];
            if (n % 4 != 1) {
                throw new UnsupportedOperationException();
            }
            int k = n / 4;
            IntVar[] h = new IntVar[k];
            Task[] tasks = new Task[k];
            for (int i = 0; i < n; i++) {
                vars[i] = VF.enumerated("v_" + i, domains[i], solver);
                if (map != null) map.put(domains[i], vars[i]);
            }
            for (int i = 0; i < k; i++) {
                tasks[i] = VF.task(vars[i], vars[i + k], vars[i + 2 * k]);
                h[i] = vars[i + 3 * k];
            }
            IntVar capa = vars[vars.length - 1];
            solver.post(ICF.cumulative(tasks, h, capa, true));
            solver.set(ISF.random_bound(vars));
            return solver;
        }

        @Override
        public String name() {
            return "modelCumulative";
        }
    };

    Modeler modelSortBC = new Modeler() {
        @Override
        public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map, Object parameters) {
            Solver s = new Solver("Sort");

            IntVar[] X = new IntVar[n / 2];
            for (int i = 0; i < n / 2; i++) {
                X[i] = VariableFactory.bounded("X_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
                if (map != null) map.put(domains[i], X[i]);
            }
            IntVar[] Y = new IntVar[n / 2];
            for (int i = n / 2; i < n; i++) {
                Y[i - n / 2] = VariableFactory.bounded("Y_" + i, domains[i][0], domains[i][domains[i].length - 1], s);
                if (map != null) map.put(domains[i], Y[i - n / 2]);
            }
            s.post(ICF.sort(X, Y));

            AbstractStrategy strategy = IntStrategyFactory.random_bound(ArrayUtils.append(X, Y), System.currentTimeMillis());
            s.set(strategy);
            return s;
        }

        @Override
        public String name() {
            return "modelSortBC";
        }
    };
}
