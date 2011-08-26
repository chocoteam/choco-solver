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

package choco.constraints;

import choco.kernel.ESat;
import choco.kernel.common.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import samples.nqueen.NQueenDualGlobal;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.InverseChanneling;
import solver.exception.ContradictionException;
import solver.propagation.engines.IPropagationEngine;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30 sept. 2010
 */
public class InverseChannelingTest {

    public static void model(int n, int lb, int nbSol, int nbNod) {
        Solver s = new Solver();
        choco.kernel.memory.IEnvironment env = s.getEnvironment();

        IntVar[] x = new IntVar[n];
        IntVar[] y = new IntVar[n];
        for (int i = 0; i < n; i++) {
            x[i] = VariableFactory.enumerated("x" + i, lb, lb + n - 1, s);
            y[i] = VariableFactory.enumerated("y" + i, lb, lb + n - 1, s);
        }
        IntVar[] allvars = ArrayUtils.append(x, y);

        Constraint[] cstrs = new Constraint[1];
        cstrs[0] = new InverseChanneling(x, y, s);

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(allvars, env);

        s.post(cstrs);
        s.set(strategy);

        s.findSolution();
        do {
            Assert.assertEquals(cstrs[0].isSatisfied(), ESat.TRUE);
        } while (s.nextSolution() == Boolean.TRUE);
        Assert.assertEquals(s.getMeasures().getSolutionCount(), nbSol);
        Assert.assertEquals(s.getMeasures().getNodeCount(), nbNod);
    }

    @Test(groups = "1s")
    public void test() {
        model(5, 0, 120, 239);
        model(5, 2, 120, 239);
        model(5, -2, 120, 239);
    }

