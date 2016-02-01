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
package org.chocosolver.solver;

import org.chocosolver.memory.Environments;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.ESat;
import org.chocosolver.util.ProblemMaker;
import org.chocosolver.util.criteria.Criterion;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.MessageFormat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 juil. 2010
 */
public class SolverTest {

    final static int[] capacites = {0, 34};
    final static int[] energies = {6, 4, 3};
    final static int[] volumes = {7, 5, 2};
    final static int[] nbOmax = {4, 6, 17};
    final static int n = 3;

    public static Solver knapsack(boolean copy) {
        Solver s;
        if(copy){
            s = new Solver(Environments.COPY.make(), "test");
        }else{
            s = new Solver();
        }
        IntVar power = VariableFactory.bounded("v_" + n, 0, 9999, s);
        IntVar[] objects = new IntVar[n];
        for (int i = 0; i < n; i++) {
            objects[i] = VariableFactory.enumerated("v_" + i, 0, nbOmax[i], s);
        }
        s.post(IntConstraintFactory.scalar(objects, volumes, "=", VariableFactory.bounded("capa", capacites[0], capacites[1], s)));
        s.post(IntConstraintFactory.scalar(objects, energies, "=", power));
        s.setObjectives(power);
        s.set(IntStrategyFactory.lexico_LB(objects));
        Chatterbox.showShortStatistics(s);
        return s;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final int ONE = 0, NEXT = 1, ALL = 2, OPT = 3;

    public static void conf(Solver s, int... is) throws SolverException {
        for (int i : is) {
            switch (i) {
                case ONE:
                    s.findSolution();
                    break;
                case NEXT:
                    s.nextSolution();
                    break;
                case ALL:
                    s.findAllSolutions();
                    break;
                case OPT:
                    s.findOptimalSolution(ResolutionPolicy.MAXIMIZE, (IntVar) s.getVar(0));
                    break;
                default:
                    Assert.fail("unknonw case");
                    break;
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testRight() {
        tr2(true);
        tr2(false);
    }

    private void tr2(boolean copy){
        boolean alive = true;
        int cas = 0;
        while (alive) {
            cas++;
            Solver s = knapsack(copy);
            try {
                switch (cas) {
                    case 1:
                        conf(s, ONE);
                        break;
                    case 2:
                        conf(s, ONE, NEXT);
                        break;
                    case 3:
                        conf(s, ONE, NEXT, NEXT);
                        break;
                    case 4:
                        conf(s, ONE, ONE);
                        break;
                    case 5:
                        conf(s, ONE, ALL);
                        break;
                    case 6:
                        conf(s, ONE, OPT);
                        break;
                    case 7:
                        conf(s, ALL);
                        break;
                    case 8:
                        conf(s, OPT);
                        break;
                    case 9:
                        conf(s, ALL, ONE);
                        break;
                    case 10:
                        conf(s, ALL, ALL);
                        break;
                    case 11:
                        conf(s, ALL, OPT);
                        break;
                    case 12:
                        conf(s, ALL, NEXT);
                        break;
                    case 13:
                        conf(s, OPT, ONE);
                        break;
                    case 14:
                        conf(s, OPT, ALL);
                        break;
                    case 15:
                        conf(s, OPT, OPT);
                        break;
                    case 16:
                        conf(s, OPT, NEXT);
                        break;
                    case 17:
                        conf(s, NEXT);
                        break;
                    default:
                        alive = false;

                }
            } catch (SolverException ingored) {
                Assert.fail(MessageFormat.format("Fail on {0}", cas));
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testFH1() {
        Solver solver = new Solver();
        BoolVar b = VF.bool("b", solver);
        IntVar i = VF.bounded("i", VF.MIN_INT_BOUND, VF.MAX_INT_BOUND, solver);
        SetVar s = VF.set("s", 2, 3, solver);
        RealVar r = VF.real("r", 1.0, 2.2, 0.01, solver);

        BoolVar[] bvars = solver.retrieveBoolVars();
        Assert.assertEquals(bvars, new BoolVar[]{b});

        IntVar[] ivars = solver.retrieveIntVars(false);
        Assert.assertEquals(ivars, new IntVar[]{i});

        SetVar[] svars = solver.retrieveSetVars();
        Assert.assertEquals(svars, new SetVar[]{s});

        RealVar[] rvars = solver.retrieveRealVars();
        Assert.assertEquals(rvars, new RealVar[]{r});

    }


    @Test(groups="1s", timeOut=60000)
    public void testRetrieveInt() {
        Solver solver = new Solver();
        BoolVar b = VF.bool("b", solver);
        IntVar i = VF.enumerated("i", 1, 3, solver);
        IntVar[] is = solver.retrieveIntVars(false);
        Assert.assertEquals(1, is.length);
        IntVar[] is2 = solver.retrieveIntVars(true);
        Assert.assertEquals(2, is2.length);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRetrieveBool() {
        Solver solver = new Solver();
        BoolVar b = VF.bool("b", solver);
        IntVar i = VF.enumerated("i", 1, 3, solver);
        IntVar[] bs = solver.retrieveBoolVars();
        Assert.assertEquals(1, bs.length);
    }

    @Test(groups="1s", timeOut=60000)
    public void testFH2() {
        Solver solver = new Solver();
        BoolVar b = VF.bool("b", solver);
        solver.post(ICF.arithm(b, "=", 2));
        solver.findAllSolutions();
        Assert.assertEquals(solver.isFeasible(), ESat.FALSE);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL1() {
        Solver s = new Solver();
        s.post(ICF.arithm(s.ONE(), "!=", s.ZERO()));
        if (s.findSolution()) {
            while (s.nextSolution()) ;
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testP1() {
        ParallelResolution pares = new ParallelResolution();
        int n = 4; // number of solvers to use
        for (int i = 0; i < n; i++) {
            pares.addSolver(knapsack(true));
            pares.addSolver(knapsack(false));
        }
        pares.findSolution();
        Chatterbox.printSolutions(pares.getFinder());
        Assert.assertEquals(pares.getFinder().getMeasures().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testP2() {
        // this test fails presumably because a solver stops before another finishes initialisation
        for(int iter=0;iter<50;iter++) {
            ParallelResolution pares = new ParallelResolution();
            for (int i = 0; i < 10; i++) {
                pares.addSolver(knapsack(true));
                pares.addSolver(knapsack(false));
            }
            pares.findOptimalSolution(ResolutionPolicy.MAXIMIZE);
            Chatterbox.printSolutions(pares.getFinder());
            Assert.assertEquals(pares.getFinder().getObjectiveManager().getBestSolutionValue(), 51);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL300(){
        Solver s = new Solver();
        IntVar i = VF.enumerated("i", -5, 5, s);
        s.findOptimalSolution(ResolutionPolicy.MAXIMIZE, i);
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(s.getSolutionRecorder().getLastSolution().getIntVal(i).intValue(), 5);

        s.getEngine().flush();
        s.getSearchLoop().reset();

        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 11);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMonitors(){
        Solver solver = new Solver();
        IntVar v = VF.bool("b", solver);
        final int[] c = {0};
        final int[] d = {0};
        IMonitorSolution sm1 = () -> c[0]++;
        IMonitorSolution sm2 = () -> d[0]++;
        solver.plugMonitor(sm1);
        solver.plugMonitor(sm2);
        solver.findAllSolutions();
        Assert.assertEquals(2, c[0]);
        Assert.assertEquals(2, d[0]);
        // unplug
        solver.unplugMonitor(sm1);
        solver.search.reset();
        solver.findAllSolutions();
        Assert.assertEquals(2, c[0]);
        Assert.assertEquals(4, d[0]);
        // plug
        solver.unplugAllMonitors();
        solver.search.reset();
        solver.findAllSolutions();
        Assert.assertEquals(2, c[0]);
        Assert.assertEquals(4, d[0]);
    }

    @Test(groups="1s", timeOut=60000)
    public void testCriteria(){
        Solver solver = new Solver();
        IntVar v = VF.bool("b", solver);
        Criterion c1 = () -> solver.getMeasures().getNodeCount() == 1;
        Criterion c2 = () -> solver.getMeasures().getSolutionCount() == 1;
        solver.addStopCriterion(c1);
        solver.addStopCriterion(c2);
        solver.findAllSolutions();
        Assert.assertEquals(0, solver.getMeasures().getSolutionCount());
        // unplug
        solver.removeStopCriterion(c1);
        solver.search.reset();
        solver.findAllSolutions();
        Assert.assertEquals(1, solver.getMeasures().getSolutionCount());
        // plug
        solver.removeAllStopCriteria();
        solver.search.reset();
        solver.findAllSolutions();
        Assert.assertEquals(2, solver.getMeasures().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testCompSearch(){
        Solver solver = new Solver();
        IntVar[] v = VF.boolArray("v", 2, solver);
        IntVar[] w = VF.boolArray("w", 2, solver);
        solver.post(ICF.arithm(v[0], "!=", v[1]));
        solver.post(ICF.arithm(w[0], "!=", w[1]));
        solver.set(ISF.lexico_LB(v));
        solver.makeCompleteSearch(true);
        solver.findSolution();
        Assert.assertEquals(solver.isSatisfied(),ESat.TRUE);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAssociates(){
        Solver s = new Solver();
        BoolVar v = VF.bool("V", s);
        Assert.assertEquals(s.getNbVars(), 1);
        s.associates(v);
        Assert.assertEquals(s.getNbVars(), 2);
        s.unassociates(v);
        Assert.assertEquals(s.getNbVars(), 1);
        s.unassociates(v);
        Assert.assertEquals(s.getNbVars(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRestore() throws ContradictionException {
        Solver solver = new Solver();
        IntVar[] v = VF.boolArray("v", 2, solver);
        solver.post(ICF.arithm(v[0], "!=", v[1]));
        solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, v[0]);
        solver.restoreLastSolution();
        Assert.assertTrue(v[0].isInstantiated());
        Assert.assertTrue(v[0].isInstantiatedTo(1));
    }

    @Test(groups="1s", timeOut=60000)
    public void testHook(){
        Solver solver = new Solver();
        String toto = "TOTO";
        String titi = "TITI";
        solver.addHook("toto", toto);
        solver.addHook("titi", titi);
        Assert.assertEquals(solver.getHooks().size(), 2);
        Assert.assertEquals(solver.getHook("toto"), toto);
        solver.removeHook("toto");
        Assert.assertEquals(solver.getHook("toto"), null);
        Assert.assertEquals(solver.getHooks().size(), 1);
        solver.removeAllHooks();
        Assert.assertEquals(solver.getHooks().size(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testName(){
        Solver solver = new Solver();
        Assert.assertTrue(solver.getName().startsWith("Solver-"));
        solver.setName("Revlos");
        Assert.assertEquals(solver.getName(), "Revlos");
    }

    @Test(groups="1s", timeOut=60000)
    public void testNextSolution(){
        Solver s = ProblemMaker.makeNQueenWithBinaryConstraints(8);
        s.nextSolution(); //  should not throw exception
    }
}
