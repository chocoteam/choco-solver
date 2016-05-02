/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.checker.fmk;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import static java.lang.System.arraycopy;
import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;

/**
 * @author Jean-Guillaume Fages
 * @since 01/13
 */
public interface SetTestModel {

    Model model(int n, Variable[] rvars, Domain[] domains, Object parameters);

    SetTestModel setUnion = new SetTestModel() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length; i++)
                types[i] = Correctness.SET;
        }

        public Model model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Model s = new Model("boolSum_" + n);
            SetVar[] vars = new SetVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = s.setVar("s_" + i, domains[i].getSetKer(), domains[i].getSetEnv());
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            SetVar[] sets = new SetVar[n - 1];
            arraycopy(vars, 0, sets, 0, n - 1);
            s.union(sets, vars[n - 1]).post();
            return s;
        }
    };

    SetTestModel setInter = new SetTestModel() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length; i++)
                types[i] = Correctness.SET;
        }

        public Model model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Model s = new Model("boolSum_" + n);
            SetVar[] vars = new SetVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = s.setVar("s_" + i, domains[i].getSetKer(), domains[i].getSetEnv());
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            SetVar[] sets = new SetVar[n - 1];
            arraycopy(vars, 0, sets, 0, n - 1);
            s.intersection(sets, vars[n - 1]).post();
            return s;
        }
    };

    SetTestModel setDisj = new SetTestModel() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length; i++)
                types[i] = Correctness.SET;
        }

        public Model model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Model s = new Model("boolSum_" + n);
            SetVar[] vars = new SetVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = s.setVar("s_" + i, domains[i].getSetKer(), domains[i].getSetEnv());
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            s.allDisjoint(vars).post();
            return s;
        }
    };

    SetTestModel setDiff = new SetTestModel() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length; i++)
                types[i] = Correctness.SET;
        }

        public Model model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Model s = new Model("boolSum_" + n);
            SetVar[] vars = new SetVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = s.setVar("s_" + i, domains[i].getSetKer(), domains[i].getSetEnv());
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            s.allDifferent(vars).post();
            return s;
        }
    };

    SetTestModel setSubSet = new SetTestModel() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length; i++)
                types[i] = Correctness.SET;
        }

        public Model model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Model s = new Model("boolSum_" + n);
            SetVar[] vars = new SetVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = s.setVar("s_" + i, domains[i].getSetKer(), domains[i].getSetEnv());
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            s.subsetEq(vars).post();
            return s;
        }
    };

    SetTestModel setAllEq = new SetTestModel() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length; i++)
                types[i] = Correctness.SET;
        }

        public Model model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Model s = new Model("boolSum_" + n);
            SetVar[] vars = new SetVar[n];
            for (int i = 0; i < n; i++) {
                vars[i] = s.setVar("s_" + i, domains[i].getSetKer(), domains[i].getSetEnv());
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            s.allEqual(vars).post();
            return s;
        }
    };

    SetTestModel boolSum = new SetTestModel() {
        public void fillTypes(int[] types) {
            for (int i = 0; i < types.length - 1; i++)
                types[i] = Correctness.BOOL;
        }

        public Model model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Model s = new Model("boolSum_" + n);
            IntVar[] vars = new IntVar[n];
            BoolVar[] bools = new BoolVar[n - 1];
            vars[n - 1] = s.intVar("sum", domains[n - 1].getIntDom());
            if (rvars[n - 1] == null) rvars[n - 1] = vars[n - 1];
            for (int i = 0; i < n - 1; i++) {
                vars[i] = bools[i] = s.boolVar("v_" + i);
                if (domains[i].getIntDom().length == 1) {
                    try {
                        bools[i].instantiateTo(domains[i].getIntDom()[0], Null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (rvars[i] == null) rvars[i] = vars[i];
            }
            s.sum(bools, "=", vars[n - 1]).post();
            s.getSolver().set(inputOrderLBSearch(vars));
            return s;
        }
    };

    SetTestModel modelEqAC = new SetTestModel() {
        public void fillTypes(int[] types) {
        }

        public Model model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Model s = new Model("EqAC_" + n);
            IntVar[] vars = new IntVar[n];
            try {
                for (int i = 0; i < vars.length; i++) {
                    vars[i] = s.intVar("v_" + i, domains[i].getIntDom());
                    if (rvars[i] == null) rvars[i] = vars[i];
                }
            } catch (ArrayIndexOutOfBoundsException ce) {
//                System.out.printf("");
            }
            s.arithm(vars[0], "=", vars[1]).post();
            s.getSolver().set(inputOrderLBSearch(vars));
            return s;
        }
    };

    SetTestModel modelInverseChannelingAC = new SetTestModel() {
        public void fillTypes(int[] types) {
        }

        public Model model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Model s = new Model("InverseChannelingAC_" + n);
            IntVar[] X = new IntVar[n / 2];
            IntVar[] Y = new IntVar[n / 2];
            for (int i = 0; i < n / 2; i++) {
                X[i] = s.intVar("X_" + i, domains[i].getIntDom());
                if (rvars[i] == null) rvars[i] = X[i];
                Y[i] = s.intVar("Y_" + i, domains[i + (n / 2)].getIntDom());
                if (rvars[i + n / 2] == null) rvars[i + n / 2] = Y[i];
            }
            IntVar[] allvars = append(X, Y);
            s.inverseChanneling(X, Y, 0, 0).post();
            s.getSolver().set(inputOrderLBSearch(allvars));
            return s;
        }
    };

    SetTestModel modelNValues = new SetTestModel() {
        public void fillTypes(int[] types) {
        }

        public Model model(int n, Variable[] rvars, Domain[] domains, Object parameters) {
            Model s = new Model("modelNValues" + n);
            IntVar[] vars = new IntVar[n];
            IntVar[] decvars = new IntVar[n - 1];
            for (int i = 0; i < n; i++) {
                vars[i] = s.intVar("v_" + i, domains[i].getIntDom());
                if (rvars[i] == null) rvars[i] = vars[i];
                if (i < n - 1) {
                    decvars[i] = vars[i];
                }
            }
            Solver r = s.getSolver();
            s.nValues(decvars, vars[n - 1]).post();
            r.set(inputOrderLBSearch(vars));
            return s;
        }
    };

    void fillTypes(int[] types);
}