    @Test(groups = "1s")
    public void test1() {
        Solver s = new Solver();
        choco.kernel.memory.IEnvironment env = s.getEnvironment();

        IntVar[] x = new IntVar[2];
        IntVar[] y = new IntVar[2];
        for (int i = 0; i < 2; i++) {
            x[i] = VariableFactory.enumerated("x" + i, -1, 2, s);
            y[i] = VariableFactory.enumerated("y" + i, -1, 2, s);
        }
        IntVar[] allvars = ArrayUtils.append(x, y);

        Constraint[] cstrs = new Constraint[1];
        cstrs[0] = new InverseChanneling(x, y, s);

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(allvars, env);

        s.post(cstrs);
        s.set(strategy);

        s.findSolution();
        do {
            Assert.assertEquals(cstrs[0].isSatisfied(), ESat.TRUE);
        } while (s.nextSolution() == Boolean.TRUE);
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups = "1s")
    public void test2() {
        Solver s = new Solver();
        choco.kernel.memory.IEnvironment env = s.getEnvironment();

        IntVar[] x = new IntVar[2];
        IntVar[] y = new IntVar[2];
        for (int i = 0; i < 2; i++) {
            x[i] = VariableFactory.enumerated("x" + i, -2, 1, s);
            y[i] = VariableFactory.enumerated("y" + i, -2, 1, s);
        }
        IntVar[] allvars = ArrayUtils.append(x, y);

        Constraint[] cstrs = new Constraint[1];
        cstrs[0] = new InverseChanneling(x, y, s);

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(allvars, env);

        s.post(cstrs);
        s.set(strategy);

        s.findSolution();
        do {
            Assert.assertEquals(cstrs[0].isSatisfied(), ESat.TRUE);
        } while (s.nextSolution() == Boolean.TRUE);
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups = "1s")
    public void test3() {
        Solver s = new Solver();
        choco.kernel.memory.IEnvironment env = s.getEnvironment();

        IntVar[] x = new IntVar[2];
        IntVar[] y = new IntVar[2];
        for (int i = 0; i < 2; i++) {
            x[i] = VariableFactory.enumerated("x" + i, 0, 3, s);
            y[i] = VariableFactory.enumerated("y" + i, 0, 3, s);
        }
        IntVar[] allvars = ArrayUtils.append(x, y);

        Constraint[] cstrs = new Constraint[1];
        cstrs[0] = new InverseChanneling(x, y, s);

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(allvars, env);

        s.post(cstrs);
        s.set(strategy);

        s.findSolution();
        do {
            Assert.assertEquals(cstrs[0].isSatisfied(), ESat.TRUE);
        } while (s.nextSolution() == Boolean.TRUE);
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups = "1s")
    public void test4() {
        Solver s = new Solver();
        choco.kernel.memory.IEnvironment env = s.getEnvironment();

        IntVar[] x = new IntVar[2];
        IntVar[] y = new IntVar[2];
        x[0] = Views.fixed("x0", 1, s);
        x[1] = Views.fixed("x1", 2, s);

        y[0] = Views.fixed("y0", -1, s);
        y[1] = Views.fixed("y1", 0, s);

        IntVar[] allvars = ArrayUtils.append(x, y);

        Constraint[] cstrs = new Constraint[1];
        cstrs[0] = new InverseChanneling(x, y, s);

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(allvars, env);

        s.post(cstrs);
        s.set(strategy);

        s.findSolution();
        do {
            Assert.assertEquals(cstrs[0].isSatisfied(), ESat.TRUE);
        } while (s.nextSolution() == Boolean.TRUE);
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test5() {
        Solver s = new Solver();
        choco.kernel.memory.IEnvironment env = s.getEnvironment();

        IntVar[] x = new IntVar[4];
        IntVar[] y = new IntVar[4];
        x[0] = Views.fixed("x0", 2, s);
        x[1] = Views.fixed("x1", 4, s);
        x[2] = Views.fixed("x2", 1, s);
        x[3] = Views.fixed("x3", 3, s);

        for (int i = 0; i < 4; i++) {
            y[i] = VariableFactory.enumerated("y" + i, 1, 4, s);
        }

        IntVar[] allvars = ArrayUtils.append(x, y);

        Constraint[] cstrs = new Constraint[1];
        cstrs[0] = new InverseChanneling(x, y, s);

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(allvars, env);

        s.post(cstrs);
        s.set(strategy);

        s.findSolution();
        do {
            Assert.assertEquals(cstrs[0].isSatisfied(), ESat.TRUE);
        } while (s.nextSolution() == Boolean.TRUE);
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test6() throws ContradictionException {
        Solver s = new Solver();
        choco.kernel.memory.IEnvironment env = s.getEnvironment();

        IntVar[] x = new IntVar[5];
        IntVar[] y = new IntVar[5];
        x[0] = VariableFactory.enumerated("x0", new int[]{-10, -9, -7, -6}, s);
        x[1] = VariableFactory.enumerated("x1", new int[]{-9, -7}, s);
        x[2] = VariableFactory.enumerated("x2", new int[]{-9, -7}, s);
        x[3] = VariableFactory.enumerated("x3", new int[]{-10, -9, -6}, s);
        x[4] = VariableFactory.enumerated("x4", new int[]{-8}, s);

        y[0] = VariableFactory.enumerated("y0", new int[]{-10, -7}, s);
        y[1] = VariableFactory.enumerated("y1", new int[]{-10, -9, -8, -7}, s);
        y[2] = VariableFactory.enumerated("y2", new int[]{-6}, s);
        y[3] = VariableFactory.enumerated("y3", new int[]{-10, -9, -8}, s);
        y[4] = VariableFactory.enumerated("y4", new int[]{-10, -7}, s);

        IntVar[] allvars = ArrayUtils.append(x, y);

        Constraint[] cstrs = new Constraint[2];
        cstrs[0] = new InverseChanneling(x, y, s);
        cstrs[1] = new AllDifferent(x, s);

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(allvars, env);

        s.post(cstrs);
        s.set(strategy);

//        s.getSearchLoop().propEngine.propagate();
//        System.out.printf("%s\n", s);
        s.findSolution();
        do {
            Assert.assertEquals(cstrs[0].isSatisfied(), ESat.TRUE);
        } while (s.nextSolution() == Boolean.TRUE);
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void test7() {
        Solver s = new Solver();
        choco.kernel.memory.IEnvironment env = s.getEnvironment();

        IntVar[] x = new IntVar[4];
        IntVar[] y = new IntVar[4];

        x[0] = VariableFactory.enumerated("x0", 0, 3, s);
        x[1] = VariableFactory.enumerated("x1", 2, 3, s);
        x[2] = VariableFactory.enumerated("x2", 2, 3, s);
        x[3] = VariableFactory.enumerated("x3", 0, 1, s);

        y[0] = VariableFactory.enumerated("y0", 0, 3, s);
        y[1] = VariableFactory.enumerated("y1", 0, 3, s);
        y[2] = VariableFactory.enumerated("y2", 0, 3, s);
        y[3] = VariableFactory.enumerated("y2", 0, 3, s);

        IntVar[] allvars = ArrayUtils.append(x, y);

        Constraint[] cstrs = new Constraint[1];
        cstrs[0] = new InverseChanneling(x, y, s);

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(allvars, env);

        s.post(cstrs);
        s.set(strategy);

        try {
            s.getSearchLoop().propEngine.init();
            s.getSearchLoop().propEngine.initialPropagation();
            s.getSearchLoop().propEngine.fixPoint();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        System.err.printf("REF:\n%s\n", s.toString());
    }

    protected Solver modelDualGlobal(int size) {
        NQueenDualGlobal nq = new NQueenDualGlobal();
        nq.readArgs("-q", Integer.toString(size));
        nq.buildModel();
        nq.configureSolver();
        return nq.getSolver();
    }

    @Test(groups = "1s")
    public void testNode() throws ContradictionException {
        String first = testNode(modelDualGlobal(22));
        for (int i = 1; i < 3; i++) {
            Assert.assertEquals(testNode(modelDualGlobal(22)), first, "i=" + i);
        }
    }

    @Test(groups = "1s")
    public void testNode2() throws ContradictionException {
        String first = testNode2(modelDualGlobal(22));
        for (int i = 1; i < 3; i++) {
            Assert.assertEquals(testNode2(modelDualGlobal(22)), first, "i=" + i);
        }
    }

    private String testNode(Solver solver) throws ContradictionException {
        IPropagationEngine propagator = solver.getSearchLoop().propEngine;
        Variable[] vars = solver.getVars();
        List<IntVar> Q = new ArrayList<IntVar>(22);
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].getName().startsWith("Q_")) {
                Q.add((IntVar) vars[i]);
            }
        }
        propagator.init();
        propagator.initialPropagation();

        Q.get(0).instantiateTo(1, null);
        Q.get(1).instantiateTo(3, null);
        Q.get(2).instantiateTo(5, null);
        Q.get(3).instantiateTo(2, null);
        Q.get(4).instantiateTo(4, null);
        Q.get(5).instantiateTo(9, null);
        Q.get(6).instantiateTo(11, null);
        Q.get(7).instantiateTo(14, null);
        Q.get(8).instantiateTo(17, null);
        Q.get(9).instantiateTo(19, null);
        propagator.fixPoint();
        Q.get(10).instantiateTo(6, null);
        propagator.fixPoint();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < vars.length; i++) {
            IntVar var = (IntVar) vars[i];
            result.append(var.getName()).append('{');
            int ub = var.getUB();
            for (int val = var.getLB(); val <= ub; val = var.nextValue(val)) {
                result.append(val).append(",");
            }
            result.append("} ");
        }
        return result.toString();
    }

