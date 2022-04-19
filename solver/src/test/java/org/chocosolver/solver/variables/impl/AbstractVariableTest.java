/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.alldifferent.PropAllDiffInst;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 25/10/2016.
 */
public class AbstractVariableTest {

    Model model;
    AbstractVariable v;
    Propagator[] props;


    @BeforeMethod(alwaysRun = true)
    public void setup() {
        model = new Model();
        IntVar[] is = model.intVarArray(5, 0, 6);
        v = (AbstractVariable) is[0];
        props = new Propagator[5];
        props[0] = new PropAllDiffInst(new IntVar[]{is[0]});
        props[1] = new PropAllDiffInst(new IntVar[]{is[1], is[0]});
        props[2] = new PropAllDiffInst(new IntVar[]{is[2], is[1], is[0]});
        props[3] = new PropAllDiffInst(new IntVar[]{is[3], is[2], is[1], is[0]});
        props[4] = new PropAllDiffInst(new IntVar[]{is[4], is[3], is[2], is[1], is[0]});
    }


    @Test(groups = "1s")
    public void testFixed() {
        Model model = new Model();
        IntVar var = model.intVar(0, 10);
        IntVar fix = model.intVar(10);
        IVariableMonitor<IntVar> mon = (v, e) -> {
        };
        var.addMonitor(mon);
        fix.addMonitor(mon);
        var.removeMonitor(mon);
        fix.removeMonitor(mon);
    }

    @Test(groups = "1s")
    public void testWI() throws ContradictionException {
        Model model = new Model();
        IntVar var = model.intVar(0, 10);
        IntVar fix = model.intVar(10);
        Assert.assertEquals(var.instantiationWorldIndex(), Integer.MAX_VALUE);
        Assert.assertEquals(fix.instantiationWorldIndex(), 0);
        model.getEnvironment().worldPush();
        var.instantiateTo(2, Cause.Null);
        Assert.assertEquals(var.instantiationWorldIndex(), 1);
        Assert.assertEquals(fix.instantiationWorldIndex(), 0);
        model.getEnvironment().worldPop();
        Assert.assertEquals(var.instantiationWorldIndex(), Integer.MAX_VALUE);
        Assert.assertEquals(fix.instantiationWorldIndex(), 0);
    }

    @Test(groups = "1s")
    public void testAddMonitor() throws ContradictionException {
        Model model = new Model();
        IntVar v1 = model.intVar(0, 10);
        final AtomicInteger score = new AtomicInteger(0);
        IVariableMonitor<IntVar> m1 = (var, evt) -> score.addAndGet(1);
        IVariableMonitor<IntVar> m2 = (var, evt) -> score.addAndGet(3);
        v1.addMonitor(m1);
        v1.addMonitor(m2);
        v1.notifyMonitors(IntEventType.VOID);
        // 2 monitors added as they differ.
        Assert.assertEquals(score.get(), 4);

        // Redundancy checker disabled, we expect all monitors to be updated.
        score.set(0);
        v1 = model.intVar(0, 20);
        model.getSettings().setCheckDeclaredMonitors(false);
        v1.addMonitor(m1);
        v1.addMonitor(m1);
        v1.addMonitor(m2);
        v1.notifyMonitors(IntEventType.VOID);
        Assert.assertEquals(score.get(), 5);

        // Redundancy checker enabled, we expect only 1 m1 and the m2.
        score.set(0);
        v1 = model.intVar(0, 20);
        model.getSettings().setCheckDeclaredMonitors(true);
        v1.addMonitor(m1);
        v1.addMonitor(m2);
        v1.addMonitor(m1);
        v1.notifyMonitors(IntEventType.VOID);
        Assert.assertEquals(score.get(), 4);
    }

    private static class ProTester extends Propagator<IntVar> {

        final IIntDeltaMonitor monitor;
        final IntProcedure procedure;
        final int value;
        final boolean activate;

        public ProTester(IntVar var, IntProcedure procedure, int value, boolean activate) {
            super(new IntVar[]{var}, PropagatorPriority.VERY_SLOW, true);
            this.monitor = var.monitorDelta(this);
            this.procedure = procedure;
            this.value = value;
            this.activate = activate;
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            if (activate) monitor.startMonitoring();
            vars[0].removeValue(value, this);
        }

