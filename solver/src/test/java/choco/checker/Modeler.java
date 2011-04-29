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
import gnu.trove.THashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.InverseChanneling;
import solver.constraints.ternary.Times;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
public interface Modeler {

    Solver model(int n, int[][] domains, THashMap<int[], IntVar> map);


    Modeler modelEqAC = new Modeler() {
        @Override
        public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map) {
            Solver s = new Solver("EqAC_" + n);
            IEnvironment env = s.getEnvironment();

            IntVar[] vars = new IntVar[n];
            try {
                for (int i = 0; i < vars.length; i++) {
                    vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
                    map.put(domains[i], vars[i]);
                }
            } catch (ArrayIndexOutOfBoundsException ce) {
                System.out.printf("");
            }
            Constraint ctr = ConstraintFactory.eq(vars[0], vars[1], s);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = StrategyFactory.inputOrderIncDomain(vars, env);
            s.post(ctrs);
            s.set(strategy);

            return s;
        }
    };

    Modeler modelInverseChannelingAC = new Modeler() {
        @Override
        public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map) {
            Solver s = new Solver("InverseChannelingAC_" + n);
            IEnvironment env = s.getEnvironment();

            IntVar[] X = new IntVar[n / 2];
            IntVar[] Y = new IntVar[n / 2];
            for (int i = 0; i < n / 2; i++) {
                X[i] = VariableFactory.enumerated("X_" + i, domains[i], s);
                map.put(domains[i], X[i]);
                Y[i] = VariableFactory.enumerated("Y_" + i, domains[i + (n / 2)], s);
                map.put(domains[i + (n / 2)], Y[i]);
            }
            IntVar[] allvars = ArrayUtils.append(X, Y);

            Constraint ctr = new InverseChanneling(X, Y, s);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = StrategyFactory.inputOrderIncDomain(allvars, env);
            s.post(ctrs);
            s.set(strategy);

            return s;
        }
    };

    Modeler modelNeqAC = new Modeler() {
        @Override
        public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map) {
            Solver s = new Solver("NeqAC_" + n);
            IEnvironment env = s.getEnvironment();

            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
                map.put(domains[i], vars[i]);
            }
            Constraint ctr = ConstraintFactory.neq(vars[0], vars[1], s);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = StrategyFactory.inputOrderIncDomain(vars, env);
            s.post(ctrs);
            s.set(strategy);

            return s;
        }
    };

    Modeler modelAllDiffAC = new Modeler() {
        @Override
        public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map) {
            Solver s = new Solver("AllDiffAC_" + n);
            IEnvironment env = s.getEnvironment();

            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
                map.put(domains[i], vars[i]);
            }
            Constraint ctr = new AllDifferent(vars, s, AllDifferent.Type.AC);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = StrategyFactory.inputOrderIncDomain(vars, env);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

    Modeler modelAllDiffBC = new Modeler() {
        @Override
        public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map) {
            Solver s = new Solver("AllDiffBC_" + n);
            IEnvironment env = s.getEnvironment();

            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VariableFactory.bounded("v_" + i, domains[i][0], domains[i][domains[i].length-1], s);
                map.put(domains[i], vars[i]);
            }
            Constraint ctr = new AllDifferent(vars, s, AllDifferent.Type.BC);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = StrategyFactory.inputOrderIncDomain(vars, env);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

    Modeler modelTimes = new Modeler() {
        @Override
        public Solver model(int n, int[][] domains, THashMap<int[], IntVar> map) {
            Solver s = new Solver("Times_" + n);
            IEnvironment env = s.getEnvironment();

            IntVar[] vars = new IntVar[n];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = VariableFactory.enumerated("v_" + i, domains[i], s);
                map.put(domains[i], vars[i]);
            }
            Constraint ctr = new Times(vars[0], vars[1], vars[2], s);
            Constraint[] ctrs = new Constraint[]{ctr};

            AbstractStrategy strategy = StrategyFactory.inputOrderIncDomain(vars, env);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

}
