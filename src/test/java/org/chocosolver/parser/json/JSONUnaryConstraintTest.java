/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

/**
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 12/09/2017.
 */
public class JSONUnaryConstraintTest extends JSONConstraintTest{

    @Test(groups="1s", timeOut=60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testArithm(String sign){
        Model model = new Model();
        IntVar x = model.intVar(2,6);
        model.arithm(x, sign, 3).post();
        eval(model, false);
    }


    @Test(groups="1s", timeOut=60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testArithmReify(String sign){
        Model model = new Model();
        IntVar x = model.intVar(2,6);
        model.arithm(x, sign, 4).reify();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testArithmReify2(String sign){
        Model model = new Model();
        IntVar x = model.intVar(2,6);
        model.arithm(x, sign, 4).reifyWith(model.boolVar(true));
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000, dataProviderClass = JSONConstraintTest.class, dataProvider = "sign")
    public void testArithmReify3(String sign){
        Model model = new Model();
        IntVar x = model.intVar(2,6);
        model.arithm(x, sign, 4).reifyWith(model.boolVar(false));
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMember1(){
        Model model = new Model();
        IntVar x = model.intVar(2,6);
        model.member(x, 4,5).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMember2(){
        Model model = new Model();
        IntVar x = model.intVar(2,6);
        model.notMember(x, 4,5).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMember3(){
        Model model = new Model();
        IntVar x = model.intVar(2,6);
        model.member(x, new int[]{3,5}).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMember4(){
        Model model = new Model();
        IntVar x = model.intVar(2,6);
        model.notMember(x, new int[]{3,5}).post();
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMember5(){
        Model model = new Model();
        IntVar x = model.intVar(2,6);
        model.notMember(x, new int[]{3,5}).reify();
        eval(model, false);
    }
}