        @Override
        public void propagate(int idxVarInProp, int mask) throws ContradictionException {
            monitor.forEachRemVal(procedure);
        }

        @Override
        public ESat isEntailed() {
            return ESat.TRUE;
        }
    }

    @Test(groups = "1s", expectedExceptions = SolverException.class)
    public void testDeltaMonitor1() {
        final AtomicInteger score = new AtomicInteger(0);
        Model model = new Model();
        IntVar x = model.intVar(1, 4);
        model.arithm(x, "!=", 1).post();
        new Constraint("tester", new ProTester(x, score::addAndGet, 2, false)).post();
        new Constraint("tester", new ProTester(x, i -> { /* nothing */}, 3, false)).post();
        model.getSolver().solve();
    }

    @Test(groups = "1s")
    public void testDeltaMonitor2() {
        final AtomicInteger score = new AtomicInteger(0);
        Model model = new Model();
        IntVar x = model.intVar(1, 4);
        model.arithm(x, "!=", 1).post();
        new Constraint("tester", new ProTester(x, score::addAndGet, 2, true)).post();
        new Constraint("tester", new ProTester(x, i -> { /* nothing */}, 3, false)).post();
        model.getSolver().solve();
        Assert.assertEquals(score.get(), 3);
    }

    private static class PropFake extends Propagator<IntVar> {

        private final int idx;

        public PropFake(IntVar var, int i) {
            super(new IntVar[]{var});
            this.idx = i;
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            // void
        }

        @Override
        public ESat isEntailed() {
            return ESat.UNDEFINED;
        }

        @Override
        public String toString() {
            return "PropFake{" +
                    "idx=" + idx +
                    '}';
        }
    }

    @Test(groups = "1s")
    public void testSwapStd() {
        Model model = new Model();
        IEnvironment env = model.getEnvironment();
        IntVar v = model.intVar("x", 1, 3);
        AbstractVariable.BipartiteList list = ((AbstractVariable) v).propagators[4];
        PropFake p0 = new PropFake(v, 0);
        PropFake p1 = new PropFake(v, 1);
        PropFake p2 = new PropFake(v, 2);
        v.link(p0, 0);
        v.link(p1, 0);
        v.link(p2, 0);
        //
        env.worldPush();
        v.swapOnPassivate(p2, 0);
        v.swapOnPassivate(p0, 0);
        Assert.assertEquals(list.first, 0);
        Assert.assertEquals(list.last, 3);
        Assert.assertEquals(list.stream().toArray(Propagator[]::new),
                new Propagator[]{p1});
        env.worldPop();
        Assert.assertEquals(list.first, 0);
        Assert.assertEquals(list.last, 3);
        Assert.assertEquals(list.stream().toArray(Propagator[]::new),
                new Propagator[]{p2, p0, p1});
    }

    @Test(groups = "1s", dependsOnMethods = "testSwapStd")
    public void testSwapDyn1() {
        Model model = new Model();
        IEnvironment env = model.getEnvironment();
        IntVar v = model.intVar("x", 1, 3);
        AbstractVariable.BipartiteList list = ((AbstractVariable) v).propagators[4];
        PropFake p0 = new PropFake(v, 0);
        PropFake p1 = new PropFake(v, 1);
        PropFake p2 = new PropFake(v, 2);
        v.link(p0, 0);
        v.link(p1, 0);
        v.link(p2, 0);
        //
        env.worldPush();
        PropFake p3 = new PropFake(v, 3);
        v.link(p3, 0);
        Assert.assertEquals(list.first, 0);
        Assert.assertEquals(list.last, 4);
        Assert.assertEquals(list.splitter.get(), 0);
        Assert.assertEquals(list.stream().toArray(Propagator[]::new),
                new Propagator[]{p0, p1, p2, p3});
        v.swapOnPassivate(p3, 0);
        Assert.assertEquals(list.first, 0);
        Assert.assertEquals(list.last, 4);
        Assert.assertEquals(list.splitter.get(), 1);
        Assert.assertEquals(list.stream().toArray(Propagator[]::new),
                new Propagator[]{p1, p2, p0});
        env.worldPop();
        Assert.assertEquals(list.first, 0);
        Assert.assertEquals(list.last, 4);
        Assert.assertEquals(list.splitter.get(), 0);
        Assert.assertEquals(list.stream().toArray(Propagator[]::new),
                new Propagator[]{p3, p1, p2, p0});
    }

