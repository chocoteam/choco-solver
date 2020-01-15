/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.annotations.Test;

/**
 *
 * <p>
 * Project: choco-json.
 * @author Charles Prud'homme
 * @since 27/09/2017.
 */
public class JSONSetConstraintTest extends JSONConstraintTest{

    @Test(groups="1s", timeOut=60000)
    public void testAlldiff(){
        Model model = new Model();
        SetVar[] s = model.setVarArray(3, new int[]{}, new int[]{1,2});
        model.allDifferent(s).post();
        eval(model, false);
    }


    @Test(groups="1s", timeOut=60000)
    public void testAlldisjoint(){
        Model model = new Model();
        SetVar[] s = model.setVarArray(3, new int[]{}, new int[]{1,2});
        model.allDisjoint(s).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAllequal(){
        Model model = new Model();
        SetVar[] s = model.setVarArray(3, new int[]{}, new int[]{1,2});
        model.allEqual(s).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBoolChanneling(){
        Model model = new Model();
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        BoolVar[]b = model.boolVarArray(3);
        model.setBoolsChanneling(b, s).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testCard(){
        Model model = new Model();
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        IntVar i = model.intVar(0,3);
        s.setCard(i);
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testElement(){
        Model model = new Model();
        SetVar[] ss = model.setVarArray(3, new int[]{}, new int[]{1,2});
        IntVar i = model.intVar(0,12);
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        model.element(i, ss, s).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testIntChanneling(){
        Model model = new Model();
        IntVar[] intVars = model.intVarArray(5, 2, 6);
        SetVar[] setVars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4, 5});
        model.setsIntsChanneling(setVars, intVars, 1, 1).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000, dataProviderClass = JSONConstraintTest.class,
    dataProvider = "bool")
    public void testIntersection(boolean bool){
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(5, new int[]{2}, new int[]{1, 2, 3});
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        model.intersection(setVars, s, bool).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testIntUnion(){
        Model model = new Model();
        IntVar[] intVars = model.intVarArray(5, 2, 6);
        SetVar s = model.setVar(new int[]{}, new int[]{1,2,3});
        model.union(intVars, s).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testInverse(){
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(4, new int[]{}, new int[]{1, 2, 3});
        SetVar[] inverseSetVars = model.setVarArray(4, new int[]{}, new int[]{1, 2, 3});
        model.inverseSet(setVars, inverseSetVars, 0, 0).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000, dataProviderClass = JSONConstraintTest.class,
            dataProvider = "bool")
    public void testMax(boolean bool){
        Model model = new Model();
        IntVar i = model.intVar(0,8);
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        model.max(s, i, bool).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000, dataProviderClass = JSONConstraintTest.class,
            dataProvider = "bool")
    public void testMax2(boolean bool){
        Model model = new Model();
        IntVar i = model.intVar(0,8);
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        int[] ws = new int[]{2,3,1};
        model.max(s, ws, 0, i, bool).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMember(){
        Model model = new Model();
        IntVar i = model.intVar(0,8);
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        model.member(i, s).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMember2(){
        Model model = new Model();
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        model.member(1, s).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000, dataProviderClass = JSONConstraintTest.class,
            dataProvider = "bool")
    public void testMin(boolean bool){
        Model model = new Model();
        IntVar i = model.intVar(0,8);
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        model.min(s, i, bool).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000, dataProviderClass = JSONConstraintTest.class,
            dataProvider = "bool")
    public void testMin2(boolean bool){
        Model model = new Model();
        IntVar i = model.intVar(0,8);
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        int[] ws = new int[]{2,3,1};
        model.min(s, ws, 0, i, bool).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNbEmpty(){
        Model model = new Model();
        IntVar i = model.intVar(1,2);
        SetVar[] ss = model.setVarArray(4, new int[]{}, new int[]{1, 2, 3});
        model.nbEmpty(ss, i).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNotEmpty(){
        Model model = new Model();
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        model.notEmpty(s).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNotMember(){
        Model model = new Model();
        IntVar i = model.intVar(0,8);
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        model.notMember(i, s).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNotMember2(){
        Model model = new Model();
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        model.notMember(1, s).post();
        eval(model, false);
    }
    @Test(groups="1s", timeOut=60000)
    public void testOffset(){
        Model model = new Model();
        SetVar s1 = model.setVar(new int[]{}, new int[]{1,2});
        SetVar s2 = model.setVar(new int[]{}, new int[]{1,2});
        model.offSet(s1, s2, 1).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testPartition(){
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(3, new int[]{}, new int[]{1, 2, 3});
        SetVar s = model.setVar(new int[]{}, new int[]{2,3});
        model.partition(setVars, s).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSubseteq(){
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(3, new int[]{}, new int[]{1, 2, 3});
        model.subsetEq(setVars).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSumElement(){
        Model model = new Model();
        IntVar i = model.intVar(0,12);
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        int[] ws = new int[]{2,3,1};
        model.sumElements(s, ws, i).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSymmetric(){
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(3, new int[]{2}, new int[]{1, 2, 3});
        model.symmetric(setVars, 1).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testUnion(){
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(5, new int[]{2}, new int[]{1, 2, 3});
        SetVar s = model.setVar(new int[]{}, new int[]{1,2});
        model.union(setVars, s).post();
        eval(model, false);
    }

}