    private String testNode2(Solver solver) throws ContradictionException {
        IPropagationEngine propagator = solver.getSearchLoop().propEngine;
        Variable[] vars = solver.getVars();
        List<IntVar> Q = new ArrayList<IntVar>(22);
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].getName().startsWith("Q_")) {
                Q.add((IntVar) vars[i]);
            }
        }
        propagator.init();
        propagator.initialPropagation();

        Q.get(0).instantiateTo(1, null);
        Q.get(1).instantiateTo(3, null);
        Q.get(2).instantiateTo(5, null);
        Q.get(3).instantiateTo(2, null);
        Q.get(4).instantiateTo(4, null);
        Q.get(5).instantiateTo(9, null);
        Q.get(6).instantiateTo(11, null);
        Q.get(7).instantiateTo(21, null);
        Q.get(8).instantiateTo(19, null);
        Q.get(9).instantiateTo(16, null);
        Q.get(10).instantiateTo(22, null);
        Q.get(11).instantiateTo(20, null);
        Q.get(12).instantiateTo(18, null);
        Q.get(13).instantiateTo(7, null);
        propagator.fixPoint();
        Q.get(14).instantiateTo(10, null);
        propagator.fixPoint();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < vars.length; i++) {
            IntVar var = (IntVar) vars[i];
            result.append(var.getName()).append('{');
            int ub = var.getUB();
            for (int val = var.getLB(); val <= ub; val = var.nextValue(val)) {
                result.append(val).append(",");
            }
            result.append("} ");
        }
        return result.toString();
    }
}
