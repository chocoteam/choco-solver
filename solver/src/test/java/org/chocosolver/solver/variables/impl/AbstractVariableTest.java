/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.alldifferent.PropAllDiffInst;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
    public void setup(){
        model = new Model();
        IntVar[] is =  model.intVarArray(5, 0,6);
        v = (AbstractVariable)is[0];
        props = new Propagator[5];
        props[0] = new PropAllDiffInst(new IntVar[]{is[0]});
        props[1] = new PropAllDiffInst(new IntVar[]{is[1], is[0]});
        props[2] = new PropAllDiffInst(new IntVar[]{is[2], is[1], is[0]});
        props[3] = new PropAllDiffInst(new IntVar[]{is[3], is[2], is[1], is[0]});
        props[4] = new PropAllDiffInst(new IntVar[]{is[4], is[3], is[2], is[1], is[0]});
    }


    @Test(groups = "1s")
    public void testSubscribe1() throws Exception {
        props[0].setVIndices(0,v.subscribe(props[0], 0, 4));
        Assert.assertEquals(v.propagators[0], props[0]);
        Assert.assertEquals(v.pindices[0], 0);
        Assert.assertEquals(props[0].getVIndices(), new int[]{0});

        props[1].setVIndices(1, v.subscribe(props[1], 1, 4));
        Assert.assertEquals(v.propagators[0], props[1]);
        Assert.assertEquals(v.pindices[0], 1);
        Assert.assertEquals(v.propagators[1], props[0]);
        Assert.assertEquals(v.pindices[1], 0);
        Assert.assertEquals(props[0].getVIndices(), new int[]{1});
        Assert.assertEquals(props[1].getVIndices(), new int[]{-1, 0});


        props[2].setVIndices(2, v.subscribe(props[2], 2, 4));
        Assert.assertEquals(v.propagators[0], props[2]);
        Assert.assertEquals(v.pindices[0], 2);
        Assert.assertEquals(v.propagators[1], props[0]);
        Assert.assertEquals(v.pindices[1], 0);
        Assert.assertEquals(v.propagators[2], props[1]);
        Assert.assertEquals(v.pindices[2], 1);
        Assert.assertEquals(props[0].getVIndices(), new int[]{1});
        Assert.assertEquals(props[1].getVIndices(), new int[]{-1, 2});
        Assert.assertEquals(props[2].getVIndices(), new int[]{-1, -1, 0});

        props[3].setVIndices(3, v.subscribe(props[3], 3, 4));
        Assert.assertEquals(v.propagators[0], props[3]);
        Assert.assertEquals(v.pindices[0], 3);
        Assert.assertEquals(v.propagators[1], props[0]);
        Assert.assertEquals(v.pindices[1], 0);
        Assert.assertEquals(v.propagators[2], props[1]);
        Assert.assertEquals(v.pindices[2], 1);
        Assert.assertEquals(v.propagators[3], props[2]);
        Assert.assertEquals(v.pindices[3], 2);
        Assert.assertEquals(props[0].getVIndices(), new int[]{1});
        Assert.assertEquals(props[1].getVIndices(), new int[]{-1, 2});
        Assert.assertEquals(props[2].getVIndices(), new int[]{-1, -1, 3});
        Assert.assertEquals(props[3].getVIndices(), new int[]{-1, -1, -1, 0});

        props[4].setVIndices(4, v.subscribe(props[4], 4, 4));
        Assert.assertEquals(v.propagators[0], props[4]);
        Assert.assertEquals(v.pindices[0], 4);
        Assert.assertEquals(v.propagators[1], props[0]);
        Assert.assertEquals(v.pindices[1], 0);
        Assert.assertEquals(v.propagators[2], props[1]);
        Assert.assertEquals(v.pindices[2], 1);
        Assert.assertEquals(v.propagators[3], props[2]);
        Assert.assertEquals(v.pindices[3], 2);
        Assert.assertEquals(v.propagators[4], props[3]);
        Assert.assertEquals(v.pindices[4], 3);
        Assert.assertEquals(props[0].getVIndices(), new int[]{1});
        Assert.assertEquals(props[1].getVIndices(), new int[]{-1, 2});
        Assert.assertEquals(props[2].getVIndices(), new int[]{-1, -1, 3});
        Assert.assertEquals(props[3].getVIndices(), new int[]{-1, -1, -1, 4});
        Assert.assertEquals(props[4].getVIndices(), new int[]{-1, -1, -1, -1, 0});
    }

    @Test(groups = "1s")
    public void testSubscribe2() throws Exception {
        props[0].setVIndices(0,v.subscribe(props[0], 0, 4));
        Assert.assertEquals(v.propagators[0], props[0]);
        Assert.assertEquals(v.pindices[0], 0);
        Assert.assertEquals(props[0].getVIndices(), new int[]{0});

        props[1].setVIndices(1, v.subscribe(props[1], 1, 0));
        Assert.assertEquals(v.propagators[0], props[1]);
        Assert.assertEquals(v.pindices[0], 1);
        Assert.assertEquals(v.propagators[1], props[0]);
        Assert.assertEquals(v.pindices[1], 0);
        Assert.assertEquals(props[0].getVIndices(), new int[]{1});
        Assert.assertEquals(props[1].getVIndices(), new int[]{-1, 0});


        props[2].setVIndices(2, v.subscribe(props[2], 2, 0));
        Assert.assertEquals(v.propagators[0], props[2]);
        Assert.assertEquals(v.pindices[0], 2);
        Assert.assertEquals(v.propagators[1], props[1]);
        Assert.assertEquals(v.pindices[1], 1);
        Assert.assertEquals(v.propagators[2], props[0]);
        Assert.assertEquals(v.pindices[2], 0);
        Assert.assertEquals(props[0].getVIndices(), new int[]{2});
        Assert.assertEquals(props[1].getVIndices(), new int[]{-1, 1});
        Assert.assertEquals(props[2].getVIndices(), new int[]{-1, -1, 0});

        props[3].setVIndices(3, v.subscribe(props[3], 3, 0));
        Assert.assertEquals(v.propagators[0], props[3]);
        Assert.assertEquals(v.pindices[0], 3);
        Assert.assertEquals(v.propagators[1], props[1]);
        Assert.assertEquals(v.pindices[1], 1);
        Assert.assertEquals(v.propagators[2], props[2]);
        Assert.assertEquals(v.pindices[2], 2);
        Assert.assertEquals(v.propagators[3], props[0]);
        Assert.assertEquals(v.pindices[3], 0);
        Assert.assertEquals(props[0].getVIndices(), new int[]{3});
        Assert.assertEquals(props[1].getVIndices(), new int[]{-1, 1});
        Assert.assertEquals(props[2].getVIndices(), new int[]{-1, -1, 2});
        Assert.assertEquals(props[3].getVIndices(), new int[]{-1, -1, -1, 0});

        props[4].setVIndices(4, v.subscribe(props[4], 4, 0));
        Assert.assertEquals(v.propagators[0], props[4]);
        Assert.assertEquals(v.pindices[0], 4);
        Assert.assertEquals(v.propagators[1], props[1]);
        Assert.assertEquals(v.pindices[1], 1);
        Assert.assertEquals(v.propagators[2], props[2]);
        Assert.assertEquals(v.pindices[2], 2);
        Assert.assertEquals(v.propagators[3], props[3]);
        Assert.assertEquals(v.pindices[3], 3);
        Assert.assertEquals(v.propagators[4], props[0]);
        Assert.assertEquals(v.pindices[4], 0);
        Assert.assertEquals(props[0].getVIndices(), new int[]{4});
        Assert.assertEquals(props[1].getVIndices(), new int[]{-1, 1});
        Assert.assertEquals(props[2].getVIndices(), new int[]{-1, -1, 2});
        Assert.assertEquals(props[3].getVIndices(), new int[]{-1, -1, -1, 3});
        Assert.assertEquals(props[4].getVIndices(), new int[]{-1, -1, -1, -1, 0});
    }


    @Test(groups = "1s")
    public void testCancel1() throws Exception {
        props[0].setVIndices(0,v.subscribe(props[0], 0, 4));
        props[1].setVIndices(1, v.subscribe(props[1], 1, 4));
        props[2].setVIndices(2, v.subscribe(props[2], 2, 4));
        props[3].setVIndices(3, v.subscribe(props[3], 3, 4));
        props[4].setVIndices(4, v.subscribe(props[4], 4, 4));

        Assert.assertEquals(v.propagators[0], props[4]);
        Assert.assertEquals(v.pindices[0], 4);
        Assert.assertEquals(v.propagators[1], props[0]);
        Assert.assertEquals(v.pindices[1], 0);
        Assert.assertEquals(v.propagators[2], props[1]);
        Assert.assertEquals(v.pindices[2], 1);
        Assert.assertEquals(v.propagators[3], props[2]);
        Assert.assertEquals(v.pindices[3], 2);
        Assert.assertEquals(v.propagators[4], props[3]);
        Assert.assertEquals(v.pindices[4], 3);
        Assert.assertEquals(props[0].getVIndices(), new int[]{1});
        Assert.assertEquals(props[1].getVIndices(), new int[]{-1, 2});
        Assert.assertEquals(props[2].getVIndices(), new int[]{-1, -1, 3});
        Assert.assertEquals(props[3].getVIndices(), new int[]{-1, -1, -1, 4});
        Assert.assertEquals(props[4].getVIndices(), new int[]{-1, -1, -1, -1, 0});

        v.cancel(0, 4);
        Assert.assertEquals(v.propagators[0], props[3]);
        Assert.assertEquals(v.pindices[0], 3);
        Assert.assertEquals(v.propagators[1], props[0]);
        Assert.assertEquals(v.pindices[1], 0);
        Assert.assertEquals(v.propagators[2], props[1]);
        Assert.assertEquals(v.pindices[2], 1);
        Assert.assertEquals(v.propagators[3], props[2]);
        Assert.assertEquals(v.pindices[3], 2);
        Assert.assertEquals(props[0].getVIndices(), new int[]{1});
        Assert.assertEquals(props[1].getVIndices(), new int[]{-1, 2});
        Assert.assertEquals(props[2].getVIndices(), new int[]{-1, -1, 3});
        Assert.assertEquals(props[3].getVIndices(), new int[]{-1, -1, -1, 0});

        v.cancel(0, 4);
        Assert.assertEquals(v.propagators[0], props[2]);
        Assert.assertEquals(v.pindices[0], 2);
        Assert.assertEquals(v.propagators[1], props[0]);
        Assert.assertEquals(v.pindices[1], 0);
        Assert.assertEquals(v.propagators[2], props[1]);
        Assert.assertEquals(v.pindices[2], 1);
        Assert.assertEquals(props[0].getVIndices(), new int[]{1});
        Assert.assertEquals(props[1].getVIndices(), new int[]{-1, 2});
        Assert.assertEquals(props[2].getVIndices(), new int[]{-1, -1, 0});

        v.cancel(0, 4);
        Assert.assertEquals(v.propagators[0], props[1]);
        Assert.assertEquals(v.pindices[0], 1);
        Assert.assertEquals(v.propagators[1], props[0]);
        Assert.assertEquals(v.pindices[1], 0);
        Assert.assertEquals(props[0].getVIndices(), new int[]{1});
        Assert.assertEquals(props[1].getVIndices(), new int[]{-1, 0});

        v.cancel(0, 4);
        Assert.assertEquals(v.propagators[0], props[0]);
        Assert.assertEquals(v.pindices[0], 0);
        Assert.assertEquals(props[0].getVIndices(), new int[]{0});
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


}