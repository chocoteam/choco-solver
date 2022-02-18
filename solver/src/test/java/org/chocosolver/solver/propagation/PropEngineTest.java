/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.propagation;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.PropagatorPriority.UNARY;
import static org.chocosolver.solver.search.strategy.Search.minDomLBSearch;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.chocosolver.solver.variables.events.IEventType.ALL_EVENTS;
import static org.chocosolver.solver.variables.events.IntEventType.VOID;
import static org.chocosolver.util.ESat.TRUE;
import static org.chocosolver.util.ProblemMaker.makeNQueenWithBinaryConstraints;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/06/12
 */
public class PropEngineTest {

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Model model = new Model("t1");
        IntVar x = model.intVar("X", 1, 3, true);
        IntVar y = model.intVar("Y", 1, 3, true);
        model.arithm(x, ">=", y).post();
        model.arithm(x, "<=", 2).post();

        model.getSolver().solve();
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = SolverException.class)
    public void test2a() {
        Model model = new Model();
        IntVar[] VARS = model.intVarArray("X", 2, 0, 2, false);
        Constraint CSTR = model.arithm(VARS[0], "+", VARS[1], "=", 2);
        model.post(CSTR, CSTR);
        while (model.getSolver().solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void test2b() {
        Model model = new Model();
        IntVar[] VARS = model.intVarArray("X", 2, 0, 2, false);
        Constraint CSTR = model.arithm(VARS[0], "+", VARS[1], "=", 2);
        model.post(CSTR);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 3);
        model.getSolver().reset();
        model.unpost(CSTR);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 9);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2c() {
        Model model = new Model();
        IntVar[] VARS = model.intVarArray("X", 2, 0, 2, false);
        Constraint CSTR = model.arithm(VARS[0], "+", VARS[1], "=", 2);
        model.post(CSTR);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 3);
        model.getSolver().reset();
        model.unpost(CSTR);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 9);
    }

    public static void main(String[] args) {
        for(int i =1; i < 15; i++) {
            System.out.printf("%d -> %d \n", i, Integer.lowestOneBit(i));
            System.out.printf("%d -> %d \n", i, Integer.lowestOneBit(i>>2));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Model model = makeNQueenWithBinaryConstraints(8);
        model.getSolver().setEngine(new PropagationEngine(model));
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 92);
    }

    @Test(groups="10s", timeOut=60000)
    public void test5(){
        Model model = ProblemMaker.makeGolombRuler(10);
        model.getSolver().setEngine(new PropagationEngine(model));
        model.getSolver().setSearch(minDomLBSearch((IntVar[])model.getHook("ticks")));
        int obj = Integer.MAX_VALUE;
        while(model.getSolver().solve()){
            obj = ((IntVar)(model.getObjective())).getValue();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
        Assert.assertEquals(obj, 55);
    }

    @Test(groups="1s", timeOut=60000)
    public void testGregy41(){
        for(int i = 0 ; i < 20; i++) {
            Model model = new Model("Propagation condition");
            IntVar[] X = model.intVarArray("X", 2, 0, 2, false);
            new Constraint("test", new Propagator(X, UNARY, true) {

                @Override
                public int getPropagationConditions(int vIdx) {
                    if (vIdx == 0) {
                        return VOID.getMask();
                    } else {
                        return ALL_EVENTS;
                    }
                }

                @Override
                public void propagate(int evtmask) throws ContradictionException {
                    // initial propagation
                }

                @Override
                public void propagate(int idxVarInProp, int mask) throws ContradictionException {
                    if (idxVarInProp == 0) {
                        fail();
                    }
                }

                @Override
                public ESat isEntailed() {
                    return TRUE;
                }
            }).post();
            model.getSolver().setSearch(randomSearch(X, 0));
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 9);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG1() {
        Model model = new Model();
        Solver solver = model.getSolver();
        IntVar[] variables = model.intVarArray("s", 3, 0, 2);
        Constraint arithm = model.arithm(variables[0], "!=", variables[1]);

        model.post(arithm);
        solver.findAllSolutions();

        model.unpost(arithm);
        solver.reset(); // error (-1)
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG2(){
        Model model = new Model();
        Solver solver = model.getSolver();
        IntVar[] variables=model.intVarArray("s", 3, 0, 2);
        Constraint arithm = model.arithm(variables[0], "!=", variables[1]);

        model.post(arithm);
        solver.findAllSolutions();

        solver.getEngine().clear();
        solver.reset(); // error (null)
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG3(){
        Model model = new Model();
        Solver solver = model.getSolver();
        IntVar[] variables=model.intVarArray("s", 3, 0, 2);
        solver.findAllSolutions();

        solver.getEngine().clear();
        solver.reset(); // error (null)
    }
}
