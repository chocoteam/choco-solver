/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.limits.BacktrackCounter;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.testng.Assert.*;

/**
 * @author Jean-Guillaume Fages
 */
public class MinMaxTest {

    @DataProvider(name = "params")
    public Object[][] data1D(){
        // first boolean indicates whether it is minimization or maximization
        // second boolean indicates whether to use explanations or not
        List<Object[]> elt = new ArrayList<>();
        elt.add(new Object[]{true});
        elt.add(new Object[]{false});
        return elt.toArray(new Object[elt.size()][1]);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test(groups="1s", timeOut=60000, dataProvider = "params")
    public void testNominal(boolean min) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(3, -2, 3);
        IntVar mMvar = model.intVar(-3, 2);
        if(min){
            model.min(mMvar,vars).post();
        }else{
            model.max(mMvar,vars).post();
        }
        int nbSol = checkSolutions(vars, mMvar, min);

        // compare to scalar+arithm
        model = new Model();
        vars = model.intVarArray(3, -2, 3);
        mMvar = model.intVar(-3,2);
        model.element(mMvar,vars,model.intVar(0,vars.length-1),0).post();
        for(IntVar v:vars){
            model.arithm(v,min?">=":"<=",mMvar).post();
        }
        int nbSol2 = 0;
        model.getSolver().setSearch(inputOrderLBSearch(vars),inputOrderLBSearch(mMvar));
        while (model.getSolver().solve()) {
            nbSol2++;
        }
        assertEquals(nbSol, nbSol2);
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "params")
    public void testBools(boolean min) {
        int n1 = 0;
        {
            Model model = new Model();
            BoolVar[] vars = model.boolVarArray(3);
            BoolVar mMvar = model.boolVar();
            if (min) {
                model.min(mMvar, vars).post();
            } else {
                model.max(mMvar, vars).post();
            }
            n1 = checkSolutions(vars, mMvar, min);
        }
        int n2 = 0;
        {
            Model model = new Model();
            BoolVar[] vars = model.boolVarArray(3);
            BoolVar mMvar = model.boolVar();
            if (min) {
                model.min(mMvar, vars).post();
            } else {
                model.max(mMvar, vars).post();
            }
            n2 = checkSolutions(vars, mMvar, min);
        }
        System.out.println(n1);
        System.out.println(n2);
        assertEquals(n1, n2);
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "params")
    public void testBoolsGG(boolean min) {
        {
            Model m = new Model();
            IntVar[] vars = new BoolVar[2];
            for (int t = 0; t < vars.length; t++) {
                vars[t] = m.boolVar("x"+t);
            }
            IntVar mMvar = m.boolVar("max");
            if (min) {
                m.arithm(vars[0],"=",0).post();
                m.arithm(mMvar,"=",0).post();
                m.min(mMvar, vars).post();
            } else {
                m.arithm(vars[0],">",0).post();
                m.arithm(mMvar,">",0).post();
                m.max(mMvar, vars).post();
            }
            try {
                m.getSolver().propagate();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            assertEquals(vars[0].getValue(),min?0:1);
            assertTrue(!vars[1].isInstantiated());
        }
        {
            Model m = new Model();
            BoolVar[] vars = new BoolVar[2];
            for (int t = 0; t < vars.length; t++) {
                vars[t] = m.boolVar("x"+t);
            }
            BoolVar mMvar = m.boolVar("max");
            if (min) {
                m.arithm(vars[0],"=",0).post();
                m.arithm(mMvar,"=",0).post();
                m.min(mMvar, vars).post();
            } else {
                m.arithm(vars[0],">",0).post();
                m.arithm(mMvar,">",0).post();
                m.max(mMvar, vars).post();
            }
            try {
                m.getSolver().propagate();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            assertEquals(vars[0].getValue(),min?0:1);
            assertTrue(!vars[1].isInstantiated());
        }
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testNoSolution(boolean min) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 5);
        IntVar mM = model.intVar(26, 30);
        if(min) {
            model.min(mM,vars).post();
        }else{
            model.max(mM,vars).post();
        }
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testNoSolution2(boolean min) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray(5, 0, 5);
        IntVar mM = model.intVar(-26, -3);
        if(min) {
            model.min(mM,vars).post();
        }else{
            model.max(mM,vars).post();
        }
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testZero(boolean min) {
        Model model = new Model();
        IntVar[] vars = new IntVar[]{
                model.intVar(-5, -1),
                model.intVar(1, 5),
                model.intVar(-5, -1),
                model.intVar(1, 5)
        };
        IntVar mM = model.intVar(0);
        if(min) {
            model.min(mM,vars).post();
        }else{
            model.max(mM,vars).post();
        }
        checkSolutions(vars, mM, min);
    }


    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testSameVariableSolution(boolean min) {
        Model model = new Model();
        IntVar mM = model.intVar(1, 5);
        IntVar ref = model.intVar(1, 5);
        IntVar[] vars = new IntVar[]{ref, ref};
        if(min) {
            model.min(mM,vars).post();
        }else{
            model.max(mM,vars).post();
        }
        checkSolutions(vars, mM, min);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testSameVariableNoSolution(boolean min) {
        Model model = new Model();
        IntVar mM = model.intVar(0);
        IntVar ref = model.intVar(1, 5);
        IntVar[] vars = new IntVar[]{ref, ref};
        if(min) {
            model.min(mM,vars).post();
        }else{
            model.max(mM,vars).post();
        }
        assertFalse(model.getSolver().solve());
    }


    @Test(groups = "1s", timeOut=60000, dataProvider = "params", expectedExceptions = AssertionError.class)
    public void testZeroElements(boolean min) {
        Model model = new Model();
        IntVar mM = model.intVar(-1,1);
        IntVar[] vars = new IntVar[0];
        if(min) {
            model.min(mM,vars).post();
        }else{
            model.max(mM,vars).post();
        }
        assertEquals(checkSolutions(vars, mM, min), 1);
    }

    private int checkSolutions(IntVar[] intVars, IntVar sum, boolean minOrMax) {
        Model model = sum.getModel();
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for(IntVar v:intVars){
                min = Math.min(min, v.getValue());
                max = Math.max(max, v.getValue());
            }
            int target = minOrMax?min:max;
            assertTrue(target == sum.getValue());
        }
        return nbSol;
    }

    @Test(groups="1s", timeOut=60000)
    public void testMin1(){
        Model model = new Model();
        BoolVar[] bvars = model.boolVarArray("b",2);
        BoolVar target = model.boolVar("t");

//        model.sum(bvars, "=", bvars.length).reifyWith(target);
        model.min(target, bvars).post();
        Solver solver = model.getSolver();
        try {
            solver.propagate();
            model.getEnvironment().worldPush();
            bvars[0].instantiateTo(1, Cause.Null);
            solver.propagate();
            model.getEnvironment().worldPush();
            bvars[1].instantiateTo(1, Cause.Null);
            solver.propagate();
            Assert.assertTrue(target.isInstantiatedTo(1));
            model.getEnvironment().worldPop();
            bvars[1].instantiateTo(1, Cause.Null);
            solver.propagate();
            Assert.assertTrue(target.isInstantiatedTo(1));

        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testMin2(){
        Model model = new Model();
        BoolVar[] bvars = model.boolVarArray("b",2);
        BoolVar target = model.boolVar("t");
        Random rnd = new Random(0);
//        model.sum(bvars, "=", bvars.length).reifyWith(target);
        model.min(target, bvars).post();
        Solver solver = model.getSolver();
        try {
            int v;
            solver.propagate();
            for(int i = 0; i< 10000; i++){
                model.getEnvironment().worldPush();
                switch (rnd.nextInt(3)){
                    case 0:
                        bvars[0].instantiateTo(rnd.nextInt(2), Cause.Null);
                        solver.propagate();
                        bvars[1].instantiateTo(rnd.nextInt(2), Cause.Null);
                        solver.propagate();
                        break;
                    case 1:
                        v = rnd.nextInt(2);
                        bvars[0].instantiateTo(v, Cause.Null);
                        solver.propagate();
                        if(v == 1) {
                            target.instantiateTo(rnd.nextInt(2), Cause.Null);
                            solver.propagate();
                        }
                        break;
                    case 2:
                        v = rnd.nextInt(2);
                        bvars[1].instantiateTo(v, Cause.Null);
                        solver.propagate();
                        if(v == 1) {
                            target.instantiateTo(rnd.nextInt(2), Cause.Null);
                            solver.propagate();
                        }
                        break;
                }
                Assert.assertEquals(solver.isSatisfied(), ESat.TRUE);
                model.getEnvironment().worldPop();
            }
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }


    @Test(groups="1s", timeOut=60000)
    public void testMin3(){
        int n = 10;
        for(int i = 0; i < n; i++) {
            Model model = new Model();
            BoolVar[] bvars = new BoolVar[n];
            for(int j = 0; j < n; j++){
                bvars[j] = i == j ? model.boolVar(): model.boolVar(true);
            }
            BoolVar target = model.boolVar(false);

            model.min(target, bvars).post();
            Solver solver = model.getSolver();
            try {
                solver.propagate();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertTrue(bvars[i].isInstantiatedTo(0));
        }
    }


    @Test(groups="1s", timeOut=60000)
    public void testMax1(){
        Model model = new Model();
        BoolVar[] bvars = model.boolVarArray("b",2);
        BoolVar target = model.boolVar("t");

//        model.sum(bvars, "=", bvars.length).reifyWith(target);
        model.max(target, bvars).post();
        Solver solver = model.getSolver();
        try {
            solver.propagate();
            model.getEnvironment().worldPush();
            bvars[0].instantiateTo(0, Cause.Null);
            solver.propagate();
            model.getEnvironment().worldPush();
            bvars[1].instantiateTo(0, Cause.Null);
            solver.propagate();
            Assert.assertTrue(target.isInstantiatedTo(0));
            model.getEnvironment().worldPop();
            bvars[1].instantiateTo(0, Cause.Null);
            solver.propagate();
            Assert.assertTrue(target.isInstantiatedTo(0));

        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testMax2(){
        Model model = new Model();
        BoolVar[] bvars = model.boolVarArray("b",2);
        BoolVar target = model.boolVar("t");
        Random rnd = new Random(0);
//        model.sum(bvars, "=", bvars.length).reifyWith(target);
        model.max(target, bvars).post();
        Solver solver = model.getSolver();
        try {
            int v;
            solver.propagate();
            for(int i = 0; i< 10000; i++){
                model.getEnvironment().worldPush();
                switch (rnd.nextInt(3)){
                    case 0:
                        bvars[0].instantiateTo(rnd.nextInt(2), Cause.Null);
                        solver.propagate();
                        bvars[1].instantiateTo(rnd.nextInt(2), Cause.Null);
                        solver.propagate();
                        break;
                    case 1:
                        v = rnd.nextInt(2);
                        bvars[0].instantiateTo(v, Cause.Null);
                        solver.propagate();
                        if(v == 0) {
                            target.instantiateTo(rnd.nextInt(2), Cause.Null);
                            solver.propagate();
                        }
                        break;
                    case 2:
                        v = rnd.nextInt(2);
                        bvars[1].instantiateTo(v, Cause.Null);
                        solver.propagate();
                        if(v == 0) {
                            target.instantiateTo(rnd.nextInt(2), Cause.Null);
                            solver.propagate();
                        }
                        break;
                }
                Assert.assertEquals(solver.isSatisfied(), ESat.TRUE, ""+i);
                model.getEnvironment().worldPop();
            }
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testMax3(){
        int n = 10;
        for(int i = 0; i < n; i++) {
            Model model = new Model();
            BoolVar[] bvars = new BoolVar[n];
            for(int j = 0; j < n; j++){
                bvars[j] = i == j ? model.boolVar(): model.boolVar(false);
            }
            BoolVar target = model.boolVar(true);

            model.max(target, bvars).post();
            Solver solver = model.getSolver();
            try {
                solver.propagate();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertTrue(bvars[i].isInstantiatedTo(1));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testMax30(){
        int l = 299;
        int s = 17;
        Random rnd = new Random(0L);
        for(int n = 2; n < s; n++) {
            for (int i = 0; i < l; i++) {
                rnd.setSeed(i * s + n);
                Settings settings = new DefaultSettings().setWarnUser(false);
                Model model = new Model(settings);
                BoolVar[] bvars = new BoolVar[n];
                for (int j = 0; j < n; j++) {
                    bvars[j] = DomainBuilder.makeBoolVar(model, rnd, j);
                }
                BoolVar target = DomainBuilder.makeBoolVar(model, rnd, n+1);

                model.max(target, bvars).post();
                Solver solver = model.getSolver();
                long nbsol = solver.findAllSolutions().size();
                solver.reset();
                solver.setLubyRestart(4, new BacktrackCounter(model, 0), 100);
                solver.setNoGoodRecordingFromRestarts();
                Assert.assertEquals(solver.findAllSolutions().size(), nbsol);
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testMin30(){
        int l = 299;
        int s = 17;
        Random rnd = new Random(0L);
        for(int n = 2; n < s; n++) {
            for (int i = 0; i < l; i++) {
                rnd.setSeed(i * s + n);
                Settings settings = new DefaultSettings().setWarnUser(false);
                Model model = new Model(settings);
                BoolVar[] bvars = new BoolVar[n];
                for (int j = 0; j < n; j++) {
                    bvars[j] = DomainBuilder.makeBoolVar(model, rnd, j);
                }
                BoolVar target = DomainBuilder.makeBoolVar(model, rnd, n+1);

                model.min(target, bvars).post();
                Solver solver = model.getSolver();
                long nbsol = solver.findAllSolutions().size();
                solver.reset();
                solver.setLubyRestart(4, new BacktrackCounter(model, 0), 100);
                solver.setNoGoodRecordingFromRestarts();
                Assert.assertEquals(solver.findAllSolutions().size(), nbsol);
            }
        }
    }

}
