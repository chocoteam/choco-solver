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

package choco.checker.fmk;

import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import com.sun.istack.internal.Nullable;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.propagators.nary.sum.PropBoolSum;
import solver.constraints.set.SetConstraintsFactory;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.SetStrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.*;

/**
 * @author Jean-Guillaume Fages
 * @since 01/13
 */
public interface Model {

    Solver model(int n, Variable[] rvars, Domain[] domains, Object parameters);

    Model setUnion = new Model() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length; i++)
                types[i] = Correctness.SET;
        }

        public Solver model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Solver s = new Solver("boolSum_" + n);
            IEnvironment env = s.getEnvironment();
            SetVar[] vars = new SetVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = VariableFactory.set("s_" + i, s);
                for (int e : domains[i].getSetEnv()) {
                    vars[i].getEnvelope().add(e);
                }
                for (int k : domains[i].getSetKer()) {
                    vars[i].getKernel().add(k);
                }
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            SetVar[] sets = new SetVar[n - 1];
            System.arraycopy(vars, 0, sets, 0, n - 1);
            Constraint ctr = SetConstraintsFactory.union(sets, vars[n - 1]);
            Constraint[] ctrs = new Constraint[]{ctr};
            AbstractStrategy strategy = SetStrategyFactory.setLex(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

    Model setInter = new Model() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length; i++)
                types[i] = Correctness.SET;
        }

        public Solver model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Solver s = new Solver("boolSum_" + n);
            IEnvironment env = s.getEnvironment();
            SetVar[] vars = new SetVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = VariableFactory.set("s_" + i, s);
                for (int e : domains[i].getSetEnv()) {
                    vars[i].getEnvelope().add(e);
                }
                for (int k : domains[i].getSetKer()) {
                    vars[i].getKernel().add(k);
                }
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            SetVar[] sets = new SetVar[n - 1];
            System.arraycopy(vars, 0, sets, 0, n - 1);
            Constraint ctr = SetConstraintsFactory.intersection(sets, vars[n - 1]);
            Constraint[] ctrs = new Constraint[]{ctr};
            AbstractStrategy strategy = SetStrategyFactory.setLex(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

    Model setDisj = new Model() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length; i++)
                types[i] = Correctness.SET;
        }

        public Solver model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Solver s = new Solver("boolSum_" + n);
            IEnvironment env = s.getEnvironment();
            SetVar[] vars = new SetVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = VariableFactory.set("s_" + i, s);
                for (int e : domains[i].getSetEnv()) {
                    vars[i].getEnvelope().add(e);
                }
                for (int k : domains[i].getSetKer()) {
                    vars[i].getKernel().add(k);
                }
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            Constraint ctr = SetConstraintsFactory.all_disjoint(vars);
            Constraint[] ctrs = new Constraint[]{ctr};
            AbstractStrategy strategy = SetStrategyFactory.setLex(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

    Model setDiff = new Model() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length; i++)
                types[i] = Correctness.SET;
        }

        public Solver model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Solver s = new Solver("boolSum_" + n);
            IEnvironment env = s.getEnvironment();
            SetVar[] vars = new SetVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = VariableFactory.set("s_" + i, s);
                for (int e : domains[i].getSetEnv()) {
                    vars[i].getEnvelope().add(e);
                }
                for (int k : domains[i].getSetKer()) {
                    vars[i].getKernel().add(k);
                }
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            Constraint ctr = SetConstraintsFactory.all_different(vars);
            Constraint[] ctrs = new Constraint[]{ctr};
            AbstractStrategy strategy = SetStrategyFactory.setLex(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

    Model setSubSet = new Model() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length; i++)
                types[i] = Correctness.SET;
        }

        public Solver model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Solver s = new Solver("boolSum_" + n);
            IEnvironment env = s.getEnvironment();
            SetVar[] vars = new SetVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = VariableFactory.set("s_" + i, s);
                for (int e : domains[i].getSetEnv()) {
                    vars[i].getEnvelope().add(e);
                }
                for (int k : domains[i].getSetKer()) {
                    vars[i].getKernel().add(k);
                }
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            Constraint ctr = SetConstraintsFactory.subsetEq(vars);
            Constraint[] ctrs = new Constraint[]{ctr};
            AbstractStrategy strategy = SetStrategyFactory.setLex(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

    Model setAllEq = new Model() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length; i++)
                types[i] = Correctness.SET;
        }

        public Solver model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Solver s = new Solver("boolSum_" + n);
            IEnvironment env = s.getEnvironment();
            SetVar[] vars = new SetVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = VariableFactory.set("s_" + i, s);
                for (int e : domains[i].getSetEnv()) {
                    vars[i].getEnvelope().add(e);
                }
                for (int k : domains[i].getSetKer()) {
                    vars[i].getKernel().add(k);
                }
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            Constraint ctr = SetConstraintsFactory.all_equal(vars);
            Constraint[] ctrs = new Constraint[]{ctr};
            AbstractStrategy strategy = SetStrategyFactory.setLex(vars);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

    Model boolSum = new Model() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length - 1; i++)
                types[i] = Correctness.BOOL;
        }

        public Solver model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Solver s = new Solver("boolSum_" + n);
            IEnvironment env = s.getEnvironment();
            IntVar[] vars = new IntVar[n];
            BoolVar[] bools = new BoolVar[n - 1];
            vars[n - 1] = VariableFactory.enumerated("sum", domains[n - 1].getIntDom(), s);
            if (rvars[n - 1] == null) rvars[n - 1] = vars[n - 1];
            for (int i = 0; i < n - 1; i++) {
                vars[i] = bools[i] = VariableFactory.bool("v_" + i, s);
                if (domains[i].getIntDom().length == 1) {
                    try {
                        bools[i].instantiateTo(domains[i].getIntDom()[0], dummyCause);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            Constraint ctr = new Constraint(s);
            ctr.addPropagators(new PropBoolSum(bools, vars[n - 1], s, ctr));
            Constraint[] ctrs = new Constraint[]{ctr};
            AbstractStrategy strategy = IntStrategyFactory.inputOrderMinVal(vars, env);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

    Model modelEqAC = new Model() {
        public void fillTypes(int[] types) {
        }

        public Solver model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Solver s = new Solver("EqAC_" + n);
            IEnvironment env = s.getEnvironment();
            IntVar[] vars = new IntVar[n];
            try {
                for (int i = 0; i < vars.length; i++) {
                    vars[i] = VariableFactory.enumerated("v_" + i, domains[i].getIntDom(), s);
                    if (rvars[i] == null) rvars[i] = vars[i];
                }
            } catch (ArrayIndexOutOfBoundsException ce) {
                System.out.printf("");
            }
            Constraint ctr = IntConstraintFactory.arithm(vars[0], "=", vars[1]);
            Constraint[] ctrs = new Constraint[]{ctr};
            AbstractStrategy strategy = IntStrategyFactory.inputOrderMinVal(vars, env);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

    Model modelInverseChannelingAC = new Model() {
        public void fillTypes(int[] types) {
        }

        public Solver model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Solver s = new Solver("InverseChannelingAC_" + n);
            IEnvironment env = s.getEnvironment();
            IntVar[] X = new IntVar[n / 2];
            IntVar[] Y = new IntVar[n / 2];
            for (int i = 0; i < n / 2; i++) {
                X[i] = VariableFactory.enumerated("X_" + i, domains[i].getIntDom(), s);
                if (rvars[i] == null) rvars[i] = X[i];
                Y[i] = VariableFactory.enumerated("Y_" + i, domains[i + (n / 2)].getIntDom(), s);
                if (rvars[i + n / 2] == null) rvars[i + n / 2] = Y[i];
            }
            IntVar[] allvars = ArrayUtils.append(X, Y);
            Constraint ctr = IntConstraintFactory.channeling(X, Y);
            Constraint[] ctrs = new Constraint[]{ctr};
            AbstractStrategy strategy = IntStrategyFactory.inputOrderMinVal(allvars, env);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

    Model modelNValues_AtMostBC = new Model() {
        public void fillTypes(int[] types) {
        }

        public Solver model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Solver s = new Solver("modelNValues_AtMostBC_" + n);
            IEnvironment env = s.getEnvironment();
            IntVar[] vars = new IntVar[n];
            IntVar[] decvars = new IntVar[n - 1];
            for (int i = 0; i < n; i++) {
                vars[i] = VariableFactory.enumerated("v_" + i, domains[i].getIntDom(), s);
                if (rvars[i] == null) rvars[i] = vars[i];
                if (i < n - 1) {
                    decvars[i] = vars[i];
                }
            }
            Constraint ctr = IntConstraintFactory.nvalues(decvars, vars[n - 1], "at_most_BC");
            Constraint[] ctrs = new Constraint[]{ctr};
            AbstractStrategy strategy = IntStrategyFactory.inputOrderMinVal(vars, env);
            s.post(ctrs);
            s.set(strategy);
            return s;
        }
    };

    public void fillTypes(int[] types);

    final static ICause dummyCause = new ICause() {
        public Constraint getConstraint() {
            return null;
        }

        public void explain(@Nullable Deduction d, Explanation e) {
        }

        public boolean reactOnPromotion() {
            return false;
        }

        public int getPropagationConditions(int vIdx) {
            return 0;
        }
    };
}