    @Test(groups = "1s", dependsOnMethods = {"testSwapStd"})
    public void testSwapDyn2() {
        Model model = new Model();
        IEnvironment env = model.getEnvironment();
        IntVar v = model.intVar("x", 1, 3);
        AbstractVariable.BipartiteList list = ((AbstractVariable) v).propagators[4];
        PropFake p0 = new PropFake(v, 0);
        PropFake p1 = new PropFake(v, 1);
        PropFake p2 = new PropFake(v, 2);
        v.link(p0, 0);
        v.link(p1, 0);
        v.link(p2, 0);
        //
        env.worldPush();
        PropFake p3 = new PropFake(v, 3);
        v.link(p3, 0);
        env.save(() -> v.unlink(p3, 0));
        v.swapOnPassivate(p3, 0);
        env.worldPop();
        Assert.assertEquals(list.first, 0);
        Assert.assertEquals(list.last, 3);
        Assert.assertEquals(list.splitter.get(), 0);
        Assert.assertEquals(list.stream().toArray(Propagator[]::new),
                new Propagator[]{p0, p1, p2});
    }

    @Test(groups = "1s", dependsOnMethods = {"testSwapStd"})
    public void testSwapDyn3() {
        Model model = new Model();
        IEnvironment env = model.getEnvironment();
        IntVar v = model.intVar("x", 1, 3);
        AbstractVariable.BipartiteList list = ((AbstractVariable) v).propagators[4];
        PropFake p0 = new PropFake(v, 0);
        PropFake p1 = new PropFake(v, 1);
        PropFake p2 = new PropFake(v, 2);
        v.link(p0, 0);
        v.link(p1, 0);
        v.link(p2, 0);
        //
        env.worldPush();
        PropFake p3 = new PropFake(v, 3);
        v.link(p3, 0);
        env.save(() -> v.unlink(p3, 0));
        v.swapOnPassivate(p3, 0);
        env.worldPop();
        env.worldPush();
        PropFake p4 = new PropFake(v, 4);
        v.link(p4, 0);
        env.save(() -> v.unlink(p4, 0));
        v.swapOnPassivate(p4, 0);
        env.worldPop();
        Assert.assertEquals(list.first, 0);
        Assert.assertEquals(list.last, 3);
        Assert.assertEquals(list.splitter.get(), 0);
        Assert.assertEquals(list.stream().toArray(Propagator[]::new),
                new Propagator[]{p0, p1, p2});
    }

    @Test(groups = "1s", dependsOnMethods = {"testSwapStd"})
    public void testSwapDyn4() {
        Model model = new Model();
        IEnvironment env = model.getEnvironment();
        IntVar v = model.intVar("x", 1, 3);
        AbstractVariable.BipartiteList list = ((AbstractVariable) v).propagators[4];
        PropFake p0 = new PropFake(v, 0);
        PropFake p1 = new PropFake(v, 1);
        PropFake p2 = new PropFake(v, 2);
        v.link(p0, 0);
        v.link(p1, 0);
        v.link(p2, 0);
        //
        env.worldPush();
        PropFake p3 = new PropFake(v, 3);
        v.link(p3, 0);
        env.save(() -> v.unlink(p3, 0));
        v.swapOnPassivate(p3, 0);
        PropFake p4 = new PropFake(v, 4);
        v.link(p4, 0);
        env.save(() -> v.unlink(p4, 0));
        Assert.assertEquals(list.first, 0);
        Assert.assertEquals(list.last, 5);
        Assert.assertEquals(list.splitter.get(), 1);
        Assert.assertEquals(list.stream().toArray(Propagator[]::new),
                new Propagator[]{p1, p2, p0, p4});
        env.worldPop();
        Assert.assertEquals(list.first, 0);
        Assert.assertEquals(list.last, 3);
        Assert.assertEquals(list.splitter.get(), 0);
        Assert.assertEquals(list.stream().toArray(Propagator[]::new),
                new Propagator[]{p0, p1, p2});
    }
}