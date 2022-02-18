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

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.EnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.testng.Assert.assertFalse;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 sept. 2010
 */
public class DeltaTest {

    @Test(groups="1s", timeOut=60000)
    public void testAdd() {
        Model sol = new Model();
        EnumDelta d = new EnumDelta(sol.getEnvironment());
        for (int i = 1; i < 40; i++) {
            d.add(i, Cause.Null);
            Assert.assertEquals(d.size(), i);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testEq() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("X", 1, 6, false);
        IntVar y = model.intVar("Y", 1, 6, false);

        model.arithm(x, "=", y).post();

        model.getSolver().propagate();

        x.removeValue(4, Null);

        model.getSolver().propagate();

        assertFalse(y.contains(4));

    }

    @Test(groups="1s", timeOut=60000)
    public void testJL() {
        Model model = new Model();
        final SetVar s0 = model.setVar("s0", new int[]{}, new int[]{0, 1});
        final BoolVar b0 = model.boolVar("b0");
        final BoolVar b1 = model.boolVar("b1");
        final IntVar i0 = model.boolVar("i0");
        model.getSolver().setSearch(inputOrderLBSearch(i0));
        model.setBoolsChanneling(new BoolVar[]{b0, b1}, s0).post();
        s0.setCard(model.intVar(0));

        model.getSolver().solve();
        model.getSolver().reset();
        model.getSolver().solve();
    }


    @Test(groups="1s", timeOut=60000)
    public void testJL2() {
        for (int k = 0; k < 50; k++) {
            Model s = new Model();
            final IntVar i = s.intVar("i", -2, 2, false);
            final IntVar j = s.intVar("j", -2, 2, false);
            s.getSolver().setSearch(randomSearch(new IntVar[]{i, j}, 0));
            new Constraint("Constraint", new PropTestDM1(i, j), new PropTestDM2(i, j)).post();
            while (s.getSolver().solve()) ;
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL3() {
        for (int k = 0; k < 10; k++) {
            Model s = new Model();
            final IntVar i = s.intVar("i", -2, 2, true);
            final IntVar j = s.intVar("j", -2, 2, true);


            s.getSolver().setSearch(randomSearch(new IntVar[]{i, j}, 0));
            new Constraint("Constraint", new PropTestDM1(i, j), new PropTestDM2(i, j)).post();
            while (s.getSolver().solve()) ;
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL4() {
        for (int k = 0; k < 10; k++) {
            Model s = new Model();
            final IntVar i = s.boolVar("i");
            final IntVar j = s.boolVar("j");


            s.getSolver().setSearch(randomSearch(new IntVar[]{i, j}, 0));
            new Constraint("Constraint", new PropTestDM1(i, j), new PropTestDM2(i, j)).post();
            while (s.getSolver().solve()) ;
        }
    }

    private static class PropTestDM1 extends Propagator<IntVar> {
        IntVar i, j;
        IIntDeltaMonitor iD;
        IIntDeltaMonitor jD;

        private PropTestDM1(IntVar i, IntVar j) {
            super(new IntVar[]{i, j}, PropagatorPriority.UNARY, true);
            this.i = i;
            this.j = j;
            iD = i.monitorDelta(this);
            jD = j.monitorDelta(this);
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            iD.startMonitoring();
            jD.startMonitoring();
        }

        @Override
        public void propagate(int idxVarInProp, int mask) throws ContradictionException {
            if (idxVarInProp == 0) {
                iD.forEachRemVal((IntProcedure) x -> {
                    if (i.contains(x)) {
                        Assert.fail();
                    }
                });
            } else {
                jD.forEachRemVal((IntProcedure) x -> {
                    if (j.contains(x)) {
                        Assert.fail();
                    }
                });
            }
        }

        @Override
        public ESat isEntailed() {
            return ESat.TRUE;
        }
    }

    private static class PropTestDM2 extends Propagator<IntVar> {
        IntVar i, j;

        private PropTestDM2(IntVar i, IntVar j) {
            super(new IntVar[]{i, j}, PropagatorPriority.UNARY, false);
            this.i = i;
            this.j = j;
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            if (j.isInstantiatedTo(1)) {
                i.removeValue(1, this);
            }
        }

        @Override
        public ESat isEntailed() {
            return ESat.TRUE;
        }

    }
}
