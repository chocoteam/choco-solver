/**
*  Copyright (c) 2010, Ecole des Mines de Nantes
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

package choco.propagation.proba.nqueen;

import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import org.testng.Assert;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.InverseChanneling;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
public class NQueenModeler {



    public static Solver modelBinaryGlobal(int n, int[][] domains) {
        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, domains[i], s);
        }

        List<Constraint> lcstrs = new ArrayList<Constraint>(10);
        List<Constraint> lcstrs1 = new ArrayList<Constraint>(1);
        List<Constraint> lcstrs2 = new ArrayList<Constraint>(10);

        lcstrs1.add(new AllDifferent(vars, s, AllDifferent.Type.PROBABILISTIC));
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                lcstrs2.add(ConstraintFactory.neq(vars[i], vars[j], -k, s));
                lcstrs2.add(ConstraintFactory.neq(vars[i], vars[j], k, s));
            }
        }
        lcstrs.addAll(lcstrs1);
        lcstrs.addAll(lcstrs2);

        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

        AbstractStrategy strategy = StrategyFactory.inputOrderIncDomain(vars, env);

        //AbstractPilotPropag pilot = Pilots.preset(eng1, eng2);
        //s.post(cstrs);
        //s.set(pilot, strategy);
        Assert.fail();
//        eng1.initEngine(lcstrs1.toArray(new Constraint[lcstrs1.size()]), vars);
//        eng2.initEngine(lcstrs2.toArray(new Constraint[lcstrs2.size()]), vars);
        return s;
    }


    public static Solver modelGlobal(int n, int[][] domains) {
        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();

        IntVar[] vars = new IntVar[n];
        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];

        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, domains[i], s);
            diag1[i] = VariableFactory.enumerated("D1_" + i, 1, 2 * n, s);
            diag2[i] = VariableFactory.enumerated("D2_" + i, -n, n, s);
        }

        IntVar[] allvars = ArrayUtils.append(vars, diag1, diag2);

        List<Constraint> lcstrs = new ArrayList<Constraint>(10);

        lcstrs.add(new AllDifferent(vars, s, AllDifferent.Type.PROBABILISTIC));
        for (int i = 0; i < n; i++) {
            lcstrs.add(ConstraintFactory.eq(diag1[i], vars[i], i, s));
            lcstrs.add(ConstraintFactory.eq(diag2[i], vars[i], -i, s));
        }
        lcstrs.add(new AllDifferent(diag1, s, AllDifferent.Type.PROBABILISTIC));
        lcstrs.add(new AllDifferent(diag2, s, AllDifferent.Type.PROBABILISTIC));

        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

        AbstractStrategy strategy = StrategyFactory.inputOrderIncDomain(vars, env);

        s.post(cstrs);
        s.set(strategy);
        return s;
    }

    public static Solver modelGlobal2(int n, int[][] domains) {
        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();

        IntVar[] vars = new IntVar[n];
        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];

        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, domains[i], s);
            diag1[i] = VariableFactory.enumerated("D1_" + i, 1, 2 * n, s);
            diag2[i] = VariableFactory.enumerated("D2_" + i, -n, n, s);
        }

        IntVar[] allvars = ArrayUtils.append(vars, diag1, diag2);

        List<Constraint> lcstrs = new ArrayList<Constraint>(10);

        List<Constraint> lcstrs1 = new ArrayList<Constraint>(10);
        List<Constraint> lcstrs2 = new ArrayList<Constraint>(10);
//        List<Constraint> lcstrs3 = new ArrayList<Constraint>(10);

        lcstrs1.add(new AllDifferent(vars, s, AllDifferent.Type.PROBABILISTIC));
        for (int i = 0; i < n; i++) {
            lcstrs2.add(ConstraintFactory.eq(diag1[i], vars[i], i, s));
            lcstrs2.add(ConstraintFactory.eq(diag2[i], vars[i], -i, s));
        }
        lcstrs1.add(new AllDifferent(diag1, s, AllDifferent.Type.PROBABILISTIC));
        lcstrs1.add(new AllDifferent(diag2, s, AllDifferent.Type.PROBABILISTIC));

        lcstrs.addAll(lcstrs1);
        lcstrs.addAll(lcstrs2);
//        lcstrs.addAll(lcstrs3);

        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

        AbstractStrategy strategy = StrategyFactory.inputOrderIncDomain(vars, env);

        //AbstractPilotPropag pilot = Pilots.basic(eng1, eng2);
        s.post(cstrs);
        //s.set(pilot, strategy);
        Assert.fail();
//        eng1.initEngine(lcstrs1.toArray(new Constraint[lcstrs1.size()]), allvars);
//        eng2.initEngine(lcstrs2.toArray(new Constraint[lcstrs2.size()]), allvars);
//        eng3.initEngine(lcstrs3.toArray(new Constraint[lcstrs3.size()]), ArrayUtils.append(diag2, vars));
        return s;
    }


    public static Solver modelDualGlobal(int n, int[][] domains) {
        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();

        IntVar[] vars = new IntVar[n];
        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];

        IntVar[] dualvars = new IntVar[n];
        IntVar[] dualdiag1 = new IntVar[n];
        IntVar[] dualdiag2 = new IntVar[n];

        for (int i = 0; i < n; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, domains[i], s);
            diag1[i] = VariableFactory.enumerated("D1_" + i, 1, 2 * n, s);
            diag2[i] = VariableFactory.enumerated("D2_" + i, -n, n, s);

            dualvars[i] = VariableFactory.enumerated("DQ_" + i, 1, n, s);
            dualdiag1[i] = VariableFactory.enumerated("DD1_" + i, 1, 2 * n, s);
            dualdiag2[i] = VariableFactory.enumerated("DD2_" + i, -n, n, s);
        }

        IntVar[] allvars = ArrayUtils.append(vars, dualvars, diag1, diag2, dualdiag1, dualdiag2);

        List<Constraint> lcstrs = new ArrayList<Constraint>(10);

        lcstrs.add(new AllDifferent(vars, s, AllDifferent.Type.PROBABILISTIC));
        lcstrs.add(new AllDifferent(dualvars, s, AllDifferent.Type.PROBABILISTIC));

        for (int i = 0; i < n; i++) {
            lcstrs.add(ConstraintFactory.eq(diag1[i], vars[i], i, s));
            lcstrs.add(ConstraintFactory.eq(diag2[i], vars[i], -i, s));

            lcstrs.add(ConstraintFactory.eq(dualdiag1[i], dualvars[i], i, s));
            lcstrs.add(ConstraintFactory.eq(dualdiag2[i], dualvars[i], -i, s));
        }
        lcstrs.add(new AllDifferent(diag1, s, AllDifferent.Type.PROBABILISTIC));
        lcstrs.add(new AllDifferent(diag2, s, AllDifferent.Type.PROBABILISTIC));
        lcstrs.add(new AllDifferent(dualdiag1, s, AllDifferent.Type.PROBABILISTIC));
        lcstrs.add(new AllDifferent(dualdiag2, s, AllDifferent.Type.PROBABILISTIC));

        lcstrs.add(new InverseChanneling(vars, dualvars, s));

        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

        AbstractStrategy strategy = StrategyFactory.inputOrderIncDomain(vars, env);

        s.post(cstrs);
        s.set(strategy);
        return s;
    }


}
